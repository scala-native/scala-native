package native
package nir

object Tags {
  final val NoAdvice   = 1
  final val HintAdvice = 1 + NoAdvice
  final val MustAdvice = 1 + HintAdvice

  final val UsgnAttr      = 1 + MustAdvice
  final val InlineAttr    = 1 + UsgnAttr
  final val OverridesAttr = 1 + InlineAttr

  final val AddBin  = 1 + OverridesAttr
  final val SubBin  = 1 + AddBin
  final val MulBin  = 1 + SubBin
  final val DivBin  = 1 + MulBin
  final val ModBin  = 1 + DivBin
  final val ShlBin  = 1 + ModBin
  final val LshrBin = 1 + ShlBin
  final val AshrBin = 1 + LshrBin
  final val AndBin  = 1 + AshrBin
  final val OrBin   = 1 + AndBin
  final val XorBin  = 1 + OrBin

  final val EqComp  = 1 + XorBin
  final val NeqComp = 1 + EqComp
  final val LtComp  = 1 + NeqComp
  final val LteComp = 1 + LtComp
  final val GtComp  = 1 + LteComp
  final val GteComp = 1 + GtComp

  final val TruncConv    = 1 + GteComp
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

  final val VarDefn      = 1 + BitcastConv
  final val DeclareDefn  = 1 + VarDefn
  final val DefineDefn   = 1 + DeclareDefn
  final val StructDefn   = 1 + DefineDefn
  final val IntefaceDefn = 1 + StructDefn
  final val ClassDefn    = 1 + IntefaceDefn
  final val ModuleDefn   = 1 + ClassDefn

  final val UnreachableOp = 1 + ModuleDefn
  final val RetOp         = 1 + UnreachableOp
  final val JumpOp        = 1 + RetOp
  final val IfOp          = 1 + JumpOp
  final val SwitchOp      = 1 + IfOp
  final val InvokeOp      = 1 + SwitchOp

  final val ThrowOp = 1 + InvokeOp
  final val TryOp   = 1 + ThrowOp

  final val CallOp        = 1 + TryOp
  final val LoadOp        = 1 + CallOp
  final val StoreOp       = 1 + LoadOp
  final val ElemOp        = 1 + StoreOp
  final val ExtractOp     = 1 + ElemOp
  final val InsertOp      = 1 + ExtractOp
  final val AllocaOp      = 1 + InsertOp
  final val BinOp         = 1 + AllocaOp
  final val CompOp        = 1 + BinOp
  final val ConvOp        = 1 + CompOp

  final val AllocOp     = 1 + ConvOp
  final val FieldOp     = 1 + AllocOp
  final val MethodOp    = 1 + FieldOp
  final val ModuleOp    = 1 + MethodOp
  final val AsOp        = 1 + ModuleOp
  final val IsOp        = 1 + AsOp
  final val ArrAllocOp  = 1 + IsOp
  final val ArrLengthOp = 1 + ArrAllocOp
  final val ArrElemOp   = 1 + ArrLengthOp
  final val CopyOp      = 1 + ArrElemOp

  final val NoneType           = 1 + CopyOp
  final val VoidType           = 1 + NoneType
  final val SizeType           = 1 + VoidType
  final val BoolType           = 1 + SizeType
  final val LabelType          = 1 + BoolType
  final val I8Type             = 1 + LabelType
  final val I16Type            = 1 + I8Type
  final val I32Type            = 1 + I16Type
  final val I64Type            = 1 + I32Type
  final val F32Type            = 1 + I64Type
  final val F64Type            = 1 + F32Type
  final val ArrayType          = 1 + F64Type
  final val PtrType            = 1 + ArrayType
  final val FunctionType       = 1 + PtrType
  final val StructType         = 1 + FunctionType
  final val UnitType           = 1 + StructType
  final val NothingType        = 1 + UnitType
  final val NullType           = 1 + NothingType
  final val ClassType          = 1 + NullType
  final val InterfaceClassType = 1 + ClassType
  final val ModuleClassType    = 1 + InterfaceClassType
  final val ArrayClassType     = 1 + ModuleClassType

  final val NoneVal   = 1 + ArrayClassType
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
  final val NullVal   = 1 + UnitVal
  final val StringVal = 1 + NullVal
  final val SizeVal   = 1 + StringVal
  final val ClassVal  = 1 + SizeVal
}
