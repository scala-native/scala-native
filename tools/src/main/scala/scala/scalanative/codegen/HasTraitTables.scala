package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Trait, Class}

class HasTraitTables(meta: Metadata) {
  private implicit val pos: Position = Position.NoPosition

  private val classHasTraitName = Global.Top("__class_has_trait")
  val classHasTraitVal = Val.Global(classHasTraitName, Type.Ptr)
  var classHasTraitDefn: Defn = _

  private val traitHasTraitName = Global.Top("__trait_has_trait")
  val traitHasTraitVal = Val.Global(traitHasTraitName, Type.Ptr)
  var traitHasTraitDefn: Defn = _

  initClassHasTrait()
  initTraitHasTrait()

  private def markTraits(matrix: BitMatrix, row: Int, cls: Class): Unit = {
    cls.traits.foreach(markTraits(matrix, row, _))
    cls.parent.foreach(markTraits(matrix, row, _))
  }

  private def markTraits(matrix: BitMatrix, row: Int, trt: Trait): Unit = {
    matrix.set(row, meta.ids(trt))
    trt.traits.foreach(markTraits(matrix, row, _))
  }

  private def initClassHasTrait(): Unit = {
    println(s"meta.classes.length = ${meta.classes.length}")
    println(s"meta.traits.length = ${meta.traits.length}")
    val matrix = BitMatrix(meta.classes.length, meta.traits.length)
    var row = 0
    meta.classes.foreach { cls =>
      markTraits(matrix, row, cls)

      row += 1
    }
    val tableVal = Val.ArrayValue(Type.Long, matrix.toSeq.map(Val.Long))

    classHasTraitDefn =
      Defn.Const(Attrs.None, classHasTraitName, tableVal.ty, tableVal)
  }

  private def initTraitHasTrait(): Unit = {
    val matrix = BitMatrix(meta.traits.length, meta.traits.length)
    var row = 0
    meta.traits.foreach { left =>
      markTraits(matrix, row, left)
      matrix.set(row, meta.ids(left))

      row += 1
    }
    val tableVal = Val.ArrayValue(Type.Long, matrix.toSeq.map(Val.Long))

    traitHasTraitDefn =
      Defn.Const(Attrs.None, traitHasTraitName, tableVal.ty, tableVal)
  }
}
