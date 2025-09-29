// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import java.util.function._

import org.junit.Assert._
import org.junit.Test

class DoubleToIntFunctionTest {
  @Test def applyAsInt(): Unit = {
    val f = new DoubleToIntFunction {
      override def applyAsInt(value: Double): Int = value.toInt
    }
    assertEquals(f.applyAsInt(0.5), 0)
    assertEquals(f.applyAsInt(3.3), 3)
  }
}
