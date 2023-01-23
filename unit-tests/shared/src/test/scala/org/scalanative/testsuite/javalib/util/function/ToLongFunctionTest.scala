// Ported from Scala.js commit 00e462d dated: 2023-01-22

package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class ToLongFunctionTest {
  @Test def applyAsLong(): Unit = {
    val op = new ToLongFunction[String] {
      override def applyAsLong(value: String): Long = value.toLong
    }
    assertEquals(op.applyAsLong("123456787654321"), 123456787654321L)
  }
}
