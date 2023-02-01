// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class IntToDoubleFunctionTest {
  @Test def testApply(): Unit = {
    val f = new IntToDoubleFunction {
      override def applyAsDouble(value: Int): Double = value.toDouble / 10d
    }
    assertEquals(f.applyAsDouble(3), 0.3, 0.0)
    assertEquals(f.applyAsDouble(20), 2, 0.0)
  }
}
