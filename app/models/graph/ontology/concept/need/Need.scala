package models.graph.ontology.concept.need

import models.graph.ontology.concept.consequence.ConsequenceStep
import play.api.data.Form
import play.api.data.Forms._

/**
 * Object used by instances to decide the action they'd better do.
 */
case class Need(id: Long, label: String, consequencesSteps: List[ConsequenceStep])

object Need {

  val error = Need(-1L, "error", List())

  val form = Form(mapping(
    "id" -> longNumber,
    "label" -> text,
    "consequencesSteps" -> list(ConsequenceStep.form.mapping)
  )(Need.apply)(Need.unapply))

}
