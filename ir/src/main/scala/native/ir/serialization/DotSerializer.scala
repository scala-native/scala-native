package native
package ir
package serialization

import native.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                               Repeat => r, Newline => n}
import native.util.sh
import native.ir.{Schema => Sc}
import java.nio.ByteBuffer
import java.lang.System.{identityHashCode => id}
import DotSerializer._

class DotSerializer extends Pass {
  var shows = List.empty[Show.Result]

  def style(dep: Dep) = () match {
    case _ if dep.isVal  => s()
    case _ if dep.isEf   => s("[style=dashed]")
    case _ if dep.isCf   => s("[style=bold]")
    case _ if dep.isDefn => s("[style=dotted]")
    case _               => throw new Exception("unreachable")
  }

  def style(node: Node) =
    node.desc match {
      case _: Desc.Cf =>
        s("[shape=box, style=filled, color=lightgrey, label=\"", label(node), "\"]")
      case Desc.Defn.Extern =>
        s("[shape=box, color=red, label=\"", label(node), "\"]")
      case _ =>
        s("[shape=box, label=\"", label(node), "\"]")
    }

  def key(node: Node) =
    s("\"", System.identityHashCode(node).toString, "\"")

  var defined = Set.empty[Node]
  def define(node: Node) =
    if (!defined.contains(node) && (node ne Empty)) {
      shows = s(key(node), style(node)) :: shows
      defined = defined + node
    }

  def onNode(node: Node): Unit = {
    val k = key(node)
    define(node)
    node.deps.foreach { d =>
      if (d.dep ne Empty) {
        define(d.dep)
        shows = s(key(d.dep), " -> ", k, style(d)) :: shows
      }
    }
    //node.uses.foreach { case slot: Slot =>
    //  define(slot.node)
    //  shows = s(key(slot.node), " -> ", k, "[color=red, style=dotted]") :: shows
    //}
  }
}
object DotSerializer {
  def label(node: Node): String =
    node.name match {
      case Name.No => node.desc.toString
      case name    => s"${node.desc} $name"
    }

  implicit val showScope: Show[Scope] = Show { scope =>
    s(r(scope.entries.values.toSeq.map(n(_))))
  }

  implicit val showNode: Show[Node] = Show { node =>
    val pass = new DotSerializer
    Pass.run(pass, node)
    s("digraph \"", label(node), "\" {",
        r(pass.shows.map(i)),
      n("}"))
  }
}
