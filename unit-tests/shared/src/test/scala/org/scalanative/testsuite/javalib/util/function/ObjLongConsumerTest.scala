// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert.*
import org.junit.Test

import java.util.function.*

class ObjLongConsumerTest {
  @Test def accept(): Unit = {
    // side-effects
    var current: String = ""

    val op = new ObjLongConsumer[String] {
      override def accept(left: String, right: Long): Unit =
        current += s"$left $right "
    }
    op.accept("First", 2L)
    op.accept("Second", 3L)
    assertEquals(current, "First 2 Second 3 ")
  }
}
