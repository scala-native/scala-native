package native
package compiler
package analysis

import native.nir._
import scala.collection.mutable

object ClassHierarchy {
  sealed abstract class Node
  object Node {
    final case class Interface(
      name:             Global,
      var interfaces:   Seq[Node] = Seq(),
      var methods:      Seq[Node] = Seq()
    ) extends Node
    final case class Class(
      name:           Global,
      var parent:     Option[Node] = None,
      var interfaces: Seq[Node]    = Seq(),
      var members:    Seq[Node]    = Seq()
    ) extends Node
    final case class Method(
      in:             Node,
      name:           Global,
      var overrides:  Option[Node] = None,
      var implements: Seq[Node]    = Seq()
    ) extends Node
    final case class Field(
      in:   Node,
      name: Global
    ) extends Node
  }

  def apply(defns: Seq[Defn]): Map[Global, Node] = {
    val nodes = mutable.Map.empty[Global, Node]

    def enterMember(in: Node, defn: Defn): Unit = defn match {
      case defn: Defn.Var =>
        nodes += (defn.name -> Node.Field(in, defn.name))

      case defn: Defn.Declare =>
        nodes += (defn.name -> Node.Method(in, defn.name))

      case defn: Defn.Define =>
        nodes += (defn.name -> Node.Method(in, defn.name))
    }

    def enter(defn: Defn): Unit = defn match {
      case defn: Defn.Interface =>
        val node = Node.Interface(defn.name)
        nodes   += (defn.name -> node)
        defn.members.foreach(enterMember(node, _))

      case defn: Defn.Class =>
        val node = Node.Class(defn.name)
        nodes   += (defn.name -> node)
        defn.members.foreach(enterMember(node, _))

      case defn: Defn.Module =>
        val node = Node.Class(defn.name)
        nodes   += (defn.name -> node)
        defn.members.foreach(enterMember(node, _))
    }

    def enrichMethod(name: Global, attrs: Seq[Attr]): Unit = {
      val node = nodes(name).asInstanceOf[Node.Method]
      attrs.foreach {
        case Attr.Overrides(n)  => node.overrides  = Some(nodes(n))
        case Attr.Implements(n) => node.implements = node.implements :+ nodes(n)
      }
    }

    def enrichClass(name: Global, parent: Option[Global],
                    interfaces: Seq[Global], members: Seq[Defn]): Unit = {
      val node        = nodes(name).asInstanceOf[Node.Class]
      node.parent     = parent.map(nodes(_))
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
      case defn: Defn.Var                             => ()
      case defn: Defn.Declare                         => enrichMethod(defn.name, defn.attrs)
      case defn: Defn.Define                          => enrichMethod(defn.name, defn.attrs)
      case Defn.Interface(_, n, ifaces, members)      => enrichInterface(n, ifaces, members)
      case Defn.Class(_, n, parent, ifaces, members)  => enrichClass(n, parent, ifaces, members)
      case Defn.Module(_, n, parent, ifaces, members) => enrichClass(n, parent, ifaces, members)
    }

    defns.foreach(enter)
    defns.foreach(enrich)
    nodes.toMap
  }
}
