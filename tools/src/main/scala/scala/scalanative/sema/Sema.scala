package scala.scalanative
package sema

import scala.collection.mutable
import scalanative.nir._
import scalanative.util.unreachable

object Sema {
  def apply(entries: Seq[Global], defns: Seq[Defn]): Top = {
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
        enter(defn.name, new Method(defn.attrs, defn.name, defn.ty, Seq()))

      case defn: Defn.Define =>
        enter(defn.name, new Method(defn.attrs, defn.name, defn.ty, defn.insts))

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

    val top =
      new Top(nodes = nodes,
              structs = structs,
              classes = sortClasses(classes),
              traits = sortTraits(traits),
              methods = methods,
              fields = fields)

    def completeMethods(): Unit = methods.foreach { meth =>
      if (meth.name.isTop) {
        meth.in = top
      } else {
        val owner = nodes(meth.name.top).asInstanceOf[Scope]
        meth.in = owner
        owner.methods += meth
      }
    }

    def completeFields(): Unit = fields.foreach { node =>
      val owner = nodes(node.name.top).asInstanceOf[Class]
      node.in = owner
      owner.fields += node
    }

    def completeTraits(): Unit =
      top.traits.foreach { node =>
        node.in = top
        node.traitNames.foreach { name =>
          node.traits += nodes(name).asInstanceOf[Trait]
        }
      }

    def completeStructs(): Unit =
      top.structs.foreach { node =>
        node.in = top
      }

    def completeClasses(): Unit = top.classes.foreach { cls =>
      cls.in = top
      cls.parent = cls.parentName.map { name =>
        nodes(name).asInstanceOf[Class]
      }
      cls.traitNames.foreach { name =>
        cls.traits += nodes(name).asInstanceOf[Trait]
      }
      def loopParent(parent: Class): Unit = {
        parent.subclasses += cls
        parent.parent.foreach(loopParent)
        parent.traits.foreach(loopTraits)
      }
      def loopTraits(trt: Trait): Unit = {
        trt.implementors += cls
        trt.traits.foreach(loopTraits)
      }
      cls.parent.foreach(loopParent)
      cls.traits.foreach(loopTraits)
    }

    def completeAllocatedAndCalled(): Unit = {
      def markCalled(scopeName: Global, methName: Global): Unit = {
        val sig = methName.id
        nodes(scopeName).asInstanceOf[Scope].calls += sig
      }
      def markAllocated(clsName: Global): Unit = {
        val cls = top.nodes(clsName).asInstanceOf[Class]
        cls.allocated = true
      }

      entries.foreach { entry =>
        top.nodes.get(entry.top) match {
          case Some(node: Class) =>
            markAllocated(node.name)
          case _ =>
            ()
        }
      }

      methods.foreach { meth =>
        meth.insts.foreach {
          case Inst.Let(_, op, _) =>
            op match {
              case Op.Method(obj, methName) =>
                obj.ty match {
                  case Type.Module(name) =>
                    markCalled(name, methName)
                  case Type.Class(name) =>
                    markCalled(name, methName)
                  case Type.Trait(name) =>
                    markCalled(name, methName)
                  case _ =>
                    ()
                }
                if (arrayAlloc.contains(methName)) {
                  markAllocated(arrayAlloc(methName))
                }
              case Op.Classalloc(name) =>
                markAllocated(name)
              case Op.Module(name) =>
                markAllocated(name)
              case Op.Call(_, Val.Global(methName, _), _)
                  if arrayAlloc.contains(methName) =>
                markAllocated(arrayAlloc(methName))
              case _ =>
                ()
            }
          case _ =>
            ()
        }
      }
    }

    def completeResolved(): Unit = {
      top.classes.foreach { cls =>
        def update(sig: String): Unit =
          cls.resolved(sig) = cls.resolveImpl(sig).get

        cls.parent.fold {
          cls.resolved = mutable.Map.empty
        } { parent =>
          cls.resolved = parent.resolved.clone()
        }

        cls.methods.foreach { meth =>
          meth.name.id match {
            case Rt.JavaEqualsSig =>
              update(Rt.ScalaEqualsSig)
              update(Rt.JavaEqualsSig)
            case Rt.JavaHashCodeSig =>
              update(Rt.ScalaHashCodeSig)
              update(Rt.JavaHashCodeSig)
            case sig =>
              update(sig)
          }
        }
      }
    }

    completeFields()
    completeMethods()
    completeTraits()
    completeStructs()
    completeClasses()
    completeAllocatedAndCalled()
    completeResolved()

    top
  }

  val arrayAlloc = Seq(
    "BooleanArray",
    "CharArray",
    "ByteArray",
    "ShortArray",
    "IntArray",
    "LongArray",
    "FloatArray",
    "DoubleArray",
    "ObjectArray"
  ).map { arr =>
    val cls          = "scala.scalanative.runtime." + arr
    val module       = "scala.scalanative.runtime." + arr + "$"
    val from: Global = Global.Member(Global.Top(module), "alloc_i32_" + cls)
    val to: Global   = Global.Top(cls)
    from -> to
  }.toMap
}
