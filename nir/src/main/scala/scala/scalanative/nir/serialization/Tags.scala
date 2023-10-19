package scala.scalanative
package nir
package serialization

/** Serialization tags are unique type ids used to identify types in the binary
 *  representation of NIR. There are some holes in the numbering of the types to
 *  allow for binary-compatible leeway with adding new IR nodes.
 */
object Tags {

  // Attibutes
  final val MayInlineAttr = 1
  final val InlineHintAttr = 1 + MayInlineAttr
  final val NoInlineAttr = 1 + InlineHintAttr
  final val AlwaysInlineAttr = 1 + NoInlineAttr
  final val MaySpecialize = 1 + AlwaysInlineAttr
  final val NoSpecialize = 1 + MaySpecialize
  final val UnOptAttr = 1 + NoSpecialize
  final val NoOptAttr = 1 + UnOptAttr
  final val DidOptAttr = 1 + NoOptAttr
  final val BailOptAttr = 1 + DidOptAttr
  final val ExternAttr = 1 + BailOptAttr
  final val LinkAttr = 1 + ExternAttr
  final val DynAttr = 1 + LinkAttr
  final val StubAttr = 1 + DynAttr
  final val AbstractAttr = 1 + StubAttr
  final val VolatileAttr = 1 + AbstractAttr
  final val FinalAttr = 1 + VolatileAttr
  final val LinktimeResolvedAttr = 1 + FinalAttr
  final val UsesIntrinsicAttr = 1 + LinktimeResolvedAttr
  final val AlignAttr = 1 + UsesIntrinsicAttr
  final val DefineAttr = 1 + AlignAttr

  // Binary ops
  final val IaddBin = 1
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
  final val ShlBin = 1 + FremBin
  final val LshrBin = 1 + ShlBin
  final val AshrBin = 1 + LshrBin
  final val AndBin = 1 + AshrBin
  final val OrBin = 1 + AndBin
  final val XorBin = 1 + OrBin

  // Comparison ops
  final val IeqComp = 1
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

  // Conversion ops
  final val TruncConv = 1
  final val ZextConv = 1 + TruncConv
  final val SextConv = 1 + ZextConv
  final val FptruncConv = 1 + SextConv
  final val FpextConv = 1 + FptruncConv
  final val FptouiConv = 1 + FpextConv
  final val FptosiConv = 1 + FptouiConv
  final val UitofpConv = 1 + FptosiConv
  final val SitofpConv = 1 + UitofpConv
  final val PtrtointConv = 1 + SitofpConv
  final val InttoptrConv = 1 + PtrtointConv
  final val BitcastConv = 1 + InttoptrConv
  final val SSizeCastConv = 1 + BitcastConv
  final val ZSizeCastConv = 1 + SSizeCastConv

  // Definitions
  final val VarDefn = 1
  final val ConstDefn = 1 + VarDefn
  final val DeclareDefn = 1 + ConstDefn
  final val DefineDefn = 1 + DeclareDefn
  final val TraitDefn = 1 + DefineDefn
  final val ClassDefn = 1 + TraitDefn
  final val ModuleDefn = 1 + ClassDefn

  // Control-flow ops
  final val LabelInst = 1
  final val LetInst = 1 + LabelInst
  final val LetUnwindInst = 1 + LetInst
  final val RetInst = 1 + LetUnwindInst
  final val JumpInst = 1 + RetInst
  final val IfInst = 1 + JumpInst
  final val SwitchInst = 1 + IfInst
  final val ThrowInst = 1 + SwitchInst
  final val UnreachableInst = 1 + ThrowInst
  final val LinktimeIfInst = 1 + UnreachableInst

  // Globals
  final val NoneGlobal = 1
  final val TopGlobal = 1 + NoneGlobal
  final val MemberGlobal = 1 + TopGlobal

  // Sigs
  final val FieldSig = 1
  final val CtorSig = 1 + FieldSig
  final val MethodSig = 1 + CtorSig
  final val ProxySig = 1 + MethodSig
  final val ExternSig = 1 + ProxySig
  final val GeneratedSig = 1 + ExternSig
  final val DuplicateSig = 1 + GeneratedSig

  // Nexts
  final val NoneNext = 1
  final val UnwindNext = 1 + NoneNext
  final val CaseNext = 1 + UnwindNext
  final val LabelNext = 1 + CaseNext

