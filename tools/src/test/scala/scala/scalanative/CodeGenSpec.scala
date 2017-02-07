package scala.scalanative

import io.VirtualFile
import optimizer.Driver
import java.nio.file.Paths
import tools.Config

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
      fn: (Config, Seq[nir.Attr.Link], VirtualFile) => T): T =
    optimize(entry, sources, driver) {
      case (config, links, assembly) =>
        tools.codegen(config, assembly)
        val llFile =
          config.targetDirectory.get(Paths.get("out.ll")) getOrElse fail(
            "out.ll not found.")

        fn(config, links, llFile)
    }

}
