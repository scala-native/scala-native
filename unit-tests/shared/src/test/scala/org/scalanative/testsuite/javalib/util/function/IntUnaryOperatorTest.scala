// Ported from Scala.js commit: d028054 dated: 2022-05-16

package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class IntUnaryOperatorTest {
  private val f = new IntUnaryOperator {
    override def applyAsInt(operand: Int): Int = operand - 1
  }
  private val g = new IntUnaryOperator {
    override def applyAsInt(operand: Int): Int = operand * 2
  }

  @Test def applyAsInt(): Unit = {
    assertEquals(f.applyAsInt(3), 2)
  }

  @Test def andThen(): Unit = {
    val h: IntUnaryOperator = f.andThen(g)
    assertEquals(h.applyAsInt(5), 8)
  }

  @Test def compose(): Unit = {
    val h: IntUnaryOperator = f.compose(g)
    assertEquals(h.applyAsInt(5), 9)
  }

  @Test def identity(): Unit = {
    val f: IntUnaryOperator = IntUnaryOperator.identity()
    assertEquals(1, f.applyAsInt(1))
  }
}
