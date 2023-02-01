// Ported from Scala.js, commit sha:cfb4888a6 dated:2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class ToLongBiFunctionTest {
  @Test def applyAsLong(): Unit = {
    val op = new ToLongBiFunction[String, String] {
      override def applyAsLong(t: String, u: String): Long = t.toLong * u.toLong
    }
    assertEquals(op.applyAsLong("11111111", "2222222"), 24691355308642L)
  }
}