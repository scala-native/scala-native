package scala.scalanative

import build.{ScalaNative, Config, Mode}

/** Base class to test the optimizer */
abstract class OptimizerSpec extends LinkerSpec {

  /**
   * Runs the optimizer defined on `sources`.
   * The code will first be linked using `entry` as entry point.
   *
   * @param entry   The entry point for the linker.
   * @param sources Map from file name to file content representing all the code
   *                to compile and optimize.
   * @param fn      A function to apply to the products of the compilation.
   * @return The result of applying `fn` to the resulting definitions.
   */
  def optimize[T](entry: String, sources: Map[String, String])(
      fn: (Config, linker.Result) => T): T =
    link(entry, sources) {
      case (config, linked) =>
        val optimized = ScalaNative.optimize(config, linked)
        fn(config, optimized)
    }

}
