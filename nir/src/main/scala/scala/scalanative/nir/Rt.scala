package scala.scalanative
package nir

object Rt {
  val String = nir.Type.Class(Global.Type("java.lang.String"))
  val Object = nir.Type.Class(Global.Type("java.lang.Object"))
  val Type   = nir.Type.Struct(Global.Type("scala.scalanative.runtime.Type"))
}
