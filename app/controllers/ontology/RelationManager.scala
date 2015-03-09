package controllers.ontology

import models.graph.NeoDAO
import models.graph.ontology.Relation
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
  val relationCreationForm = Form(
    tuple(
      "concept1_id" -> number,
      "relation_label" -> nonEmptyText,
      "concept2_id" -> number,
      "is_action" -> boolean
    )
  )

  /**
   * Action to direct to the telation creation form
   * @author Thomas GIOVANNINI
   * @return an action creating a relation
   */
  def createRelation = Action {
    Ok(views.html.manager.relation.relationCreator())
  }

  /**
   * Handle the relation creation form
   * @author Thomas GIOVANNINI
   */
  def create = Action { implicit request =>
    /**
    * Print errors contained in a form
    * @author Thhomas GIOVANNINI
    */
    def printErrors(form: Form[(Int, String, Int, Boolean)]) = {
      form.errors.foreach(error => println("###Error:\n" + error.messages.mkString("\n")))
    }

    /**
     * Create the relation following a form with no errors in it.
     * @author Thomas GIOVANNINI
     * @param form containing the update
     * @return whether the creation went well or not
     */
    def doCreate(form: (Int, String, Int, Boolean)): Boolean = {
      val concept1_id = form._1
      val relation = Relation(form._2)
      val concept2_id = form._3
      NeoDAO.addRelationToDB(concept1_id, relation, concept2_id)
    }

    val newTodoForm = relationCreationForm.bindFromRequest()
    newTodoForm.fold( hasErrors = {form => printErrors(form)},
                      success = {relationCreationForm => doCreate(relationCreationForm)})
    Redirect(controllers.routes.Application.index())
  }

}
