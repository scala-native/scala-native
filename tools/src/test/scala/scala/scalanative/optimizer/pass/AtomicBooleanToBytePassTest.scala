package scala.scalanative
package optimizer
package pass

import org.scalatest.Matchers

import scala.scalanative.nir.{Defn, Inst, Op, Type}
import scala.scalanative.optimizer.analysis.ClassHierarchy.Top
import scala.scalanative.optimizer.analysis.ClassHierarchyExtractors.FieldRef
import scala.scalanative.build.Config

class AtomicBooleanToBytePassTest extends OptimizerSpec with Matchers {

  val code =
    """object A {
      |  @volatile var foo = true
      |
      |  def main(args: Array[String]): Unit = {
      |    println(foo)
      |  }
      |}""".stripMargin

  it should "have an effect (this is a self-test)" in {
    // given
    val driver = Some(
      Driver.empty.withPasses(Seq(inject.Main, AtomicBooleanToBytePassCheck)))

    // when
    val e = intercept[Exception] {
      optimize("$A", code, driver) { case (_, _, _) => () }
    }

    // then
    e.getMessage should include(
      "Found an occurrence of volatile boolean field in:")
  }

  it should "convert all volatile boolean fields to byte fields" in {
    // given
    val driver = Some(Driver.empty.withPasses(
      Seq(inject.Main, AtomicBooleanToBytePass, AtomicBooleanToBytePassCheck)))

    // when & then
    optimize("$A", code, driver) { case (_, _, _) => () }
  }

  private class AtomicBooleanToBytePassCheck(implicit top: Top) extends Pass {

    override def onDefn(defn: Defn): Defn = defn match {
      case Defn.Var(attrs, FieldRef(_, _), Type.Bool, _)
          if attrs.isJavaVolatile =>
        fail(
          s"Found an occurrence of volatile boolean field in: ${defn.show}.stripMargin")
      case _ => defn
    }

    override def onInst(inst: Inst): Inst = inst match {
      case Inst.Let(_, _ @Op.Load(Type.Bool, _, _, _isAtomic @ true)) =>
        fail(s"Found an occurence of atomic boolean load in: ${inst.show}")
      case Inst.Let(_, _ @Op.Store(Type.Bool, _, _, _, _isAtomic @ true)) =>
        fail(s"Found an occurence of atomic boolean store in: ${inst.show}")
      case _ => inst
    }
  }

  private object AtomicBooleanToBytePassCheck extends PassCompanion {
    override def apply(config: Config, top: Top): AnyPass =
      new AtomicBooleanToBytePassCheck()(top)
  }

}
