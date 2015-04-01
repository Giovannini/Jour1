package controllers

import models.Intelligence
import models.graph.ontology.Instance
import models.graph.ontology.concept.{Concept, ConceptDAO}
import models.graph.ontology.relation.{RelationDAO, Relation}
import models.interaction.action.{InstanceActionParser, InstanceAction}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, Controller, Request}

/**
 * Controller to send Json data to client
 */
object RestCall extends Controller {

  /**
   * Get all the concepts existing in the graph
   * @author Thomas GIOVANNINI
   */
  def getAllConcepts: Action[AnyContent] = Action {
    val concepts = ConceptDAO.getAll
      .map(_.toJson)
    Ok(Json.toJson(concepts))
  }

  def getAllSimplifiedConcepts() = Action {
    val concepts = ConceptDAO.getAllSimlified
      .map(_.toSimplifiedJson)
    Ok(Json.toJson(concepts))
  }

  /**
   * Get a concept by its id
   * @author Aurélie LORGEOUX
   * @param conceptId id of the concept
   * @return concept in JSON
   */
  def getConceptById(conceptId: Long): Action[AnyContent] = Action {
    val concept = ConceptDAO.getById(conceptId)
    Ok(concept.toJson)
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
      val actionId = RelationDAO.getActionIdFromRelationId(actionReference)
      val actionArguments = (jsonRequest \ "instances").as[List[Long]]
      InstanceActionParser.parseAction(actionId, actionArguments)
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
  def getPossibleDestinationOfAction(initInstanceId: Long, relationId: Long, conceptId: Long)
  : Action[AnyContent] = Action {
    val sourceInstance = Application.map.getInstanceById(initInstanceId)
    val actionID = RelationDAO.getActionIdFromRelationId(relationId)
    val action = InstanceActionParser.getAction(actionID)
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

  def editInstance(instanceId: Long): Action[AnyContent] = Action {
    val instance = Application.map.getInstanceById(instanceId)
    Ok(views.html.manager.instance.instanceEditor(instance, controllers.ontology.routes.InstanceManager.update()))
  }

  def createInstance(conceptId: Long): Action[AnyContent] = Action {
    val concept = ConceptDAO.getById(conceptId)
    val instance = Instance.createRandomInstanceOf(concept)
    Ok(views.html.manager.instance.instanceEditor(instance, controllers.ontology.routes.InstanceManager.create()))
  }

  /**
   * Get an instance with its id
   * @author Aurélie LORGEOUX
   * @param instanceId id of the instance
   * @return instance in JSON
   */
  def getInstanceById(instanceId: Long) = Action {
    val instance = Application.map.getInstanceById(instanceId)
    Ok(instance.toJson)
  }

  def getBestAction(instanceID: Long): Action[AnyContent] = Action {
    val t1 = System.currentTimeMillis()
    val instance = Application.map.getInstanceById(instanceID)
    val bestAction = instance.selectAction(instance.getSensedInstances.flatMap(Application.map.getInstancesAt))
    val t2 = System.currentTimeMillis()
    println("Getting best action took " + (t2 - t1) + "ms.")
    Ok("Best action for instance " + instanceID + " - " + instance.label + "\n" +
       bestAction._1.label + " to instance " + bestAction._2.id + " - " + bestAction._2.label)
  }

  def next() = Action {
    Intelligence.calculate(nrOfWorkers = 4)
    Redirect(routes.MapController.show())
  }

}
