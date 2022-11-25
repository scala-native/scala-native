package scala.scalanative.nscplugin

import dotty.tools.dotc.core
import core.Symbols._
import core.Symbols.{toClassDenot, toDenot}
import core.Contexts._
import core.Names._
import core.Types._
import core.Decorators._
import dotty.tools.backend.jvm.DottyPrimitives
import scala.annotation.{threadUnsafe => tu}
import dotty.tools.dotc.parsing.Scanners.IndentWidth.Run
import dotty.tools.dotc.core.Definitions
import NirGenUtil.ContextCached

object NirDefinitions {
  private val cached = ContextCached(NirDefinitions())
  def get(using Context): NirDefinitions = cached.get
}

// scalafmt: { maxColumn = 120}
final class NirDefinitions()(using ctx: Context) {
  // Native library
  // Annotations
  @tu lazy val AlwaysInlineType = requiredClassRef("scala.scalanative.annotation.alwaysinline")
  @tu lazy val NoOptimizeType = requiredClassRef("scala.scalanative.annotation.nooptimize")
  @tu lazy val NoSpecializeType = requiredClassRef("scala.scalanative.annotation.nospecialize")
  def AlwaysInlineClass(using Context) = AlwaysInlineType.symbol.asClass
  def NoOptimizeClass(using Context) = NoOptimizeType.symbol.asClass
  def NoSpecializeClass(using Context) = NoSpecializeType.symbol.asClass

  @tu lazy val StubType = requiredClassRef("scala.scalanative.annotation.stub")
  @tu lazy val NameType = requiredClassRef("scala.scalanative.unsafe.name")
  @tu lazy val LinkType = requiredClassRef("scala.scalanative.unsafe.link")
  @tu lazy val ExternType = requiredClassRef("scala.scalanative.unsafe.extern")
  @tu lazy val BlockingType = requiredClassRef("scala.scalanative.unsafe.blocking")
  @tu lazy val StructType = requiredClassRef("scala.scalanative.runtime.struct")
  @tu lazy val ResolvedAtLinktimeType = requiredClassRef("scala.scalanative.unsafe.resolvedAtLinktime")
  @tu lazy val ExportedType = requiredClassRef("scala.scalanative.unsafe.exported")
  @tu lazy val ExportAccessorsType = requiredClassRef("scala.scalanative.unsafe.exportAccessors")
  def StubClass(using Context) = StubType.symbol.asClass
  def NameClass(using Context) = NameType.symbol.asClass
  def LinkClass(using Context) = LinkType.symbol.asClass
  def ExternClass(using Context) = ExternType.symbol.asClass
  def BlockingClass(using Context) = BlockingType.symbol.asClass
  def StructClass(using Context) = StructType.symbol.asClass
  def ResolvedAtLinktimeClass(using Context) = ResolvedAtLinktimeType.symbol.asClass
  def ExportedClass(using Context) = ExportedType.symbol.asClass
  def ExportAccessorsClass(using Context) = ExportAccessorsType.symbol.asClass

  // Unsigned types
  @tu lazy val UByteClassVal = requiredClassRef("scala.scalanative.unsigned.UByte")
  @tu lazy val UShortType = requiredClassRef("scala.scalanative.unsigned.UShort")
  @tu lazy val UIntType = requiredClassRef("scala.scalanative.unsigned.UInt")
  @tu lazy val ULongType = requiredClassRef("scala.scalanative.unsigned.ULong")
  @tu lazy val SizeType = requiredClassRef("scala.scalanative.unsafe.Size")
  @tu lazy val USizeType = requiredClassRef("scala.scalanative.unsigned.USize")
  @tu lazy val RawSizeType = requiredClassRef("scala.scalanative.runtime.RawSize")
  def UByteClass(using Context) = UByteClassVal.symbol.asClass
  def UShortClass(using Context) = UShortType.symbol.asClass
  def UIntClass(using Context) = UIntType.symbol.asClass
  def ULongClass(using Context) = ULongType.symbol.asClass
  def SizeClass(using Context) = SizeType.symbol.asClass
  def USizeClass(using Context) = USizeType.symbol.asClass
  def RawSizeClass(using Context) = RawSizeType.symbol.asClass

  // Pointers
  @tu lazy val PtrType = requiredClassRef("scala.scalanative.unsafe.Ptr")
  @tu lazy val RawPtrType = requiredClassRef("scala.scalanative.runtime.RawPtr")
  def PtrClass(using Context) = PtrType.symbol.asClass
  def RawPtrClass(using Context) = RawPtrType.symbol.asClass

