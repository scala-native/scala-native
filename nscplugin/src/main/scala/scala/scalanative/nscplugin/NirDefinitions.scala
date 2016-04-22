package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirDefinitions { self: NirGlobalAddons =>
  import global._
  import definitions._
  import rootMirror._

  object nirDefinitions {
    lazy val NStringClass  = getRequiredClass("java.lang._String")
    lazy val NStringModule = getRequiredModule("java.lang._String")

    lazy val InlineClass   = getRequiredClass("scala.inline")
    lazy val NoInlineClass = getRequiredClass("scala.noinline")

    lazy val NativeModule = getRequiredModule("scala.scalanative.native.package")
    lazy val ExternMethod = getMember(NativeModule, TermName("extern"))
    lazy val ExternClass  = getRequiredClass("scala.scalanative.native.extern")

    lazy val Object_wait = getMember(ObjectClass, TermName("wait"))

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
      'O' -> getRequiredClass("scala.scalanative.runtime.RefArray")
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
  }
}
