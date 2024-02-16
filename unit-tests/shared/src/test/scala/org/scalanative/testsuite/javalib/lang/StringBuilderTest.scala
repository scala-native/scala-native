package org.scalanative.testsuite.javalib.lang

import java.lang._

// Ported from Scala.js. Additional code added for Scala Native.

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringBuilderTest {

  /* Implementation Notes:
   *
   * 1) Many of these methods are default methods in
   *    AbstractStringBuilder.scala.  Many tests of such default
   *    methods are implemented only in this file, because they would
   *    be duplicate boilerplate and a maintenance headache in
   *    StringBufferTest.scala.
   *
   * 2) Many of these methods are default methods in
   *    This file contains a number of "fooShouldNotChangePriorString"
   *    tests. These are for methods which could potentially change
   *    a String created before they are called.
   *
   *    For methods such as 'capacity()' it is clear that no such tests
   *    are needed. There are also no "shouldNotChange" tests for the following
   *    three methods. Their access to the StringBuilder.value Array should be
   *    strictly read only:
   *      subSequence(int start, int end)
   *      substring(int start)
   *      substring(int start, int end)
   */

  val expectedString =
    """
    |Είναι πλέον κοινά παραδεκτό ότι ένας αναγνώστης αποσπάται από το
    |περιεχόμενο που διαβάζει, όταν εξετάζει τη διαμόρφωση μίας σελίδας.
    """

  def newBuilder: java.lang.StringBuilder =
    new java.lang.StringBuilder

  def initBuilder(str: String): java.lang.StringBuilder =
    new java.lang.StringBuilder(str)

  @Test def append(): Unit = {
    assertEquals("asdf", newBuilder.append("asdf").toString)
    assertEquals("null", newBuilder.append(null: AnyRef).toString)
    assertEquals("null", newBuilder.append(null: String).toString)
    assertEquals("nu", newBuilder.append(null: CharSequence, 0, 2).toString)
    assertEquals("true", newBuilder.append(true).toString)
    assertEquals("a", newBuilder.append('a').toString)
    assertEquals("abcd", newBuilder.append(Array('a', 'b', 'c', 'd')).toString)
    assertEquals(
      "bc",
      newBuilder.append(Array('a', 'b', 'c', 'd'), 1, 2).toString
    )
    assertEquals("4", newBuilder.append(4.toByte).toString)
    assertEquals("304", newBuilder.append(304.toShort).toString)
    assertEquals("100000", newBuilder.append(100000).toString)
  }

  @Test def appendFloats(): Unit = {
    assertEquals("2.5", newBuilder.append(2.5f).toString)
    assertEquals(
      "2.5 3.5",
      newBuilder.append(2.5f).append(' ').append(3.5f).toString
    )
  }

  @Test def appendDoubles(): Unit = {
    assertEquals("3.5", newBuilder.append(3.5).toString)
    assertEquals(
      "2.5 3.5",
      newBuilder.append(2.5).append(' ').append(3.5).toString
    )
  }

  @Test def appendShouldNotChangePriorString(): Unit = {
    val sb = initBuilder(expectedString)
    val prior = sb.toString()

    sb.append("Suffix")

    assertEquals("Unexpected change in prior string", expectedString, prior)
  }

  @Test def insert(): Unit = {
    assertEquals("asdf", newBuilder.insert(0, "asdf").toString)
    assertEquals("null", newBuilder.insert(0, null: AnyRef).toString)
    assertEquals("null", newBuilder.insert(0, null: String).toString)
    assertEquals("nu", newBuilder.insert(0, null: CharSequence, 0, 2).toString)
    assertEquals("true", newBuilder.insert(0, true).toString)
    assertEquals("a", newBuilder.insert(0, 'a').toString)
    assertEquals(
      "abcd",
      newBuilder.insert(0, Array('a', 'b', 'c', 'd')).toString
    )
    assertEquals(
      "bc",
      newBuilder.insert(0, Array('a', 'b', 'c', 'd'), 1, 2).toString
    )
    assertEquals("4", newBuilder.insert(0, 4.toByte).toString)
    assertEquals("304", newBuilder.insert(0, 304.toShort).toString)
    assertEquals("100000", newBuilder.insert(0, 100000).toString)

    assertEquals("abcdef", initBuilder("adef").insert(1, "bc").toString)
    assertEquals("abcdef", initBuilder("abcd").insert(4, "ef").toString)
    assertEquals(
      "abcdef",
      initBuilder("adef").insert(1, Array('b', 'c')).toString
    )
    assertEquals(
      "abcdef",
      initBuilder("adef").insert(1, initBuilder("bc")).toString
    )
    assertEquals(
      "abcdef",
      initBuilder("abef")
        .insert(2, Array('a', 'b', 'c', 'd', 'e'), 2, 2)
        .toString
    )

    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuilder("abcd").insert(-1, "whatever")
    )
    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuilder("abcd").insert(5, "whatever")
    )
  }

  @Test def insertFloatOrDouble(): Unit = {
    assertEquals("2.5", newBuilder.insert(0, 2.5f).toString)
    assertEquals("3.5", newBuilder.insert(0, 3.5).toString)
  }

  @Test def insertStringBuilder(): Unit = {
    assertEquals(
      "abcdef",
      initBuilder("abef").insert(2, initBuilder("abcde"), 2, 4).toString
    )
  }

  @Test def insertShouldNotChangePriorString(): Unit = {
    val sb = initBuilder(expectedString)
    val prior = sb.toString()

    sb.insert(10, "Intron")

    assertEquals("Unexpected change in prior string", expectedString, prior)
  }

  @Test def shouldAllowStringInterpolationToSurviveNullAndUndefined(): Unit = {
    assertEquals("null", s"${null}")
  }

  @Test def deleteCharAt(): Unit = {
    assertEquals("023", initBuilder("0123").deleteCharAt(1).toString)
    assertEquals("123", initBuilder("0123").deleteCharAt(0).toString)
    assertEquals("012", initBuilder("0123").deleteCharAt(3).toString)
    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuilder("0123").deleteCharAt(-1)
    )
    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuilder("0123").deleteCharAt(4)
    )
  }

  @Test def deleteCharAtShouldNotChangePriorString(): Unit = {
    val sb = initBuilder(expectedString)
    val prior = sb.toString()

    sb.deleteCharAt(10)

    assertEquals("Unexpected change in prior string", expectedString, prior)
  }

  @Test def replace(): Unit = {
    assertEquals("0bc3", initBuilder("0123").replace(1, 3, "bc").toString)
    assertEquals("abcd", initBuilder("0123").replace(0, 4, "abcd").toString)
    assertEquals("abcd", initBuilder("0123").replace(0, 10, "abcd").toString)
    assertEquals("012defg", initBuilder("0123").replace(3, 10, "defg").toString)
    assertEquals("xxxx123", initBuilder("0123").replace(0, 1, "xxxx").toString)
    assertEquals("0xxxx123", initBuilder("0123").replace(1, 1, "xxxx").toString)
    assertEquals("0123x", initBuilder("0123").replace(4, 5, "x").toString)

    assertThrows(
      classOf[StringIndexOutOfBoundsException],
      initBuilder("0123").replace(-1, 3, "x")
    )
  }

  @Test def replaceShouldNotChangePriorString(): Unit = {
    val sb = initBuilder(expectedString)
    val prior = sb.toString()

    val replacement = "Intruder Alert on deck 20!"
    val offset = 20

    sb.replace(offset, offset + replacement.length(), replacement)

    assertEquals("Unexpected change in prior string", expectedString, prior)
  }

  @Test def reverseShouldNotChangePriorString(): Unit = {
    val sb = initBuilder(expectedString)
    val prior = sb.toString()

    sb.reverse()

    assertEquals("Unexpected change in prior string", expectedString, prior)
  }

  @Test def setCharAt(): Unit = {
    val b = newBuilder
    b.append("foobar")

    b.setCharAt(2, 'x')
    assertEquals("foxbar", b.toString)

    b.setCharAt(5, 'h')
    assertEquals("foxbah", b.toString)

    assertThrows(classOf[StringIndexOutOfBoundsException], b.setCharAt(-1, 'h'))
    assertThrows(classOf[StringIndexOutOfBoundsException], b.setCharAt(6, 'h'))
  }

  @Test def setCharAtShouldNotChangePriorString(): Unit = {
    val sb = initBuilder(expectedString)
    val prior = sb.toString()

    sb.setCharAt(30, '?')

    assertEquals("Unexpected change in prior string", expectedString, prior)
  }

  @Test def ensureCapacity(): Unit = {
    // test that ensureCapacity is linking. And grows first time without throw.
    newBuilder.ensureCapacity(20)
  }

  @Test def ensureCapacityNotChangePriorString(): Unit = {
    val sb = initBuilder(expectedString)
    val prior = sb.toString()

    sb.ensureCapacity(expectedString.length() * 2)

    assertEquals("Unexpected change in prior string", expectedString, prior)
  }

  @Test def shouldProperlySetLength(): Unit = {
    val b = newBuilder
    b.append("foobar")

    assertThrows(classOf[StringIndexOutOfBoundsException], b.setLength(-3))

    assertEquals("foo", { b.setLength(3); b.toString })
    assertEquals("foo\u0000\u0000\u0000", { b.setLength(6); b.toString })
  }

  @Test def setLengthShouldNotChangePriorString(): Unit = {
    val sb = initBuilder(expectedString)
    val prior = sb.toString()

    sb.setLength(5)

    assertEquals("Unexpected change in prior string", expectedString, prior)
  }

  @Test def trimToSizeShouldNotChangePriorString(): Unit = {
    /* sb.length < InitialCapacity means there are unused Char slots
     * so "trimToSize()" will compact & change StringBuffer value.
     */
    val expected = "Mordor"
    val sb = initBuilder(expected)
    val prior = sb.toString()

    sb.trimToSize()

    assertEquals("Unexpected change in prior string", expected, prior)
  }

  @Test def appendCodePoint(): Unit = {
    val b = newBuilder
    b.appendCodePoint(0x61)
    assertEquals("a", b.toString)
    b.appendCodePoint(0x10000)
    assertEquals("a\uD800\uDC00", b.toString)
    b.append("fixture")
    b.appendCodePoint(0x00010ffff)
    assertEquals("a\uD800\uDC00fixture\uDBFF\uDFFF", b.toString)
  }

  /** Checks that modifying a StringBuilder, converted to a String using a
   *  `.toString` call, is not breaking String immutability.
   */
  @Test def toStringThenModifyStringBuilder(): Unit = {
    val b = newBuilder
    b.append("foobar")

    val s = b.toString
    b.setCharAt(0, 'm')

    assertTrue(
      s"foobar should start with 'f' instead of '${s.charAt(0)}'",
      'f' == s.charAt(0)
    )
  }

  @Test def indexOfSubStringWithSurrogatePair(): Unit = {
    // Outlined "hello" in surrogate pairs
    val sb = new StringBuilder(
      "\ud835\udd59\ud835\udd56\ud835\udd5d\ud835\udd5d\ud835\udd60"
    )

    val needle = "\ud835\udd5d\ud835\udd60" // outlined ell oh

    val index = sb.indexOf(needle)
    assertEquals("indexOf surrogate outlined ell oh", 6, index)
  }

  @Test def lastIndexOfSubStringWithSurrogatePair(): Unit = {
    // Outlined "hello" in surrogate pairs
    val sb = new StringBuilder(
      "\ud835\udd59\ud835\udd56\ud835\udd5d\ud835\udd5d\ud835\udd60"
    )

    val needle = "\ud835\udd56\ud835\udd5d" // outlined e ell

    val index = sb.lastIndexOf(needle)
    assertEquals("lastIndexOf surrogate outlined ell", 2, index)
  }

}