  private lazy val CFuncPtrNNames = (0 to 22).map("scala.scalanative.unsafe.CFuncPtr" + _)
  @tu lazy val CFuncPtrType = requiredClassRef("scala.scalanative.unsafe.CFuncPtr")
  @tu lazy val CFuncPtrNTypes = CFuncPtrNNames.map(requiredClassRef)
  @tu lazy val CFuncPtrNModuleTypes = CFuncPtrNNames.map(requiredModuleRef)
  @tu lazy val CFuncPtr_apply = CFuncPtrNClass.map(_.requiredMethod("apply"))
  @tu lazy val CFuncPtr_fromScalaFunction = CFuncPtrNModules.map(_.requiredMethod("fromScalaFunction"))

  def CFuncPtrClass(using Context) = CFuncPtrType.symbol.asClass
  def CFuncPtrNClass(using Context) = CFuncPtrNTypes.map(_.symbol.asClass)
  def CFuncPtrNModules(using Context) = CFuncPtrNModuleTypes.map(_.symbol)

  @tu lazy val CStructTypes = (0 to 22).map(n => requiredClassRef("scala.scalanative.unsafe.CStruct" + n))
  @tu lazy val CArrayType = requiredClassRef("scala.scalanative.unsafe.CArray")
  def CStructClasses(using Context) = CStructTypes.map(_.symbol.asClass)
  def CArrayClass(using Context) = CArrayType.symbol.asClass

  // Unsafe package
  @tu lazy val UnsafePackageVal = requiredPackage("scala.scalanative.unsafe")
  @tu lazy val UnsafePackage = UnsafePackageVal.moduleClass.asClass
  @tu lazy val UnsafePackage_externR = UnsafePackageVal.requiredMethodRef("extern")
  @tu lazy val UnsafePackage_resolvedR = UnsafePackageVal.requiredMethodRef("resolved")
  def UnsafePackage_extern(using Context) = UnsafePackage_externR.symbol
  def UnsafePackage_resolved(using Context) = UnsafePackage_resolvedR.symbol

  @tu lazy val CQuoteClass = UnsafePackage.requiredClass("CQuote")
  @tu lazy val CQuote_cR = CQuoteClass.requiredMethodRef("c")
  def CQuote_c(using Context) = CQuote_cR.symbol

  @tu lazy val NatModuleVal = requiredModuleRef("scala.scalanative.unsafe.Nat")
  @tu lazy val NatModule = NatModuleVal.classSymbol.asClass
  @tu lazy val NatBaseClasses = (0 to 9).map(n => NatModule.requiredClass(s"_$n"))
  @tu lazy val NatDigitClasses = (2 to 9).map(n => NatModule.requiredClass(s"Digit$n"))

