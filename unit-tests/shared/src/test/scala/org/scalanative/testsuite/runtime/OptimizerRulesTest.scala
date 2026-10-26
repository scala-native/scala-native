package org.scalanative.testsuite.runtime

import org.junit.Assert._
import org.junit.Test

/** Results of code that the optimizer folds or rewrites. The methods take their
 *  operands as arguments, so that only the optimizer sees the constants.
 */
class OptimizerRulesTest {
  @noinline def maxGt(x: Int): Boolean = Int.MaxValue > x
  @noinline def gtMax(x: Int): Boolean = x > Int.MaxValue
  @noinline def minLt(x: Int): Boolean = Int.MinValue < x
  @noinline def ltMin(x: Int): Boolean = x < Int.MinValue
  @noinline def minGe(x: Long): Boolean = Long.MinValue >= x
  @noinline def maxLe(x: Long): Boolean = Long.MaxValue <= x
  @noinline def gtOrEq(x: Int, y: Int): Boolean = (x > y) | (x == y)

  @Test def comparisonsWithMinAndMaxValues(): Unit = {
    assertTrue(maxGt(0))
    assertFalse(maxGt(Int.MaxValue))
    assertFalse(gtMax(0))
    assertTrue(minLt(0))
    assertFalse(minLt(Int.MinValue))
    assertFalse(ltMin(0))
    assertFalse(minGe(0L))
    assertTrue(minGe(Long.MinValue))
    assertFalse(maxLe(0L))
    assertTrue(maxLe(Long.MaxValue))
  }

  @Test def combinedComparisons(): Unit = {
    assertTrue(gtOrEq(2, 1))
    assertTrue(gtOrEq(1, 1))
    assertFalse(gtOrEq(0, 1))
  }
}
