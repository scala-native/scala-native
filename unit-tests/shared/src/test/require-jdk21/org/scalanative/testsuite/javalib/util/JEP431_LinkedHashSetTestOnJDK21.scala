package org.scalanative.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.{lang => jl}
import java.util.LinkedHashSet

class JEP431_LinkedHashSetTestOnJDK21 {

  @Test def addFirst_JVM(): Unit = {

    assumeTrue(
      "SN does not properly implement JVM defined behavior",
      Platform.executingInJVM
    )

    val expectedSize = 4
    val expected = 6

    val lhs = new LinkedHashSet[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      lhs.add(j)

    lhs.addFirst(expected)

    assertEquals("LinkedHashSet size", expectedSize, lhs.size())
    assertEquals("first element", expected, lhs.getFirst())
  }

  @Test def addFirst_SN(): Unit = {

    assumeFalse(
      "Skip forced hard failure on known incorrect SN implementation",
      Platform.executingInJVM
    )

    val lhs = new LinkedHashSet[Int]()

    assertThrows(
      "LinkedHashSet#addFirst should throw, SN implementation is wrong",
      classOf[UnsupportedOperationException],
      lhs.addFirst(6)
    )
  }

  @Test def addLast(): Unit = {
    val expectedSize = 4
    val expected = 7

    val lhs = new LinkedHashSet[Int](expectedSize)

    for (j <- 1 to expectedSize - 1)
      lhs.add(j)

    lhs.addLast(expected)

    assertEquals("LinkedHashSet size", expectedSize, lhs.size())
    assertEquals("last element", expected, lhs.getLast())
  }

  @Test def getFirst_EmptyLinkedHashSet(): Unit = {
    val lhs = new LinkedHashSet[Int]()

    assertThrows(
      "getFirst() of empty LinkedHashSet should throw",
      classOf[NoSuchElementException],
      lhs.getFirst()
    )
  }

  @Test def getFirst(): Unit = {
    val expected = -2

    val lhs = new LinkedHashSet[Int]()

    lhs.add(expected)
    lhs.add(0)
    lhs.add(2)

    val result = lhs.getFirst()

    assertEquals("getFirst()", expected, result)
  }

  @Test def getLast_EmptyLinkedHashSet(): Unit = {
    val lhs = new LinkedHashSet[Int]()

    assertThrows(
      "getLast() of empty LinkedHashSet should throw",
      classOf[NoSuchElementException],
      lhs.getLast()
    )
  }

  @Test def getLast(): Unit = {
    val expected = 2

    val lhs = new LinkedHashSet[Int]()

    lhs.add(0)
    lhs.add(-9)
    lhs.add(expected)

    val result = lhs.getLast()

    assertEquals("getLast()", expected, result)
  }

  @Test def removeFirst_EmptyLinkedHashSet(): Unit = {
    val lhs = new LinkedHashSet[Int]()

    assertThrows(
      "removeFirst of empty LinkedHashSet should throw",
      classOf[NoSuchElementException],
      lhs.removeFirst()
    )
  }

  @Test def removeFirst(): Unit = {
    val expected = -2

    val lhs = new LinkedHashSet[Int]()

    lhs.add(expected)
    lhs.add(2)
    lhs.add(0)
    lhs.add(4)

    val result = lhs.removeFirst()

    assertEquals("removeFirst()", expected, result)
  }

  @Test def removeLast_EmptyLinkedHashSet(): Unit = {
    val lhs = new LinkedHashSet[Int]()

    assertThrows(
      "removeLast() of empty LinkedHashSet should throw",
      classOf[NoSuchElementException],
      lhs.removeLast()
    )
  }

  @Test def removeLast(): Unit = {
    val expected = -0.0

    val lhs = new LinkedHashSet[jl.Double]()

    lhs.add(-1.1)
    lhs.add(-3.3)
    lhs.add(-2.2)
    lhs.add(expected)

    val result = lhs.removeLast()

    assertEquals("removeLast()", expected, result)

    assertTrue(
      "retrieval of -0.0D failed",
      expected.compareTo(result) == 0
    ) // if broken, +0.0 will fail having result: -1
  }

  @Test def reversed(): Unit = {

    val expectedSize = 9

    val lhs = new LinkedHashSet[Int]()

    for (j <- 1 to expectedSize)
      lhs.add(j)

    assertEquals("original size()", expectedSize, lhs.size())

    val revLhs = lhs.reversed()

    assertEquals("reversed size()", expectedSize, revLhs.size())

    val it = revLhs.iterator()

    for (j <- expectedSize to 1 by -1)
      assertEquals("reversed content", j, it.next())
  }

  @Test def reversed_TwiceReversed(): Unit = {

    val expectedSize = 9

    val lhs = new LinkedHashSet[Int]()

    for (j <- 1 to expectedSize)
      lhs.add(j)

    val revLhs = lhs.reversed()

    /* JDK 24 Javadoc does not require reference equality and the library
     * does not implement it.
     * The calls equivalent to reversed().reversed() would most likely be
     * .toDescendingSet().toDescendingSet(). Apparently the original Set is
     * not remembered here.
     *
     * Scala Native follows the JVM practice of providing only content
     * equality for this class.
     */

    assertEquals(
      "twice reversed LinkedHashSet should be content equal to original",
      lhs,
      revLhs.reversed()
    )
  }
}
