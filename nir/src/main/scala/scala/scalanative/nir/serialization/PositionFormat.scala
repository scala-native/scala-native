package scala.scalanative.nir.serialization

// Ported from Scala.js

private[serialization] object PositionFormat {
  /* Positions are serialized incrementally as diffs wrt the last position.
   *
   * Formats are (the first byte is decomposed in bits):
   *
   *   1st byte | next bytes |  description
   *  -----------------------------------------
   *   ccccccc0 |            | Column diff (7-bit signed)
   *   llllll01 | CC         | Line diff (6-bit signed), column (8-bit unsigned)
   *   ____0011 | LL LL CC   | Line diff (16-bit signed), column (8-bit unsigned)
   *   ____0111 | 12 bytes   | File index, line, column (all 32-bit signed)
   *   11111111 |            | NoPosition (is not compared/stored in last position)
   *
   * Underscores are irrelevant and must be set to 0.
   */

  final val Format1Mask      = 0x01
  final val Format1MaskValue = 0x00
  final val Format1Shift     = 1

  final val Format2Mask      = 0x03
  final val Format2MaskValue = 0x01
  final val Format2Shift     = 2

  final val Format3Mask      = 0x0f
  final val Format3MaskValue = 0x03

  final val FormatFullMask      = 0x0f
  final val FormatFullMaskValue = 0x7

  final val FormatNoPositionValue = -1
}
