package scala.scalanative
package linker

import java.io.{File, PrintWriter}
import scala.collection.mutable
import nir._
import nir.serialization._
import nir.Shows._
import util.sh

final class Linker(dotpath: Option[String], paths: Seq[String]) {
  private val assemblies: Seq[Assembly] = paths.flatMap(Assembly(_))

  private def load(global: Global): Option[(Seq[Dep], Seq[Attr.Link], Defn)] =
    assemblies.collectFirst {
      case assembly if assembly.contains(global) =>
        assembly.load(global)
    }.flatten

  private val writer = dotpath.map(path => new PrintWriter(new File(path)))

  private def writeStart(): Unit =
    writer.foreach { writer =>
      writer.println("digraph G {")
    }

  private def writeEdge(from: Global, to: Global): Unit =
    writer.foreach { writer =>
      def quoted(s: String) = "\"" + s + "\""
      writer.print(quoted(sh"$from".toString))
      writer.print("->")
      writer.print(quoted(sh"$to".toString))
      writer.println(";")
    }

  private def writeEnd(): Unit =
    writer.foreach { writer =>
      writer.println("}")
      writer.close()
    }

  def link(entries: Seq[Global]): (Seq[Global], Seq[Attr.Link], Seq[Defn]) = {
    val resolved    = mutable.Set.empty[Global]
    val unresolved  = mutable.Set.empty[Global]
    val links       = mutable.Set.empty[Attr.Link]
    val defns       = mutable.UnrolledBuffer.empty[Defn]
    val direct      = mutable.Stack.empty[Global]
    var conditional = mutable.UnrolledBuffer.empty[Dep.Conditional]

    def processDirect =
      while (direct.nonEmpty) {
        val workitem = direct.pop()

        if (!workitem.isIntrinsic && !resolved.contains(workitem) &&
            !unresolved.contains(workitem)) {

          load(workitem).fold[Unit] {
            unresolved += workitem
          } {
            case (deps, newlinks, defn) =>
              resolved += workitem
              defns += defn
              links ++= newlinks

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
    entries.foreach(writeEdge(Global.Top("main"), _))
    direct.pushAll(entries)
    while (direct.nonEmpty) {
      processDirect
      processConditional
    }
    writeEnd()

    (unresolved.toSeq, links.toSeq, defns.sortBy(_.name.toString).toSeq)
  }

  def linkClosed(entries: Seq[Global]): (Seq[Attr.Link], Seq[Defn]) = {
    val (unresolved, links, defns) = link(entries)

    assemblies.foreach(_.close)

    if (unresolved.nonEmpty) {
      println(s"Unresolved dependencies:")
      unresolved.map(u => sh"  `$u`".toString).sorted.foreach(println(_))
      throw new LinkingError("Failed to resolve all dependencies.")
    }

    (links, defns)
  }
}
