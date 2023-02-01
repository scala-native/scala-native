// Ported from Scala.js, commit sha:cfb4888a6 dated:2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class LongFunctionTest {
  @Test def testApply(): Unit = {
    val f = new LongFunction[Seq[Long]] {
      override def apply(value: Long): Seq[Long] = List.fill(value.toInt)(value)
    }
    assertEquals(f.apply(1L), Seq(1L))
    assertEquals(f.apply(3L), Seq(3L, 3L, 3L))
  }
}