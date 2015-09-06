package salty.ir

import salty.ir.Combinators._
import salty.util.Show, Show.{Sequence => s, Indent => i, Unindent => u,
                              Repeat => r, Newline => n}
import salty.util.sh

abstract class Pass {
  def onRoot(node: Root): Unit
}
object Pass {
  def run(entry: Root, pass: Pass) = {
    val epoch = Root.nextEpoch
    def loop(node: Root): Unit =
      if (node.epoch < epoch) {
        node.epoch = epoch
        pass.onRoot(node)
        node.next.foreach(loop)
      }
    loop(entry)
  }
}

object ShowDOT {
  def key(defn: Defn) = ???

  class ShowPass extends Pass {
    var shows = List.empty[Show.Result]
    def key(root: Root): String = root match {
      case ty: Type => key(ty)
      case node: Node => key(node)
      case defn: Defn => key(defn)
      case meta: Meta => key(meta)
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
    def key(node: Node): String = {
      val id = System.identityHashCode(node)
      key(node.op.mnemonic + "#" + id)
    }
    def key(meta: Meta): String = {
      val mnemonic = meta.tag match {
        case Tag.Meta.Parent    => "parent"
        case Tag.Meta.Interface => "interface"
        case Tag.Meta.Overrides => "overrides"
        case Tag.Meta.Belongs   => "belongs"
      }
      val id = System.identityHashCode(meta)
      key(mnemonic + "#" + id)
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
      val id = System.identityHashCode(defn)
      key(mnemonic + "#" + id)
    }
    def key(s: String): String = "\"" + s + "\""
    def cross(node: Root, nodes: Seq[Root]) = {
      val k = key(node)
      nodes.foreach { n => shows = s(k, " -> ", key(n)) :: shows }
    }
    def onRoot(root: Root) =
      cross(root, root.next)
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
