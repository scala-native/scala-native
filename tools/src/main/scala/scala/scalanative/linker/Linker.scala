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
    var deps       = mutable.Stack[Global](entry)
    var defns      = mutable.UnrolledBuffer.empty[Defn]
    val resolved   = mutable.Set.empty[Global]
    var unresolved = mutable.Set.empty[Global]

    while (deps.nonEmpty) {
      val dep = deps.pop()
      if (!resolved.contains(dep) && !dep.isIntrinsic) {
        load(dep).fold {
          unresolved += dep
        } { case (newdeps, newdefn) =>
          deps.pushAll(newdeps)
          defns    += newdefn
          resolved += dep
        }
      }
    }

    (unresolved.toSeq, defns.toSeq)
  }
}
