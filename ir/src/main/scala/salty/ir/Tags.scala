package salty.ir

import salty.ir.{Tags => T}

object Tags {
  final val Empty = 1

  final val Label         = 1 + Empty
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

  final val Add      = 1 + End
  final val Sub      = 1 + Add
  final val Mul      = 1 + Sub
  final val Div      = 1 + Mul
  final val Mod      = 1 + Div
  final val Shl      = 1 + Mod
  final val Lshr     = 1 + Shl
  final val Ashr     = 1 + Lshr
  final val And      = 1 + Ashr
  final val Or       = 1 + And
  final val Xor      = 1 + Or
  final val Eq       = 1 + Xor
  final val Neq      = 1 + Eq
  final val Lt       = 1 + Neq
  final val Lte      = 1 + Lt
  final val Gt       = 1 + Lte
  final val Gte      = 1 + Gt
  final val Trunc    = 1 + Gte
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

  final val EfPhi  = 1 + Bitcast
  final val Call   = 1 + EfPhi
  final val Load   = 1 + Call
  final val Store  = 1 + Load
  final val Elem   = 1 + Store
  final val Param  = 1 + Elem
  final val Phi    = 1 + Param
  final val Alloc  = 1 + Phi
  final val Alloca = 1 + Alloc

  final val Equals     = 1 + Alloca
  final val Hash       = 1 + Equals
  final val FieldElem  = 1 + Hash
  final val MethodElem = 1 + FieldElem
  final val SliceElem  = 1 + MethodElem
  final val GetClass   = 1 + SliceElem
  final val Length     = 1 + GetClass
  final val Is         = 1 + Length
  final val As         = 1 + Is
  final val Box        = 1 + As
  final val Unbox      = 1 + Box
  final val ClassAlloc = 1 + Unbox
  final val SliceAlloc = 1 + ClassAlloc

  final val Unit  = 1 + SliceAlloc
  final val Null  = 1 + Unit
  final val True  = 1 + Null
  final val False = 1 + True
  final val Zero  = 1 + False
  final val Size  = 1 + Zero
  final val I8    = 1 + Size
  final val I16   = 1 + I8
  final val I32   = 1 + I16
  final val I64   = 1 + I32
  final val F32   = 1 + I64
  final val F64   = 1 + F32
  final val Str   = 1 + F64

  final val UnitDefn    = 1 + Str
  final val BoolDefn    = 1 + UnitDefn
  final val I8Defn      = 1 + BoolDefn
  final val I16Defn     = 1 + I8Defn
  final val I32Defn     = 1 + I16Defn
  final val I64Defn     = 1 + I32Defn
  final val F32Defn     = 1 + I64Defn
  final val F64Defn     = 1 + F32Defn
  final val NothingDefn = 1 + F64Defn
  final val NullDefn    = 1 + NothingDefn
  final val AnyRefDefn  = 1 + NullDefn

  final val GlobalDefn   = 1 + AnyRefDefn
  final val ConstantDefn = 1 + GlobalDefn
  final val DefineDefn   = 1 + ConstantDefn
  final val DeclareDefn  = 1 + DefineDefn
  final val ExternDefn   = 1 + DeclareDefn
  final val StructDefn   = 1 + ExternDefn
  final val PtrDefn      = 1 + StructDefn
  final val FunctionDefn = 1 + PtrDefn

  final val ClassDefn     = 1 + FunctionDefn
  final val InterfaceDefn = 1 + ClassDefn
  final val ModuleDefn    = 1 + InterfaceDefn
  final val MethodDefn    = 1 + ModuleDefn
  final val FieldDefn     = 1 + MethodDefn
  final val SliceDefn     = 1 + FieldDefn

