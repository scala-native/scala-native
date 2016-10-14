package scala.scalanative
package optimizer
package pass

import analysis.ClassHierarchy._
import analysis.ClassHierarchyExtractors._
import util.{sh, unsupported}
import nir._, Shows._, Inst.Let

class MethodCallProfiling(implicit top: Top, fresh: Fresh) extends Pass {
  import MethodCallProfiling._

  override def preInst = {
    case inst @ Let(n, Op.Method(obj, MethodRef(cls: Class, meth)))
        if meth.isVirtual =>
      val tpe        = Val.Local(fresh(), cls.typeStruct)
      val typeptr    = Val.Local(fresh(), Type.Ptr)
      val typeid     = Val.Local(fresh(), Type.I32)
      val methptrptr = Val.Local(fresh(), Type.Ptr)

      val instname = s"${n.scope}.${n.id}"

      Seq(
          Let(typeptr.name, Op.Load(Type.Ptr, obj)),
          Let(tpe.name, Op.Load(cls.typeStruct, typeptr)),
          Let(typeid.name, Op.Extract(tpe, Seq(0))),
          Let(Op.Call(callCountSig, callCount, Seq())),
          Let(
              Op.Call(profileMethodSig,
                      profileMethod,
                      Seq(typeid, Val.String(s"$instname:${meth.name.id}")))),
          inst
      )
  }
}

object MethodCallProfiling extends PassCompanion {
  override def apply(config: tools.Config, top: Top) =
    if (config.profileDispatch)
      new MethodCallProfiling()(top, top.fresh)
    else
      EmptyPass

  val callCountName = Global.Top("method_call_count")
  val callCountSig  = Type.Function(Seq(), Type.Void)
  val callCount     = Val.Global(callCountName, callCountSig)

  val profileMethodName = Global.Top("method_call_log")
  val profileMethodSig  = Type.Function(Seq(Arg(Type.I32), Arg(Rt.String)), Type.Void)
  val profileMethod     = Val.Global(profileMethodName, profileMethodSig)

  override val injects = Seq(
      Defn.Declare(Attrs.None, profileMethodName, profileMethodSig),
      Defn.Declare(Attrs.None, callCountName, callCountSig))
}
