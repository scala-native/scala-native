package scala.scalanative
package nir

object Tags {
  final val InlineHintAttr = 1
  final val NoInlineAttr   = 1 + InlineHintAttr
  final val MustInlineAttr = 1 + NoInlineAttr

  final val PrivateAttr             = 1 + MustInlineAttr
  final val InternalAttr            = 1 + PrivateAttr
  final val AvailableExternallyAttr = 1 + InternalAttr
  final val LinkOnceAttr            = 1 + AvailableExternallyAttr
  final val WeakAttr                = 1 + LinkOnceAttr
  final val CommonAttr              = 1 + WeakAttr
  final val AppendingAttr           = 1 + CommonAttr
  final val ExternWeakAttr          = 1 + AppendingAttr
  final val LinkOnceODRAttr         = 1 + ExternWeakAttr
  final val WeakODRAttr             = 1 + LinkOnceODRAttr
  final val ExternalAttr            = 1 + WeakODRAttr

  final val OverrideAttr = 1 + ExternalAttr
  final val PinAttr      = 1 + OverrideAttr
  final val PinIfAttr    = 1 + PinAttr

  final val IaddBin = 1 + PinIfAttr
  final val FaddBin = 1 + IaddBin
  final val IsubBin = 1 + FaddBin
  final val FsubBin = 1 + IsubBin
  final val ImulBin = 1 + FsubBin
  final val FmulBin = 1 + ImulBin
  final val SdivBin = 1 + FmulBin
  final val UdivBin = 1 + SdivBin
  final val FdivBin = 1 + UdivBin
  final val SremBin = 1 + FdivBin
  final val UremBin = 1 + SremBin
  final val FremBin = 1 + UremBin
  final val ShlBin  = 1 + FremBin
  final val LshrBin = 1 + ShlBin
  final val AshrBin = 1 + LshrBin
  final val AndBin  = 1 + AshrBin
  final val OrBin   = 1 + AndBin
  final val XorBin  = 1 + OrBin

  final val IeqComp = 1 + XorBin
  final val IneComp = 1 + IeqComp
  final val UgtComp = 1 + IneComp
  final val UgeComp = 1 + UgtComp
  final val UltComp = 1 + UgeComp
  final val UleComp = 1 + UltComp
  final val SgtComp = 1 + UleComp
  final val SgeComp = 1 + SgtComp
  final val SltComp = 1 + SgeComp
  final val SleComp = 1 + SltComp

  final val FeqComp = 1 + SleComp
  final val FneComp = 1 + FeqComp
  final val FgtComp = 1 + FneComp
  final val FgeComp = 1 + FgtComp
  final val FltComp = 1 + FgeComp
  final val FleComp = 1 + FltComp

  final val TruncConv    = 1 + FleComp
  final val ZextConv     = 1 + TruncConv
  final val SextConv     = 1 + ZextConv
  final val FptruncConv  = 1 + SextConv
  final val FpextConv    = 1 + FptruncConv
  final val FptouiConv   = 1 + FpextConv
  final val FptosiConv   = 1 + FptouiConv
  final val UitofpConv   = 1 + FptosiConv
  final val SitofpConv   = 1 + UitofpConv
  final val PtrtointConv = 1 + SitofpConv
  final val InttoptrConv = 1 + PtrtointConv
  final val BitcastConv  = 1 + InttoptrConv

  final val VarDefn     = 1 + BitcastConv
  final val ConstDefn   = 1 + VarDefn
  final val DeclareDefn = 1 + ConstDefn
  final val DefineDefn  = 1 + DeclareDefn
  final val StructDefn  = 1 + DefineDefn
  final val TraitDefn   = 1 + StructDefn
  final val ClassDefn   = 1 + TraitDefn
  final val ModuleDefn  = 1 + ClassDefn

