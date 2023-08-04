package scala.scalanative.nscplugin

import dotty.tools.dotc.core
import core.Symbols._
import core.Symbols.{toClassDenot, toDenot}
import core.Contexts._
import core.Names._
import core.Types._
import scala.annotation.{threadUnsafe => tu}
import dotty.tools.dotc.util.Property.StickyKey
import NirGenUtil.ContextCached

object NirDefinitions {
  private val cached = ContextCached(NirDefinitions())
  def get(using Context): NirDefinitions = cached.get
  object NonErasedType extends StickyKey[Type]
  object NonErasedTypes extends StickyKey[List[Type]]
}

// scalafmt: { maxColumn = 120}
final class NirDefinitions()(using ctx: Context) {
  // Native library
  // Annotations
  @tu lazy val AlwaysInlineClass = requiredClass("scala.scalanative.annotation.alwaysinline")
  @tu lazy val NoOptimizeClass = requiredClass("scala.scalanative.annotation.nooptimize")
  @tu lazy val NoSpecializeClass = requiredClass("scala.scalanative.annotation.nospecialize")

  @tu lazy val StubClass = requiredClass("scala.scalanative.annotation.stub")
  @tu lazy val AlignClass = requiredClass("scala.scalanative.annotation.align")
  @tu lazy val NameClass = requiredClass("scala.scalanative.unsafe.name")
  @tu lazy val LinkClass = requiredClass("scala.scalanative.unsafe.link")
  @tu lazy val ExternClass = requiredClass("scala.scalanative.unsafe.extern")
  @tu lazy val BlockingClass = requiredClass("scala.scalanative.unsafe.blocking")
  @tu lazy val StructClass = requiredClass("scala.scalanative.runtime.struct")
  @tu lazy val ResolvedAtLinktimeClass = requiredClass("scala.scalanative.unsafe.resolvedAtLinktime")
  @tu lazy val ExportedClass = requiredClass("scala.scalanative.unsafe.exported")
  @tu lazy val ExportAccessorsClass = requiredClass("scala.scalanative.unsafe.exportAccessors")

  // Unsigned types
  @tu lazy val UByteClass = requiredClass("scala.scalanative.unsigned.UByte")
  @tu lazy val UShortClass = requiredClass("scala.scalanative.unsigned.UShort")
  @tu lazy val UIntClass = requiredClass("scala.scalanative.unsigned.UInt")
  @tu lazy val ULongClass = requiredClass("scala.scalanative.unsigned.ULong")
  @tu lazy val SizeClass = requiredClass("scala.scalanative.unsafe.Size")
  @tu lazy val USizeClass = requiredClass("scala.scalanative.unsigned.USize")
  @tu lazy val RawSizeClass = requiredClass("scala.scalanative.runtime.RawSize")

  @tu lazy val USizeModule = requiredModule("scala.scalanative.unsigned.USize")
  @tu lazy val USize_fromUByte = USizeModule.requiredMethod("fromUByte")
  @tu lazy val USize_fromUShort = USizeModule.requiredMethod("fromUShort")
  @tu lazy val USize_fromUInt = USizeModule.requiredMethod("fromUInt")

  @tu lazy val SizeModule = requiredModule("scala.scalanative.unsafe.Size")
  @tu lazy val Size_fromByte = SizeModule.requiredMethod("fromByte")
  @tu lazy val Size_fromShort = SizeModule.requiredMethod("fromShort")
  @tu lazy val Size_fromInt = SizeModule.requiredMethod("fromInt")

  // Pointers
  @tu lazy val PtrClass = requiredClass("scala.scalanative.unsafe.Ptr")
  @tu lazy val RawPtrClass = requiredClass("scala.scalanative.runtime.RawPtr")

  private lazy val CFuncPtrNNames = (0 to 22).map("scala.scalanative.unsafe.CFuncPtr" + _)
  @tu lazy val CFuncPtrClass = requiredClass("scala.scalanative.unsafe.CFuncPtr")
  @tu lazy val CFuncPtrNClass = CFuncPtrNNames.map(requiredClass)
  @tu lazy val CFuncPtrNModules = CFuncPtrNNames.map(requiredModule)
  @tu lazy val CFuncPtr_apply = CFuncPtrNClass.map(_.requiredMethod("apply"))
  @tu lazy val CFuncPtr_fromScalaFunction = CFuncPtrNModules.map(_.requiredMethod("fromScalaFunction"))

