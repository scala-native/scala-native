// Ported from Scala.js, commit sha:cfb4888a6 dated:2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class LongToIntFunctionTest {
  @Test def testApply(): Unit = {
    val f = new LongToIntFunction {
      override def applyAsInt(value: Long): Int = value.toInt / 2
    }
    assertEquals(f.applyAsInt(3), 1)
  }
}