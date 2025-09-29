package org.scalanative.testsuite.javalib.util

import java.util.{HashMap, Map}
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* The Map.of static methods were introduced in Java 9.
 * The Map.copyOf static method was introduced in Java 10.
 *
 * Strictly these tests should be in require-jdk9 and require-jdk10
 * directories. Scala Native Continuous Integration (CI) currently runs
 * jobs using Java 8, 11, 17, and 21.  These are in 21 to increase the
 * chances that they get run in CI.  Conflicting goals: regular & predictable
 * location or actually being exercised.
 *
 * If these tests are going to be out-of-place, they might as well be
 * located near the envisioned ListDefaultMethodsOnJDK21.scala for
 * default methods which were introduced in Java 21.
 */

/* Some of the strange coding style, especially of specifying type
 * parameters explicitly and not using lambdas is due to the need to
 * support Scala versions from 2.12.19 through 3.N.
 */

class MapStaticMethodsTestOnJDK21 {

  @Test def copyOf_ValidateArgs(): Unit = {

    assertThrows(
      "null argument should throw",
      classOf[NullPointerException],
      ju.Map.copyOf[String, Int](null)
    )

    val expectedSize = 5

    val expected = new HashMap[String, Int](expectedSize)

    for (j <- 0 until expectedSize - 1)
      expected.put(s"copyOf_ValidateArgs_${j}", j)

    expected.put(null, expectedSize) // Pick an arbitary victim

    assertThrows(
      "null source map content should throw",
      classOf[NullPointerException],
      ju.Map.copyOf[String, Int](expected)
    )
  }

  @Test def copyOf(): Unit = {
    val expectedSize = 10
    val prefix = "copyOf_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

    val result = ju.Map.copyOf[String, Int](expected)

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_NoArg(): Unit = {
    val expectedSize = 0

    val result = ju.Map.of()

    assertEquals("map size", expectedSize, result.size())
  }

  @Test def of_TwoArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](null, "value_1")
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object]("key_1", null)
    )
  }

  @Test def of_TwoArgs(): Unit = {
    val expectedSize = 1

    val prefix = "twoArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

    val result = ju.Map.of(s"${prefix}${1}", 1)

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_FourArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](null, "value_1")
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object]("key_1", null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object]("key_1", "value_1", null, "value_2")
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object]("key_1", "value_1", "key_2", null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of[Int, Int](1, 1, 1, 2)
    )
  }

  @Test def of_FourArgs(): Unit = {
    val expectedSize = 2

    val prefix = "fourArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

    val result = ju.Map.of(s"${prefix}${1}", 1, s"${prefix}${2}", 2)

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_SixArgs_ValidateArgs(): Unit = {
// format: off

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        null,    "value_1",
        "key_2", "value_2",
        "key_3", "value_3"
      )
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", null,
        "key_2", "value_2",
        "key_3", "value_3"
      )
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        null,    "value_2",
        "key_3", "value_3"
      )
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", null,
        "key_3", "value_3"
      )
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        null,    "value_3"
      )
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", null
      )
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of(1, 2, 2, 2, 1, 3)
    )

// format: on
  }

  @Test def of_SixArgs(): Unit = {
    val expectedSize = 3

    val prefix = "sixArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

    val result =
      ju.Map.of(s"${prefix}${1}", 1, s"${prefix}${2}", 2, s"${prefix}${3}", 3)

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_EightArgs_ValidateArgs(): Unit = {
// format: off

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        null,    "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4"
      )
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", null,
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4"
      )
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        null,    "value_2",
        "key_3", "value_3",
        "key_4", "value_4"
      )
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", null,
        "key_3", "value_3",
        "key_4", "value_4"
      )
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        null,    "value_3",
        "key_4", "value_4"
      )
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", null,
        "key_4", "value_4"
      )
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        null,    "value_4"
      )
    )

    assertThrows(
      "null eighth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", null
      )
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of(1, 2, 2, 2, 1, 3, 4, 4)
    )

// format: on
  }

  @Test def of_EightArgs(): Unit = {
    val expectedSize = 4

    val prefix = "eightArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

// format: off

    val result =
      ju.Map.of(
        s"${prefix}${1}", 1,
        s"${prefix}${2}", 2,
        s"${prefix}${3}", 3,
        s"${prefix}${4}", 4
      )

// format: on

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_TenArgs_ValidateArgs(): Unit = {
// format: off

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        null,    "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5"
      )
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", null,
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5"
      )
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        null,    "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5"
      )
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", null,
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5"
      )
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        null,    "value_3",
        "key_4", "value_4",
        "key_5", "value_5"
      )
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", null,
        "key_4", "value_4",
        "key_5", "value_5"
      )
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        null,    "value_4",
        "key_5", "value_5"
      )
    )

    assertThrows(
      "null eighth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", null,
        "key_5", "value_5"
      )
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        null,    "value_5"
      )
    )

    assertThrows(
      "null tenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", null
      )
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of(1, 2, 2, 2, 3, 3, 5, 4, 5, 5)
    )

