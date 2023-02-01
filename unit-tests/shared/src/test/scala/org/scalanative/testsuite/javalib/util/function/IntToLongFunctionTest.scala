// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class IntToLongFunctionTest {
  @Test def testApply(): Unit = {
    val f = new IntToLongFunction {
      override def applyAsLong(value: Int): Long = value.toLong * Int.MaxValue
    }
    assertEquals(f.applyAsLong(3), 6442450941L)
  }
}
