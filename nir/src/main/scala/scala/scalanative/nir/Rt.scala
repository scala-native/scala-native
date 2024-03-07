package scala.scalanative
package nir

import Type._

private[scalanative] object Rt {
  val Object = Ref(Global.Top("java.lang.Object"))
  val Class = Ref(Global.Top("java.lang.Class"))
  val String = Ref(Global.Top("java.lang.String"))
  val Runtime = Ref(Global.Top("scala.scalanative.runtime.package$"))
  val RuntimeNothing = Type.Ref(Global.Top("scala.runtime.Nothing$"))
  val RuntimeNull = Type.Ref(Global.Top("scala.runtime.Null$"))

  val BoxedPtr = Ref(Global.Top("scala.scalanative.unsafe.Ptr"))
  val BoxedNull = Ref(Global.Top("scala.runtime.Null$"))
  val BoxedUnit = Ref(Global.Top("scala.runtime.BoxedUnit"))
  val BoxedUnitModule = Ref(Global.Top("scala.scalanative.runtime.BoxedUnit$"))

  val GetClassSig = Sig.Method("getClass", Seq(Rt.Class)).mangled
  val ScalaMainSig =
    Sig.Method("main", Seq(Array(Rt.String), Unit), Sig.Scope.PublicStatic)
  val IsArraySig = Sig.Method("isArray", Seq(Bool)).mangled
  val IsAssignableFromSig =
    Sig.Method("isAssignableFrom", Seq(Class, Bool)).mangled
  val GetNameSig = Sig.Method("getName", Seq(String)).mangled
  val BitCountSig = Sig.Method("bitCount", Seq(Int, Int)).mangled
  val ReverseBytesSig = Sig.Method("reverseBytes", Seq(Int, Int)).mangled
  val NumberOfLeadingZerosSig =
    Sig.Method("numberOfLeadingZeros", Seq(Int, Int)).mangled
  val CosSig = Sig.Method("cos", Seq(Double, Double)).mangled
  val SinSig = Sig.Method("sin", Seq(Double, Double)).mangled
  val PowSig = Sig.Method("pow", Seq(Double, Double, Double)).mangled
  val MaxSig = Sig.Method("max", Seq(Double, Double, Double)).mangled
  val SqrtSig = Sig.Method("sqrt", Seq(Double, Double)).mangled
  val FromRawPtrSig = Sig.Method("fromRawPtr", Seq(Ptr, BoxedPtr)).mangled
  val ToRawPtrSig = Sig.Method("toRawPtr", Seq(BoxedPtr, Ptr)).mangled

  val ClassName = Class.name
  val ClassIdName = ClassName.member(Sig.Field("id"))
  val ClassTraitIdName = ClassName.member(Sig.Field("traitId"))
  val ClassNameName = ClassName.member(Sig.Field("name"))
  val ClassSizeName = ClassName.member(Sig.Field("size"))
  val ClassIdRangeUntilName = ClassName.member(Sig.Field("idRangeUntil"))

  val StringName = String.name
  val StringValueName = StringName.member(Sig.Field("value"))
  val StringOffsetName = StringName.member(Sig.Field("offset"))
  val StringCountName = StringName.member(Sig.Field("count"))
  val StringCachedHashCodeName = StringName.member(Sig.Field("cachedHashCode"))

  val PrimitiveTypes: Seq[Global.Top] = Seq(
    "Byte",
    "Short",
    "Int",
    "Long",
    "Char",
    "Float",
    "Double",
    "Boolean",
    "Unit"
  ).map(name => Global.Top(s"scala.scalanative.runtime.Primitive$name"))

  val GenericArray = Ref(Global.Top("scala.scalanative.runtime.Array"))

  val arrayAlloc: Map[Sig, Global.Top] = Seq(
    "BooleanArray",
    "CharArray",
    "ByteArray",
    "ShortArray",
    "IntArray",
    "LongArray",
    "FloatArray",
    "DoubleArray",
    "ObjectArray",
    "BlobArray"
  ).map { arr =>
    val cls = Global.Top("scala.scalanative.runtime." + arr)
    val sig = Sig.Method("alloc", Seq(Int, Ref(cls))).mangled
    sig -> cls
  }.toMap
  val RuntimeObjectMonitor = Ref(
    Global.Top("scala.scalanative.runtime.monitor.ObjectMonitor")
  )
}