  // Tags
  @tu lazy val TagModuleRef = requiredModuleRef("scala.scalanative.unsafe.Tag")
  @tu lazy val TagModule = TagModuleRef.symbol
  @tu lazy val UnsafeTag_materializeUnitTagR = TagModule.requiredMethodRef("materializeUnitTag")
  @tu lazy val UnsafeTag_materializeBooleanTagR = TagModule.requiredMethodRef("materializeBooleanTag")
  @tu lazy val UnsafeTag_materializeCharTagR = TagModule.requiredMethodRef("materializeCharTag")
  @tu lazy val UnsafeTag_materializeByteTagR = TagModule.requiredMethodRef("materializeByteTag")
  @tu lazy val UnsafeTag_materializeUByteTagR = TagModule.requiredMethodRef("materializeUByteTag")
  @tu lazy val UnsafeTag_materializeShortTagR = TagModule.requiredMethodRef("materializeShortTag")
  @tu lazy val UnsafeTag_materializeUShortTagR = TagModule.requiredMethodRef("materializeUShortTag")
  @tu lazy val UnsafeTag_materializeIntTagR = TagModule.requiredMethodRef("materializeIntTag")
  @tu lazy val UnsafeTag_materializeUIntTagR = TagModule.requiredMethodRef("materializeUIntTag")
  @tu lazy val UnsafeTag_materializeLongTagR = TagModule.requiredMethodRef("materializeLongTag")
  @tu lazy val UnsafeTag_materializeULongTagR = TagModule.requiredMethodRef("materializeULongTag")
  @tu lazy val UnsafeTag_materializeFloatTagR = TagModule.requiredMethodRef("materializeFloatTag")
  @tu lazy val UnsafeTag_materializeDoubleTagR = TagModule.requiredMethodRef("materializeDoubleTag")
  @tu lazy val UnsafeTag_materializePtrTagR = TagModule.requiredMethodRef("materializePtrTag")
  @tu lazy val UnsafeTag_materializeClassTagR = TagModule.requiredMethodRef("materializeClassTag")
  @tu lazy val UnsafeTag_materializeCArrayTagR = TagModule.requiredMethodRef("materializeCArrayTag")
  @tu lazy val UnsafeTag_materializeNatBaseTagsR =
    (0 to 9).map(n => TagModule.requiredMethodRef(s"materializeNat${n}Tag"))
  @tu lazy val UnsafeTag_materializeNatDigitTagsR =
    (2 to 9).map(n => TagModule.requiredMethodRef(s"materializeNatDigit${n}Tag"))
  @tu lazy val UnsafeTag_materializeCStructTagsR =
    (0 to 22).map(n => TagModule.requiredMethodRef(s"materializeCStruct${n}Tag"))
  @tu lazy val UnsafeTag_materializeSizeTagR = TagModule.requiredMethodRef("materializeSizeTag")
  @tu lazy val UnsafeTag_materializeUSizeTagR = TagModule.requiredMethodRef("materializeUSizeTag")
  def UnsafeTag_materializeUnitTag(using Context) = UnsafeTag_materializeUnitTagR.symbol
  def UnsafeTag_materializeBooleanTag(using Context) = UnsafeTag_materializeBooleanTagR.symbol
  def UnsafeTag_materializeCharTag(using Context) = UnsafeTag_materializeCharTagR.symbol
  def UnsafeTag_materializeByteTag(using Context) = UnsafeTag_materializeByteTagR.symbol
  def UnsafeTag_materializeUByteTag(using Context) = UnsafeTag_materializeUByteTagR.symbol
  def UnsafeTag_materializeShortTag(using Context) = UnsafeTag_materializeShortTagR.symbol
  def UnsafeTag_materializeUShortTag(using Context) = UnsafeTag_materializeUShortTagR.symbol
  def UnsafeTag_materializeIntTag(using Context) = UnsafeTag_materializeIntTagR.symbol
  def UnsafeTag_materializeUIntTag(using Context) = UnsafeTag_materializeUIntTagR.symbol
  def UnsafeTag_materializeLongTag(using Context) = UnsafeTag_materializeLongTagR.symbol
  def UnsafeTag_materializeULongTag(using Context) = UnsafeTag_materializeULongTagR.symbol
  def UnsafeTag_materializeFloatTag(using Context) = UnsafeTag_materializeFloatTagR.symbol
  def UnsafeTag_materializeDoubleTag(using Context) = UnsafeTag_materializeDoubleTagR.symbol
  def UnsafeTag_materializePtrTag(using Context) = UnsafeTag_materializePtrTagR.symbol
  def UnsafeTag_materializeClassTag(using Context) = UnsafeTag_materializeClassTagR.symbol
  def UnsafeTag_materializeCArrayTag(using Context) = UnsafeTag_materializeCArrayTagR.symbol
  def UnsafeTag_materializeNatBaseTags(using Context) = UnsafeTag_materializeNatBaseTagsR.map(_.symbol)
  def UnsafeTag_materializeNatDigitTags(using Context) = UnsafeTag_materializeNatDigitTagsR.map(_.symbol)
  def UnsafeTag_materializeCStructTags(using Context) = UnsafeTag_materializeCStructTagsR.map(_.symbol)
  def UnsafeTag_materializeSizeTag(using Context) = UnsafeTag_materializeSizeTagR.symbol
  def UnsafeTag_materializeUSizeTag(using Context) = UnsafeTag_materializeUSizeTagR.symbol

  // Native runtime package
  @tu lazy val RuntimePackageVal = requiredModuleRef("scala.scalanative.runtime.package")
  @tu lazy val RuntimePackageClass = RuntimePackageVal.classSymbol.asClass
  @tu lazy val RuntimePackage_enterMonitorR = RuntimePackageClass.requiredMethodRef("enterMonitor")
  @tu lazy val RuntimePackage_exitMonitorR = RuntimePackageClass.requiredMethodRef("exitMonitor")
  def RuntimePackage_enterMonitor(using Context) = RuntimePackage_enterMonitorR.symbol
  def RuntimePackage_exitMonitor(using Context) = RuntimePackage_exitMonitorR.symbol

