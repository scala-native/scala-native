package scala.scalanative

import scala.scalanative.build.{Config, NativeConfig, Mode, ScalaNative}
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.scalanative.linker.ReachabilityAnalysis

/** Base class to test the optimizer */
abstract class OptimizerSpec extends LinkerSpec {

  /** Runs the optimizer defined on `sources`. The code will first be linked
   *  using `entry` as entry point.
   *
   *  @param entry
   *    The entry point for the linker.
   *  @param sources
   *    Map from file name to file content representing all the code to compile
   *    and optimize.
   *  @param fn
   *    A function to apply to the products of the compilation.
   *  @return
   *    The result of applying `fn` to the resulting definitions.
   */
  def optimize[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: NativeConfig => NativeConfig = identity
  )(
      fn: (Config, ReachabilityAnalysis.Result) => T
  ): T =
    link(entry, sources, setupConfig) {
      case (config, linked) =>
        val optimized = ScalaNative.optimize(config, linked)
        val result = Await.result(optimized, Duration.Inf)
        fn(config, result)
    }

  protected def findEntry(linked: Seq[nir.Defn]): Option[nir.Defn.Define] = {
    import OptimizerSpec.*
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
}

object OptimizerSpec {
  private object TestMain {
    val TestModule = nir.Global.Top("Test$")
    val CompanionMain =
      TestModule.member(nir.Rt.ScalaMainSig.copy(scope = nir.Sig.Scope.Public))

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
  private object TestMainForwarder {
    val staticForwarder = nir.Global.Top("Test").member(nir.Rt.ScalaMainSig)
    def unapply(name: nir.Global): Boolean = name == staticForwarder
  }
}