// format: on
  }

  @Test def of_TenArgs(): Unit = {
    val expectedSize = 5

    val prefix = "tenArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

// format: off

    val result =
      ju.Map.of(
        s"${prefix}${1}", 1,
        s"${prefix}${2}", 2,
        s"${prefix}${3}", 3,
        s"${prefix}${4}", 4,
        s"${prefix}${5}", 5
      )

// format: on

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_TwelveArgs_ValidateArgs(): Unit = {
// format: off

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        null,    "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", null,
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        null,    "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", null,
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        null,    "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", null,
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        null,    "value_4",
        "key_5", "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null eighth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", null,
        "key_5", "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        null,    "value_5",
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null tenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", null,
        "key_6", "value_6"
      )
    )

    assertThrows(
      "null eleventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        null,    "value_6"
      )
    )

    assertThrows(
      "null twelfth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", null
      )
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of(1, 2, 2, 2, 3, 3, 5, 4, 5, 5, 6, 6)
    )

// format: on
  }

  @Test def of_TwelveArgs(): Unit = {
    val expectedSize = 6

    val prefix = "twelveArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

// format: off

    val result =
      ju.Map.of(
        s"${prefix}${1}", 1,
        s"${prefix}${2}", 2,
        s"${prefix}${3}", 3,
        s"${prefix}${4}", 4,
        s"${prefix}${5}", 5,
        s"${prefix}${6}", 6
      )

// format: on

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_FourteenArgs_ValidateArgs(): Unit = {
// format: off

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        null,    "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", null,
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        null,    "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", null,
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        null,    "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", null,
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        null,    "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null eighth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", null,
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        null,    "value_5",
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null tenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", null,
        "key_6", "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null eleventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        null,    "value_6",
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null twelfth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", null,
        "key_7", "value_7"
      )
    )

    assertThrows(
      "null thirteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        null,    "value_7"
      )
    )

    assertThrows(
      "null fourteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", null
      )
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of(1, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 6, 7)
    )

// format: on
  }

  @Test def of_FourteenArgs(): Unit = {
    val expectedSize = 7

    val prefix = "fourteenArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

// format: off

    val result =
      ju.Map.of(
        s"${prefix}${1}", 1,
        s"${prefix}${2}", 2,
        s"${prefix}${3}", 3,
        s"${prefix}${4}", 4,
        s"${prefix}${5}", 5,
        s"${prefix}${6}", 6,
        s"${prefix}${7}", 7
      )

// format: on

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_SixteenArgs_ValidateArgs(): Unit = {
// format: off

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        null,    "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", null,
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        null,    "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", null,
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        null,    "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", null,
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        null,    "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null eighth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", null,
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        null,    "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null tenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", null,
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null eleventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        null,    "value_6",
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null twelfth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", null,
        "key_7", "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null thirteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        null,    "value_7",
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null fourteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", null,
        "key_8", "value_8"
      )
    )

    assertThrows(
      "null fifteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        null,    "value_8"
      )
    )

    assertThrows(
      "null fifteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", null
      )
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of(1, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 7, 8)
    )

// format: on
  }

  @Test def of_SixteenArgs(): Unit = {
    val expectedSize = 8

    val prefix = "sixteenArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

// format: off

    val result =
      ju.Map.of(
        s"${prefix}${1}", 1,
        s"${prefix}${2}", 2,
        s"${prefix}${3}", 3,
        s"${prefix}${4}", 4,
        s"${prefix}${5}", 5,
        s"${prefix}${6}", 6,
        s"${prefix}${7}", 7,
        s"${prefix}${8}", 8
      )

// format: on

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_EighteenArgs_ValidateArgs(): Unit = {
// format: off

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        null,    "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", null,
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        null,    "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", null,
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        null,    "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", null,
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        null,    "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null eighth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", null,
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        null,    "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null tenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", null,
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null eleventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        null,    "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null twelfth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", null,
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null thirteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        null,    "value_7",
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null fourteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", null,
        "key_8", "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null fifteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        null,    "value_8",
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null sixteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", null,
        "key_9", "value_9"
      )
    )

    assertThrows(
      "null seventeenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        null,    "value_9"
      )
    )

    assertThrows(
      "null eighteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", null
      )
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of(1, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 8, 9)
    )

