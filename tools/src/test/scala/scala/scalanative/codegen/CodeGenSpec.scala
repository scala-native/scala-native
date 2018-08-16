package scala.scalanative
package codegen

import java.nio.file.{Path, Paths}
import scalanative.io.VirtualDirectory
import scalanative.optimizer.Driver
import scalanative.build.{Config, ScalaNative}
import scalanative.util.Scope

/** Base class to test code generation */
abstract class CodeGenSpec extends OptimizerSpec {

  /**
   * Performs code generation on the given sources.
   *
   * @param entry   The entry point for the linker.
   * @param sources Map from file name to file content representing all the code
   *                to compile
   * @param fn      A function to apply to the products of the compilation.
   * @param driver  The driver that defines the pipeline.
   * @return The result of applying `fn` to the resulting file.
   */
  def codegen[T](entry: String,
                 sources: Map[String, String],
                 driver: Option[Driver] = None)(
      f: (Config, Seq[nir.Attr.Link], Path) => T): T =
    optimize(entry, sources, driver) {
      case (config, links, assembly) =>
        Scope { implicit in =>
          val entries = ScalaNative.entries(config)
          ScalaNative.codegen(config, entries, assembly, Seq.empty)
          val workdir = VirtualDirectory.real(config.workdir)
          val outfile = Paths.get("out.ll")

          assert(workdir.contains(outfile), "out.ll not found.")

          f(config, links, outfile)
        }
    }

}
