// Ported from Scala.js, commit sha:cfb4888a6 dated:2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class DoubleUnaryOperatorTest {
  private val minus5 = new DoubleUnaryOperator {
    override def applyAsDouble(operand: Double): Double = operand - 5
  }
  private val times2 = new DoubleUnaryOperator {
    override def applyAsDouble(operand: Double): Double = operand * 2
  }

  @Test def applyAsDouble(): Unit = {
    val times4 = new DoubleUnaryOperator {
      override def applyAsDouble(operand: Double): Double = operand * 4
    }
    assertEquals(times4.applyAsDouble(0.5), 2.0, 0)
    assertEquals(times4.applyAsDouble(3.3), 13.2, 0)
  }

  @Test def andThen(): Unit = {
    val f: DoubleUnaryOperator = minus5.andThen(times2)
    assertEquals(f.applyAsDouble(3), -4, 0)
  }

  @Test def compose(): Unit = {
    val f: DoubleUnaryOperator = minus5.compose(times2)
    assertEquals(f.applyAsDouble(3), 1, 0)
  }

  @Test def identity(): Unit = {
    val id: DoubleUnaryOperator = DoubleUnaryOperator.identity()
    assertEquals(id.applyAsDouble(3), 3, 0)
    assertEquals(id.applyAsDouble(10), 10, 0)
  }
}