// format: on
  }

  @Test def of_EighteenArgs(): Unit = {
    val expectedSize = 9

    val prefix = "eighteenArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

// format: off

    val result =
      ju.Map.of(
        s"${prefix}${1}", 1,
        s"${prefix}${2}", 2,
        s"${prefix}${3}", 3,
        s"${prefix}${4}", 4,
        s"${prefix}${5}", 5,
        s"${prefix}${6}", 6,
        s"${prefix}${7}", 7,
        s"${prefix}${8}", 8,
        s"${prefix}${9}", 9
      )

// format: on

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_TwentyArgs_ValidateArgs(): Unit = {
// format: off

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        null,     "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  null,
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        null,     "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null fourth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2",  null,
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        null,     "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  null,
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        null,     "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null eighth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  null,
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        null,     "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null tenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  null,
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null eleventh argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        null,     "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null twelfth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  null,
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null thirteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        null,     "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null fourteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  null,
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null fifteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        null,     "value_8",
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null sixteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  null,
        "key_9",  "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null seventeenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        null,     "value_9",
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null eighteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  null,
        "key_10", "value_10"
      )
    )

    assertThrows(
      "null nineteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1", "value_1",
        "key_2", "value_2",
        "key_3", "value_3",
        "key_4", "value_4",
        "key_5", "value_5",
        "key_6", "value_6",
        "key_7", "value_7",
        "key_8", "value_8",
        "key_9", "value_9",
        null,    "value_10"
      )
    )

    assertThrows(
      "null nineteenth argument should throw",
      classOf[NullPointerException],
      ju.Map.of[Object, Object](
        "key_1",  "value_1",
        "key_2",  "value_2",
        "key_3",  "value_3",
        "key_4",  "value_4",
        "key_5",  "value_5",
        "key_6",  "value_6",
        "key_7",  "value_7",
        "key_8",  "value_8",
        "key_9",  "value_9",
        "key_10", null
      )
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Map.of(1, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 9, 10)
    )

// format: on
  }

  @Test def of_TwentyArgs(): Unit = {
    val expectedSize = 10

    val prefix = "twentyArgs_"

    val expected = new HashMap[String, Int](expectedSize)
    for (j <- 1 to expectedSize)
      expected.put(s"${prefix}${j}", j)

// format: off

    val result =
      ju.Map.of(
        s"${prefix}${1}", 1,
        s"${prefix}${2}", 2,
        s"${prefix}${3}", 3,
        s"${prefix}${4}", 4,
        s"${prefix}${5}", 5,
        s"${prefix}${6}", 6,
        s"${prefix}${7}", 7,
        s"${prefix}${8}", 8,
        s"${prefix}${9}", 9,
        s"${prefix}${10}", 10
      )

// format: on

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )
    }

    val removeKey = s"${prefix}3" // an arbitrary element
    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(removeKey)
    )
  }

  @Test def of_Entries_ValidateArgs(): Unit = {
    assertThrows(
      "null argument should throw",
      classOf[NullPointerException],
      ju.Map.ofEntries[Int, Int](null)
    )

    assertThrows(
      "Map.entry null key should throw",
      classOf[NullPointerException],
      Map.entry(null, "value_1")
    )

    assertThrows(
      "Map.entry null value should throw",
      classOf[NullPointerException],
      Map.entry("key_1", null)
    )
  }

  @Test def of_Entries(): Unit = {
    val prefix = "key_"

    val expectedSize = 5
    val entry_1 = Map.entry[String, Int](s"${prefix}1", 1)
    val entry_2 = Map.entry[String, Int](s"${prefix}2", 2)
    val entry_3 = Map.entry[String, Int](s"${prefix}3", 3)
    val entry_4 = Map.entry[String, Int](s"${prefix}4", 4)
    val entry_5 = Map.entry[String, Int](s"${prefix}5", 5)

    val result = Map.ofEntries[String, Int](
      entry_1,
      entry_2,
      entry_3,
      entry_4,
      entry_5
    )

    assertEquals("map size", expectedSize, result.size())

    for (j <- 1 to expectedSize) {
      val key = s"${prefix}${j}"

      assertEquals(
        s"key-value pair not found in result map: <${key}, ${j}>",
        j,
        result.getOrDefault(key, -1)
      )

      assertThrows(
        "result.clear() should throw",
        classOf[UnsupportedOperationException],
        result.clear()
      )
    }
  }
}
