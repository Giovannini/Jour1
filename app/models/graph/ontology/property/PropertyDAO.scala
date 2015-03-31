package models.graph.ontology.property

import anorm.SqlParser._
import anorm._
import controllers.Application
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps


/**
 * Model for properties persistancy
 */
object PropertyDAO {

  implicit val connection = Application.connection

  private var mapping = collection.mutable.Map.empty[Long, Property]

  /**
   * Parse property to interact with database
   * @author Thomas GIOVANNINI
   */
  private val propertyParser: RowParser[Property] = {
    get[String]("label") ~
    get[String]("type") ~
    get[Double]("defaultValue") map {
      case label ~ propertyType ~ defaultValue =>
        Property(label, PropertyType.parse(propertyType), defaultValue)
    }
  }

  /**
   * Clear the database
   * @author Thomas GIOVANNINI
   * @return number of properties deleted
   */
  def clear: Int = {
    mapping = mapping.empty
    DB.withConnection { implicit connection =>
      val statement = PropertyStatement.clearDB
      statement.executeUpdate
    }
  }

  /**
   * Get all properties saved in database
   * @author Thomas GIOVANNINI
   * @return a list of properties
   */
  def getAll: List[Property] = {
    DB.withConnection { implicit connection =>
      val statement = PropertyStatement.getAll
      statement.as(propertyParser *)
    }
  }

  /**
   * Save property in database
   * @author Thomas GIOVANNINI
   * @param property to put in the database
   * @return true if the property was saved
   *         false else
   */
  def save(property: Property): Property = {
    DB.withConnection { implicit connection =>
      val statement = PropertyStatement.add(property)
      statement.execute()
      property
    }
  }

  /**
   * Get one property saved in database with its id
   * @author Thomas GIOVANNINI
   * @param id id of the property
   * @return property identified by id
   */
  def getById(id: Long): Property = {
    mapping.getOrElse(id, {
      DB.withConnection { implicit connection =>
        val statement = PropertyStatement.getById(id)
        val property = statement.as(propertyParser.singleOpt)
          .getOrElse(Property.error)
        mapping += id -> property
        property
      }
    })
  }

  /**
   * Get one property saved in database with its name
   * @author Thomas GIOVANNINI
   * @param name of the property
   * @return property identified by id
   */
  def getByName(name: String): Property = {
    DB.withConnection { implicit connection =>
      val statement = PropertyStatement.getByName(name)
      val maybePrecondition = statement.as(propertyParser.singleOpt)
      maybePrecondition.getOrElse(Property.error)
    }
  }

  /**
   * Update a property in database
   * @author Thomas GIOVANNINI
   * @param id id of the property
   * @param property property identified by id
   */
  def update(id: Long, property: Property): Int = {
    DB.withConnection { implicit connection =>
      val statement = PropertyStatement.update(id, property)
      statement.executeUpdate
    }
  }

  /**
   * Delete a property in database
   * @author Thomas GIOVANNINI
   * @param id id of the property
   */
  def delete(id: Long): Int = {
    DB.withConnection { implicit connection =>
      val statement = PropertyStatement.remove(id)
      statement.executeUpdate
    }
  }
}


