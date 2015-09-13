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
    def key(node: Node): String =
      s""""${node.getClass.getSimpleName.toString} # ${System.identityHashCode(node)}""""
    def cross(node: Node, edges: Seq[Edge]) = {
      val k = key(node)
      edges.foreach { e => shows = s(key(e.node), " -> ", k) :: shows }
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
