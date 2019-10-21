package scala.scalanative
package nscplugin

import scala.collection.mutable
import scala.reflect.internal.Flags
import scala.tools.nsc._

trait NirCompat { self: NirGenPhase =>
  import global._

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
}
