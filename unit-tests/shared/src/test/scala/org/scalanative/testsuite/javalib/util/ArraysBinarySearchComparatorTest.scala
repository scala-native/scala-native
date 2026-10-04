package org.scalanative.testsuite.javalib.util

import java.util.{Arrays, Comparator}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ArraysBinarySearchComparatorTest {
  private val comparator = String.CASE_INSENSITIVE_ORDER

  @Test def fullArrayUsesComparatorEquality(): Unit = {
    val values = Array("alpha", "bravo", "charlie")
    assertEquals(1, Arrays.binarySearch(values, "BRAVO", comparator))
    assertEquals(-1, Arrays.binarySearch(values, "aardvark", comparator))
    assertEquals(-2, Arrays.binarySearch(values, "beta", comparator))
    assertEquals(-4, Arrays.binarySearch(values, "delta", comparator))
  }

  @Test def rangeUsesAbsoluteIndices(): Unit = {
    val values = Array("outside", "alpha", "bravo", "charlie", "outside")
    assertEquals(2, Arrays.binarySearch(values, 1, 4, "BRAVO", comparator))
    assertEquals(-2, Arrays.binarySearch(values, 1, 4, "aardvark", comparator))
    assertEquals(-5, Arrays.binarySearch(values, 1, 4, "delta", comparator))
    assertEquals(-3, Arrays.binarySearch(values, 2, 2, "bravo", comparator))
  }

  @Test def supertypeAndReverseComparators(): Unit = {
    val supertypeComparator: Comparator[AnyRef] = new Comparator[AnyRef] {
      def compare(a: AnyRef, b: AnyRef): Int =
        comparator.compare(a.toString(), b.toString())
    }
    val values = Array("alpha", "bravo", "charlie")
    assertEquals(
      1,
      Arrays.binarySearch[String](values, "BRAVO", supertypeComparator)
    )
    assertEquals(
      1,
      Arrays.binarySearch[String](values, 0, 3, "BRAVO", supertypeComparator)
    )

    val reverse = comparator.reversed()
    val descending = Array("charlie", "bravo", "alpha")
    assertEquals(1, Arrays.binarySearch(descending, "BRAVO", reverse))
    assertEquals(-1, Arrays.binarySearch(descending, "delta", reverse))
  }

  @Test def nullComparatorUsesNaturalOrder(): Unit = {
    val values = Array("alpha", "bravo", "charlie")
    assertEquals(1, Arrays.binarySearch(values, "bravo", null))
    assertEquals(-2, Arrays.binarySearch(values, "beta", null))
    assertEquals(1, Arrays.binarySearch(values, 1, 3, "bravo", null))
    assertEquals(-4, Arrays.binarySearch(values, 1, 3, "delta", null))
  }

  @Test def nullAwareComparator(): Unit = {
    val nullable = Comparator.nullsFirst[String](comparator)
    val values = Array[String](null, "alpha", "bravo")
    assertEquals(0, Arrays.binarySearch(values, null, nullable))
    assertEquals(2, Arrays.binarySearch(values, "BRAVO", nullable))
    assertEquals(-2, Arrays.binarySearch(values, 1, 3, null, nullable))
  }

  @Test def rangeValidation(): Unit = {
    val values = Array("alpha", "bravo")
    assertThrows(
      classOf[IllegalArgumentException],
      Arrays.binarySearch(values, 2, 1, "alpha", comparator)
    )
    assertThrows(
      classOf[IndexOutOfBoundsException],
      Arrays.binarySearch(values, -1, 2, "alpha", comparator)
    )
    assertThrows(
      classOf[IndexOutOfBoundsException],
      Arrays.binarySearch(values, 0, 3, "alpha", comparator)
    )
  }

  @Test def nullArrayRejected(): Unit = {
    val values: Array[String] = null
    assertThrows(
      classOf[NullPointerException],
      Arrays.binarySearch(values, "alpha", comparator)
    )
    assertThrows(
      classOf[NullPointerException],
      Arrays.binarySearch(values, 0, 1, "alpha", comparator)
    )
  }
}
