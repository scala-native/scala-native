// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class ToIntFunctionTest {
  @Test def applyAsInt(): Unit = {
    val op = new ToIntFunction[String] {
      override def applyAsInt(value: String): Int = value.length
    }
    assertEquals(op.applyAsInt("abc"), 3)
  }
}
