package models.instance_action

import models.instance_action.action.InstanceAction
import org.scalatest.FunSuite
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Test class for the object Rule
 */
class InstanceAction$Test extends FunSuite {
  val test : Interaction = Interaction(0, "test", List(), List(), List())

  test("save a rule") {
    running(FakeApplication()) {
      assert(Interaction.save(test) match {
        case Interaction.error.id => false
        case id =>
          Interaction.delete(id)
          true
      })
    }
  }

  test("load a rule") {
    running(FakeApplication()) {
      assert(Interaction.save(test) match {
        case Interaction.error.id => false
        case id => Interaction.getById(id) match {
          case Interaction.error => false
          case action: Interaction =>
            Interaction.delete(action.id)
            true
          case _ => false
        }
      })
    }
  }

  test("delete a rule") {
    running(FakeApplication()) {
      assert(Interaction.save(test) match {
        case Interaction.error.id => false
        case id => Interaction.delete(id) == 1
      })
    }
  }

  test("update a rule") {
    running(FakeApplication()) {
      assert(Interaction.save(test) match {
        case Interaction.error.id => false
        case id =>
          val res = Interaction.update(id, Interaction(0, "test2", List(), List(), List())) == 1
          Interaction.delete(id)
          res
      })
    }
  }

}

