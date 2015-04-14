package models.interaction.effect

import models.interaction.action.InstanceActionManager
import models.interaction.parameter.ParameterReference

/**
 * Manager for all effects
 */
object EffectManager {

  private var list: collection.mutable.Map[String, Effect] = collection.mutable.Map()

  def initialization() = {
    println("Initialization of Effect Manager")

    val deadInstance = ParameterReference("deadInstance", "Long")
    val death = Effect(0L, "EFFECT_DEATH",
      List(
        (InstanceActionManager.nameToInstanceAction("_removeInstanceAt").toEffect,
          Map(
            ParameterReference("deadInstance", "Long") -> deadInstance
          ))
      ),
      List(deadInstance)
    ).save

    list += "death" ->  death
  }

}
