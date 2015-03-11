package models.rules

import models.rules.action.Action
import org.scalatest.FunSuite
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Test class for the object Rule
 */
class Action$Test extends FunSuite {
  val test : Action = Action(0, "test", List(), List(), List())

  test("save a rule") {
    running(FakeApplication()) {
      assert(Action.save(test) match {
        case Action.error.id => false
        case id =>
          Action.delete(id)
          true
      })
    }
  }

  test("load a rule") {
    running(FakeApplication()) {
      assert(Action.save(test) match {
        case Action.error.id => false
        case id => Action.getById(id) match {
          case Action.error => false
          case action: Action =>
            Action.delete(action.id)
            true
          case _ => false
        }
      })
    }
  }

  test("delete a rule") {
    running(FakeApplication()) {
      assert(Action.save(test) match {
        case Action.error.id => false
        case id => Action.delete(id) == 1
      })
    }
  }

  test("update a rule") {
    running(FakeApplication()) {
      assert(Action.save(test) match {
        case Action.error.id => false
        case id =>
          val res = Action.update(id, Action(0, "test2", List(), List(), List())) == 1
          Action.delete(id)
          res
      })
    }
  }

}

