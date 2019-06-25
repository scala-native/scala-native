package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Trait, Class}

class HasTraitTables(meta: Metadata) {
  val classHasTraitName       = Global.Top("__class_has_trait")
  val classHasTraitVal        = Val.Global(classHasTraitName, Type.Ptr)
  var classHasTraitTy: Type   = _
  var classHasTraitDefn: Defn = _

  val traitHasTraitName       = Global.Top("__trait_has_trait")
  val traitHasTraitVal        = Val.Global(traitHasTraitName, Type.Ptr)
  var traitHasTraitTy: Type   = _
  var traitHasTraitDefn: Defn = _

  initClassHasTrait()
  initTraitHasTrait()

  def markTraits(row: Array[Boolean], cls: Class): Unit = {
    cls.traits.foreach(markTraits(row, _))
    cls.parent.foreach(markTraits(row, _))
  }

  def markTraits(row: Array[Boolean], trt: Trait): Unit = {
    row(meta.ids(trt)) = true
    trt.traits.foreach { right =>
      row(meta.ids(right)) = true
    }
    trt.traits.foreach(markTraits(row, _))
  }

  def initClassHasTrait(): Unit = {
    val columns = meta.classes.map { cls =>
      val row = new Array[Boolean](meta.traits.length)
      markTraits(row, cls)
      Val.ArrayValue(Type.Bool, row.map(Val.Bool))
    }
    val table =
      Val.ArrayValue(Type.ArrayValue(Type.Bool, meta.traits.length), columns)

    classHasTraitTy = table.ty
    classHasTraitDefn =
      Defn.Const(Attrs.None, classHasTraitName, table.ty, table)
  }

  def initTraitHasTrait(): Unit = {
    val columns = meta.traits.map { left =>
      val row = new Array[Boolean](meta.traits.length)
      markTraits(row, left)
      row(meta.ids(left)) = true
      Val.ArrayValue(Type.Bool, row.map(Val.Bool))
    }
    val table =
      Val.ArrayValue(Type.ArrayValue(Type.Bool, meta.traits.length), columns)

    traitHasTraitTy = table.ty
    traitHasTraitDefn =
      Defn.Const(Attrs.None, traitHasTraitName, table.ty, table)
  }
}
