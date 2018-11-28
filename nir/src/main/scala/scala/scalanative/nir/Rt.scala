package scala.scalanative
package nir

import Type._

object Rt {
  val Object    = Ref(Global.Top("java.lang.Object"))
  val Class     = Ref(Global.Top("java.lang.Class"))
  val String    = Ref(Global.Top("java.lang.String"))
  val BoxedUnit = Ref(Global.Top("scala.runtime.BoxedUnit"))
  val Type      = StructValue(Seq(Int, Int, Ptr, Byte))

  val JavaEqualsSig       = Sig.Method("equals", Seq(Object, Bool))
  val JavaHashCodeSig     = Sig.Method("hashCode", Seq(Int))
  val ScalaEqualsSig      = Sig.Method("scala_==", Seq(Object, Bool))
  val ScalaHashCodeSig    = Sig.Method("scala_##", Seq(Int))
  val GetClassSig         = Sig.Method("getClass", Seq(Class))
  val IsArraySig          = Sig.Method("isArray", Seq(Bool))
  val IsAssignableFromSig = Sig.Method("isAssignableFrom", Seq(Class, Bool))
  val GetNameSig          = Sig.Method("getName", Seq(String))
  val BitCountSig         = Sig.Method("bitCount", Seq(Int, Int))
  val ReverseBytesSig     = Sig.Method("reverseBytes", Seq(Int, Int))
  val NumberOfLeadingZerosSig =
    Sig.Method("numberOfLeadingZeros", Seq(Int, Int))
  val CosSig  = Sig.Method("cos", Seq(Double, Double))
  val SinSig  = Sig.Method("sin", Seq(Double, Double))
  val PowSig  = Sig.Method("pow", Seq(Double, Double, Double))
  val MaxSig  = Sig.Method("max", Seq(Double, Double, Double))
  val SqrtSig = Sig.Method("sqrt", Seq(Double, Double))

  val StringName               = String.name
  val StringValueName          = StringName member Sig.Field("value")
  val StringOffsetName         = StringName member Sig.Field("offset")
  val StringCountName          = StringName member Sig.Field("count")
  val StringCachedHashCodeName = StringName member Sig.Field("cachedHashCode")

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
