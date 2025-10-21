// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import java.util.function._

import org.junit.Assert._
import org.junit.Test

class ToLongFunctionTest {
  @Test def applyAsLong(): Unit = {
    val op = new ToLongFunction[String] {
      override def applyAsLong(value: String): Long = value.toLong
    }
    assertEquals(op.applyAsLong("123456787654321"), 123456787654321L)
  }
}
