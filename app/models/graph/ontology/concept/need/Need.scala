package models.graph.ontology.concept.need

import models.graph.ontology.Instance
import models.graph.ontology.concept.consequence.ConsequenceStep
import models.graph.ontology.property.Property
import models.instance_action.action.InstanceAction

/**
 * Object used by instances to decide the action they'd better do.
 *
 * An instance will for the moment try to satisfy ONLY ONE need at a time. It can have several means to satisfy its
 * needs, all these means are actions stored in parameter meansOfSatisfaction.
 * A need refers to a property.
 * meansOfSatisfaction is sorted
 */
case class Need(
  id: Long,
  label: String,
  affectedProperty: Property,
  priority: Double,
  _consequencesSteps: List[ConsequenceStep],
  meansOfSatisfaction: List[InstanceAction]) {

  val consequencesSteps = _consequencesSteps.sortBy(-_.value)

  def withId(newID: Long): Need = {
    Need(newID, label, affectedProperty, priority, consequencesSteps, meansOfSatisfaction)
  }

  /**
   * Evaluate the priority of a need for a given instance
   * @author Thomas GIOVANNINI
   * @param instance for which the need is evaluated
   * @return a Double representing the priority value for the need
   */
  def evaluate(instance: Instance): Double = {
    val valueOfAffectedProperty = instance.getValueForProperty(affectedProperty)
    val actualConsequenceStep = consequencesSteps.find(_.value > valueOfAffectedProperty)
      .getOrElse(consequencesSteps.last)
    val numberOfTurnsBeforeConsequence = actualConsequenceStep.value - valueOfAffectedProperty
    val result =
      if (numberOfTurnsBeforeConsequence > 0) {
        (priority + actualConsequenceStep.consequence.severity) / numberOfTurnsBeforeConsequence
      }
      else { // The last possible consequence step has been exceeded
        // Given ln(3) = 1.09, this value will be higher than the one at last turn
        val factorValue = math.log(numberOfTurnsBeforeConsequence + 3)
        (priority + actualConsequenceStep.consequence.severity) / factorValue
      }
    println("Priority for need " + label + " is " + result)
    result
  }
}

// TODO: the label is not really mandatory here

object Need {
  val error = Need(-1L, "error", Property.error, 0, List(), List())
}
