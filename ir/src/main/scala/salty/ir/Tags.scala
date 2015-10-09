package salty.ir

import salty.ir.{Tags => T}

object Tags {
  final val Start         = 1
  final val Label         = 1 + Start
  final val If            = 1 + Label
  final val Switch        = 1 + If
  final val Try           = 1 + Switch
  final val CaseTrue      = 1 + Try
  final val CaseFalse     = 1 + CaseTrue
  final val CaseConst     = 1 + CaseFalse
  final val CaseDefault   = 1 + CaseConst
  final val CaseException = 1 + CaseDefault
  final val Merge         = 1 + CaseException
  final val Return        = 1 + Merge
  final val Throw         = 1 + Return
  final val Undefined     = 1 + Throw
  final val End           = 1 + Undefined

  final val Add           = 1 + End
  final val Sub           = 1 + Add
  final val Mul           = 1 + Sub
  final val Div           = 1 + Mul
  final val Mod           = 1 + Div
  final val Shl           = 1 + Mod
  final val Lshr          = 1 + Shl
  final val Ashr          = 1 + Lshr
  final val And           = 1 + Ashr
  final val Or            = 1 + And
  final val Xor           = 1 + Or
  final val Eq            = 1 + Xor
  final val Neq           = 1 + Eq
  final val Lt            = 1 + Neq
  final val Lte           = 1 + Lt
  final val Gt            = 1 + Lte
  final val Gte           = 1 + Gt
  final val Trunc         = 1 + Gte
  final val Zext          = 1 + Trunc
  final val Sext          = 1 + Zext
  final val Fptrunc       = 1 + Sext
  final val Fpext         = 1 + Fptrunc
  final val Fptoui        = 1 + Fpext
  final val Fptosi        = 1 + Fptoui
  final val Uitofp        = 1 + Fptosi
  final val Sitofp        = 1 + Uitofp
  final val Ptrtoint      = 1 + Sitofp
  final val Inttoptr      = 1 + Ptrtoint
  final val Bitcast       = 1 + Inttoptr
  final val As            = 1 + Bitcast
  final val Box           = 1 + As
  final val Unbox         = 1 + Box

  final val EfPhi         = 1 + Unbox
  final val Call          = 1 + EfPhi
  final val Load          = 1 + Call
  final val Store         = 1 + Load
  final val Equals        = 1 + Store
  final val Hash          = 1 + Equals

  final val Phi           = 1 + Hash
  final val Param         = 1 + Phi
  final val Alloc         = 1 + Param
  final val Alloca        = 1 + Alloc
  final val Allocs        = 1 + Alloca
  final val Is            = 1 + Allocs
  final val Length        = 1 + Is
  final val Elem          = 1 + Length
  final val GetClass      = 1 + Elem

  final val Unit  = 1 + GetClass
  final val Null  = 1 + Unit
  final val True  = 1 + Null
  final val False = 1 + True
  final val I8    = 1 + False
  final val I16   = 1 + I8
  final val I32   = 1 + I16
  final val I64   = 1 + I32
  final val F32   = 1 + I64
  final val F64   = 1 + F32
  final val Str   = 1 + F64
  final val Tag   = 1 + Str

  final val Class     = 1 + Tag
  final val Interface = 1 + Class
  final val Module    = 1 + Interface
  final val Declare   = 1 + Module
  final val Define    = 1 + Declare
  final val Field     = 1 + Define
  final val Extern    = 1 + Field
  final val Type      = 1 + Extern
  final val Primitive = 1 + Type

  final val NoName        = 1 + Primitive
  final val ClassName     = 1 + NoName
  final val ModuleName    = 1 + ClassName
  final val InterfaceName = 1 + ModuleName
  final val PrimitiveName = 1 + InterfaceName
  final val SliceName     = 1 + PrimitiveName
  final val FieldName     = 1 + SliceName
  final val MethodName    = 1 + FieldName

  final val HoleShape  = 1 + MethodName
  final val RefShape   = 1 + HoleShape
  final val SliceShape = 1 + RefShape

  val plain2tag: Map[Desc.Plain, Int] = Map(
    Desc.Start         -> T.Start        ,
    Desc.If            -> T.If           ,
    Desc.Switch        -> T.Switch       ,
    Desc.Try           -> T.Try          ,
    Desc.CaseTrue      -> T.CaseTrue     ,
    Desc.CaseFalse     -> T.CaseFalse    ,
    Desc.CaseConst     -> T.CaseConst    ,
    Desc.CaseDefault   -> T.CaseDefault  ,
    Desc.CaseException -> T.CaseException,
    Desc.Merge         -> T.Merge        ,
    Desc.Return        -> T.Return       ,
    Desc.Throw         -> T.Throw        ,
    Desc.Undefined     -> T.Undefined    ,
    Desc.End           -> T.End          ,

    Desc.Add  -> T.Add ,
    Desc.Sub  -> T.Sub ,
    Desc.Mul  -> T.Mul ,
    Desc.Div  -> T.Div ,
    Desc.Mod  -> T.Mod ,
    Desc.Shl  -> T.Shl ,
    Desc.Lshr -> T.Lshr,
    Desc.Ashr -> T.Ashr,
    Desc.And  -> T.And ,
    Desc.Or   -> T.Or  ,
    Desc.Xor  -> T.Xor ,
    Desc.Eq   -> T.Eq  ,
    Desc.Neq  -> T.Neq ,
    Desc.Lt   -> T.Lt  ,
    Desc.Lte  -> T.Lte ,
    Desc.Gt   -> T.Gt  ,
    Desc.Gte  -> T.Gte ,

    Desc.Trunc    -> T.Trunc   ,
    Desc.Zext     -> T.Zext    ,
    Desc.Sext     -> T.Sext    ,
    Desc.Fptrunc  -> T.Fptrunc ,
    Desc.Fpext    -> T.Fpext   ,
    Desc.Fptoui   -> T.Fptoui  ,
    Desc.Fptosi   -> T.Fptosi  ,
    Desc.Uitofp   -> T.Uitofp  ,
    Desc.Sitofp   -> T.Sitofp  ,
    Desc.Ptrtoint -> T.Ptrtoint,
    Desc.Inttoptr -> T.Inttoptr,
    Desc.Bitcast  -> T.Bitcast ,
    Desc.As       -> T.As      ,
    Desc.Box      -> T.Box     ,
    Desc.Unbox    -> T.Unbox   ,

    Desc.EfPhi  -> T.EfPhi ,
    Desc.Call   -> T.Call  ,
    Desc.Load   -> T.Load  ,
    Desc.Store  -> T.Store ,
    Desc.Equals -> T.Equals,
    Desc.Hash   -> T.Hash  ,

    Desc.Phi         -> T.Phi        ,
    Desc.Alloc       -> T.Alloc      ,
    Desc.Alloca      -> T.Alloca     ,
    Desc.Allocs      -> T.Allocs     ,
    Desc.Is          -> T.Is         ,
    Desc.Length      -> T.Length     ,
    Desc.Elem        -> T.Elem       ,
    Desc.GetClass    -> T.GetClass   ,

    Desc.Unit  -> T.Unit ,
    Desc.Null  -> T.Null ,
    Desc.True  -> T.True ,
    Desc.False -> T.False
  )

  val tag2plain: Map[Int, Desc.Plain] = plain2tag.map { case (k, v) => (v, k) }
}
