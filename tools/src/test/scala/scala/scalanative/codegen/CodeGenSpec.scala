package scala.scalanative
package codegen

import java.nio.file.Path

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

import org.junit.Assert._

import scala.scalanative.build.NativeConfig
import scala.scalanative.linker.ReachabilityAnalysis
import scalanative.build.{Config, ScalaNative}
import scalanative.util.Scope

/** Base class to test code generation */
abstract class CodeGenSpec extends OptimizerSpec {

  /** Performs code generation on the given sources.
   *
   *  @param entry
   *    The entry point for the linker.
   *  @param sources
   *    Map from file name to file content representing all the code to compile
   *  @param fn
   *    A function to apply to the products of the compilation.
   *  @return
   *    The result of applying `fn` to the resulting file.
   */
  def codegen[T](
      entry: String,
      sources: Map[String, String],
      setupConfig: NativeConfig => NativeConfig = identity
  )(
      f: (Config, ReachabilityAnalysis.Result, Seq[Path]) => T
  ): T =
    optimize(entry, sources, setupConfig.compose(_.withBaseName(entry))) {
      case (config, optimized) =>
        Scope { implicit in =>
          // Await each IR Future: the outer Future only starts them. Listing the
          // workdir early can see *-body.ll files that merge then deletes.
          val generators = Await.result(
            ScalaNative.codegen(config, optimized),
            1.minute
          )
          val outfiles =
            Await.result(Future.sequence(generators), 1.minute)

          assertTrue("Empty code generator output", outfiles.nonEmpty)

          f(config, optimized, outfiles)
        }
    }

}
