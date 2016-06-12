package scala.scalanative
package compiler
package analysis

import scala.collection.mutable
import util.{sh, unreachable}
import nir._, Shows._

object ClassHierarchy {
  sealed abstract class Node {
    var id: Int = -1
    def attrs: Attrs
    def name: Global
  }

  sealed abstract class Scope extends Node {
    var members: Seq[Node]
  }

  final class Struct(val attrs: Attrs,
                     val name: Global,
                     val tys: Seq[nir.Type],
                     var members: Seq[Node] = Seq())
      extends Scope

  final class Trait(val attrs: Attrs,
                    val name: Global,
                    var traits: Seq[Trait] = Seq(),
                    var members: Seq[Node] = Seq())
      extends Scope {
    def methods: Seq[Method] = ???
  }

  final class Class(val attrs: Attrs,
                    val name: Global,
                    val isModule: Boolean,
                    var range: Range = null,
                    var parent: Option[Class] = scala.None,
                    var subclasses: Seq[Class] = Seq(),
                    var traits: Seq[Trait] = Seq(),
                    var members: Seq[Node] = Seq())
      extends Scope {
    def ty =
      Type.Class(name)

    def alltraits: Seq[Trait] =
      (parent.fold(Seq.empty[Trait])(_.alltraits) ++ traits).distinct

    def allfields: Seq[Field] =
      parent.fold(Seq.empty[Field])(_.allfields) ++ fields

    def fields =
      members.collect { case fld: Field => fld }

    def allmethods: Seq[Method] =
      parent.fold(Seq.empty[Method])(_.allmethods) ++ methods

    def methods: Seq[Method] =
      members.collect { case meth: Method => meth }

    def allvslots: Seq[Method] =
      parent.fold(Seq.empty[Method])(_.allvslots) ++ vslots

    def vslots: Seq[Method] =
      members.collect {
        case meth: Method if meth.isVirtual =>
          meth
      }

    def vtable: Seq[Val] = {
      val base = parent.fold(Seq.empty[Val])(_.vtable)

      def performOverrides(base: Seq[Val], members: Seq[Node]): Seq[Val] =
        members match {
          case Seq() =>
            base
          case (meth: Method) +: rest =>
            val nbase = meth.classOverride.fold(base) { basemeth =>
              base.updated(basemeth.vindex, meth.value)
            }
            performOverrides(nbase, rest)
          case _ +: rest =>
            performOverrides(base, rest)
        }

      val baseWithOverrides = performOverrides(base, members)

      baseWithOverrides ++ vslots.map(_.value)
    }

    def vtableStruct: Type.Struct =
      Type.Struct(Global.None, vtable.map(_.ty))

    def vtableValue: Val.Struct =
      Val.Struct(Global.None, vtable)

    def typeStruct: Type.Struct =
      Type.Struct(Global.None, Seq(Type.I32, Type.Ptr, vtableStruct))

    def typeValue: Val.Struct =
      Val.Struct(Global.None,
                 Seq(Val.I32(id),
                     Val.String(name.id),
                     vtableValue))

    def typeConst: Val =
      Val.Global(name tag "class" tag "type", Type.Ptr)
  }

  final class Method(val attrs: Attrs,
                     val name: Global,
                     val ty: nir.Type,
                     val isConcrete: Boolean,
                     var in: Option[Scope] = None,
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
        case meth if meth.in.fold(false)(_.isInstanceOf[Class]) =>
          meth
      }) match {
        case Seq()  => scala.None
        case Seq(m) => Some(m)
        case _      => unreachable
      }

    def traitOverrides: Seq[Method] =
      overrides.collect {
        case meth if meth.in.fold(false)(_.isInstanceOf[Trait]) =>
          meth
      }

    def vslot: Method = {
      assert(isVirtual)

      classOverride.fold(this)(_.vslot)
    }

    def vindex: Int = {
      assert(isVirtual)
      assert(in.fold(false)(_.isInstanceOf[Class]))

      val cls = in.get.asInstanceOf[Class]
      val res = cls.allvslots.indexOf(this)
      assert(res >= 0,
             s"failed to find vslot for ${this.name} in ${in.map(_.name)} (" +
             s"all vslots: ${cls.allvslots.map(_.name)}, " +
             s"all methods: ${cls.allmethods.map(_.name)})")
      res
    }
  }

  final class Field(val attrs: Attrs,
                    val name: Global,
                    val ty: nir.Type,
                    var in: Class = null)
      extends Node {
    def index = in.allfields.indexOf(this)
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
        case defn: Class  => node.id = classes.length; classes += defn
        case defn: Trait  => node.id = traits.length; traits += defn
        case defn: Method => node.id = methods.length; methods += defn
        case defn: Field  => node.id = fields.length; fields += defn
        case defn: Struct => node.id = structs.length; structs += defn
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

      if (!node.name.isTop) {
        val owner = nodes(name.top).asInstanceOf[Scope]
        node.in = Some(owner)
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
    }

    def enrichTrait(name: Global, traits: Seq[Global]): Unit = {
      val node = nodes(name).asInstanceOf[Trait]
      node.traits = traits.map(n => nodes(n).asInstanceOf[Trait])
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

    def assignClassRanges(): Unit = {
      var id = 0

      def loop(node: Class): Unit = {
        val start = id
        id += 1
        node.subclasses.foreach(loop)
        val end = id - 1
        node.id = start
        node.range = start to end
      }

      loop(nodes(Rt.Object.name).asInstanceOf[Class])
    }

    defns.foreach(enterDefn)
    defns.foreach(enrich)
    assignClassRanges()

    new Graph(nodes = nodes.toMap,
              structs = structs.toSeq,
              classes = classes.toSeq,
              traits = traits.toSeq,
              methods = methods.toSeq,
              fields = fields.toSeq)
  }
}
