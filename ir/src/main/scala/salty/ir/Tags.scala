package salty.ir

object Tag {
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
    final val Of      = 1 + Slice
  }

  object Op {
    final val Start       = 1 + Tag.Type.Of
    final val If          = 1 + Start
    final val Switch      = 1 + If
    final val CaseTrue    = 1 + Switch
    final val CaseFalse   = 1 + CaseTrue
    final val CaseValue   = 1 + CaseFalse
    final val CaseDefault = 1 + CaseValue
    final val CaseEnd     = 1 + CaseDefault
    final val Return      = 1 + CaseEnd
    final val Throw       = 1 + Return
    final val Undefined   = 1 + Throw
    final val Merge       = 1 + Undefined
    final val End         = 1 + Merge
    final val Add         = 1 + End
    final val Sub         = 1 + Add
    final val Mul         = 1 + Sub
    final val Div         = 1 + Mul
    final val Mod         = 1 + Div
    final val Shl         = 1 + Mod
    final val Lshr        = 1 + Shl
    final val Ashr        = 1 + Lshr
    final val And         = 1 + Ashr
    final val Or          = 1 + And
    final val Xor         = 1 + Or
    final val Eq          = 1 + Xor
    final val Equals      = 1 + Eq
    final val Neq         = 1 + Equals
    final val Lt          = 1 + Neq
    final val Lte         = 1 + Lt
    final val Gt          = 1 + Lte
    final val Gte         = 1 + Gt
    final val Trunc       = 1 + Gte
    final val Zext        = 1 + Trunc
    final val Sext        = 1 + Zext
    final val Fptrunc     = 1 + Sext
    final val Fpext       = 1 + Fptrunc
    final val Fptoui      = 1 + Fpext
    final val Fptosi      = 1 + Fptoui
    final val Uitofp      = 1 + Fptosi
    final val Sitofp      = 1 + Uitofp
    final val Ptrtoint    = 1 + Sitofp
    final val Inttoptr    = 1 + Ptrtoint
    final val Bitcast     = 1 + Inttoptr
    final val Cast        = 1 + Bitcast
    final val Is          = 1 + Cast
    final val Alloc       = 1 + Is
    final val Salloc      = 1 + Alloc
    final val Call        = 1 + Salloc
    final val Phi         = 1 + Call
    final val Load        = 1 + Phi
    final val Store       = 1 + Load
    final val Box         = 1 + Store
    final val Unbox       = 1 + Box
    final val Length      = 1 + Unbox
    final val Catchpad    = 1 + Length
    final val Elem        = 1 + Catchpad
    final val Class       = 1 + Elem
    final val In          = 1 + Class
    final val Val         = 1 + In
    final val Type        = 1 + Val
    final val Defn        = 1 + Type
    final val Name        = 1 + Defn
  }

  object Defn {
    final val Class     = 1 + Tag.Op.Name
    final val Interface = 1 + Class
    final val Module    = 1 + Interface
    final val Declare   = 1 + Module
    final val Define    = 1 + Declare
    final val Field     = 1 + Define
    final val Extern    = 1 + Field
  }

  object Rel {
    final val Parent    = 1 + Tag.Defn.Extern
    final val Interface = 1 + Parent
    final val Overrides = 1 + Interface
    final val Belongs   = 1 + Overrides
  }

  object Name {
    final val Local  = 1 + Tag.Rel.Belongs
    final val Global = 1 + Local
    final val Nested = 1 + Global
  }
}
