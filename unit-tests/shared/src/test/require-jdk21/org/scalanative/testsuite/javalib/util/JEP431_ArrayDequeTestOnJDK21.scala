package org.scalanative.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.lang as jl
import java.util.ArrayDeque

/* Test only the JEP431 methods of Deque.scala.
 */

class JEP431_ArrayDequeTestOnJDK21 {

  @Test def addFirst(): Unit = {
    val expectedSize = 4
    val expected = 6

    val ad = new ArrayDeque[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      ad.add(j)

    ad.addFirst(expected)

    assertEquals("arraylist size", expectedSize, ad.size())
    assertEquals("first element", expected, ad.peek())
  }

  @Test def addLast(): Unit = {
    val expectedSize = 4
    val expected = 7

    val ad = new ArrayDeque[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      ad.add(j)

    ad.addLast(expected)

    assertEquals("arraylist size", expectedSize, ad.size())
    assertEquals("last element", expected, ad.peekLast())
  }

  @Test def getFirst_EmptyArrayDeque(): Unit = {
    val ad = new ArrayDeque[Int]()

    assertThrows(
      "getFirst() of empty ArrayDeque should throw",
      classOf[NoSuchElementException],
      ad.getFirst()
    )
  }

  @Test def getFirst(): Unit = {
    val expected = -2

    val ad = new ArrayDeque[Int]()

    ad.add(expected)
    ad.add(0)
    ad.add(2)

    val result = ad.getFirst()

    assertEquals("getFirst()", expected, result)
  }

  @Test def getLast_EmptyArrayDeque(): Unit = {
    val ad = new ArrayDeque[Int]()

    assertThrows(
      "getLast() of empty ArrayDeque should throw",
      classOf[NoSuchElementException],
      ad.getLast()
    )
  }

  @Test def getLast(): Unit = {
    val expected = -9

    val ad = new ArrayDeque[Int]()

    ad.add(0)
    ad.add(2)
    ad.add(expected)

    val result = ad.getLast()

    assertEquals("getLast()", expected, result)
  }

  @Test def removeFirst_EmptyArrayDeque(): Unit = {
    val ad = new ArrayDeque[Int]()

    assertThrows(
      "removeFirst of empty ArrayDeque should throw",
      classOf[NoSuchElementException],
      ad.removeFirst()
    )
  }

  @Test def removeFirst(): Unit = {
    val expected = -2

    val ad = new ArrayDeque[Int]()

    ad.add(expected)
    ad.add(2)
    ad.add(0)
    ad.add(4)

    val result = ad.removeFirst()

    assertEquals("removeFirst()", expected, result)
  }

  @Test def removeLast_EmptyArrayDeque(): Unit = {
    val ad = new ArrayDeque[Int]()

    assertThrows(
      "removeLast() of empty ArrayDeque should throw",
      classOf[NoSuchElementException],
      ad.removeLast()
    )
  }

  @Test def removeLast(): Unit = {
    val expected = -0.0 // -0.0 is always a problem magnet

    val ad = new ArrayDeque[scala.Double]() // java primitive double

    ad.add(1)
    ad.add(0)
    ad.add(-1)
    ad.add(expected)

    val result = ad.removeLast()

    assertTrue(
      "removeLast()",
      jl.Double.compare(expected, result) == 0 // compare primitive doubles.
    ) // if removeLast is broken & returned +0.0, compare will fail -1
  }

  /* ArrayDeque uses the default Deque#reversed method.
   * Exercise that method in a concrete class.
   *
   * See JEP431_ReverseOrderOnJDK21.scala for more extensive testing
   * of the reversed Sequence.
   */

  @Test def reversed(): Unit = {

    val expectedSize = 9

    val ad = new ArrayDeque[Int](expectedSize)

    for (j <- 1 to expectedSize)
      ad.add(j)

    assertEquals("original size()", expectedSize, ad.size())

    val revAd = ad.reversed()

    assertEquals("reversed size()", expectedSize, revAd.size())

    val it = revAd.iterator()

    for (j <- expectedSize to 1 by -1)
      assertEquals("reversed content", j, it.next())
  }
}
