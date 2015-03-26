package models.graph.ontology.concept.need

import models.graph.ontology.concept.{ConceptDAO, Concept}
import models.instance_action.action.InstanceAction
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

/**
 * Created by giovannini on 3/25/15.
 */
case class MeanOfSatisfaction(action: InstanceAction, destinationConcepts: List[Concept]) {

  override def toString = action.id + " -> " + destinationConcepts.map(_.id).mkString(",")

  def toJson : JsValue = Json.obj(
    "action" -> action.id,
    "destinationConcepts" -> destinationConcepts.map(_.id)
  )
}

object MeanOfSatisfaction {

  val error = MeanOfSatisfaction(InstanceAction.error, List())

  def parseList(string: String): List[MeanOfSatisfaction] = Try{
    string.split(";").map(parseString).toList
  } match {
    case Success(list) => list
    case Failure(e) =>
      println("Error while parsing list of MeansOfSatisfaction:")
      println("e")
      List()
  }

  def parseString(string: String): MeanOfSatisfaction = Try {
    val splitted = string.split(" -> ")
    val action = InstanceAction.getById(splitted(0).toLong)
    val destinationsConcept = {
      if (splitted.length == 1){
        List()
      }
      else splitted(1).split(",")
        .map(id => ConceptDAO.getById(id.toLong))
        .toList
    }
    MeanOfSatisfaction(action, destinationsConcept)
  } match {
    case Success(m) => m
    case Failure(e) =>
      println("Error while parsing a mean of satisfaction")
      println(e.getStackTrace.mkString("\n"))
      error
  }
}
