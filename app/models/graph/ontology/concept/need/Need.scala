package models.graph.ontology.concept.need

import models.graph.ontology.concept.consequence.ConsequenceStep
import models.graph.ontology.property.Property
import models.instance_action.action.InstanceAction
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

/**
 * Object used by instances to decide the action they'd better do.
 *
 * An instance will for the moment try to satisfy ONLY ONE need at a time. It can have several means to satisfy its
 * needs, all these means are actions stored in parameter meansOfSatisfaction.
 * A need refers to a property.
 * meansOfSatisfaction is sorted
 */
case class Need(id: Long,
  label: String,
  affectedProperty: Property,
  priority: Double,
  consequencesSteps: List[ConsequenceStep],
  meansOfSatisfaction: List[InstanceAction])

// TODO: the labal is not really mandatory here

object Need {

  val error = Need(-1L, "error", Property.error, 0, List(), List())
  println("caca2")
  val form = Form(mapping(
    "id" -> longNumber,
    "label" -> text,
    "affectedProperty" -> Property.form.mapping,
    "priority" -> of[Double],
    "consequencesSteps" -> list(ConsequenceStep.form.mapping),
    "meansOfSatisfaction" -> list(InstanceAction.form.mapping)
  )(Need.apply)(Need.unapply))
}
