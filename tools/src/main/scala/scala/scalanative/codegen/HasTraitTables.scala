package scala.scalanative
package codegen

import scalanative.nir._
import scalanative.linker.{Trait, Class}

class HasTraitTables(meta: Metadata) {
  private implicit val pos: Position = Position.NoPosition
  def generated(id: String): Global.Member =
    Global.Top("__scalanative_metadata").member(Sig.Generated(id))
  private val classHasTraitName = generated("__class_has_trait")
  val classHasTraitVal = Val.Global(classHasTraitName, Type.Ptr)
  var classHasTraitTy: Type = _
  var classHasTraitDefn: Defn = _

  private val traitHasTraitName = generated("__trait_has_trait")
  val traitHasTraitVal = Val.Global(traitHasTraitName, Type.Ptr)
  var traitHasTraitTy: Type = _
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
    val matrix = BitMatrix(meta.classes.length, meta.traits.length)
    var row = 0
    meta.classes.foreach { cls =>
      markTraits(matrix, row, cls)

      row += 1
    }
    val tableVal = Val.ArrayValue(Type.Int, matrix.toSeq.map(i => Val.Int(i)))

    classHasTraitDefn =
      Defn.Const(Attrs.None, classHasTraitName, tableVal.ty, tableVal)
    classHasTraitTy = tableVal.ty
  }

  private def initTraitHasTrait(): Unit = {
    val matrix = BitMatrix(meta.traits.length, meta.traits.length)
    var row = 0
    meta.traits.foreach { left =>
      markTraits(matrix, row, left)
      matrix.set(row, meta.ids(left))

      row += 1
    }
    val tableVal = Val.ArrayValue(Type.Int, matrix.toSeq.map(l => Val.Int(l)))

    traitHasTraitDefn =
      Defn.Const(Attrs.None, traitHasTraitName, tableVal.ty, tableVal)
    traitHasTraitTy = tableVal.ty
  }
}
