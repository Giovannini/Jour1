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
   * @return
   */
  def createRelation = Action {
    Ok(views.html.manager.relation.relationCreator())
  }

  /**
   * Handle the relation creation form
   * @author Thomas GIOVANNINI
   */
  def create = Action { implicit request =>
    val newTodoForm = relationCreationForm.bindFromRequest()
    newTodoForm.fold(
      hasErrors = { form =>
        println("###Error")
        form.errors.foreach(println)
      },
      success = { relationCreationForm =>
        println("Success")
        val concept1_id = relationCreationForm._1
        val relation = Relation(relationCreationForm._2)
        val concept2_id = relationCreationForm._3
        NeoDAO.addRelationToDB(concept1_id, relation, concept2_id)
      }
    )
    Redirect(controllers.routes.Application.index())
  }
}
