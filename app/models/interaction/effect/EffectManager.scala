package models.interaction.effect

import models.interaction.action.InstanceActionManager
import models.interaction.parameter.{ParameterValue, ParameterReference}

/**
 * Manager for all effects
 */
object EffectManager {

  var nameToId: collection.mutable.Map[String, Effect] = collection.mutable.Map.empty[String, Effect]

  def initialization() = {
    println("Initialization of Effect Manager")

    val death = {
      val deadInstance = ParameterReference("deadInstance", "Long")
      Effect(0L, "EFFECT_DEATH",
        List(
          (InstanceActionManager.nameToInstanceAction("_removeInstanceAt").toEffect,
            Map(
              ParameterReference("instanceToRemove", "Long") -> deadInstance
            ))
        ),
        List(deadInstance)
      ).save
    }
    nameToId += "death" -> death

    val hunger = {
      val hungryInstanceID = ParameterReference("hungryInstance", "Long")
      Effect(0L, "EFFECT_HUNGER",
        List(
          (InstanceActionManager.nameToInstanceAction("_addToProperty").toEffect,
            Map(
              ParameterReference("instanceID", "Long") -> hungryInstanceID,
              ParameterReference("propertyName", "Property") -> ParameterValue("Hunger", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(1, "Int")
            ))
        ),
        List(hungryInstanceID)
      ).save
    }
    nameToId += "hunger" -> hunger

    val starve = {
      val starvingInstanceID = ParameterReference("starvingInstance", "Long")
      Effect(0L, "EFFECT_STARVE",
        List(
          (InstanceActionManager.nameToInstanceAction("_addToProperty").toEffect,
            Map(
              ParameterReference("instanceID", "Long") -> starvingInstanceID,
              ParameterReference("propertyName", "Property") -> ParameterValue("Wound", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(1, "Int")
            ))
        ),
        List(starvingInstanceID)
      ).save
    }
    nameToId += "starve" -> starve

    val grow = {
      val growingInstanceID = ParameterReference("growingInstance", "Long")
      Effect(0L, "EFFECT_GROW",
        List(
          (InstanceActionManager.nameToInstanceAction("_addToProperty").toEffect,
            Map(
              ParameterReference("instanceID", "Long") -> growingInstanceID,
              ParameterReference("propertyName", "Property") -> ParameterValue("DuplicationSpeed", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(-1, "Int")
            ))
        ),
        List(growingInstanceID)
      ).save
    }
    nameToId += "grow" -> grow
  }


}
