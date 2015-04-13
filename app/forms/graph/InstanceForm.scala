package forms.graph

import forms.graph.property.ValuedPropertyForm
import models.graph.concept.ConceptDAO
import models.graph.property.ValuedProperty
import models.graph.{Coordinates, Instance}
import play.api.data.Form
import play.api.data.Forms._


object InstanceForm {
  val form = Form(
    mapping(
      "id" -> longNumber, // can't be modified
      "label" -> text,
      "coordinates" -> CoordinatesForm.form.mapping,
      "properties" -> list(ValuedPropertyForm.form.mapping),
      "concept" -> longNumber // can't be modified
    )(applyForm)(unapplyForm)
  )

  /**
   * Apply method used in the Instance controller
   * Allows to match a json to a form
   * @param id instance id
   * @param label instance label
   * @param coordinates instance coordinates
   * @param properties instance properties
   * @param concept concept id
   * @return an instance using these parameters
   */
  private def applyForm(id: Long, label: String, coordinates: Coordinates, properties: List[ValuedProperty], concept: Long) : Instance = {
    Instance(id, label, coordinates, properties, ConceptDAO.getById(concept))
  }

  /**
   * Unapply method used in the Instance controller
   * Allows to match a json to a form
   * @param instance instance
   * @return the different parts of an instance
   */
  private def unapplyForm(instance: Instance) : Option[(Long, String, Coordinates, List[ValuedProperty], Long)] = {
    Some((instance.id, instance.label, instance.coordinates, instance.properties, instance.concept.id))
  }
}
