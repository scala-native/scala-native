// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert.*
import org.junit.Test

import java.util.function.*

class ToDoubleBiFunctionTest {
  @Test def applyAsDouble(): Unit = {
    val op = new ToDoubleBiFunction[String, String] {
      override def applyAsDouble(t: String, u: String): Double =
        s"$t.$u".toDouble
    }
    assertEquals(op.applyAsDouble("123", "456"), 123.456, 0.0)
  }
}
