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
    var rtti: RuntimeTypeInformation = _

    val members = mutable.UnrolledBuffer.empty[Node]
    val methods = mutable.UnrolledBuffer.empty[Method]
    val fields  = mutable.UnrolledBuffer.empty[Field]
  }

  final class Struct(val attrs: Attrs,
                     val name: Global,
                     val tys: Seq[nir.Type])
      extends Scope

  final class Trait(val attrs: Attrs,
                    val name: Global,
                    val traitNames: Seq[Global])
      extends Scope {
    val traits = mutable.UnrolledBuffer.empty[Trait]
  }

  final class Class(val attrs: Attrs,
                    val name: Global,
                    val parentName: Option[Global],
                    val traitNames: Seq[Global],
                    val isModule: Boolean)
      extends Scope {
    val ty         = Type.Class(name)
    val subclasses = mutable.UnrolledBuffer.empty[Class]
    val traits     = mutable.UnrolledBuffer.empty[Trait]

    var parent: Option[Class]  = _
    var range: Range           = _
    var vtable: VirtualTable   = _
    var layout: FieldLayout    = _
    var dynmap: DynamicHashMap = _
  }

  final class Method(val attrs: Attrs,
                     val name: Global,
                     val ty: nir.Type,
                     val isConcrete: Boolean)
      extends Node {
    val overrides = mutable.UnrolledBuffer.empty[Method]
    val overriden = mutable.UnrolledBuffer.empty[Method]
    val value =
      if (isConcrete) Val.Global(name, Type.Ptr)
      else Val.Null
    def isVirtual =
      !isConcrete || overriden.nonEmpty
    def isStatic =
      !isVirtual
  }

  final class Field(val attrs: Attrs, val name: Global, val ty: nir.Type)
      extends Node

  final class Top(val nodes: mutable.Map[Global, Node],
                  val structs: Seq[Struct],
                  val classes: Seq[Class],
                  val traits: Seq[Trait],
                  val dyns: Seq[String],
                  override val methods: mutable.UnrolledBuffer[Method],
                  override val fields: mutable.UnrolledBuffer[Field])
      extends Scope {
    val fresh                       = nir.Fresh("tx")
    def name                        = Global.None
    def attrs                       = Attrs.None
    var tables: TraitDispatchTables = _
    var moduleArray: ModuleArray    = _
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

    def assignMethodIds(): Unit = {
      var id = 0
      traits.foreach { trt =>
        trt.methods.foreach { meth =>
          meth.id = id
          id += 1
        }
      }
      classes.foreach { cls =>
        cls.methods.foreach { meth =>
          meth.id = id
          id += 1
        }
      }
    }

    def completeMethods(): Unit = methods.foreach { meth =>
      if (meth.name.isTop) {
        meth.in = top
      } else {
        val owner = nodes(meth.name.top).asInstanceOf[Scope]
        meth.in = owner
        owner.members += meth
        owner.methods += meth
        meth.attrs.overrides.foreach { name =>
          val ovmeth = nodes(name).asInstanceOf[Method]
          meth.overrides += ovmeth
          ovmeth.overriden += meth
        }
      }
    }

    def completeFields(): Unit = fields.foreach { node =>
      val owner = nodes(node.name.top).asInstanceOf[Class]
      node.in = owner
      owner.members += node
      owner.fields += node
    }

    def completeTraits(): Unit =
      top.traits.foreach { node =>
        node.in = top
        node.traitNames.foreach { name =>
          node.traits += nodes(name).asInstanceOf[Trait]
        }
        node.rtti = new RuntimeTypeInformation(node)
      }

    def completeStructs(): Unit =
      top.structs.foreach { node =>
        node.in = top
        node.rtti = new RuntimeTypeInformation(node)
      }

    def completeClasses(): Unit = top.classes.foreach { cls =>
      cls.in = top
      cls.parent = cls.parentName.map { name =>
        val parent = nodes(name).asInstanceOf[Class]
        parent.subclasses += cls
        parent
      }
      cls.traitNames.foreach { name =>
        cls.traits += nodes(name).asInstanceOf[Trait]
      }
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

    def completeClassMembers(): Unit = top.classes.foreach { cls =>
      cls.vtable = new VirtualTable(cls)
      cls.layout = new FieldLayout(cls)
      cls.dynmap = new DynamicHashMap(cls, dyns)
      cls.rtti = new RuntimeTypeInformation(cls)
    }

    def completeTop(): Unit = {
      top.tables = new TraitDispatchTables(top)
      top.moduleArray = new ModuleArray(top)
    }

    completeFields()
    completeMethods()
    completeTraits()
    completeStructs()
    completeClasses()
    assignClassIds()
    assignMethodIds()
    completeClassMembers()
    completeTop()

    top
  }
}
