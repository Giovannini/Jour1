package models.instance_action.action

import anorm.SqlParser._
import anorm._
import controllers.Application
import models.graph.ontology.Instance
import models.instance_action.parameter.{Parameter, ParameterReference, ParameterValue}
import models.instance_action.precondition.Precondition
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
 * Model of rule for persistence
 * @author Aurélie LORGEOUX
 * @param id primary key auto-increment
 * @param label name of the rule
 * @param preconditions preconditions for the function
 * @param subActions content of the rule
 * @param parameters parameters for the function
 */
case class InstanceAction(
  id: Long,
  label: String,
  preconditions: List[(Precondition, Map[ParameterReference, Parameter])],
  subActions: List[(InstanceAction, Map[ParameterReference, Parameter])],
  parameters: List[ParameterReference]) {


  /**
   * Modify ID of the action
   * @author Thomas GIOVANNINI
   * @param newId that will be given to the action
   * @return a new InstanceAction looking like this one but with a new ID
   */
  def withId(newId: Long): InstanceAction = {
    InstanceAction(newId, this.label, this.preconditions, this.subActions, this.parameters)
  }


  /*#################
    Json parsing
  #################*/
  /**
   * Parse the instance action to a Json object
   * @author Thomas GIOVANNINI
   * @return a json object representing the instance action
   */
  def toJson: JsValue = {
    Json.obj(
      "id" -> JsNumber(id),
      "label" -> JsString(label),
      "preconditions" -> preconditions.map(item => item._1.toJsonWithParameters(item._2)),
      "subActions" -> subActions.map(item => item._1.toJsonWithParameters(item._2)),
      "parameters" -> parameters.map(_.toJson)
    )
  }

  def toJsonWithParameters(referenceToParameter: Map[ParameterReference, Parameter]): JsValue = {
    Json.obj(
      "id" -> JsNumber(id),
      "label" -> JsString(label),
      "parameters" -> referenceToParameter.map(item => {
        Parameter.toJsonWithIsParam(item._1, item._2)
      })
    )
  }


  /*#################
    DB interactions
  #################*/
  /**
   * Save the action to database
   * @author Thomas GIOVANNINI
   * @return its ID
   */
  def save: InstanceAction = {
    InstanceAction.save(this)
  }


  /*#################
    Executions
  #################*/
  /**
   * Get the instances that validate all the preconditions of a given action
   * @author Thomas GIOVANNINI
   * @param sourceInstance the source of the action
   * @param instances list of instances to validate
   * @return a list of instances under JSON format
   */
  def getDestinationList(sourceInstance: Instance, instances: List[Instance]): List[Instance] = {
    val result = preconditions
      .map(_._1.instancesThatFill(sourceInstance, instances))
      .foldRight(instances.toSet)(_ intersect _)
      .toList
    result
  }

  /**
   * Check if all the action's preconditions are filled before executing it.
   * @param arguments to use to check those preconditions
   * @return true if all the preconditions are filled
   *         false else
   */
  def checkPreconditions(arguments: Map[ParameterReference, ParameterValue]): Boolean = {
    preconditions.forall(item => item._1.isFilled(item._2, arguments))
  }

  /**
   * Execute a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def execute(arguments: Map[ParameterReference, ParameterValue]): Boolean = {
    val preconditionCheck = checkPreconditions(arguments)

    if (preconditionCheck) {
      this.label match {
        /*case "createInstance" =>
          HardCodedAction.createInstance(arguments)
          true*/
        case "addInstanceAt" =>
          HardCodedAction.addInstanceAt(arguments)
          true
        case "removeInstanceAt" =>
          HardCodedAction.removeInstanceAt(arguments)
          true
        case "addToProperty" =>
          HardCodedAction.addToProperty(arguments)
          true
        case "modifyProperty" =>
          HardCodedAction.modifyProperty(arguments)
          true
        case _ =>
          subActions.forall(subAction => subAction._1.execute(takeGoodArguments(subAction._2, arguments)))
      }
    } else {
      println("Precondition not filled for action " + this.label + ".")
      false
    }
  }

  /**
   * LOG a given action with given arguments
   * @author Thomas GIOVANNINI
   * @param arguments with which execute the action
   * @return true if the action was correctly executed
   *         false else
   */
  def log(arguments: Map[ParameterReference, ParameterValue]): List[LogAction] = {
    val preconditionCheck = checkPreconditions(arguments)

    if (preconditionCheck) {
      this.label match {
        case "addInstanceAt" =>
          val instanceId = arguments(ParameterReference("instanceToAdd", "Long")).value.asInstanceOf[Long]
          val groundId = arguments(ParameterReference("groundWhereToAddIt", "Long")).value.asInstanceOf[Long]
          List(LogAction("ADD " + instanceId + " " + groundId))
        case "removeInstanceAt" =>
          val instanceId = arguments(ParameterReference("instanceToRemove", "Long")).value.asInstanceOf[Long]
          List(LogAction("REMOVE " + instanceId))
        case "addToProperty" =>
          val instanceId = arguments(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
          val propertyString = arguments(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]
          val valueToAdd = arguments(ParameterReference("propertyValue", "Property")).value.asInstanceOf[Double]
          List(LogAction("ADD_TO_PROPERTY " + instanceId + " " + propertyString + " " + valueToAdd))
        case "modifyProperty" =>
          val instanceId = arguments(ParameterReference("instanceID", "Long")).value.asInstanceOf[Long]
          val propertyString = arguments(ParameterReference("propertyName", "Property")).value.asInstanceOf[String]
          val newValue = arguments(ParameterReference("propertyValue", "Property")).value.asInstanceOf[Double]
          List(LogAction("MODIFY_PROPERTY " + instanceId + " " + propertyString + " " + newValue))
        case _ =>
          subActions.flatMap(subAction => subAction._1.log(takeGoodArguments(subAction._2, arguments)))
      }
    } else {
      println("Precondition not filled for action " + this.label + ".")
      List(LogAction.nothing)
    }
  }

  /**
   * Take the good argument list from the list of arguments of sur-action
   * @author Thomas GIOVANNINI
   * @return a reduced argument list
   */
  def takeGoodArguments(parameters: Map[ParameterReference, Parameter], arguments: Map[ParameterReference, ParameterValue]): Map[ParameterReference, ParameterValue] = {
    parameters.mapValues {
      case reference: ParameterReference => arguments(reference.asInstanceOf[ParameterReference])
      case value: ParameterValue => value.asInstanceOf[ParameterValue]
      case e: Parameter =>
        println("Failed to match parameter " + e)
        ParameterValue.error
    }.filter(_._2 != ParameterValue.error)
  }
}

