package org.scalanative.testsuite.javalib.util.stream

import java.util.Arrays
import java.util.stream._
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class DoubleStreamTestOnJDK16 {

  // Since: Java 16
  @Test def doubleStreamMapMulti_Eliding(): Unit = {
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
  @Test def doubleStreamMapMulti_Expanding(): Unit = {

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

  // SN Issue 4742
  @Test def streamFilter_ShrinkingDownstream(): Unit = {

    val ds = DoubleStream.of(
      5.5, 4.4, -1.1, 0.0, -2.2, 3.3
    )

    val expectedCount = 2

    val expectedData = new Array[scala.Double](expectedCount)
    expectedData(0) = -1.1
    expectedData(1) = -2.2

    val filtered: Array[scala.Double] = ds.filter(i => i < 0.0).toArray()

    assertEquals("filtered size", expectedCount, filtered.size)
    for (j <- 0 until expectedCount)
      assertEquals("contents j: $j", expectedData(j), filtered(j), 0.0)
  }

  // SN Issue 4743
  @Test def streamDistinct_ShrinkingDownstream(): Unit = {

    val ds = DoubleStream.of(
      5.5, 0.0, 4.4, -1.1, -1.1, 4.4, -2.2, -2.2, 3.3, 4.4
    )

    val expectedCount = 6

    val expectedData = new Array[scala.Double](expectedCount)
    expectedData(0) = 5.5
    expectedData(1) = 0.0
    expectedData(2) = 4.4
    expectedData(3) = -1.1
    expectedData(4) = -2.2
    expectedData(5) = 3.3

    val distinctElements: Array[scala.Double] = ds.distinct().toArray()

    assertEquals("distinct size", expectedCount, distinctElements.size)
    for (j <- 0 until expectedCount)
      assertEquals("contents j: $j", expectedData(j), distinctElements(j), 0.0)
  }
}
