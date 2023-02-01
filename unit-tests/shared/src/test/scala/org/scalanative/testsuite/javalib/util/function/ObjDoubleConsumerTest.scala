// Ported from Scala.js, commit sha:cfb4888a6 dated:2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert._
import org.junit.Test

import java.util.function._

class ObjDoubleConsumerTest {
  @Test def accept(): Unit = {
    // side-effects
    var current: String = ""

    val op = new ObjDoubleConsumer[String] {
      override def accept(left: String, right: Double): Unit = current += s"$left $right "
    }

    op.accept("First", 1.1)
    op.accept("Second", 2.2)
    assertEquals(current, "First 1.1 Second 2.2 ")
  }
}