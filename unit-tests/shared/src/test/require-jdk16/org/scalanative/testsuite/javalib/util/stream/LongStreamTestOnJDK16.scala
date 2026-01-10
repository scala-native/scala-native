package org.scalanative.testsuite.javalib.util.stream

import java.util.Arrays
import java.util.stream._
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class LongStreamTestOnJDK16 {

  // Since: Java 16
  @Test def longStreamMapMulti_Eliding(): Unit = {
    val initialCount = 6
    val expectedCount = 4

    val data = new Array[Long](initialCount)
    data(0) = 55
    data(1) = 44
    data(2) = -11
    data(3) = 0
    data(4) = -22
    data(5) = 33L

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
  @Test def longStreamMapMulti_Expanding(): Unit = {

    val initialCount = 6
    val expectedCount = 7

    val data = new Array[Long](initialCount)
    data(0) = 55
    data(1) = 44
    data(2) = -11
    data(3) = 0
    data(4) = -22
    data(5) = 33L

    val s = Arrays.stream(data)

    // Expand one item with multiple replacements. Otherwise 1 to 1.
    val mappedMulti = s.mapMulti((element, consumer) =>
      if (element != 0) {
        consumer.accept(element)
      } else {
        consumer.accept(jl.Long.MIN_VALUE)
        consumer.accept(jl.Long.MIN_VALUE)
      }
    )

    var count = mappedMulti.count()

    assertTrue("unexpected empty stream", count > 0)
    assertEquals("unexpected number of elements", expectedCount, count)
  }

  // SN Issue 4742
  @Test def streamFilter_ShrinkingDownstream(): Unit = {

    val ds = LongStream.of(
      55L, 44L, -11L, 0L, -22L, 33L
    )

    val expectedCount = 2

    val expectedData = new Array[scala.Long](expectedCount)
    expectedData(0) = -11
    expectedData(1) = -22

    val filtered: Array[scala.Long] = ds.filter(i => i < 0L).toArray()

    assertEquals("filtered size", expectedCount, filtered.size)
    for (j <- 0 until expectedCount)
      assertEquals("contents j: $j", expectedData(j), filtered(j))
  }
}
