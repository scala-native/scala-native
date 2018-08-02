package scala.scalanative
package sema

import scala.collection.mutable
import util.unreachable
import nir._

object Sema {
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
                      fields = fields)
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
      }

    def completeStructs(): Unit =
      top.structs.foreach { node =>
        node.in = top
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

    completeFields()
    completeMethods()
    completeTraits()
    completeStructs()
    completeClasses()
    assignClassIds()
    assignMethodIds()

    top
  }
}
