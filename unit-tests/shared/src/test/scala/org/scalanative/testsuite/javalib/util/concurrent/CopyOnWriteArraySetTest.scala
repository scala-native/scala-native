/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util._
import java.util.concurrent.CopyOnWriteArraySet

import org.junit.Assert._
import org.junit.{Ignore, Test}

class CopyOnWriteArraySetTest extends JSR166Test {
  import CopyOnWriteArraySetTest._
  import JSR166Test._

  private val itemOne = itemFor(1)
  private val itemTwo = itemFor(2)
  private val itemThree = itemFor(3)
  private val itemFour = itemFor(4)
  private val itemFive = itemFor(5)
  private val itemSix = itemFor(6)

  /** Default-constructed set is empty */
  @Test def testConstructor(): Unit = {
    val a = new CopyOnWriteArraySet[Item]()
    assertTrue(a.isEmpty())
  }

  /** Collection-constructed set holds all of its elements */
  @Test def testConstructor3(): Unit = {
    val items = defaultItems
    val a = new CopyOnWriteArraySet[Item](Arrays.asList(items: _*))
    for (i <- 0 until SIZE) mustContain(a, i)
  }

  /** addAll adds each non-duplicate element from the given collection */
  @Test def testAddAll(): Unit = {
    val full = populatedSet(3).asInstanceOf[Set[Item]]
    assertTrue(full.addAll(Arrays.asList(itemThree, itemFour, itemFive)))
    mustEqual(6, full.size())
    assertFalse(full.addAll(Arrays.asList(itemThree, itemFour, itemFive)))
    mustEqual(6, full.size())
  }

  /** addAll adds each non-duplicate element from the given collection */
  @Test def testAddAll2(): Unit = {
    val full = populatedSet(3).asInstanceOf[Set[Item]]
    assertTrue(full.addAll(Arrays.asList(itemThree, itemFour, itemOne)))
    mustEqual(5, full.size())
    assertFalse(full.addAll(Arrays.asList(itemThree, itemFour, itemOne)))
    mustEqual(5, full.size())
  }

  /** add will not add the element if it already exists in the set */
  @Test def testAdd2(): Unit = {
    val full = populatedSet(3).asInstanceOf[Set[Item]]
    full.add(itemOne)
    mustEqual(3, full.size())
  }

  /** add adds the element when it does not exist in the set */
  @Test def testAdd3(): Unit = {
    val full = populatedSet(3).asInstanceOf[Set[Item]]
    full.add(itemThree)
    mustContain(full, itemThree)
  }

  /** clear removes all elements from the set */
  @Test def testClear(): Unit = {
    val full = populatedSet(3).asInstanceOf[Collection[Item]]
    full.clear()
    mustEqual(0, full.size())
    assertTrue(full.isEmpty())
  }

  /** contains returns true for added elements */
  @Test def testContains(): Unit = {
    val full = populatedSet(3).asInstanceOf[Collection[Item]]
    mustContain(full, itemOne)
    mustNotContain(full, itemFive)
  }

  /** Sets with equal elements are equal */
  @Test def testEquals(): Unit = {
    val a = populatedSet(3)
    val b = populatedSet(3)
    assertTrue(a.equals(b))
    assertTrue(b.equals(a))
    assertTrue(a.containsAll(b))
    assertTrue(b.containsAll(a))
    mustEqual(a.hashCode(), b.hashCode())
    mustEqual(a.size(), b.size())

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
    mustEqual(a.hashCode(), b.hashCode())

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
    mustEqual(a.hashCode(), b.hashCode())
    mustEqual(a.size(), b.size())

    val empty1 =
      new CopyOnWriteArraySet[Item](Collections.emptyList[Item]())
    val empty2 =
      new CopyOnWriteArraySet[Item](Collections.emptyList[Item]())
    assertTrue(empty1.equals(empty1))
    assertTrue(empty1.equals(empty2))

    assertFalse(empty1.equals(a))
    assertFalse(a.equals(empty1))
    assertFalse(a.equals(null))
  }

