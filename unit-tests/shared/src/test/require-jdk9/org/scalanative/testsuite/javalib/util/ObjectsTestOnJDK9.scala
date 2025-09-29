package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ObjectsTestOnJDK9 {

  @Test def testCheckFromIndexSizeInt(): Unit = {
    assertThrows(
      "fromIndex < 0",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromIndexSize(-1, 2, 3)
    )

    assertThrows(
      "size < 0",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromIndexSize(1, -1, 4)
    )

    assertThrows(
      "fromIndex + size > length",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromIndexSize(1, 5, 5)
    )

    val expected = 1
    assertEquals(
      "invalid return value",
      expected,
      ju.Objects.checkFromIndexSize(expected, 5, 6)
    )
  }

  @Test def testCheckFromToIndexInt(): Unit = {

    assertThrows(
      "fromIndex < 0",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromToIndex(-1, 2, 3)
    )

    assertThrows(
      "fromIndex > toIndex",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromToIndex(2, 1, 3)
    )

    assertThrows(
      "toIndex > length",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkFromToIndex(2, 4, 3)
    )

    val expected = 2
    assertEquals(
      "invalid return value",
      expected,
      ju.Objects.checkFromToIndex(expected, 4, 5)
    )
  }

  @Test def testCheckIndexInt(): Unit = {

    assertThrows(
      "index < 0",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkIndex(-1, 2)
    )

    assertThrows(
      "index >= length",
      classOf[IndexOutOfBoundsException],
      ju.Objects.checkIndex(2, 2)
    )

    val expected = 4
    assertEquals(
      "invalid return value",
      expected,
      ju.Objects.checkIndex(expected, 5)
    )
  }

  @Test def testRequireNonNullElse(): Unit = {
    val obj = "obj"
    val defaultObj = "defaultObj"

    assertThrows(
      "both args null",
      classOf[NullPointerException],
      ju.Objects.requireNonNullElse(null, null)
    )

    assertEquals(
      "expect obj",
      obj,
      ju.Objects.requireNonNullElse(obj, null)
    )

    assertEquals(
      "expect defaultObj",
      defaultObj,
      ju.Objects.requireNonNullElse(null, defaultObj)
    )
  }

  @Test def testRequireNonNullElseGet(): Unit = {

    val message = "This too shall pass"

    val supplierOfMessage = new ju.function.Supplier[String] {
      def get(): String = message
    }

    val supplierOfNulls = new ju.function.Supplier[String] {
      def get(): String = null
    }

    assertThrows(
      "both args null",
      classOf[NullPointerException],
      ju.Objects.requireNonNullElseGet(null, null)
    )

    assertThrows(
      "supplierOfNulls",
      classOf[NullPointerException],
      ju.Objects.requireNonNullElseGet(null, supplierOfNulls)
    )

    val expected = "Amenirdis"
    assertEquals(
      "expect obj",
      expected,
      ju.Objects.requireNonNullElseGet(expected, null)
    )

    assertEquals(
      "test supplierOfMessage",
      message,
      ju.Objects.requireNonNullElseGet(null, supplierOfMessage)
    )
  }
}
