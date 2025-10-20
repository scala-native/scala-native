// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert.*
import org.junit.Test

import java.util.function.*

class DoubleBinaryOperatorTest {
  @Test def applyAsDouble(): Unit = {
    val sumOp = new DoubleBinaryOperator {
      override def applyAsDouble(left: Double, right: Double): Double =
        left + right
    }
    assertEquals(30, sumOp.applyAsDouble(10, 20), 0)
  }
}
