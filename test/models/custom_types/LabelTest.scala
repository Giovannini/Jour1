package models.custom_types

import org.scalatest.FunSuite

/**
 * Tests on the model Label
 */
class LabelTest extends FunSuite {

    test("A label must start with a capital letter") {
        intercept[Exception] { Label("hello") }

        intercept[Exception] { Label("17Hello") }
    }

    test("A label can't be empty") {
        intercept[Exception] { Label("") }
    }

    test("A label don't modify its content") {
        assert(Label("Hello World").content == "Hello World")
    }
}
