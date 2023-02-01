// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class ToDoubleFunctionTest {
  @Test def applyAsDouble(): Unit = {
    val op = new ToDoubleFunction[String] {
      override def applyAsDouble(value: String): Double = s"$value.5".toDouble
    }
    assertEquals(op.applyAsDouble("1"), 1.5, 0.0)
  }
}
