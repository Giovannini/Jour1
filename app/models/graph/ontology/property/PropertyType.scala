package models.graph.ontology.property

/**
 * Created by giovannini on 3/22/15.
 */
object PropertyType extends Enumeration{

  type PropertyType = Value
  val Int, Bool, Double, Error = Value

  def parse(string: String) = string match {
    case "Int" => Int
    case "Bool" => Bool
    case "Double" => Double
    case _ => Error
  }

}
