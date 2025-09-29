// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import java.util.function._

import org.junit.Assert._
import org.junit.Test

class LongToIntFunctionTest {
  @Test def testApply(): Unit = {
    val f = new LongToIntFunction {
      override def applyAsInt(value: Long): Int = value.toInt / 2
    }
    assertEquals(f.applyAsInt(3), 1)
  }
}
