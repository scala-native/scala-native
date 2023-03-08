package scala.scalanative
package codegen

import nir._
import scala.collection.mutable

/** Created by lukaskellenberger on 17.12.16.
 */
object GenerateReflectiveProxies {

  private def genReflProxy(defn: Defn.Define): Defn.Define = {
    implicit val fresh: Fresh = Fresh()
    val Global.Member(owner, sig) = defn.name: @unchecked
    val defnType = defn.ty.asInstanceOf[Type.Function]
    implicit val pos: Position = defn.pos

    val proxyArgs = genProxyArgs(defnType)
    val proxyTy = genProxyTy(defnType, proxyArgs)

    val label = genProxyLabel(proxyArgs)
    val unboxInsts = genArgUnboxes(label, defnType.args)
    val method = Inst.Let(Op.Method(label.params.head, sig), Next.None)
    val call = genCall(defnType, method, label.params, unboxInsts)
    val retInsts = genRet(call.name, defnType.ret, proxyTy.ret)

    Defn.Define(
      Attrs.fromSeq(Seq(Attr.Dyn)),
      Global.Member(owner, sig.toProxy),
      proxyTy,
      Seq(
        Seq(label),
        unboxInsts,
        Seq(method, call),
        retInsts
      ).flatten
    )
  }

  private def genProxyArgs(defnTy: Type.Function) =
    defnTy.args.map(argty => Type.box.getOrElse(argty, argty))

  private def genProxyTy(defnTy: Type.Function, args: Seq[Type]) =
    Type.Function(
      args,
      defnTy.ret match {
        case Type.Unit => Rt.BoxedUnit
        case _         => Rt.Object
      }
    )

  private def genProxyLabel(
      args: Seq[Type]
  )(implicit pos: nir.Position, fresh: Fresh) = {
    val argLabels = Val.Local(fresh(), args.head) ::
      args.tail.map(argty => Val.Local(fresh(), argty)).toList

    Inst.Label(fresh(), argLabels)
  }

  private def genArgUnboxes(label: Inst.Label, origArgTypes: Seq[nir.Type])(
      implicit fresh: Fresh
  ) = {
    import label.pos
    label.params
      .zip(origArgTypes)
      .tail
      .map {
        case (local: Val.Local, _: Type.PrimitiveKind)
            if Type.unbox.contains(local.ty) =>
          Inst.Let(Op.Unbox(local.ty, local), Next.None)
        case (local: Val.Local, _) =>
          Inst.Let(Op.Copy(local), Next.None)
      }
  }

  private def genCall(
      defnTy: Type.Function,
      method: Inst.Let,
      params: Seq[Val.Local],
      unboxes: Seq[Inst.Let]
  )(implicit fresh: Fresh) = {
    import method.pos
    val callParams =
      params.head ::
        unboxes
          .zip(params.tail)
          .map {
            case (let, local) =>
              val resTy = let.op.resty match {
                case ty: Type.PrimitiveKind =>
                  Type.unbox.getOrElse(local.ty, local.ty)
                case ty => ty
              }
              Val.Local(let.name, resTy)
          }
          .toList

    Inst.Let(
      Op.Call(defnTy, Val.Local(method.name, Type.Ptr), callParams),
      Next.None
    )
  }

  private def genRetValBox(callName: Local, defnRetTy: Type, proxyRetTy: Type)(
      implicit
      pos: nir.Position,
      fresh: Fresh
  ): Inst.Let =
    Type.box.get(defnRetTy) match {
      case Some(boxTy) =>
        Inst.Let(Op.Box(boxTy, Val.Local(callName, defnRetTy)), Next.None)
      case None =>
        Inst.Let(Op.Copy(Val.Local(callName, defnRetTy)), Next.None)
    }

  private def genRet(callName: Local, defnRetTy: Type, proxyRetTy: Type)(
      implicit
      pos: nir.Position,
      fresh: Fresh
  ): Seq[Inst] = {
    defnRetTy match {
      case Type.Unit =>
        Inst.Ret(Val.Unit) :: Nil
      case _ =>
        val box = genRetValBox(callName, defnRetTy, proxyRetTy)
        val ret = Inst.Ret(Val.Local(box.name, proxyRetTy))
        Seq(box, ret)
    }
  }

  def apply(dynimpls: Seq[Global], defns: Seq[Defn]): Seq[Defn.Define] = {

    // filters methods with same name and args but different return type for each given type
    val toProxy =
      dynimpls
        .foldLeft(Map[(Global, Sig), Global]()) {
          case (acc, g @ Global.Member(owner, sig)) if !sig.isStatic =>
            val proxySig = sig.toProxy
            if (!acc.contains((owner, proxySig))) {
              acc + ((owner, proxySig) -> g)
            } else {
              acc
            }
          case (acc, _) =>
            acc
        }
        .values
        .toSet

    // generates a reflective proxy from the defn
    val result = mutable.UnrolledBuffer.empty[Defn.Define]
    defns.foreach {
      case defn: Defn.Define =>
        if (toProxy.contains(defn.name)) {
          result += genReflProxy(defn)
        }
      case _ =>
        ()
    }
    result.toSeq
  }
}