  final val PrivateLink             = 1 + ModuleDefn
  final val InternalLink            = 1 + PrivateLink
  final val AvailableExternallyLink = 1 + InternalLink
  final val OnceLink                = 1 + AvailableExternallyLink
  final val WeakLink                = 1 + OnceLink
  final val CommonLink              = 1 + WeakLink
  final val AppendingLink           = 1 + CommonLink
  final val ExternWeakLink          = 1 + AppendingLink
  final val OnceODRLink             = 1 + ExternWeakLink
  final val WeakODRLink             = 1 + OnceODRLink
  final val ExternalLink            = 1 + WeakODRLink

  final val UnreachableCf = 1 + ExternalLink
  final val RetCf         = 1 + UnreachableCf
  final val JumpCf        = 1 + RetCf
  final val IfCf          = 1 + JumpCf
  final val SwitchCf      = 1 + IfCf
  final val InvokeCf      = 1 + SwitchCf
  final val ResumeCf      = 1 + InvokeCf

  final val ThrowCf = 1 + ResumeCf
  final val TryCf   = 1 + ThrowCf

  final val ValGlobal    = 1 + TryCf
  final val TypeGlobal   = 1 + ValGlobal
  final val MemberGlobal = 1 + TypeGlobal

  final val SuccNext  = 1 + MemberGlobal
  final val FailNext  = 1 + SuccNext
  final val LabelNext = 1 + FailNext
  final val CaseNext  = 1 + LabelNext

  final val CallOp    = 1 + CaseNext
  final val LoadOp    = 1 + CallOp
  final val StoreOp   = 1 + LoadOp
  final val ElemOp    = 1 + StoreOp
  final val ExtractOp = 1 + ElemOp
  final val InsertOp  = 1 + ExtractOp
  final val AllocaOp  = 1 + InsertOp
  final val BinOp     = 1 + AllocaOp
  final val CompOp    = 1 + BinOp
  final val ConvOp    = 1 + CompOp

  final val AllocOp   = 1 + ConvOp
  final val FieldOp   = 1 + AllocOp
  final val MethodOp  = 1 + FieldOp
  final val ModuleOp  = 1 + MethodOp
  final val AsOp      = 1 + ModuleOp
  final val IsOp      = 1 + AsOp
  final val CopyOp    = 1 + IsOp
  final val SizeofOp  = 1 + CopyOp
  final val TypeofOp  = 1 + SizeofOp
  final val ClosureOp = 1 + TypeofOp

  final val NoneType       = 1 + ClosureOp
  final val VoidType       = 1 + NoneType
  final val LabelType      = 1 + VoidType
  final val VarargType     = 1 + LabelType
  final val PtrType        = 1 + VarargType
  final val BoolType       = 1 + PtrType
  final val I8Type         = 1 + BoolType
  final val I16Type        = 1 + I8Type
  final val I32Type        = 1 + I16Type
  final val I64Type        = 1 + I32Type
  final val F32Type        = 1 + I64Type
  final val F64Type        = 1 + F32Type
  final val ArrayType      = 1 + F64Type
  final val FunctionType   = 1 + ArrayType
  final val StructType     = 1 + FunctionType

  final val SizeType    = 1 + StructType
  final val UnitType    = 1 + SizeType
  final val ClassType   = 1 + UnitType
  final val TraitType   = 1 + ClassType
  final val ModuleType  = 1 + TraitType

  final val NoneVal   = 1 + ModuleType
  final val TrueVal   = 1 + NoneVal
  final val FalseVal  = 1 + TrueVal
  final val ZeroVal   = 1 + FalseVal
  final val I8Val     = 1 + ZeroVal
  final val I16Val    = 1 + I8Val
  final val I32Val    = 1 + I16Val
  final val I64Val    = 1 + I32Val
  final val F32Val    = 1 + I64Val
  final val F64Val    = 1 + F32Val
  final val StructVal = 1 + F64Val
  final val ArrayVal  = 1 + StructVal
  final val CharsVal  = 1 + ArrayVal
  final val LocalVal  = 1 + CharsVal
  final val GlobalVal = 1 + LocalVal
  final val UnitVal   = 1 + GlobalVal
  final val StringVal = 1 + UnitVal
}
