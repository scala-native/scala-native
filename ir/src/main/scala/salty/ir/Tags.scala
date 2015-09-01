package salty.ir

object Tags {
  type Tag = Int

  object Type {
    final val Null    = 1
    final val Nothing = 1 + Null
    final val Unit    = 1 + Nothing
    final val Bool    = 1 + Unit
    final val I8      = 1 + Bool
    final val I16     = 1 + I8
    final val I32     = 1 + I16
    final val I64     = 1 + I32
    final val F32     = 1 + I64
    final val F64     = 1 + F32
    final val Ref     = 1 + F64
    final val Slice   = 1 + Ref
    final val Array   = 1 + Slice
    final val Named   = 1 + Array
  }

  object Instr {
    final val Assign = 1 + Type.Named
  }

  object Termn {
    final val Out    = 1 + Instr.Assign
    final val Return = 1 + Out
    final val Throw  = 1 + Return
    final val Jump   = 1 + Throw
    final val If     = 1 + Jump
    final val Switch = 1 + If
    final val Try    = 1 + Switch
  }

  object BinOp {
    final val Add    = 1 + Termn.Try
    final val Sub    = 1 + Add
    final val Mul    = 1 + Sub
    final val Div    = 1 + Mul
    final val Mod    = 1 + Div
    final val Shl    = 1 + Mod
    final val Lshr   = 1 + Shl
    final val Ashr   = 1 + Lshr
    final val And    = 1 + Ashr
    final val Or     = 1 + And
    final val Xor    = 1 + Or
    final val Eq     = 1 + Xor
    final val Equals = 1 + Eq
    final val Neq    = 1 + Equals
    final val Lt     = 1 + Neq
    final val Lte    = 1 + Lt
    final val Gt     = 1 + Lte
    final val Gte    = 1 + Gt
  }

  object ConvOp {
    final val Trunc    = 1 + BinOp.Gte
    final val Zext     = 1 + Trunc
    final val Sext     = 1 + Zext
    final val Fptrunc  = 1 + Sext
    final val Fpext    = 1 + Fptrunc
    final val Fptoui   = 1 + Fpext
    final val Fptosi   = 1 + Fptoui
    final val Uitofp   = 1 + Fptosi
    final val Sitofp   = 1 + Uitofp
    final val Ptrtoint = 1 + Sitofp
    final val Inttoptr = 1 + Ptrtoint
    final val Bitcast  = 1 + Inttoptr
    final val Cast     = 1 + Bitcast
  }

  object Expr {
    def unapply(tag: Tag) = Tags.Val.unapply(tag) || (tag >= Bin && tag <= Catchpad)

    final val Bin      = 1 + ConvOp.Cast
    final val Conv     = 1 + Bin
    final val Is       = 1 + Conv
    final val Alloc    = 1 + Is
    final val Call     = 1 + Alloc
    final val Phi      = 1 + Call
    final val Load     = 1 + Phi
    final val Store    = 1 + Load
    final val Box      = 1 + Store
    final val Unbox    = 1 + Box
    final val Length   = 1 + Unbox
    final val Catchpad = 1 + Length
  }

  object Val {
    def unapply(tag: Tag) = Tags.Name.unapply(tag) || (tag >= Null && tag <= Str)

    final val Null   = 1 + Expr.Catchpad
    final val Unit   = 1 + Null
    final val This   = 1 + Unit
    final val Bool   = 1 + This
    final val Number = 1 + Bool
    final val Array  = 1 + Number
    final val Slice  = 1 + Array
    final val Elem   = 1 + Slice
    final val Class  = 1 + Elem
    final val Str    = 1 + Class
  }

  object Stat {
    final val Class     = 1 + Val.Str
    final val Interface = 1 + Class
    final val Module    = 1 + Interface
    final val Var       = 1 + Module
    final val Declare   = 1 + Var
    final val Define    = 1 + Declare
  }

  object Name {
    def unapply(tag: Tag) = tag >= Local && tag <= Nested

    final val Local  = 1 + Stat.Define
    final val Global = 1 + Local
    final val Nested = 1 + Global
  }
}
