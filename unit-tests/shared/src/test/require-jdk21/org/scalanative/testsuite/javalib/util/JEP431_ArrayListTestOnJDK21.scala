package org.scalanative.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.{lang => jl}
import java.util.ArrayList

/* Test only the JEP431 methods of a time-honored and frequently used
 * List implementation.
 * It is sure to be one of the first that users in the wild exercise.
 */

class JEP431_ArrayListTestOnJDK21 {

  /* To help keep track of the players: ArrayList uses the List interface
   * default implementation of reversed() but overrides the List default
   * *First and *Last methods.
   */

  @Test def addFirst(): Unit = {
    val expectedSize = 4
    val expected = 6

    val al = new ArrayList[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      al.add(j)

    al.addFirst(expected)

    assertEquals("ArrayList size", expectedSize, al.size())
    assertEquals("first element", expected, al.get(0))
  }

  @Test def addLast(): Unit = {
    val expectedSize = 4
    val expected = 7

    val al = new ArrayList[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      al.add(j)

    al.addLast(expected)

    assertEquals("ArrayList size", expectedSize, al.size())
    assertEquals("last element", expected, al.get(expectedSize - 1))
  }

  @Test def getFirst_EmptyArrayList(): Unit = {
    val al = new ArrayList[Int]()

    assertThrows(
      "getFirst() of empty ArrayList should throw",
      classOf[NoSuchElementException],
      al.getFirst()
    )
  }

  @Test def getFirst(): Unit = {
    val expected = -2

    val al = new ArrayList[Int]()

    al.add(expected)
    al.add(0)
    al.add(2)

    val result = al.getFirst()

    assertEquals("getFirst()", expected, result)
  }

  @Test def getLast_EmptyArrayList(): Unit = {
    val al = new ArrayList[Int]()

    assertThrows(
      "getLast() of empty ArrayList should throw",
      classOf[NoSuchElementException],
      al.getLast()
    )
  }

  @Test def getLast(): Unit = {
    val expected = -9

    val al = new ArrayList[Int]()

    al.add(0)
    al.add(2)
    al.add(expected)

    val result = al.getLast()

    assertEquals("getLast()", expected, result)
  }

  @Test def removeFirst_EmptyArrayList(): Unit = {
    val al = new ArrayList[Int]()

    assertThrows(
      "removeFirst of empty ArrayList should throw",
      classOf[NoSuchElementException],
      al.removeFirst()
    )
  }

  @Test def removeFirst(): Unit = {
    val expected = -2

    val al = new ArrayList[Int]()

    al.add(expected)
    al.add(2)
    al.add(0)
    al.add(4)

    val result = al.removeFirst()

    assertEquals("removeFirst()", expected, result)
  }

  @Test def removeLast_EmptyArrayList(): Unit = {
    val al = new ArrayList[Int]()

    assertThrows(
      "removeLast() of empty ArrayList should throw",
      classOf[NoSuchElementException],
      al.removeLast()
    )
  }

  @Test def removeLast(): Unit = {
    val expected = -0.0 // -0.0 is always a problem magnet

    val al = new ArrayList[scala.Double]() // java primitive double

    al.add(1)
    al.add(0)
    al.add(-1)
    al.add(expected)

    val result = al.removeLast()

    assertTrue(
      "removeLast()",
      jl.Double.compare(expected, result) == 0 // compare primitive doubles.
    ) // if removeLast is broken & returned +0.0, compare will fail -1
  }

  /* ArrayList uses the default List#reversed method.
   * Exercise that method in a concrete class.
   *
   * See JEP431_ReverseOrderOnJDK21.scala for more extensive testing
   * of the reversed Sequence.
   */

  @Test def reversed(): Unit = {

    val expectedSize = 9

    val al = new ArrayList[Int](expectedSize)

    for (j <- 1 to expectedSize)
      al.add(j)

    assertEquals("original size()", expectedSize, al.size())

    val revAl = al.reversed()

    assertEquals("reversed size()", expectedSize, revAl.size())

    val it = revAl.iterator()

    for (j <- expectedSize to 1 by -1)
      assertEquals("reversed content", j, it.next())
  }
}