  /** containsAll returns true for collections with subset of elements */
  @Test def testContainsAll(): Unit = {
    val full = populatedSet(3).asInstanceOf[Collection[Item]]
    assertTrue(full.containsAll(full))
    assertTrue(full.containsAll(Collections.emptyList[Item]()))
    assertTrue(full.containsAll(Arrays.asList(itemOne)))
    assertTrue(full.containsAll(Arrays.asList(itemOne, itemTwo)))
    assertFalse(full.containsAll(Arrays.asList(itemOne, itemTwo, itemSix)))
    assertFalse(full.containsAll(Arrays.asList(itemSix)))

    val empty1 =
      new CopyOnWriteArraySet[Item](Collections.emptyList[Item]())
    val empty2 =
      new CopyOnWriteArraySet[Item](Collections.emptyList[Item]())
    assertTrue(empty1.containsAll(empty2))
    assertTrue(empty1.containsAll(empty1))
    assertFalse(empty1.containsAll(full))
    assertTrue(full.containsAll(empty1))

    try {
      full.containsAll(null)
      shouldThrow()
    } catch {
      case _: NullPointerException =>
    }
  }

  /** isEmpty is true when empty, else false */
  @Test def testIsEmpty(): Unit = {
    assertTrue(populatedSet(0).isEmpty())
    assertFalse(populatedSet(3).isEmpty())
  }

  /** iterator() returns elements in insertion order */
  @Test def testIterator(): Unit = {
    val empty = new CopyOnWriteArraySet[Item]()
    assertFalse(empty.iterator().hasNext())
    try {
      empty.iterator().next()
      shouldThrow()
    } catch {
      case _: NoSuchElementException =>
    }

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full = populatedSet(elements)

    val it = full.iterator()
    for (j <- 0 until SIZE) {
      assertTrue(it.hasNext())
      mustEqual(elements(j), it.next())
    }
    assertIteratorExhausted(it)
  }

  /** iterator of empty collection has no elements */
  @Test def testEmptyIterator(): Unit = {
    assertIteratorExhausted(new CopyOnWriteArraySet[Item]().iterator())
  }

  /** iterator remove is unsupported */
  @Test def testIteratorRemove(): Unit = {
    val full = populatedSet(3)
    val it = full.iterator()
    it.next()
    try {
      it.remove()
      shouldThrow()
    } catch {
      case _: UnsupportedOperationException =>
    }
  }

  /** toString holds toString of elements */
  @Test def testToString(): Unit = {
    mustEqual("[]", new CopyOnWriteArraySet[Item]().toString())
    val full = populatedSet(3).asInstanceOf[Collection[Item]]
    val s = full.toString()
    for (i <- 0 until 3)
      assertTrue(s.contains(String.valueOf(i)))
    mustEqual(new ArrayList[Item](full).toString(), full.toString())
  }

  /** removeAll removes all elements from the given collection */
  @Test def testRemoveAll(): Unit = {
    val full = populatedSet(3).asInstanceOf[Set[Item]]
    assertTrue(full.removeAll(Arrays.asList(itemOne, itemTwo)))
    mustEqual(1, full.size())
    assertFalse(full.removeAll(Arrays.asList(itemOne, itemTwo)))
    mustEqual(1, full.size())
  }

  /** remove removes an element */
  @Test def testRemove(): Unit = {
    val full = populatedSet(3).asInstanceOf[Collection[Item]]
    full.remove(itemOne)
    mustNotContain(full, itemOne)
    mustEqual(2, full.size())
  }

  /** size returns the number of elements */
  @Test def testSize(): Unit = {
    val empty = new CopyOnWriteArraySet[Item]()
    val full = populatedSet(3)
    mustEqual(3, full.size())
    mustEqual(0, empty.size())
  }

