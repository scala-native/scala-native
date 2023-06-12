package org.scalanative.testsuite.javalib.util.stream

import java.util.Spliterator

object StreamTestHelpers {

  // spliterator and, presumably, stream characteristic names
  private val maskNames = Map(
    0x00000001 -> "DISTINCT",
    0x00000004 -> "SORTED",
    0x00000010 -> "ORDERED",
    0x00000040 -> "SIZED",
    0x00000100 -> "NONNULL",
    0x00000400 -> "IMMUTABLE",
    0x00001000 -> "CONCURRENT",
    0x00004000 -> "SUBSIZED"
  )

  private def maskToName(mask: Int): String =
    maskNames.getOrElse(mask, s"0x${mask.toHexString.toUpperCase}")

  def verifyCharacteristics[T](
      splItr: Spliterator[T],
      requiredPresent: Seq[Int],
      requiredAbsent: Seq[Int]
  ): Unit = {
    /* The splItr.hasCharacteristics() and splItr.characteristics()
     * sections both seek the same information: Does the spliterator report
     * the required characteristics and no other. They ask the question
     * in slightly different ways to exercise each of the two Spliterator
     * methods. The answers should match, belt & suspenders.
     */

    for (rp <- requiredPresent) {
      assert(
        splItr.hasCharacteristics(rp),
        s"missing requiredPresent characteristic: ${maskToName(rp)}"
      )
    }

    for (rp <- requiredAbsent) {
      assert(
        !splItr.hasCharacteristics(rp),
        s"found requiredAbsent characteristic: ${maskToName(rp)}"
      )
    }

    val sc = splItr.characteristics()
    val requiredPresentMask = requiredPresent.fold(0)((x, y) => x | y)

    val unknownBits = sc & ~requiredPresentMask
    val unknownBitsMsg = s"0X${unknownBits.toHexString}"
    assert(
      0 == unknownBits,
      s"unexpected characteristics, unknown mask: ${unknownBitsMsg}"
    )
  }
}
