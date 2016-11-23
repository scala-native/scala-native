package scala.scalanative
package linker

import java.io.{File, PrintWriter}
import scala.collection.mutable
import nir._
import nir.serialization._
import nir.Shows._
import util.sh
import util.unsupported


final class Linker(dotpath: Option[String], paths: Seq[String]) {
  private val assemblies: Seq[Assembly] = paths.flatMap(Assembly(_))

  private def load(global: Global): Option[(Seq[Dep], Seq[Attr.Link], Seq[String], Defn)] =
    assemblies.collectFirst {
      case assembly if assembly.contains(global) =>
        assembly.load(global)
    }.flatten

  private val writer = dotpath.map(path => new PrintWriter(new File(path)))

  private def writeStart(): Unit =
    writer.foreach { writer =>
      writer.println("digraph G {")
    }

  private def writeEdge(from: String, to: Global): Unit =
    writer.foreach { writer =>
      def quoted(s: String) = "\"" + s + "\""
      writer.print(quoted(sh"$from".toString))
      writer.print("->")
      writer.print(quoted(sh"$to".toString))
      writer.println(";")
    }

  private def writeEdge(from: Global, to: Global): Unit =
    writer.foreach { writer =>
      def quoted(s: String) = "\"" + s + "\""
      writer.print(sh"$from".toString)
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
    val weak        = mutable.Set.empty[Global] // use multi map for constant lookup with new signatures ?
    val dyn         = mutable.Set.empty[String]
    val direct      = mutable.Stack.empty[Global]
    var conditional = mutable.UnrolledBuffer.empty[Dep.Conditional]
    val structuralMethods = mutable.Set.empty[Global]

    def processDirect =
      while (direct.nonEmpty) {
        val workitem = direct.pop()

        if (!workitem.isIntrinsic && !resolved.contains(workitem) &&
            !unresolved.contains(workitem)) {

          load(workitem).fold[Unit] {
            unresolved += workitem
          } {
            case (deps, newlinks, newDyns, defn) =>
              resolved += workitem
              defns += defn
              links ++= newlinks
              dyn ++= newDyns

              // Comparing new signatures with old weak dependencies
              newDyns.flatMap(s => weak.collect { case g if g.id == s => g } ).foreach {
                global =>
                  writeEdge(global.id, global)
                  direct.push(global)
                  structuralMethods += global
              }

              deps.foreach {
                case Dep.Direct(dep) =>
                  writeEdge(workitem, dep)
                  direct.push(dep)

                case cond: Dep.Conditional =>
                  conditional += cond

                case Dep.Weak(g) =>
                      def genSignature(global: Global): String = {
                        val fullSignature = global.id
                        val index = fullSignature.lastIndexOf("_")
                        if(index != -1) {
                          fullSignature.substring(0, index)
                        } else {
                          fullSignature
                        }
                      }
                  // comparing new dependencies with all signatures
                  if(dyn(genSignature(g))) {
                    writeEdge(g.id, g)
                    direct.push(g)
                    structuralMethods += g
                  }

                  weak += g
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

    val defnss = defns.sortBy(_.name.toString).toSeq.map {
      case Defn.Define(attrs, name, ty, insts) if structuralMethods.contains(name) =>
        Defn.Define(Attrs.fromSeq(attrs.toSeq :+ Attr.Dyn), name, ty, insts)
      case defn => defn
    }

    (unresolved.toSeq, links.toSeq, defnss)
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
