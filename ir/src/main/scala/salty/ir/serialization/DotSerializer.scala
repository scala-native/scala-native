package salty.ir
package serialization

import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                              Repeat => r, Newline => n}
import salty.util.sh
import salty.ir.{Schema => Sc}
import java.nio.ByteBuffer
import java.lang.System.{identityHashCode => id}
import DotSerializer._

class DotSerializer extends Pass {
  var shows = List.empty[Show.Result]

  def style(sc: Sc) = sc match {
    case Sc.Val => s()
    case Sc.Ef  => s("[style=dashed]")
    case Sc.Cf  => s("[style=bold]")
    case Sc.Ref => s("[style=dotted]")
    case _      => throw new Exception("unreachable")
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
    node.edges.foreach { case (sc, next) =>
      if (next.get ne Empty) {
        define(next)
        shows = s(key(next), " -> ", k, style(sc)) :: shows
      }
    }
    //node.uses.foreach { case slot: Slot =>
    //  define(slot.node)
    //  shows = s(key(slot.node), " -> ", k, "[color=red, style=dotted]") :: shows
    //}
  }
}
object DotSerializer {
  def label(node: Node): String = {
    val body =
      (node.desc match {
        case desc: Desc.Plain => desc.toString
        case Desc.I8(v)       => s"${v}i8"
        case Desc.I16(v)      => s"${v}i16"
        case Desc.I32(v)      => s"${v}i32"
        case Desc.I64(v)      => s"${v}i64"
        case Desc.F32(v)      => s"${v}f32"
        case Desc.F64(v)      => s"${v}f64"
        case Desc.Str(v)      => "\"" + v + "\""
      }).replace("\"", "\\\"")

    node.name match {
      case Name.No => body
      case name    => s"$body $name"
    }
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
