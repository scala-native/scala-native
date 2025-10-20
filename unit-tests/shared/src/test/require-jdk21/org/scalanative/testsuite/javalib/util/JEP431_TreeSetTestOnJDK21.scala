package org.scalanative.testsuite.javalib.util

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import java.lang as jl
import java.util.TreeSet

/* This file exercises local overrides of methods in SortedSet and
 * NavigableSet.
 * 
 * See JEP431_ReverseOrderTreeSetTestOnJDK21 for more Tests using forward
 * and reversed() TreeSets.
 */

class JEP431_TreeSetTestOnJDK21 {

  @Test def addFirst(): Unit = {
    val ts = new TreeSet[Int]()

    assertThrows(
      "TreeSet#addFirst should throw",
      classOf[UnsupportedOperationException],
      ts.addFirst(6)
    )
  }

  @Test def addLast(): Unit = {
    val ts = new TreeSet[Int]()

    assertThrows(
      "TreeSet#addLast should throw",
      classOf[UnsupportedOperationException],
      ts.addLast(4)
    )
  }

  @Test def getFirst_EmptyTreeSet(): Unit = {
    val ts = new TreeSet[Int]()

    assertThrows(
      "getFirst() of empty TreeSet should throw",
      classOf[NoSuchElementException],
      ts.getFirst()
    )
  }

  @Test def getFirst(): Unit = {
    val expected = -2

    val ts = new TreeSet[Int]()

    ts.add(expected)
    ts.add(0)
    ts.add(2)

    val result = ts.getFirst()

    assertEquals("getFirst()", expected, result)
  }

  @Test def getLast_EmptyTreeSet(): Unit = {
    val ts = new TreeSet[Int]()

    assertThrows(
      "getLast() of empty TreeSet should throw",
      classOf[NoSuchElementException],
      ts.getLast()
    )
  }

  @Test def getLast(): Unit = {
    val expected = 2

    val ts = new TreeSet[Int]()

    ts.add(0)
    ts.add(expected)
    ts.add(-9)

    val result = ts.getLast()

    assertEquals("getLast()", expected, result)
  }

  @Test def removeFirst_EmptyTreeSet(): Unit = {
    val ts = new TreeSet[Int]()

    assertThrows(
      "removeFirst of empty TreeSet should throw",
      classOf[NoSuchElementException],
      ts.removeFirst()
    )
  }

  @Test def removeFirst(): Unit = {
    val expected = -2

    val ts = new TreeSet[Int]()

    ts.add(expected)
    ts.add(2)
    ts.add(0)
    ts.add(4)

    val result = ts.removeFirst()

    assertEquals("removeFirst()", expected, result)
  }

  @Test def removeLast_EmptyTreeSet(): Unit = {
    val ts = new TreeSet[Int]()

    assertThrows(
      "removeLast() of empty TreeSet should throw",
      classOf[NoSuchElementException],
      ts.removeLast()
    )
  }

  @Test def removeLast(): Unit = {
    val expected = -0.0

    val ts = new TreeSet[jl.Double]()

    ts.add(-1.1)
    ts.add(-3.3)
    ts.add(expected)
    ts.add(-2.2)

    val result = ts.removeLast()

    assertEquals("removeLast()", expected, result)

    assertTrue(
      "retrieval of -0.0D failed",
      expected.compareTo(result) == 0
    ) // if broken, +0.0 will fail having result: -1
  }

  @Test def reversed(): Unit = {

    val expectedSize = 9

    val ts = new TreeSet[Int]()

    for (j <- 1 to expectedSize)
      ts.add(j)

    assertEquals("original size()", expectedSize, ts.size())

    val revTs = ts.reversed()

    assertEquals("reversed size()", expectedSize, revTs.size())

    val it = revTs.iterator()

    for (j <- expectedSize to 1 by -1)
      assertEquals("reversed content", j, it.next())
  }

  @Test def reversed_TwiceReversed(): Unit = {

    val expectedSize = 9

    val ts = new TreeSet[Int]()

    for (j <- 1 to expectedSize)
      ts.add(j)

    val revTs = ts.reversed()

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
      "twice reversed TreeSet should be content equal to original",
      ts,
      revTs.reversed()
    )
  }
}
