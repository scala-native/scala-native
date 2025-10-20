// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert.*
import org.junit.Test

import java.util.function.*

class IntConsumerTest {
  @Test def accept(): Unit = {
    // side-effects
    var current: Int = 0

    val add = new IntConsumer {
      override def accept(value: Int): Unit = current += value
    }

    add.accept(3)
    assertEquals(current, 3)
    add.accept(-10)
    assertEquals(current, -7)
  }

  @Test def andThen(): Unit = {
    // side-effects
    var buffer = scala.collection.mutable.ListBuffer.empty[Int]

    val add = new IntConsumer {
      override def accept(value: Int): Unit = buffer += value
    }
    val add10x = new IntConsumer {
      override def accept(value: Int): Unit = buffer += value * 10
    }
    val f: IntConsumer = add.andThen(add10x)

    f.accept(1)
    assertEquals(List(1, 10), buffer.toList)
    f.accept(2)
    assertEquals(List(1, 10, 2, 20), buffer.toList)
  }
}
