package scala.scalanative
package optimizer

import scala.collection.mutable
import nir._

/** Optimizer reporters can override one of the corresponding methods to
 *  get notified whenever one of the optimization events happens.
 */
object Optimizer {

  private def time[T](msg: String)(f: => T): T = {
    import java.lang.System.nanoTime
    val start = nanoTime()
    val res   = f
    val end   = nanoTime()
    println(s"[info] $msg (${(end - start) / 1000000} ms)")
    res
  }

  private def partition(defns: Seq[Defn]) = {
    val batches = java.lang.Runtime.getRuntime.availableProcessors * 4
    defns.groupBy { defn =>
      Math.abs(System.identityHashCode(defn)) % batches
    }
  }

  /** Run all of the passes on given assembly. */
  def apply(config: tools.Config,
            driver: Driver,
            assembly: Seq[Defn],
            dyns: Seq[String],
            reporter: Reporter): Seq[Defn] = {
    import reporter._

    val injects    = driver.passes.filter(_.isInjectionPass)
    val transforms = driver.passes.filterNot(_.isInjectionPass)
    val world      = analysis.ClassHierarchy(assembly, dyns)

    val injected = {
      val buf = mutable.UnrolledBuffer.empty[Defn]
      buf ++= assembly
      injects.foreach { make =>
        make(config, world) match {
          case NoPass         => ()
          case inject: Inject => inject(buf)
          case _              => util.unreachable
        }
      }
      buf
    }

    def loop(batchId: Int,
             batchDefns: Seq[Defn],
             passes: Seq[(AnyPass, Int)]): Seq[Defn] =
      passes match {
        case Seq() =>
          batchDefns

        case (NoPass, _) +: rest =>
          loop(batchId, batchDefns, rest)

        case (pass: Pass, passId) +: rest =>
          val passResult = pass.onDefns(batchDefns)
          onPass(batchId, passId, pass, passResult)
          loop(batchId, passResult, rest)
      }

    partition(injected).par
      .map {
        case (batchId, batchDefns) =>
          onStart(batchId, batchDefns)
          val passes = transforms.map(_.apply(config, world))
          val res    = loop(batchId, batchDefns, passes.zipWithIndex)
          onComplete(batchId, res)
          res
      }
      .seq
      .flatten
      .toSeq
  }
}
