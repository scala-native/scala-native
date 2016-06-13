package scala.scalanative
package compiler

import scala.collection.mutable
import nir._

trait Pass extends (Seq[Defn] => Seq[Defn]) {
  type OnAssembly = PartialFunction[Seq[Defn], Seq[Defn]]
  type OnDefn     = PartialFunction[Defn, Seq[Defn]]
  type OnBlock    = PartialFunction[Block, Seq[Block]]
  type OnInst     = PartialFunction[Inst, Seq[Inst]]
  type OnCf       = PartialFunction[Cf, Cf]
  type OnNext     = PartialFunction[Next, Next]
  type OnVal      = PartialFunction[Val, Val]
  type OnType     = PartialFunction[Type, Type]

  def preAssembly: OnAssembly  = null
  def postAssembly: OnAssembly = null
  def preDefn: OnDefn          = null
  def postDefn: OnDefn         = null
  def preBlock: OnBlock        = null
  def postBlock: OnBlock       = null
  def preInst: OnInst          = null
  def postInst: OnInst         = null
  def preCf: OnCf              = null
  def postCf: OnCf             = null
  def preNext: OnNext          = null
  def postNext: OnNext         = null
  def preVal: OnVal            = null
  def postVal: OnVal           = null
  def preType: OnType          = null
  def postType: OnType         = null

  @inline private def hook[A, B](
      pf: PartialFunction[A, B], arg: A, default: B): B =
    if (pf == null) default else pf.applyOrElse(arg, (_: A) => default)

  private def txAssembly(assembly: Seq[Defn]): Seq[Defn] = {
    val pre = hook(preAssembly, assembly, assembly)

    val post = pre.flatMap { defn =>
      txDefn(defn)
    }

    hook(postAssembly, post, post)
  }

  private def txDefn(defn: Defn): Seq[Defn] = {
    val pres = hook(preDefn, defn, Seq(defn))

    pres.flatMap { pre =>
      val post = pre match {
        case defn @ Defn.Var(_, _, ty, value) =>
          defn.copy(ty = txType(ty), value = txVal(value))
        case defn @ Defn.Const(_, _, ty, value) =>
          defn.copy(ty = txType(ty), value = txVal(value))
        case defn @ Defn.Declare(_, _, ty) =>
          defn.copy(ty = txType(ty))
        case defn @ Defn.Define(_, _, ty, blocks) =>
          defn.copy(ty = txType(ty), blocks = blocks.flatMap(txBlock))
        case defn @ Defn.Struct(_, _, tys) =>
          defn.copy(tys = tys.map(txType))
        case defn @ Defn.Trait(_, _, _) =>
          defn
        case defn @ Defn.Class(_, _, _, _) =>
          defn
        case defn @ Defn.Module(_, _, _, _) =>
          defn
      }

      hook(postDefn, post, Seq(post))
    }
  }

  private def txBlock(block: Block): Seq[Block] = {
    val pres = hook(preBlock, block, Seq(block))

    pres.flatMap { pre =>
      val newparams = pre.params.map { param =>
        Val.Local(param.name, txType(param.ty))
      }
      val newinsts = pre.insts.flatMap(txInst)
      val newcf    = txCf(pre.cf)
      val post     = Block(pre.name, newparams, newinsts, newcf)

      hook(postBlock, post, Seq(post))
    }
  }

  private def txInst(inst: Inst): Seq[Inst] = {
    val pres = hook(preInst, inst, Seq(inst))

    pres.flatMap { pre =>
      val newop = pre.op match {
        case Op.Call(ty, ptrv, argvs) =>
          Op.Call(txType(ty), txVal(ptrv), argvs.map(txVal))
        case Op.Load(ty, ptrv) => Op.Load(txType(ty), txVal(ptrv))
        case Op.Store(ty, ptrv, v) =>
          Op.Store(txType(ty), txVal(ptrv), txVal(v))
        case Op.Elem(ty, ptrv, indexvs) =>
          Op.Elem(txType(ty), txVal(ptrv), indexvs.map(txVal))
        case Op.Extract(aggrv, indexvs) => Op.Extract(txVal(aggrv), indexvs)
        case Op.Insert(aggrv, v, indexvs) =>
          Op.Insert(txVal(aggrv), txVal(v), indexvs)
        case Op.Stackalloc(ty) => Op.Stackalloc(txType(ty))
        case Op.Bin(bin, ty, lv, rv) =>
          Op.Bin(bin, txType(ty), txVal(lv), txVal(rv))
        case Op.Comp(comp, ty, lv, rv) =>
          Op.Comp(comp, txType(ty), txVal(lv), txVal(rv))
        case Op.Conv(conv, ty, v) => Op.Conv(conv, txType(ty), txVal(v))

        case Op.Classalloc(n)    => Op.Classalloc(n)
        case Op.Field(ty, v, n)  => Op.Field(txType(ty), txVal(v), n)
        case Op.Method(ty, v, n) => Op.Method(txType(ty), txVal(v), n)
        case Op.Module(n)        => Op.Module(n)
        case Op.As(ty, v)        => Op.As(txType(ty), txVal(v))
        case Op.Is(ty, v)        => Op.Is(txType(ty), txVal(v))
        case Op.Copy(v)          => Op.Copy(txVal(v))
        case Op.Sizeof(ty)       => Op.Sizeof(txType(ty))
        case Op.Closure(ty, fun, captures) =>
          Op.Closure(txType(ty), txVal(fun), captures.map(txVal))
      }
      val post = Inst(pre.name, newop)

      hook(postInst, post, Seq(post))
    }
  }