  @tu lazy val CStructClasses = (0 to 22).map(n => requiredClass("scala.scalanative.unsafe.CStruct" + n))
  @tu lazy val CArrayClass = requiredClass("scala.scalanative.unsafe.CArray")

  // Unsafe package
  @tu lazy val UnsafePackageVal = requiredPackage("scala.scalanative.unsafe")
  @tu lazy val UnsafePackage = UnsafePackageVal.moduleClass.asClass
  @tu lazy val UnsafePackage_extern = UnsafePackageVal.requiredMethod("extern")
  @tu lazy val UnsafePackage_resolved = UnsafePackageVal.requiredMethod("resolved")

  @tu lazy val CQuoteClass = UnsafePackage.requiredClass("CQuote")
  @tu lazy val CQuote_c = CQuoteClass.requiredMethod("c")

  @tu lazy val NatModule = requiredModule("scala.scalanative.unsafe.Nat")
  @tu lazy val NatBaseClasses = (0 to 9).map(n => NatModule.requiredClass(s"_$n"))
  @tu lazy val NatDigitClasses = (2 to 9).map(n => NatModule.requiredClass(s"Digit$n"))

  // Native runtime package
  @tu lazy val RuntimePackageClass = requiredModule("scala.scalanative.runtime.package")
  @tu lazy val RuntimePackage_enterMonitor = RuntimePackageClass.requiredMethod("enterMonitor")
  @tu lazy val RuntimePackage_exitMonitor = RuntimePackageClass.requiredMethod("exitMonitor")
  @tu lazy val RuntimePackage_fromRawSize = RuntimePackageClass.requiredMethod("fromRawSize")
  @tu lazy val RuntimePackage_fromRawUSize = RuntimePackageClass.requiredMethod("fromRawUSize")

  @tu lazy val RuntimePackage_toRawSizeAlts = RuntimePackageClass.info
    .member(termName("toRawSize"))
    .alternatives
    .map(_.symbol)
    .ensuring(_.size == 2)

  @tu lazy val RuntimeSafeZoneAllocatorModuleRef = requiredModuleRef("scala.scalanative.runtime.SafeZoneAllocator")
  @tu lazy val RuntimeSafeZoneAllocatorModule = RuntimeSafeZoneAllocatorModuleRef.symbol
  @tu lazy val RuntimeSafeZoneAllocator_allocate =
    try Some(RuntimeSafeZoneAllocatorModule.requiredMethod("allocate"))
    catch { case _: dotty.tools.dotc.core.TypeError => None }

