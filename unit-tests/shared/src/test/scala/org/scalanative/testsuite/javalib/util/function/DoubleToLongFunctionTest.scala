// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import java.util.function._

import org.junit.Assert._
import org.junit.Test

class DoubleToLongFunctionTest {
  @Test def applyAsLong(): Unit = {
    val f = new DoubleToLongFunction {
      override def applyAsLong(value: Double): Long = (10 * value).toLong
    }
    assertEquals(f.applyAsLong(0.5), 5L)
    assertEquals(f.applyAsLong(3.3), 33L)
  }
}
