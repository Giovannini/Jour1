package controllers

import models.graph.ontology.{Instance, Relation, Concept}
import models.utils.precondition.Precondition
import play.api.libs.json._
import play.api.mvc.{Controller, Action}
import models.utils.action.{Action => InstanceAction}

/**
 * Controller to send Json data to client
 */
object RestCall extends Controller{

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
  def getAllRelationsOf(conceptId: Int) = Action {
    val relations = Concept.getReachableRelations(conceptId)
      .map(relationnedConceptToJson)
    
    Ok(Json.toJson(relations))
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
   */
  def executeAction = Action(parse.json) { request =>
    println("Received JSON: " + request.body)
    val jsonRequest = Json.toJson(request.body)
    val actionReference = (jsonRequest \ "action").as[String]
    val actionArguments = (jsonRequest \ "instances").as[List[Int]]
    val result = Application.actionParser.parseAction(actionReference, actionArguments)
    if(result) Ok("Ok")
    else Ok("Error while executing this action")
  }

  /**
   * Get all the instances that can be destination of a given action with its preconditions.
   * json model: {action: "action", instance: instanceID, concept: conceptID}
   * @author Thomas GIOVANNINI
   * @return a list of instances under JSON format
   */
  def getPossibleDestinationOfAction = Action(parse.json) { request =>
    val jsonRequest = Json.toJson(request.body)
    val sourceInstance = Application.map.getInstanceById((jsonRequest \ "instance").as[Int])
    val action = Application.actionParser.getAction((jsonRequest \ "action").as[String])
    val destinationInstancesList = Application.map.getInstancesOf((jsonRequest \ "concept").as[Int])
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
    //println(sourceInstance.coordinates)
    val reducedInstanceList = instances.filter { instance =>
      preconditionsToValidate.forall(validateConditionFor(_, action, sourceInstance, instance))
    }
    //println(reducedInstanceList.map(_.coordinates).mkString(", "))
    reducedInstanceList.map(_.toJson)
  }

  /**
   * Validate or invalidate a precondition between two instances
   * @author Thomas GIOVANNINI
   * @param precondition to validate
   * @param action from which the precondition is
   * @param sourceInstance of the action
   * @param destInstance of the action
   * @return true if te precondition is validated
   *         false else
   */
  def validateConditionFor(precondition: Precondition, action: InstanceAction, sourceInstance: Instance, destInstance: Instance) = {
    val arguments = Application.actionParser.getArgumentsList(action, List(sourceInstance.id, destInstance.id))
    Application.preconditionManager.isFilled(precondition, arguments)
  }
}
