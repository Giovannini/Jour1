package controllers.rules

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import models.rules.action.InstanceAction

object RulesVisualisation extends Controller {
  val actionForm = Form(
    mapping(
      "id" -> number,
      "label" -> text,
      "param" -> list(text),
      "precond" -> list(text),
      "content" -> list(text)
    )(ruleApply)(ruleUnapply)
  )

  def ruleApply(id: Int, label: String, param: List[String], precond: List[String], content: List[String]): InstanceAction = {
    InstanceAction(id, label, List(), List(), List())
  }

  def ruleUnapply(action: InstanceAction): Option[(Int, String, List[String], List[String], List[String])] = {
    Option((action.id.toInt, action.label, action.parameters.map(_.toString), action.preconditions.map(_.toString), action.subActions.map(_.toString)))
  }

  def index = Action {
    Ok(views.html.rules.rules())
  }

  def delete(id: Long) = Action {
    InstanceAction.delete(id)
    Ok(views.html.rules.rules())
  }

  def load(id: Long) = Action {
    InstanceAction.getById(id) match {
      case InstanceAction.error => Ok(views.html.rules.rules())
      case action => Ok(views.html.rules.show(action))
    }
  }

  def form = Action {
    Ok(views.html.rules.form(actionForm))
  }

  def submit = Action { implicit request =>
    val ruleData = actionForm.bindFromRequest.get
    InstanceAction.save(ruleData)
    Ok(views.html.rules.rules())
  }
}


