package scala.scalanative
package codegen

import java.nio.file.{Path, Paths}
import scalanative.io.VirtualDirectory
import scalanative.build.{Config, ScalaNative}
import scala.scalanative.linker.ReachabilityAnalysis
import scalanative.util.Scope
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

import org.junit.Assert.*
import scala.scalanative.build.NativeConfig
import java.nio.file.Files

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
          val codeGen = ScalaNative.codegen(config, optimized)
          val _ = Await.result(codeGen, 1.minute)
          Thread.sleep(1000)

          val outfiles = Files
            .list(config.workDir.resolve("generated"))
            .toArray
            .toSeq
            .asInstanceOf[Seq[Path]]

          assertTrue("Empty code generator output", outfiles.nonEmpty)

          f(config, optimized, outfiles)
        }
    }

}
