package models.intelligence

import models.graph.concept.{Concept, ConceptDAO}
import models.interaction.action.{InstanceAction, InstanceActionDAO}
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

/**
 * Object representing the means to satisfy a need
 * @param action mean for instance to satisfy their need
 * @param destinationConcept destination of the action
 */
case class MeanOfSatisfaction(action: InstanceAction, destinationConcept: Concept) {

  val destinationConcepts = destinationConcept :: destinationConcept.descendance

  override def toString = action.id + " -> " + destinationConcept.id

  def toJson : JsValue = Json.obj(
    "action" -> action.id,
    "concept" -> destinationConcept.id
  )
}

object MeanOfSatisfaction {

  val error = MeanOfSatisfaction(InstanceAction.error, Concept.error)

  def parseList(string: String): List[MeanOfSatisfaction] = Try{
    if(string != ""){
      string.split(";").map(parseString).toList
    }else{
      List()
    }
  } match {
    case Success(list) => list
    case Failure(e) =>
      Console.println("Error while parsing list of MeansOfSatisfaction:")
      Console.println("e")
      List()
  }

  def parseString(string: String): MeanOfSatisfaction = Try {
    val splitted = string.split(" -> ")
    val action = InstanceActionDAO.getById(splitted(0).toLong)
    val destinationsConcept = {
      if (splitted.length == 1){
        Concept.error
      }
      else {
        ConceptDAO.getById(splitted(1).toLong)
      }
    }
    MeanOfSatisfaction(action, destinationsConcept)
  } match {
    case Success(m) => m
    case Failure(e) =>
      Console.println("Error while parsing a mean of satisfaction")
      Console.println(e)
      error
  }
}
