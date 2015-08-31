package salty.ir

object Tags {
  private var counter: Byte = Byte.MinValue
  private def iota(): Byte = {
    assert(counter < Byte.MaxValue)
    counter = (counter + 1.toByte).toByte
    counter
  }

  object Type {
    val Null    = iota()
    val Nothing = iota()
    val Unit    = iota()
    val Bool    = iota()
    val I       = iota()
    val F       = iota()
    val Ptr     = iota()
    val Slice   = iota()
    val Array   = iota()
  }

  object Instr {
    val Assign = iota()
  }

  object Termn {
    val Out    = iota()
    val Return = iota()
    val Throw  = iota()
    val Jump   = iota()
    val If     = iota()
    val Switch = iota()
    val Try    = iota()
  }

  object BinOp {
    val Add    = iota()
    val Sub    = iota()
    val Mul    = iota()
    val Div    = iota()
    val Mod    = iota()
    val Shl    = iota()
    val Lshr   = iota()
    val Ashr   = iota()
    val And    = iota()
    val Or     = iota()
    val Xor    = iota()
    val Eq     = iota()
    val Equals = iota()
    val Neq    = iota()
    val Lt     = iota()
    val Lte    = iota()
    val Gt     = iota()
    val Gte    = iota()
  }

  object ConvOp {
    val Trunc    = iota()
    val Zext     = iota()
    val Sext     = iota()
    val Fptrunc  = iota()
    val Fpext    = iota()
    val Fptoui   = iota()
    val Fptosi   = iota()
    val Uitofp   = iota()
    val Sitofp   = iota()
    val Ptrtoint = iota()
    val Inttoptr = iota()
    val Bitcast  = iota()
    val Cast     = iota()
  }

  object Expr {
    val Bin      = iota()
    val Conv     = iota()
    val Is       = iota()
    val Alloc    = iota()
    val Call     = iota()
    val Phi      = iota()
    val Load     = iota()
    val Store    = iota()
    val Box      = iota()
    val Unbox    = iota()
    val Length   = iota()
    val Catchpad = iota()
  }

  object Val {
    val Null   = iota()
    val Unit   = iota()
    val This   = iota()
    val Bool   = iota()
    val Number = iota()
    val Array  = iota()
    val Slice  = iota()
    val Elem   = iota()
    val Class  = iota()
    val Str    = iota()
  }

  object Stat {
    val Class     = iota()
    val Interface = iota()
    val Module    = iota()
    val Var       = iota()
    val Declare   = iota()
    val Define    = iota()
  }

  object Name {
    val Local  = iota()
    val Global = iota()
    val Nested = iota()
  }
}
