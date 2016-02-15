package native
package compiler
package analysis

import scala.collection.mutable
import native.nir._, Shows._
import native.util.{sh, unreachable}

object ClassHierarchy {
  sealed abstract class Node {
    def name: Global
  }
  object Node {
    final class Interface(
      val name:       Global,
      var interfaces: Seq[Node] = Seq(),
      var methods:    Seq[Node] = Seq()
    ) extends Node

    final class Class(
      val name:       Global,
      var parent:     Option[Node.Class] = None,
      var interfaces: Seq[Node]          = Seq(),
      var members:    Seq[Node]          = Seq()
    ) extends Node {
      def fields: Seq[Field] =
        parent.map(_.fields).getOrElse(Seq()) ++ ownfields

      def ownfields =
        members.collect { case fld: Field => fld }

      def methods: Seq[Method] =
        parent.map(_.methods).getOrElse(Seq()) ++ ownmethods

      def ownmethods: Seq[Method] =
        members.collect { case meth: Method => meth }

      def vslots: Seq[Method] =
        parent.map(_.vslots).getOrElse(Seq()) ++ ownvslots

      def ownvslots: Seq[Method] =
        members.collect {
          case meth: Method if meth.isVirtual && meth.overrides.isEmpty =>
            meth
        }

      def vtable: Seq[Val] = {
        val base = parent.map(_.vtable).getOrElse(Seq())

        def performOverrides(base: Seq[Val], members: Seq[Node]): Seq[Val] =
          members match {
            case Seq() =>
              base
            case (meth: Method) +: rest =>
              val nbase =
                meth.classOverride.map { basemeth =>
                  base.updated(basemeth.vindex, meth.value)
                }.getOrElse(base)
              performOverrides(nbase, rest)
            case _ +: rest =>
              performOverrides(base, rest)
          }

        val baseWithOverrides = performOverrides(base, members)

        baseWithOverrides ++ ownvslots.map(_.value)
      }
    }

    final class Method(
      val in:         Node,
      val name:       Global,
      val ty:         Type,
      val isConcrete: Boolean,
      var overrides:  Seq[Method] = Seq(),
      var overriden:  Seq[Method] = Seq()
    ) extends Node {
      def isVirtual = !isConcrete || overriden.nonEmpty

      def isStatic = !isVirtual

      def value =
        if (isConcrete)
          Val.Global(name, Type.Ptr(ty))
        else
          Val.Zero(Type.Ptr(ty))

      def classOverride: Option[Method] =
        (overrides.collect {
          case meth if meth.in.isInstanceOf[Class] =>
            meth
        }) match {
          case Seq()  => None
          case Seq(m) => Some(m)
          case _      => unreachable
        }

      def interfaceOverrides: Seq[Method] =
        overrides.collect {
          case meth if meth.in.isInstanceOf[Interface] =>
            meth
        }

      def vslot: Method = {
        assert(isVirtual)

        classOverride.map(_.vslot).getOrElse(this)
      }

      def vindex: Int = {
        assert(isVirtual)
        assert(in.isInstanceOf[Class])

        in.asInstanceOf[Class].vslots.indexOf(this)
      }
    }

    final class Field(
      val in:    Class,
      val name:  Global,
      val ty:    Type
    ) extends Node {
      def index = in.fields.indexOf(this)
    }
  }

  type Result = Map[Global, Node]

  def apply(defns: Seq[Defn]): Result = {
    val nodes = mutable.Map.empty[Global, Node]

    def enterIntrs(): Unit = {
      val classes = Map(
        Intr.object_ -> Seq(
          Intr.object_init,
          Intr.object_equals,
          Intr.object_hashCode,
          Intr.object_toString
        )
      )

      classes.foreach { case (Type.Class(clsname), methods) =>
        val clsnode = new Node.Class(clsname)
        clsnode.members = methods.map { case Val.Global(name, Type.Ptr(ty)) =>
          val node = new Node.Method(clsnode, name, ty, isConcrete = true)
          nodes += name -> node
          node
        }
        nodes += clsname -> clsnode
      }
    }

    def enterMember(in: Node, defn: Defn): Unit = defn match {
      case defn: Defn.Var =>
        val node = new Node.Field(in.asInstanceOf[Node.Class], defn.name, defn.ty)
        nodes += (defn.name -> node)

      case defn: Defn.Declare =>
        val node = new Node.Method(in, defn.name, defn.ty, isConcrete = false)
        nodes += (defn.name -> node)

      case defn: Defn.Define =>
        val node = new Node.Method(in, defn.name, defn.ty, isConcrete = true)
        nodes += (defn.name -> node)

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
        case Attr.Overrides(n) =>
          val ovnode       = nodes(n).asInstanceOf[Node.Method]
          node.overrides   = node.overrides :+ ovnode
          ovnode.overriden = ovnode.overriden :+ node

        case _ =>
          ()
      }
    }

    def enrichClass(name: Global, parent: Global,
                    interfaces: Seq[Global], members: Seq[Defn]): Unit = {
      val node        = nodes(name).asInstanceOf[Node.Class]
      node.parent     = Some(nodes(parent).asInstanceOf[Node.Class])
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

    enterIntrs()
    defns.foreach(enter)
    defns.foreach(enrich)
    nodes.toMap
  }
}
