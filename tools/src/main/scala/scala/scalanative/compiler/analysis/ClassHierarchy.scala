package scala.scalanative
package compiler
package analysis

import scala.collection.mutable
import util.{sh, unreachable}
import nir._, Shows._

object ClassHierarchy {
  sealed abstract class Node {
    def attrs: Seq[Attr]
    def name: Global
  }

  final class Trait(
    val attrs:        Seq[Attr],
    val name:         Global,
    var id:           Int        = -1,
    var traits:       Seq[Node]  = Seq(),
    var implementors: Seq[Class] = Seq(),
    var members:      Seq[Node]  = Seq()
  ) extends Node {
    def methods: Seq[Method] = ???
  }

  final class Class(
    val attrs:      Seq[Attr],
    val name:       Global,
    var id:         Int           = -1,
    var range:      Range         = null,
    var parent:     Option[Class] = None,
    var subclasses: Seq[Class]    = Seq(),
    var traits:     Seq[Node]     = Seq(),
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
    val attrs:      Seq[Attr],
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

    def traitOverrides: Seq[Method] =
      overrides.collect {
        case meth if meth.in.isInstanceOf[Trait] =>
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
    val attrs: Seq[Attr],
    val name:  Global,
    val ty:    Type
  ) extends Node {
    def index = in.fields.indexOf(this)
  }

  final class Graph(
    val nodes:   Map[Global, Node],
    val classes: Seq[Class],
    val traits:  Seq[Trait],
    val methods: Seq[Method],
    val fields:  Seq[Field]
  )

  def apply(defns: Seq[Defn]): Graph = {
    val nodes   = mutable.Map.empty[Global, Node]
    val classes = mutable.UnrolledBuffer.empty[Class]
    val traits  = mutable.UnrolledBuffer.empty[Trait]
    val methods = mutable.UnrolledBuffer.empty[Method]
    val fields  = mutable.UnrolledBuffer.empty[Field]

    def enter[T <: Node](name: Global, node: T): T = {
      nodes += name -> node
      node match {
        case cls:   Class     => classes    += cls
        case iface: Trait => traits += iface
        case meth:  Method    => methods    += meth
        case fld:   Field     => fields     += fld
      }
      node
    }

    def enterNrt(): Unit = {
      val classes = Map(
        Nrt.Object -> Seq(
          Nrt.Object_init,
          Nrt.Object_equals,
          Nrt.Object_hashCode,
          Nrt.Object_toString
        )
      )

      classes.foreach { case (Type.Class(clsname), clsmethods) =>
        val clsnode = enter(clsname, new Class(Seq(), clsname))
        clsnode.members = clsmethods.map { case Val.Global(name, Type.Ptr(ty)) =>
          enter(name, new Method(clsnode, Seq(), name, ty, isConcrete = true))
        }
      }
    }

    def enterMember(in: Node, defn: Defn): Unit = defn match {
      case defn: Defn.Var =>
        enter(defn.name, new Field(in.asInstanceOf[Class], defn.attrs, defn.name, defn.ty))

      case defn: Defn.Declare =>
        enter(defn.name, new Method(in, defn.attrs, defn.name, defn.ty, isConcrete = false))

      case defn: Defn.Define =>
        enter(defn.name, new Method(in, defn.attrs, defn.name, defn.ty, isConcrete = true))

      case _ =>
        unreachable
    }

    def enterDefn(defn: Defn): Unit = defn match {
      case defn: Defn.Trait =>
        val node = enter(defn.name, new Trait(defn.attrs, defn.name))
        defn.members.foreach(enterMember(node, _))

      case defn: Defn.Class =>
        val node = enter(defn.name, new Class(defn.attrs, defn.name))
        defn.members.foreach(enterMember(node, _))

      case defn: Defn.Module =>
        val node = enter(defn.name, new Class(defn.attrs, defn.name))
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
                    traitNames: Seq[Global], members: Seq[Defn]): Unit = {
      val node          = nodes(name).asInstanceOf[Class]
      val parent        = nodes(parentName).asInstanceOf[Class]
      val traits        = traitNames.map(nodes(_).asInstanceOf[Trait])
      node.parent       = Some(parent)
      node.traits       = traits
      node.members      = members.map(m => nodes(m.name))
      parent.subclasses = parent.subclasses :+ node
      traits.foreach { iface =>
        iface.implementors = iface.implementors :+ node
      }
      members.foreach(enrich)
    }

    def enrichTrait(name: Global, traits: Seq[Global], members: Seq[Defn]): Unit = {
      val node     = nodes(name).asInstanceOf[Trait]
      node.traits  = traits.map(nodes(_))
      node.members = members.map(m => nodes(m.name))
      members.foreach(enrich)
    }

    def enrich(defn: Defn): Unit = defn match {
      case defn: Defn.Declare                         => enrichMethod(defn.name, defn.attrs)
      case defn: Defn.Define                          => enrichMethod(defn.name, defn.attrs)
      case Defn.Trait(_, n, ifaces, members)          => enrichTrait(n, ifaces, members)
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

      def idTrait(node: Trait): Unit = {
        node.id = id
        id += 1
      }

      def idMethod(node: Method): Unit = {
        node.id = id
        id += 1
      }

      idClass(nodes(Nrt.Object.name).asInstanceOf[Class])
      traits.foreach(idTrait(_))
      methods.foreach(idMethod(_))
    }

    enterNrt()
    defns.foreach(enterDefn)
    defns.foreach(enrich)
    identify()

    new Graph(
      nodes   = nodes.toMap,
      classes = classes.toSeq,
      traits  = traits.toSeq,
      methods = methods.toSeq,
      fields  = fields.toSeq
    )
  }
}
