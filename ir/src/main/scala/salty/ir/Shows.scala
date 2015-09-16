package salty.ir

import salty.ir.Combinators._
import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                              Repeat => r, Newline => n}
import salty.util.sh

object ShowDOT {
  import System.{identityHashCode => id}
  def key(defn: Defn) = ???

  class ShowPass extends Pass {
    var shows = List.empty[Show.Result]
    def mnemonic(node: Node) =
      (node match {
        case _: Instr.Const =>
          node.toString
        case d: Defn =>
          defn(d)
        case _ =>
          node.getClass.getSimpleName.toString
      }).replace("\"", "\\\"")
    def key(node: Node) =
      s("\"", System.identityHashCode(node).toString, "\"")
    def style(edge: Edge) = edge match {
      case Edge.Val(_) => s()
      case Edge.Ef(_)  => s("[style=dashed]")
      case Edge.Cf(_)  => s("[style=bold]")
      case _           => s("[style=dotted]")
    }
    def defn(defn: Defn) = {
      val name = defn.name
      defn match {
        case _: Defn.Class     => s"class $name"
        case _: Defn.Interface => s"interface $name"
        case _: Defn.Module    => s"module $name"
        case _: Defn.Declare   => s"declare $name"
        case _: Defn.Define    => s"define $name"
        case _: Defn.Field     => s"field $name"
        case _: Defn.Extern    => s"extern $name"
      }
    }
    def style(node: Node) =
      node match {
        case _: Instr.Cf =>
          s("[shape=box, style=filled, color=lightgrey, label=\"", mnemonic(node), "\"]")
        case _ =>
          s("[shape=box, label=\"", mnemonic(node), "\"]")
      }
    def cross(node: Node, edges: Seq[Edge]) = {
      //node match {
        //case _: Instr =>
          val k = key(node)
          shows = s(k, style(node)) :: shows
          edges.foreach { e =>
            //e match {
              //case _: Edge.Val | _: Edge.Ef | _: Edge.Cf =>
                shows = s(key(e.node), style(e.node), ";") ::
                        s(key(e.node), " -> ", k, style(e), ";") :: shows
              //case _ =>
            //}
          }
        //case _ =>
      //}
    }
    def onNode(node: Node) =
      cross(node, Edge.of(node))
  }

  implicit val showScope: Show[Scope] = Show { scope =>
    val pass = new ShowPass
    scope.entries.map { case (_, defn) =>
      Pass.run(defn, pass)
    }
    s("digraph G {",
        r(pass.shows.map(i)),
      n("}"))
  }
}
