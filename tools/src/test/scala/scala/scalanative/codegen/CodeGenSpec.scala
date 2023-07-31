package scala.scalanative
package codegen

import java.nio.file.{Path, Paths}
import scalanative.io.VirtualDirectory
import scalanative.build.Config
import scalanative.build.ScalaNative
import scalanative.util.Scope
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import org.junit.Assert._

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
  def codegen[T](entry: String, sources: Map[String, String])(
      f: (Config, linker.Result, Path) => T
  ): T =
    optimize(entry, sources) {
      case (config, optimized) =>
        Scope { implicit in =>
          val codeGen = ScalaNative.codegen(config, optimized)
          Await.ready(codeGen, 1.minute)
          val workDir = VirtualDirectory.real(config.workDir)
          val outfile = Paths.get("out.ll")

          assertTrue("out.ll not found.", workDir.contains(outfile))

          f(config, optimized, outfile)
        }
    }

}
