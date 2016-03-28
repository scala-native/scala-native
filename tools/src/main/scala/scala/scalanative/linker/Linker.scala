package scala.scalanative
package linker

import scala.collection.mutable
import nir._
import nir.serialization._

final class Linker(paths: Seq[String]) {
  private val assemblies = paths.map(Assembly(_))

  private def load(global: Global): Option[(Seq[Global], Defn)] =
    assemblies.collectFirst {
      case assembly if assembly.contains(global) =>
        assembly.load(global)
    }.flatten

  def link(entry: Global): Seq[Defn] = {
    val loaded = mutable.Set.empty[Global]
    var deps   = mutable.Stack[Global](entry)
    var defns  = mutable.UnrolledBuffer.empty[Defn]

    while (deps.nonEmpty) {
      val dep = deps.pop()
      if (!loaded.contains(dep) && !dep.isIntrinsic) {
        println(s"looking for $dep")
        val (newdeps, newdefn) = load(dep).get
        deps.pushAll(newdeps)
        defns  += newdefn
        loaded += dep
      }
    }

    defns.toSeq
  }
}