  // Runtime intriniscs
  @tu lazy val IntrinsicsModuleType = requiredModuleRef("scala.scalanative.runtime.Intrinsics")
  @tu lazy val IntrinsicsModule = IntrinsicsModuleType.classSymbol.asClass
  @tu lazy val Intrinsics_divUIntR = IntrinsicsModule.requiredMethodRef("divUInt")
  @tu lazy val Intrinsics_divULongR = IntrinsicsModule.requiredMethodRef("divULong")
  @tu lazy val Intrinsics_remUIntR = IntrinsicsModule.requiredMethodRef("remUInt")
  @tu lazy val Intrinsics_remULongR = IntrinsicsModule.requiredMethodRef("remULong")
  @tu lazy val Intrinsics_byteToUIntR = IntrinsicsModule.requiredMethodRef("byteToUInt")
  @tu lazy val Intrinsics_byteToULongR = IntrinsicsModule.requiredMethodRef("byteToULong")
  @tu lazy val Intrinsics_shortToUIntR = IntrinsicsModule.requiredMethodRef("shortToUInt")
  @tu lazy val Intrinsics_shortToULongR = IntrinsicsModule.requiredMethodRef("shortToULong")
  @tu lazy val Intrinsics_intToULongR = IntrinsicsModule.requiredMethodRef("intToULong")
  @tu lazy val Intrinsics_uintToFloatR = IntrinsicsModule.requiredMethodRef("uintToFloat")
  @tu lazy val Intrinsics_ulongToFloatR = IntrinsicsModule.requiredMethodRef("ulongToFloat")
  @tu lazy val Intrinsics_uintToDoubleR = IntrinsicsModule.requiredMethodRef("uintToDouble")
  @tu lazy val Intrinsics_ulongToDoubleR = IntrinsicsModule.requiredMethodRef("ulongToDouble")
  @tu lazy val Intrinsics_loadBoolR = IntrinsicsModule.requiredMethodRef("loadBoolean")
  @tu lazy val Intrinsics_loadCharR = IntrinsicsModule.requiredMethodRef("loadChar")
  @tu lazy val Intrinsics_loadByteR = IntrinsicsModule.requiredMethodRef("loadByte")
  @tu lazy val Intrinsics_loadShortR = IntrinsicsModule.requiredMethodRef("loadShort")
  @tu lazy val Intrinsics_loadIntR = IntrinsicsModule.requiredMethodRef("loadInt")
  @tu lazy val Intrinsics_loadLongR = IntrinsicsModule.requiredMethodRef("loadLong")
  @tu lazy val Intrinsics_loadFloatR = IntrinsicsModule.requiredMethodRef("loadFloat")
  @tu lazy val Intrinsics_loadDoubleR = IntrinsicsModule.requiredMethodRef("loadDouble")
  @tu lazy val Intrinsics_loadRawPtrR = IntrinsicsModule.requiredMethodRef("loadRawPtr")
  @tu lazy val Intrinsics_loadRawSizeR = IntrinsicsModule.requiredMethodRef("loadRawSize")
  @tu lazy val Intrinsics_loadObjectR = IntrinsicsModule.requiredMethodRef("loadObject")
  @tu lazy val Intrinsics_storeBoolR = IntrinsicsModule.requiredMethodRef("storeBoolean")
  @tu lazy val Intrinsics_storeCharR = IntrinsicsModule.requiredMethodRef("storeChar")
  @tu lazy val Intrinsics_storeByteR = IntrinsicsModule.requiredMethodRef("storeByte")
  @tu lazy val Intrinsics_storeShortR = IntrinsicsModule.requiredMethodRef("storeShort")
  @tu lazy val Intrinsics_storeIntR = IntrinsicsModule.requiredMethodRef("storeInt")
  @tu lazy val Intrinsics_storeLongR = IntrinsicsModule.requiredMethodRef("storeLong")
  @tu lazy val Intrinsics_storeFloatR = IntrinsicsModule.requiredMethodRef("storeFloat")
  @tu lazy val Intrinsics_storeDoubleR = IntrinsicsModule.requiredMethodRef("storeDouble")
  @tu lazy val Intrinsics_storeRawPtrR = IntrinsicsModule.requiredMethodRef("storeRawPtr")
  @tu lazy val Intrinsics_storeRawSizeR = IntrinsicsModule.requiredMethodRef("storeRawSize")
  @tu lazy val Intrinsics_storeObjectR = IntrinsicsModule.requiredMethodRef("storeObject")
  @tu lazy val Intrinsics_elemRawPtrR = IntrinsicsModule.requiredMethodRef("elemRawPtr")
  @tu lazy val Intrinsics_castRawPtrToObjectR = IntrinsicsModule.requiredMethodRef("castRawPtrToObject")
  @tu lazy val Intrinsics_castObjectToRawPtrR = IntrinsicsModule.requiredMethodRef("castObjectToRawPtr")
  @tu lazy val Intrinsics_castIntToFloatR = IntrinsicsModule.requiredMethodRef("castIntToFloat")
  @tu lazy val Intrinsics_castFloatToIntR = IntrinsicsModule.requiredMethodRef("castFloatToInt")
  @tu lazy val Intrinsics_castLongToDoubleR = IntrinsicsModule.requiredMethodRef("castLongToDouble")
  @tu lazy val Intrinsics_castDoubleToLongR = IntrinsicsModule.requiredMethodRef("castDoubleToLong")
  @tu lazy val Intrinsics_castRawPtrToIntR = IntrinsicsModule.requiredMethodRef("castRawPtrToInt")
  @tu lazy val Intrinsics_castRawPtrToLongR = IntrinsicsModule.requiredMethodRef("castRawPtrToLong")
  @tu lazy val Intrinsics_castIntToRawPtrR = IntrinsicsModule.requiredMethodRef("castIntToRawPtr")
  @tu lazy val Intrinsics_castLongToRawPtrR = IntrinsicsModule.requiredMethodRef("castLongToRawPtr")
  @tu lazy val Intrinsics_castRawSizeToIntR = IntrinsicsModule.requiredMethodRef("castRawSizeToInt")
  @tu lazy val Intrinsics_castRawSizeToLongR = IntrinsicsModule.requiredMethodRef("castRawSizeToLong")
  @tu lazy val Intrinsics_castRawSizeToLongUnsignedR = IntrinsicsModule.requiredMethodRef("castRawSizeToLongUnsigned")
  @tu lazy val Intrinsics_castIntToRawSizeR = IntrinsicsModule.requiredMethodRef("castIntToRawSize")
  @tu lazy val Intrinsics_castIntToRawSizeUnsignedR = IntrinsicsModule.requiredMethodRef("castIntToRawSizeUnsigned")
  @tu lazy val Intrinsics_castLongToRawSizeR = IntrinsicsModule.requiredMethodRef("castLongToRawSize")
  @tu lazy val Intrinsics_stackallocR = IntrinsicsModule.requiredMethodRef("stackalloc")
  @tu lazy val Intrinsics_classFieldRawPtrR = IntrinsicsModule.requiredMethodRef("classFieldRawPtr")
  @tu lazy val Intrinsics_sizeOfAlts = IntrinsicsModule.info.member(termName("sizeOf")).alternatives
  @tu lazy val Intrinsics_sizeOfR = Intrinsics_sizeOfAlts.find(_.info.paramInfoss.flatten.nonEmpty).get
  @tu lazy val Intrinsics_sizeOfTypeR = Intrinsics_sizeOfAlts.find(_.info.paramInfoss.flatten.isEmpty).get

