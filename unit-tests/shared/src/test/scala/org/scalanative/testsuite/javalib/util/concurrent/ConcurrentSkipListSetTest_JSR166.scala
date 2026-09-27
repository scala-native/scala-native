/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ConcurrentSkipListSet
import java.util.{
  Arrays, BitSet, Collection, Comparator, Iterator, NavigableSet,
  NoSuchElementException, Random, Set, SortedSet
}

import org.junit.Assert._
import org.junit._

object ConcurrentSkipListSetTest_JSR166 extends ConcurrentSkipListItems {
  import JSR166Test.{mustAdd, mustEqual}

  class MyReverseComparator extends Comparator[AnyRef] {
    def compare(x: AnyRef, y: AnyRef): Int =
      y.asInstanceOf[Comparable[AnyRef]].compareTo(x)
  }

  /** Returns a new set of given size containing consecutive Items 0 ... n - 1.
   */
  private def populatedSet(n: Int): ConcurrentSkipListSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]()
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
    assertFalse(q.isEmpty)
    mustEqual(n, q.size)
    q
  }

  /** Returns a new set of first 5 ints.
   */
  private def set5(): ConcurrentSkipListSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.isEmpty)
    q.add(one)
    q.add(two)
    q.add(three)
    q.add(four)
    q.add(five)
    mustEqual(5, q.size)
    q
  }

  private def newSet(): NavigableSet[Item] = {
    val result = new ConcurrentSkipListSet[Item]()
    mustEqual(0, result.size)
    assertFalse(result.iterator().hasNext)
    result
  }

  private def assertEq(i: Item, j: Int): Unit = {
    if (i == null) mustEqual(j, -1)
    else mustEqual(i, j)
  }
}

class ConcurrentSkipListSetTest_JSR166 extends JSR166Test {
  import ConcurrentSkipListSetTest_JSR166._
  // Selective import avoids ambiguity with companion Item zero/one/...
  // shouldThrow / assertIteratorExhausted are instance methods (inherited).
  import JSR166Test.{
    SIZE, defaultItems, expensiveTests, fortytwo, itemFor, mustAdd, mustContain,
    mustEqual, mustNotContain, mustNotRemove, mustRemove, ninetynine
  }

  /** A new set has unbounded capacity
   */
  @Test def testConstructor1(): Unit = {
    mustEqual(0, new ConcurrentSkipListSet[Item]().size)
  }

