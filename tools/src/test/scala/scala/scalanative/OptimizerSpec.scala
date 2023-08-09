package scala.scalanative

import scala.scalanative.build.{Config, NativeConfig, Mode, ScalaNative}
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.scalanative.nir._

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
      fn: (Config, linker.Result) => T
  ): T =
    link(entry, sources, setupConfig) {
      case (config, linked) =>
        val optimized = ScalaNative.optimize(config, linked)
        val result = Await.result(optimized, Duration.Inf)
        fn(config, result)
    }

  protected def findEntry(linked: Seq[Defn]): Option[Defn.Define] = {
    import OptimizerSpec._
    val companionMethod = linked
      .collectFirst { case defn @ Defn.Define(_, TestMain(), _, _) => defn }
    def staticForwarder = linked
      .collectFirst {
        case defn @ Defn.Define(_, TestMainForwarder(), _, _) => defn
      }
    companionMethod
      .orElse(staticForwarder)
      .ensuring(_.isDefined, "Not found linked method")
  }
}

object OptimizerSpec {
  private object TestMain {
    val TestModule = Global.Top("Test$")
    val CompanionMain =
      TestModule.member(Rt.ScalaMainSig.copy(scope = Sig.Scope.Public))

    def unapply(name: Global): Boolean = name match {
      case CompanionMain => true
      case Global.Member(TestModule, sig) =>
        sig.unmangled match {
          case Sig.Duplicate(of, _) => of == CompanionMain.sig
          case _                    => false
        }
      case _ => false
    }
  }
  private object TestMainForwarder {
    val staticForwarder = Global.Top("Test").member(Rt.ScalaMainSig)
    def unapply(name: Global): Boolean = name == staticForwarder
  }
}
