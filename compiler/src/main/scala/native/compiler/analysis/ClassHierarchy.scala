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

  final class Interface(
    val name:       Global,
    var id:         Int       = -1,
    var interfaces: Seq[Node] = Seq(),
    var methods:    Seq[Node] = Seq()
  ) extends Node

  final class Class(
    val name:       Global,
    var id:         Int           = -1,
    var range:      Range         = null,
    var parent:     Option[Class] = None,
    var subclasses: Seq[Class]    = Seq(),
    var interfaces: Seq[Node]     = Seq(),
    var members:    Seq[Node]     = Seq()
  ) extends Node {
    def ty =
      Type.Class(name)

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
    var id:         Int = -1,
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

  type Result = Map[Global, Node]

  def apply(defns: Seq[Defn]): Result = {
    val nodes      = mutable.Map.empty[Global, Node]
    val interfaces = mutable.UnrolledBuffer.empty[Interface]
    val methods    = mutable.UnrolledBuffer.empty[Method]

    def enterIntrs(): Unit = {
      val classes = Map(
        Intr.object_ -> Seq(
          Intr.object_init,
          Intr.object_equals,
          Intr.object_hashCode,
          Intr.object_toString
        )
      )

      classes.foreach { case (Type.Class(clsname), clsmethods) =>
        val clsnode = new Class(clsname)
        clsnode.members = clsmethods.map { case Val.Global(name, Type.Ptr(ty)) =>
          val node = new Method(clsnode, name, ty, isConcrete = true)
          nodes   += name -> node
          methods += node
          node
        }
        nodes += clsname -> clsnode
      }
    }

    def enterMember(in: Node, defn: Defn): Unit = defn match {
      case defn: Defn.Var =>
        val node = new Field(in.asInstanceOf[Class], defn.name, defn.ty)
        nodes   += (defn.name -> node)

      case defn: Defn.Declare =>
        val node = new Method(in, defn.name, defn.ty, isConcrete = false)
        nodes   += (defn.name -> node)
        methods += node

      case defn: Defn.Define =>
        val node = new Method(in, defn.name, defn.ty, isConcrete = true)
        nodes   += (defn.name -> node)
        methods += node

      case _ =>
        unreachable
    }

    def enter(defn: Defn): Unit = defn match {
      case defn: Defn.Interface =>
        val node    = new Interface(defn.name)
        nodes      += (defn.name -> node)
        interfaces += node
        defn.members.foreach(enterMember(node, _))

      case defn: Defn.Class =>
        val node = new Class(defn.name)
        nodes   += (defn.name -> node)
        defn.members.foreach(enterMember(node, _))

      case defn: Defn.Module =>
        val node = new Class(defn.name)
        nodes   += (defn.name -> node)
        defn.members.foreach(enterMember(node, _))

      case _ =>
        ()
    }

    def enrichMethod(name: Global, attrs: Seq[Attr]): Unit = {
      val node = nodes(name).asInstanceOf[Method]
      attrs.foreach {
        case Attr.Override(n) =>
          val ovnode       = nodes(n).asInstanceOf[Method]
          node.overrides   = node.overrides :+ ovnode
          ovnode.overriden = ovnode.overriden :+ node

        case _ =>
          ()
      }
    }

    def enrichClass(name: Global, parentName: Global,
                    interfaces: Seq[Global], members: Seq[Defn]): Unit = {
      val node          = nodes(name).asInstanceOf[Class]
      val parent        = nodes(parentName).asInstanceOf[Class]
      parent.subclasses = parent.subclasses :+ node
      node.parent       = Some(parent)
      node.members      = members.map(m => nodes(m.name))
      members.foreach(enrich)
    }

    def enrichInterface(name: Global, interfaces: Seq[Global], members: Seq[Defn]): Unit = {
      val node        = nodes(name).asInstanceOf[Interface]
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

    def identify(): Unit = {
      var id = 1

      def idClass(node: Class): Unit = {
        val start = id
        id += 1
        node.subclasses.foreach(idClass)
        val end = id - 1
        node.id    = start
        node.range = start to end
      }

      def idInterface(node: Interface): Unit = {
        node.id = id
        id += 1
      }

      def idMethod(node: Method): Unit = {
        node.id = id
        id += 1
      }

      idClass(nodes(Intr.object_.name).asInstanceOf[Class])
      interfaces.foreach(idInterface(_))
      methods.foreach(idMethod(_))
    }

    enterIntrs()
    defns.foreach(enter)
    defns.foreach(enrich)
    identify()

    nodes.toMap
  }
}
