package scala

import org.junit.Assert._
import org.junit.Test

class LongFloatPrimitiveTest {
  @inline def inlineFloat(): Float =
    java.lang.Float.intBitsToFloat(1079290514)
  @noinline def noinlineFloat(): Float =
    java.lang.Float.intBitsToFloat(1079290514)
  @inline def inlineLong(): Long =
    1412906027847L
  @noinline def noinlineLong(): Long =
    1412906027847L

  // scala/bug#11253: Long op Float is computed in Float precision
  @Test def longFloatBinaryOperationsUseFloatPrecision(): Unit = {
    assertEquals(1.164309f, noinlineLong() % noinlineFloat(), 0.0f)
    assertEquals(1.164309f, inlineLong() % inlineFloat(), 0.0f)
    assertEquals(
      noinlineLong().toFloat % noinlineFloat(),
      noinlineLong() % noinlineFloat(),
      0.0f
    )
    assertEquals(
      noinlineLong().toFloat / noinlineFloat(),
      noinlineLong() / noinlineFloat(),
      0.0f
    )
    assertEquals(
      noinlineFloat() * noinlineLong().toFloat,
      noinlineFloat() * noinlineLong(),
      0.0f
    )
  }
}
