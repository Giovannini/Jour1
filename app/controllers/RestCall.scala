package controllers

import models.graph.ontology.concept.{ConceptDAO, Concept}
import models.graph.ontology.relation.Relation
import models.graph.ontology.Instance
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Request}
import models.instance_action.action.{InstanceAction, ActionParser}

/**
 * Controller to send Json data to client
 */
object RestCall extends Controller {

  /**
   * Get all the concepts existing in the graph
   * @author Thomas GIOVANNINI
   */
  def getAllConcepts = Action {
    val concepts = ConceptDAO.getAll
      .map(_.toJson)
    Ok(Json.toJson(concepts))
  }

  /**
   * Get all the relation existing for a given concept except for its instances
   * @author Thomas GIOVANNINI
   * @param conceptId of the concept the relations are desired
   */
  def getAllActionsOf(conceptId: Long) = Action {
    val t1 = System.currentTimeMillis()
    val relations = ConceptDAO.getReachableRelations(conceptId)
    val actions = relations.filter(_._1.isAnAction)
      .map(relationnedConceptToJson)
    val t2 = System.currentTimeMillis()
    println("Getting all actions took " + (t2 - t1) + "ms.")
    Ok(Json.toJson(actions))
  }

  /**
   * Parse a relationned concept to Json
   * @author Thomas GIOVANNINI
   * @param tuple containing the relationned concept
   * @return a JsValue representing the relationned concept
   */
  def relationnedConceptToJson(tuple: (Relation, Concept)): JsValue = {
    Json.obj( "relationID" -> JsNumber(tuple._1.id),
              "relationLabel" -> JsString(tuple._1.label),
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
      val actionReference = (jsonRequest \ "action").as[Long]
      val actionId = Relation.DBList.getActionIdFromRelationId(actionReference)
      val actionArguments = (jsonRequest \ "instances").as[List[Long]]
      val result = ActionParser.parseAction(actionId, actionArguments)
      result
    }

    val t1 = System.currentTimeMillis()
    val result = execution(request)
    val t2 = System.currentTimeMillis()
    println("Executing action took " + (t2 - t1) + "ms.")
    if(result) Ok("Ok")
    else Ok("Error while executing this action")
  }

  /**
   * Get all the instances that can be destination of a given action with its preconditions.
   * json model: {action: "action", instance: instanceID, concept: conceptID}
   * @author Thomas GIOVANNINI
   * @return a list of instances under JSON format
   */
  def getPossibleDestinationOfAction(initInstanceId: Long, relationId: Long, conceptId: Long) = Action {
    val sourceInstance = Application.map.getInstanceById(initInstanceId)
    val actionID = Relation.DBList.getActionIdFromRelationId(relationId)
    val action = ActionParser.getAction(actionID)
    val destinationInstancesList = Application.map.getInstancesOf(conceptId)
    if (sourceInstance == Instance.error || action == InstanceAction.error){
      Ok(Json.arr())
    }
    val t1 = System.currentTimeMillis()
    val reducedList = action.getDestinationList(sourceInstance, destinationInstancesList)
      .map(_.toJson)
    val t2 = System.currentTimeMillis()
    println("Getting destinations took " + (t2 - t1) + "ms.")
    Ok(Json.toJson(reducedList))
  }

  def editInstance(instanceId: Long) = Action {
    val instance = Application.map.getInstanceById(instanceId)
    Ok(views.html.manager.instance.instanceEditor(instance, controllers.ontology.routes.InstanceManager.update()))
  }

  def createInstance(conceptId: Long) = Action {
    val concept = ConceptDAO.getById(conceptId)
    val instance = Instance.createRandomInstanceOf(concept)
    Ok(views.html.manager.instance.instanceEditor(instance, controllers.ontology.routes.InstanceManager.create()))
  }

  def getBestAction(instanceID: Long) = Action {
    val t1 = System.currentTimeMillis()
    val instance = Application.map.getInstanceById(instanceID)
    val bestAction = instance.selectAction
    val t2 = System.currentTimeMillis()
    println("Getting best action took " + (t2 - t1) + "ms.")
    Ok("Best action for instance " + instanceID + " - " + instance.label + "\n" + bestAction.label)
  }
}
