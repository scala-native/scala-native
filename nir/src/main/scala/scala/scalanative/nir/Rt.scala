package scala.scalanative
package nir

import Type._

object Rt {
  val String   = Class(Global.Type("java.lang.String"))
  val Object   = Class(Global.Type("java.lang.Object"))
  val Type     = Struct(Global.Type("scala.scalanative.runtime.Type"))
  val RefArray = Class(Global.Type("scala.scalanative.runtime.RefArray"))
  val Rt       = Module(Global.Val("scala.scalanative.runtime.package"))
  val Unit     = Module(Global.Val("scala.scalanative.runtime.Unit"))
  val Exc      = AnonStruct(Seq(Ptr(I8), I32))

  val mainName = Global.Val("main")
  val mainSig  = Function(Seq(I32, Ptr(Ptr(I8))), I32)

  val initName = Global.Val(
    "scala.scalanative.runtime.package",
    "init_i32_class.scala.scalanative.native.Ptr_class.nrt.Array")
  val initSig = Function(Seq(I32, Ptr(Ptr(I8))), RefArray)
  val init    = Val.Global(initName, initSig)

  val throwName = Global.Val("scalanative_throw")
  val throwSig  = Function(Seq(Ptr(I8)), Void)

  val beginCatchName = Global.Val("scalanative_begin_catch")
  val beginCatchSig  = Function(Seq(Ptr(I8)), Ptr(I8))
  val beginCatch     = Val.Global(beginCatchName, Ptr(beginCatchSig))

  val endCatchName = Global.Val("scalanative_end_catch")
  val endCatchSig  = Function(Seq(), Void)
  val endCatch     = Val.Global(endCatchName, endCatchSig)

  val allocName = Global.Val("scalanative_alloc")
  val allocSig  = Function(Seq(Ptr(I8), Size), Ptr(I8))
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
    Unit.name,
    init.name
  )
}
