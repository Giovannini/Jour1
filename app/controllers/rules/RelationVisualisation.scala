package controllers.rules

import models.graph.ontology.relation.Relation
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc.{Action, Controller}

object RelationVisualisation extends Controller {
  /**
   * Form used to create a new relation
   */
  val relationForm = Form(
    mapping(
      "label" -> nonEmptyText.verifying("Invalid label", label => label.matches("^[A-Z][A-Z0-9_]*$"))
    )(relationApply)(relationUnapply)
  )

  def relationApply(label: String): Relation = {
    Relation(label)
  }

  def relationUnapply(relation: Relation): Option[(String)] = {
    Option(relation.label)
  }

  /**
   * Print errors contained in a form
   * @author Thhomas GIOVANNINI
   */
  def printErrors(form: Form[(Relation)]) = {
    form.errors.foreach(error => println("###Error:\n" + error.messages.mkString("\n")))
  }

  /**
   * Display all types of relation
   * @author Aurélie LORGEOUX
   * @return an action displaying all relations
   */
  def index = Action {
    Ok(views.html.relations.relations())
  }

  /**
   * Display form to create a relation
   * @author Aurélie LORGEOUX
   * @return an action displaying form to create a new relation
   */
  def createForm = Action {
    Ok(views.html.relations.formCreator(relationForm))
  }

  /**
   * Create a new relation from form
   * @author Aurélie LORGEOUX
   * @return an action redirecting to the list of relations
   */
  def createSubmit = Action { implicit request =>
    /**
     * Create relation
     * @author Aurélie LORGEOUX
     * @return true if relation created
     *         false else
     */
    def doCreate(relation: Relation): Boolean = {
      Relation.DBList.save(relation.label) != -1L
    }

    val form = relationForm.bindFromRequest()
    form.fold(
      hasErrors = {
        formWithErrors => BadRequest(formWithErrors.errorsAsJson)
      },
      success = {
        relation => {
          if (doCreate(relation)) {
            Ok(views.html.relations.relations())
          } else {
            InternalServerError("Couldn't add relation to DB")
          }
        }
      }
    )
  }

  /**
   * Display form to update a relation
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @return an action displaying a form to update the relation
   */
  def updateForm(id: Long) = Action {
    val relation = Relation.DBList.getById(id)
    val relationFormFilled = relationForm.fill(relation)
    Ok(views.html.relations.formEditor(relationFormFilled, id))
  }

  /**
   * Update a relation from form
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @return an action redirecting to the list of relations
   */
  def updateSubmit(id: Long) = Action { implicit request =>
    /**
     * Update relation
     * @author Aurélie LORGEOUX
     * @return true if relation updated
     *         false else
     */
    def doUpdate(relation: Relation): Boolean = {
      Relation.DBList.update(id, relation.label) == 1
    }

    val form = relationForm.bindFromRequest()
    form.fold(
      hasErrors = {
        formWithErrors => BadRequest(formWithErrors.errorsAsJson)
      },
      success = {
        relation => {
          if (doUpdate(relation)) {
            Ok(views.html.relations.relations())
          } else {
            InternalServerError("Couldn't update relation in DB")
          }
        }
      }
    )
  }

  /**
   * Delete entirely a relation
   * @author Aurélie LORGEOUX
   * @param id id of the relation
   * @return an action reidrecting to the list of relations
   */
  def delete(id: Long) = Action {
    Relation.DBList.delete(id)
    Ok(views.html.relations.relations())
  }
}