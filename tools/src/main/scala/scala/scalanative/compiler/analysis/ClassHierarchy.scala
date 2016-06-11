package scala.scalanative
package compiler
package analysis

import scala.collection.mutable
import util.{sh, unreachable}
import nir._, Shows._

object ClassHierarchy {
  sealed abstract class Node {
    def attrs: Attrs
    def name: Global
    def id: Int
  }

  final case object None extends Node {
    def attrs = Attrs.None
    def name  = Global.None
    def id    = 0
  }

  sealed abstract class Top extends Node {
    var members: Seq[Node]
  }

  final class Struct(val attrs: Attrs,
                     val name: Global,
                     val tys: Seq[nir.Type],
                     var id: Int = -1,
                     var members: Seq[Node] = Seq())
      extends Top

  final class Trait(val attrs: Attrs,
                    val name: Global,
                    var id: Int = -1,
                    var traits: Seq[Node] = Seq(),
                    var implementors: Seq[Class] = Seq(),
                    var members: Seq[Node] = Seq())
      extends Top {
    def methods: Seq[Method] = ???
  }

  final class Class(val attrs: Attrs,
                    val name: Global,
                    val isModule: Boolean,
                    var id: Int = -1,
                    var range: Range = null,
                    var parent: Option[Class] = scala.None,
                    var subclasses: Seq[Class] = Seq(),
                    var traits: Seq[Node] = Seq(),
                    var members: Seq[Node] = Seq())
      extends Top {
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
        case meth: Method if meth.isVirtual =>
          meth
      }

