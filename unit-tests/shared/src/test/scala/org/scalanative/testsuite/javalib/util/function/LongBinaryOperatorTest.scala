// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert.*
import org.junit.Test

import java.util.function.*

class LongBinaryOperatorTest {
  @Test def applyAsLong(): Unit = {
    val sumOp = new LongBinaryOperator {
      override def applyAsLong(left: Long, right: Long): Long = left + right
    }
    assertEquals(30, sumOp.applyAsLong(10, 20))
  }
}
