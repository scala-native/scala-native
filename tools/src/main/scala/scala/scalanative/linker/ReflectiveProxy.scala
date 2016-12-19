package scala.scalanative
package linker

import nir._
import scala.collection.mutable

/**
 * Created by lukaskellenberger on 17.12.16.
 */
object ReflectiveProxy {
  implicit val fresh = new Fresh("proxy")

  private def genReflProxy(defn: Defn.Define): Defn.Define = {
    val Global.Member(owner, id) = defn.name
    val defnType                 = defn.ty.asInstanceOf[Type.Function]

    val proxyArgs = genProxyArgs(defnType)
    val proxyTy   = genProxyTy(defnType, proxyArgs)

    val label      = genProxyLabel(proxyArgs)
    val unboxInsts = genArgUnboxes(label)
    val method     = Inst.Let(Op.Method(label.params.head, defn.name))
    val call       = genCall(defnType, method, label.params, unboxInsts)
    val box        = genRetValBox(call.name, defnType.ret, proxyTy.ret)
    val retInst    = genRet(box.name, proxyTy.ret)

    Defn.Define(
      Attrs.fromSeq(Seq(Attr.Dyn)),
      Global.Member(owner, Global.genSignature(defn.name, proxy = true)),
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
      case _         => Type.Class(Global.Top("java.lang.Object"))
    })

  private def genProxyLabel(args: Seq[Type]) = {
    val argLabels = Val.Local(fresh(), args.head) ::
        args.tail.map(argty => Val.Local(fresh(), argty)).toList

    Inst.Label(fresh(), argLabels)
  }

  private def genArgUnboxes(label: Inst.Label) =
    label.params.tail.map {
      case local: Val.Local if Type.unbox.contains(local.ty) =>
        Inst.Let(Op.Unbox(local.ty, local))
      case local: Val.Local =>
        Inst.Let(Op.Copy(local))
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

    Inst.Let(
      Op.Call(defnTy, Val.Local(method.name, Type.Ptr), callParams, Next.None))
  }

  private def genRetValBox(callName: Local,
                           defnRetTy: Type,
                           proxyRetTy: Type) =
    Type.box.get(defnRetTy) match {
      case Some(boxTy) =>
        Inst.Let(Op.Box(boxTy, Val.Local(callName, defnRetTy)))
      case None =>
        Inst.Let(Op.As(proxyRetTy, Val.Local(callName, defnRetTy)))
    }

  private def genRet(retValBoxName: Local, proxyRetTy: Type) =
    proxyRetTy match {
      case Type.Unit => Inst.Ret(Val.Unit)
      case _         => Inst.Ret(Val.Local(retValBoxName, proxyRetTy))
    }

  def genAllReflectiveProxies(
      dyndefns: mutable.Set[Global],
      defns: mutable.UnrolledBuffer[Defn]): Seq[Defn.Define] = {

    // filters methods with same name and args but different return type for each given type
    val toProxy =
      dyndefns
        .foldLeft(Map[(Global, String), Global]()) {
          case (acc, g @ Global.Member(owner, _)) =>
            val sign = Global.genSignature(g)
            if (!acc.contains((owner, sign))) {
              acc + ((owner, sign) -> g)
            } else {
              acc
            }
          case (acc, _) =>
            acc
        }
        .values

    // generates a reflective proxy from the defn
    toProxy.flatMap { g =>
      defns.collectFirst {
        case defn: Defn.Define if defn.name == g => genReflProxy(defn)
      }
    }.toSeq
  }
}
