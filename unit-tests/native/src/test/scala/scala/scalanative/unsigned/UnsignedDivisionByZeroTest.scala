package scala.scalanative.unsigned

import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/** An unsigned division by a constant zero throws, also once the optimizer has
 *  rewritten divisions by powers of two.
 */
class UnsignedDivisionByZeroTest {
  @noinline def uintByZero(x: Int): Int = (x.toUInt / 0.toUInt).toInt
  @noinline def ulongByZero(x: Long): Long = (x.toULong / 0.toULong).toLong

  @Test def uintDivisionByZero(): Unit =
    assertThrows(classOf[ArithmeticException], uintByZero(7))

  @Test def ulongDivisionByZero(): Unit =
    assertThrows(classOf[ArithmeticException], ulongByZero(7L))
}
