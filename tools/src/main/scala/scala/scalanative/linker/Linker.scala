package scala.scalanative
package linker

import scala.collection.mutable
import nir._
import nir.serialization._
import nir.Shows._
import util.sh

final class Linker(paths: Seq[String]) {
  private val assemblies = paths.flatMap(Assembly(_))

  private def load(global: Global): Option[(Seq[Dep], Defn)] =
    assemblies.collectFirst {
      case assembly if assembly.contains(global) =>
        assembly.load(global)
    }.flatten

  def link(entry: Global): (Seq[Global], Seq[Defn]) = {
    val resolved   = mutable.Set.empty[Global]
    val unresolved = mutable.Set.empty[Global]
    val defns      = mutable.UnrolledBuffer.empty[Defn]
    val direct     = mutable.Stack.empty[Global]
    var conditional = mutable.UnrolledBuffer.empty[Dep.Conditional]

    def processDirect =
      while (direct.nonEmpty) {
        val workitem = direct.pop()

        if (!workitem.isIntrinsic && !resolved.contains(workitem) &&
            !unresolved.contains(workitem)) {

          load(workitem).fold[Unit] {
            unresolved += workitem
          } {
            case (deps, defn) =>
              resolved += workitem
              defns += defn

              deps.foreach {
                case Dep.Direct(dep) =>
                  direct.push(dep)

                case cond: Dep.Conditional =>
                  conditional += cond
              }
          }
        }
      }

    def processConditional = {
      val rest = mutable.UnrolledBuffer.empty[Dep.Conditional]

      conditional.foreach {
        case Dep.Conditional(dep, cond)
            if resolved.contains(dep) || unresolved.contains(dep) =>
          ()

        case Dep.Conditional(dep, cond) if resolved.contains(cond) =>
          direct.push(dep)

        case dep =>
          rest += dep
      }

      conditional = rest
    }

    direct.push(entry)
    Rt.pinned.foreach(direct.push)
    while (direct.nonEmpty) {
      processDirect
      processConditional
    }

    (unresolved.toSeq, defns.sortBy(_.name.toString).toSeq)
  }

  def linkClosed(entry: Global): Seq[Defn] = {
    val (unresolved, defns) = link(entry)

    if (unresolved.nonEmpty) {
      println(s"Unresolved dependencies:")
      unresolved.map(u => sh"  `$u`".toString).sorted.foreach(println(_))
      throw new LinkingError("Failed to resolve all dependencies.")
    }

    defns
  }
}
