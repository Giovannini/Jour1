package forms.graph.ontology.concept

import forms.graph.ontology.concept.need.NeedForm
import forms.graph.ontology.property.PropertyForm
import forms.graph.ontology.property.ValuedPropertyForm
import models.graph.custom_types.DisplayProperty
import models.graph.ontology.ValuedProperty
import models.graph.ontology.concept.Concept
import models.graph.ontology.concept.need.Need
import models.graph.ontology.property.Property
import play.api.data.Form
import play.api.data.Forms._

/**
 * Created by vlynn on 23/03/15.
 */
object ConceptForm {
  /**
   * Concept form
   */
  val form = Form(
    mapping(
      "label" -> nonEmptyText, //can't be modified
      "properties" -> list(PropertyForm.form.mapping),
      "rules" -> list(ValuedPropertyForm.form.mapping),
      "needs" -> list(NeedForm.form.mapping),
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
                 properties: List[Property],
                 rules: List[ValuedProperty],
                 needs: List[Need],
                 displayProperty: DisplayProperty) : Concept = {
    Concept(label, properties, rules, needs, displayProperty)
  }

  /**
   * Unapply method used in the Concept controller
   * Allows to match a json to a form
   * @param concept concept
   * @return the different parts of a concept
   */
  private def unapplyForm(concept: Concept) : Option[(String, List[Property], List[ValuedProperty], List[Need], DisplayProperty)] = {
    Some(concept.label, concept.properties, concept.rules, concept.needs, concept.displayProperty)
  }
}
