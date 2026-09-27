/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ConcurrentSkipListSet
import java.util.{Arrays, Comparator, Iterator, NavigableSet, SortedSet}

import org.junit.Assert._
import org.junit._

object ConcurrentSkipListSubSetTest extends ConcurrentSkipListItems {
  import JSR166Test.{itemFor, mustAdd, mustEqual}

  class MyReverseComparator extends Comparator[AnyRef] {
    def compare(x: AnyRef, y: AnyRef): Int =
      y.asInstanceOf[Comparable[AnyRef]].compareTo(x)
  }

  /** Returns a new set of given size containing consecutive Items 0 ... n - 1.
   */
  private def populatedSet(n: Int): NavigableSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]
    assertTrue(q.isEmpty)

    var i = n - 1
    while (i >= 0) {
      mustAdd(q, i)
      i -= 2
    }
    i = n & 1
    while (i < n) {
      mustAdd(q, i)
      i += 2
    }
    mustAdd(q, -n)
    mustAdd(q, n)
    val s = q.subSet(itemFor(0), true, itemFor(n), false)
    assertFalse(s.isEmpty)
    mustEqual(n, s.size)
    s
  }

  /** Returns a new set of first 5 ints.
   */
  private def set5(): NavigableSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]
    assertTrue(q.isEmpty)
    q.add(one)
    q.add(two)
    q.add(three)
    q.add(four)
    q.add(five)
    q.add(zero)
    q.add(seven)
    val s = q.subSet(one, true, seven, false)
    mustEqual(5, s.size)
    s
  }

  /** Returns a new set of first 5 negative ints.
   */
  private def dset5(): NavigableSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]
    assertTrue(q.isEmpty)
    q.add(minusOne)
    q.add(minusTwo)
    q.add(minusThree)
    q.add(minusFour)
    q.add(minusFive)
    val s = q.descendingSet()
    mustEqual(5, s.size)
    s
  }

  private def set0(): NavigableSet[Item] = {
    val set = new ConcurrentSkipListSet[Item]
    assertTrue(set.isEmpty)
    set.tailSet(minusOne, true)
  }

  private def dset0(): NavigableSet[Item] = {
    val set = new ConcurrentSkipListSet[Item]
    assertTrue(set.isEmpty)
    set
  }
}

class ConcurrentSkipListSubSetTest extends JSR166Test {
  import ConcurrentSkipListSubSetTest._
  // Selective import avoids ambiguity with companion Item zero/one/...
  // shouldThrow is an instance method on JSR166Test (inherited).
  import JSR166Test.{
    SIZE, defaultItems, expensiveTests, fortytwo, itemFor, mustAdd, mustContain,
    mustEqual, mustNotContain, mustNotRemove, mustRemove, ninetynine
  }

  /** A new set has unbounded capacity
   */
  @Test def testConstructor1(): Unit = {
    mustEqual(0, set0().size)
  }

  /** isEmpty is true before add, false after
   */
  @Test def testEmpty(): Unit = {
    val q = set0()
    assertTrue(q.isEmpty)
    mustAdd(q, one)
    assertFalse(q.isEmpty)
    mustAdd(q, two)
    q.pollFirst()
    q.pollFirst()
    assertTrue(q.isEmpty)
  }

