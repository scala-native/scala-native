package scala.scalanative
package nir

import Type._

object Rt {
  val RtName    = Global.Val("scala.scalanative.runtime.package")
  val Rt        = Module(RtName)
  val BoxedUnit = Module(Global.Val("scala.runtime.BoxedUnit"))
  val String    = Class(Global.Type("java.lang.String"))
  val Object    = Class(Global.Type("java.lang.Object"))
  val RefArray  = Class(Global.Type("scala.scalanative.runtime.RefArray"))
  val Type      = Struct(Global.Type("scala.scalanative.runtime.Type"), Seq(I32, Ptr))
  val Exc       = Struct(Global.Type("scala.scalanative.runtime.Exc"), Seq(Ptr, I32))

  val mainName = Global.Val("main")
  val mainSig  = Function(Seq(I32, Ptr), I32)

  val initName = RtName member "init_i32_ptr_class.ssnr.RefArray"
  val initSig  = Function(Seq(I32, Ptr), RefArray)
  val init     = Val.Global(initName, initSig)

  val throwName = Global.Val("scalanative_throw")
  val throwSig  = Function(Seq(Ptr), Void)
  val throw_    = Val.Global(throwName, Ptr)
  val throwDefn = Defn.Declare(Seq(), throwName, throwSig)

  val beginCatchName = Global.Val("scalanative_begin_catch")
  val beginCatchSig  = Function(Seq(Ptr), Ptr)
  val beginCatch     = Val.Global(beginCatchName, Ptr)
  val beginCatchDefn =
      Defn.Declare(Seq(), beginCatchName, beginCatchSig)

  val endCatchName = Global.Val("scalanative_end_catch")
  val endCatchSig  = Function(Seq(), Void)
  val endCatch     = Val.Global(endCatchName, endCatchSig)
  val endCatchDefn = Defn.Declare(Seq(), endCatchName, endCatchSig)

  val allocName = Global.Val("scalanative_alloc")
  val allocSig  = Function(Seq(Ptr, Size), Ptr)
  val alloc     = Val.Global(allocName, allocSig)
  val allocDefn = Defn.Declare(Seq(), allocName, allocSig)

  val unitName   = Global.Val("scalanative_unit")
  val unit       = Val.Global(unitName, Ptr)
  val unitTy     = Struct(BoxedUnit.name tag "class", Seq(Ptr))
  val unitConst  = Val.Global(BoxedUnit.name tag "const", Ptr)
  val unitValue  = Val.Struct(unitTy.name, Seq(unitConst))
  val unitDefn   = Defn.Const(Seq(), unitName, unitTy, unitValue)

  val defns = Seq(
      throwDefn,
      beginCatchDefn,
      endCatchDefn,
      allocDefn,
      unitDefn
  )

  def pinned = Seq(
      Rt.name,
      init.name,
      BoxedUnit.name,
      String.name,
      Object.name,
      RefArray.name,
      Type.name,
      Exc.name
  )
}
