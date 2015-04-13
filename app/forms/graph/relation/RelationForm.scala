package forms.graph.relation

import forms.graph.concept.ConceptForm
import models.graph.relation.{RelationSqlDAO, Relation}
import play.api.data.Form
import play.api.data.Forms._

/**
 * Created by vlynn on 31/03/15.
 */
object RelationForm {
  val form = Form(
    mapping(
      "id" -> longNumber,
      "label" -> nonEmptyText
    )(Relation.apply)(Relation.unapply)
  )

  def simpleApply(id: Long): Relation = {
    RelationSqlDAO.getById(id)
  }

  def simpleUnapply(relation: Relation): Option[Long] = {
    Some(relation.id)
  }

  val simpleForm = Form(
    mapping(
      "id" -> longNumber
    )(simpleApply)(simpleUnapply).verifying(
        "Relation not found",
        relation => relation != Relation.error
      )
  )

  val graphForm = Form(
    tuple(
      "source" -> ConceptForm.idForm.mapping,
      "destination" -> ConceptForm.idForm.mapping,
      "relation" -> simpleForm.mapping
    )
  )
}
