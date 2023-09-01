package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirDefinitions {
  val global: Global
  import global._
  import definitions._
  import rootMirror._

  object nirDefinitions {
    case class NonErasedType(tpe: Type) extends PlainAttachment
    case class NonErasedTypes(tpes: List[Type]) extends PlainAttachment

    // Native library

    lazy val UByteClass = getRequiredClass("scala.scalanative.unsigned.UByte")
    lazy val UShortClass = getRequiredClass("scala.scalanative.unsigned.UShort")
    lazy val UIntClass = getRequiredClass("scala.scalanative.unsigned.UInt")
    lazy val ULongClass = getRequiredClass("scala.scalanative.unsigned.ULong")
    lazy val SizeClass = getRequiredClass("scala.scalanative.unsafe.Size")
    lazy val USizeClass = getRequiredClass("scala.scalanative.unsigned.USize")
    lazy val RawSizeClass = getRequiredClass(
      "scala.scalanative.runtime.RawSize"
    )

    lazy val USizeModule = getRequiredModule("scala.scalanative.unsigned.USize")
    lazy val USize_fromUByte = getDecl(USizeModule, TermName("fromUByte"))
    lazy val USize_fromUShort = getDecl(USizeModule, TermName("fromUShort"))
    lazy val USize_fromUInt = getDecl(USizeModule, TermName("fromUInt"))

    lazy val SizeModule = getRequiredModule("scala.scalanative.unsafe.Size")
    lazy val Size_fromByte = getDecl(SizeModule, TermName("fromByte"))
    lazy val Size_fromShort = getDecl(SizeModule, TermName("fromShort"))
    lazy val Size_fromInt = getDecl(SizeModule, TermName("fromInt"))

    lazy val PtrClass = getRequiredClass("scala.scalanative.unsafe.Ptr")
    lazy val RawPtrClass = getRequiredClass("scala.scalanative.runtime.RawPtr")

    lazy val NameClass = getRequiredClass("scala.scalanative.unsafe.name")
    lazy val LinkClass = getRequiredClass("scala.scalanative.unsafe.link")
    lazy val ExternClass = getRequiredClass(
      "scala.scalanative.unsafe.package$extern"
    )
    lazy val BlockingClass = getRequiredClass(
      "scala.scalanative.unsafe.package$blocking"
    )
    lazy val ExportedClass = getRequiredClass(
      "scala.scalanative.unsafe.exported"
    )
    lazy val ExportAccessorsClass = getRequiredClass(
      "scala.scalanative.unsafe.exportAccessors"
    )
    lazy val StubClass = getRequiredClass("scala.scalanative.annotation.stub")

    lazy val AlwaysInlineClass = getRequiredClass(
      "scala.scalanative.annotation.alwaysinline"
    )
    lazy val NoOptimizeClass = getRequiredClass(
      "scala.scalanative.annotation.nooptimize"
    )
    lazy val NoSpecializeClass = getRequiredClass(
      "scala.scalanative.annotation.nospecialize"
    )
    lazy val AlignClass = getRequiredClass("scala.scalanative.annotation.align")

    lazy val NativeModule = getRequiredModule(
      "scala.scalanative.unsafe.package"
    )
    lazy val ExternMethod = getMember(NativeModule, TermName("extern"))

    lazy val CQuoteClass = getRequiredClass(
      "scala.scalanative.unsafe.package$CQuote"
    )
    lazy val CQuoteMethod = getDecl(CQuoteClass, TermName("c"))

    lazy val CFuncPtrNClass = (0 to 22).map { n =>
      getRequiredClass(s"scala.scalanative.unsafe.CFuncPtr$n")
    }

    lazy val CFuncPtrNModule = (0 to 22).map { n =>
      getRequiredModule(s"scala.scalanative.unsafe.CFuncPtr$n")
    }

    lazy val CFuncPtrClass =
      getRequiredClass("scala.scalanative.unsafe.CFuncPtr")

    lazy val NatBaseClass = (0 to 9).map { n =>
      getRequiredClass("scala.scalanative.unsafe.Nat$_" + n)
    }
    lazy val NatDigitClass = (2 to 9).map { n =>
      getRequiredClass("scala.scalanative.unsafe.Nat$Digit" + n)
    }

    lazy val CStructClass = (0 to 22).map { n =>
      getRequiredClass("scala.scalanative.unsafe.CStruct" + n)
    }
    lazy val CArrayClass =
      getRequiredClass("scala.scalanative.unsafe.CArray")

    // scala names

    lazy val EqEqMethodName = newTermName("$eq$eq")
    lazy val NotEqMethodName = newTermName("$bang$eq")

    // Native runtime

    lazy val StructClass = getRequiredClass("scala.scalanative.runtime.struct")

    lazy val RuntimePackage = getPackageObject("scala.scalanative.runtime")

    lazy val RuntimeEnterMonitorMethod =
      getDecl(RuntimePackage, TermName("enterMonitor"))
    lazy val RuntimeExitMonitorMethod =
      getDecl(RuntimePackage, TermName("exitMonitor"))
    lazy val RuntimePackage_fromRawSize =
      getDecl(RuntimePackage, TermName("fromRawSize"))
    lazy val RuntimePackage_fromRawUSize =
      getDecl(RuntimePackage, TermName("fromRawUSize"))
    lazy val RuntimePackage_toRawSizeAlts =
      getDecl(RuntimePackage, TermName("toRawSize")).alternatives
        .ensuring(_.size == 2)

    lazy val RuntimeTypeClass = getRequiredClass(
      "scala.scalanative.runtime.Type"
    )

    lazy val RuntimeModule = getRequiredModule(
      "scala.scalanative.runtime.package"
    )
    lazy val IntrinsicsModule = getRequiredModule(
      "scala.scalanative.runtime.Intrinsics"
    )
    lazy val IntrinsicsInternalModule =
      getMember(IntrinsicsModule, TermName("internal"))
    lazy val DivUIntMethod = getMember(IntrinsicsModule, TermName("divUInt"))
    lazy val DivULongMethod = getMember(IntrinsicsModule, TermName("divULong"))
    lazy val RemUIntMethod = getMember(IntrinsicsModule, TermName("remUInt"))
    lazy val RemULongMethod = getMember(IntrinsicsModule, TermName("remULong"))
    lazy val UnsignedOfMethods =
      getMember(IntrinsicsModule, TermName("unsignedOf")).alternatives
        .ensuring(_.size == 5)
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
    lazy val LoadRawSizeMethod =
      getMember(IntrinsicsModule, TermName("loadRawSize"))
    lazy val LoadCharMethod = getMember(IntrinsicsModule, TermName("loadChar"))
    lazy val LoadByteMethod = getMember(IntrinsicsModule, TermName("loadByte"))
    lazy val LoadShortMethod =
      getMember(IntrinsicsModule, TermName("loadShort"))
    lazy val LoadIntMethod = getMember(IntrinsicsModule, TermName("loadInt"))
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
    lazy val StoreRawSizeMethod =
      getMember(IntrinsicsModule, TermName("storeRawSize"))
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
    lazy val StackallocMethods =
      getMember(IntrinsicsModule, TermName("stackalloc")).alternatives
    lazy val StackallocInternalMethod =
      getMember(IntrinsicsInternalModule, TermName("stackalloc"))
    lazy val ClassFieldRawPtrMethod =
      getMember(IntrinsicsModule, TermName("classFieldRawPtr"))
    lazy val SizeOfMethod =
      getMember(IntrinsicsModule, TermName("sizeOf"))
    lazy val SizeOfInternalMethod =
      getMember(IntrinsicsInternalModule, TermName("sizeOf"))
    lazy val AlignmentOfMethod =
      getMember(IntrinsicsModule, TermName("alignmentOf"))
    lazy val AlignmentOfInternalMethod =
      getMember(IntrinsicsInternalModule, TermName("alignmentOf"))

    lazy val CFuncPtrApplyMethods = CFuncPtrNClass.map(
      getMember(_, TermName("apply"))
    )

    lazy val CFuncPtrFromFunctionMethods =
      CFuncPtrNModule.zipWithIndex.map {
        case (module, n) =>
          getMember(module, TermName(s"fromScalaFunction"))
      }

    lazy val CastRawSizeToInt =
      getMember(IntrinsicsModule, TermName("castRawSizeToInt"))
    lazy val CastRawSizeToLong =
      getMember(IntrinsicsModule, TermName("castRawSizeToLong"))
    lazy val CastRawSizeToLongUnsigned =
      getMember(IntrinsicsModule, TermName("castRawSizeToLongUnsigned"))
    lazy val CastIntToRawSize =
      getMember(IntrinsicsModule, TermName("castIntToRawSize"))
    lazy val CastIntToRawSizeUnsigned =
      getMember(IntrinsicsModule, TermName("castIntToRawSizeUnsigned"))
    lazy val CastLongToRawSize =
      getMember(IntrinsicsModule, TermName("castLongToRawSize"))

    lazy val ResolvedAtLinktimeClass = getRequiredClass(
      "scala.scalanative.unsafe.resolvedAtLinktime"
    )
    lazy val ResolvedMethod = getMember(NativeModule, TermName("resolved"))

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

    private def mapValue[K, V1, V2](fn: V1 => V2)(in: (K, V1)): (K, V2) =
      (in._1, fn(in._2))

    lazy val RuntimeArrayModule: Map[Char, Symbol] =
      RuntimeArrayClass.map(mapValue(_.companion))

    lazy val RuntimeArrayAllocMethod: Map[Char, Symbol] =
      RuntimeArrayModule.map(mapValue(getMember(_, TermName("alloc"))))

    lazy val RuntimeArrayApplyMethod: Map[Char, Symbol] =
      RuntimeArrayClass.map(mapValue(getMember(_, TermName("apply"))))

    lazy val RuntimeArrayUpdateMethod: Map[Char, Symbol] =
      RuntimeArrayClass.map(mapValue(getMember(_, TermName("update"))))

    lazy val RuntimeArrayLengthMethod: Map[Char, Symbol] =
      RuntimeArrayClass.map(mapValue(getMember(_, TermName("length"))))

    lazy val RuntimeArrayCloneMethod: Map[Char, Symbol] =
      RuntimeArrayClass.map(mapValue(getMember(_, TermName("clone"))))

    lazy val RuntimeBoxesModule = getRequiredModule(
      "scala.scalanative.runtime.Boxes"
    )

    // Java library

    lazy val NObjectClass = getRequiredClass("java.lang._Object")
    lazy val NObjectInitMethod = getDecl(NObjectClass, TermName("<init>"))
    lazy val NObjectHashCodeMethod =
      getDecl(NObjectClass, TermName("__scala_$hash$hash"))
    lazy val NObjectEqualsMethod =
      getDecl(NObjectClass, TermName("__scala_$eq$eq"))

    lazy val NStringClass = getRequiredClass("java.lang._String")
    lazy val NStringModule = getRequiredModule("java.lang._String")

    // Scala library & runtime

    lazy val InlineClass = getRequiredClass("scala.inline")
    lazy val NoInlineClass = getRequiredClass("scala.noinline")
    lazy val EnumerationClass = getRequiredClass("scala.Enumeration")
    lazy val PropertiesTrait = getRequiredClass("scala.util.PropertiesTrait")
    lazy val JavaProperties = getRequiredClass("java.util.Properties")

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
      UByteClass -> getDecl(RuntimeBoxesModule, TermName("boxToUByte")),
      UShortClass -> getDecl(RuntimeBoxesModule, TermName("boxToUShort")),
      UIntClass -> getDecl(RuntimeBoxesModule, TermName("boxToUInt")),
      ULongClass -> getDecl(RuntimeBoxesModule, TermName("boxToULong")),
      USizeClass -> getDecl(RuntimeBoxesModule, TermName("boxToUSize"))
    )

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
      UByteClass -> getDecl(RuntimeBoxesModule, TermName("unboxToUByte")),
      UShortClass -> getDecl(RuntimeBoxesModule, TermName("unboxToUShort")),
      UIntClass -> getDecl(RuntimeBoxesModule, TermName("unboxToUInt")),
      ULongClass -> getDecl(RuntimeBoxesModule, TermName("unboxToULong")),
      USizeClass -> getDecl(RuntimeBoxesModule, TermName("unboxToUSize"))
    )

    lazy val ReflectModule =
      getRequiredModule("scala.scalanative.reflect.Reflect")
    lazy val Reflect_registerLoadableModuleClass =
      getMemberMethod(ReflectModule, newTermName("registerLoadableModuleClass"))
    lazy val Reflect_registerInstantiatableClass =
      getMemberMethod(ReflectModule, newTermName("registerInstantiatableClass"))

    lazy val EnableReflectiveInstantiationAnnotation =
      getRequiredClass(
        "scala.scalanative.reflect.annotation.EnableReflectiveInstantiation"
      )
  }

}