/**
 * Model for rule
 */
object InstanceAction {

  implicit val connection = Application.connection

  val error = InstanceAction(-1, "error", List(), List(), List())

  /*######################
    Parsing
  ######################*/
  /**
   * Parse an action from strings
   * @param id of the action
   * @param label of the action
   * @param parametersToParse to retrieve real parameters of the action
   * @param preconditionsToParse to retrieve real preconditions of the action
   * @param subActionsToParse to retrieve real sub-actions of the action
   * @return the corresponding action
   */
  def parse(id: Long, label: String, parametersToParse: String, preconditionsToParse: String, subActionsToParse: String)
  : InstanceAction = {
    InstanceAction(
      id,
      label,
      Precondition.parseSubConditions(preconditionsToParse),
      parseSubActions(subActionsToParse),
      Parameter.parseParameters(parametersToParse)
    )
  }

  def parseSubActions(subActionsToParse: String): List[(InstanceAction, Map[ParameterReference, Parameter])] = {
    if (subActionsToParse != "") {
      subActionsToParse.split(";")
        .map(s => parseSubAction(s))
        .toList
    } else {
      List()
    }
  }

  def parseSubAction(subActionToParse: String): (InstanceAction, Map[ParameterReference, Parameter]) = {
    val globalPattern = "(^\\d*|\\(.*\\)$)".r
    val result = globalPattern.findAllIn(subActionToParse).toArray

    // Get the precondition
    val id = result(0).toInt
    val action = InstanceAction.getById(id)

    // Set the map of parameters
    val paramPattern = "([^\\(|,|\\)]+)".r
    val params = paramPattern.findAllIn(result(1)).toList.map(Parameter.parse)

    (action, Parameter.linkParameterToReference(action.parameters, params))
  }

  def retrieveFromStringOfIds(stringOfIds: String): List[InstanceAction] = {
    Try {
      stringOfIds.split(";")
        .map { id =>
        getById(id.toLong)
      }.toList
    } match {
      case Success(list) => list
      case Failure(e) =>
        println("Error occured while executing function retrieveFromStringOfIds in class InstanceAction")
        println(e)
        List()
    }
  }

  /**
   * Parse an action of an instance from database
   * @author Thomas GIOVANNINI
   */
  private val actionParser: RowParser[InstanceAction] = {
    get[Long]("id") ~
    get[String]("label") ~
    get[String]("param") ~
    get[String]("precond") ~
    get[String]("content") map {
      case id ~ label ~ param ~ precond ~ content => InstanceAction.parse(id, label, param, precond, content)
    }
  }

  /**
   * Clear the database
   * @author Aurélie LORGEOUX
   * @return number of rules deleted
   */
  def clearDB: Int = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.clearDB
      statement.executeUpdate
    }
  }

  /**
   * Get all rules saved in database
   * @author Aurélie LORGEOUX
   * @return all rules
   */
  def getAll: List[InstanceAction] = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.getAll
      statement.as(actionParser *)
    }
  }

  /**
   * Save rule in database
   * @author Aurélie LORGEOUX
   * @param action rule to put in the database
   * @return true if the rule saved
   *         false else
   */
  def save(action: InstanceAction): InstanceAction = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.add(action)
      val optionId: Option[Long] = statement.executeInsert()
      val id = optionId.getOrElse(-1L)
      action.withId(id)
    }
  }

  /**
   * Get one rule saved in database with its id
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @return rule identified by id
   */
  def getById(id: Long): InstanceAction = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.get(id)
      statement.as(actionParser.singleOpt).getOrElse(InstanceAction.error)
    }
  }

  /**
   * Get one rule saved in database with its name
   * @author Thomas GIOVANNINI
   * @param name of the rule
   * @return rule identified by id
   */
  def getByName(name: String): InstanceAction = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.getByName(name)
      statement.as(actionParser.singleOpt).getOrElse(InstanceAction.error)
    }
  }

  /**
   * Update a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   * @param action rule identified by id
   */
  def update(id: Long, action: InstanceAction): Int = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.set(id, action)
      statement.executeUpdate
    }
  }

  /**
   * Delete a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   */
  def delete(id: Long): Int = {
    DB.withConnection { implicit connection =>
      val statement = InstanceActionStatement.remove(id)
      statement.executeUpdate
    }
  }

}


