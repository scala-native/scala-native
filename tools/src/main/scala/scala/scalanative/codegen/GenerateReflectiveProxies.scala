package scala.scalanative
package codegen

import nir._
import scala.collection.mutable

/**
 * Created by lukaskellenberger on 17.12.16.
 */
object GenerateReflectiveProxies {
  implicit val fresh = Fresh()

  private def genReflProxy(defn: Defn.Define): Defn.Define = {
    val Global.Member(owner, sig) = defn.name
    val defnType                  = defn.ty.asInstanceOf[Type.Function]

    val proxyArgs = genProxyArgs(defnType)
    val proxyTy   = genProxyTy(defnType, proxyArgs)

    val label      = genProxyLabel(proxyArgs)
    val unboxInsts = genArgUnboxes(label)
    val method     = Inst.Let(Op.Method(label.params.head, sig), Next.None)
    val call       = genCall(defnType, method, label.params, unboxInsts)
    val box        = genRetValBox(call.name, defnType.ret, proxyTy.ret)
    val retInst    = genRet(box.name, proxyTy.ret)

    Defn.Define(
      Attrs.fromSeq(Seq(Attr.Dyn)),
      Global.Member(owner, sig.toProxy),
      proxyTy,
      Seq(
        Seq(label),
        unboxInsts,
        Seq(method, call, box, retInst)
      ).flatten
    )
  }

  private def genProxyArgs(defnTy: Type.Function) =
    defnTy.args.map(argty => Type.box.getOrElse(argty, argty))

  private def genProxyTy(defnTy: Type.Function, args: Seq[Type]) =
    Type.Function(args, defnTy.ret match {
      case Type.Unit => Type.Unit
      case _         => Type.Ref(Global.Top("java.lang.Object"))
    })

  private def genProxyLabel(args: Seq[Type]) = {
    val argLabels = Val.Local(fresh(), args.head) ::
      args.tail.map(argty => Val.Local(fresh(), argty)).toList

    Inst.Label(fresh(), argLabels)
  }

  private def genArgUnboxes(label: Inst.Label) =
    label.params.tail.map {
      case local: Val.Local if Type.unbox.contains(local.ty) =>
        Inst.Let(Op.Unbox(local.ty, local), Next.None)
      case local: Val.Local =>
        Inst.Let(Op.Copy(local), Next.None)
    }

  private def genCall(defnTy: Type.Function,
                      method: Inst.Let,
                      params: Seq[Val.Local],
                      unboxes: Seq[Inst.Let]) = {
    val callParams =
      params.head ::
        unboxes
        .zip(params.tail)
        .map {
          case (let, local) =>
            Val.Local(let.name, Type.unbox.getOrElse(local.ty, local.ty))
        }
        .toList

    Inst.Let(Op.Call(defnTy, Val.Local(method.name, Type.Ptr), callParams),
             Next.None)
  }

  private def genRetValBox(callName: Local, defnRetTy: Type, proxyRetTy: Type) =
    Type.box.get(defnRetTy) match {
      case Some(boxTy) =>
        Inst.Let(Op.Box(boxTy, Val.Local(callName, defnRetTy)), Next.None)
      case None =>
        Inst.Let(Op.Copy(Val.Local(callName, defnRetTy)), Next.None)
    }

  private def genRet(retValBoxName: Local, proxyRetTy: Type) =
    proxyRetTy match {
      case Type.Unit => Inst.Ret(Val.Unit)
      case _         => Inst.Ret(Val.Local(retValBoxName, proxyRetTy))
    }

  def apply(dynimpls: Seq[Global], defns: Seq[Defn]): Seq[Defn.Define] = {

    // filters methods with same name and args but different return type for each given type
    val toProxy =
      dynimpls
        .foldLeft(Map[(Global, Sig), Global]()) {
          case (acc, g @ Global.Member(owner, sig)) =>
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
    result
  }
}
