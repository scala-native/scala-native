package org.scalanative.testsuite.javalib.util

import java.util.LinkedList
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class JEP431_LinkedListTestOnJDK21 {

  /* To help keep track of the players: LinkedList overrides all the
   * List default methods, including reversed().
   *
   * It also tests the interaction/precedence of JEP 431 methods
   * from both List.scala & Deque.scala.
   */

  @Test def addFirst(): Unit = {
    val expectedSize = 4
    val expected = 6

    val ll = new LinkedList[Int]()

    for (j <- 1 to expectedSize - 1)
      ll.add(j)

    ll.addFirst(expected)

    assertEquals("arraylist size", expectedSize, ll.size())
    assertEquals("first element", expected, ll.get(0))
  }

  @Test def addLast(): Unit = {
    val expectedSize = 4
    val expected = 7

    val ll = new LinkedList[Int]()

    for (j <- 1 to expectedSize - 1)
      ll.add(j)

    ll.addLast(expected)

    assertEquals("arraylist size", expectedSize, ll.size())
    assertEquals("last element", expected, ll.get(expectedSize - 1))
  }

  @Test def getFirst_EmptyLinkedList(): Unit = {
    val ll = new LinkedList[Int]()

    assertThrows(
      "getFirst() of empty LinkedList should throw",
      classOf[NoSuchElementException],
      ll.getFirst()
    )
  }

  @Test def getFirst(): Unit = {
    val expected = -2

    val ll = new LinkedList[Int]()

    ll.add(expected)
    ll.add(0)
    ll.add(2)

    val result = ll.getFirst()

    assertEquals("getFirst()", expected, result)
  }

  @Test def getLast_EmptyLinkedList(): Unit = {
    val ll = new LinkedList[Int]()

    assertThrows(
      "getLast() of empty LinkedList should throw",
      classOf[NoSuchElementException],
      ll.getLast()
    )
  }

  @Test def getLast(): Unit = {
    val expected = -9

    val ll = new LinkedList[Int]()

    ll.add(0)
    ll.add(2)
    ll.add(expected)

    val result = ll.getLast()

    assertEquals("getLast()", expected, result)
  }

  @Test def removeFirst_EmptyLinkedList(): Unit = {
    val ll = new LinkedList[Int]()

    assertThrows(
      "removeFirst of empty LinkedList should throw",
      classOf[NoSuchElementException],
      ll.removeFirst()
    )
  }

  @Test def removeFirst(): Unit = {
    val expected = -2

    val ll = new LinkedList[Int]()

    ll.add(expected)
    ll.add(2)
    ll.add(0)
    ll.add(4)

    val result = ll.removeFirst()

    assertEquals("removeFirst()", expected, result)
  }

  @Test def removeLast_EmptyLinkedList(): Unit = {
    val ll = new LinkedList[Int]()

    assertThrows(
      "removeLast() of empty LinkedList should throw",
      classOf[NoSuchElementException],
      ll.removeLast()
    )
  }

  @Test def removeLast(): Unit = {
    val expected = -0.0

    val ll = new LinkedList[jl.Double]()

    ll.add(1)
    ll.add(0)
    ll.add(-1)
    ll.add(expected)

    val result = ll.removeLast()

    assertEquals("removeLast()", expected, result)

    assertTrue(
      "retrieval of -0.0D failed",
      expected.compareTo(result) == 0
    ) // if broken, +0.0 will fail having result: -1
  }

  @Test def reversed(): Unit = {

    val expectedSize = 9

    val ll = new LinkedList[Int]()

    for (j <- 1 to expectedSize)
      ll.add(j)

    assertEquals("original size()", expectedSize, ll.size())

    val revLl = ll.reversed()

    assertEquals("reversed size()", expectedSize, revLl.size())

    val it = revLl.iterator()

    for (j <- expectedSize to 1 by -1)
      assertEquals("reversed content", j, it.next())
  }
}
