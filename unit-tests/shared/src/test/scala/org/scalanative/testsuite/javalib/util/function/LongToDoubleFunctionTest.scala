// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class LongToDoubleFunctionTest {
  @Test def testApply(): Unit = {
    val f = new LongToDoubleFunction {
      override def applyAsDouble(value: Long): Double = value.toDouble * 0.5
    }
    assertEquals(f.applyAsDouble(3), 1.5, 0.0)
  }
}
