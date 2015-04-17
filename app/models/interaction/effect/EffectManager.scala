package models.interaction.effect

import models.interaction.action.InstanceActionManager
import models.interaction.parameter.{ParameterValue, ParameterReference}

import scala.collection.mutable
import scala.collection.immutable

/**
 * Manager for all effects
 */
object EffectManager {

  var nameToId: mutable.Map[String, Effect] = mutable.Map.empty[String, Effect]

  def initialization() = {
    Console.println("Initialization of Effect Manager")

    val death = {
      val deadInstance = ParameterReference("deadInstance", "Long")
      Effect(0L, "EFFECT_DEATH",
        List(
          (InstanceActionManager.nameToInstanceAction("_removeInstanceAt").toEffect,
            immutable.Map(
              ParameterReference("instanceToRemove", "Long") -> deadInstance
            ))
        ),
        List(deadInstance)
      ).save
    }
    nameToId.put("death", death)

    val hunger = {
      val hungryInstanceID = ParameterReference("hungryInstance", "Long")
      Effect(0L, "EFFECT_HUNGER",
        List(
          (InstanceActionManager.nameToInstanceAction("_addToProperty").toEffect,
            immutable.Map(
              ParameterReference("instanceID", "Long") -> hungryInstanceID,
              ParameterReference("propertyName", "Property") -> ParameterValue("Hunger", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(1, "Int")
            ))
        ),
        List(hungryInstanceID)
      ).save
    }
    nameToId.put("hunger", hunger)

    val starve = {
      val starvingInstanceID = ParameterReference("starvingInstance", "Long")
      Effect(0L, "EFFECT_STARVE",
        List(
          (InstanceActionManager.nameToInstanceAction("_addToProperty").toEffect,
            immutable.Map(
              ParameterReference("instanceID", "Long") -> starvingInstanceID,
              ParameterReference("propertyName", "Property") -> ParameterValue("Wound", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(1, "Int")
            ))
        ),
        List(starvingInstanceID)
      ).save
    }
    nameToId.put("starve", starve)

    val grow = {
      val growingInstanceID = ParameterReference("growingInstance", "Long")
      Effect(0L, "EFFECT_GROW",
        List(
          (InstanceActionManager.nameToInstanceAction("_addToProperty").toEffect,
            immutable.Map(
              ParameterReference("instanceID", "Long") -> growingInstanceID,
              ParameterReference("propertyName", "Property") -> ParameterValue("DuplicationSpeed", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue(-1, "Int")
            ))
        ),
        List(growingInstanceID)
      ).save
    }
    nameToId.put("grow", grow)

    val addDesire = {
      val instanceID = ParameterReference("desiringInstance", "Long")
      Effect(0L, "EFFECT_GROW",
        List(
          (InstanceActionManager.nameToInstanceAction("_addToProperty").toEffect,
            immutable.Map(
              ParameterReference("instanceID", "Long") -> instanceID,
              ParameterReference("propertyName", "Property") -> ParameterValue("Desire", "Property"),
              ParameterReference("valueToAdd", "Int") -> ParameterValue("Comfort", "Property")
            ))
        ),
        List(instanceID)
      ).save
    }
    nameToId.put("addDesire", addDesire)
  }


}
