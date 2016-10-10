package scala.scalanative
package compiler
package analysis

import scala.collection.mutable
import util.{sh, unreachable}
import nir._, Shows._

object ClassHierarchy {
  sealed abstract class Node {
    var id: Int   = -1
    var in: Scope = _

    def inTop: Boolean   = in.isInstanceOf[Top]
    def inClass: Boolean = in.isInstanceOf[Class]
    def inTrait: Boolean = in.isInstanceOf[Trait]
    def attrs: Attrs
    def name: Global
  }

  sealed abstract class Scope extends Node {
    var members: Seq[Node] = Seq()

    def methods: Seq[Method] =
      members.collect { case meth: Method => meth }

    def fields: Seq[Field] =
      members.collect { case fld: Field => fld }
  }

  final class Struct(val attrs: Attrs,
                     val name: Global,
                     val tys: Seq[nir.Type])
      extends Scope

  final class Trait(val attrs: Attrs,
                    val name: Global,
                    val traitNames: Seq[Global])
      extends Scope {
    var traits: Seq[Trait] = Seq()

    lazy val alltraits: Seq[Trait] =
      traits.flatMap(_.alltraits).distinct :+ this

    lazy val allmethods: Seq[Method] = {
      val parent =
        alltraits.init.map(_.allmethods).foldLeft(Seq.empty[Method])(_ ++ _)
      parent ++ methods
    }
  }

  final class Class(val attrs: Attrs,
                    val name: Global,
                    val parentName: Option[Global],
                    val traitNames: Seq[Global],
                    val isModule: Boolean)
      extends Scope {
    var range: Range           = _
    var parent: Option[Class]  = None
    var subclasses: Seq[Class] = Seq()
    var traits: Seq[Trait]     = Seq()

    lazy val ty = Type.Class(name)

    lazy val alltraits: Seq[Trait] = {
      val base = parent.fold(Seq.empty[Trait])(_.alltraits)
      (base ++ traits.flatMap(_.alltraits)).distinct
    }

    lazy val allfields: Seq[Field] =
      parent.fold(Seq.empty[Field])(_.allfields) ++ fields

    lazy val allmethods: Seq[Method] =
      parent.fold(Seq.empty[Method])(_.allmethods) ++ methods

    lazy val allvslots: Seq[Method] =
      parent.fold(Seq.empty[Method])(_.allvslots) ++ vslots

    lazy val vslots: Seq[Method] = members.collect {
      case meth: Method if meth.isVirtual =>
        meth
    }

    lazy val vtableStruct: Type.Struct =
      Type.Struct(Global.None, vtable.map(_.ty))

    lazy val vtableValue: Val.Struct = Val.Struct(Global.None, vtable)

    lazy val typeStruct: Type.Struct =
      Type.Struct(Global.None, Seq(Type.I32, Type.Ptr, vtableStruct))

    lazy val typeValue: Val.Struct = Val
      .Struct(Global.None, Seq(Val.I32(id), Val.String(name.id), vtableValue))

    lazy val typeConst: Val = Val.Global(name tag "class" tag "type", Type.Ptr)

    lazy val vtable: Seq[Val] = {
      val base = parent.fold(Seq.empty[Val])(_.vtable)

      val overrides = methods.flatMap { meth =>
        meth.classOverrides.map((meth, _))
      }
      val baseWithOverrides = overrides.foldLeft(base) {
        case (base, (meth, ovmeth)) =>
          base.updated(ovmeth.vindex, meth.value)
      }

      baseWithOverrides ++ vslots.map(_.value)
    }

    lazy val imap: Map[Method, Val] = {
      val traitOverrides = allmethods.flatMap { meth =>
        meth.traitOverrides.map((meth, _))
      }
      val traitMethods = alltraits.flatMap(_.allmethods).distinct

      traitMethods.map { tmethod =>
        var impl: Val = Val.Null
        traitOverrides.foreach {
          case (meth, ovmeth) =>
            if (ovmeth == tmethod) {
              impl = meth.value
            }
        }
        tmethod -> impl
      }.toMap
    }

    lazy val alloverrides: Seq[(Method, Method)] = {
      val base = parent.fold(Seq.empty[(Method, Method)])(_.alloverrides)
      base ++ methods.flatMap {
        case meth if meth.overrides.nonEmpty =>
          meth.overrides.map(ov => (meth, ov))
        case _ =>
          Seq()
      }
    }
  }

  final class Method(val attrs: Attrs,
                     val name: Global,
                     val ty: nir.Type,
                     val isConcrete: Boolean)
      extends Node {
    var overrides: Seq[Method] = Seq()
    var overriden: Seq[Method] = Seq()

    def isVirtual = !isConcrete || overriden.nonEmpty

    def isStatic = !isVirtual

    lazy val value =
      if (isConcrete) Val.Global(name, Type.Ptr)
      else Val.Zero(Type.Ptr)

    lazy val classOverrides: Seq[Method] = overrides.collect {
      case meth if meth.inClass =>
        meth
    }

    lazy val traitOverrides: Seq[Method] = overrides.collect {
      case meth if meth.inTrait =>
        meth
    }

    lazy val vslot: Method = {
      assert(isVirtual)

      classOverrides.headOption.fold(this)(_.vslot)
    }

    lazy val vindex: Int = {
      assert(isVirtual)
      assert(inClass)

      val cls = in.asInstanceOf[Class]
      val res = cls.allvslots.indexOf(this)
      assert(res >= 0,
             s"failed to find vslot for ${this.name} in ${in.name} (" +
               s"all vslots: ${cls.allvslots.map(_.name)}, " +
               s"all methods: ${cls.allmethods.map(_.name)})")
      res
    }
  }

