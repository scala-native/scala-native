package scala.scalanative
package codegen

import scalanative.linker.{Trait, Class}

class HasTraitTables(meta: Metadata) {

  private implicit val pos: nir.SourcePosition = nir.SourcePosition.NoPosition

  def generated(id: String): nir.Global.Member =
    nir.Global.Top("__scalanative_metadata").member(nir.Sig.Generated(id))

  private val classHasTraitName = generated("__class_has_trait")
  val classHasTraitVal = nir.Val.Global(classHasTraitName, nir.Type.Ptr)
  var classHasTraitTy: nir.Type = _
  var classHasTraitDefn: nir.Defn = _

  private val traitHasTraitName = generated("__trait_has_trait")
  val traitHasTraitVal = nir.Val.Global(traitHasTraitName, nir.Type.Ptr)
  var traitHasTraitTy: nir.Type = _
  var traitHasTraitDefn: nir.Defn = _

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
    val tableVal =
      nir.Val.ArrayValue(nir.Type.Int, matrix.toSeq.map(i => nir.Val.Int(i)))

    classHasTraitDefn =
      nir.Defn.Const(nir.Attrs.None, classHasTraitName, tableVal.ty, tableVal)
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
    val tableVal =
      nir.Val.ArrayValue(nir.Type.Int, matrix.toSeq.map(l => nir.Val.Int(l)))

    traitHasTraitDefn =
      nir.Defn.Const(nir.Attrs.None, traitHasTraitName, tableVal.ty, tableVal)
    traitHasTraitTy = tableVal.ty
  }

}
