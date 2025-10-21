package org.scalanative.testsuite.javalib.util

import java.{lang => jl, util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ObjectsTestOnJDK16 {

  @Test def testCheckFromIndexSizeLong(): Unit = {

    assertThrows(
      "fromIndex < 0",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromIndexSize(-1L, 2L, 3L)
    )

    assertThrows(
      "size < 0",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromIndexSize(1L, -1L, jl.Long.MAX_VALUE - 1)
    )

    assertThrows(
      "fromIndex + size > length",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromIndexSize(
        jl.Integer.MAX_VALUE + 2L,
        5L,
        jl.Integer.MAX_VALUE + 1L
      )
    )

    val expected = jl.Integer.MAX_VALUE + 1L
    assertEquals(
      "invalid return value",
      expected,
      ju.Objects.checkFromIndexSize(expected, 5L, expected + 6L)
    )
  }

  @Test def testCheckFromToIndexLong(): Unit = {
    assertThrows(
      "fromIndex < 0",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromToIndex(-1L, 2L, 3L)
    )

    assertThrows(
      "fromIndex > toIndex",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromToIndex(
        jl.Integer.MAX_VALUE + 5L,
        jl.Integer.MAX_VALUE + 1L,
        3L
      )
    )

    assertThrows(
      "toIndex > length",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromToIndex(2, 4, 3)
    )

    assertThrows(
      "toIndex > length",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromToIndex(
        jl.Integer.MAX_VALUE + 2L,
        jl.Integer.MAX_VALUE + 4L,
        3L
      )
    )

    val expected = jl.Integer.MAX_VALUE + 2L
    assertEquals(
      "invalid return value",
      expected,
      ju.Objects.checkFromToIndex(expected, expected + 4L, expected + 6L)
    )
  }

  @Test def testCheckIndexLong(): Unit = {
    assertThrows(
      "index < 0",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkIndex(-1L, 2L)
    )

    assertThrows(
      "index >= length",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkIndex(jl.Integer.MAX_VALUE + 2, jl.Integer.MAX_VALUE + 2)
    )

    val expected = jl.Integer.MAX_VALUE + 4L
    assertEquals(
      "invalid return value",
      expected,
      ju.Objects.checkIndex(expected, expected + 1L)
    )
  }
}
