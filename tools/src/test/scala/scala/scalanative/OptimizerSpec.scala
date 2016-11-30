package scala.scalanative

import optimizer.Driver
import tools.Config

/** Base class to test the optimizer */
abstract class OptimizerSpec extends LinkerSpec {

  /**
   * Runs the optimizer defined by `driver` on `sources`.
   * The code will first be linked using `entry` as entry point.
   *
   * @param entry   The entry point for the linker.
   * @param sources Map from file name to file content representing all the code
   *                to compile and optimize.
   * @param driver  The driver that defines the pipeline.
   * @param fn      A function to apply to the products of the compilation.
   * @return The result of applying `fn` to the resulting definitions.
   */
  def optimize[T](entry: String,
                  sources: Map[String, String],
                  driver: Driver = Driver())(fn: (Config, Seq[nir.Attr.Link],
                                                  Seq[nir.Defn]) => T): T =
    link(entry, sources, driver) {
      case (config, links, assembly) =>
        fn(config, links, tools.optimize(config, driver, assembly))
    }

}
