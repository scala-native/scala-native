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

    lazy val NameClass   = getRequiredClass("scala.scalanative.native.name")
    lazy val LinkClass   = getRequiredClass("scala.scalanative.native.link")
    lazy val ExternClass = getRequiredClass("scala.scalanative.native.extern")
    lazy val PinClass    = getRequiredClass("scala.scalanative.native.pin")

    lazy val InlineHintClass = getRequiredClass(
      "scala.scalanative.native.inlinehint")

    lazy val NativeModule = getRequiredModule(
      "scala.scalanative.native.package")
    lazy val CastMethod   = getMember(NativeModule, TermName("cast"))
    lazy val ExternMethod = getMember(NativeModule, TermName("extern"))
    lazy val SizeofMethod = getMember(NativeModule, TermName("sizeof"))
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

    lazy val TagModule        = getRequiredModule("scala.scalanative.native.Tag")
    lazy val UnitTagMethod    = getDecl(TagModule, TermName("Unit"))
    lazy val BooleanTagMethod = getDecl(TagModule, TermName("Boolean"))
    lazy val CharTagMethod    = getDecl(TagModule, TermName("Char"))
    lazy val ByteTagMethod    = getDecl(TagModule, TermName("Byte"))
    lazy val UByteTagMethod   = getDecl(TagModule, TermName("UByte"))
    lazy val ShortTagMethod   = getDecl(TagModule, TermName("Short"))
    lazy val UShortTagMethod  = getDecl(TagModule, TermName("UShort"))
    lazy val IntTagMethod     = getDecl(TagModule, TermName("Int"))
    lazy val UIntTagMethod    = getDecl(TagModule, TermName("UInt"))
    lazy val LongTagMethod    = getDecl(TagModule, TermName("Long"))
    lazy val ULongTagMethod   = getDecl(TagModule, TermName("ULong"))
    lazy val FloatTagMethod   = getDecl(TagModule, TermName("Float"))
    lazy val DoubleTagMethod  = getDecl(TagModule, TermName("Double"))
    lazy val PtrTagMethod     = getDecl(TagModule, TermName("Ptr"))
    lazy val RefTagMethod     = getDecl(TagModule, TermName("Ref"))
    lazy val NatBaseTagMethod = (0 to 9).map { n =>
      getDecl(TagModule, TermName("Nat" + n))
    }
    lazy val NatDigitTagMethod = getDecl(TagModule, TermName("NatDigit"))
    lazy val CArrayTagMethod   = getDecl(TagModule, TermName("CArray"))
    lazy val CStructTagMethod = (0 to 22).map { n =>
      getDecl(TagModule, TermName("CStruct" + n))
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
    lazy val TypeofMethod = getMember(RuntimeModule, TermName("typeof"))
    lazy val GetMonitorMethod =
      getMember(RuntimeModule, TermName("getMonitor"))
    lazy val DivUIntMethod  = getMember(RuntimeModule, TermName("divUInt"))
    lazy val DivULongMethod = getMember(RuntimeModule, TermName("divULong"))
    lazy val RemUIntMethod  = getMember(RuntimeModule, TermName("remUInt"))
    lazy val RemULongMethod = getMember(RuntimeModule, TermName("remULong"))
    lazy val ByteToUIntMethod =
      getMember(RuntimeModule, TermName("byteToUInt"))
    lazy val ByteToULongMethod =
      getMember(RuntimeModule, TermName("byteToULong"))
    lazy val ShortToUIntMethod =
      getMember(RuntimeModule, TermName("shortToUInt"))
    lazy val ShortToULongMethod =
      getMember(RuntimeModule, TermName("shortToULong"))
    lazy val IntToULongMethod =
      getMember(RuntimeModule, TermName("intToULong"))
    lazy val SelectMethod = getMember(RuntimeModule, TermName("select"))

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

    lazy val InlineClass   = getRequiredClass("scala.inline")
    lazy val NoInlineClass = getRequiredClass("scala.noinline")

    lazy val StringConcatMethod = getMember(StringClass, TermName("concat"))

    lazy val BoxMethod = Map[Char, Symbol](
      'B' -> getDecl(BoxesRunTimeModule, TermName("boxToBoolean")),
      'C' -> getDecl(BoxesRunTimeModule, TermName("boxToCharacter")),
      'z' -> getDecl(RuntimeBoxesModule, TermName("boxToUByte")),
      'Z' -> getDecl(BoxesRunTimeModule, TermName("boxToByte")),
      's' -> getDecl(RuntimeBoxesModule, TermName("boxToUShort")),
      'S' -> getDecl(BoxesRunTimeModule, TermName("boxToShort")),
      'i' -> getDecl(RuntimeBoxesModule, TermName("boxToUInt")),
      'I' -> getDecl(BoxesRunTimeModule, TermName("boxToInteger")),
      'l' -> getDecl(RuntimeBoxesModule, TermName("boxToULong")),
      'L' -> getDecl(BoxesRunTimeModule, TermName("boxToLong")),
      'F' -> getDecl(BoxesRunTimeModule, TermName("boxToFloat")),
      'D' -> getDecl(BoxesRunTimeModule, TermName("boxToDouble"))
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
      'z' -> getDecl(RuntimeBoxesModule, TermName("unboxToUByte")),
      'Z' -> getDecl(BoxesRunTimeModule, TermName("unboxToByte")),
      's' -> getDecl(RuntimeBoxesModule, TermName("unboxToUShort")),
      'S' -> getDecl(BoxesRunTimeModule, TermName("unboxToShort")),
      'i' -> getDecl(RuntimeBoxesModule, TermName("unboxToUInt")),
      'I' -> getDecl(BoxesRunTimeModule, TermName("unboxToInt")),
      'l' -> getDecl(RuntimeBoxesModule, TermName("unboxToULong")),
      'L' -> getDecl(BoxesRunTimeModule, TermName("unboxToLong")),
      'F' -> getDecl(BoxesRunTimeModule, TermName("unboxToFloat")),
      'D' -> getDecl(BoxesRunTimeModule, TermName("unboxToDouble"))
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

    lazy val PureModules: Set[Symbol] =
      Set(PredefModule, BoxesRunTimeModule, NativeModule, RuntimeBoxesModule)
        .map(_.moduleClass)
    lazy val PureMethods: Set[Symbol] =
      (BoxMethod.values ++ UnboxMethod.values).toSet
  }
}
