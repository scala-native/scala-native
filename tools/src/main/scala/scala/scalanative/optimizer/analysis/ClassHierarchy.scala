package scala.scalanative
package optimizer
package analysis

import scala.collection.mutable
import util.unreachable
import nir._

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
    val members = mutable.UnrolledBuffer.empty[Node]

    def methods: Seq[Method] =
      members.collect { case meth: Method => meth }

    def fields: Seq[Field] =
      members.collect { case fld: Field => fld }

    def typeName: Global = this.name member "type"
  }

  final class Struct(val attrs: Attrs,
                     val name: Global,
                     val tys: Seq[nir.Type])
      extends Scope

  final class Trait(val attrs: Attrs,
                    val name: Global,
                    val traitNames: Seq[Global])
      extends Scope {
    var traits: Seq[Trait]      = _
    var alltraits: Seq[Trait]   = _
    var allmethods: Seq[Method] = _
  }

  final class Class(val attrs: Attrs,
                    val name: Global,
                    val parentName: Option[Global],
                    val traitNames: Seq[Global],
                    val isModule: Boolean)
      extends Scope {
    val subclasses = mutable.UnrolledBuffer.empty[Class]

    var range: Range                        = _
    var parent: Option[Class]               = _
    var traits: Seq[Trait]                  = _
    var alltraits: Seq[Trait]               = _
    var allfields: Seq[Field]               = _
    var allmethods: Seq[Method]             = _
    var allvslots: Seq[Method]              = _
    var alloverrides: Seq[(Method, Method)] = _
    var vslots: Seq[Method]                 = _
    var vtable: Seq[Val]                    = _
    var imap: mutable.Map[Method, Val]      = _
    var dynmethods: Seq[Method]             = _
    var alldynmethods: Seq[Method]          = _
    var dynDispatchTableValue: Val          = _

    def ty: Type.Class =
      Type.Class(name)
    def vtableStruct: Type.Struct =
      Type.Struct(Global.None, vtable.map(_.ty))
    def vtableValue: Val.Struct =
      Val.Struct(Global.None, vtable)
    def dynDispatchTableStruct =
      Type.Struct(Global.None, Seq(Type.Int, Type.Ptr, Type.Ptr, Type.Ptr))
    def size: Long = MemoryLayout(Type.Ptr +: allfields.map(_.ty)).size
    def typeStruct: Type.Struct =
      Type.Struct(Global.None,
                  Seq(Type.Int,
                      Type.Ptr,
                      Type.Long,
                      dynDispatchTableStruct,
                      vtableStruct))
    def typeValue: Val.Struct =
      Val.Struct(Global.None,
                 Seq(Val.Int(id),
                     Val.String(name.id),
                     Val.Long(size),
                     dynDispatchTableValue,
                     vtableValue))
    def typeConst: Val =
      Val.Global(name member "type", Type.Ptr)
    def classStruct: Type.Struct = {
      val data            = allfields.map(_.ty)
      val classStructName = name member "layout"
      val classStructBody = Type.Ptr +: data
      val classStructTy   = Type.Struct(classStructName, classStructBody)

      Type.Struct(classStructName, classStructBody)
    }
  }

  final class Method(val attrs: Attrs,
                     val name: Global,
                     val ty: nir.Type,
                     val isConcrete: Boolean)
      extends Node {
    val overrides                   = mutable.UnrolledBuffer.empty[Method]
    val overriden                   = mutable.UnrolledBuffer.empty[Method]
    var vslot: Method               = _
    var vindex: Int                 = _
    var classOverrides: Seq[Method] = _
    var traitOverrides: Seq[Method] = _

    def isVirtual = !isConcrete || overriden.nonEmpty

    def isStatic = !isVirtual

    val value =
      if (isConcrete) Val.Global(name, Type.Ptr)
      else Val.Zero(Type.Ptr)

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
                  val dyns: Seq[String],
                  override val methods: Seq[Method],
                  override val fields: Seq[Field])
      extends Scope {
    val fresh = nir.Fresh("tx")
    def name  = Global.None
    def attrs = Attrs.None

    val dispatchName       = Global.Top("__dispatch")
    val dispatchVal        = Val.Global(dispatchName, Type.Ptr)
    var dispatchTy: Type   = _
    var dispatchDefn: Defn = _

    val instanceName       = Global.Top("__instance")
    val instanceVal        = Val.Global(instanceName, Type.Ptr)
    var instanceTy: Type   = _
    var instanceDefn: Defn = _
  }

  def apply(defns: Seq[Defn], dyns: Seq[String]): Top = {
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
        enter(defn.name,
              new Class(defn.attrs,
                        defn.name,
                        defn.parent,
                        defn.traits,
                        isModule = true))

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

    def sortTraits(traits: Seq[Trait]): Seq[Trait] = {
      var res     = mutable.UnrolledBuffer.empty[Trait]
      val todo    = mutable.Stack.empty[Trait]
      val visited = mutable.Set.empty[Trait]
      todo.pushAll(traits)

      def visit(trt: Trait): Unit = {
        if (!visited.contains(trt)) {
          trt.traitNames.foreach { n =>
            visit(nodes(n).asInstanceOf[Trait])
          }
          visited += trt
          res += trt
        }
      }

      while (todo.nonEmpty) {
        visit(todo.pop())
      }

      res
    }

    def sortClasses(classes: Seq[Class]): Seq[Class] = {
      var res     = mutable.UnrolledBuffer.empty[Class]
      val todo    = mutable.Stack.empty[Class]
      val visited = mutable.Set.empty[Class]
      todo.pushAll(classes)

      def visit(cls: Class): Unit = {
        if (!visited.contains(cls)) {
          cls.parentName.foreach { n =>
            visit(nodes(n).asInstanceOf[Class])
          }
          visited += cls
          res += cls
        }
      }

      while (todo.nonEmpty) {
        visit(todo.pop())
      }

      res
    }

    defns.foreach(enterDefn)
    val top = new Top(nodes = nodes,
                      structs = structs,
                      classes = sortClasses(classes),
                      traits = sortTraits(traits),
                      methods = methods,
                      fields = fields,
                      dyns = dyns)
    top.members ++= nodes.values

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

    def completeMethods(): Unit = methods.foreach { node =>
      if (node.name.isTop) {
        node.in = top
      } else {
        val owner = nodes(node.name.top).asInstanceOf[Scope]
        node.in = owner
        owner.members += node
        node.attrs.overrides.foreach { n =>
          val ovnode = nodes(n).asInstanceOf[Method]
          node.overrides += ovnode
          ovnode.overriden += node
        }
      }
    }

    def completeFields(): Unit = fields.foreach { node =>
      val parent = nodes(node.name.top).asInstanceOf[Class]
      node.in = parent
      parent.members += node
    }

    def completeTraits(): Unit =
      top.traits.foreach { node =>
        node.in = top
        node.traits = node.traitNames.map(n => nodes(n).asInstanceOf[Trait])
        node.alltraits = node.traits.flatMap(_.alltraits).distinct :+ node
        node.allmethods = node.alltraits.init
            .flatMap(_.allmethods) ++ node.methods
      }

    def completeClasses(): Unit = top.classes.foreach { self =>
      import self._

      in = top
      parent = parentName.map(nodes(_).asInstanceOf[Class])
      self.traits = traitNames.map(nodes(_).asInstanceOf[Trait])
      parent.foreach { parent =>
        parent.subclasses += self
      }
      vslots = members.collect {
        case meth: Method if meth.isVirtual =>
          meth
      }
      allvslots = {
        val base = parent.fold(Seq.empty[Method])(_.allvslots)
        base ++ vslots
      }
      self.methods.foreach { method =>
        method.classOverrides = method.overrides.collect {
          case meth if meth.inClass =>
            meth
        }
        method.traitOverrides = method.overrides.collect {
          case meth if meth.inTrait =>
            meth
        }
        if (method.isVirtual) {
          method.vslot = method.classOverrides.headOption.fold(method)(_.vslot)
          method.vindex = allvslots.indexOf(method.vslot)
        }
      }
      alltraits = {
        val base = parent.fold(Seq.empty[Trait])(_.alltraits)
        (base ++ self.traits.flatMap(_.alltraits)).distinct
      }
      allmethods = {
        val base = parent.fold(Seq.empty[Method])(_.allmethods)
        base ++ self.methods
      }
      alloverrides = {
        val base = parent.fold(Seq.empty[(Method, Method)])(_.alloverrides)
        base ++ self.methods.flatMap {
          case meth if meth.overrides.nonEmpty =>
            meth.overrides.map(ov => (meth, ov))
          case _ =>
            Seq()
        }
      }
      allfields = {
        val base = parent.fold(Seq.empty[Field])(_.allfields)
        base ++ self.fields
      }
      vtable = {
        val base = parent.fold(Seq.empty[Val])(_.vtable)

        val overrides = self.methods.flatMap { meth =>
          meth.classOverrides.map((meth, _))
        }
        val baseWithOverrides = overrides.foldLeft(base) {
          case (base, (meth, ovmeth)) =>
            base.updated(ovmeth.vindex, meth.value)
        }

        baseWithOverrides ++ vslots.map(_.value)
      }
      imap = {
        val traitOverrides = allmethods.flatMap { meth =>
          meth.traitOverrides.map((meth, _))
        }
        val methodMap = mutable.Map.empty[Method, Val]
        traitOverrides.foreach {
          case (meth, ovmeth) =>
            methodMap(ovmeth) = meth.value
        }
        methodMap
      }
      dynmethods = self.methods.filter(_.attrs.isDyn)
      alldynmethods = {
        val signatureSet = dynmethods.map(m => m.name.id).toSet

        parent
          .fold(Seq.empty[Method])(_.alldynmethods)
          .filterNot(m => signatureSet.contains(m.name.id)) ++ dynmethods
      }
      dynDispatchTableValue = DynmethodPerfectHashMap(alldynmethods, dyns)
    }

    def completeTopDispatchTable(): Unit = {
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

      top.dispatchTy = table.ty
      top.dispatchDefn =
        Defn.Const(Attrs.None, top.dispatchName, table.ty, table)
    }

    def completeTopInstanceTable(): Unit = {
      val columns = classes.sortBy(_.id).map { cls =>
        val row = new Array[Boolean](traits.length)
        cls.alltraits.foreach { trt =>
          row(trt.id) = true
        }
        Val.Array(Type.Bool, row.map(Val.Bool))
      }
      val table = Val.Array(Type.Array(Type.Bool, traits.length), columns)

      top.instanceTy = table.ty
      top.instanceDefn =
        Defn.Const(Attrs.None, top.instanceName, table.ty, table)
    }

    completeMethods()
    assignMethodIds()
    completeFields()
    completeTraits()
    completeClasses()
    assignClassIds()
    completeTopDispatchTable()
    completeTopInstanceTable()

    top
  }
}
