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

  private val writer = new java.io.PrintWriter(new java.io.File("out.dot"))
  private def writeStart(): Unit =
    writer.println("digraph G {")
  private def writeEdge(from: Global, to: Global): Unit = {
    def quoted(s: String) = "\"" + s + "\""
    writer.print(quoted(sh"$from".toString))
    writer.print("->")
    writer.print(quoted(sh"$to".toString))
    writer.println(";")
  }
  private def writeEnd(): Unit = {
    writer.println("}")
    writer.close()
  }

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
                  writeEdge(workitem, dep)
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
          writeEdge(cond, dep)
          direct.push(dep)

        case dep =>
          rest += dep
      }

      conditional = rest
    }

    writeStart()
    writeEdge(Global.Val("main"), entry)
    direct.push(entry)
    Rt.pinned.foreach(direct.push)
    while (direct.nonEmpty) {
      processDirect
      processConditional
    }
    writeEnd()

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
