package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy.Top
import util.sh
import nir._
import Shows._
import tools._

class AsLoweringTest extends OptimizerSpec {

  "The `AsLoweringPhase`" should "have an effect (this is a self-test)" in {
    val driver = Driver.empty.append(AsLoweringCheck)
    assertThrows[org.scalatest.exceptions.TestFailedException] {
      optimize("A$", code, driver) { case (_, _, _) => () }
    }
  }

  it should "remove all occurrences of `Op.As`" in {
    val driver = Driver.empty.append(AsLowering).append(AsLoweringCheck)
    optimize("A$", code, driver) { case (_, _, _) => () }
  }

  private val code = """object A {
                       |  def main(args: Array[String]): Unit =
                       |    println(123.asInstanceOf[Double])
                       |}""".stripMargin

  private class AsLoweringCheck extends Pass {
    override def preInst = {
      case inst @ Inst.Let(_, _: Op.As) =>
        val asString = sh"${inst: Inst}".toString
        fail(s"""Found an occurrence of `Op.As` in:
                |  $asString""".stripMargin)
    }
  }
  private object AsLoweringCheck extends PassCompanion {
    override def apply(config: Config, top: Top): Pass = new AsLoweringCheck
  }

}
