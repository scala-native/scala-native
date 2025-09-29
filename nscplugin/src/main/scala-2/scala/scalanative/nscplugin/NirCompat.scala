package scala.scalanative
package nscplugin

import scala.reflect.internal.Flags
import scala.tools.nsc._

trait NirCompat[G <: Global with Singleton] { self: NirPhase[G] =>
  import global._

  import NirCompat.infiniteLoop

  /* SAMFunction was introduced in 2.12 for LMF-capable SAM types.
   * DottyEnumSingleton was introduced in 2.13.6 to identify Scala 3 `enum` singleton cases.
   */
  object AttachmentsCompatDef {
    object DottyEnumSingleton extends PlainAttachment
  }

  object AttachmentsCompat {
    import AttachmentsCompatDef._

    object Inner {
      import global._

      val DottyEnumSingletonAlias = DottyEnumSingleton
    }
  }

  lazy val DottyEnumSingletonCompat =
    AttachmentsCompat.Inner.DottyEnumSingletonAlias

  implicit final class SAMFunctionCompatOps(self: SAMFunction) {
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
    def isTraitOrInterface: Boolean = self.isTrait || self.isInterface

    def isScala3Defined: Boolean = false
  }
}

object NirCompat {
  private def infiniteLoop(): Nothing =
    throw new AssertionError("Infinite loop in NirCompat")
}
