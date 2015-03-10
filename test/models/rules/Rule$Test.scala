package models.rules

import models.rules.Rule
import org.scalatest.FunSuite
import play.api.test.FakeApplication
import play.api.test.Helpers._

/**
 * Test class for the object Rule
 */
class Rule$Test extends FunSuite {
  val test : Rule = Rule(None, "test", Array("bonjour","coucou","salut"), Array("preconditions"), Array("blabla"))

  test("save a rule") {
    running(FakeApplication()) {
      assert(Rule.save(test) match {
        case Some(id) => {
          Rule.delete(id)
          true
        }
        case _ => false
      })
    }
  }

  test("load a rule") {
    running(FakeApplication()) {
      assert(Rule.save(test) match {
        case Some(id) => Rule.load(id) match {
          case Some(rule) => {
            Rule.delete(id)
            true
          }
          case _ => false
        }
        case _ => false
      })
    }
  }

  test("delete a rule") {
    running(FakeApplication()) {
      assert(Rule.save(test) match {
        case Some(id) => Rule.delete(id) == 1
        case _ => false
      });
    }
  }

  test("update a rule") {
    running(FakeApplication()) {
      assert(Rule.save(test) match {
        case Some(id) => {
          val res = Rule.update(id, Rule(None, "test2", Array("bouh"), Array("precond"), Array("bla"))) == 1
          Rule.delete(id)
          res
        }
        case _ => false
      });
    }
  }
}
