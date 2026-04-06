/*
 * Ported from OpenJDK JSR-166 TCK test.
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.{Arrays, Collection, Iterator, NoSuchElementException}
import java.util.concurrent.CopyOnWriteArraySet

import scala.collection.JavaConverters._

import org.junit.Assert._
import org.junit.Test

import JSR166Test._

class CopyOnWriteArraySetTest extends JSR166Test {

  private def populatedSet(n: Int): CopyOnWriteArraySet[Item] = {
    val a = new CopyOnWriteArraySet[Item]
    assertTrue(a.isEmpty)
    (0 until n).foreach(i => mustAdd(a, i))
    mustEqual(n == 0, a.isEmpty)
    mustEqual(n, a.size)
    a
  }

  private def populatedSet(elements: Array[Item]): CopyOnWriteArraySet[Item] = {
    val a = new CopyOnWriteArraySet[Item]
    assertTrue(a.isEmpty)
    elements.foreach(e => mustAdd(a, e))
    assertFalse(a.isEmpty)
    mustEqual(elements.length, a.size)
    a
  }

  /**
   * Default-constructed set is empty
   */
  @Test def testConstructor(): Unit = {
    val a = new CopyOnWriteArraySet[Item]
    assertTrue(a.isEmpty)
  }

  /**
   * Collection-constructed set holds all of its elements
   */
  @Test def testConstructor3(): Unit = {
    val items = defaultItems
    val a = new CopyOnWriteArraySet[Item](items.asJava)
    (0 until SIZE).foreach(i => mustContain(a, i))
  }

  /**
   * addAll adds each non-duplicate element from the given collection
   */
  @Test def testAddAll(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.addAll(Arrays.asList(three, four, five)))
    mustEqual(6, full.size)
    assertFalse(full.addAll(Arrays.asList(three, four, five)))
    mustEqual(6, full.size)
  }

  /**
   * addAll adds each non-duplicate element from the given collection
   */
  @Test def testAddAll2(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.addAll(Arrays.asList(three, four, one)))
    mustEqual(5, full.size)
    assertFalse(full.addAll(Arrays.asList(three, four, one)))
    mustEqual(5, full.size)
  }

  /**
   * add will not add the element if it already exists in the set
   */
  @Test def testAdd2(): Unit = {
    val full = populatedSet(3)
    full.add(one)
    mustEqual(3, full.size)
  }

  /**
   * add adds the element when it does not exist in the set
   */
  @Test def testAdd3(): Unit = {
    val full = populatedSet(3)
    full.add(three)
    mustContain(full, three)
  }

  /**
   * clear removes all elements from the set
   */
  @Test def testClear(): Unit = {
    val full = populatedSet(3)
    full.clear()
    mustEqual(0, full.size)
    assertTrue(full.isEmpty)
  }

  /**
   * contains returns true for added elements
   */
  @Test def testContains(): Unit = {
    val full = populatedSet(3)
    mustContain(full, one)
    mustNotContain(full, five)
  }

  /**
   * Sets with equal elements are equal
   */
  @Test def testEquals(): Unit = {
    val a = populatedSet(3)
    val b = populatedSet(3)
    assertTrue(a.equals(b))
    assertTrue(b.equals(a))
    assertTrue(a.containsAll(b))
    assertTrue(b.containsAll(a))
    mustEqual(a.hashCode, b.hashCode)
    mustEqual(a.size, b.size)

    a.add(minusOne)
    assertFalse(a.equals(b))
    assertFalse(b.equals(a))
    assertTrue(a.containsAll(b))
    assertFalse(b.containsAll(a))
    b.add(minusOne)
    assertTrue(a.equals(b))
    assertTrue(b.equals(a))
    assertTrue(a.containsAll(b))
    assertTrue(b.containsAll(a))
    mustEqual(a.hashCode, b.hashCode)

    val x = a.iterator().next()
    a.remove(x)
    assertFalse(a.equals(b))
    assertFalse(b.equals(a))
    assertFalse(a.containsAll(b))
    assertTrue(b.containsAll(a))
    a.add(x)
    assertTrue(a.equals(b))
    assertTrue(b.equals(a))
    assertTrue(a.containsAll(b))
    assertTrue(b.containsAll(a))
    mustEqual(a.hashCode, b.hashCode)
    mustEqual(a.size, b.size)

    val empty1 = new CopyOnWriteArraySet[Item]()
    val empty2 = new CopyOnWriteArraySet[Item]()
    assertTrue(empty1.equals(empty1))
    assertTrue(empty1.equals(empty2))

    assertFalse(empty1.equals(a))
    assertFalse(a.equals(empty1))

    assertFalse(a.equals(null))
  }

  /**
   * containsAll returns true for collections with subset of elements
   */
  @Test def testContainsAll(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.containsAll(full))
    assertTrue(full.containsAll(Arrays.asList()))
    assertTrue(full.containsAll(Arrays.asList(one)))
    assertTrue(full.containsAll(Arrays.asList(one, two)))
    assertFalse(full.containsAll(Arrays.asList(one, two, six)))
    assertFalse(full.containsAll(Arrays.asList(six)))

    val empty1 = new CopyOnWriteArraySet[Item]()
    val empty2 = new CopyOnWriteArraySet[Item]()
    assertTrue(empty1.containsAll(empty2))
    assertTrue(empty1.containsAll(empty1))
    assertFalse(empty1.containsAll(full))
    assertTrue(full.containsAll(empty1))

    try {
      full.containsAll(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /**
   * isEmpty is true when empty, else false
   */
  @Test def testIsEmpty(): Unit = {
    assertTrue(populatedSet(0).isEmpty)
    assertFalse(populatedSet(3).isEmpty)
  }

  /**
   * iterator() returns an iterator containing the elements of the
   * set in insertion order
   */
  @Test def testIterator(): Unit = {
    val empty = new CopyOnWriteArraySet[Item]
    assertFalse(empty.iterator().hasNext)
    try {
      empty.iterator().next()
      shouldThrow()
    } catch {
      case success: NoSuchElementException =>
    }

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full = populatedSet(elements)

    val it = full.iterator()
    (0 until SIZE).foreach { j =>
      assertTrue(it.hasNext)
      mustEqual(elements(j), it.next())
    }
    assertIteratorExhausted(it)
  }

  /**
   * iterator of empty collection has no elements
   */
  @Test def testEmptyIterator(): Unit = {
    assertIteratorExhausted(new CopyOnWriteArraySet[Item]().iterator())
  }

  /**
   * iterator remove is unsupported
   */
  @Test def testIteratorRemove(): Unit = {
    val full = populatedSet(3)
    val it = full.iterator()
    it.next()
    try {
      it.remove()
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
  }

  /**
   * toString holds toString of elements
   */
  @Test def testToString(): Unit = {
    mustEqual("[]", new CopyOnWriteArraySet[Item]().toString)
    val full = populatedSet(3)
    val s = full.toString
    (0 until 3).foreach(i => assertTrue(s.contains(String.valueOf(i))))
    mustEqual(new java.util.ArrayList[Item](full).toString, full.toString)
  }

  /**
   * removeAll removes all elements from the given collection
   */
  @Test def testRemoveAll(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.removeAll(Arrays.asList(one, two)))
    mustEqual(1, full.size)
    assertFalse(full.removeAll(Arrays.asList(one, two)))
    mustEqual(1, full.size)
  }

  /**
   * remove removes an element
   */
  @Test def testRemove(): Unit = {
    val full = populatedSet(3)
    full.remove(one)
    mustNotContain(full, one)
    mustEqual(2, full.size)
  }

  /**
   * size returns the number of elements
   */
  @Test def testSize(): Unit = {
    val empty = new CopyOnWriteArraySet[Item]
    val full = populatedSet(3)
    mustEqual(3, full.size)
    mustEqual(0, empty.size)
  }

  /**
   * toArray() returns an Object array containing all elements from
   * the set in insertion order
   */
  @Test def testToArray(): Unit = {
    val a = new CopyOnWriteArraySet[Item]().toArray()
    assertTrue(Arrays.equals(new Array[AnyRef](0), a))
    assertSame(classOf[Array[AnyRef]], a.getClass)

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full = populatedSet(elements)

    assertTrue(Arrays.equals(elements, full.toArray()))
    assertSame(classOf[Array[AnyRef]], full.toArray().getClass)
  }

  /**
   * A deserialized/reserialized set equals original
   */
  @Test def testSerialization(): Unit = {
    val x = populatedSet(SIZE)
    val y = serialClone(x)

    assertNotSame(y, x)
    mustEqual(x.size, y.size)
    mustEqual(x.toString, y.toString)
    assertTrue(Arrays.equals(x.toArray, y.toArray))
    mustEqual(x, y)
    mustEqual(y, x)
  }

  /**
   * addAll is idempotent
   */
  @Test def testAddAll_idempotent(): Unit = {
    val x = populatedSet(SIZE)
    val y = new CopyOnWriteArraySet[Item](x)
    y.addAll(x)
    mustEqual(x, y)
    mustEqual(y, x)
  }

}
