// Ported from Scala.js, commit SHA: 1ef4c4e0f dated: 2020-09-06
package org.scalanative.testsuite.javalib.util.function

import java.util.function.BinaryOperator

import org.junit.Assert._
import org.junit.Test

class BinaryOperatorTest {
  @Test def minBy(): Unit = {
    val binOp: BinaryOperator[Int] = BinaryOperator.minBy(Ordering[Int])
    assertEquals(10, binOp.apply(10, 20))
  }

  @Test def maxBy(): Unit = {
    val binOp: BinaryOperator[Int] = BinaryOperator.maxBy(Ordering[Int])
    assertEquals(20, binOp.apply(10, 20))
  }
}