  // Runtime intriniscs
  @tu lazy val IntrinsicsModule = requiredModule("scala.scalanative.runtime.Intrinsics")
  @tu lazy val IntrinsicsInternalModule = requiredModule("scala.scalanative.runtime.Intrinsics.internal")
  @tu lazy val Intrinsics_divUInt = IntrinsicsModule.requiredMethod("divUInt")
  @tu lazy val Intrinsics_divULong = IntrinsicsModule.requiredMethod("divULong")
  @tu lazy val Intrinsics_remUInt = IntrinsicsModule.requiredMethod("remUInt")
  @tu lazy val Intrinsics_remULong = IntrinsicsModule.requiredMethod("remULong")
  @tu lazy val Intrinsics_byteToUInt = IntrinsicsModule.requiredMethod("byteToUInt")
  @tu lazy val Intrinsics_byteToULong = IntrinsicsModule.requiredMethod("byteToULong")
  @tu lazy val Intrinsics_shortToUInt = IntrinsicsModule.requiredMethod("shortToUInt")
  @tu lazy val Intrinsics_shortToULong = IntrinsicsModule.requiredMethod("shortToULong")
  @tu lazy val Intrinsics_intToULong = IntrinsicsModule.requiredMethod("intToULong")
  @tu lazy val Intrinsics_uintToFloat = IntrinsicsModule.requiredMethod("uintToFloat")
  @tu lazy val Intrinsics_ulongToFloat = IntrinsicsModule.requiredMethod("ulongToFloat")
  @tu lazy val Intrinsics_uintToDouble = IntrinsicsModule.requiredMethod("uintToDouble")
  @tu lazy val Intrinsics_ulongToDouble = IntrinsicsModule.requiredMethod("ulongToDouble")
  @tu lazy val Intrinsics_loadBool = IntrinsicsModule.requiredMethod("loadBoolean")
  @tu lazy val Intrinsics_loadChar = IntrinsicsModule.requiredMethod("loadChar")
  @tu lazy val Intrinsics_loadByte = IntrinsicsModule.requiredMethod("loadByte")
  @tu lazy val Intrinsics_loadShort = IntrinsicsModule.requiredMethod("loadShort")
  @tu lazy val Intrinsics_loadInt = IntrinsicsModule.requiredMethod("loadInt")
  @tu lazy val Intrinsics_loadLong = IntrinsicsModule.requiredMethod("loadLong")
  @tu lazy val Intrinsics_loadFloat = IntrinsicsModule.requiredMethod("loadFloat")
  @tu lazy val Intrinsics_loadDouble = IntrinsicsModule.requiredMethod("loadDouble")
  @tu lazy val Intrinsics_loadRawPtr = IntrinsicsModule.requiredMethod("loadRawPtr")
  @tu lazy val Intrinsics_loadRawSize = IntrinsicsModule.requiredMethod("loadRawSize")
  @tu lazy val Intrinsics_loadObject = IntrinsicsModule.requiredMethod("loadObject")
  @tu lazy val Intrinsics_storeBool = IntrinsicsModule.requiredMethod("storeBoolean")
  @tu lazy val Intrinsics_storeChar = IntrinsicsModule.requiredMethod("storeChar")
  @tu lazy val Intrinsics_storeByte = IntrinsicsModule.requiredMethod("storeByte")
  @tu lazy val Intrinsics_storeShort = IntrinsicsModule.requiredMethod("storeShort")
  @tu lazy val Intrinsics_storeInt = IntrinsicsModule.requiredMethod("storeInt")
  @tu lazy val Intrinsics_storeLong = IntrinsicsModule.requiredMethod("storeLong")
  @tu lazy val Intrinsics_storeFloat = IntrinsicsModule.requiredMethod("storeFloat")
  @tu lazy val Intrinsics_storeDouble = IntrinsicsModule.requiredMethod("storeDouble")
  @tu lazy val Intrinsics_storeRawPtr = IntrinsicsModule.requiredMethod("storeRawPtr")
  @tu lazy val Intrinsics_storeRawSize = IntrinsicsModule.requiredMethod("storeRawSize")
  @tu lazy val Intrinsics_storeObject = IntrinsicsModule.requiredMethod("storeObject")
  @tu lazy val Intrinsics_elemRawPtr = IntrinsicsModule.requiredMethod("elemRawPtr")
  @tu lazy val Intrinsics_castRawPtrToObject = IntrinsicsModule.requiredMethod("castRawPtrToObject")
  @tu lazy val Intrinsics_castObjectToRawPtr = IntrinsicsModule.requiredMethod("castObjectToRawPtr")
  @tu lazy val Intrinsics_castIntToFloat = IntrinsicsModule.requiredMethod("castIntToFloat")
  @tu lazy val Intrinsics_castFloatToInt = IntrinsicsModule.requiredMethod("castFloatToInt")
  @tu lazy val Intrinsics_castLongToDouble = IntrinsicsModule.requiredMethod("castLongToDouble")
  @tu lazy val Intrinsics_castDoubleToLong = IntrinsicsModule.requiredMethod("castDoubleToLong")
  @tu lazy val Intrinsics_castRawPtrToInt = IntrinsicsModule.requiredMethod("castRawPtrToInt")
  @tu lazy val Intrinsics_castRawPtrToLong = IntrinsicsModule.requiredMethod("castRawPtrToLong")
  @tu lazy val Intrinsics_castIntToRawPtr = IntrinsicsModule.requiredMethod("castIntToRawPtr")
  @tu lazy val Intrinsics_castLongToRawPtr = IntrinsicsModule.requiredMethod("castLongToRawPtr")
  @tu lazy val Intrinsics_castRawSizeToInt = IntrinsicsModule.requiredMethod("castRawSizeToInt")
  @tu lazy val Intrinsics_castRawSizeToLong = IntrinsicsModule.requiredMethod("castRawSizeToLong")
  @tu lazy val Intrinsics_castRawSizeToLongUnsigned = IntrinsicsModule.requiredMethod("castRawSizeToLongUnsigned")
  @tu lazy val Intrinsics_castIntToRawSize = IntrinsicsModule.requiredMethod("castIntToRawSize")
  @tu lazy val Intrinsics_castIntToRawSizeUnsigned = IntrinsicsModule.requiredMethod("castIntToRawSizeUnsigned")
  @tu lazy val Intrinsics_castLongToRawSize = IntrinsicsModule.requiredMethod("castLongToRawSize")
  @tu lazy val Intrinsics_stackallocAlts = IntrinsicsModule.info
    .member(termName("stackalloc"))
    .alternatives
    .map(_.symbol)
    .ensuring(_.size == 2)
  @tu lazy val IntrinsicsInternal_stackalloc = IntrinsicsInternalModule.requiredMethod("stackalloc")
  @tu lazy val Intrinsics_classFieldRawPtr = IntrinsicsModule.requiredMethod("classFieldRawPtr")
  @tu lazy val Intrinsics_sizeOf = IntrinsicsModule.requiredMethod("sizeOf")
  @tu lazy val IntrinsicsInternal_sizeOf = IntrinsicsInternalModule.requiredMethod("sizeOf")
  @tu lazy val Intrinsics_alignmentOf = IntrinsicsModule.requiredMethod("alignmentOf")
  @tu lazy val IntrinsicsInternal_alignmentOf = IntrinsicsInternalModule.requiredMethod("alignmentOf")
  @tu lazy val Intrinsics_unsignedOfAlts =
    IntrinsicsModule.info
      .member(termName("unsignedOf"))
      .alternatives
      .map(_.symbol)
      .ensuring(_.size == 5)

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
  @tu lazy val RuntimeBoxesModule = requiredModule("scala.scalanative.runtime.Boxes")
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
  @tu lazy val ReflectModule = requiredModule("scala.scalanative.reflect.Reflect")
  @tu lazy val Reflect_registerLoadableModuleClass =
    ReflectModule.requiredMethod("registerLoadableModuleClass")
  @tu lazy val Reflect_registerInstantiatableClass =
    ReflectModule.requiredMethod("registerInstantiatableClass")

