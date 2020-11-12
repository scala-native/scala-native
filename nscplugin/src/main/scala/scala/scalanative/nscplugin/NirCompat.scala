package scala.scalanative
package nscplugin

import scala.reflect.internal.Flags
import scala.tools.nsc._

trait NirCompat[G <: Global with Singleton] { self: NirGenPhase[G] =>
  import NirCompat.{infiniteLoop, noImplClasses}
  import global._

  // SAMFunction was introduced in 2.12 for LMF-capable SAM type

  object SAMFunctionAttachCompatDef {
    case class SAMFunction(samTp: Type, sam: Symbol, synthCls: Symbol)
        extends PlainAttachment
  }

  object SAMFunctionAttachCompat {
    import SAMFunctionAttachCompatDef._

    object Inner {
      import global._

      type SAMFunctionAlias = SAMFunction
      val SAMFunctionAlias = SAMFunction
    }
  }

  type SAMFunctionCompat = SAMFunctionAttachCompat.Inner.SAMFunctionAlias
  lazy val SAMFunctionCompat = SAMFunctionAttachCompat.Inner.SAMFunctionAlias

  implicit final class SAMFunctionCompatOps(self: SAMFunctionCompat) {
    // Introduced in 2.12.5 to synthesize bridges in LMF classes
    def synthCls: Symbol = NoSymbol
  }

  implicit final class SymbolCompat(self: Symbol) {
    def originalOwner: Symbol =
      global.originalOwner.getOrElse(self, self.rawowner)

    def implClass: Symbol = NoSymbol

    def isTraitOrInterface: Boolean = self.isTrait || self.isInterface
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
