package models.graph.property

/**
 * Enumeration object listing all different property types.
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
