package scala.scalanative
package codegen

import scala.collection.mutable

/** Created by lukaskellenberger on 17.12.16.
 */
private[codegen] object GenerateReflectiveProxies {
  implicit val scopeId: nir.ScopeId = nir.ScopeId.TopLevel

  private def genReflProxy(defn: nir.Defn.Define): nir.Defn.Define = {
    implicit val fresh: nir.Fresh = nir.Fresh()
    val nir.Global.Member(owner, sig) = defn.name
    val defnType = defn.ty.asInstanceOf[nir.Type.Function]
    implicit val pos: nir.SourcePosition = defn.pos

    val proxyArgs = genProxyArgs(defnType)
    val proxyTy = genProxyTy(defnType, proxyArgs)

    val label = genProxyLabel(proxyArgs)
    val unboxInsts = genArgUnboxes(label, defnType.args)
    val method =
      nir.Inst.Let(nir.Op.Method(label.params.head, sig), nir.Next.None)
    val call = genCall(defnType, method, label.params, unboxInsts)
    val retInsts = genRet(call.id, defnType.ret, proxyTy.ret)

    nir.Defn.Define(
      nir.Attrs.fromSeq(Seq(nir.Attr.Dyn)),
      nir.Global.Member(owner, sig.toProxy),
      proxyTy,
      Seq(
        Seq(label),
        unboxInsts,
        Seq(method, call),
        retInsts
      ).flatten
    )
  }

  private def genProxyArgs(defnTy: nir.Type.Function) =
    defnTy.args.map(argty => nir.Type.box.getOrElse(argty, argty))

  private def genProxyTy(defnTy: nir.Type.Function, args: Seq[nir.Type]) =
    nir.Type.Function(
      args,
      defnTy.ret match {
        case nir.Type.Unit =>
          nir.Rt.BoxedUnit
        case _ =>
          nir.Rt.Object
      }
    )

  private def genProxyLabel(
      args: Seq[nir.Type]
  )(implicit pos: nir.SourcePosition, fresh: nir.Fresh) = {
    val argLabels = nir.Val.Local(fresh(), args.head) ::
      args.tail.map(argty => nir.Val.Local(fresh(), argty)).toList

    nir.Inst.Label(fresh(), argLabels)
  }

  private def genArgUnboxes(label: nir.Inst.Label, origArgTypes: Seq[nir.Type])(
      implicit fresh: nir.Fresh
  ) = {
    import label.pos
    label.params
      .zip(origArgTypes)
      .tail
      .map {
        case (local: nir.Val.Local, _: nir.Type.PrimitiveKind)
            if nir.Type.unbox.contains(local.ty) =>
          nir.Inst.Let(nir.Op.Unbox(local.ty, local), nir.Next.None)
        case (local: nir.Val.Local, _) =>
          nir.Inst.Let(nir.Op.Copy(local), nir.Next.None)
      }
  }

  private def genCall(
      defnTy: nir.Type.Function,
      method: nir.Inst.Let,
      params: Seq[nir.Val.Local],
      unboxes: Seq[nir.Inst.Let]
  )(implicit fresh: nir.Fresh) = {
    import method.pos
    val callParams =
      params.head ::
        unboxes
          .zip(params.tail)
          .map {
            case (let, local) =>
              val resTy = let.op.resty match {
                case ty: nir.Type.PrimitiveKind =>
                  nir.Type.unbox.getOrElse(local.ty, local.ty)
                case ty => ty
              }
              nir.Val.Local(let.id, resTy)
          }
          .toList

    nir.Inst.Let(
      nir.Op.Call(defnTy, nir.Val.Local(method.id, nir.Type.Ptr), callParams),
      nir.Next.None
    )
  }

  private def genRetValBox(
      callName: nir.Local,
      defnRetTy: nir.Type,
      proxyRetTy: nir.Type
  )(implicit
      pos: nir.SourcePosition,
      fresh: nir.Fresh
  ): nir.Inst.Let =
    nir.Type.box.get(defnRetTy) match {
      case Some(boxTy) =>
        nir.Inst.Let(
          nir.Op.Box(boxTy, nir.Val.Local(callName, defnRetTy)),
          nir.Next.None
        )
      case None =>
        nir.Inst.Let(
          nir.Op.Copy(nir.Val.Local(callName, defnRetTy)),
          nir.Next.None
        )
    }

  private def genRet(
      callName: nir.Local,
      defnRetTy: nir.Type,
      proxyRetTy: nir.Type
  )(implicit
      pos: nir.SourcePosition,
      fresh: nir.Fresh
  ): Seq[nir.Inst] = {
    defnRetTy match {
      case nir.Type.Unit =>
        nir.Inst.Ret(nir.Val.Unit) :: Nil
      case _ =>
        val box = genRetValBox(callName, defnRetTy, proxyRetTy)
        val ret = nir.Inst.Ret(nir.Val.Local(box.id, proxyRetTy))
        Seq(box, ret)
    }
  }

  def apply(
      dynimpls: Seq[nir.Global],
      defns: Seq[nir.Defn]
  ): Seq[nir.Defn.Define] = {

    // filters methods with same name and args but different return type for each given type
    val toProxy =
      dynimpls
        .foldLeft(Map[(nir.Global, nir.Sig), nir.Global]()) {
          case (acc, g @ nir.Global.Member(owner, sig)) if !sig.isStatic =>
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
    val result = mutable.UnrolledBuffer.empty[nir.Defn.Define]
    defns.foreach {
      case defn: nir.Defn.Define =>
        if (toProxy.contains(defn.name)) {
          result += genReflProxy(defn)
        }
      case _ =>
        ()
    }
    result.toSeq
  }
}
