package scala.scalanative.util

object ScalaStdlibCompat {
  private[scalanative] object ArraySeqCompatDef {
    val ArraySeq = scala.collection.immutable.Vector
    type ArraySeq[T] = scala.collection.immutable.Vector[T]
  }

  private[scalanative] object ArraySeqCompatSelect {
    import ArraySeqCompatDef.*
    object Inner {
      import scala.collection.immutable.*
      val ArraySeqAlias = ArraySeq
      type ArraySeqAlias[T] = ArraySeq[T]
    }
  }
  // Vector in Scala 2.12, ArraySeq otherwise
  val ArraySeqCompat = ArraySeqCompatSelect.Inner.ArraySeqAlias
  type ArraySeqCompat[T] = ArraySeqCompatSelect.Inner.ArraySeqAlias[T]
}
