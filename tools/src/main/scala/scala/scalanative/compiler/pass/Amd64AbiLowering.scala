package scala.scalanative
package compiler
package pass

import scala.scalanative.nir._

/**
 * Created by Kamil on 09.07.2016.
 */
class Amd64AbiLowering(implicit fresh: Fresh) extends Pass {

  def coerceArguments(args: Seq[Type]): Seq[Type] = args flatMap coerceArgument

  def size(t: Type): Int = t match {
    case Type.Bool => 1
    case Type.I(w) => w / 8
    case Type.F(w) => w / 8
    case Type.Ptr  => 8
    case Type.Array(e, n) => size(e) * n
    case Type.Struct(_, e) => e.map(size).sum
  }

  def coerceArgument(arg: Type): Seq[Type] = arg match {
    case struct @ Type.Struct(name, tys) =>

      size(struct) match {
        case s if s <= 8 => // One parameter
          Seq(Type.I(s * 8))
        case s if s <= 16 => // Two parameters
          Seq(Type.I64, Type.I((s - 8) * 8))
        case _ => // Pointer
          Seq(Type.Ptr)
      }
    // TODO: Once we get vector types their lowering should be handled here too
    case x => Seq(x)
  }

  def coerceReturnType(ret: Type): Type = coerceArgument(ret) match {
    case Seq(t) => t
    case s => Type.Struct(Global.None, s)
  }

  def  coerceFunctionType(t: Type): Type = {
    val Type.Function(argtys, retty) = t
    val ret = coerceReturnType(retty)
    val args = coerceArguments(argtys)
    if(ret != retty && ret == Type.Ptr)
      Type.Function(Type.Ptr +: args, Type.Void)
    else
      Type.Function(args, ret)
  }

  private var smallParamAlloc: Val.Local = _
  private var returnPointerParam: Val.Local = _
  private var returnCoercionType: Type = _
  private val SmallParamStructType = Type.Struct(Global.None, Seq(Type.I64, Type.I64))

  private var bigParamAllocs: Seq[Val.Local] = _
  private var bigParamAllocType: Type = _

  private var bigReturnAlloc: Val.Local = _
  private var bigReturnType: Type = _

  override def preDefn: OnDefn = {
    case defn @ Defn.Define(attrs, name, Type.Function(argtys, retty), blocks) =>
      smallParamAlloc = Val.Local(fresh(), Type.Ptr)
      returnCoercionType = coerceReturnType(retty)
      returnPointerParam = returnCoercionType match {
        case Type.Ptr if retty != Type.Ptr =>
          Val.Local(fresh(), Type.Ptr)
        case _ => null
      }
      val allocs = for {
        block <- blocks
        Inst(_, Op.Call(ty: Type.Function, _, _)) <- block.insts
      } yield for {
          argty <- ty.args
          coerced = coerceArgument(argty)
          if coerced == Seq(Type.Ptr) && argty != Type.Ptr
        } yield argty
      val maxArgs = (0 +: allocs.map(_.size)).max
      val maxArgSize = (0 +: allocs.flatten.map(size)).max
      bigParamAllocType = Type.Array(Type.I8, maxArgSize)
      bigParamAllocs = Seq.fill(maxArgs)(Val.Local(fresh(), Type.Ptr))

      val rets = for {
        block <- blocks
        Inst(_, Op.Call(Type.Function(_, retty), _, _)) <- block.insts
        coerced = coerceReturnType(retty)
        if coerced == Type.Ptr && retty != Type.Ptr
      } yield retty
      val maxRetSize = (0 +: rets.map(size)).max
      bigReturnType = Type.Array(Type.I8, maxRetSize)
      bigReturnAlloc = Val.Local(fresh(), Type.Ptr)

      Seq(defn)
  }

