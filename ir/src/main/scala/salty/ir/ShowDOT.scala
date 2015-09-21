package salty.ir

import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                              Repeat => r, Newline => n}
import salty.util.sh
import salty.ir.{Graph => G}

object ShowDOT {
  import System.{identityHashCode => id}
  def key(defn: Defn) = ???

  /*
  class ShowPass extends G.Pass {
    var shows = List.empty[Show.Result]
    def mnemonic(node: G.Node) =
      (node match {
        case tag: Instr.Tag =>
          "Tag"
        case _: Instr.Const =>
          node.toString
        case param: Instr.Param =>
          s"Param ${param.name}"
        case label: Instr.Label =>
          s"Label ${label.name}"
        case d: Defn =>
          defn(d)
        case _ =>
          node.getClass.getSimpleName.toString
      }).replace("\"", "\\\"")
    def key(node: G.Node) =
      s("\"", System.identityHashCode(node).toString, "\"")
    def style(edge: G.Edge) = edge.tag match {
      case Tag.Edge.Val => s()
      case Tag.Edge.Ef  => s("[style=dashed]")
      case Tag.Edge.Cf  => s("[style=bold]")
      case _            => s("[style=dotted]")
    }
    def defn(defn: Defn) = {
      val name = defn.name
      defn match {
        case _: Defn.Class     => s"Class $name"
        case _: Defn.Interface => s"Interface $name"
        case _: Defn.Module    => s"Module $name"
        case _: Defn.Declare   => s"Declare $name"
        case _: Defn.Define    => s"Define $name"
        case _: Defn.Field     => s"Field $name"
        case _: Defn.Extern    => s"Extern $name"
      }
    }
    def style(node: G.Node) =
      node match {
        case _: Instr.Cf =>
          s("[shape=box, style=filled, color=lightgrey, label=\"", mnemonic(node), "\"]")
        case _ =>
          s("[shape=box, label=\"", mnemonic(node), "\"]")
      }
    def onNode(node: G.Node) ={
      //node match {
      //  case _: Instr =>
          val k = key(node)
          shows = s(k, style(node)) :: shows
          node.edges.foreach { e =>
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
  }
  */

  implicit val showScope: Show[Scope] = Show { scope =>
    ???
    /*
    s(scope.entries.map { case (_, defn) =>
      val pass = new ShowPass
      Pass.run(defn, pass)
      s("digraph \"", defn.name.toString, "\" {",
          r(pass.shows.map(i)),
        n("}"))
    }.toSeq: _*)
    */
  }
}
