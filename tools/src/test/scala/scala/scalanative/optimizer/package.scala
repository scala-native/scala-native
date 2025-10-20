package scala.scalanative

import org.junit.Assert.*
import scala.scalanative.linker.ReachabilityAnalysis

package object optimizer {

  def assertContainsAll[T](
      msg: String,
      expected: Iterable[T],
      actual: Iterable[T]
  ) = {
    val left = expected.toSeq
    val right = actual.toSeq
    val diff = left.diff(right)
    assertTrue(s"$msg - not found ${diff} in $right", diff.isEmpty)
  }

  def assertContains[T](msg: String, expected: T, actual: Iterable[T]) = {
    assertTrue(
      s"$msg - not found ${expected} in ${actual.toSeq}",
      actual.find(_ == expected).isDefined
    )
  }

  def assertDistinct(localNames: Iterable[nir.LocalName]) = {
    val duplicated =
      localNames.groupBy(identity).filter(_._2.size > 1).map(_._1)
    assertTrue(s"Found duplicated names of ${duplicated}", duplicated.isEmpty)
  }

  def namedLets(defn: nir.Defn.Define): Map[nir.Inst.Let, nir.LocalName] =
    defn.insts.collect {
      case inst: nir.Inst.Let if defn.debugInfo.localNames.contains(inst.id) =>
        inst -> defn.debugInfo.localNames(inst.id)
    }.toMap

  protected[optimizer] def findEntry(
      linked: Seq[nir.Defn],
      entryName: String = "Test"
  ): Option[nir.Defn.Define] = {
    object TestMain {
      val TestModule = nir.Global.Top(s"$entryName$$")
      val CompanionMain =
        TestModule.member(
          nir.Rt.ScalaMainSig.copy(scope = nir.Sig.Scope.Public)
        )

      def unapply(name: nir.Global): Boolean = name match {
        case CompanionMain                      => true
        case nir.Global.Member(TestModule, sig) =>
          sig.unmangled match {
            case nir.Sig.Duplicate(of, _) =>
              of == CompanionMain.sig
            case _ =>
              false
          }
        case _ => false
      }
    }
    object TestMainForwarder {
      val staticForwarder = nir.Global.Top("Test").member(nir.Rt.ScalaMainSig)
      def unapply(name: nir.Global): Boolean = name == staticForwarder
    }
    val companionMethod = linked
      .collectFirst {
        case defn @ nir.Defn.Define(_, TestMain(), _, _, _) => defn
      }
    def staticForwarder = linked
      .collectFirst {
        case defn @ nir.Defn.Define(_, TestMainForwarder(), _, _, _) => defn
      }
    companionMethod
      .orElse(staticForwarder)
      .ensuring(_.isDefined, "Not found linked method")
  }

  def afterLowering(
      config: build.Config,
      optimized: => ReachabilityAnalysis.Result
  )(
      fn: Seq[nir.Defn] => Unit
  ): Unit = {
    import scala.scalanative.codegen.*
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.*
    val defns = optimized.defns
    implicit def logger: build.Logger = config.logger
    implicit val platform: PlatformInfo = PlatformInfo(config)
    implicit val meta: Metadata =
      new Metadata(optimized, config, Nil)
    val lowered = llvm.CodeGen.lower(defns)
    Await.result(lowered.map(fn), duration.Duration.Inf)
  }

}
