package forms.graph.concept

import forms.graph.concept.need.NeedForm
import forms.graph.property.ValuedPropertyForm
import models.graph.DisplayProperty
import models.intelligence.need.Need
import models.graph.concept.{Concept, ConceptDAO}
import models.graph.property.ValuedProperty
import play.api.data.Form
import play.api.data.Forms._

/**
 * Created by vlynn on 23/03/15.
 */
object ConceptForm {
  def idApply(id: Long): Concept = {
    ConceptDAO.getById(id)
  }

  def idUnapply(concept: Concept): Option[Long] = {
    concept match {
      case Concept.error => None
      case _ => Some(concept.id)
    }
  }

  val idForm = Form(
    mapping(
      "id" -> longNumber
    )(idApply)(idUnapply).verifying("Concept not found", {
      concept => concept != Concept.error
    })
  )

  def nameApply(label: String): Concept = {
    ConceptDAO.getByLabel(label)
  }

  def nameUnapply(concept: Concept): Option[String] = {
    concept match {
      case Concept.error => None
      case _ => Some(concept.label)
    }
  }

  val nameForm = Form(
    mapping(
      "id" -> nonEmptyText
    )(nameApply)(nameUnapply).verifying("Concept not found", {
      concept => concept != Concept.error
    })
  )

  /**
   * Concept form
   */
  val form = Form(
    mapping(
      "label" -> nonEmptyText, //can't be modified
      "properties" -> list(ValuedPropertyForm.form.mapping),
      "rules" -> list(ValuedPropertyForm.form.mapping),
      "needs" -> optional(list(NeedForm.form.mapping)),
      "displayProperty" -> DisplayProperty.form.mapping
    )(applyForm)(unapplyForm)
  )


  /**
   * Apply method used in the Concept controller
   * Allows to match a json to a form
   * @param label concept label
   * @param properties concept properties
   * @param rules concept rules
   * @param displayProperty concept display properties
   * @return a concept using these parameters
   */
  private def applyForm(label: String,
                 properties: List[ValuedProperty],
                 rules: List[ValuedProperty],
                 option_needs: Option[List[Need]],
                 displayProperty: DisplayProperty) : Concept = {
    option_needs match {
      case Some(needs) => Concept(label, properties, rules, needs, displayProperty)
      case None => Concept(label, properties, rules, List(), displayProperty)
    }
  }

  /**
   * Unapply method used in the Concept controller
   * Allows to match a json to a form
   * @param concept concept
   * @return the different parts of a concept
   */
  private def unapplyForm(concept: Concept) : Option[(String, List[ValuedProperty], List[ValuedProperty], Option[List[Need]], DisplayProperty)] = {
    Some((concept.label, concept.properties, concept.rules, Some(concept.needs), concept.displayProperty))
  }
}
