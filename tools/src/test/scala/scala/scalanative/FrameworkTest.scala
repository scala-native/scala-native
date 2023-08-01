package scala.scalanative

import nir.Global

import org.junit.Test
import org.junit.Assert._

class FrameworkTest extends codegen.CodeGenSpec {

  @Test def singleClassDefinitions(): Unit = {
    link(
      "A",
      """object A {
           |  def main(args: Array[String]): Unit =
           |    println("Hello, world!")
           |}""".stripMargin
    ) {
      case (_, res) =>
        val defNames = res.defns map (_.name)
        assertTrue(defNames.contains(Global.Top("A$")))
    }
  }

  @Test def multipleFilesClassDefintions(): Unit = {
    val sources = Map(
      "A.scala" -> "class A",
      "B.scala" -> """object B extends A {
                     |  def main(args: Array[String]): Unit = ()
                     |}""".stripMargin
    )

    link("B", sources) {
      case (_, res) =>
        val defNames = res.defns map (_.name)
        assertTrue(defNames.contains(Global.Top("A")))
        assertTrue(defNames.contains(Global.Top("B$")))
    }
  }
}
