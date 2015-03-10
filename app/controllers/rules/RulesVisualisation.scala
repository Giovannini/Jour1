package controllers.rules

import models.rules.Rule
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.format.Formats._

object RulesVisualisation extends Controller {
  val ruleForm = Form(
    mapping(
      "id" -> optional(of[Long]),
      "label" -> text,
      "param" -> list(text),
      "precond" -> list(text),
      "content" -> list(text)
    )(Rule.apply)(Rule.unapply)
  )

  def index = Action {
    Ok(views.html.rules.index())
  }

  def delete(id: Long) = Action {
    Rule.delete(id)
    Ok(views.html.rules.index())
  }

  def load(id: Long) = Action {
    Rule.load(id) match {
      case Some(rule) => Ok(views.html.rules.show(rule))
      case _ => Ok(views.html.rules.index())
    }
  }

  def form = Action {
    Ok(views.html.rules.form(ruleForm))
  }

  def submit = Action { implicit request =>
    val ruleData = ruleForm.bindFromRequest.get
    Rule.save(ruleData)
    Ok(views.html.rules.index())
  }
}


