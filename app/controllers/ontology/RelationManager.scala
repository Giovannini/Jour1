package controllers.ontology

import models.graph.NeoDAO
import models.graph.ontology.relation.Relation
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

/**
 * Object to create, delete and modify relations of the graph
 */
object RelationManager extends Controller{
  /**
   * Form used to create a new relation between two concepts
   */
  val relationForm = Form(
    tuple(
      "concept1_id" -> number,
      "existing_label" -> nonEmptyText,
      "new_label" -> text,
      "concept2_id" -> number,
      "is_action" -> boolean
    )
  )

  /**
   * Action to direct to the relation creation form
   * @author Thomas GIOVANNINI
   * @return an action creating a relation
   */
  def createRelation = Action {
    Ok(views.html.manager.relation.relationCreator())
  }

  /**
   * Print errors contained in a form
   * @author Thhomas GIOVANNINI
   */
  def printErrors(form: Form[(Int, String, String, Int, Boolean)]) = {
    form.errors.foreach(error => println("###Error:\n" + error.messages.mkString("\n")))
  }
  
  /**
   * Handle the relation creation form
   * @author Thomas GIOVANNINI
   */
  def create = Action { implicit request =>
    /**
     * Create the relation following a form with no errors in it.
     * @author Thomas GIOVANNINI
     * @return whether the creation went well or not
     */
    def doCreate(form: (Int, String, String, Int, Boolean)): Boolean = {
      val concept1_id = form._1
      val relation = { if (form._3 == "") form._2 else form._3 }
      val relationID = Relation.DBList.save(relation)
      val concept2_id = form._4
      NeoDAO.addRelationToDB(concept1_id, relationID, concept2_id)
    }

    val newTodoForm = relationForm.bindFromRequest()
    newTodoForm.fold( hasErrors = {form => printErrors(form)},
                      success = {relationCreationForm => doCreate(relationCreationForm)})
    Redirect(controllers.routes.Application.index())
  }

  /**
   * Delete a relation between two concepts
   * @author Aurélie LORGEOUX
   * @param src concept source
   * @param id id of the relation
   * @param dest concept destination
   * @return an action redirecting to index of application
   */
  def delete(src: Long, id: Long, dest: Long) = Action {
    NeoDAO.removeRelationFromDB(src, id, dest)
    Redirect(controllers.routes.Application.index())
  }
}
