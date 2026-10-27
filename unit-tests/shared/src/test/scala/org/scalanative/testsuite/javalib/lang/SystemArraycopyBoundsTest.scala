package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* Positions and lengths whose sum is past Int.MaxValue: as an Int, the sum
 * wraps around to a negative number, which a bounds check written as
 * `pos + length > array.length` lets pass.
 */
class SystemArraycopyBoundsTest {
  private val cases = Seq(
    // (srcPos, destPos, length)
    (1, 1, Int.MaxValue),
    (Int.MaxValue, 0, 1),
    (0, Int.MaxValue, 1),
    (5, 5, Int.MaxValue - 4),
    (Int.MaxValue, Int.MaxValue, Int.MaxValue)
  )

  @Test def intArrayPositionPlusLengthPastIntMaxValue(): Unit =
    for ((srcPos, destPos, length) <- cases) {
      val src = Array.tabulate(10)(i => i + 1)
      val dest = new Array[Int](10)
      assertThrows(
        s"arraycopy($srcPos, $destPos, $length)",
        classOf[ArrayIndexOutOfBoundsException],
        System.arraycopy(src, srcPos, dest, destPos, length)
      )
      assertArrayEquals(new Array[Int](10), dest)
    }

  @Test def objectArrayPositionPlusLengthPastIntMaxValue(): Unit =
    for ((srcPos, destPos, length) <- cases) {
      val src = Array.tabulate[AnyRef](10)(i => Integer.valueOf(i))
      val dest = new Array[AnyRef](10)
      assertThrows(
        s"arraycopy($srcPos, $destPos, $length)",
        classOf[ArrayIndexOutOfBoundsException],
        System.arraycopy(src, srcPos, dest, destPos, length)
      )
      assertTrue(dest.forall(_ == null))
    }

  @Test def copyWithinTheBoundsStillWorks(): Unit = {
    val src = Array.tabulate(10)(i => i + 1)
    val dest = new Array[Int](10)
    System.arraycopy(src, 3, dest, 5, 5)
    assertArrayEquals(Array(0, 0, 0, 0, 0, 4, 5, 6, 7, 8), dest)
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      System.arraycopy(src, 3, dest, 5, 6)
    )
    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      System.arraycopy(src, 5, dest, 3, 6)
    )
  }
}
