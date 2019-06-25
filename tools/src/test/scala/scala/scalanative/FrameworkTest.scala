package scala.scalanative

import nir.Global

import org.scalatest._

class FrameworkTest extends codegen.CodeGenSpec with Matchers {

  "The test framework" should "return the definitions for a single class" in {
    link("A$",
         """object A {
           |  def main(args: Array[String]): Unit =
           |    println("Hello, world!")
           |}""".stripMargin) {
      case (_, res) =>
        val defNames = res.defns map (_.name)
        defNames should contain(Global.Top("A$"))
    }
  }

  it should "return the definitions for classes in multiple files" in {
    val sources = Map(
      "A.scala" -> "class A",
      "B.scala" -> """object B extends A {
                     |  def main(args: Array[String]): Unit = ()
                     |}""".stripMargin
    )

    link("B$", sources) {
      case (_, res) =>
        val defNames = res.defns map (_.name)
        defNames should contain(Global.Top("A"))
        defNames should contain(Global.Top("B$"))
    }
  }
}
