package scala.scalanative
package nscplugin

import scala.reflect.internal.Flags
import scala.tools.nsc._

trait NirCompat[G <: Global with Singleton] { self: NirPhase[G] =>
  import NirCompat.{infiniteLoop, noImplClasses}
  import global._

  /* SAMFunction was introduced in 2.12 for LMF-capable SAM types.
   * DottyEnumSingleton was introduced in 2.13.6 to identify Scala 3 `enum` singleton cases.
   */
  object AttachmentsCompatDef {
    case class SAMFunction(samTp: Type, sam: Symbol, synthCls: Symbol)
        extends PlainAttachment
    object DottyEnumSingleton extends PlainAttachment
  }

  object AttachmentsCompat {
    import AttachmentsCompatDef._

    object Inner {
      import global._

      type SAMFunctionAlias = SAMFunction
      val SAMFunctionAlias = SAMFunction

      val DottyEnumSingletonAlias = DottyEnumSingleton
    }
  }

  type SAMFunctionCompat = AttachmentsCompat.Inner.SAMFunctionAlias
  lazy val SAMFunctionCompat = AttachmentsCompat.Inner.SAMFunctionAlias

  lazy val DottyEnumSingletonCompat =
    AttachmentsCompat.Inner.DottyEnumSingletonAlias

  implicit final class SAMFunctionCompatOps(self: SAMFunctionCompat) {
    // Introduced in 2.12.5 to synthesize bridges in LMF classes
    def synthCls: Symbol = NoSymbol
  }

  implicit final class TyperCompatOps(self: NirCompat.this.global.typer.type) {
    def checkClassTypeOrModule(tpt: Tree): Boolean = {
      import typer._
      checkClassType(tpt)
    }

    object typer {
      def checkClassType(tpt: Tree): Boolean = infiniteLoop()
    }
  }

  implicit final class SymbolCompat(self: Symbol) {
    def originalOwner: Symbol =
      global.originalOwner.getOrElse(self, self.rawowner)

    def implClass: Symbol = NoSymbol

    def isTraitOrInterface: Boolean = self.isTrait || self.isInterface

    def isScala3Defined: Boolean = false
  }

  implicit final class GlobalCompat(self: NirCompat.this.global.type) {

    object originalOwner {
      def getOrElse(sym: Symbol, orElse: => Symbol): Symbol = infiniteLoop()
    }
  }

  private implicit final class FlagsCompat(self: Flags.type) {
    def IMPLCLASS: Long = infiniteLoop()
  }

  lazy val scalaUsesImplClasses: Boolean =
    definitions.SeqClass.implClass != NoSymbol // a trait we know has an impl class

  def isImplClass(sym: Symbol): Boolean =
    scalaUsesImplClasses && sym.hasFlag(Flags.IMPLCLASS)

  implicit final class StdTermNamesCompat(self: global.nme.type) {
    def IMPL_CLASS_SUFFIX: String = noImplClasses()

    def isImplClassName(name: Name): Boolean = false
  }

  implicit final class StdTypeNamesCompat(self: global.tpnme.type) {
    def IMPL_CLASS_SUFFIX: String = noImplClasses()

    def interfaceName(implname: Name): TypeName = noImplClasses()
  }

}

object NirCompat {
  private def infiniteLoop(): Nothing =
    throw new AssertionError("Infinite loop in NirCompat")

  private def noImplClasses(): Nothing =
    throw new AssertionError("No impl classes in this version")
}
