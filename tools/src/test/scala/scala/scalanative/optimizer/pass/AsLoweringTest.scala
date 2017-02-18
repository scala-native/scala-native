package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy.Top
import nir._
import tools._

class AsLoweringTest extends OptimizerSpec {

  "The `AsLoweringPhase`" should "have an effect (this is a self-test)" in {
    val driver = Some(Driver.empty.append(AsLoweringCheck))
    assertThrows[Exception] {
      optimize("A$", code, driver) { case (_, _, _) => () }
    }
  }

  it should "remove all occurrences of `Op.As`" in {
    val driver = Some(Driver.empty.append(AsLowering).append(AsLoweringCheck))
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