  def Intrinsics_divUInt(using Context) = Intrinsics_divUIntR.symbol
  def Intrinsics_divULong(using Context) = Intrinsics_divULongR.symbol
  def Intrinsics_remUInt(using Context) = Intrinsics_remUIntR.symbol
  def Intrinsics_remULong(using Context) = Intrinsics_remULongR.symbol
  def Intrinsics_byteToUInt(using Context) = Intrinsics_byteToUIntR.symbol
  def Intrinsics_byteToULong(using Context) = Intrinsics_byteToULongR.symbol
  def Intrinsics_shortToUInt(using Context) = Intrinsics_shortToUIntR.symbol
  def Intrinsics_shortToULong(using Context) = Intrinsics_shortToULongR.symbol
  def Intrinsics_intToULong(using Context) = Intrinsics_intToULongR.symbol
  def Intrinsics_uintToFloat(using Context) = Intrinsics_uintToFloatR.symbol
  def Intrinsics_ulongToFloat(using Context) = Intrinsics_ulongToFloatR.symbol
  def Intrinsics_uintToDouble(using Context) = Intrinsics_uintToDoubleR.symbol
  def Intrinsics_ulongToDouble(using Context) = Intrinsics_ulongToDoubleR.symbol
  def Intrinsics_loadBool(using Context) = Intrinsics_loadBoolR.symbol
  def Intrinsics_loadChar(using Context) = Intrinsics_loadCharR.symbol
  def Intrinsics_loadByte(using Context) = Intrinsics_loadByteR.symbol
  def Intrinsics_loadShort(using Context) = Intrinsics_loadShortR.symbol
  def Intrinsics_loadInt(using Context) = Intrinsics_loadIntR.symbol
  def Intrinsics_loadLong(using Context) = Intrinsics_loadLongR.symbol
  def Intrinsics_loadFloat(using Context) = Intrinsics_loadFloatR.symbol
  def Intrinsics_loadDouble(using Context) = Intrinsics_loadDoubleR.symbol
  def Intrinsics_loadRawPtr(using Context) = Intrinsics_loadRawPtrR.symbol
  def Intrinsics_loadRawSize(using Context) = Intrinsics_loadRawSizeR.symbol
  def Intrinsics_loadObject(using Context) = Intrinsics_loadObjectR.symbol
  def Intrinsics_storeBool(using Context) = Intrinsics_storeBoolR.symbol
  def Intrinsics_storeChar(using Context) = Intrinsics_storeCharR.symbol
  def Intrinsics_storeByte(using Context) = Intrinsics_storeByteR.symbol
  def Intrinsics_storeShort(using Context) = Intrinsics_storeShortR.symbol
  def Intrinsics_storeInt(using Context) = Intrinsics_storeIntR.symbol
  def Intrinsics_storeLong(using Context) = Intrinsics_storeLongR.symbol
  def Intrinsics_storeFloat(using Context) = Intrinsics_storeFloatR.symbol
  def Intrinsics_storeDouble(using Context) = Intrinsics_storeDoubleR.symbol
  def Intrinsics_storeRawPtr(using Context) = Intrinsics_storeRawPtrR.symbol
  def Intrinsics_storeRawSize(using Context) = Intrinsics_storeRawSizeR.symbol
  def Intrinsics_storeObject(using Context) = Intrinsics_storeObjectR.symbol
  def Intrinsics_elemRawPtr(using Context) = Intrinsics_elemRawPtrR.symbol
  def Intrinsics_castRawPtrToObject(using Context) = Intrinsics_castRawPtrToObjectR.symbol
  def Intrinsics_castObjectToRawPtr(using Context) = Intrinsics_castObjectToRawPtrR.symbol
  def Intrinsics_castIntToFloat(using Context) = Intrinsics_castIntToFloatR.symbol
  def Intrinsics_castFloatToInt(using Context) = Intrinsics_castFloatToIntR.symbol
  def Intrinsics_castLongToDouble(using Context) = Intrinsics_castLongToDoubleR.symbol
  def Intrinsics_castDoubleToLong(using Context) = Intrinsics_castDoubleToLongR.symbol
  def Intrinsics_castRawPtrToInt(using Context) = Intrinsics_castRawPtrToIntR.symbol
  def Intrinsics_castRawPtrToLong(using Context) = Intrinsics_castRawPtrToLongR.symbol
  def Intrinsics_castIntToRawPtr(using Context) = Intrinsics_castIntToRawPtrR.symbol
  def Intrinsics_castLongToRawPtr(using Context) = Intrinsics_castLongToRawPtrR.symbol
  def Intrinsics_castRawSizeToInt(using Context) = Intrinsics_castRawSizeToIntR.symbol
  def Intrinsics_castRawSizeToLong(using Context) = Intrinsics_castRawSizeToLongR.symbol
  def Intrinsics_castRawSizeToLongUnsigned(using Context) = Intrinsics_castRawSizeToLongUnsignedR.symbol
  def Intrinsics_castIntToRawSize(using Context) = Intrinsics_castIntToRawSizeR.symbol
  def Intrinsics_castIntToRawSizeUnsigned(using Context) = Intrinsics_castIntToRawSizeUnsignedR.symbol
  def Intrinsics_castLongToRawSize(using Context) = Intrinsics_castLongToRawSizeR.symbol
  def Intrinsics_stackalloc(using Context) = Intrinsics_stackallocR.symbol
  def Intrinsics_classFieldRawPtr(using Context) = Intrinsics_classFieldRawPtrR.symbol
  def Intrinsics_sizeOf(using Context) = Intrinsics_sizeOfR.symbol
  def Intrinsics_sizeOfType(using Context) = Intrinsics_sizeOfTypeR.symbol