    def vtable: Seq[Val] = {
      val base = parent.map(_.vtable).getOrElse(Seq())

      def performOverrides(base: Seq[Val], members: Seq[Node]): Seq[Val] =
        members match {
          case Seq() =>
            base
          case (meth: Method) +: rest =>
            val nbase = meth.classOverride.map { basemeth =>
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

  final class Method(val attrs: Attrs,
                     val name: Global,
                     val ty: Type,
                     val isConcrete: Boolean,
                     var in: Node = null,
                     var id: Int = -1,
                     var overrides: Seq[Method] = Seq(),
                     var overriden: Seq[Method] = Seq())
      extends Node {
    def isVirtual = !isConcrete || overriden.nonEmpty

    def isStatic = !isVirtual

    def value =
      if (isConcrete) Val.Global(name, Type.Ptr)
      else Val.Zero(Type.Ptr)

    def classOverride: Option[Method] =
      (overrides.collect {
        case meth if meth.in.isInstanceOf[Class] =>
          meth
      }) match {
        case Seq()  => scala.None
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

      val cls = in.asInstanceOf[Class]
      val res = cls.vslots.indexOf(this)
      assert(
          res >= 0,
          s"failed to find vslot for ${this.name} in ${in.name} (all vslots: ${cls.vslots
            .map(_.name)}, all methods: ${cls.methods.map(_.name)})")
      res
    }
  }

  final class Field(val attrs: Attrs,
                    val name: Global,
                    val ty: Type,
                    var in: Class = null)
      extends Node {
    // TODO: generate field ids
    def id = -1

    def index = in.fields.indexOf(this)
  }

  final class Graph(val nodes: Map[Global, Node],
                    val structs: Seq[Struct],
                    val classes: Seq[Class],
                    val traits: Seq[Trait],
                    val methods: Seq[Method],
                    val fields: Seq[Field])

  def apply(defns: Seq[Defn]): Graph = {
    val nodes   = mutable.Map.empty[Global, Node]
    val structs = mutable.UnrolledBuffer.empty[Struct]
    val classes = mutable.UnrolledBuffer.empty[Class]
    val traits  = mutable.UnrolledBuffer.empty[Trait]
    val methods = mutable.UnrolledBuffer.empty[Method]
    val fields  = mutable.UnrolledBuffer.empty[Field]

    def enter[T <: Node](name: Global, node: T): T = {
      nodes += name -> node
      node match {
        case defn: Class  => classes += defn
        case defn: Trait  => traits += defn
        case defn: Method => methods += defn
        case defn: Field  => fields += defn
        case defn: Struct => structs += defn
      }
      node
    }

    def enterDefn(defn: Defn): Unit = defn match {
      case defn: Defn.Trait =>
        enter(defn.name, new Trait(defn.attrs, defn.name))

      case defn: Defn.Class =>
        enter(defn.name, new Class(defn.attrs, defn.name, isModule = false))

      case defn: Defn.Module =>
        val name = defn.name tag "module"
        val cls  = new Class(defn.attrs, name, isModule = true)
        enter(defn.name, cls)
        enter(name, cls)

      case defn: Defn.Var =>
        enter(defn.name, new Field(defn.attrs, defn.name, defn.ty))

      case defn: Defn.Declare =>
        enter(defn.name,
              new Method(defn.attrs, defn.name, defn.ty, isConcrete = false))

      case defn: Defn.Define =>
        enter(defn.name,
              new Method(defn.attrs, defn.name, defn.ty, isConcrete = true))

      case defn: Defn.Struct =>
        enter(defn.name, new Struct(defn.attrs, defn.name, defn.tys))

      case _ =>
        ()
    }

    def enrichMethod(name: Global, attrs: Attrs): Unit = {
      val node = nodes(name).asInstanceOf[Method]

      if (node.name.isTop)
        node.in = None
      else {
        val owner = nodes(name.top).asInstanceOf[Top]
        node.in = owner
        owner.members = owner.members :+ node
        attrs.overrides.foreach { n =>
          val ovnode = nodes(n).asInstanceOf[Method]
          node.overrides = node.overrides :+ ovnode
          ovnode.overriden = ovnode.overriden :+ node
        }
      }
    }

    def enrichField(name: Global): Unit = {
      val node   = nodes(name).asInstanceOf[Field]
      val parent = nodes(name.top).asInstanceOf[Class]
      node.in = parent
      parent.members = parent.members :+ node
    }

    def enrichClass(name: Global,
                    parentName: Option[Global],
                    traitNames: Seq[Global]): Unit = {
      val node   = nodes(name).asInstanceOf[Class]
      val parent = parentName.map(nodes(_).asInstanceOf[Class])
      val traits = traitNames.map(nodes(_).asInstanceOf[Trait])
      node.parent = parent
      node.traits = traits
      parent.foreach { parent =>
        parent.subclasses = parent.subclasses :+ node
      }
      traits.foreach { iface =>
        iface.implementors = iface.implementors :+ node
      }
    }

    def enrichTrait(name: Global, traits: Seq[Global]): Unit = {
      val node = nodes(name).asInstanceOf[Trait]
      node.traits = traits.map(nodes(_))
    }

    def enrich(defn: Defn): Unit = defn match {
      case defn: Defn.Declare                => enrichMethod(defn.name, defn.attrs)
      case defn: Defn.Define                 => enrichMethod(defn.name, defn.attrs)
      case defn: Defn.Var                    => enrichField(defn.name)
      case Defn.Trait(_, n, ifaces)          => enrichTrait(n, ifaces)
      case Defn.Class(_, n, parent, ifaces)  => enrichClass(n, parent, ifaces)
      case Defn.Module(_, n, parent, ifaces) => enrichClass(n, parent, ifaces)
      case _                                 => ()
    }

    def identify(): Unit = {
      var id = 1

      def idClass(node: Class): Unit = {
        val start = id
        id += 1
        node.subclasses.foreach(idClass)
        val end = id - 1
        node.id = start
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

      idClass(nodes(Rt.Object.name).asInstanceOf[Class])
      traits.foreach(idTrait(_))
      methods.foreach(idMethod(_))
    }

    defns.foreach(enterDefn)
    defns.foreach(enrich)
    identify()

    new Graph(nodes = nodes.toMap,
              structs = structs.toSeq,
              classes = classes.toSeq,
              traits = traits.toSeq,
              methods = methods.toSeq,
              fields = fields.toSeq)
  }
}
