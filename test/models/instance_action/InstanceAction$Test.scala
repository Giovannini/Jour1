package models.instance_action

import models.instance_action.action.InstanceAction$
import org.scalatest.FunSuite
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Test class for the object Rule
 */
class InstanceAction$Test extends FunSuite {
  val test : InstanceAction = InstanceAction(0, "test", List(), List(), List())

  test("save a rule") {
    running(FakeApplication()) {
      assert(InstanceAction.save(test) match {
        case InstanceAction.error.id => false
        case id =>
          InstanceAction.delete(id)
          true
      })
    }
  }

  test("load a rule") {
    running(FakeApplication()) {
      assert(InstanceAction.save(test) match {
        case InstanceAction.error.id => false
        case id => InstanceAction.getById(id) match {
          case InstanceAction.error => false
          case action: InstanceAction =>
            InstanceAction.delete(action.id)
            true
          case _ => false
        }
      })
    }
  }

  test("delete a rule") {
    running(FakeApplication()) {
      assert(InstanceAction.save(test) match {
        case InstanceAction.error.id => false
        case id => InstanceAction.delete(id) == 1
      })
    }
  }

  test("update a rule") {
    running(FakeApplication()) {
      assert(InstanceAction.save(test) match {
        case InstanceAction.error.id => false
        case id =>
          val res = InstanceAction.update(id, InstanceAction(0, "test2", List(), List(), List())) == 1
          InstanceAction.delete(id)
          res
      })
    }
  }

}

