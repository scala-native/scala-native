package org.scalanative.testsuite.javalib.util.stream

import java.util.Arrays
import java.util.stream._
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class IntStreamTestOnJDK16 {

  // Since: Java 16
  @Test def intStreamMapMulti_Eliding(): Unit = {
    val initialCount = 6
    val expectedCount = 4

    val data = new Array[Int](initialCount)
    data(0) = 55
    data(1) = 44
    data(2) = -11
    data(3) = 0
    data(4) = -22
    data(5) = 33

    val s = Arrays.stream(data)

    // By design, the mapper will return empty results for two items.
    val mappedMulti = s.mapMulti((element, consumer) =>
      if ((element != 0) && (element != 44)) {
        consumer.accept(element)
      }
    )

    var count = mappedMulti.count()

    assertTrue("unexpected empty stream", count > 0)
    assertEquals("unexpected number of elements", expectedCount, count)
  }

  // Since: Java 16
  @Test def intStreamMapMulti_Expanding(): Unit = {

    val initialCount = 6
    val expectedCount = 7

    val data = new Array[Int](initialCount)
    data(0) = 55
    data(1) = 44
    data(2) = -11
    data(3) = 0
    data(4) = -22
    data(5) = 33

    val s = Arrays.stream(data)

    // Expand one item with multiple replacements. Otherwise 1 to 1.
    val mappedMulti = s.mapMulti((element, consumer) =>
      if (element != 0) {
        consumer.accept(element)
      } else {
        consumer.accept(jl.Integer.MIN_VALUE)
        consumer.accept(jl.Integer.MIN_VALUE)
      }
    )

    var count = mappedMulti.count()

    assertTrue("unexpected empty stream", count > 0)
    assertEquals("unexpected number of elements", expectedCount, count)
  }

  // SN Issue 4742
  @Test def streamFilter_ShrinkingDownstream(): Unit = {

    val is = IntStream.of(
      55, 44, -11, 0, -22, 33
    )

    val expectedCount = 2

    val expectedData = new Array[scala.Int](expectedCount)
    expectedData(0) = -11
    expectedData(1) = -22

    val filtered: Array[scala.Int] = is.filter(i => i < 0).toArray()

    assertEquals("filtered size", expectedCount, filtered.size)
    for (j <- 0 until expectedCount)
      assertEquals("contents j: $j", expectedData(j), filtered(j))
  }

  // SN Issue 4743
  @Test def streamDistinct_ShrinkingDownstream(): Unit = {

    val is = IntStream.of(
      55, 0, 44, -11, -11, 44, -22, -22, 33, 44
    )

    val expectedCount = 6

    val expectedData = new Array[scala.Int](expectedCount)
    expectedData(0) = 55
    expectedData(1) = 0
    expectedData(2) = 44
    expectedData(3) = -11
    expectedData(4) = -22
    expectedData(5) = 33

    val distinctElements: Array[scala.Int] = is.distinct().toArray()

    assertEquals("distinct size", expectedCount, distinctElements.size)
    for (j <- 0 until expectedCount)
      assertEquals("contents j: $j", expectedData(j), distinctElements(j))
  }
}
