package controllers.rules

import models.graph.ontology.relation.Relation
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

object RelationVisualisation extends Controller {
  /**
   * Form used to create a new relation
   */
  val relationForm = Form(
    mapping(
      "label" -> text
    )(relationApply)(relationUnapply)
  )

  def relationApply(label: String): Relation = {
    Relation(label)
  }

  def relationUnapply(relation: Relation): Option[(String)] = {
    Option(relation.label)
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
    val relation = relationForm.bindFromRequest.get
    Relation.DBList.save(relation.label)
    Ok(views.html.relations.relations())
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
    val relation = relationForm.bindFromRequest.get
    Relation.DBList.update(id, relation.label)
    Ok(views.html.relations.relations())
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