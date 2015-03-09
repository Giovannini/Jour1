package controllers

import models.graph.ontology.{Instance, Relation, Concept}
import models.utils.precondition.Precondition
import play.api.libs.json._
import play.api.mvc.{Request, Controller, Action}
import models.utils.action.{Action => InstanceAction}

/**
 * Controller to send Json data to client
 */
object RestCall extends Controller {

  /**
   * Get all the concepts existing in the graph
   * @author Thomas GIOVANNINI
   */
  def getAllConcepts = Action {
    val concepts = Concept.findAll
      .map(_.toJson)
    Ok(Json.toJson(concepts))
  }

  /**
   * Get all the relation existing for a given concept except for its instances
   * @author Thomas GIOVANNINI
   * @param conceptId of the concept the relations are desired
   */
  def getAllActionsOf(conceptId: Int) = Action {
    val relations = Concept.getReachableRelations(conceptId)
    val actions = relations.filter(_._1.isAnAction)
      .map(relationnedConceptToJson)
    Ok(Json.toJson(actions))
  }

  /**
   * Parse a relationned concept to Json
   * @author Thomas GIOVANNINI
   * @param tuple containing the relationned concept
   * @return a JsValue representing the relationned concept
   */
  def relationnedConceptToJson(tuple: (Relation, Concept)): JsValue = {
    Json.obj( "relation" -> JsString(tuple._1.toString),
              "conceptId" -> JsNumber(tuple._2.hashCode()))
  }

  /**
   * Receive a json action and execute it server side, then actualize the client-side.
   * json model: {action: "action", instances: [instance, instance, ...]}
   * @author Thomas GIOVANNINI
   */
  def executeAction = Action(parse.json) { request =>
    /**
     * Parse json request and execute it.
     * @author Thomas GIOVANNINI
     */
    def execution(request: Request[JsValue]): Boolean = {
      val jsonRequest = Json.toJson(request.body)
      val actionReference = (jsonRequest \ "action").as[String]
      val actionArguments = (jsonRequest \ "instances").as[List[Int]]
      Application.actionParser.parseAction(actionReference, actionArguments)
    }

    val result = execution(request)
    if(result) Ok("Ok")
    else Ok("Error while executing this action")
  }

  /**
   * Get all the instances that can be destination of a given action with its preconditions.
   * json model: {action: "action", instance: instanceID, concept: conceptID}
   * @author Thomas GIOVANNINI
   * @return a list of instances under JSON format
   */
  def getPossibleDestinationOfAction(initInstanceId: Int, actionType: String, conceptId: Int) = Action {
    val sourceInstance = Application.map.getInstanceById(initInstanceId)
    val action = Application.actionParser.getAction(actionType)
    val destinationInstancesList = Application.map.getInstancesOf(conceptId)
    val reducedList = reduceDestinationList(sourceInstance, action, destinationInstancesList)
    Ok(Json.toJson(reducedList))
  }

  /**
   * Get the instances that validate all the preconditions of a given action
   * @author Thomas GIOVANNINI
   * @param sourceInstance the source of the action
   * @param action from which the preconditions should be validated
   * @param instances list of instances to validate
   * @return a list of instances under JSON format
   */
  def reduceDestinationList(sourceInstance: Instance, action: InstanceAction, instances: List[Instance]) = {
    val preconditionsToValidate = action.preconditions
    val reducedInstanceList = reduceInstanceList(instances, preconditionsToValidate, action, sourceInstance)
    reducedInstanceList.map(_.toJson)
  }

  def reduceInstanceList(instances: List[Instance], preconditions: List[Precondition], action: InstanceAction,
                         sourceInstance: Instance): List[Instance] = {
    preconditions match {
      case precondition::tail =>
        val validatedInstances = instances.filter(validateConditionFor(precondition, action, sourceInstance, _))
        reduceInstanceList (validatedInstances, tail, action, sourceInstance)
      case _ => instances
    }
  }

  /**
   * Validate or invalidate a precondition between two instances
   * @author Thomas GIOVANNINI
   * @param precondition to validate
   * @param action from which the precondition is
   * @param source of the action
   * @param destination of the action
   * @return true if te precondition is validated
   *         false else
   */
  def validateConditionFor(precondition: Precondition, action: InstanceAction, source: Instance, destination: Instance) = {
    val arguments = Application.actionParser.getArgumentsList(action, List(source.id, destination.id))
    precondition.isFilled(arguments, Application.map)
  }

  def editInstance(instanceId: Int) = Action {
    val instance = Application.map.getInstanceById(instanceId)
    Ok(views.html.manager.instance.instanceEditor(instance))
  }
}