  final val NoName             = 1 + SliceDefn
  final val MainName           = 1 + NoName
  final val BuiltinName        = 1 + MainName
  final val LocalName          = 1 + BuiltinName
  final val ClassName          = 1 + LocalName
  final val VtableName         = 1 + ClassName
  final val VtableConstantName = 1 + VtableName
  final val ClassDataName      = 1 + VtableConstantName
  final val ClassRefName       = 1 + ClassDataName
  final val ModuleName         = 1 + ClassRefName
  final val ModuleAccessorName = 1 + ModuleName
  final val ModuleDataName     = 1 + ModuleAccessorName
  final val InterfaceName      = 1 + ModuleDataName
  final val SliceName          = 1 + InterfaceName
  final val FieldName          = 1 + SliceName
  final val ConstructorName    = 1 + FieldName
  final val MethodName         = 1 + ConstructorName

  final val ValSchema  = 1 + MethodName
  final val CfSchema   = 1 + ValSchema
  final val EfSchema   = 1 + CfSchema
  final val RefSchema  = 1 + EfSchema
  final val ManySchema = 1 + RefSchema

  val plain2tag: Map[Desc.Plain, Int] = Map(
    Desc.Empty -> T.Empty,

    Desc.Label         -> T.Label        ,
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

    Desc.EfPhi  -> T.EfPhi ,
    Desc.Call   -> T.Call  ,
    Desc.Load   -> T.Load  ,
    Desc.Store  -> T.Store ,
    Desc.Elem   -> T.Elem  ,
    Desc.Param  -> T.Param ,
    Desc.Phi    -> T.Phi   ,
    Desc.Alloc  -> T.Alloc ,
    Desc.Alloca -> T.Alloca,

    Desc.Equals     -> T.Equals    ,
    Desc.Hash       -> T.Hash      ,
    Desc.FieldElem  -> T.FieldElem ,
    Desc.MethodElem -> T.MethodElem,
    Desc.SliceElem  -> T.SliceElem ,
    Desc.GetClass   -> T.GetClass  ,
    Desc.Length     -> T.Length    ,
    Desc.Is         -> T.Is        ,
    Desc.As         -> T.As        ,
    Desc.Box        -> T.Box       ,
    Desc.Unbox      -> T.Unbox     ,
    Desc.ClassAlloc -> T.ClassAlloc,
    Desc.SliceAlloc -> T.SliceAlloc,

    Desc.Unit  -> T.Unit ,
    Desc.Null  -> T.Null ,
    Desc.True  -> T.True ,
    Desc.False -> T.False,
    Desc.Zero  -> T.Zero ,
    Desc.Size  -> T.Size ,

    Desc.Builtin.Unit    -> T.UnitDefn   ,
    Desc.Builtin.Bool    -> T.BoolDefn   ,
    Desc.Builtin.I8      -> T.I8Defn     ,
    Desc.Builtin.I16     -> T.I16Defn    ,
    Desc.Builtin.I32     -> T.I32Defn    ,
    Desc.Builtin.I64     -> T.I64Defn    ,
    Desc.Builtin.F32     -> T.F32Defn    ,
    Desc.Builtin.F64     -> T.F64Defn    ,
    Desc.Builtin.Nothing -> T.NothingDefn,
    Desc.Builtin.Null    -> T.NullDefn   ,
    Desc.Builtin.AnyRef  -> T.AnyRefDefn ,

    Desc.Defn.Global   -> T.GlobalDefn  ,
    Desc.Defn.Define   -> T.DefineDefn  ,
    Desc.Defn.Declare  -> T.DeclareDefn ,
    Desc.Defn.Extern   -> T.ExternDefn  ,
    Desc.Defn.Struct   -> T.StructDefn  ,
    Desc.Defn.Ptr      -> T.PtrDefn     ,
    Desc.Defn.Function -> T.FunctionDefn,

    Desc.Defn.Class     -> T.ClassDefn    ,
    Desc.Defn.Interface -> T.InterfaceDefn,
    Desc.Defn.Module    -> T.ModuleDefn   ,
    Desc.Defn.Method    -> T.MethodDefn   ,
    Desc.Defn.Field     -> T.FieldDefn    ,
    Desc.Defn.Slice     -> T.SliceDefn
  )

  val tag2plain: Map[Int, Desc.Plain] = plain2tag.map { case (k, v) => (v, k) }
}
