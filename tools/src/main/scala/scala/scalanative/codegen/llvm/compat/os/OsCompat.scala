package scala.scalanative
package codegen
package llvm
package compat.os

import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.util.ShowBuilder

private[codegen] abstract class OsCompat(
    protected val codegen: AbstractCodeGen
) {

  protected def osPersonalityType: String

  def useOpaquePointers = codegen.meta.platform.useOpaquePointers

  def genPrelude()(implicit sb: ShowBuilder): Unit
  def genLandingPad(
      unwind: nir.Next.Unwind
  )(implicit
      fresh: nir.Fresh,
      pos: nir.Position,
      sb: ShowBuilder
  ): Unit
  def genBlockAlloca(block: nir.ControlFlow.Block)(implicit
      sb: ShowBuilder
  ): Unit

  final lazy val gxxPersonality =
    if (useOpaquePointers) s"personality ptr $osPersonalityType"
    else s"personality i8* bitcast (i32 (...)* $osPersonalityType to i8*)"

}
