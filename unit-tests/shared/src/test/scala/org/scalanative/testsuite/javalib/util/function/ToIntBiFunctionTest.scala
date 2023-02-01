// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class ToIntBiFunctionTest {
  @Test def applyAsInt(): Unit = {
    val op = new ToIntBiFunction[String, String] {
      override def applyAsInt(t: String, u: String): Int = s"$t$u".toInt
    }
    assertEquals(op.applyAsInt("10", "24"), 1024)
  }
}
