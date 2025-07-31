package org.scalanative.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.{util => ju}

/* The List.of static methods were introduced in Java 9.
 * The List.copyOf static method was introduced in Java 10.
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

class ListStaticMethodsTestOnJDK21 {

  @Test def copyOf_ValidateArgs(): Unit = {

    assertThrows(
      "null argument should throw",
      classOf[NullPointerException],
      ju.List.copyOf(null)
    )

    val expectedSize = 4

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"copyOf_ValidateArgs_${j}"

    expected(2) = null // Pick an arbitary victim

    val hs = new ju.HashSet[String]()
    for (j <- 0 until expectedSize)
      hs.add(expected(j))

    assertThrows(
      "null collection content should throw",
      classOf[NullPointerException],
      ju.List.copyOf(hs)
    )
  }

  @Test def copyOf(): Unit = {
    val expectedSize = 10

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"copyOf_${j}"

    val ts = new ju.TreeSet[String]()
    for (j <- (expectedSize - 1) to 0 by -1)
      ts.add(expected(j))

    val result = ju.List.copyOf(ts)

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))

    assertTrue(
      "result is not instanceOf[RandomAccess]",
      result.isInstanceOf[ju.RandomAccess]
    )

    assertThrows(
      "result.remove() should throw",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )
  }

  @Test def of_NoArg(): Unit = {
    val expectedSize = 0

    val result = ju.List.of()

    assertEquals("list size", expectedSize, result.size())
  }

  @Test def of_OneArg_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )
  }

  @Test def of_OneArg(): Unit = {
    val expectedSize = 1

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"OneArg_${j}"

    val result = ju.List.of[String](
      expected(0)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))

    assertTrue(
      "result is not instanceOf[RandomAccess]",
      result.isInstanceOf[ju.RandomAccess]
    )

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
      ju.List.of(varArgs: _*)
    )
  }

  @Test def of_CheckUnmodifiableRobustness(): Unit = {
    val expectedSize = 10

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"Unmodifiable_${j}"

    val result = ju.List.of(expected: _*)

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))

    assertTrue(
      "result is not instanceOf[RandomAccess]",
      result.isInstanceOf[ju.RandomAccess]
    )

    assertThrows(
      "List.of() result itself should be unmodifiable",
      classOf[UnsupportedOperationException],
      result.remove(expectedSize - 1)
    )

    // subList
    val fromIndex = 2
    val toIndex = expectedSize - 1
    val sublist = result.subList(fromIndex, toIndex)

    assertThrows(
      "subLists of List.of() should be unmodifiable - remove",
      classOf[UnsupportedOperationException],
      sublist.remove(4) // an arbitrary index in center of new sublist
    )

    assertThrows(
      "subLists of List.of() should be unmodifiable - add",
      classOf[UnsupportedOperationException],
      sublist.add("GrochoMarx")
    )

    // ListIterator

    val li = result.listIterator(3)

    val unusedRemove = li.next()

    assertThrows(
      "ListIterator#remove of List.of() should be unmodifiable - remove",
      classOf[UnsupportedOperationException],
      li.remove()
    )

    val unusedSet1 = li.next() // skip to a position not touched above.
    val unusedSet2 = li.next()

    assertThrows(
      "ListIterator#add of List.of() should be unmodifiable - set",
      classOf[UnsupportedOperationException],
      li.set("SetShouldThrow")
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
    val result = ju.List.of(expected: _*)

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
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

    val result = ju.List.of(expected: _*)

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_TwoArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )
  }

  @Test def of_TwoArgs(): Unit = {
    val expectedSize = 2

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"TwoArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_ThreeArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, null)
    )
  }

  @Test def of_ThreeArgs(): Unit = {
    val expectedSize = 3

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"ThreeArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_FourArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, null)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, null)
    )
  }

  @Test def of_FourArgs(): Unit = {
    val expectedSize = 4

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"FourArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2),
      expected(3)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_FiveArgs_ValidateArgs(): Unit = {

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, null)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, null)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, null)
    )
  }

  @Test def of_FiveArgs(): Unit = {
    val expectedSize = 5

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"FiveArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2),
      expected(3),
      expected(4)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_SixArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, null)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, null)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, null)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, null)
    )
  }

  @Test def of_SixArgs(): Unit = {
    val expectedSize = 6

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"SixArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2),
      expected(3),
      expected(4),
      expected(5)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_SevenArgs_ValidateArgs(): Unit = {

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, null)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, null)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, null)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, null)
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, null)
    )
  }

  @Test def of_SevenArgs(): Unit = {
    val expectedSize = 7

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"SevenArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2),
      expected(3),
      expected(4),
      expected(5),
      expected(6)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_EightArgs_ValidateArgs(): Unit = {

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, null)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, null)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, null)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, null)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, null)
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, null)
    )

    assertThrows(
      "null eigth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, 7, null)
    )
  }

  @Test def of_EightArgs(): Unit = {
    val expectedSize = 8

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"EightArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2),
      expected(3),
      expected(4),
      expected(5),
      expected(6),
      expected(7)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_NineArgs_ValidateArgs(): Unit = {

    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, null)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, null)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, null)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, null)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, null)
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, null)
    )

    assertThrows(
      "null eigth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, 7, null)
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, 7, 8, null)
    )
  }

  @Test def of_NineArgs(): Unit = {
    val expectedSize = 9

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"NineArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2),
      expected(3),
      expected(4),
      expected(5),
      expected(6),
      expected(7),
      expected(8)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_TenArgs_ValidateArgs(): Unit = {
    assertThrows(
      "null first argument should throw",
      classOf[NullPointerException],
      ju.List.of[String](null)
    )

    assertThrows(
      "null second argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, null)
    )

    assertThrows(
      "null third argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, null)
    )

    assertThrows(
      "null forth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, null)
    )

    assertThrows(
      "null fifth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, null)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, null)
    )

    assertThrows(
      "null sixth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, null)
    )

    assertThrows(
      "null seventh argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, null)
    )

    assertThrows(
      "null eigth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, 7, null)
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, 7, 8, null)
    )

    assertThrows(
      "null ninth argument should throw",
      classOf[NullPointerException],
      ju.List.of(1, 2, 3, 4, 5, 6, 7, 9, null)
    )
  }

  @Test def of_TenArgs(): Unit = {
    val expectedSize = 10

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"TenArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2),
      expected(3),
      expected(4),
      expected(5),
      expected(6),
      expected(7),
      expected(8),
      expected(9)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

  @Test def of_ElevenArgs(): Unit = {
    /* Boundary case.
     *  Does this fall over to using varargs on JVM? On SN?
     *  It exceeds the number of explicitly defined .of() methods.
     *  Answer: Appears to work fine on both JDK & Scala Native.
     */

    val expectedSize = 11

    val expected = new Array[String](expectedSize)
    for (j <- 0 until expectedSize)
      expected(j) = s"ElevenArgs_${j}"

    val result = ju.List.of(
      expected(0),
      expected(1),
      expected(2),
      expected(3),
      expected(4),
      expected(5),
      expected(6),
      expected(7),
      expected(8),
      expected(9),
      expected(10)
    )

    assertEquals("list size", expectedSize, result.size())

    for (j <- 0 until expectedSize)
      assertEquals(s"contents(${j})", expected(j), result.get(j))
  }

}
