package models.ontology

import models.custom_types.Label

/**
 * Created by giovannini on 1/22/15.
 */
abstract class Property{
    val label: Label
    val value: Any
}

case class StringProperty(label: Label, value: String) extends Property

case class IntProperty(label: Label, value: Int) extends Property