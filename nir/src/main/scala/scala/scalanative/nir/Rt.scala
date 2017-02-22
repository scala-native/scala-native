package scala.scalanative
package nir

import Type._

object Rt {
  val Object = Class(Global.Top("java.lang.Object"))
  val String = Class(Global.Top("java.lang.String"))
  val Type =
    Struct(Global.Top("scala.scalanative.runtime.Type"), Seq(Int, Ptr, Long))
}