  @tu lazy val EnableReflectiveInstantiationAnnotationClass =
    requiredClass("scala.scalanative.reflect.annotation.EnableReflectiveInstantiation")

  @tu lazy val ReflectSelectableClass = requiredClass("scala.reflect.Selectable")
  @tu lazy val ReflectSelectable_selectDynamic = ReflectSelectableClass.requiredMethod("selectDynamic")
  @tu lazy val ReflectSelectable_applyDynamic = ReflectSelectableClass.requiredMethod("applyDynamic")
  @tu lazy val ReflectSelectable_selectedValue = ReflectSelectableClass.requiredMethod("selectedValue")

  // Java library
  @tu lazy val NObjectClass = requiredClass("java.lang._Object")
  @tu lazy val NObject_init = NObjectClass.requiredMethod("<init>")
  @tu lazy val NObject_hashCode = NObjectClass.requiredMethod("__scala_##")
  @tu lazy val NObject_equals = NObjectClass.requiredMethod("__scala_==")

  @tu lazy val NStringClass = requiredClass("java.lang._String")
  @tu lazy val NStringModuleType = requiredModule("java.lang._String")

  @tu lazy val String_concat = defn.StringClass.requiredMethod("concat")

  // Scala library & runtime
  @tu lazy val InlineClass = requiredClass("scala.inline")
  @tu lazy val NoInlineClass = requiredClass("scala.noinline")

}
