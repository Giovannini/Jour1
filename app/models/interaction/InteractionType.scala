package models.interaction

/**
 * Created by vlynn on 09/04/15.
 */
object InteractionType extends Enumeration {
  type InteractionType = Value
  val Simple, Action, Effect, Mood = Value

  def parse(string: String) = string match {
    case "Action" => Action
    case "Effect" => Effect
    case "Mood" => Mood

    case "ACTION_" => Action
    case "EFFECT_" => Effect
    case "MOOD_" => Mood

    case _ => Simple
  }


}
