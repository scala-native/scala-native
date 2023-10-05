package org.scalanative.testsuite.javalib.lang

import org.junit.Test
import org.junit.Assert._

class MathTestOnJDK9 {

  @Test def testFma(): Unit = {
    assertEquals(10.0f, Math.fma(2.0f, 3.0f, 4.0f), 0.0f)
    assertEquals(10.0, Math.fma(2.0, 3.0, 4.0), 0.0)
  }

  @Test def multiplyHighTests(): Unit = {
    case class MHTest(a: Long, b: Long, expected: Long)
    val maxval = java.lang.Long.MAX_VALUE
    val minval = java.lang.Long.MIN_VALUE
    val halfmax = maxval >> 32
    val halfmin = minval >> 32

    val testcases: List[MHTest] =
      MHTest(maxval, maxval, 4611686018427387903L) ::
        MHTest(maxval, minval, -4611686018427387904L) ::
        MHTest(minval, minval, 4611686018427387904L) ::
        MHTest(maxval, 0L, 0L) ::
        MHTest(minval, 0L, 0L) ::
        MHTest(0L, 0L, 0L) ::
        MHTest(maxval, halfmax, 1073741823L) ::
        MHTest(maxval, halfmin, -1073741824L) ::
        MHTest(halfmax, halfmin, -1L) ::
        MHTest(halfmin, halfmin, 0L) ::
        MHTest(halfmax, 127L, 0L) ::
        MHTest(halfmax * 42L, halfmax * 1337L, 14038L) ::
        MHTest(halfmin * 42L, halfmax * 1337L, -14039L) ::
        MHTest(13L, 37L, 0L) ::
        MHTest(123456789123456789L, 987654321L, 6609981L) ::
        MHTest(123123456456789789L, 998877665544332211L, 6667044887047954L) ::
        MHTest(-123123456456789789L, 998877665544332211L, -6667044887047955L) ::
        MHTest(123123456456789789L, -998877665544332211L, -6667044887047955L) ::
        MHTest(-123123456456789789L, -998877665544332211L, 6667044887047954L) ::
        Nil

    for (tc <- testcases) {
      val result = Math.multiplyHigh(tc.a, tc.b)
      assertTrue(
        s"Math.multiplyHigh(${tc.a}, ${tc.b}) result: ${result} != expected: ${tc.expected}",
        Math.multiplyHigh(tc.a, tc.b) == tc.expected
      )
    }
  }
}
