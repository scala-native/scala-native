// Ported from Scala.js, commit SHA: cfb4888a6 dated: 2021-01-07
package org.scalanative.testsuite.javalib.util.function

import org.junit.Assert.*
import org.junit.Test

import java.util.function.*

class DoubleConsumerTest {
  @Test def accept(): Unit = {
    // Side-effects
    var current: Double = 0

    val add = new DoubleConsumer {
      override def accept(value: Double): Unit = current += value
    }

    add.accept(5)
    assertEquals(5, current, 0)
    add.accept(15)
    assertEquals(20, current, 0)
  }

  @Test def andThen(): Unit = {
    // Side-effects
    var buffer = scala.collection.mutable.ListBuffer.empty[Double]

    val add = new DoubleConsumer {
      override def accept(value: Double): Unit = buffer += value
    }
    val add2x = new DoubleConsumer {
      override def accept(value: Double): Unit = buffer += value * 2
    }
    val merged: DoubleConsumer = add.andThen(add2x)

    merged.accept(1d)
    assertEquals(List(1d, 2d), buffer.toList)
    merged.accept(4d)
    assertEquals(List(1d, 2d, 4d, 8d), buffer.toList)
  }
}
