package org.scalanative.testsuite.javalib.util

import java.util.{ArrayList, HashSet}
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* The Set.of static methods were introduced in Java 9.
 * The Set.copyOf static method was introduced in Java 10.
 * HashSet.newHashSet() was introduced in Java 19.
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

class SetStaticMethodsTestOnJDK21 {

  @Test def copyOf_ValidateArgs(): Unit = {

    assertThrows(
      "null argument should throw",
      classOf[NullPointerException],
      ju.Set.copyOf(null)
    )

    val expectedSize = 4

    val expected = new ArrayList[String](expectedSize)

    for (j <- 0 until expectedSize)
      expected.add(s"copyOf_ValidateArgs_${j}")

    expected.set(1, null) // Pick an arbitary victim

    assertThrows(
      "null collection content should throw",
      classOf[NullPointerException],
      ju.Set.copyOf(expected)
    )
  }

  @Test def copyOf(): Unit = {
    val expectedSize = 10

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"copyOf_${j}")

    val result = ju.Set.copyOf(expected)

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def copyOf_DuplicatesAreSquashed(): Unit = {
    val expectedSize = 10

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"copyOf_${j}")

    expected.set(6, "copyOf_4") // pick a victim & create duplicate
    expected.set(2, "copyOf_3") // pick another victim & create duplicate

    // duplicates are _silently_ not added to result set.
    val result = ju.Set.copyOf(expected)

