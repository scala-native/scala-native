package scala.scalanative
package nir

import Type._

object Rt {
  val Object = Class(Global.Top("java.lang.Object"))
  val String = Class(Global.Top("java.lang.String"))
  val Exc    = Struct(Global.None, Seq(Ptr, I32))
  val Type = Struct(
      Global.Top("scala.scalanative.runtime.Type"), Seq(I32, Ptr))

  val beginCatchName = Global.Top("scalanative_begin_catch")
  val beginCatchSig  = Function(Seq(Ptr), Ptr)
  val beginCatch     = Val.Global(beginCatchName, Ptr)

  val endCatchName = Global.Top("scalanative_end_catch")
  val endCatchSig  = Function(Seq(), Void)
  val endCatch     = Val.Global(endCatchName, endCatchSig)
}
