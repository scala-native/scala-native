package scala.scalanative
package nir

import Type._

object Rt {
  val String    = Class(Global.Type("java.lang.String"))
  val Object    = Class(Global.Type("java.lang.Object"))
  val Type      = Struct(Global.Type("scala.scalanative.runtime.Type"))
  val RefArray  = Class(Global.Type("scala.scalanative.runtime.RefArray"))
  val BoxedUnit = Module(Global.Val("scala.scalanative.runtime.BoxedUnit"))
  val Exc       = AnonStruct(Seq(Ptr, I32))
  val RtName    = Global.Val("scala.scalanative.runtime.package")
  val Rt        = Module(RtName)

  val mainName = Global.Val("main")
  val mainSig  = Function(Seq(I32, Ptr), I32)

  val initName = RtName member "init_i32_ptr_class.ssnr.RefArray"
  val initSig  = Function(Seq(I32, Ptr), RefArray)
  val init     = Val.Global(initName, initSig)

  val throwName = Global.Val("scalanative_throw")
  val throwSig  = Function(Seq(Ptr), Void)

  val beginCatchName = Global.Val("scalanative_begin_catch")
  val beginCatchSig  = Function(Seq(Ptr), Ptr)
  val beginCatch     = Val.Global(beginCatchName, Ptr)

  val endCatchName = Global.Val("scalanative_end_catch")
  val endCatchSig  = Function(Seq(), Void)
  val endCatch     = Val.Global(endCatchName, endCatchSig)

  val allocName = Global.Val("scalanative_alloc")
  val allocSig  = Function(Seq(Ptr, Size), Ptr)
  val alloc     = Val.Global(allocName, allocSig)

  val decls = Seq(
      Defn.Declare(Seq(), throwName, throwSig),
      Defn.Declare(Seq(), endCatchName, endCatchSig),
      Defn.Declare(Seq(), beginCatchName, beginCatchSig),
      Defn.Declare(Seq(), allocName, allocSig)
  )

  def pinned = Seq(
      String.name,
      Object.name,
      Type.name,
      RefArray.name,
      Rt.name,
      BoxedUnit.name,
      init.name
  )
}
