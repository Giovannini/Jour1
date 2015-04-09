package forms.interaction

import forms.interaction.action.InstanceActionForm
import models.interaction.InteractionType
import models.interaction.InteractionType.InteractionType
import models.interaction.action.InstanceAction
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formatter

/**
 * Created by vlynn on 09/04/15.
 */
object InteractionForm {
  val form: Form[(InteractionType, InstanceAction)] = Form(
    tuple(
      "type" -> of(InteractionTypeFormatter),
      "action" -> InstanceActionForm.form.mapping
    ).verifying("The action does not fit the type", formResult => formResult._2.isValid(formResult._1))
  )

  implicit def InteractionTypeFormatter: Formatter[InteractionType] = new Formatter[InteractionType] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], InteractionType] = {
      data.get(key) match {
        case None => Left(Seq(FormError(key, "Action type not defined")))
        case Some(_type) => Right(InteractionType.parse(_type))
      }
    }

    override def unbind(key: String, value: InteractionType): Map[String, String] = {
      Map(key -> value.toString)
    }
  }
}
