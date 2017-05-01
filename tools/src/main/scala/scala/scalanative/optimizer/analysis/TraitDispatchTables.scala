package scala.scalanative
package optimizer
package analysis

import ClassHierarchy._
import nir._

class TraitDispatchTables(top: Top) {
  val dispatchName                     = Global.Top("__dispatch")
  val dispatchVal                      = Val.Global(dispatchName, Type.Ptr)
  var dispatchTy: Type                 = _
  var dispatchDefn: Defn               = _
  var dispatchOffset: Map[Method, Int] = _

  val classHasTraitName       = Global.Top("__class_has_trait")
  val classHasTraitVal        = Val.Global(classHasTraitName, Type.Ptr)
  var classHasTraitTy: Type   = _
  var classHasTraitDefn: Defn = _

  val traitHasTraitName       = Global.Top("__trait_has_trait")
  val traitHasTraitVal        = Val.Global(traitHasTraitName, Type.Ptr)
  var traitHasTraitTy: Type   = _
  var traitHasTraitDefn: Defn = _

  def initDispatch(): Unit = {
    val methods       = top.methods.filter(_.inTrait).sortBy(_.id)
    val methodsLength = methods.length
    val classes       = top.classes.sortBy(_.id)
    val classesLength = classes.length
    val table =
      Array.fill[Val](classes.length * methods.length)(Val.Null)
    def put(cls: Int, meth: Int, value: Val) =
      table(meth * classesLength + cls) = value
    def get(cls: Int, meth: Int) =
      table(meth * classesLength + cls)

    classes.foreach { cls =>
      def visit(cur: Class): Unit = {
        cur.methods.foreach { meth =>
          meth.overrides.foreach { ovmeth =>
            if (ovmeth.inTrait && (get(cls.id, ovmeth.id) eq Val.Null)) {
              put(cls.id, ovmeth.id, meth.value)
            }
          }
        }
        cur.parent.foreach(visit)
      }
      visit(cls)
    }

    val value = Val.Array(Type.Ptr, table)

    dispatchOffset = methods.map { m =>
      m -> m.id * classes.length
    }.toMap
    dispatchTy = Type.Ptr
    dispatchDefn = Defn.Const(Attrs.None, dispatchName, value.ty, value)
  }

  def markTraits(row: Array[Boolean], cls: Class): Unit = {
    cls.traits.foreach(markTraits(row, _))
    cls.parent.foreach(markTraits(row, _))
  }

  def markTraits(row: Array[Boolean], trt: Trait): Unit = {
    row(trt.id) = true
    trt.traits.foreach { right =>
      row(right.id) = true
    }
    trt.traits.foreach(markTraits(row, _))
  }

  def initClassHasTrait(): Unit = {
    val columns = top.classes.sortBy(_.id).map { cls =>
      val row = new Array[Boolean](top.traits.length)
      markTraits(row, cls)
      Val.Array(Type.Bool, row.map(Val.Bool))
    }
    val table = Val.Array(Type.Array(Type.Bool, top.traits.length), columns)

    classHasTraitTy = table.ty
    classHasTraitDefn =
      Defn.Const(Attrs.None, classHasTraitName, table.ty, table)
  }

  def initTraitHasTrait(): Unit = {
    val columns = top.traits.sortBy(_.id).map { left =>
      val row = new Array[Boolean](top.traits.length)
      markTraits(row, left)
      row(left.id) = true
      Val.Array(Type.Bool, row.map(Val.Bool))
    }
    val table = Val.Array(Type.Array(Type.Bool, top.traits.length), columns)

    traitHasTraitTy = table.ty
    traitHasTraitDefn =
      Defn.Const(Attrs.None, traitHasTraitName, table.ty, table)
  }

  initDispatch()
  initClassHasTrait()
  initTraitHasTrait()
}