    // expectedSize minus one because 2 duplicates should not have been added.
    assertEquals("set size", expectedSize - 2, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }
  }

  @Test def of_NoArg(): Unit = {
    val expectedSize = 0

    val result = ju.Set.of()

    assertEquals("set size", expectedSize, result.size())
  }

  @Test def of_OneArg_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of[String](null)
    )
  }

  @Test def of_OneArg(): Unit = {
    val expectedSize = 1

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"OneArg_${j}")

    val result = ju.Set.of[String](
      expected.get(0)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_VarArgs_ValidateArgs(): Unit = {

    val varArgs = new Array[String](4)
    varArgs(0) = "va_1"
    varArgs(1) = "va_2"
    varArgs(2) = null
    varArgs(3) = "va_4"

    assertThrows(
      "null variable argument should throw",
      classOf[NullPointerException],
      ju.Set.of(varArgs: _*)
    )

    varArgs(2) = "va_4"
    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(varArgs: _*)
    )
  }

  @Test def of_VarArgs_SmallN(): Unit = {
    /* Does a varargs with less than 10 elements conflict with the overloads
     * which specify one through 10 args explicitly?  Can I break things before
     * users do?
     */

    val expectedSize = 5
    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"VarArgs_SmallN_${j}"

    // 'Normal' varargs usage
    val result = ju.Set.of(expected: _*)

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }
  }

  @Test def of_VarArgs_LargeN(): Unit = {

    /* Does a varargs with more than 10 elements conflict with the overloads
     * which specify one through 10 args explicitly?  Can I break things before
     * users do?
     */

    val expectedSize = 20
    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"VarArgs_LargeN_${j}"

    val result = ju.Set.of(expected: _*)

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }
  }

  @Test def of_TwoArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 1)
    )
  }

  @Test def of_TwoArgs(): Unit = {
    val expectedSize = 2

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"TwoArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_ThreeArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2, 3)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null, 3)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 2, 2)
    )
  }

  @Test def of_ThreeArgs(): Unit = {
    val expectedSize = 3

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"ThreeArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_FourArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2, 3, 4)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null, 3, 4)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, null, 4)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 2, 3, 2)
    )
  }

  @Test def of_FourArgs(): Unit = {
    val expectedSize = 4

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"FourArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2),
      expected.get(3)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_FiveArgs_ValidateArgs(): Unit = {

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2, 3, 4, 5)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null, 3, 4, 5)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, null, 4, 5)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, null, 5)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 2, 3, 3, 4)
    )
  }

  @Test def of_FiveArgs(): Unit = {
    val expectedSize = 5

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"FiveArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2),
      expected.get(3),
      expected.get(4)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_SixArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2, 3, 4, 5, 6)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null, 3, 4, 5, 6)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, null, 4, 5, 6)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, null, 5, 6)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, null, 6)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 2, 3, 5, 4, 5)
    )
  }

  @Test def of_SixArgs(): Unit = {
    val expectedSize = 6

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"SixArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2),
      expected.get(3),
      expected.get(4),
      expected.get(5)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_SevenArgs_ValidateArgs(): Unit = {

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2, 3, 4, 5, 6, 7)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null, 3, 4, 5, 6, 7)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, null, 4, 5, 6, 7)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, null, 5, 6, 7)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, null, 6, 7)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, null, 7)
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 2, 3, 4, 5, 6, 6)
    )
  }

  @Test def of_SevenArgs(): Unit = {
    val expectedSize = 7

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"SevenArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2),
      expected.get(3),
      expected.get(4),
      expected.get(5),
      expected.get(6)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_EightArgs_ValidateArgs(): Unit = {

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2, 3, 4, 5, 6, 7, 8)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null, 3, 4, 5, 6, 7, 8)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, null, 4, 5, 6, 7, 8)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, null, 5, 6, 7, 8)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, null, 6, 7, 8)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, null, 7, 8)
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, null, 8)
    )

    assertThrows(
      "null eigth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, 7, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 2, 3, 4, 5, 6, 7, 7)
    )
  }

  @Test def of_EightArgs(): Unit = {
    val expectedSize = 8

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"EightArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2),
      expected.get(3),
      expected.get(4),
      expected.get(5),
      expected.get(6),
      expected.get(7)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_NineArgs_ValidateArgs(): Unit = {

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2, 3, 4, 5, 6, 7, 8, 9)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null, 3, 4, 5, 6, 7, 8, 9)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, null, 4, 5, 6, 7, 8, 9)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, null, 5, 6, 7, 8, 9)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, null, 6, 7, 8, 9)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, null, 7, 8, 9)
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, null, 8, 9)
    )

    assertThrows(
      "null eigth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, 7, null, 9)
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, 7, 8, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 2, 2, 3, 4, 5, 6, 7, 8)
    )
  }

  @Test def of_NineArgs(): Unit = {
    val expectedSize = 9

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"NineArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2),
      expected.get(3),
      expected.get(4),
      expected.get(5),
      expected.get(6),
      expected.get(7),
      expected.get(8)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_TenArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.Set.of(null, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, null, 3, 4, 5, 6, 7, 8, 9, 10)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, null, 4, 5, 6, 7, 8, 9, 10)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, null, 5, 6, 7, 8, 9, 10)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, null, 6, 7, 8, 9, 10)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, null, 7, 8, 9, 10)
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, null, 8, 9, 10)
    )

    assertThrows(
      "null eigth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, 7, null, 9, 10)
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, 7, 8, null, 10)
    )

    assertThrows(
      "null tenth argument should throw",
      classOf[NullPointerException],
      ju.Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, null)
    )

    assertThrows(
      "duplicate elements in constructor should throw",
      classOf[IllegalArgumentException],
      ju.Set.of(1, 2, 3, 4, 4, 5, 6, 7, 9)
    )
  }

  @Test def of_TenArgs(): Unit = {
    val expectedSize = 10

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"TenArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2),
      expected.get(3),
      expected.get(4),
      expected.get(5),
      expected.get(6),
      expected.get(7),
      expected.get(8),
      expected.get(9)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_ElevenArgs(): Unit = {
    /* Boundary case.
     *  Does this fall over to using varargs on JVM? On SN?
     *  It exceeds the number of explicitly defined .of() methods.
     *  Answer: Appears to work fine on both JDK & Scala Native.
     */

    val expectedSize = 11

    val expected = new ArrayList[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected.add(s"ElevenArgs_${j}")

    val result = ju.Set.of(
      expected.get(0),
      expected.get(1),
      expected.get(2),
      expected.get(3),
      expected.get(4),
      expected.get(5),
      expected.get(6),
      expected.get(7),
      expected.get(8),
      expected.get(9),
      expected.get(10)
    )

    assertEquals("set size", expectedSize, result.size())

    for (j <- 0 until expectedSize) {
      val element = expected.get(j)
      assertTrue(
        s"element not found in result set: ${element}",
        result.contains(element)
      )
    }

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }
}
