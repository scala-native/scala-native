package scala.scalanative
package nir

import Type._

object Rt {
  val Object = Class(Global.Top("java.lang.Object"))
  val String = Class(Global.Top("java.lang.String"))
  val Type   = StructValue(Global.None, Seq(Int, Int, Ptr, Byte))

  val JavaEqualsSig    = "equals_java.lang.Object_bool"
  val JavaHashCodeSig  = "hashCode_i32"
  val ScalaEqualsSig   = "scala$underscore$==_java.lang.Object_bool"
  val ScalaHashCodeSig = "scala$underscore$##_i32"

  val arrayAlloc = Seq(
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
    val cls          = "scala.scalanative.runtime." + arr
    val module       = "scala.scalanative.runtime." + arr + "$"
    val from: String = "alloc_i32_" + cls
    val to: Global   = Global.Top(cls)
    from -> to
  }.toMap
}
