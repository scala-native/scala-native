package salty.ir

import java.nio.ByteBuffer
import salty.ir.Tags.Tag

object Deserializers {
  type BlockEnv = Map[Name, Block]

  implicit class RichGet(val bb: ByteBuffer) extends AnyVal {
    import bb._

    def getTag: Tag = getInt

    def getTypeSeq: Seq[Type] = getSeq(getType)
    def getType: Type = getType(getTag)
    def getType(tag: Tag): Type = tag match {
      case Tags.Type.Unit    => Type.Unit
      case Tags.Type.Null    => Type.Null
      case Tags.Type.Nothing => Type.Nothing
      case Tags.Type.Bool    => Type.Bool
      case Tags.Type.I8      => Type.I8
      case Tags.Type.I16     => Type.I16
      case Tags.Type.I32     => Type.I32
      case Tags.Type.I64     => Type.I64
      case Tags.Type.F32     => Type.F32
      case Tags.Type.F64     => Type.F64
      case Tags.Type.Ref     => Type.Ref(getType)
      case Tags.Type.Slice   => Type.Slice(getType)
      case Tags.Type.Named   => Type.Named(getName.asInstanceOf[Name.Global])
    }

    def getInstrSeq(implicit env: BlockEnv): Seq[Instr] = getSeq(getInstr)
    def getInstr(implicit env: BlockEnv): Instr = getInstr(getTag)
    def getInstr(tag: Tag)(implicit env: BlockEnv): Instr = tag match {
      case Tags.Expr()       => getExpr(tag)
      case Tags.Instr.Assign => Instr.Assign(getName, getExpr)
    }

    def getTermn(implicit env: BlockEnv): Termn = getTermn(getTag)
    def getTermn(tag: Tag)(implicit env: BlockEnv): Termn = tag match {
      case Tags.Termn.Undefined => Termn.Undefined
      case Tags.Termn.Out       => Termn.Out(getVal)
      case Tags.Termn.Return    => Termn.Return(getVal)
      case Tags.Termn.Throw     => Termn.Throw(getVal)
      case Tags.Termn.Jump      => Termn.Jump(getBlock)
      case Tags.Termn.If        => Termn.If(getVal, getBlock, getBlock)
      case Tags.Termn.Switch    => Termn.Switch(getVal, getBlock, getBranchSeq)
      case Tags.Termn.Try       => Termn.Try(getBlock, getBlockOpt, getBlockOpt)
    }

    def getExpr(implicit env: BlockEnv): Expr = getExpr(getTag)
    def getExpr(tag: Tag)(implicit env: BlockEnv): Expr = tag match {
      case Tags.Val()         => getVal(tag)
      case Tags.Expr.Bin      => Expr.Bin(getBinOp, getVal, getVal)
      case Tags.Expr.Conv     => Expr.Conv(getConvOp, getVal, getType)
      case Tags.Expr.Is       => Expr.Is(getVal, getType)
      case Tags.Expr.Alloc    => Expr.Alloc(getType, getValOpt)
      case Tags.Expr.Call     => Expr.Call(getName, getValSeq)
      case Tags.Expr.Phi      => Expr.Phi(getBranchSeq)
      case Tags.Expr.Load     => Expr.Load(getVal)
      case Tags.Expr.Store    => Expr.Store(getVal, getVal)
      case Tags.Expr.Box      => Expr.Box(getVal, getType)
      case Tags.Expr.Unbox    => Expr.Unbox(getVal, getType)
      case Tags.Expr.Length   => Expr.Length(getVal)
      case Tags.Expr.Catchpad => Expr.Catchpad
    }

    def getBinOp: BinOp = getTag match {
      case Tags.BinOp.Add    => BinOp.Add
      case Tags.BinOp.Sub    => BinOp.Sub
      case Tags.BinOp.Mul    => BinOp.Mul
      case Tags.BinOp.Div    => BinOp.Div
      case Tags.BinOp.Mod    => BinOp.Mod
      case Tags.BinOp.Shl    => BinOp.Shl
      case Tags.BinOp.Lshr   => BinOp.Lshr
      case Tags.BinOp.Ashr   => BinOp.Ashr
      case Tags.BinOp.And    => BinOp.And
      case Tags.BinOp.Or     => BinOp.Or
      case Tags.BinOp.Xor    => BinOp.Xor
      case Tags.BinOp.Eq     => BinOp.Eq
      case Tags.BinOp.Equals => BinOp.Equals
      case Tags.BinOp.Neq    => BinOp.Neq
      case Tags.BinOp.Lt     => BinOp.Lt
      case Tags.BinOp.Lte    => BinOp.Lte
      case Tags.BinOp.Gt     => BinOp.Gt
      case Tags.BinOp.Gte    => BinOp.Gte
    }

    def getConvOp: ConvOp = getTag match {
      case Tags.ConvOp.Trunc    => ConvOp.Trunc
      case Tags.ConvOp.Zext     => ConvOp.Zext
      case Tags.ConvOp.Sext     => ConvOp.Sext
      case Tags.ConvOp.Fptrunc  => ConvOp.Fptrunc
      case Tags.ConvOp.Fpext    => ConvOp.Fpext
      case Tags.ConvOp.Fptoui   => ConvOp.Fptoui
      case Tags.ConvOp.Fptosi   => ConvOp.Fptosi
      case Tags.ConvOp.Uitofp   => ConvOp.Uitofp
      case Tags.ConvOp.Sitofp   => ConvOp.Sitofp
      case Tags.ConvOp.Ptrtoint => ConvOp.Ptrtoint
      case Tags.ConvOp.Inttoptr => ConvOp.Inttoptr
      case Tags.ConvOp.Bitcast  => ConvOp.Bitcast
      case Tags.ConvOp.Cast     => ConvOp.Cast
    }

    def getValOpt: Option[Val] = getOption(getVal)
    def getValSeq: Seq[Val] = getSeq(getVal)
    def getVal: Val = getVal(getTag)
    def getVal(tag: Tag): Val = tag match {
      case Tags.Name()     => getName(tag)
      case Tags.Val.Null   => Val.Null
      case Tags.Val.Unit   => Val.Unit
      case Tags.Val.This   => Val.This
      case Tags.Val.Bool   => Val.Bool(getBool)
      case Tags.Val.Number => Val.Number(getString, getType)
      case Tags.Val.Elem   => Val.Elem(getVal, getVal)
      case Tags.Val.Class  => Val.Class(getType)
      case Tags.Val.Str    => Val.Str(getString)
    }

    def getStat: Stat = getStat(getTag)
    def getStat(tag: Tag): Stat = tag match {
      case Tags.Stat.Class     => Stat.Class(getName, getNameSeq, getScope)
      case Tags.Stat.Interface => Stat.Interface(getNameSeq, getScope)
      case Tags.Stat.Module    => Stat.Module(getName, getNameSeq, getScope)
      case Tags.Stat.Field     => Stat.Field(getType)
      case Tags.Stat.Declare   => Stat.Declare(getType, getTypeSeq)
      case Tags.Stat.Define    => Stat.Define(getType, getLabeledTypeSeq, getEntryBlock)
    }

    def getScope = Scope(Map(getSeq((getName, getStat)): _*))

    def getBlockOpt(implicit env: BlockEnv): Option[Block] = getOption(getBlock)
    def getBlock(implicit env: BlockEnv): Block = env(getName)
    def getEntryBlock: Block = {
      val blocks = Seq.fill(getInt)(Block(getName, null, null))
      implicit val env = blocks.map { case b => b.name -> b }.toMap
      blocks.foreach { b =>
        b.instrs = getInstrSeq
        b.termn = getTermn
      }
      blocks.head
    }

    def getNameSeq: Seq[Name] = getSeq(getName)
    def getName: Name = getName(getTag)
    def getName(tag: Tag): Name = tag match {
      case Tags.Name.Local  => Name.Local(getString)
      case Tags.Name.Global => Name.Global(getString)
      case Tags.Name.Nested => Name.Nested(getName, getName)
    }

    def getBranchSeq(implicit env: BlockEnv): Seq[Branch] = getSeq(getBranch)
    def getBranch(implicit env: BlockEnv): Branch = Branch(getVal, getBlock)

    def getLabeledTypeSeq: Seq[LabeledType] = getSeq(getLabeledType)
    def getLabeledType: LabeledType = LabeledType(getType, getName)

    def getOption[T](f: => T): Option[T] = get.toInt match {
      case 0 => None
      case 1 => Some(f)
    }
    def getSeq[T](f: => T): Seq[T] =
      Seq.fill(getInt)(f)
    def getString: String =
      new String(getSeq(getChar).toArray)
    def getBool: Boolean = get.toInt match {
      case 1 => true
      case 0 => false
    }
  }
}
