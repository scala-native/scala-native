package scala.bugcompat

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

  @Test def scalaBugIssue11253(): Unit = {
    assertEquals(noinlineLong() % noinlineFloat(), 2.3242621f, 0.0f)
    assertEquals(inlineLong() % inlineFloat(), 2.3242621f, 0.0f)
  }
}