  // Ops
  final val CallOp = 1
  final val LoadOp = 1 + CallOp
  final val LoadSyncOp = 1 + LoadOp
  final val StoreOp = 1 + LoadSyncOp
  final val StoreSyncOp = 1 + StoreOp
  final val ElemOp = 1 + StoreSyncOp
  final val ExtractOp = 1 + ElemOp
  final val InsertOp = 1 + ExtractOp
  final val StackallocOp = 1 + InsertOp
  final val BinOp = 1 + StackallocOp
  final val CompOp = 1 + BinOp
  final val ConvOp = 1 + CompOp
  final val ClassallocOp = 1 + ConvOp
  final val ClassallocZoneOp = 1 + ClassallocOp
  final val FieldOp = 1 + ClassallocZoneOp
  final val FieldloadOp = 1 + FieldOp
  final val FieldstoreOp = 1 + FieldloadOp
  final val MethodOp = 1 + FieldstoreOp
  final val ModuleOp = 1 + MethodOp
  final val AsOp = 1 + ModuleOp
  final val IsOp = 1 + AsOp
  final val CopyOp = 1 + IsOp
  final val SizeOfOp = 1 + CopyOp
  final val AlignmentOfOp = 1 + SizeOfOp
  final val BoxOp = 1 + AlignmentOfOp
  final val UnboxOp = 1 + BoxOp
  final val DynmethodOp = 1 + UnboxOp
  final val VarOp = 1 + DynmethodOp
  final val VarloadOp = 1 + VarOp
  final val VarstoreOp = 1 + VarloadOp
  final val ArrayallocOp = 1 + VarstoreOp
  final val ArrayallocZoneOp = 1 + ArrayallocOp
  final val ArrayloadOp = 1 + ArrayallocZoneOp
  final val ArraystoreOp = 1 + ArrayloadOp
  final val ArraylengthOp = 1 + ArraystoreOp
  final val FenceOp = 1 + ArraylengthOp

  // Types
  final val VarargType = 1
  final val BoolType = 1 + VarargType
  final val PtrType = 1 + BoolType
  final val CharType = 1 + PtrType
  final val ByteType = 1 + CharType
  final val ShortType = 1 + ByteType
  final val IntType = 1 + ShortType
  final val LongType = 1 + IntType
  final val FloatType = 1 + LongType
  final val DoubleType = 1 + FloatType
  final val ArrayValueType = 1 + DoubleType
  final val StructValueType = 1 + ArrayValueType
  final val FunctionType = 1 + StructValueType
  final val NullType = 1 + FunctionType
  final val NothingType = 1 + NullType
  final val VirtualType = 1 + NothingType
  final val VarType = 1 + VirtualType
  final val UnitType = 1 + VarType
  final val ArrayType = 1 + UnitType
  final val RefType = 1 + ArrayType
  final val SizeType = 1 + RefType

  // Values
  final val TrueVal = 1
  final val FalseVal = 1 + TrueVal
  final val NullVal = 1 + FalseVal
  final val ZeroVal = 1 + NullVal
  final val CharVal = 1 + ZeroVal
  final val ByteVal = 1 + CharVal
  final val ShortVal = 1 + ByteVal
  final val IntVal = 1 + ShortVal
  final val LongVal = 1 + IntVal
  final val FloatVal = 1 + LongVal
  final val DoubleVal = 1 + FloatVal
  final val StructValueVal = 1 + DoubleVal
  final val ArrayValueVal = 1 + StructValueVal
  final val ByteStringVal = 1 + ArrayValueVal
  final val LocalVal = 1 + ByteStringVal
  final val GlobalVal = 1 + LocalVal
  final val UnitVal = 1 + GlobalVal
  final val ConstVal = 1 + UnitVal
  final val StringVal = 1 + ConstVal
  final val VirtualVal = 1 + StringVal
  final val ClassOfVal = 1 + VirtualVal
  final val LinktimeConditionVal = 1 + ClassOfVal
  final val SizeVal = 1 + LinktimeConditionVal

  // Synchronization info
  final val Unordered = 1
  final val MonotonicOrder = 1 + Unordered
  final val AcquireOrder = 1 + MonotonicOrder
  final val ReleaseOrder = 1 + AcquireOrder
  final val AcqRelOrder = 1 + ReleaseOrder
  final val SeqCstOrder = 1 + AcqRelOrder
}
