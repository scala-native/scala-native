package scala.scalanative
package optimizer
package pass

import org.scalatest.Matchers

import analysis.ClassHierarchy.Top
import build.Config
import nir._

class AsLoweringTest extends OptimizerSpec with Matchers {

  "The `AsLoweringPhase`" should "have an effect (this is a self-test)" in {
    val driver =
      Some(Driver.empty.withPasses(Seq(inject.Main, AsLoweringCheck)))
    val e = intercept[Exception] {
      optimize("A$", code, driver) { case (_, _, _) => () }
    }
    e.getMessage should include("Found an occurrence of `Op.As` in:")
  }

  it should "remove all occurrences of `Op.As`" in {
    val driver =
      Some(
        Driver.empty.withPasses(Seq(inject.Main, AsLowering, AsLoweringCheck)))
    optimize("A$", code, driver) { case (_, _, _) => () }
  }

  private val code = """object A {
                       |  def main(args: Array[String]): Unit =
                       |    println(123.asInstanceOf[Double])
                       |}""".stripMargin

  private class AsLoweringCheck extends Pass {
    override def onInst(inst: Inst) = inst match {
      case inst @ Inst.Let(_, _: Op.As) =>
        val asString = inst.show
        fail(s"""Found an occurrence of `Op.As` in:
                |  $asString""".stripMargin)
      case inst =>
        inst
    }
  }

  private object AsLoweringCheck extends PassCompanion {
    override def apply(config: Config, top: Top): Pass = new AsLoweringCheck
  }
}
