package scala.scalanative
package optimizer

import scala.collection.mutable
import nir._

/** Optimizer reporters can override one of the corresponding methods to
 *  get notified whenever one of the optimization events happens.
 */
object Optimizer {

  def partition(defns: Seq[Defn]) = {
    val procs   = java.lang.Runtime.getRuntime.availableProcessors
    val batches = procs * procs
    defns.groupBy { defn =>
      Math.abs(System.identityHashCode(defn)) % batches
    }
  }

  /** Run all of the passes on given assembly. */
  def apply(config: build.Config,
            driver: Driver,
            assembly: Seq[Defn]): Seq[Defn] = {
    val reporter = driver.optimizerReporter
    import reporter._

    val top = sema.Sema(assembly)

    def loop(batchId: Int,
             batchDefns: Seq[Defn],
             passes: Seq[(Pass, Int)]): Seq[Defn] =
      passes match {
        case Seq() =>
          batchDefns

        case (pass: Pass, passId) +: rest =>
          val passResult = batchDefns.map {
            case defn: Defn.Define =>
              defn.copy(insts = pass.onInsts(defn.insts))
            case defn =>
              defn
          }
          onPass(batchId, passId, pass, passResult)
          loop(batchId, passResult, rest)
      }

    partition(assembly).par
      .map {
        case (batchId, batchDefns) =>
          onStart(batchId, batchDefns)
          val passes = driver.passes.map(_.apply(config, top))
          val res    = loop(batchId, batchDefns, passes.zipWithIndex)
          onComplete(batchId, res)
          res
      }
      .seq
      .flatten
      .toSeq
  }
}
