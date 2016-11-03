package scala.scalanative
package testing
package optimizer

import tools._

import org.scalatest._

class FrameworkTest extends OptimizerSpec with Matchers {

  "The test framework" should "return the definitions for a single class" in {
    withDefinitions("""class A {
                      |  def foo(name: String): Unit =
                      |    println(s"Hello, $name!")
                      |}""".stripMargin) {
      defns =>
        defns should have length(3)
    }
  }

  it should "return the definitions for classes in multiple files" in {
    val sources = Map("A.scala" -> "class A",
                      "B.scala" -> "class B extends A")

    withDefinitions(sources) { defns =>
      defns should have length(4)
    }
  }
}
