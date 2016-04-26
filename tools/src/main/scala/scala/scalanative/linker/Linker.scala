package scala.scalanative
package linker

import scala.collection.mutable
import nir._
import nir.serialization._

final class Linker(paths: Seq[String]) {
  private val assemblies = paths.flatMap(Assembly(_))

  private def load(global: Global): Option[(Seq[Global], Defn)] =
    assemblies.collectFirst {
      case assembly if assembly.contains(global) =>
        assembly.load(global)
    }.flatten

  def link(entry: Global): (Seq[Global], Seq[Defn]) = {
    val resolved   = mutable.Set.empty[Global]
    var unresolved = mutable.Set.empty[Global]
    var worklist   = mutable.Stack[Global](entry)
    var defns      = mutable.UnrolledBuffer.empty[Defn]

    while (worklist.nonEmpty) {
      val workitem = worklist.pop()

      if (!workitem.isIntrinsic &&
          !resolved.contains(workitem) &&
          !unresolved.contains(workitem)) {

        load(workitem).fold[Unit] {
          unresolved += workitem
        } { case (deps, defn) =>
          resolved  += workitem
          defns     += defn
          worklist ++= deps
        }
      }
    }

    (unresolved.toSeq, defns.sortBy(_.name.toString).toSeq)
  }
}
