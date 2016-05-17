package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirDefinitions { self: NirGlobalAddons =>
  import global._
  import definitions._
  import rootMirror._

  object nirDefinitions {
    lazy val PtrClass        = getRequiredClass("scala.scalanative.native.Ptr")
    lazy val PtrLoadMethod   = getDecl(PtrClass, TermName("unary_$bang"))
    lazy val PtrStoreMethod  = getDecl(PtrClass, TermName("unary_$bang_$eq"))
    lazy val PtrAddMethod    = getDecl(PtrClass, TermName("$plus"))
    lazy val PtrSubMethod    = getDecl(PtrClass, TermName("$minus"))
    lazy val PtrApplyMethod  = getDecl(PtrClass, TermName("apply"))
    lazy val PtrUpdateMethod = getDecl(PtrClass, TermName("update"))

    lazy val NObjectClass = getRequiredClass("java.lang._Object")
    lazy val NObjectHashCodeMethod = getDecl(
        NObjectClass, TermName("_hashCode"))
    lazy val NObjectEqualsMethod = getDecl(NObjectClass, TermName("_equals"))
    lazy val NObjectInitMethod   = getDecl(NObjectClass, TermName("<init>"))

    lazy val NMonitorClass = getRequiredClass(
        "scala.scalanative.runtime.Monitor")
    lazy val NMonitorModule = getRequiredModule(
        "scala.scalanative.runtime.Monitor")
    lazy val NMonitorEnterMethod = getDecl(NMonitorClass, TermName("enter"))
    lazy val NMonitorExitMethod  = getDecl(NMonitorClass, TermName("exit"))
    lazy val NMonitorGetMethod   = getDecl(NMonitorModule, TermName("get"))

    lazy val NTypeClass     = getRequiredClass("scala.scalanative.runtime.Type")
    lazy val NTypeModule    = getRequiredModule("scala.scalanative.runtime.Type")
    lazy val NTypeGetMethod = getDecl(NTypeModule, TermName("get"))

    lazy val NStringClass  = getRequiredClass("java.lang._String")
    lazy val NStringModule = getRequiredModule("java.lang._String")

    lazy val InlineClass   = getRequiredClass("scala.inline")
    lazy val NoInlineClass = getRequiredClass("scala.noinline")
    lazy val MustInlineClass = getRequiredClass(
        "scala.scalanative.runtime.mustinline")
    lazy val PureClass = getRequiredClass("scala.scalanative.runtime.pure")

    lazy val NativeModule = getRequiredModule(
        "scala.scalanative.native.package")
    lazy val CastMethod       = getMember(NativeModule, TermName("cast"))
    lazy val ExternMethod     = getMember(NativeModule, TermName("extern"))
    lazy val SizeofMethod     = getMember(NativeModule, TermName("sizeof"))
    lazy val StackallocMethod = getMember(NativeModule, TermName("stackalloc"))
    lazy val ExternClass      = getRequiredClass("scala.scalanative.native.extern")
    lazy val StructClass      = getRequiredClass("scala.scalanative.native.struct")

    lazy val RuntimeModule = getRequiredModule(
        "scala.scalanative.runtime.package")
    lazy val InfoofMethod = getMember(RuntimeModule, TermName("infoof"))

    lazy val CQuoteClass = getRequiredClass(
        "scala.scalanative.native.package$CQuote")
    lazy val CQuoteMethod = getDecl(CQuoteClass, TermName("c"))

    lazy val CCastClass = getRequiredClass(
        "scala.scalanative.native.package$CCast")
    lazy val CCastMethod = getDecl(CCastClass, TermName("cast"))

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

    lazy val NArrayClass: Map[Char, Symbol] = Map(
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

    lazy val NArrayModule: Map[Char, Symbol] =
      NArrayClass.mapValues(_.companion)

    lazy val NArrayAllocMethod: Map[Char, Symbol] =
      NArrayModule.mapValues(getMember(_, TermName("alloc")))

    lazy val NArrayApplyMethod: Map[Char, Symbol] =
      NArrayClass.mapValues(getMember(_, TermName("apply")))

    lazy val NArrayUpdateMethod: Map[Char, Symbol] =
      NArrayClass.mapValues(getMember(_, TermName("update")))

    lazy val NArrayLengthMethod: Map[Char, Symbol] =
      NArrayClass.mapValues(getMember(_, TermName("length")))

    lazy val NArrayCloneMethod: Map[Char, Symbol] =
      NArrayClass.mapValues(getMember(_, TermName("clone")))

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