  /** toArray() returns an Object array in insertion order */
  @Test def testToArray(): Unit = {
    val a = new CopyOnWriteArraySet[Item]().toArray()
    assertTrue(Arrays.equals(new Array[AnyRef](0), a))
    assertSame(classOf[Array[AnyRef]], a.getClass())

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full = populatedSet(elements)

    assertTrue(Arrays.equals(elements.asInstanceOf[Array[AnyRef]], full.toArray()))
    assertSame(classOf[Array[AnyRef]], full.toArray().getClass())
  }

  /** toArray(Item array) returns an Item array in insertion order */
  @Test def testToArray2(): Unit = {
    def fillItems(a: Array[Item]): Unit =
      Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    def arraysEqual(a: Array[Item], b: Array[Item]): Boolean =
      Arrays.equals(
        a.asInstanceOf[Array[Object]],
        b.asInstanceOf[Array[Object]]
      )

    val empty = new CopyOnWriteArraySet[Item]()
    var a: Array[Item] = null

    a = new Array[Item](0)
    assertSame(a, empty.toArray(a))

    a = new Array[Item](SIZE / 2)
    fillItems(a)
    assertSame(a, empty.toArray(a))
    assertNull(a(0))
    for (i <- 1 until a.length) mustEqual(42, a(i))

    val elements = seqItems(SIZE)
    shuffle(elements)
    val full = populatedSet(elements)

    fillItems(a)
    assertTrue(arraysEqual(elements, full.toArray(a)))
    for (i <- 0 until a.length) mustEqual(42, a(i))
    assertSame(classOf[Array[Item]], full.toArray(a).getClass())

    a = new Array[Item](SIZE)
    fillItems(a)
    assertSame(a, full.toArray(a))
    assertTrue(arraysEqual(elements, a))

    a = new Array[Item](2 * SIZE)
    fillItems(a)
    assertSame(a, full.toArray(a))
    assertTrue(arraysEqual(elements, Arrays.copyOf(a, SIZE)))
    assertNull(a(SIZE))
    for (i <- SIZE + 1 until a.length) mustEqual(42, a(i))
  }

  /** toArray throws ArrayStoreException for incompatible arrays */
  @Ignore("Scala Native reference arrays do not preserve runtime component types")
  @Test def testToArray_ArrayStoreException(): Unit = {
    val c = new CopyOnWriteArraySet[Item]()
    c.add(itemOne)
    c.add(itemTwo)
    try {
      c.toArray(new Array[java.lang.Long](5))
      shouldThrow()
    } catch {
      case _: ArrayStoreException =>
    }
  }

  /** A deserialized/reserialized set equals original */
  @Ignore("No ObjectInputStream in Scala Native")
  @Test def testSerialization(): Unit = ()

  /** addAll is idempotent */
  @Test def testAddAll_idempotent(): Unit = {
    val x = populatedSet(SIZE).asInstanceOf[Set[Item]]
    val y = new CopyOnWriteArraySet[Item](x)
    y.addAll(x)
    mustEqual(x, y)
    mustEqual(y, x)
  }
}

object CopyOnWriteArraySetTest {
  import JSR166Test._

  def populatedSet(n: Int): CopyOnWriteArraySet[Item] = {
    val a = new CopyOnWriteArraySet[Item]()
    assertTrue(a.isEmpty())
    for (i <- 0 until n) mustAdd(a, i)
    mustEqual(n == 0, a.isEmpty())
    mustEqual(n, a.size())
    a
  }

  def populatedSet(elements: Array[Item]): CopyOnWriteArraySet[Item] = {
    val a = new CopyOnWriteArraySet[Item]()
    assertTrue(a.isEmpty())
    for (i <- elements.indices) mustAdd(a, elements(i))
    assertFalse(a.isEmpty())
    mustEqual(elements.length, a.size())
    a
  }
}
