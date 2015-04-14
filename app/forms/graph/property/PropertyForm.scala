package forms.graph.property

import models.graph.property.{PropertyDAO, Property, PropertyType}
import play.api.data.{FormError, Form}
import play.api.data.Forms._
import play.api.data.format.{Formatter, Formats}

object PropertyForm {
  val form: Form[Property] = Form(mapping(
    "id" -> longNumber,
    "label" -> nonEmptyText.verifying("Label has to begin with a capital", label => label.matches("^[A-Z][A-Za-z0-9]*$")),
    "propertyType" -> nonEmptyText,
    "defaultValue" -> of(Formats.doubleFormat)
  )(applyForm)(unapplyForm))

  def applyForm(id: Long, label: String, propertyType: String, defaultValue: Double): Property = {
    if (PropertyType.parse(propertyType) == PropertyType.Error) {
      Property.error
    } else {
      Property(id, label, PropertyType.parse(propertyType), defaultValue)
    }
  }

  def unapplyForm(property: Property): Option[(Long, String, String, Double)] = {
    if (property == Property.error) {
      None
    } else {
      Some((property.id, property.label, property.propertyType.toString, property.defaultValue))
    }
  }

  val labelForm: Form[Property] = Form(mapping(
    "label" -> nonEmptyText.verifying("Label has to begin with a capital", label => label.matches("^[A-Z][A-Za-z0-9]*$"))
  )(applyLabelForm)(unapplyLabelForm))

  def applyLabelForm(label: String): Property = {
    PropertyDAO.getByName(label)
  }

  def unapplyLabelForm(property: Property): Option[(String)] = {
    if (property == Property.error) None
    else Some(property.label)
  }

  def PropertyLabelFormat: Formatter[Property] = new Formatter[Property] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Property] = {
      data.get(key) match {
        case None => Left(Seq(FormError(key, "error.required")))
        case Some(label) =>
          PropertyDAO.getByName(label) match {
            case Property.error => Left(Seq(FormError(key, "notFound")))
            case property => Right(property)
          }
      }
    }

    override def unbind(key: String, value: Property): Map[String, String] = {
      Map(key -> value.label)
    }
  }
}