  /** Initializing from null Collection throws NPE
   */
  @Test def testConstructor3(): Unit = {
    try {
      new ConcurrentSkipListSet[Item](null.asInstanceOf[Collection[Item]])
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Initializing from Collection of null elements throws NPE
   */
  @Test def testConstructor4(): Unit = {
    try {
      new ConcurrentSkipListSet[Item](Arrays.asList(new Array[Item](SIZE): _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Initializing from Collection with some null elements throws NPE
   */
  @Test def testConstructor5(): Unit = {
    val items = new Array[Item](2)
    items(0) = zero
    try {
      new ConcurrentSkipListSet[Item](Arrays.asList(items: _*))
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** Set contains all elements of collection used to initialize
   */
  @Test def testConstructor6(): Unit = {
    val items = defaultItems
    val q = new ConcurrentSkipListSet[Item](Arrays.asList(items: _*))
    var i = 0
    while (i < SIZE) {
      mustEqual(items(i), q.pollFirst())
      i += 1
    }
  }

  /** The comparator used in constructor is used
   */
  @Test def testConstructor7(): Unit = {
    val cmp = new MyReverseComparator
    val q = new ConcurrentSkipListSet[Item](
      cmp.asInstanceOf[Comparator[_ >: Item]]
    )
    mustEqual(cmp, q.comparator())
    val items = defaultItems
    q.addAll(Arrays.asList(items: _*))
    var i = SIZE - 1
    while (i >= 0) {
      mustEqual(items(i), q.pollFirst())
      i -= 1
    }
  }

  /** isEmpty is true before add, false after
   */
  @Test def testEmpty(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
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
    val q = new ConcurrentSkipListSet[Item]()
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
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.add(zero))
    assertTrue(q.add(one))
  }

  /** Add of duplicate element fails
   */
  @Test def testAddDup(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.add(zero))
    assertFalse(q.add(zero))
  }

  /** Add of non-Comparable throws CCE
   */
  @Test def testAddNonComparable(): Unit = {
    val q = new ConcurrentSkipListSet[AnyRef]()
    try {
      q.add(new AnyRef)
      q.add(new AnyRef)
      shouldThrow()
    } catch {
      case success: ClassCastException =>
        assertTrue(q.size < 2)
        var i = 0
        val size = q.size
        while (i < size) {
          assertSame(classOf[AnyRef], q.pollFirst().getClass)
          i += 1
        }
        assertNull(q.pollFirst())
        assertTrue(q.isEmpty)
        mustEqual(0, q.size)
    }
  }

  /** addAll(null) throws NPE
   */
  @Test def testAddAll1(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
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
    val q = new ConcurrentSkipListSet[Item]()
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
    val q = new ConcurrentSkipListSet[Item]()
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
    val items = defaultItems
    val q = new ConcurrentSkipListSet[Item]()
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
  }

  /** pollFirst succeeds unless empty
   */
  @Test def testPollFirst(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
    assertNull(q.pollFirst())
  }

  /** pollLast succeeds unless empty
   */
  @Test def testPollLast(): Unit = {
    val q = populatedSet(SIZE)
    var i = SIZE - 1
    while (i >= 0) {
      mustEqual(i, q.pollLast())
      i -= 1
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
    val p = new ConcurrentSkipListSet[Item]()
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
    var i = 0
    while (i < a.length) {
      assertSame(a(i), q.pollFirst())
      i += 1
    }
    assertTrue(q.isEmpty)
  }

  /** toArray(a) contains all elements in sorted order
   */
  @Test def testToArray2(): Unit = {
    val q = populatedSet(SIZE)
    val items = new Array[Item](SIZE)
    assertSame(items, q.toArray(items))
    var i = 0
    while (i < items.length) {
      assertSame(items(i), q.pollFirst())
      i += 1
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
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  /** iterator of empty set has no elements
   */
  @Test def testEmptyIterator(): Unit = {
    val s: NavigableSet[Item] = new ConcurrentSkipListSet[Item]()
    assertIteratorExhausted(s.iterator())
    assertIteratorExhausted(s.descendingSet().iterator())
  }

  /** iterator.remove removes current element
   */
  @Test def testIteratorRemove(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
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
  @Test def testToString(): Unit = {
    val q = populatedSet(SIZE)
    val s = q.toString
    var i = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  /** A cloned set equals original
   */
  @Test def testClone(): Unit = {
    val x = populatedSet(SIZE)
    val y = x.clone()

    assertNotSame(x, y)
    mustEqual(x.size, y.size)
    mustEqual(x, y)
    mustEqual(y, x)
    while (!x.isEmpty) {
      assertFalse(y.isEmpty)
      mustEqual(x.pollFirst(), y.pollFirst())
    }
    assertTrue(y.isEmpty)
  }

  /** A deserialized/reserialized set equals original
   *
   *  serialClone is unavailable on Scala Native (no ObjectInputStream).
   */
  // @Test def testSerialization(): Unit = {
  //   val x: NavigableSet[Item] = populatedSet(SIZE)
  //   val y = serialClone(x)
  //
  //   assertNotSame(x, y)
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
    mustNotRemove(sm, three)
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
    mustContain(sm, two)
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

  var rnd = new Random(666)

  /** Subsets of subsets subdivide correctly
   */
  @Test def testRecursiveSubSets(): Unit = {
    val setSize = if (expensiveTests) 1000 else 100
    val set = newSet()
    val bs = new BitSet(setSize)

    populate(set, setSize, bs)
    check(set, 0, setSize - 1, true, bs)
    check(set.descendingSet(), 0, setSize - 1, false, bs)

    mutateSet(set, 0, setSize - 1, bs)
    check(set, 0, setSize - 1, true, bs)
    check(set.descendingSet(), 0, setSize - 1, false, bs)

    bashSubSet(
      set.subSet(zero, true, itemFor(setSize), false),
      0,
      setSize - 1,
      true,
      bs
    )
  }

  /** addAll is idempotent
   */
  @Test def testAddAll_idempotent(): Unit = {
    val x: Set[Item] = populatedSet(SIZE)
    val y: Set[Item] = new ConcurrentSkipListSet[Item](x)
    y.addAll(x)
    mustEqual(x, y)
    mustEqual(y, x)
  }

  def populate(set: NavigableSet[Item], limit: Int, bs: BitSet): Unit = {
    var i = 0
    val n = 2 * limit / 3
    while (i < n) {
      val element = rnd.nextInt(limit)
      put(set, element, bs)
      i += 1
    }
  }

  def mutateSet(
      set: NavigableSet[Item],
      min: Int,
      max: Int,
      bs: BitSet
  ): Unit = {
    val size = set.size
    val rangeSize = max - min + 1

    // Remove a bunch of entries directly
    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(set, min - 5 + rnd.nextInt(rangeSize + 10), bs)
      i += 1
    }

    // Remove a bunch of entries with iterator
    val it = set.iterator()
    while (it.hasNext) {
      if (rnd.nextBoolean) {
        bs.clear(it.next().value)
        it.remove()
      }
    }

    // Add entries till we're back to original size
    while (set.size < size) {
      val element = min + rnd.nextInt(rangeSize)
      assertTrue(element >= min && element <= max)
      put(set, element, bs)
    }
  }

  def mutateSubSet(
      set: NavigableSet[Item],
      min: Int,
      max: Int,
      bs: BitSet
  ): Unit = {
    val size = set.size
    val rangeSize = max - min + 1

    // Remove a bunch of entries directly
    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(set, min - 5 + rnd.nextInt(rangeSize + 10), bs)
      i += 1
    }

    // Remove a bunch of entries with iterator
    val it = set.iterator()
    while (it.hasNext) {
      if (rnd.nextBoolean) {
        bs.clear(it.next().value)
        it.remove()
      }
    }

    // Add entries till we're back to original size
    while (set.size < size) {
      val element = min - 5 + rnd.nextInt(rangeSize + 10)
      if (element >= min && element <= max) {
        put(set, element, bs)
      } else {
        try {
          set.add(itemFor(element))
          shouldThrow()
        } catch {
          case success: IllegalArgumentException =>
        }
      }
    }
  }

  def put(set: NavigableSet[Item], element: Int, bs: BitSet): Unit = {
    if (set.add(itemFor(element)))
      bs.set(element)
  }

  def remove(set: NavigableSet[Item], element: Int, bs: BitSet): Unit = {
    if (set.remove(itemFor(element)))
      bs.clear(element)
  }

  def bashSubSet(
      set: NavigableSet[Item],
      min: Int,
      max: Int,
      ascending: Boolean,
      bs: BitSet
  ): Unit = {
    check(set, min, max, ascending, bs)
    check(set.descendingSet(), min, max, !ascending, bs)

    mutateSubSet(set, min, max, bs)
    check(set, min, max, ascending, bs)
    check(set.descendingSet(), min, max, !ascending, bs)

    // Recurse
    if (max - min < 2) return
    val midPoint = (min + max) / 2

    // headSet - pick direction and endpoint inclusion randomly
    var incl = rnd.nextBoolean()
    val hm = set.headSet(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean)
        bashSubSet(hm, min, midPoint - (if (incl) 0 else 1), true, bs)
      else
        bashSubSet(
          hm.descendingSet(),
          min,
          midPoint - (if (incl) 0 else 1),
          false,
          bs
        )
    } else {
      if (rnd.nextBoolean)
        bashSubSet(hm, midPoint + (if (incl) 0 else 1), max, false, bs)
      else
        bashSubSet(
          hm.descendingSet(),
          midPoint + (if (incl) 0 else 1),
          max,
          true,
          bs
        )
    }

    // tailSet - pick direction and endpoint inclusion randomly
    incl = rnd.nextBoolean()
    val tm = set.tailSet(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean)
        bashSubSet(tm, midPoint + (if (incl) 0 else 1), max, true, bs)
      else
        bashSubSet(
          tm.descendingSet(),
          midPoint + (if (incl) 0 else 1),
          max,
          false,
          bs
        )
    } else {
      if (rnd.nextBoolean)
        bashSubSet(tm, min, midPoint - (if (incl) 0 else 1), false, bs)
      else
        bashSubSet(
          tm.descendingSet(),
          min,
          midPoint - (if (incl) 0 else 1),
          true,
          bs
        )
    }

    // subSet - pick direction and endpoint inclusion randomly
    val rangeSize = max - min + 1
    val endpoints = new Array[Int](2)
    endpoints(0) = min + rnd.nextInt(rangeSize)
    endpoints(1) = min + rnd.nextInt(rangeSize)
    Arrays.sort(endpoints)
    val lowIncl = rnd.nextBoolean()
    val highIncl = rnd.nextBoolean()
    if (ascending) {
      val sm = set.subSet(
        itemFor(endpoints(0)),
        lowIncl,
        itemFor(endpoints(1)),
        highIncl
      )
      if (rnd.nextBoolean)
        bashSubSet(
          sm,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          true,
          bs
        )
      else
        bashSubSet(
          sm.descendingSet(),
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          false,
          bs
        )
    } else {
      val sm = set.subSet(
        itemFor(endpoints(1)),
        highIncl,
        itemFor(endpoints(0)),
        lowIncl
      )
      if (rnd.nextBoolean)
        bashSubSet(
          sm,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          false,
          bs
        )
      else
        bashSubSet(
          sm.descendingSet(),
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          true,
          bs
        )
    }
  }

  /** min and max are both inclusive. If max < min, interval is empty.
   */
  def check(
      set: NavigableSet[Item],
      min: Int,
      max: Int,
      ascending: Boolean,
      bs: BitSet
  ): Unit = {
    class ReferenceSet {
      def lower(element: Int): Int =
        if (ascending) lowerAscending(element) else higherAscending(element)
      def floor(element: Int): Int =
        if (ascending) floorAscending(element) else ceilingAscending(element)
      def ceiling(element: Int): Int =
        if (ascending) ceilingAscending(element) else floorAscending(element)
      def higher(element: Int): Int =
        if (ascending) higherAscending(element) else lowerAscending(element)
      def first(): Int =
        if (ascending) firstAscending() else lastAscending()
      def last(): Int =
        if (ascending) lastAscending() else firstAscending()
      def lowerAscending(element: Int): Int =
        floorAscending(element - 1)
      def floorAscending(element0: Int): Int = {
        var element = element0
        if (element < min) return -1
        else if (element > max) element = max

        // BitSet should support this! Test would run much faster
        while (element >= min) {
          if (bs.get(element)) return element
          element -= 1
        }
        -1
      }
      def ceilingAscending(element0: Int): Int = {
        var element = element0
        if (element < min) element = min
        else if (element > max) return -1
        val result = bs.nextSetBit(element)
        if (result > max) -1 else result
      }
      def higherAscending(element: Int): Int =
        ceilingAscending(element + 1)
      private def firstAscending(): Int = {
        val result = ceilingAscending(min)
        if (result > max) -1 else result
      }
      private def lastAscending(): Int = {
        val result = floorAscending(max)
        if (result < min) -1 else result
      }
    }
    val rs = new ReferenceSet

    // Test contents using containsElement
    var size = 0
    var i = min
    while (i <= max) {
      val bsContainsI = bs.get(i)
      mustEqual(bsContainsI, set.contains(itemFor(i)))
      if (bsContainsI) size += 1
      i += 1
    }
    mustEqual(size, set.size)

    // Test contents using contains elementSet iterator
    var size2 = 0
    var previousElement = -1
    val sit = set.iterator()
    while (sit.hasNext) {
      val element = sit.next()
      assertTrue(bs.get(element.value))
      size2 += 1
      assertTrue(
        previousElement < 0 ||
        (if (ascending) element.value - previousElement > 0
         else element.value - previousElement < 0)
      )
      previousElement = element.value
    }
    mustEqual(size2, size)

    // Test navigation ops
    var element = min - 1
    while (element <= max + 1) {
      val e = itemFor(element)
      assertEq(set.lower(e), rs.lower(element))
      assertEq(set.floor(e), rs.floor(element))
      assertEq(set.higher(e), rs.higher(element))
      assertEq(set.ceiling(e), rs.ceiling(element))
      element += 1
    }

    // Test extrema
    if (set.size != 0) {
      assertEq(set.first(), rs.first())
      assertEq(set.last(), rs.last())
    } else {
      mustEqual(rs.first(), -1)
      mustEqual(rs.last(), -1)
      try {
        set.first()
        shouldThrow()
      } catch {
        case success: NoSuchElementException =>
      }
      try {
        set.last()
        shouldThrow()
      } catch {
        case success: NoSuchElementException =>
      }
    }
  }
}
