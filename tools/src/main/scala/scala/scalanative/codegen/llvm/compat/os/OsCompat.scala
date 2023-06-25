package scala.scalanative.codegen
package llvm
package compat.os

import scala.scalanative.nir.ControlFlow.Block
import scala.scalanative.nir.{Fresh, Next, Position}
import scala.scalanative.util.ShowBuilder

private[codegen] trait OsCompat {
  protected def codegen: AbstractCodeGen
  protected def osPersonalityType: String

  def useOpaquePointers = codegen.meta.platform.useOpaquePointers

  def genPrelude()(implicit sb: ShowBuilder): Unit
  def genLandingPad(
      unwind: Next.Unwind
  )(implicit
      fresh: Fresh,
      pos: Position,
      sb: ShowBuilder,
      dwf: MetadataCodeGen.Context
  ): Unit
  def genBlockAlloca(block: Block)(implicit sb: ShowBuilder): Unit

  final lazy val gxxPersonality =
    if (useOpaquePointers) s"personality ptr $osPersonalityType"
    else s"personality i8* bitcast (i32 (...)* $osPersonalityType to i8*)"
}
