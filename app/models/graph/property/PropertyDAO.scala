package models.graph.property

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

  private var mappingId = collection.mutable.Map.empty[Long, Property]
  private var mappingName = collection.mutable.Map.empty[String, Property]

  /**
   * Parse property to interact with database
   * @author Thomas GIOVANNINI
   */
  private val propertyParser: RowParser[Property] = {
    get[Long]("id") ~
    get[String]("label") ~
    get[String]("type") ~
    get[Double]("defaultValue") map {
      case id ~ label ~ propertyType ~ defaultValue =>
        Property(id, label, PropertyType.parse(propertyType), defaultValue)
    }
  }

  /**
   * Clear the database
   * @author Thomas GIOVANNINI
   * @return number of properties deleted
   */
  def clear: Int = {
    mappingId = mappingId.empty
    mappingName = mappingName.empty
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
   * @return 1 if the property was saved
   */
  def save(property: Property): Int = {
    DB.withConnection { implicit connection =>
      val statement = PropertyStatement.add(property)
      statement.executeUpdate
    }
  }

  /**
   * Get one property saved in database with its id
   * @author Thomas GIOVANNINI
   * @param id id of the property
   * @return property identified by id
   */
  def getById(id: Long): Property = {
    mappingId.getOrElse(id, {
      DB.withConnection { implicit connection =>
        val statement = PropertyStatement.getById(id)
        val property = statement.as(propertyParser.singleOpt)
          .getOrElse(Property.error)
        if (property != Property.error) {
          mappingId += id -> property
          mappingName += property.label -> property
        }
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
    mappingName.getOrElse(name, {
      DB.withConnection { implicit connection =>
        val statement = PropertyStatement.getByName(name)
        val property = statement.as(propertyParser.singleOpt)
          .getOrElse(Property.error)
        if (property != Property.error) {
          mappingName += name -> property
        }
        property
      }
    })
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


