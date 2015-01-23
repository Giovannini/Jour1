package models.ontology

import models.custom_types.Label

/**
 * Model for properties
 * @author Thomas GIOVANNINI
 */
abstract class Property{
    val label: Label
    val value: Any
}

/**
 * Model for properties which values are String
 * @author Thomas GIOVANNINI
 * @param label of the property
 * @param value of the property
 */
case class StringProperty(label: Label, value: String) extends Property

/**
 * Model for properties which values are Int
 * @author Thomas GIOVANNINI
 * @param label of the property
 * @param value of the property
 */
case class IntProperty(label: Label, value: Int) extends Property