  override def postDefn: OnDefn = {
    case Defn.Declare(attrs, name, ty: Type.Function) =>
      Seq(Defn.Declare(attrs, name, coerceFunctionType(ty)))

    case Defn.Define(attrs, name, ty: Type.Function, entryBlock +: otherBlocks) =>
      val newEntryBlock = entryBlock match {
        case Block(blockName, params, insts, cf) =>
          val allocSmallParam = Inst(smallParamAlloc.name, Op.Stackalloc(SmallParamStructType))

          val allocBigRet = Inst(bigReturnAlloc.name, Op.Stackalloc(bigReturnType))

          val allocBigParams = bigParamAllocs.map { case Val.Local(n, _) => Inst(n, Op.Stackalloc(bigParamAllocType)) }

          val argCoercions: Seq[(Seq[Val.Local], Seq[Inst])] =
            for(param <- params) yield coerceArgument(param.valty) match {
              case Seq(ty) if ty == param.ty =>
                (Seq(param), Seq())
              case Seq(Type.Ptr) =>
                val ptr = Val.Local(fresh(), Type.Ptr)
                (Seq(ptr), Seq(
                  Inst(param.name, Op.Load(param.ty, ptr))
                ))
              case tys =>
                val newArgs = tys.map(ty => Val.Local(fresh(), ty))
                val coerceStructType = Type.Struct(Global.None, tys)
                val storeInsts = newArgs.zipWithIndex.flatMap { case (v, i) =>
                  val ptr = Val.Local(fresh(), Type.Ptr)
                  Seq(
                    Inst(ptr.name, Op.Elem(coerceStructType, smallParamAlloc, Seq(Val.I32(0), Val.I32(i)))),
                    Inst(Op.Store(v.ty, ptr, v))
                  )
                }
                val loadInst = Inst(param.name, Op.Load(param.ty, smallParamAlloc))
                (newArgs, storeInsts :+ loadInst)
            }

          val additionalParams: Seq[Val.Local] = returnCoercionType match {
            case Type.Ptr if ty.ret != Type.Ptr =>
              Seq(returnPointerParam)
            case _ => Seq()
          }

          Block(blockName, additionalParams ++ argCoercions.flatMap(_._1),
            Seq(allocSmallParam, allocBigRet) ++ allocBigParams ++ argCoercions.flatMap(_._2) ++ insts,
            cf)
      }
      val defn = Defn.Define(attrs, name, coerceFunctionType(ty), newEntryBlock +: otherBlocks)
      Seq(defn)
  }

  override def postInst: OnInst = {
    case Inst(res, Op.Call(functy: Type.Function, ptr, args)) =>
      var bigParamIndex = 0
      val argCoertions: Seq[(Seq[Val], Seq[Inst])] =
        for(arg <- args) yield coerceArgument(arg.ty) match {
          case Seq(ty) if ty == arg.ty =>
            (Seq(arg), Seq())
          case Seq(Type.Ptr) =>
            val alloc = bigParamAllocs(bigParamIndex)
            bigParamIndex += 1
            (Seq(alloc), Seq(
              Inst(Op.Store(arg.ty, alloc, arg))
            ))
          case coerced =>
            val newArgs = coerced map (t => Val.Local(fresh(), t))
            val coerceType = Type.Struct(Global.None, coerced)
            val coercedStruct = Val.Local(fresh(), coerceType)

            val prepareInst = Inst(Op.Store(arg.ty, smallParamAlloc, arg))
            val parameterInsertions = newArgs.zipWithIndex.flatMap {
              case (v, i) =>
                val ptr = Val.Local(fresh(), Type.Ptr)
                Seq(
                  Inst(ptr.name, Op.Elem(coerceType, smallParamAlloc, Seq(Val.I32(0), Val.I32(i)))),
                  Inst(v.name, Op.Load(v.ty, ptr))
                )
            }
            (newArgs, prepareInst +: parameterInsertions)
        }

      val coercedRetType = coerceReturnType(functy.ret)
      val retCoercions: (Option[Local], Seq[Inst], Seq[Inst], Seq[Val]) = coercedRetType match {
        case Type.Void | Type.Unit | Type.Nothing =>
          (None, Seq(), Seq(), Seq())
        case x if x == functy.ret =>
          (Some(res), Seq(), Seq(), Seq())
        case Type.Ptr =>
          val post = Inst(res, Op.Load(functy.ret, bigReturnAlloc))
          (None, Seq(), Seq(post), Seq(bigReturnAlloc))
        case ty =>
          val ret = Val.Local(fresh(), ty)
          val post = Seq(
            Inst(Op.Store(ty, smallParamAlloc, ret)),
            Inst(res, Op.Load(functy.ret, smallParamAlloc))
          )
          (Some(ret.name), Seq(), post, Seq())
      }
      val (retn: Option[Local], preRetInsts: Seq[Inst], postRetInsts: Seq[Inst], additionalArgs: Seq[Val]) = retCoercions

      val call = Inst(retn getOrElse fresh(), Op.Call(coerceFunctionType(functy), ptr, additionalArgs ++ argCoertions.flatMap(_._1)))
      argCoertions.flatMap(_._2) ++ preRetInsts ++ Seq(call) ++ postRetInsts
  }


  override def postBlock: OnBlock = {
    case block @ Block(name, params, insts, Cf.Ret(v)) =>
      returnCoercionType match {
        case Type.Ptr if v.ty != Type.Ptr =>
          val storeInst = Inst(Op.Store(v.ty, returnPointerParam, v))
          Seq(Block(name, params, insts :+ storeInst, Cf.Ret(Val.None)))
        case s @ Type.Struct(_, tys) =>
          val ret = Val.Local(fresh(), s)
          val retInsts = Seq(
            Inst(Op.Store(v.ty, smallParamAlloc, v)),
            Inst(ret.name, Op.Load(s, smallParamAlloc))
          )
          Seq(Block(name, params, insts ++ retInsts, Cf.Ret(ret)))
        case _ => Seq(block)
      }
  }
}

object Amd64AbiLowering extends PassCompanion {

  override def apply(ctx: Ctx): Pass = new Amd64AbiLowering()(ctx.fresh)

}