  // Runtime types
  @tu lazy val RuntimePrimitive: Map[Char, Symbol] = Map(
    'B' -> requiredClass("scala.scalanative.runtime.PrimitiveBoolean"),
    'C' -> requiredClass("scala.scalanative.runtime.PrimitiveChar"),
    'Z' -> requiredClass("scala.scalanative.runtime.PrimitiveByte"),
    'S' -> requiredClass("scala.scalanative.runtime.PrimitiveShort"),
    'I' -> requiredClass("scala.scalanative.runtime.PrimitiveInt"),
    'L' -> requiredClass("scala.scalanative.runtime.PrimitiveLong"),
    'F' -> requiredClass("scala.scalanative.runtime.PrimitiveFloat"),
    'D' -> requiredClass("scala.scalanative.runtime.PrimitiveDouble"),
    'U' -> requiredClass("scala.scalanative.runtime.PrimitiveUnit")
  )

  @tu lazy val RuntimeArrayClass: Map[Char, Symbol] = Map(
    'B' -> requiredClass("scala.scalanative.runtime.BooleanArray"),
    'C' -> requiredClass("scala.scalanative.runtime.CharArray"),
    'Z' -> requiredClass("scala.scalanative.runtime.ByteArray"),
    'S' -> requiredClass("scala.scalanative.runtime.ShortArray"),
    'I' -> requiredClass("scala.scalanative.runtime.IntArray"),
    'L' -> requiredClass("scala.scalanative.runtime.LongArray"),
    'F' -> requiredClass("scala.scalanative.runtime.FloatArray"),
    'D' -> requiredClass("scala.scalanative.runtime.DoubleArray"),
    'O' -> requiredClass("scala.scalanative.runtime.ObjectArray")
  )

