// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import java.util.function._

import org.junit.Assert._
import org.junit.Test

class DoubleFunctionTest {
  @Test def testApply(): Unit = {
    val f = new DoubleFunction[String] {
      override def apply(value: Double): String = s"${value}d"
    }
    assertEquals(f.apply(0.5), "0.5d")
    assertEquals(f.apply(3.3), "3.3d")
  }
}
