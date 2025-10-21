// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import java.util.function._

import org.junit.Assert._
import org.junit.Test

class ObjIntConsumerTest {
  @Test def accept(): Unit = {
    // side-effects
    var current: String = ""

    val op = new ObjIntConsumer[String] {
      override def accept(left: String, right: Int): Unit =
        current += left * right
    }

    op.accept("First", 1)
    op.accept("Second", 2)
    assertEquals(current, "FirstSecondSecond")
  }
}