  private def mapValues[K, V1, V2](in: Map[K, V1])(fn: V1 => V2): Map[K, V2] =
    in.map { (key, value) =>
      (key, fn(value))
    }

  // Runtime array
  @tu lazy val RuntimeArrayModule = mapValues(RuntimeArrayClass)(_.companionModule)
  @tu lazy val RuntimeArray_alloc = mapValues(RuntimeArrayModule)(_.requiredMethod("alloc"))
  @tu lazy val RuntimeArray_apply = mapValues(RuntimeArrayClass)(_.requiredMethod("apply"))
  @tu lazy val RuntimeArray_update = mapValues(RuntimeArrayClass)(_.requiredMethod("update"))
  @tu lazy val RuntimeArray_length = mapValues(RuntimeArrayClass)(_.requiredMethod("length"))
  @tu lazy val RuntimeArray_clone = mapValues(RuntimeArrayClass)(_.requiredMethod("clone"))

  // Scala Native runtime boxes
  @tu lazy val RuntimeBoxesModuleVal = requiredModuleRef("scala.scalanative.runtime.Boxes")
  @tu lazy val RuntimeBoxesModule = RuntimeBoxesModuleVal.symbol.asClass
  @tu lazy val BoxUnsignedMethod = Map[Symbol, Symbol](
    UByteClass -> RuntimeBoxesModule.requiredMethod("boxToUByte"),
    UShortClass -> RuntimeBoxesModule.requiredMethod("boxToUShort"),
    UIntClass -> RuntimeBoxesModule.requiredMethod("boxToUInt"),
    ULongClass -> RuntimeBoxesModule.requiredMethod("boxToULong"),
    USizeClass -> RuntimeBoxesModule.requiredMethod("boxToUSize")
  )
  @tu lazy val UnboxUnsignedMethod = Map[Symbol, Symbol](
    UByteClass -> RuntimeBoxesModule.requiredMethod("unboxToUByte"),
    UShortClass -> RuntimeBoxesModule.requiredMethod("unboxToUShort"),
    UIntClass -> RuntimeBoxesModule.requiredMethod("unboxToUInt"),
    ULongClass -> RuntimeBoxesModule.requiredMethod("unboxToULong"),
    USizeClass -> RuntimeBoxesModule.requiredMethod("unboxToUSize")
  )

  // Scala boxes
  @tu lazy val BoxMethod = Map[Char, Symbol](
    'B' -> defn.BoxesRunTimeModule.requiredMethod("boxToBoolean"),
    'C' -> defn.BoxesRunTimeModule.requiredMethod("boxToCharacter"),
    'Z' -> defn.BoxesRunTimeModule.requiredMethod("boxToByte"),
    'S' -> defn.BoxesRunTimeModule.requiredMethod("boxToShort"),
    'I' -> defn.BoxesRunTimeModule.requiredMethod("boxToInteger"),
    'L' -> defn.BoxesRunTimeModule.requiredMethod("boxToLong"),
    'F' -> defn.BoxesRunTimeModule.requiredMethod("boxToFloat"),
    'D' -> defn.BoxesRunTimeModule.requiredMethod("boxToDouble")
  )

  @tu lazy val UnboxMethod = Map[Char, Symbol](
    'B' -> defn.BoxesRunTimeModule.requiredMethod("unboxToBoolean"),
    'C' -> defn.BoxesRunTimeModule.requiredMethod("unboxToChar"),
    'Z' -> defn.BoxesRunTimeModule.requiredMethod("unboxToByte"),
    'S' -> defn.BoxesRunTimeModule.requiredMethod("unboxToShort"),
    'I' -> defn.BoxesRunTimeModule.requiredMethod("unboxToInt"),
    'L' -> defn.BoxesRunTimeModule.requiredMethod("unboxToLong"),
    'F' -> defn.BoxesRunTimeModule.requiredMethod("unboxToFloat"),
    'D' -> defn.BoxesRunTimeModule.requiredMethod("unboxToDouble")
  )

  // Scala Native reflect
  @tu lazy val ReflectModuleVal = requiredModuleRef("scala.scalanative.reflect.Reflect")
  @tu lazy val ReflectModule = ReflectModuleVal.symbol.moduleClass.asClass
  @tu lazy val Reflect_registerLoadableModuleClassR =
    ReflectModule.requiredMethodRef("registerLoadableModuleClass")
  @tu lazy val Reflect_registerInstantiatableClassR =
    ReflectModule.requiredMethodRef("registerInstantiatableClass")
  def Reflect_registerLoadableModuleClass(using Context) = Reflect_registerLoadableModuleClassR.symbol
  def Reflect_registerInstantiatableClass(using Context) = Reflect_registerInstantiatableClassR.symbol

