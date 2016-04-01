package scala.scalanative
package nscplugin

import scala.tools.nsc._

trait NirDefinitions { self: NirGlobalAddons =>
  import global._
  import definitions._
  import rootMirror._

  object nirDefinitions {
    lazy val boxToCharacterMethod = getDecl(BoxesRunTimeModule, TermName("boxToCharacter"))
    lazy val boxToBooleanMethod   = getDecl(BoxesRunTimeModule, TermName("boxToBoolean"))
    lazy val boxToByteMethod      = getDecl(BoxesRunTimeModule, TermName("boxToByte"))
    lazy val boxToShortMethod     = getDecl(BoxesRunTimeModule, TermName("boxToShort"))
    lazy val boxToIntMethod       = getDecl(BoxesRunTimeModule, TermName("boxToInteger"))
    lazy val boxToLongMethod      = getDecl(BoxesRunTimeModule, TermName("boxToLong"))
    lazy val boxToFloatMethod     = getDecl(BoxesRunTimeModule, TermName("boxToFloat"))
    lazy val boxToDoubleMethod    = getDecl(BoxesRunTimeModule, TermName("boxToDouble"))

    lazy val unboxToCharacterMethod = getDecl(BoxesRunTimeModule, TermName("unboxToChar"))
    lazy val unboxToBooleanMethod   = getDecl(BoxesRunTimeModule, TermName("unboxToBoolean"))
    lazy val unboxToByteMethod      = getDecl(BoxesRunTimeModule, TermName("unboxToByte"))
    lazy val unboxToShortMethod     = getDecl(BoxesRunTimeModule, TermName("unboxToShort"))
    lazy val unboxToIntMethod       = getDecl(BoxesRunTimeModule, TermName("unboxToInt"))
    lazy val unboxToLongMethod      = getDecl(BoxesRunTimeModule, TermName("unboxToLong"))
    lazy val unboxToFloatMethod     = getDecl(BoxesRunTimeModule, TermName("unboxToFloat"))
    lazy val unboxToDoubleMethod    = getDecl(BoxesRunTimeModule, TermName("unboxToDouble"))

    lazy val boxToMethod = Map[Symbol, Symbol](
      CharClass    -> boxToCharacterMethod,
      BooleanClass -> boxToBooleanMethod,
      ByteClass    -> boxToByteMethod,
      ShortClass   -> boxToShortMethod,
      IntClass     -> boxToIntMethod,
      LongClass    -> boxToLongMethod,
      FloatClass   -> boxToFloatMethod,
      DoubleClass  -> boxToDoubleMethod
    )

    lazy val unboxToMethod = Map[Symbol, Symbol](
      CharClass    -> unboxToCharacterMethod,
      BooleanClass -> unboxToBooleanMethod,
      ByteClass    -> unboxToByteMethod,
      ShortClass   -> unboxToShortMethod,
      IntClass     -> unboxToIntMethod,
      LongClass    -> unboxToLongMethod,
      FloatClass   -> unboxToFloatMethod,
      DoubleClass  -> unboxToDoubleMethod
    )

    lazy val AnyArrayClass     = getRequiredClass("scala.scalanative.runtime.Array")
    lazy val BooleanArrayClass = getRequiredClass("scala.scalanative.runtime.BooleanArray")
    lazy val CharArrayClass    = getRequiredClass("scala.scalanative.runtime.CharArray")
    lazy val ByteArrayClass    = getRequiredClass("scala.scalanative.runtime.ByteArray")
    lazy val ShortArrayClass   = getRequiredClass("scala.scalanative.runtime.ShortArray")
    lazy val IntArrayClass     = getRequiredClass("scala.scalanative.runtime.IntArray")
    lazy val LongArrayClass    = getRequiredClass("scala.scalanative.runtime.LongArray")
    lazy val FloatArrayClass   = getRequiredClass("scala.scalanative.runtime.FloatArray")
    lazy val DoubleArrayClass  = getRequiredClass("scala.scalanative.runtime.DoubleArray")
    lazy val RefArrayClass     = getRequiredClass("scala.scalanative.runtime.RefArray")

    lazy val Object_wait = getMember(ObjectClass, TermName("wait"))
  }
}
