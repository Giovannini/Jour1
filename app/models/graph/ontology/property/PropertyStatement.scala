package models.graph.ontology.property

import anorm._


object PropertyStatement {
  /**
   * Request to clear the database
   * @author Thomas GIOVANNINI
   * @return a sql statement
   */
  val clearDB = {
    SQL("DELETE FROM properties;")
  }


  /**
   * Request to select all elements from database
   * @author Thomas GIOVANNINI
   * @return a sql statement
   */
  val getAll = {
    SQL("SELECT * FROM properties")
  }

  /**
   * Add a property to database
   * @author Thomas GIOVANNINI
   * @param property property to add
   * @return a sql statement
   */
  def add(property: Property): SimpleSql[Row] = {
    SQL("""INSERT INTO properties(label, defaultValue)
           VALUES({label}, {defaultValue})""")
      .on(
        'label -> property.label,
        'defaultValue -> property.defaultValue.toString
      )
  }

  /**
   * Get a properties from database
   * @author Thomas GIOVANNINI
   * @param id id of the property
   * @return a sql statement
   */
  def getById(id: Long) = {
    SQL("SELECT * from properties WHERE id = {id}")
      .on('id -> id)
  }

  def getByName(name: String) = {
    SQL("SELECT * from properties WHERE label = {name}")
      .on('name -> name)
  }

  /**
   * Update a property in database
   * @author Thomas GIOVANNINI
   * @param id id of the property
   * @param property new property with changes
   * @return a sql statement
   */
  def update(id: Long, property: Property) = {
    SQL("""
      UPDATE properties
      SET label = {label}, defaultValue = {defaultValue}
      WHERE id = {id}""").on(
        'id -> id,
        'label -> property.label,
        'defaultValue -> property.defaultValue.toString
      )
  }

  /**
   * Remove a PRECONDITION from database
   * @author Thomas GIOVANNINI
   * @param id id of the property
   * @return a sql statement
   */
  def remove(id: Long) = {
    SQL("DELETE FROM properties where id = {id}")
      .on('id -> id)
  }

}
