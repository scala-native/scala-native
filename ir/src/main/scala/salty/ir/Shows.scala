package salty.ir

import salty.ir.Combinators._
import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                              Repeat => r, Newline => n}
import salty.util.sh

abstract class Pass {
  def onNode(node: Node): Unit
}
object Pass {
  def run(entry: Node, pass: Pass) = {
    val epoch = Node.nextEpoch
    def loop(node: Node): Unit =
      if (node.epoch < epoch) {
        node.epoch = epoch
        pass.onNode(node)
        node.next.foreach(loop)
      }
    loop(entry)
  }
}

object ShowDOT {
  import System.{identityHashCode => id}
  def key(defn: Defn) = ???

  class ShowPass extends Pass {
    var shows = List.empty[Show.Result]
    def key(node: Node): String = node match {
      case ty: Type     => key(ty)
      case instr: Instr => key(instr)
      case defn: Defn   => key(defn)
      case rel: Rel     => key(rel)
    }
    def key(ty: Type): String = {
      val mnemonic = ty.tag match {
        case Tag.Type.Null    => "null"
        case Tag.Type.Nothing => "nothing"
        case Tag.Type.Unit    => "unit"
        case Tag.Type.Bool    => "bool"
        case Tag.Type.I8      => "i8"
        case Tag.Type.I16     => "i16"
        case Tag.Type.I32     => "i32"
        case Tag.Type.I64     => "i64"
        case Tag.Type.F32     => "f32"
        case Tag.Type.F64     => "f64"
        case Tag.Type.Ref     => "ref"
        case Tag.Type.Slice   => "slice"
        case Tag.Type.Of      => "of"
      }
      val id = System.identityHashCode(ty)
      key(mnemonic + "#" + id)
    }
    def key(instr: Instr): String =
      key(instr.op.mnemonic + "#" + id(instr))
    def key(rel: Rel): String = {
      val mnemonic = rel.tag match {
        case Tag.Rel.Parent    => "parent"
        case Tag.Rel.Interface => "interface"
        case Tag.Rel.Overrides => "overrides"
        case Tag.Rel.Belongs   => "belongs"
      }
      key(mnemonic + "#" + id(rel))
    }
    def key(defn: Defn): String = {
      val mnemonic = defn.tag match {
        case Tag.Defn.Class     => "class"
        case Tag.Defn.Interface => "interface"
        case Tag.Defn.Module    => "module"
        case Tag.Defn.Declare   => "declare"
        case Tag.Defn.Define    => "define"
        case Tag.Defn.Field     => "field"
        case Tag.Defn.Extern    => "extern"
      }
      key(mnemonic + "#" + id(defn))
    }
    def key(s: String): String = "\"" + s + "\""
    def cross(node: Node, nodes: Seq[Node]) = {
      val k = key(node)
      nodes.foreach { n => shows = s(key(n), " -> ", k) :: shows }
    }
    def onNode(node: Node) =
      cross(node, node.next)
  }

  implicit val showScope: Show[Scope] = Show { scope =>
    val pass = new ShowPass
    scope.entries.map { case (_, Defn(_, nodes, _)) =>
      nodes.foreach(Pass.run(_, pass))
    }
    s("digraph G {",
        r(pass.shows.map(i)),
      n("}"))
  }
}
