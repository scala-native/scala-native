package scala.scalanative
package optimizer

import nir._

/** Optimizer reporters can override one of the corresponding methods to
 *  get notified whenever one of the optimization events happens.
 */
object Optimizer {

  /** Run all of the passes on given assembly. */
  def apply(config: tools.Config,
            driver: Driver,
            assembly: Seq[Defn],
            reporter: Reporter): Seq[Defn] = {
    import reporter._

    val world  = analysis.ClassHierarchy(assembly)
    val passes = driver.passes.map(_.apply(config, world))

    def loop(assembly: Seq[Defn], passes: Seq[(Pass, Int)]): Seq[Defn] =
      passes match {
        case Seq() =>
          assembly

        case (pass.EmptyPass, _) +: rest =>
          loop(assembly, rest)

        case (pass, id) +: rest =>
          val passResult = pass(assembly)
          onPass(pass, passResult)
          loop(passResult, rest)
      }

    onStart(assembly)

    val result = loop(assembly, passes.zipWithIndex)

    onComplete(result)

    result
  }
}
