package scala.scalanative.codegen.compat.os

import scala.scalanative.nir.ControlFlow.Block
import scala.scalanative.nir.{Fresh, Next, Position}
import scala.scalanative.util.ShowBuilder

private[codegen] trait OsCompat {

  protected def osPersonalityType: String

  def genPrelude()(implicit sb: ShowBuilder): Unit
  def genLandingPad(unwind: Next.Unwind)(implicit fresh: Fresh,
                                         pos: Position,
                                         sb: ShowBuilder): Unit
  def genBlockAlloca(block: Block)(implicit sb: ShowBuilder): Unit

  final lazy val gxxPersonality =
    s"personality i8* bitcast (i32 (...)* $osPersonalityType to i8*)"

}
