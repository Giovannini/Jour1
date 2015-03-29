package controllers.rules

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import models.interaction.action.{InstanceActionDAO, InstanceAction}

/**
 * Object to create and modify rules
 */
object RulesVisualisation extends Controller {
  /**
   * Form used to create a new rule
   */
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
    Option((action.id.toInt, action.label, action.parameters.map(_.toString), action.preconditions.map(_.toString()), action.subActions.map(_.toString())))
  }

  /**
   * Display all rules
   * @author Aurélie LORGEOUX
   * @return an action for listing all rules
   */
  def index: Action[AnyContent] = Action {
    Ok(views.html.rules.rules())
  }

  /**
   * Delete a rule
   * @author Aurélie LORGEOUX
   * @param id id of rule
   * @return an action redirecting to the listing of rules
   */
  def delete(id: Long): Action[AnyContent] = Action {
    InstanceActionDAO.delete(id)
    Ok(views.html.rules.rules())
  }

  /**
   * Get a rule by its id
   * @author Aurélie LORGEOUX
   * @param id id of rule
   * @return an action displaying the rule
   */
  def load(id: Long):Action[AnyContent] = Action {
    InstanceActionDAO.getById(id) match {
      case InstanceAction.error => Ok(views.html.rules.rules())
      case action => Ok(views.html.rules.show(action))
    }
  }

  /**
   * Display form to create a rule
   * @author Aurélie LORGEOUX
   * @return an action displaying form to create a new rule
   */
  def form: Action[AnyContent] = Action {
    Ok(views.html.rules.form(actionForm))
  }

  /**
   * Create a new rule
   * @author Aurélie LORGEOUX
   * @return an action redirecting to the listing of rules
   */
  def submit: Action[AnyContent] = Action { implicit request =>
    val ruleData = actionForm.bindFromRequest.get
    InstanceActionDAO.save(ruleData)
    Ok(views.html.rules.rules())
  }
}


