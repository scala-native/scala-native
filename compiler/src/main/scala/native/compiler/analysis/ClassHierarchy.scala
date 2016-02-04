package native
package compiler
package analysis

import scala.collection.mutable
import native.nir._, Shows._
import native.util.{sh, unreachable}

object ClassHierarchy {
  sealed abstract class Node
  object Node {
    final class Interface(
      val name:         Global,
      var interfaces:   Seq[Node] = Seq(),
      var methods:      Seq[Node] = Seq()
    ) extends Node

    final class Class(
      val name:       Global,
      var parent:     Option[Node.Class] = None,
      var interfaces: Seq[Node]          = Seq(),
      var members:    Seq[Node]          = Seq()
    ) extends Node {
      def vtable: Seq[(Type, Val)] = {
        val parentvtable = parent.map(_.vtable).getOrElse {
          Seq(Intrinsic.object_to_string,
              Intrinsic.object_hash_code,
              Intrinsic.object_equals).map {
            case v @ Val.Intrinsic(_, ty) =>
              (ty, v)
          }
        }
        val ownvtable = members.collect {
          case meth: Node.Method if meth.isVirtual =>
            val ty = Type.Ptr(meth.ty)
            (ty, Val.Global(meth.name, ty))
        }
        parentvtable ++ ownvtable
      }

      def data: Seq[Node.Field] = {
        val parentfields = parent.map(_.data).getOrElse(Seq())
        val ownfields = members.collect {
          case fld: Node.Field => fld
        }
        parentfields ++ ownfields
      }
    }

    final class Method(
      val in:         Node,
      val name:       Global,
      val ty:         Type,
      val concrete:   Boolean,
      var overrides:  Option[Node] = None,
      var overriden:  Seq[Node]    = Seq(),
      var implements: Seq[Node]    = Seq()
    ) extends Node {
      def isVirtual = !concrete || overrides.nonEmpty || overriden.nonEmpty
    }

    final class Field(
      val in:    Class,
      val name:  Global,
      val ty:    Type
    ) extends Node {
      def index = in.data.indexOf(this) + 1
    }
  }

  type Result = Map[Global, Node]

  def apply(defns: Seq[Defn]): Result = {
    val nodes = mutable.Map.empty[Global, Node]

    def enterMember(in: Node, defn: Defn): Unit = defn match {
      case defn: Defn.Var =>
        nodes += (defn.name -> new Node.Field(in.asInstanceOf[Node.Class], defn.name, defn.ty))

      case defn: Defn.Declare =>
        nodes += (defn.name -> new Node.Method(in, defn.name, defn.ty, concrete = false))

      case defn: Defn.Define =>
        nodes += (defn.name -> new Node.Method(in, defn.name, defn.ty, concrete = true))

      case _ =>
        unreachable
    }

    def enter(defn: Defn): Unit = defn match {
      case defn: Defn.Interface =>
        val node = new Node.Interface(defn.name)
        nodes   += (defn.name -> node)
        defn.members.foreach(enterMember(node, _))

      case defn: Defn.Class =>
        val node = new Node.Class(defn.name)
        nodes   += (defn.name -> node)
        defn.members.foreach(enterMember(node, _))

      case defn: Defn.Module =>
        val node = new Node.Class(defn.name)
        nodes   += (defn.name -> node)
        defn.members.foreach(enterMember(node, _))

      case _ =>
        ()
    }

    def enrichMethod(name: Global, attrs: Seq[Attr]): Unit = {
      val node = nodes(name).asInstanceOf[Node.Method]
      attrs.foreach {
        case Attr.Overrides(n)  =>
          val ovnode       = nodes(n).asInstanceOf[Node.Method]
          node.overrides   = Some(ovnode)
          ovnode.overriden = ovnode.overriden :+ node

        case Attr.Implements(n) =>
          node.implements = node.implements :+ nodes(n)

        case _ =>
          ()
      }
    }

    def enrichClass(name: Global, parent: Option[Global],
                    interfaces: Seq[Global], members: Seq[Defn]): Unit = {
      val node        = nodes(name).asInstanceOf[Node.Class]
      node.parent     = parent.map(nodes(_).asInstanceOf[Node.Class])
      node.interfaces = interfaces.map(nodes(_))
      node.members    = members.map(m => nodes(m.name))
      members.foreach(enrich)
    }

    def enrichInterface(name: Global, interfaces: Seq[Global], members: Seq[Defn]): Unit = {
      val node        = nodes(name).asInstanceOf[Node.Interface]
      node.interfaces = interfaces.map(nodes(_))
      node.methods    = members.map(m => nodes(m.name))
      members.foreach(enrich)
    }

    def enrich(defn: Defn): Unit = defn match {
      case defn: Defn.Declare                         => enrichMethod(defn.name, defn.attrs)
      case defn: Defn.Define                          => enrichMethod(defn.name, defn.attrs)
      case Defn.Interface(_, n, ifaces, members)      => enrichInterface(n, ifaces, members)
      case Defn.Class(_, n, parent, ifaces, members)  => enrichClass(n, parent, ifaces, members)
      case Defn.Module(_, n, parent, ifaces, members) => enrichClass(n, parent, ifaces, members)
      case _                                          => ()
    }

    defns.foreach(enter)
    defns.foreach(enrich)
    nodes.toMap
  }
}
