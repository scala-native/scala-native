// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class IntBinaryOperatorTest {
  @Test def applyAsInt(): Unit = {
    val max = new IntBinaryOperator {
      override def applyAsInt(left: Int, right: Int): Int = left.max(right)
    }
    assertEquals(max.applyAsInt(3, 5), 5)
    assertEquals(max.applyAsInt(0, -2), 0)
  }
}