  final class Field(val attrs: Attrs, val name: Global, val ty: nir.Type)
      extends Node {
    def index = {
      assert(inClass)
      in.asInstanceOf[Class].allfields.indexOf(this)
    }
  }

  final class Top(val nodes: mutable.Map[Global, Node],
                  val structs: Seq[Struct],
                  val classes: Seq[Class],
                  val traits: Seq[Trait],
                  override val methods: Seq[Method],
                  override val fields: Seq[Field])
      extends Scope {
    def name  = Global.None
    def attrs = Attrs.None

    lazy val dispatchName = Global.Top("__dispatch")
    lazy val dispatchVal  = Val.Global(dispatchName, Type.Ptr)
    lazy val (dispatchTy, dispatchDefn) = {
      val traitMethods = methods.filter(_.inTrait).sortBy(_.id)

      val columns = classes.sortBy(_.id).map { cls =>
        val row = Array.fill[Val](traitMethods.length)(Val.Null)
        cls.imap.foreach {
          case (meth, value) =>
            row(meth.id) = value
        }
        Val.Array(Type.Ptr, row)
      }
      val table = Val.Array(Type.Array(Type.Ptr, traitMethods.length), columns)

      (table.ty, Defn.Const(Attrs.None, dispatchName, table.ty, table))
    }

    lazy val instanceName = Global.Top("__instance")
    lazy val instanceVal  = Val.Global(instanceName, Type.Ptr)
    lazy val (instanceTy, instanceDefn) = {
      val columns = classes.sortBy(_.id).map { cls =>
        val row = new Array[Boolean](traits.length)
        cls.alltraits.foreach { trt =>
          row(trt.id) = true
        }
        Val.Array(Type.Bool, row.map(Val.Bool))
      }
      val table = Val.Array(Type.Array(Type.Bool, traits.length), columns)

      (table.ty, Defn.Const(Attrs.None, instanceName, table.ty, table))
    }
  }

  def apply(defns: Seq[Defn]): Top = {
    val nodes   = mutable.Map.empty[Global, Node]
    val structs = mutable.UnrolledBuffer.empty[Struct]
    val classes = mutable.UnrolledBuffer.empty[Class]
    val traits  = mutable.UnrolledBuffer.empty[Trait]
    val methods = mutable.UnrolledBuffer.empty[Method]
    val fields  = mutable.UnrolledBuffer.empty[Field]

    def enter[T <: Node](name: Global, node: T): T = {
      nodes += name -> node
      node match {
        case defn: Class  => classes += defn // id given in assignClassIds
        case defn: Trait  => node.id = traits.length; traits += defn
        case defn: Method => methods += defn // id given in assignMethodIds
        case defn: Field  => node.id = fields.length; fields += defn
        case defn: Struct => node.id = structs.length; structs += defn
      }
      node
    }

    def enterDefn(defn: Defn): Unit = defn match {
      case defn: Defn.Trait =>
        enter(defn.name, new Trait(defn.attrs, defn.name, defn.traits))

      case defn: Defn.Class =>
        enter(defn.name,
              new Class(defn.attrs,
                        defn.name,
                        defn.parent,
                        defn.traits,
                        isModule = false))

      case defn: Defn.Module =>
        val name = defn.name tag "module"
        val cls = new Class(defn.attrs,
                            name,
                            defn.parent,
                            defn.traits,
                            isModule = true)
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

    defns.foreach(enterDefn)
    val top = new Top(nodes = nodes,
                      structs = structs,
                      classes = classes,
                      traits = traits,
                      methods = methods,
                      fields = fields)
    top.members = nodes.values.toSeq

    def enrichMethods(): Unit = methods.foreach { node =>
      if (node.name.isTop) {
        node.in = top
      } else {
        val owner = nodes(node.name.top).asInstanceOf[Scope]
        node.in = owner
        owner.members = owner.members :+ node
        node.attrs.overrides.foreach { n =>
          val ovnode = nodes(n).asInstanceOf[Method]
          node.overrides = node.overrides :+ ovnode
          ovnode.overriden = ovnode.overriden :+ node
        }
      }
    }

    def enrichFields(): Unit = fields.foreach { node =>
      val parent = nodes(node.name.top).asInstanceOf[Class]
      node.in = parent
      parent.members = parent.members :+ node
    }

    def enrichClasses(): Unit = classes.foreach { node =>
      val parent = node.parentName.map(nodes(_).asInstanceOf[Class])
      val traits = node.traitNames.map(nodes(_).asInstanceOf[Trait])
      node.in = top
      node.parent = parent
      node.traits = traits
      parent.foreach { parent =>
        parent.subclasses = parent.subclasses :+ node
      }
    }

    def enrichTraits(): Unit = traits.foreach { node =>
      node.in = top
      node.traits = node.traitNames.map(n => nodes(n).asInstanceOf[Trait])
    }

    def assignClassIds(): Unit = {
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

    def assignMethodIds(): Unit = {
      val traitMethods = methods.filter(_.inTrait)
      val restMethods  = methods.filterNot(_.inTrait)

      var id = 0
      traitMethods.foreach { meth =>
        meth.id = id
        id += 1
      }
      restMethods.foreach { meth =>
        meth.id = id
        id += 1
      }
    }

    enrichMethods()
    enrichFields()
    enrichClasses()
    enrichTraits()
    assignClassIds()
    assignMethodIds()

    top
  }
}
