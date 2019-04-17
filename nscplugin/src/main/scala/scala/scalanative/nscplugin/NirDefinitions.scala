package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirDefinitions { self: NirGlobalAddons =>
  import global._
  import definitions._
  import rootMirror._

  object nirDefinitions {

    // Native library

    lazy val UByteClass  = getRequiredClass("scala.scalanative.native.UByte")
    lazy val UShortClass = getRequiredClass("scala.scalanative.native.UShort")
    lazy val UIntClass   = getRequiredClass("scala.scalanative.native.UInt")
    lazy val ULongClass  = getRequiredClass("scala.scalanative.native.ULong")

    lazy val PtrClass        = getRequiredClass("scala.scalanative.native.Ptr")
    lazy val PtrLoadMethod   = getDecl(PtrClass, TermName("unary_$bang"))
    lazy val PtrStoreMethod  = getDecl(PtrClass, TermName("unary_$bang_$eq"))
    lazy val PtrAddMethod    = getDecl(PtrClass, TermName("$plus"))
    lazy val PtrSubMethods   = getDecl(PtrClass, TermName("$minus")).alternatives
    lazy val PtrApplyMethod  = getDecl(PtrClass, TermName("apply"))
    lazy val PtrUpdateMethod = getDecl(PtrClass, TermName("update"))
    lazy val PtrFieldMethod = (1 to 22).map { i =>
      getDecl(PtrClass, TermName("_" + i))
    }

    lazy val RawPtrClass = getRequiredClass("scala.scalanative.runtime.RawPtr")

    lazy val NameClass   = getRequiredClass("scala.scalanative.native.name")
    lazy val LinkClass   = getRequiredClass("scala.scalanative.native.link")
    lazy val ExternClass = getRequiredClass("scala.scalanative.native.extern")
    lazy val PinClass    = getRequiredClass("scala.scalanative.native.pin")
    lazy val StubClass   = getRequiredClass("scala.scalanative.native.stub")

    lazy val InlineHintClass = getRequiredClass(
      "scala.scalanative.native.inlinehint")
    lazy val AlwaysInlineClass = getRequiredClass(
      "scala.scalanative.native.alwaysinline")
    lazy val NoOptimizeClass = getRequiredClass(
      "scala.scalanative.native.nooptimize")
    lazy val NoSpecializeClass = getRequiredClass(
      "scala.scalanative.native.nospecialize")

    lazy val NativeModule = getRequiredModule(
      "scala.scalanative.native.package")
    lazy val CastMethod   = getMember(NativeModule, TermName("cast"))
    lazy val ExternMethod = getMember(NativeModule, TermName("extern"))
    lazy val StackallocMethods =
      getMember(NativeModule, TermName("stackalloc")).alternatives

    lazy val CVarargModule = getRequiredModule(
      "scala.scalanative.native.CVararg")
    lazy val CVarargMethod = getMember(CVarargModule, TermName("apply"))

    lazy val CQuoteClass = getRequiredClass(
      "scala.scalanative.native.package$CQuote")
    lazy val CQuoteMethod = getDecl(CQuoteClass, TermName("c"))

    lazy val CCastClass = getRequiredClass(
      "scala.scalanative.native.package$CCast")
    lazy val CCastMethod = getDecl(CCastClass, TermName("cast"))

    lazy val CFunctionPtrClass = (0 to 22).map { n =>
      getRequiredClass("scala.scalanative.native.CFunctionPtr" + n)
    }
    lazy val CFunctionPtrApply =
      CFunctionPtrClass.map(getDecl(_, TermName("apply")))
    lazy val CFunctionPtrModule = getRequiredModule(
      "scala.scalanative.native.CFunctionPtr")
    lazy val CFunctionPtrFrom = (0 to 22).map { n =>
      getDecl(CFunctionPtrModule, TermName("fromFunction" + n))
    }

    lazy val CStructClass = (0 to 22).map { n =>
      getRequiredClass("scala.scalanative.native.CStruct" + n)
    }
    lazy val CArrayClass =
      getRequiredClass("scala.scalanative.native.CArray")
    lazy val NatBaseClass = (0 to 9).map { n =>
      getRequiredClass("scala.scalanative.native.Nat$_" + n)
    }
    lazy val NatDigitClass =
      getRequiredClass("scala.scalanative.native.Nat$Digit")

    lazy val TagModule     = getRequiredModule("scala.scalanative.native.Tag")
    lazy val UnitTagMethod = getDecl(TagModule, TermName("materializeUnitTag"))
    lazy val BooleanTagMethod =
      getDecl(TagModule, TermName("materializeBooleanTag"))
    lazy val CharTagMethod = getDecl(TagModule, TermName("materializeCharTag"))
    lazy val ByteTagMethod = getDecl(TagModule, TermName("materializeByteTag"))
    lazy val UByteTagMethod =
      getDecl(TagModule, TermName("materializeUByteTag"))
    lazy val ShortTagMethod =
      getDecl(TagModule, TermName("materializeShortTag"))
    lazy val UShortTagMethod =
      getDecl(TagModule, TermName("materializeUShortTag"))
    lazy val IntTagMethod  = getDecl(TagModule, TermName("materializeIntTag"))
    lazy val UIntTagMethod = getDecl(TagModule, TermName("materializeUIntTag"))
    lazy val LongTagMethod = getDecl(TagModule, TermName("materializeLongTag"))
    lazy val ULongTagMethod =
      getDecl(TagModule, TermName("materializeULongTag"))
    lazy val FloatTagMethod =
      getDecl(TagModule, TermName("materializeFloatTag"))
    lazy val DoubleTagMethod =
      getDecl(TagModule, TermName("materializeDoubleTag"))
    lazy val PtrTagMethod = getDecl(TagModule, TermName("materializePtrTag"))
    lazy val ClassTagMethod =
      getDecl(TagModule, TermName("materializeClassTag"))
    lazy val NatBaseTagMethod = (0 to 9).map { n =>
      getDecl(TagModule, TermName("materializeNat" + n + "Tag"))
    }
    lazy val NatDigitTagMethod =
      getDecl(TagModule, TermName("materializeNatDigitTag"))
    lazy val CArrayTagMethod =
      getDecl(TagModule, TermName("materializeCArrayTag"))
    lazy val CStructTagMethod = (0 to 22).map { n =>
      getDecl(TagModule, TermName("materializeCStruct" + n + "Tag"))
    }

    // scala names

    lazy val EqEqMethodName  = newTermName("$eq$eq")
    lazy val NotEqMethodName = newTermName("$bang$eq")

    // Native runtime

    lazy val StructClass = getRequiredClass("scala.scalanative.runtime.struct")

    lazy val RuntimePackage = getPackage(TermName("scala.scalanative.runtime"))

    lazy val RuntimeMonitorClass = getRequiredClass(
      "scala.scalanative.runtime.Monitor")
    lazy val RuntimeMonitorModule = getRequiredModule(
      "scala.scalanative.runtime.Monitor")
    lazy val RuntimeMonitorEnterMethod =
      getDecl(RuntimeMonitorClass, TermName("enter"))
    lazy val RuntimeMonitorExitMethod =
      getDecl(RuntimeMonitorClass, TermName("exit"))

    lazy val RuntimeTypeClass = getRequiredClass(
      "scala.scalanative.runtime.Type")

    lazy val RuntimeModule = getRequiredModule(
      "scala.scalanative.runtime.package")
    lazy val GetMonitorMethod =
      getMember(RuntimeModule, TermName("getMonitor"))

    lazy val IntrinsicsModule = getRequiredModule(
      "scala.scalanative.runtime.Intrinsics")
    lazy val DivUIntMethod  = getMember(IntrinsicsModule, TermName("divUInt"))
    lazy val DivULongMethod = getMember(IntrinsicsModule, TermName("divULong"))
    lazy val RemUIntMethod  = getMember(IntrinsicsModule, TermName("remUInt"))
    lazy val RemULongMethod = getMember(IntrinsicsModule, TermName("remULong"))
    lazy val ByteToUIntMethod =
      getMember(IntrinsicsModule, TermName("byteToUInt"))
    lazy val ByteToULongMethod =
      getMember(IntrinsicsModule, TermName("byteToULong"))
    lazy val ShortToUIntMethod =
      getMember(IntrinsicsModule, TermName("shortToUInt"))
    lazy val ShortToULongMethod =
      getMember(IntrinsicsModule, TermName("shortToULong"))
    lazy val IntToULongMethod =
      getMember(IntrinsicsModule, TermName("intToULong"))
    lazy val UIntToFloatMethod =
      getMember(IntrinsicsModule, TermName("uintToFloat"))
    lazy val ULongToFloatMethod =
      getMember(IntrinsicsModule, TermName("ulongToFloat"))
    lazy val UIntToDoubleMethod =
      getMember(IntrinsicsModule, TermName("uintToDouble"))
    lazy val ULongToDoubleMethod =
      getMember(IntrinsicsModule, TermName("ulongToDouble"))
    lazy val LoadBoolMethod =
      getMember(IntrinsicsModule, TermName("loadBoolean"))
    lazy val LoadCharMethod = getMember(IntrinsicsModule, TermName("loadChar"))
    lazy val LoadByteMethod = getMember(IntrinsicsModule, TermName("loadByte"))
    lazy val LoadShortMethod =
      getMember(IntrinsicsModule, TermName("loadShort"))
    lazy val LoadIntMethod  = getMember(IntrinsicsModule, TermName("loadInt"))
    lazy val LoadLongMethod = getMember(IntrinsicsModule, TermName("loadLong"))
    lazy val LoadFloatMethod =
      getMember(IntrinsicsModule, TermName("loadFloat"))
    lazy val LoadDoubleMethod =
      getMember(IntrinsicsModule, TermName("loadDouble"))
    lazy val LoadRawPtrMethod =
      getMember(IntrinsicsModule, TermName("loadRawPtr"))
    lazy val LoadObjectMethod =
      getMember(IntrinsicsModule, TermName("loadObject"))
    lazy val StoreBoolMethod =
      getMember(IntrinsicsModule, TermName("storeBoolean"))
    lazy val StoreCharMethod =
      getMember(IntrinsicsModule, TermName("storeChar"))
    lazy val StoreByteMethod =
      getMember(IntrinsicsModule, TermName("storeByte"))
    lazy val StoreShortMethod =
      getMember(IntrinsicsModule, TermName("storeShort"))
    lazy val StoreIntMethod = getMember(IntrinsicsModule, TermName("storeInt"))
    lazy val StoreLongMethod =
      getMember(IntrinsicsModule, TermName("storeLong"))
    lazy val StoreFloatMethod =
      getMember(IntrinsicsModule, TermName("storeFloat"))
    lazy val StoreDoubleMethod =
      getMember(IntrinsicsModule, TermName("storeDouble"))
    lazy val StoreRawPtrMethod =
      getMember(IntrinsicsModule, TermName("storeRawPtr"))
    lazy val StoreObjectMethod =
      getMember(IntrinsicsModule, TermName("storeObject"))
    lazy val ElemRawPtrMethod =
      getMember(IntrinsicsModule, TermName("elemRawPtr"))
    lazy val CastRawPtrToObjectMethod =
      getMember(IntrinsicsModule, TermName("castRawPtrToObject"))
    lazy val CastObjectToRawPtrMethod =
      getMember(IntrinsicsModule, TermName("castObjectToRawPtr"))
    lazy val CastIntToFloatMethod =
      getMember(IntrinsicsModule, TermName("castIntToFloat"))
    lazy val CastFloatToIntMethod =
      getMember(IntrinsicsModule, TermName("castFloatToInt"))
    lazy val CastLongToDoubleMethod =
      getMember(IntrinsicsModule, TermName("castLongToDouble"))
    lazy val CastDoubleToLongMethod =
      getMember(IntrinsicsModule, TermName("castDoubleToLong"))
    lazy val CastRawPtrToIntMethod =
      getMember(IntrinsicsModule, TermName("castRawPtrToInt"))
    lazy val CastRawPtrToLongMethod =
      getMember(IntrinsicsModule, TermName("castRawPtrToLong"))
    lazy val CastIntToRawPtrMethod =
      getMember(IntrinsicsModule, TermName("castIntToRawPtr"))
    lazy val CastLongToRawPtrMethod =
      getMember(IntrinsicsModule, TermName("castLongToRawPtr"))

    lazy val RuntimePrimitive: Map[Char, Symbol] = Map(
      'B' -> getRequiredClass("scala.scalanative.runtime.PrimitiveBoolean"),
      'C' -> getRequiredClass("scala.scalanative.runtime.PrimitiveChar"),
      'Z' -> getRequiredClass("scala.scalanative.runtime.PrimitiveByte"),
      'S' -> getRequiredClass("scala.scalanative.runtime.PrimitiveShort"),
      'I' -> getRequiredClass("scala.scalanative.runtime.PrimitiveInt"),
      'L' -> getRequiredClass("scala.scalanative.runtime.PrimitiveLong"),
      'F' -> getRequiredClass("scala.scalanative.runtime.PrimitiveFloat"),
      'D' -> getRequiredClass("scala.scalanative.runtime.PrimitiveDouble"),
      'U' -> getRequiredClass("scala.scalanative.runtime.PrimitiveUnit")
    )

    lazy val RuntimeArrayClass: Map[Char, Symbol] = Map(
      'B' -> getRequiredClass("scala.scalanative.runtime.BooleanArray"),
      'C' -> getRequiredClass("scala.scalanative.runtime.CharArray"),
      'Z' -> getRequiredClass("scala.scalanative.runtime.ByteArray"),
      'S' -> getRequiredClass("scala.scalanative.runtime.ShortArray"),
      'I' -> getRequiredClass("scala.scalanative.runtime.IntArray"),
      'L' -> getRequiredClass("scala.scalanative.runtime.LongArray"),
      'F' -> getRequiredClass("scala.scalanative.runtime.FloatArray"),
      'D' -> getRequiredClass("scala.scalanative.runtime.DoubleArray"),
      'O' -> getRequiredClass("scala.scalanative.runtime.ObjectArray")
    )

    lazy val RuntimeArrayModule: Map[Char, Symbol] =
      RuntimeArrayClass.mapValues(_.companion)

    lazy val RuntimeArrayAllocMethod: Map[Char, Symbol] =
      RuntimeArrayModule.mapValues(getMember(_, TermName("alloc")))

    lazy val RuntimeArrayApplyMethod: Map[Char, Symbol] =
      RuntimeArrayClass.mapValues(getMember(_, TermName("apply")))

    lazy val RuntimeArrayUpdateMethod: Map[Char, Symbol] =
      RuntimeArrayClass.mapValues(getMember(_, TermName("update")))

    lazy val RuntimeArrayLengthMethod: Map[Char, Symbol] =
      RuntimeArrayClass.mapValues(getMember(_, TermName("length")))

    lazy val RuntimeArrayCloneMethod: Map[Char, Symbol] =
      RuntimeArrayClass.mapValues(getMember(_, TermName("clone")))

    lazy val RuntimeBoxesModule = getRequiredModule(
      "scala.scalanative.runtime.Boxes")

    // Java library

    lazy val NObjectClass      = getRequiredClass("java.lang._Object")
    lazy val NObjectInitMethod = getDecl(NObjectClass, TermName("<init>"))
    lazy val NObjectHashCodeMethod =
      getDecl(NObjectClass, TermName("__scala_$hash$hash"))
    lazy val NObjectEqualsMethod =
      getDecl(NObjectClass, TermName("__scala_$eq$eq"))

    lazy val NStringClass  = getRequiredClass("java.lang._String")
    lazy val NStringModule = getRequiredModule("java.lang._String")

    // Scala library & runtime

    lazy val InlineClass      = getRequiredClass("scala.inline")
    lazy val NoInlineClass    = getRequiredClass("scala.noinline")
    lazy val EnumerationClass = getRequiredClass("scala.Enumeration")
    lazy val PropertiesTrait  = getRequiredClass("scala.util.PropertiesTrait")
    lazy val JavaProperties   = getRequiredClass("java.util.Properties")

    lazy val StringConcatMethod = getMember(StringClass, TermName("concat"))

    lazy val BoxMethod = Map[Char, Symbol](
      'B' -> getDecl(BoxesRunTimeModule, TermName("boxToBoolean")),
      'C' -> getDecl(BoxesRunTimeModule, TermName("boxToCharacter")),
      'Z' -> getDecl(BoxesRunTimeModule, TermName("boxToByte")),
      'S' -> getDecl(BoxesRunTimeModule, TermName("boxToShort")),
      'I' -> getDecl(BoxesRunTimeModule, TermName("boxToInteger")),
      'L' -> getDecl(BoxesRunTimeModule, TermName("boxToLong")),
      'F' -> getDecl(BoxesRunTimeModule, TermName("boxToFloat")),
      'D' -> getDecl(BoxesRunTimeModule, TermName("boxToDouble"))
    )

    lazy val BoxUnsignedMethod = Map[Symbol, Symbol](
      UByteClass  -> getDecl(RuntimeBoxesModule, TermName("boxToUByte")),
      UShortClass -> getDecl(RuntimeBoxesModule, TermName("boxToUShort")),
      UIntClass   -> getDecl(RuntimeBoxesModule, TermName("boxToUInt")),
      ULongClass  -> getDecl(RuntimeBoxesModule, TermName("boxToULong"))
    )

    lazy val HashMethods = Seq(
      getDecl(BoxesRunTimeModule, TermName("hashFromObject")),
      getDecl(BoxesRunTimeModule, TermName("hashFromNumber")),
      getDecl(BoxesRunTimeModule, TermName("hashFromFloat")),
      getDecl(BoxesRunTimeModule, TermName("hashFromDouble")),
      getDecl(BoxesRunTimeModule, TermName("hashFromLong"))
    ) ++ getMember(ScalaRunTimeModule, TermName("hash")).alternatives

    lazy val UnboxMethod = Map[Char, Symbol](
      'B' -> getDecl(BoxesRunTimeModule, TermName("unboxToBoolean")),
      'C' -> getDecl(BoxesRunTimeModule, TermName("unboxToChar")),
      'Z' -> getDecl(BoxesRunTimeModule, TermName("unboxToByte")),
      'S' -> getDecl(BoxesRunTimeModule, TermName("unboxToShort")),
      'I' -> getDecl(BoxesRunTimeModule, TermName("unboxToInt")),
      'L' -> getDecl(BoxesRunTimeModule, TermName("unboxToLong")),
      'F' -> getDecl(BoxesRunTimeModule, TermName("unboxToFloat")),
      'D' -> getDecl(BoxesRunTimeModule, TermName("unboxToDouble"))
    )

    lazy val UnboxUnsignedMethod = Map[Symbol, Symbol](
      UByteClass  -> getDecl(RuntimeBoxesModule, TermName("unboxToUByte")),
      UShortClass -> getDecl(RuntimeBoxesModule, TermName("unboxToUShort")),
      UIntClass   -> getDecl(RuntimeBoxesModule, TermName("unboxToUInt")),
      ULongClass  -> getDecl(RuntimeBoxesModule, TermName("unboxToULong"))
    )

    lazy val ClassTagModule  = getRequiredModule("scala.reflect.ClassTag")
    lazy val ClassTagApply   = getDecl(ClassTagModule, TermName("apply"))
    lazy val ByteClassTag    = getDecl(ClassTagModule, TermName("Byte"))
    lazy val ShortClassTag   = getDecl(ClassTagModule, TermName("Short"))
    lazy val CharClassTag    = getDecl(ClassTagModule, TermName("Char"))
    lazy val IntClassTag     = getDecl(ClassTagModule, TermName("Int"))
    lazy val LongClassTag    = getDecl(ClassTagModule, TermName("Long"))
    lazy val FloatClassTag   = getDecl(ClassTagModule, TermName("Float"))
    lazy val DoubleClassTag  = getDecl(ClassTagModule, TermName("Double"))
    lazy val BooleanClassTag = getDecl(ClassTagModule, TermName("Boolean"))
    lazy val UnitClassTag    = getDecl(ClassTagModule, TermName("Unit"))
    lazy val AnyClassTag     = getDecl(ClassTagModule, TermName("Any"))
    lazy val ObjectClassTag  = getDecl(ClassTagModule, TermName("Object"))
    lazy val AnyValClassTag  = getDecl(ClassTagModule, TermName("AnyVal"))
    lazy val AnyRefClassTag  = getDecl(ClassTagModule, TermName("AnyRef"))
    lazy val NothingClassTag = getDecl(ClassTagModule, TermName("Nothing"))
    lazy val NullClassTag    = getDecl(ClassTagModule, TermName("Null"))
  }
}
