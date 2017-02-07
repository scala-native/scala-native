package scala.scalanative
package optimizer

import scala.collection.mutable
import nir._

/** Optimizer reporters can override one of the corresponding methods to
 *  get notified whenever one of the optimization events happens.
 */
object Optimizer {

  def time[T](msg: String)(f: => T): T = {
    import java.lang.System.nanoTime
    val start = nanoTime()
    val res   = f
    val end   = nanoTime()
    println(s"--- $msg (${(end - start) / 1000000} ms)")
    res
  }

  /** Run all of the passes on given assembly. */
  def apply(config: tools.Config,
            driver: Driver,
            assembly: Seq[Defn],
            dyns: Seq[String],
            reporter: Reporter): Seq[Defn] = {
    import reporter._

    val world = time("class hierarchy analysis") {
      analysis.ClassHierarchy(assembly, dyns)
    }
    val passes = driver.passes.map(_.apply(config, world))

    def loop(defns: Seq[Defn], passes: Seq[(Pass, Int)]): Seq[Defn] =
      passes match {
        case Seq() =>
          defns

        case (pass.EmptyPass, _) +: rest =>
          loop(defns, rest)

        case (pass, id) +: rest =>
          val passResult = time(s"pass #$id ${pass.getClass.getName}") {
            pass.onDefns(defns)
          }
          onPass(pass, passResult)
          loop(passResult, rest)
      }

    onStart(assembly)

    val result = loop(assembly, passes.zipWithIndex)

    onComplete(result)

    result
  }
}
