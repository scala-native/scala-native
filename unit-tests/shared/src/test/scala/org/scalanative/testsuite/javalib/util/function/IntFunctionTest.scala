// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert.*
import org.junit.Test

import java.util.function.*

class IntFunctionTest {
  @Test def testApply(): Unit = {
    val repeat = new IntFunction[String] {
      override def apply(value: Int): String = "." * value
    }
    assertEquals(repeat.apply(1), ".")
    assertEquals(repeat.apply(3), "...")
  }
}
