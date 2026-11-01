package org.scalanative.testsuite.runtime

import java.lang.Double.doubleToRawLongBits
import java.lang.Float.floatToRawIntBits

import org.junit.Assert._
import org.junit.Test

/** Array literals are allocated zero-initialized; `-0.0` is numerically zero
 *  but is not the value held by zeroed memory, so its store must be kept.
 */
class ArrayLiteralNegativeZeroTest {
  private final val NegZeroDoubleBits = doubleToRawLongBits(-0.0d)
  private final val NegZeroFloatBits = floatToRawIntBits(-0.0f)

  @noinline def negZeroDoubles(): Array[Double] = Array(-0.0d)
  @noinline def mixedZeroDoubles(): Array[Double] = Array(0.0d, -0.0d)
  @noinline def negZeroFloats(): Array[Float] = Array(-0.0f)
  @noinline def mixedZeroFloats(): Array[Float] = Array(0.0f, -0.0f)

  @Test def allZeroDoubleLiteralKeepsSign(): Unit = {
    assertEquals(NegZeroDoubleBits, doubleToRawLongBits(negZeroDoubles()(0)))

    val mixed = mixedZeroDoubles()
    assertEquals(0L, doubleToRawLongBits(mixed(0)))
    assertEquals(NegZeroDoubleBits, doubleToRawLongBits(mixed(1)))
  }

  @Test def allZeroFloatLiteralKeepsSign(): Unit = {
    assertEquals(NegZeroFloatBits, floatToRawIntBits(negZeroFloats()(0)))

    val mixed = mixedZeroFloats()
    assertEquals(0, floatToRawIntBits(mixed(0)))
    assertEquals(NegZeroFloatBits, floatToRawIntBits(mixed(1)))
  }
}
