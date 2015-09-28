package salty.ir

import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                              Repeat => r, Newline => n}
import salty.util.sh
import salty.ir.{Schema => Sc}
import java.nio.ByteBuffer
import java.lang.System.{identityHashCode => id}

class GraphSerializer extends Pass {
  var shows = List.empty[Show.Result]

  def label(desc: Desc): String =
    (desc match {
      case Desc.Param(name) =>
        s"Param $name"
      case Desc.Label(name) =>
        s"Label $name"
      case dd: Desc.Defn =>
        labelDefn(dd)
      case _ =>
        desc.toString
    }).replace("\"", "\\\"")

  def labelDefn(desc: Desc.Defn): String = desc match {
    case Desc.Class(name)     => s"Class $name"
    case Desc.Interface(name) => s"Interface $name"
    case Desc.Module(name)    => s"Module $name"
    case Desc.Declare(name)   => s"Declare $name"
    case Desc.Define(name)    => s"Define $name"
    case Desc.Field(name)     => s"Field $name"
    case Desc.Extern(name)    => s"Extern $name"
    case Desc.Type(shape)     => s"Type $shape"
    case Desc.Primitive(name) => s"Prim $name"
  }

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
        s("[shape=box, style=filled, color=lightgrey, label=\"", label(node.desc), "\"]")
      case _ =>
        s("[shape=box, label=\"", label(node.desc), "\"]")
    }

  def key(node: Node) =
    s("\"", System.identityHashCode(node).toString, "\"")

  def onNode(node: Node) ={
    val k = key(node)
    shows = s(k, style(node)) :: shows
    node.edges.foreach { case (sc, next) =>
      shows = s(key(next), style(next), ";") ::
              s(key(next), " -> ", k, style(sc), ";") :: shows
    }
  }
}
object GraphSerializer extends ((Scope, ByteBuffer) => Unit) {
  def apply(scope: Scope, bb: ByteBuffer): Unit = {
    val res =
      s(scope.entries.toSeq.zipWithIndex.map { case ((_, node), idx) =>
        val pass = new GraphSerializer
        Pass.run(pass, node)
        s("digraph \"", idx.toString, "\" {",
            r(pass.shows.map(i)),
          n("}"))
      }.toSeq: _*)
    val bytes = res.toString.getBytes
    bb.put(bytes)
  }
}
