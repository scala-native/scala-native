package scala.scalanative
package linker

import scala.collection.mutable
import nir._
import nir.serialization._
import nir.Shows._
import util.sh

final class Linker(paths: Seq[String]) {
  private val assemblies = paths.flatMap(Assembly(_))

  private def load(global: Global): Option[(Seq[Global], Defn)] =
    assemblies.collectFirst {
      case assembly if assembly.contains(global) =>
        assembly.load(global)
    }.flatten

  private val writer =
    new java.io.PrintWriter(new java.io.File("out.dot"))
  private def writeStart(): Unit =
    writer.println("digraph G {\n")
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
    var unresolved = mutable.Set.empty[Global]
    var worklist   = mutable.Stack[Global](entry)
    var defns      = mutable.UnrolledBuffer.empty[Defn]

    writeStart()
    while (worklist.nonEmpty) {
      val workitem = worklist.pop()

      if (!workitem.isIntrinsic &&
          !resolved.contains(workitem) &&
          !unresolved.contains(workitem)) {

        load(workitem).fold[Unit] {
          unresolved += workitem
        } { case (deps, defn) =>
          deps.foreach(dep => writeEdge(workitem, dep))

          resolved  += workitem
          defns     += defn
          worklist ++= deps
        }
      }
    }
    writeEnd()

    (unresolved.toSeq, defns.sortBy(_.name.toString).toSeq)
  }
}
