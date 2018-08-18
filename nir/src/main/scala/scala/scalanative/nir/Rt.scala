package scala.scalanative
package nir

import Type._

object Rt {
  val Object = Class(Global.Top("java.lang.Object"))
  val String = Class(Global.Top("java.lang.String"))
  val Type   = Struct(Global.None, Seq(Int, Int, Ptr, Byte))

  val JavaEqualsSig    = "equals_java.lang.Object_bool"
  val JavaHashCodeSig  = "hashCode_i32"
  val ScalaEqualsSig   = "scala$underscore$==_java.lang.Object_bool"
  val ScalaHashCodeSig = "scala$underscore$##_i32"
}
