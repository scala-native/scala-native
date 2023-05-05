package org.scalanative.testsuite.javalib.util.stream

import java.{lang => jl}
import java.util.Arrays
import java.util.stream._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class DoubleStreamTestOnJDK16 {

  // Since: Java 16
  @Test def streamMapMulti_Eliding(): Unit = {
    val initialCount = 6
    val expectedCount = 4

    val data = new Array[Double](initialCount)
    data(0) = 5.5
    data(1) = 4.4
    data(2) = -1.1
    data(3) = 0.0
    data(4) = -2.2
    data(5) = 3.3

    val s = Arrays.stream(data)

    // By design, the mapper will return empty results for two items.
    val mappedMulti = s.mapMulti((element, consumer) =>
      if ((element != 0.0) && (element != 4.4)) {
        consumer.accept(element)
      }
    )

    var count = mappedMulti.count()

    assertTrue("unexpected empty stream", count > 0)
    assertEquals("unexpected number of elements", expectedCount, count)
  }

  // Since: Java 16
  @Test def streamMapMulti_Expanding(): Unit = {

    case class Item(name: String, info: Double)

    val initialCount = 6
    val expectedCount = 7

    val data = new Array[Double](initialCount)
    data(0) = 5.5
    data(1) = 4.4
    data(2) = -1.1
    data(3) = 0.0
    data(4) = -2.2
    data(5) = 3.3

    val s = Arrays.stream(data)

    // Expand one item with multiple replacements. Otherwise 1 to 1.
    val mappedMulti = s.mapMulti((element, consumer) =>
      if (element != 0.0) {
        consumer.accept(element)
      } else {
        consumer.accept(jl.Double.NEGATIVE_INFINITY)
        consumer.accept(jl.Double.POSITIVE_INFINITY)
      }
    )

    var count = mappedMulti.count()

    assertTrue("unexpected empty stream", count > 0)
    assertEquals("unexpected number of elements", expectedCount, count)
  }

}