  private def txCf(cf: Cf): Cf = {
    val pre = hook(preCf, cf, cf)
    val post = pre match {
      case Cf.Unreachable => Cf.Unreachable
      case Cf.Ret(v)      => Cf.Ret(txVal(v))
      case Cf.Jump(next)  => Cf.Jump(txNext(next))
      case Cf.If(v, thenp, elsep) =>
        Cf.If(txVal(v), txNext(thenp), txNext(elsep))
      case Cf.Switch(v, default, cases) =>
        Cf.Switch(txVal(v), txNext(default), cases.map(txNext))
      case Cf.Invoke(ty, ptrv, argvs, succ, fail) =>
        Cf.Invoke(txType(ty),
                  txVal(ptrv),
                  argvs.map(txVal),
                  txNext(succ),
                  txNext(fail))
      case Cf.Resume(excrec) => Cf.Resume(txVal(excrec))

      case Cf.Throw(v)       => Cf.Throw(txVal(v))
      case Cf.Try(norm, exc) => Cf.Try(txNext(norm), txNext(exc))
    }

    hook(postCf, post, post)
  }

  private def txVal(value: Val): Val = {
    val pre = hook(preVal, value, value)
    val post = pre match {
      case Val.Zero(ty)          => Val.Zero(txType(ty))
      case Val.Undef(ty)         => Val.Undef(txType(ty))
      case Val.Struct(n, values) => Val.Struct(n, values.map(txVal))
      case Val.Array(ty, values) => Val.Array(txType(ty), values.map(txVal))
      case Val.Local(n, ty)      => Val.Local(n, txType(ty))
      case Val.Global(n, ty)     => Val.Global(n, txType(ty))
      case Val.Const(v)          => Val.Const(txVal(v))
      case _                     => pre
    }

    hook(postVal, post, post)
  }

  private def txType(ty: Type): Type = {
    val pre = hook(preType, ty, ty)
    val post = pre match {
      case Type.Array(ty, n)      => Type.Array(txType(ty), n)
      case Type.Function(tys, ty) => Type.Function(tys.map(txType), txType(ty))
      case Type.Struct(n, tys)    => Type.Struct(n, tys.map(txType))
      case _                      => pre
    }

    hook(postType, post, post)
  }

  private def txNext(next: Next): Next = {
    val pre = hook(preNext, next, next)
    val post = pre match {
      case succ: Next.Succ     => succ
      case fail: Next.Fail     => fail
      case Next.Label(n, args) => Next.Label(n, args.map(txVal))
      case Next.Case(v, n)     => Next.Case(txVal(v), n)
    }

    hook(postNext, post, post)
  }

  final def apply(assembly: Seq[Defn]): Seq[Defn] = txAssembly(assembly)
  final def apply(defn: Defn): Seq[Defn]          = txDefn(defn)
  final def apply(block: Block): Seq[Block]       = txBlock(block)
  final def apply(inst: Inst): Seq[Inst]          = txInst(inst)
  final def apply(cf: Cf): Cf                     = txCf(cf)
  final def apply(next: Next): Next               = txNext(next)
  final def apply(value: Val): Val                = txVal(value)
  final def apply(ty: Type): Type                 = txType(ty)
}

trait PassCompanion {
  def apply(ctx: Ctx): Pass
  def depends: Seq[Global] = Seq()
  def injects: Seq[Defn]   = Seq()
}
