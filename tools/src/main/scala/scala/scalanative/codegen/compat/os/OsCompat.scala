package scala.scalanative.codegen.compat.os

import scala.scalanative.nir.ControlFlow.Block
import scala.scalanative.nir.{Fresh, Next, Position}
import scala.scalanative.util.ShowBuilder
import scala.scalanative.codegen.dwarf.GenIdx
import scala.scalanative.codegen.dwarf.DwarfSection
import scala.scalanative.nir.Defn
import scala.scalanative.nir.Global

private[codegen] trait OsCompat {

  protected def osPersonalityType: String

  def genPrelude()(implicit sb: ShowBuilder): Unit
  def genLandingPad(
      unwind: Next.Unwind
  )(implicit
      fresh: Fresh,
      pos: Position,
      sb: ShowBuilder,
      gidx: GenIdx,
      dwf: DwarfSection.Builder[Global]
  ): Unit
  def genBlockAlloca(block: Block)(implicit sb: ShowBuilder): Unit

  final lazy val gxxPersonality =
    s"personality i8* bitcast (i32 (...)* $osPersonalityType to i8*)"

}
