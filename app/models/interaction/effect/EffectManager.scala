package models.interaction.effect

import models.interaction.action.InstanceActionManager
import models.interaction.parameter.{Parameter, ParameterReference}

/**
 * Manager for all effects
 */
object EffectManager {

  private var list: collection.mutable.Map[String, Effect] = _

  def initialization = {
    val death = Effect(0L, "Death",
      List(
        (InstanceActionManager.nameToId("_removeInstanceAt").toEffect,
          Map.empty[ParameterReference, Parameter])
      ),
      List()).save
    list += "death" ->  death
  }

}
