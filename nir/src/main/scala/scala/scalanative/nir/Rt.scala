package scala.scalanative
package nir

import Type._

object Rt {
  val Object = Ref(Global.Top("java.lang.Object"))
  val String = Ref(Global.Top("java.lang.String"))
  val Type   = StructValue(Seq(Int, Int, Ptr, Byte))

  val JavaEqualsSig    = Sig.Method("equals", Seq(Object, Bool))
  val JavaHashCodeSig  = Sig.Method("hashCode", Seq(Int))
  val ScalaEqualsSig   = Sig.Method("scala_==", Seq(Object, Bool))
  val ScalaHashCodeSig = Sig.Method("scala_##", Seq(Int))

  val arrayAlloc: Map[Sig, Global] = Seq(
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
    val cls = Global.Top("scala.scalanative.runtime." + arr)
    val sig = Sig.Method("alloc", Seq(Int, Ref(cls)))
    sig -> cls
  }.toMap
}
