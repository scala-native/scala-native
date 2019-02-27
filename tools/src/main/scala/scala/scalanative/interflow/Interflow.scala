package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker._

class Interflow(val mode: build.Mode, val originals: Map[Global, Defn.Define])(
    implicit val linked: linker.Result)
    extends Visit
    with Eval
    with Inline
    with Intrinsics
    with Log {
  val todo      = mutable.Queue.empty[Global]
  val done      = mutable.Map.empty[Global, Defn.Define]
  val started   = mutable.Set.empty[Global]
  val blacklist = mutable.Set.empty[Global]
  var context   = List.empty[String]

  val mergeProcessor = new util.ScopedVar[MergeProcessor]
  val blockFresh     = new util.ScopedVar[Fresh]
  val modulePurity   = mutable.Map.empty[Global, Boolean]
}

object Interflow {
  def apply(config: build.Config,
            linked: linker.Result,
            defns: Seq[Defn]): Seq[Defn] = {
    val defnsMap = defns.collect {
      case defn: Defn.Define =>
        defn.name -> defn
    }.toMap
    val interflow = new Interflow(config.mode, defnsMap)(linked)
    linked.entries.foreach(interflow.visitEntry)
    interflow.visitLoop()
    val done = interflow.done.values.map { defn =>
      defn.name -> defn
    }.toMap
    val result = mutable.UnrolledBuffer.empty[Defn]
    result ++= done.values
    result ++= defns.iterator.filterNot(defn => done.contains(defn.name))
    result.sortBy { defn =>
      defn.name match {
        case Global.Member(Global.Top(id), sig) =>
          (id, sig.mangle)
        case Global.Top(id) =>
          (id, "")
        case _ =>
          ("", "")
      }
    }
  }
}
