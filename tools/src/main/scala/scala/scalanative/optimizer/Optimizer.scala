package scala.scalanative
package optimizer

import nir._

object Optimizer {

  /** Run all of the passes on given assembly. */
  def apply(config: tools.Config,
            driver: tools.Driver,
            assembly: Seq[Defn]): Seq[Defn] = {
    val world  = analysis.ClassHierarchy(assembly)
    val passes = driver.passes.map(_.apply(config, world))

    def loop(assembly: Seq[Defn], passes: Seq[(Pass, Int)]): Seq[Defn] =
      passes match {
        case Seq() =>
          assembly

        case (pass.EmptyPass, _) +: rest =>
          loop(assembly, rest)

        case (pass, id) +: rest =>
          loop(pass(assembly), rest)
      }

    loop(assembly, passes.zipWithIndex)
  }
}
