package models.interaction

import controllers.Application
import play.api.db.DB
import play.api.Play.current

import scala.language.postfixOps

/**
 * Distance Access Object for interactions
 */
object InteractionDAO {

  implicit val connection = Application.connection

  /**
   * Clear the database
   * @author Aurélie LORGEOUX
   * @return number of rules deleted
   */
  def clearDB(): Boolean = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.clearDB
      statement.executeUpdate()
      true
    }
  }

  /**
   * Delete a rule in database
   * @author Aurélie LORGEOUX
   * @param id id of the rule
   */
  def delete(id: Long): Int = {
    DB.withConnection { implicit connection =>
      val statement = InteractionStatement.remove(id)
      statement.executeUpdate
    }
  }
}