  @tu lazy val EnableReflectiveInstantiationAnnotationType =
    requiredClassRef("scala.scalanative.reflect.annotation.EnableReflectiveInstantiation")
  def EnableReflectiveInstantiationAnnotationClass = EnableReflectiveInstantiationAnnotationType.symbol.asClass

  // Scala reflect
  @tu lazy val ClassTagModuleVal = requiredModuleRef("scala.reflect.ClassTag")
  @tu lazy val ClasstagModule = ClassTagModuleVal.symbol.moduleClass
  @tu lazy val ClassTagApply = ClasstagModule.requiredMethod("apply")
  @tu lazy val ByteClassTag = ClasstagModule.requiredMethod("Byte")
  @tu lazy val ShortClassTag = ClasstagModule.requiredMethod("Short")
  @tu lazy val CharClassTag = ClasstagModule.requiredMethod("Char")
  @tu lazy val IntClassTag = ClasstagModule.requiredMethod("Int")
  @tu lazy val LongClassTag = ClasstagModule.requiredMethod("Long")
  @tu lazy val FloatClassTag = ClasstagModule.requiredMethod("Float")
  @tu lazy val DoubleClassTag = ClasstagModule.requiredMethod("Double")
  @tu lazy val BooleanClassTag = ClasstagModule.requiredMethod("Boolean")
  @tu lazy val UnitClassTag = ClasstagModule.requiredMethod("Unit")
  @tu lazy val AnyClassTag = ClasstagModule.requiredMethod("Any")
  @tu lazy val ObjectClassTag = ClasstagModule.requiredMethod("Object")
  @tu lazy val AnyValClassTag = ClasstagModule.requiredMethod("AnyVal")
  @tu lazy val AnyRefClassTag = ClasstagModule.requiredMethod("AnyRef")
  @tu lazy val NothingClassTag = ClasstagModule.requiredMethod("Nothing")
  @tu lazy val NullClassTag = ClasstagModule.requiredMethod("Null")

  @tu lazy val ReflectSelectableType: TypeRef = requiredClassRef("scala.reflect.Selectable")
  @tu lazy val ReflectSelectable_selectDynamicR = ReflectSelectableClass.requiredMethodRef("selectDynamic")
  @tu lazy val ReflectSelectable_applyDynamicR = ReflectSelectableClass.requiredMethodRef("applyDynamic")
  @tu lazy val ReflectSelectable_selectedValueR = ReflectSelectableClass.requiredMethodRef("selectedValue")
  def ReflectSelectableClass(using Context) = ReflectSelectableType.symbol.asClass
  def ReflectSelectable_selectDynamic(using Context) = ReflectSelectable_selectDynamicR.symbol
  def ReflectSelectable_applyDynamic(using Context) = ReflectSelectable_applyDynamicR.symbol
  def ReflectSelectable_selectedValue(using Context) = ReflectSelectable_selectedValueR.symbol

  // Java library
  @tu lazy val NObjectType = requiredClassRef("java.lang._Object")
  def NObjectClass(using Context) = NObjectType.symbol.asClass
  @tu lazy val NObject_initR = NObjectClass.requiredMethodRef("<init>")
  @tu lazy val NObject_hashCodeR = NObjectClass.requiredMethodRef("__scala_##")
  @tu lazy val NObject_equalsR = NObjectClass.requiredMethodRef("__scala_==")
  def NObject_init(using Context) = NObject_initR.symbol
  def NObject_hashCode(using Context) = NObject_hashCodeR.symbol
  def NObject_equals(using Context) = NObject_equalsR.symbol

  @tu lazy val NStringType = requiredClassRef("java.lang._String")
  @tu lazy val NStringModuleType = requiredModuleRef("java.lang._String")
  def NStringClass(using Context) = NStringType.symbol.asClass
  def NStringModule(using Context) = NStringModuleType.symbol.asClass

  @tu lazy val String_concatR = defn.StringClass.requiredMethodRef("concat")
  def String_concat(using Context) = String_concatR.symbol

  // Scala library & runtime
  @tu lazy val InlineType = requiredClassRef("scala.inline")
  @tu lazy val NoInlineType = requiredClassRef("scala.noinline")
  def InlineClass(using Context) = InlineType.symbol.asClass
  def NoInlineClass(using Context) = NoInlineType.symbol.asClass

  @tu lazy val HashMethods = Seq(
    defn.BoxesRunTimeModule.requiredMethod("hashFromObject"),
    defn.BoxesRunTimeModule.requiredMethod("hashFromNumber"),
    defn.BoxesRunTimeModule.requiredMethod("hashFromFloat"),
    defn.BoxesRunTimeModule.requiredMethod("hashFromDouble"),
    defn.BoxesRunTimeModule.requiredMethod("hashFromLong")
  ) ++ defn.ScalaRuntimeModule.requiredMethod("hash").alternatives

}