  /** size changes when elements added and removed
   */
  @Test def testSize(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(SIZE - i, q.size)
      q.pollFirst()
      i += 1
    }
    i = 0
    while (i < SIZE) {
      mustEqual(i, q.size)
      mustAdd(q, i)
      i += 1
    }
  }

  /** add(null) throws NPE
   */
  @Test def testAddNull(): Unit = {
    val q = set0()
    try {
      q.add(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Add of comparable element succeeds
   */
  @Test def testAdd(): Unit = {
    val q = set0()
    assertTrue(q.add(six))
  }

  /** Add of duplicate element fails
   */
  @Test def testAddDup(): Unit = {
    val q = set0()
    assertTrue(q.add(six))
    assertFalse(q.add(six))
  }

  /** Add of non-Comparable throws CCE
   */
  @Test def testAddNonComparable(): Unit = {
    val src = new ConcurrentSkipListSet[AnyRef]
    val q = src.tailSet(minusOne, true)
    try {
      q.add(new AnyRef)
      q.add(new AnyRef)
      shouldThrow()
    } catch {
      case success: ClassCastException =>
    }
  }

  /** addAll(null) throws NPE
   */
  @Test def testAddAll1(): Unit = {
    val q = set0()
    try {
      q.addAll(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** addAll of a collection with null elements throws NPE
   */
  @Test def testAddAll2(): Unit = {
    val q = set0()
    val items = new Array[Item](SIZE)
    try {
      q.addAll(Arrays.asList(items: _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** addAll of a collection with any null elements throws NPE after possibly
   *  adding some elements
   */
  @Test def testAddAll3(): Unit = {
    val q = set0()
    val items = new Array[Item](2)
    items(0) = zero
    try {
      q.addAll(Arrays.asList(items: _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Set contains all elements of successful addAll
   */
  @Test def testAddAll5(): Unit = {
    val empty = new Array[Item](0)
    val items = new Array[Item](SIZE)
    var i = 0
    while (i < SIZE) {
      items(i) = itemFor(SIZE - 1 - i)
      i += 1
    }
    val q = set0()
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
  }

  /** poll succeeds unless empty
   */
  @Test def testPoll(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
    assertNull(q.pollFirst())
  }

  /** remove(x) removes x and returns true if present
   */
  @Test def testRemoveElement(): Unit = {
    val q = populatedSet(SIZE)
    var i = 1
    while (i < SIZE) {
      mustContain(q, i)
      mustRemove(q, i)
      mustNotContain(q, i)
      mustContain(q, i - 1)
      i += 2
    }
    i = 0
    while (i < SIZE) {
      mustContain(q, i)
      mustRemove(q, i)
      mustNotContain(q, i)
      mustNotRemove(q, i + 1)
      mustNotContain(q, i + 1)
      i += 2
    }
    assertTrue(q.isEmpty)
  }

  /** contains(x) reports true when elements added but not yet removed
   */
  @Test def testContains(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustContain(q, i)
      q.pollFirst()
      mustNotContain(q, i)
      i += 1
    }
  }

  /** clear removes all elements
   */
  @Test def testClear(): Unit = {
    val q = populatedSet(SIZE)
    q.clear()
    assertTrue(q.isEmpty)
    mustEqual(0, q.size)
    mustAdd(q, one)
    assertFalse(q.isEmpty)
    q.clear()
    assertTrue(q.isEmpty)
  }

  /** containsAll(c) is true when c contains a subset of elements
   */
  @Test def testContainsAll(): Unit = {
    val q = populatedSet(SIZE)
    val p = set0()
    var i = 0
    while (i < SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)
      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c and reports true if changed
   */
  @Test def testRetainAll(): Unit = {
    val q = populatedSet(SIZE)
    val p = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      val changed = q.retainAll(p)
      if (i == 0) assertFalse(changed)
      else assertTrue(changed)

      assertTrue(q.containsAll(p))
      mustEqual(SIZE - i, q.size)
      p.pollFirst()
      i += 1
    }
  }

  /** removeAll(c) removes only those elements of c and reports true if changed
   */
  @Test def testRemoveAll(): Unit = {
    var i = 1
    while (i < SIZE) {
      val q = populatedSet(SIZE)
      val p = populatedSet(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size)
      var j = 0
      while (j < i) {
        mustNotContain(q, p.pollFirst())
        j += 1
      }
      i += 1
    }
  }

  /** lower returns preceding element
   */
  @Test def testLower(): Unit = {
    val q = set5()
    val e1 = q.lower(three)
    mustEqual(two, e1)

    val e2 = q.lower(six)
    mustEqual(five, e2)

    val e3 = q.lower(one)
    assertNull(e3)

    val e4 = q.lower(zero)
    assertNull(e4)
  }

  /** higher returns next element
   */
  @Test def testHigher(): Unit = {
    val q = set5()
    val e1 = q.higher(three)
    mustEqual(four, e1)

    val e2 = q.higher(zero)
    mustEqual(one, e2)

    val e3 = q.higher(five)
    assertNull(e3)

    val e4 = q.higher(six)
    assertNull(e4)
  }

  /** floor returns preceding element
   */
  @Test def testFloor(): Unit = {
    val q = set5()
    val e1 = q.floor(three)
    mustEqual(three, e1)

    val e2 = q.floor(six)
    mustEqual(five, e2)

    val e3 = q.floor(one)
    mustEqual(one, e3)

    val e4 = q.floor(zero)
    assertNull(e4)
  }

  /** ceiling returns next element
   */
  @Test def testCeiling(): Unit = {
    val q = set5()
    val e1 = q.ceiling(three)
    mustEqual(three, e1)

    val e2 = q.ceiling(zero)
    mustEqual(one, e2)

    val e3 = q.ceiling(five)
    mustEqual(five, e3)

    val e4 = q.ceiling(six)
    assertNull(e4)
  }

  /** toArray contains all elements in sorted order
   */
  @Test def testToArray(): Unit = {
    val q = populatedSet(SIZE)
    val a = q.toArray()
    assertSame(classOf[Array[AnyRef]], a.getClass)
    for (o <- a) {
      assertSame(o, q.pollFirst())
    }
    assertTrue(q.isEmpty)
  }

  /** toArray(a) contains all elements in sorted order
   */
  @Test def testToArray2(): Unit = {
    val q = populatedSet(SIZE)
    val items = new Array[Item](SIZE)
    val array = q.toArray(items)
    assertSame(items, array)
    for (o <- items) {
      assertSame(o, q.pollFirst())
    }
    assertTrue(q.isEmpty)
  }

  /** iterator iterates through all elements
   */
  @Test def testIterator(): Unit = {
    val q = populatedSet(SIZE)
    val it: Iterator[_ <: Item] = q.iterator()
    var i = 0
    while (it.hasNext) {
      assertTrue(q.contains(it.next()))
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  /** iterator of empty set has no elements
   */
  @Test def testEmptyIterator(): Unit = {
    assertIteratorExhausted(set0().iterator())
  }

  /** iterator.remove removes current element
   */
  @Test def testIteratorRemove(): Unit = {
    val q = set0()
    mustAdd(q, two)
    mustAdd(q, one)
    mustAdd(q, three)
    var it: Iterator[_ <: Item] = q.iterator()
    it.next()
    it.remove()

    it = q.iterator()
    mustEqual(it.next(), two)
    mustEqual(it.next(), three)
    assertFalse(it.hasNext)
  }

  /** toString contains toStrings of elements
   */
  @Test def testToString(): Unit = {
    val q = populatedSet(SIZE)
    val s = q.toString
    var i = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  /** A deserialized/reserialized set equals original
   *
   *  serialClone is unavailable on Scala Native (no ObjectInputStream).
   */
  // @Test def testSerialization(): Unit = {
  //   val x = populatedSet(SIZE)
  //   val y = serialClone(x)
  //
  //   assertNotSame(y, x)
  //   mustEqual(x.size, y.size)
  //   mustEqual(x, y)
  //   mustEqual(y, x)
  //   while (!x.isEmpty) {
  //     assertFalse(y.isEmpty)
  //     mustEqual(x.pollFirst(), y.pollFirst())
  //   }
  //   assertTrue(y.isEmpty)
  // }

  /** subSet returns set with keys in requested range
   */
  @Test def testSubSetContents(): Unit = {
    val set = set5()
    val sm: SortedSet[Item] = set.subSet(two, four)
    mustEqual(two, sm.first())
    mustEqual(three, sm.last())
    mustEqual(2, sm.size)
    mustNotContain(sm, one)
    mustContain(sm, two)
    mustContain(sm, three)
    mustNotContain(sm, four)
    mustNotContain(sm, five)
    val i: Iterator[_ <: Item] = sm.iterator()
    var k = i.next()
    mustEqual(two, k)
    k = i.next()
    mustEqual(three, k)
    assertFalse(i.hasNext)
    val j: Iterator[_ <: Item] = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, two)
    mustEqual(4, set.size)
    mustEqual(1, sm.size)
    mustEqual(three, sm.first())
    mustEqual(three, sm.last())
    mustRemove(sm, three)
    assertTrue(sm.isEmpty)
    mustEqual(3, set.size)
  }

  @Test def testSubSetContents2(): Unit = {
    val set = set5()
    val sm: SortedSet[Item] = set.subSet(two, three)
    mustEqual(1, sm.size)
    mustEqual(two, sm.first())
    mustEqual(two, sm.last())
    mustNotContain(sm, one)
    mustContain(sm, two)
    mustNotContain(sm, three)
    mustNotContain(sm, four)
    mustNotContain(sm, five)
    val i: Iterator[_ <: Item] = sm.iterator()
    val k = i.next()
    mustEqual(two, k)
    assertFalse(i.hasNext)
    val j: Iterator[_ <: Item] = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, two)
    mustEqual(4, set.size)
    mustEqual(0, sm.size)
    assertTrue(sm.isEmpty)
    assertFalse(sm.remove(three))
    mustEqual(4, set.size)
  }

  /** headSet returns set with keys in requested range
   */
  @Test def testHeadSetContents(): Unit = {
    val set = set5()
    val sm: SortedSet[Item] = set.headSet(four)
    mustContain(sm, one)
    mustContain(sm, two)
    mustContain(sm, three)
    mustNotContain(sm, four)
    mustNotContain(sm, five)
    val i: Iterator[_ <: Item] = sm.iterator()
    var k = i.next()
    mustEqual(one, k)
    k = i.next()
    mustEqual(two, k)
    k = i.next()
    mustEqual(three, k)
    assertFalse(i.hasNext)
    sm.clear()
    assertTrue(sm.isEmpty)
    mustEqual(2, set.size)
    mustEqual(four, set.first())
  }

  /** tailSet returns set with keys in requested range
   */
  @Test def testTailSetContents(): Unit = {
    val set = set5()
    val sm: SortedSet[Item] = set.tailSet(two)
    mustNotContain(sm, one)
    mustContain(sm, two)
    mustContain(sm, three)
    mustContain(sm, four)
    mustContain(sm, five)
    assertFalse(sm.contains(one))
    assertTrue(sm.contains(two))
    assertTrue(sm.contains(three))
    assertTrue(sm.contains(four))
    assertTrue(sm.contains(five))
    val i: Iterator[_ <: Item] = sm.iterator()
    var k = i.next()
    mustEqual(two, k)
    k = i.next()
    mustEqual(three, k)
    k = i.next()
    mustEqual(four, k)
    k = i.next()
    mustEqual(five, k)
    assertFalse(i.hasNext)

    val ssm: SortedSet[Item] = sm.tailSet(four)
    mustEqual(four, ssm.first())
    mustEqual(five, ssm.last())
    mustRemove(ssm, four)
    mustEqual(1, ssm.size)
    mustEqual(3, sm.size)
    mustEqual(4, set.size)
  }

  /** size changes when elements added and removed
   */
  @Test def testDescendingSize(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(SIZE - i, q.size)
      q.pollFirst()
      i += 1
    }
    i = 0
    while (i < SIZE) {
      mustEqual(i, q.size)
      mustAdd(q, i)
      i += 1
    }
  }

  /** add(null) throws NPE
   */
  @Test def testDescendingAddNull(): Unit = {
    val q = dset0()
    try {
      q.add(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Add of comparable element succeeds
   */
  @Test def testDescendingAdd(): Unit = {
    val q = dset0()
    assertTrue(q.add(minusSix))
  }

  /** Add of duplicate element fails
   */
  @Test def testDescendingAddDup(): Unit = {
    val q = dset0()
    assertTrue(q.add(minusSix))
    assertFalse(q.add(minusSix))
  }

  /** Add of non-Comparable throws CCE
   */
  @Test def testDescendingAddNonComparable(): Unit = {
    val q = new ConcurrentSkipListSet[AnyRef]
    try {
      q.add(new AnyRef)
      q.add(new AnyRef)
      shouldThrow()
    } catch {
      case success: ClassCastException =>
    }
  }

  /** addAll(null) throws NPE
   */
  @Test def testDescendingAddAll1(): Unit = {
    val q = dset0()
    try {
      q.addAll(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** addAll of a collection with null elements throws NPE
   */
  @Test def testDescendingAddAll2(): Unit = {
    val q = dset0()
    val items = new Array[Item](1)
    try {
      q.addAll(Arrays.asList(items: _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** addAll of a collection with any null elements throws NPE after possibly
   *  adding some elements
   */
  @Test def testDescendingAddAll3(): Unit = {
    val q = dset0()
    val items = new Array[Item](2)
    items(0) = zero
    try {
      q.addAll(Arrays.asList(items: _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Set contains all elements of successful addAll
   */
  @Test def testDescendingAddAll5(): Unit = {
    val empty = new Array[Item](0)
    val items = new Array[Item](SIZE)
    var i = 0
    while (i < SIZE) {
      items(i) = itemFor(SIZE - 1 - i)
      i += 1
    }
    val q = dset0()
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
  }

  /** poll succeeds unless empty
   */
  @Test def testDescendingPoll(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
    assertNull(q.pollFirst())
  }

  /** remove(x) removes x and returns true if present
   */
  @Test def testDescendingRemoveElement(): Unit = {
    val q = populatedSet(SIZE)
    var i = 1
    while (i < SIZE) {
      mustRemove(q, i)
      i += 2
    }
    i = 0
    while (i < SIZE) {
      mustRemove(q, i)
      mustNotRemove(q, i + 1)
      i += 2
    }
    assertTrue(q.isEmpty)
  }

  /** contains(x) reports true when elements added but not yet removed
   */
  @Test def testDescendingContains(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustContain(q, i)
      q.pollFirst()
      mustNotContain(q, i)
      i += 1
    }
  }

  /** clear removes all elements
   */
  @Test def testDescendingClear(): Unit = {
    val q = populatedSet(SIZE)
    q.clear()
    assertTrue(q.isEmpty)
    mustEqual(0, q.size)
    mustAdd(q, one)
    assertFalse(q.isEmpty)
    q.clear()
    assertTrue(q.isEmpty)
  }

  /** containsAll(c) is true when c contains a subset of elements
   */
  @Test def testDescendingContainsAll(): Unit = {
    val q = populatedSet(SIZE)
    val p = dset0()
    var i = 0
    while (i < SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)
      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  /** retainAll(c) retains only those elements of c and reports true if changed
   */
  @Test def testDescendingRetainAll(): Unit = {
    val q = populatedSet(SIZE)
    val p = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      val changed = q.retainAll(p)
      if (i == 0) assertFalse(changed)
      else assertTrue(changed)

      assertTrue(q.containsAll(p))
      mustEqual(SIZE - i, q.size)
      p.pollFirst()
      i += 1
    }
  }

  /** removeAll(c) removes only those elements of c and reports true if changed
   */
  @Test def testDescendingRemoveAll(): Unit = {
    var i = 1
    while (i < SIZE) {
      val q = populatedSet(SIZE)
      val p = populatedSet(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size)
      var j = 0
      while (j < i) {
        mustNotContain(q, p.pollFirst())
        j += 1
      }
      i += 1
    }
  }

  /** lower returns preceding element
   */
  @Test def testDescendingLower(): Unit = {
    val q = dset5()
    val e1 = q.lower(minusThree)
    mustEqual(minusTwo, e1)

    val e2 = q.lower(minusSix)
    mustEqual(minusFive, e2)

    val e3 = q.lower(minusOne)
    assertNull(e3)

    val e4 = q.lower(zero)
    assertNull(e4)
  }

  /** higher returns next element
   */
  @Test def testDescendingHigher(): Unit = {
    val q = dset5()
    val e1 = q.higher(minusThree)
    mustEqual(minusFour, e1)

    val e2 = q.higher(zero)
    mustEqual(minusOne, e2)

    val e3 = q.higher(minusFive)
    assertNull(e3)

    val e4 = q.higher(minusSix)
    assertNull(e4)
  }

  /** floor returns preceding element
   */
  @Test def testDescendingFloor(): Unit = {
    val q = dset5()
    val e1 = q.floor(minusThree)
    mustEqual(minusThree, e1)

    val e2 = q.floor(minusSix)
    mustEqual(minusFive, e2)

    val e3 = q.floor(minusOne)
    mustEqual(minusOne, e3)

    val e4 = q.floor(zero)
    assertNull(e4)
  }

  /** ceiling returns next element
   */
  @Test def testDescendingCeiling(): Unit = {
    val q = dset5()
    val e1 = q.ceiling(minusThree)
    mustEqual(minusThree, e1)

    val e2 = q.ceiling(zero)
    mustEqual(minusOne, e2)

    val e3 = q.ceiling(minusFive)
    mustEqual(minusFive, e3)

    val e4 = q.ceiling(minusSix)
    assertNull(e4)
  }

  /** toArray contains all elements
   */
  @Test def testDescendingToArray(): Unit = {
    val q = populatedSet(SIZE)
    val o = q.toArray()
    Arrays.sort(o)
    var i = 0
    while (i < o.length) {
      mustEqual(o(i), q.pollFirst())
      i += 1
    }
  }

  /** toArray(a) contains all elements
   */
  @Test def testDescendingToArray2(): Unit = {
    val q = populatedSet(SIZE)
    val items = new Array[Item](SIZE)
    assertSame(items, q.toArray(items))
    Arrays.sort(items.asInstanceOf[Array[AnyRef]])
    var i = 0
    while (i < items.length) {
      mustEqual(items(i), q.pollFirst())
      i += 1
    }
  }

  /** iterator iterates through all elements
   */
  @Test def testDescendingIterator(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    val it: Iterator[_ <: Item] = q.iterator()
    while (it.hasNext) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
  }

  /** iterator of empty set has no elements
   */
  @Test def testDescendingEmptyIterator(): Unit = {
    val q = dset0()
    var i = 0
    val it: Iterator[_ <: Item] = q.iterator()
    while (it.hasNext) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(0, i)
  }

  /** iterator.remove removes current element
   */
  @Test def testDescendingIteratorRemove(): Unit = {
    val q = dset0()
    q.add(two)
    q.add(one)
    q.add(three)

    var it: Iterator[_ <: Item] = q.iterator()
    it.next()
    it.remove()

    it = q.iterator()
    mustEqual(it.next(), two)
    mustEqual(it.next(), three)
    assertFalse(it.hasNext)
  }

  /** toString contains toStrings of elements
   */
  @Test def testDescendingToString(): Unit = {
    val q = populatedSet(SIZE)
    val s = q.toString
    var i = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  /** A deserialized/reserialized set equals original
   *
   *  serialClone is unavailable on Scala Native (no ObjectInputStream).
   */
  // @Test def testDescendingSerialization(): Unit = {
  //   val x = dset5()
  //   val y = serialClone(x)
  //
  //   assertNotSame(y, x)
  //   mustEqual(x.size, y.size)
  //   mustEqual(x, y)
  //   mustEqual(y, x)
  //   while (!x.isEmpty) {
  //     assertFalse(y.isEmpty)
  //     mustEqual(x.pollFirst(), y.pollFirst())
  //   }
  //   assertTrue(y.isEmpty)
  // }

  /** subSet returns set with keys in requested range
   */
  @Test def testDescendingSubSetContents(): Unit = {
    val set = dset5()
    val sm: SortedSet[Item] = set.subSet(minusTwo, minusFour)
    mustEqual(minusTwo, sm.first())
    mustEqual(minusThree, sm.last())
    mustEqual(2, sm.size)
    mustNotContain(sm, minusOne)
    mustContain(sm, minusTwo)
    mustContain(sm, minusThree)
    mustNotContain(sm, minusFour)
    mustNotContain(sm, minusFive)
    val i: Iterator[_ <: Item] = sm.iterator()
    var k = i.next()
    mustEqual(minusTwo, k)
    k = i.next()
    mustEqual(minusThree, k)
    assertFalse(i.hasNext)
    val j: Iterator[_ <: Item] = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, minusTwo)
    mustEqual(4, set.size)
    mustEqual(1, sm.size)
    mustEqual(minusThree, sm.first())
    mustEqual(minusThree, sm.last())
    mustRemove(sm, minusThree)
    assertTrue(sm.isEmpty)
    mustEqual(3, set.size)
  }

  @Test def testDescendingSubSetContents2(): Unit = {
    val set = dset5()
    val sm: SortedSet[Item] = set.subSet(minusTwo, minusThree)
    mustEqual(1, sm.size)
    mustEqual(minusTwo, sm.first())
    mustEqual(minusTwo, sm.last())
    mustNotContain(sm, minusOne)
    mustContain(sm, minusTwo)
    mustNotContain(sm, minusThree)
    mustNotContain(sm, minusFour)
    mustNotContain(sm, minusFive)
    val i: Iterator[_ <: Item] = sm.iterator()
    val k = i.next()
    mustEqual(minusTwo, k)
    assertFalse(i.hasNext)
    val j: Iterator[_ <: Item] = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, minusTwo)
    mustEqual(4, set.size)
    mustEqual(0, sm.size)
    assertTrue(sm.isEmpty)
    mustNotRemove(sm, minusThree)
    mustEqual(4, set.size)
  }

  /** headSet returns set with keys in requested range
   */
  @Test def testDescendingHeadSetContents(): Unit = {
    val set = dset5()
    val sm: SortedSet[Item] = set.headSet(minusFour)
    mustContain(sm, minusOne)
    mustContain(sm, minusTwo)
    mustContain(sm, minusThree)
    mustNotContain(sm, minusFour)
    mustNotContain(sm, minusFive)
    val i: Iterator[_ <: Item] = sm.iterator()
    var k = i.next()
    mustEqual(minusOne, k)
    k = i.next()
    mustEqual(minusTwo, k)
    k = i.next()
    mustEqual(minusThree, k)
    assertFalse(i.hasNext)
    sm.clear()
    assertTrue(sm.isEmpty)
    mustEqual(2, set.size)
    mustEqual(minusFour, set.first())
  }

  /** tailSet returns set with keys in requested range
   */
  @Test def testDescendingTailSetContents(): Unit = {
    val set = dset5()
    val sm: SortedSet[Item] = set.tailSet(minusTwo)
    mustNotContain(sm, minusOne)
    mustContain(sm, minusTwo)
    mustContain(sm, minusThree)
    mustContain(sm, minusFour)
    mustContain(sm, minusFive)
    val i: Iterator[_ <: Item] = sm.iterator()
    var k = i.next()
    mustEqual(minusTwo, k)
    k = i.next()
    mustEqual(minusThree, k)
    k = i.next()
    mustEqual(minusFour, k)
    k = i.next()
    mustEqual(minusFive, k)
    assertFalse(i.hasNext)

    val ssm: SortedSet[Item] = sm.tailSet(minusFour)
    mustEqual(minusFour, ssm.first())
    mustEqual(minusFive, ssm.last())
    mustRemove(ssm, minusFour)
    mustEqual(1, ssm.size)
    mustEqual(3, sm.size)
    mustEqual(4, set.size)
  }
}
