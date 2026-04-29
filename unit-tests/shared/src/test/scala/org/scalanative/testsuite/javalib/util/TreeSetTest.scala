// Ported from Scala.js, revision cbf86bb, dated 24 Oct 2020
/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import scala.reflect.ClassTag

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform._

import ju.{Comparator, TreeSet}

class TreeSetComparatorTest {

  @Test def naturalComparator_issue4796(): Unit = {
    val cmp = ju.Comparator.naturalOrder[String]()

    assertSame(cmp, new TreeSet[String](cmp).comparator())
  }

}

class TreeSetWithoutNullTest extends TreeSetTest(new TreeSetFactory) {

  @Test def comparatorNull(): Unit = {
    val ts1 = factory.empty[Int]

    assertNull(ts1.comparator())

    val ts2 = factory.empty[String]

    assertNull(ts2.comparator())
  }
}

class TreeSetWithNullTest extends TreeSetTest(new TreeSetWithNullFactory) {
  @Test def comparatorNotNull(): Unit = {
    val ts1 = factory.empty[Int]

    assertFalse(ts1.comparator() == null)

    val ts2 = factory.empty[String]

    assertFalse(ts2.comparator() == null)
  }
}

abstract class TreeSetTest(val factory: TreeSetFactory)
    extends AbstractSetTest
    with SortedSetTest
    with NavigableSetTest {
  import JSR166Test._

  private final class MyReverseComparator extends Comparator[Item] {
    override def compare(x: Item, y: Item): Int =
      y.compareTo(x)
  }

  private val zeroItem = itemFor(0)
  private val oneItem = itemFor(1)
  private val twoItem = itemFor(2)
  private val threeItem = itemFor(3)
  private val fourItem = itemFor(4)
  private val fiveItem = itemFor(5)
  private val sixItem = itemFor(6)

  private val rnd = new ju.Random(666L)
  private var bs: ju.BitSet = _

  private def populatedSet(n: Int): TreeSet[Item] = {
    val q = new TreeSet[Item]()
    assertTrue(q.isEmpty())
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
    assertFalse(q.isEmpty())
    mustEqual(n, q.size())
    q
  }

  private def set5(): TreeSet[Item] = {
    val q = new TreeSet[Item]()
    assertTrue(q.isEmpty())
    q.add(oneItem)
    q.add(twoItem)
    q.add(threeItem)
    q.add(fourItem)
    q.add(fiveItem)
    mustEqual(5, q.size())
    q
  }

  private def assertIteratorExhausted(it: ju.Iterator[_]): Unit = {
    assertFalse(it.hasNext())
    assertThrows(classOf[NoSuchElementException], it.next())
  }

  @Test def testConstructor1(): Unit =
    mustEqual(0, new TreeSet[Item]().size())

  @Test def testConstructor3(): Unit =
    assertThrows(
      classOf[NullPointerException],
      new TreeSet[Item](null.asInstanceOf[ju.Collection[Item]])
    )

  @Test def testConstructor4(): Unit =
    assertThrows(
      classOf[NullPointerException],
      new TreeSet[Item](ju.Arrays.asList(new Array[Item](SIZE): _*))
    )

  @Test def testConstructor5(): Unit = {
    val items = new Array[Item](2)
    items(1) = zeroItem
    assertThrows(
      classOf[NullPointerException],
      new TreeSet[Item](ju.Arrays.asList(items: _*))
    )
  }

  @Test def testConstructor6(): Unit = {
    val items = defaultItems
    val q = new TreeSet[Item](ju.Arrays.asList(items: _*))
    var i = 0
    while (i < SIZE) {
      mustEqual(items(i), q.pollFirst())
      i += 1
    }
  }

  @Test def testConstructor7(): Unit = {
    val cmp = new MyReverseComparator
    val q = new TreeSet[Item](cmp)
    mustEqual(cmp, q.comparator())
    val items = defaultItems
    q.addAll(ju.Arrays.asList(items: _*))
    var i = SIZE - 1
    while (i >= 0) {
      mustEqual(items(i), q.pollFirst())
      i -= 1
    }
  }

  @Test def testEmpty(): Unit = {
    val q = new TreeSet[Item]()
    assertTrue(q.isEmpty())
    q.add(oneItem)
    assertFalse(q.isEmpty())
    q.add(twoItem)
    q.pollFirst()
    q.pollFirst()
    assertTrue(q.isEmpty())
  }

  @Test def testSize(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(SIZE - i, q.size())
      q.pollFirst()
      i += 1
    }
    i = 0
    while (i < SIZE) {
      mustEqual(i, q.size())
      mustAdd(q, i)
      i += 1
    }
  }

  @Test def testAddNull(): Unit = {
    val q = populatedSet(SIZE)
    assertThrows(classOf[NullPointerException], q.add(null))
  }

  @Test def testAdd(): Unit = {
    val q = new TreeSet[Item]()
    assertTrue(q.add(zeroItem))
    assertTrue(q.add(oneItem))
  }

  @Test def testAddDup(): Unit = {
    val q = new TreeSet[Item]()
    assertTrue(q.add(zeroItem))
    assertFalse(q.add(zeroItem))
  }

  @Test def testAddNonComparable(): Unit = {
    val q = new TreeSet[AnyRef]()
    assertThrows(classOf[ClassCastException], q.add(new Object()))
  }

  @Test def testAddAll1(): Unit = {
    val q = new TreeSet[Item]()
    assertThrows(classOf[NullPointerException], q.addAll(null))
  }

  @Test def testAddAll2(): Unit = {
    val q = new TreeSet[Item]()
    val items = new Array[Item](2)
    assertThrows(
      classOf[NullPointerException],
      q.addAll(ju.Arrays.asList(items: _*))
    )
  }

  @Test def testAddAll3(): Unit = {
    val q = new TreeSet[Item]()
    val items = new Array[Item](2)
    items(0) = zeroItem
    assertThrows(
      classOf[NullPointerException],
      q.addAll(ju.Arrays.asList(items: _*))
    )
  }

  @Test def testAddAll5(): Unit = {
    val empty = new Array[Item](0)
    val items = defaultItems
    val q = new TreeSet[Item]()
    assertFalse(q.addAll(ju.Arrays.asList(empty: _*)))
    assertTrue(q.addAll(ju.Arrays.asList(items: _*)))
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
  }

  @Test def testPollFirst(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
    assertNull(q.pollFirst())
  }

  @Test def testPollLast(): Unit = {
    val q = populatedSet(SIZE)
    var i = SIZE - 1
    while (i >= 0) {
      mustEqual(i, q.pollLast())
      i -= 1
    }
    assertNull(q.pollFirst())
  }

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
    assertTrue(q.isEmpty())
  }

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

  @Test def testClear(): Unit = {
    val q = populatedSet(SIZE)
    q.clear()
    assertTrue(q.isEmpty())
    mustEqual(0, q.size())
    q.add(oneItem)
    assertFalse(q.isEmpty())
    q.clear()
    assertTrue(q.isEmpty())
  }

  @Test def testContainsAll(): Unit = {
    val q = populatedSet(SIZE)
    val p = new TreeSet[Item]()
    var i = 0
    while (i < SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)
      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  @Test def testRetainAll(): Unit = {
    val q = populatedSet(SIZE)
    val p = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      val changed = q.retainAll(p)
      if (i == 0) assertFalse(changed)
      else assertTrue(changed)
      assertTrue(q.containsAll(p))
      mustEqual(SIZE - i, q.size())
      p.pollFirst()
      i += 1
    }
  }

  @Test def testRemoveAll(): Unit = {
    var i = 1
    while (i < SIZE) {
      val q = populatedSet(SIZE)
      val p = populatedSet(i)
      assertTrue(q.removeAll(p))
      mustEqual(SIZE - i, q.size())
      var j = 0
      while (j < i) {
        mustNotContain(q, p.pollFirst())
        j += 1
      }
      i += 1
    }
  }

  @Test def testLower(): Unit = {
    val q = set5()
    mustEqual(twoItem, q.lower(threeItem))
    mustEqual(fiveItem, q.lower(sixItem))
    assertNull(q.lower(oneItem))
    assertNull(q.lower(zeroItem))
  }

  @Test def testHigher(): Unit = {
    val q = set5()
    mustEqual(fourItem, q.higher(threeItem))
    mustEqual(oneItem, q.higher(zeroItem))
    assertNull(q.higher(fiveItem))
    assertNull(q.higher(sixItem))
  }

  @Test def testFloor(): Unit = {
    val q = set5()
    mustEqual(threeItem, q.floor(threeItem))
    mustEqual(fiveItem, q.floor(sixItem))
    mustEqual(oneItem, q.floor(oneItem))
    assertNull(q.floor(zeroItem))
  }

  @Test def testCeiling(): Unit = {
    val q = set5()
    mustEqual(threeItem, q.ceiling(threeItem))
    mustEqual(oneItem, q.ceiling(zeroItem))
    mustEqual(fiveItem, q.ceiling(fiveItem))
    assertNull(q.ceiling(sixItem))
  }

  @Test def testToArray(): Unit = {
    val q = populatedSet(SIZE)
    val a = q.toArray()
    assertSame(classOf[Array[Object]], a.getClass())
    for (o <- a)
      assertSame(o, q.pollFirst())
    assertTrue(q.isEmpty())
  }

  @Test def testToArray2(): Unit = {
    val q = populatedSet(SIZE)
    val items = new Array[Item](SIZE)
    val array = q.toArray(items)
    assertSame(items, array)
    for (o <- items)
      assertSame(o, q.pollFirst())
    assertTrue(q.isEmpty())
  }

  @Test def testIterator(): Unit = {
    val q = populatedSet(SIZE)
    val it = q.iterator()
    var i = 0
    while (it.hasNext()) {
      assertTrue(q.contains(it.next()))
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  @Test def testEmptyIterator(): Unit =
    assertIteratorExhausted(new TreeSet[Item]().iterator())

  @Test def testIteratorRemove(): Unit = {
    val q = new TreeSet[Item]()
    q.add(twoItem)
    q.add(oneItem)
    q.add(threeItem)
    var it = q.iterator()
    it.next()
    it.remove()
    it = q.iterator()
    mustEqual(it.next(), twoItem)
    mustEqual(it.next(), threeItem)
    assertFalse(it.hasNext())
  }

  @Test def testToString(): Unit = {
    val s = populatedSet(SIZE).toString()
    var i = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  @Ignore(
    "scala-native#4852: ObjectInputStream/ObjectOutputStream are unsupported"
  )
  @Test def testSerialization(): Unit = ()

  @Test def testSubSetContents(): Unit = {
    val set = set5()
    val sm = set.subSet(twoItem, fourItem)
    mustEqual(twoItem, sm.first())
    mustEqual(threeItem, sm.last())
    mustEqual(2, sm.size())
    mustNotContain(sm, oneItem)
    mustContain(sm, twoItem)
    mustContain(sm, threeItem)
    mustNotContain(sm, fourItem)
    mustNotContain(sm, fiveItem)
    checkSetOrder(sm.iterator(), Array(twoItem, threeItem))
    val j = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, twoItem)
    mustEqual(4, set.size())
    mustEqual(1, sm.size())
    mustEqual(threeItem, sm.first())
    mustEqual(threeItem, sm.last())
    mustRemove(sm, threeItem)
    assertTrue(sm.isEmpty())
    mustEqual(3, set.size())
  }

  @Test def testSubSetContents2(): Unit = {
    val set = set5()
    val sm = set.subSet(twoItem, threeItem)
    mustEqual(1, sm.size())
    mustEqual(twoItem, sm.first())
    mustEqual(twoItem, sm.last())
    mustNotContain(sm, oneItem)
    mustContain(sm, twoItem)
    mustNotContain(sm, threeItem)
    mustNotContain(sm, fourItem)
    mustNotContain(sm, fiveItem)
    checkSetOrder(sm.iterator(), Array(twoItem))
    val j = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, twoItem)
    mustEqual(4, set.size())
    mustEqual(0, sm.size())
    assertTrue(sm.isEmpty())
    mustNotRemove(sm, threeItem)
    mustEqual(4, set.size())
  }

  @Test def testHeadSetContents(): Unit = {
    val set = set5()
    val sm = set.headSet(fourItem)
    mustContain(sm, oneItem)
    mustContain(sm, twoItem)
    mustContain(sm, threeItem)
    mustNotContain(sm, fourItem)
    mustNotContain(sm, fiveItem)
    checkSetOrder(sm.iterator(), Array(oneItem, twoItem, threeItem))
    sm.clear()
    assertTrue(sm.isEmpty())
    mustEqual(2, set.size())
    mustEqual(fourItem, set.first())
  }

  @Test def testTailSetContents(): Unit = {
    val set = set5()
    val sm = set.tailSet(twoItem)
    mustNotContain(sm, oneItem)
    mustContain(sm, twoItem)
    mustContain(sm, threeItem)
    mustContain(sm, fourItem)
    mustContain(sm, fiveItem)
    checkSetOrder(sm.iterator(), Array(twoItem, threeItem, fourItem, fiveItem))
    val ssm = sm.tailSet(fourItem)
    mustEqual(fourItem, ssm.first())
    mustEqual(fiveItem, ssm.last())
    mustRemove(ssm, fourItem)
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, set.size())
  }

  private def checkSetOrder(
      it: ju.Iterator[_ <: Item],
      expected: Array[Item]
  ): Unit = {
    var i = 0
    while (i < expected.length) {
      mustEqual(expected(i), it.next())
      i += 1
    }
    assertFalse(it.hasNext())
  }

  @Test def testRecursiveSubSets(): Unit = {
    val setSize = if (executingInJVM) 1000 else 100
    val set = new TreeSet[Item]()
    bs = new ju.BitSet(setSize)
    populate(set, setSize)
    check(set, 0, setSize - 1, ascending = true)
    check(set.descendingSet(), 0, setSize - 1, ascending = false)
    mutateSet(set, 0, setSize - 1)
    check(set, 0, setSize - 1, ascending = true)
    check(set.descendingSet(), 0, setSize - 1, ascending = false)
    bashSubSet(
      set.subSet(zeroItem, true, itemFor(setSize), false),
      0,
      setSize - 1,
      ascending = true
    )
  }

  @Test def testAddAll_idempotent(): Unit = {
    val x: ju.Set[Item] = populatedSet(SIZE)
    val y: ju.Set[Item] = new TreeSet[Item](x)
    y.addAll(x)
    mustEqual(x, y)
    mustEqual(y, x)
  }

  private def populate(set: ju.NavigableSet[Item], limit: Int): Unit = {
    var i = 0
    val n = 2 * limit / 3
    while (i < n) {
      put(set, rnd.nextInt(limit))
      i += 1
    }
  }

  private def mutateSet(
      set: ju.NavigableSet[Item],
      min: Int,
      max: Int
  ): Unit = {
    val size = set.size()
    val rangeSize = max - min + 1
    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(set, min - 5 + rnd.nextInt(rangeSize + 10))
      i += 1
    }
    val it = set.iterator()
    while (it.hasNext()) {
      val item = it.next()
      if (rnd.nextBoolean()) {
        bs.clear(item.value)
        it.remove()
      }
    }
    while (set.size() < size) {
      val element = min + rnd.nextInt(rangeSize)
      assertTrue(element >= min && element <= max)
      put(set, element)
    }
  }

  private def mutateSubSet(
      set: ju.NavigableSet[Item],
      min: Int,
      max: Int
  ): Unit = {
    val size = set.size()
    val rangeSize = max - min + 1
    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(set, min - 5 + rnd.nextInt(rangeSize + 10))
      i += 1
    }
    val it = set.iterator()
    while (it.hasNext()) {
      val item = it.next()
      if (rnd.nextBoolean()) {
        bs.clear(item.value)
        it.remove()
      }
    }
    while (set.size() < size) {
      val element = min - 5 + rnd.nextInt(rangeSize + 10)
      if (element >= min && element <= max) put(set, element)
      else
        assertThrows(
          classOf[IllegalArgumentException],
          set.add(itemFor(element))
        )
    }
  }

  private def put(set: ju.NavigableSet[Item], element: Int): Unit =
    if (set.add(itemFor(element)))
      bs.set(element)

  private def remove(set: ju.NavigableSet[Item], element: Int): Unit =
    if (set.remove(itemFor(element)))
      bs.clear(element)

  private def bashSubSet(
      set: ju.NavigableSet[Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): Unit = {
    check(set, min, max, ascending)
    check(set.descendingSet(), min, max, !ascending)
    mutateSubSet(set, min, max)
    check(set, min, max, ascending)
    check(set.descendingSet(), min, max, !ascending)
    if (max - min < 2) return

    val midPoint = (min + max) / 2
    var incl = rnd.nextBoolean()
    val hm = set.headSet(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean())
        bashSubSet(hm, min, midPoint - (if (incl) 0 else 1), ascending = true)
      else
        bashSubSet(
          hm.descendingSet(),
          min,
          midPoint - (if (incl) 0 else 1),
          ascending = false
        )
    } else {
      if (rnd.nextBoolean())
        bashSubSet(hm, midPoint + (if (incl) 0 else 1), max, ascending = false)
      else
        bashSubSet(
          hm.descendingSet(),
          midPoint + (if (incl) 0 else 1),
          max,
          ascending = true
        )
    }

    incl = rnd.nextBoolean()
    val tm = set.tailSet(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean())
        bashSubSet(tm, midPoint + (if (incl) 0 else 1), max, ascending = true)
      else
        bashSubSet(
          tm.descendingSet(),
          midPoint + (if (incl) 0 else 1),
          max,
          ascending = false
        )
    } else {
      if (rnd.nextBoolean())
        bashSubSet(tm, min, midPoint - (if (incl) 0 else 1), ascending = false)
      else
        bashSubSet(
          tm.descendingSet(),
          min,
          midPoint - (if (incl) 0 else 1),
          ascending = true
        )
    }

    val rangeSize = max - min + 1
    val endpoints = Array(
      min + rnd.nextInt(rangeSize),
      min + rnd.nextInt(rangeSize)
    )
    ju.Arrays.sort(endpoints)
    val lowIncl = rnd.nextBoolean()
    val highIncl = rnd.nextBoolean()
    if (ascending) {
      val sm = set.subSet(
        itemFor(endpoints(0)),
        lowIncl,
        itemFor(endpoints(1)),
        highIncl
      )
      if (rnd.nextBoolean())
        bashSubSet(
          sm,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = true
        )
      else
        bashSubSet(
          sm.descendingSet(),
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = false
        )
    } else {
      val sm = set.subSet(
        itemFor(endpoints(1)),
        highIncl,
        itemFor(endpoints(0)),
        lowIncl
      )
      if (rnd.nextBoolean())
        bashSubSet(
          sm,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = false
        )
      else
        bashSubSet(
          sm.descendingSet(),
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = true
        )
    }
  }

  private def check(
      set: ju.NavigableSet[Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): Unit = {
    def lowerAscending(element: Int): Int = floorAscending(element - 1)
    def floorAscending(element0: Int): Int = {
      var element = element0
      if (element < min) return -1
      else if (element > max) element = max
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
    def higherAscending(element: Int): Int = ceilingAscending(element + 1)
    def firstAscending(): Int = {
      val result = ceilingAscending(min)
      if (result > max) -1 else result
    }
    def lastAscending(): Int = {
      val result = floorAscending(max)
      if (result < min) -1 else result
    }
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

    var size = 0
    var element = min
    while (element <= max) {
      val contains = bs.get(element)
      assertEquals(
        s"contains($element), min=$min max=$max ascending=$ascending",
        contains,
        set.contains(itemFor(element))
      )
      if (contains) size += 1
      element += 1
    }
    assertEquals(
      s"size, min=$min max=$max ascending=$ascending",
      size,
      set.size()
    )

    var size2 = 0
    var previousElement = -1
    val it = set.iterator()
    while (it.hasNext()) {
      val e = it.next()
      assertTrue(s"unexpected element ${e.value}", bs.get(e.value))
      size2 += 1
      assertTrue(
        s"iteration order element=${e.value} previous=$previousElement ascending=$ascending",
        previousElement < 0 ||
          (if (ascending) e.value - previousElement > 0
           else e.value - previousElement < 0)
      )
      previousElement = e.value
    }
    assertEquals(
      s"iterated size, min=$min max=$max ascending=$ascending",
      size,
      size2
    )

    element = min - 1
    while (element <= max + 1) {
      val e = itemFor(element)
      assertEq(set.lower(e), lower(element))
      assertEq(set.floor(e), floor(element))
      assertEq(set.higher(e), higher(element))
      assertEq(set.ceiling(e), ceiling(element))
      element += 1
    }

    if (set.size() != 0) {
      assertEq(set.first(), first())
      assertEq(set.last(), last())
    } else {
      mustEqual(first(), -1)
      mustEqual(last(), -1)
      assertThrows(classOf[NoSuchElementException], set.first())
      assertThrows(classOf[NoSuchElementException], set.last())
    }
  }

  private def assertEq(i: Item, j: Int): Unit =
    if (i == null) mustEqual(j, -1)
    else mustEqual(i, j)

  @Test def addRemoveInt(): Unit = {
    val ts = factory.empty[Int]

    assertEquals(0, ts.size())
    assertTrue(ts.add(222))
    assertEquals(1, ts.size())
    assertTrue(ts.add(111))
    assertEquals(2, ts.size())
    assertEquals(111, ts.first)
    assertTrue(ts.remove(111))

    assertEquals(1, ts.size())
    assertEquals(222, ts.first)

    assertTrue(ts.remove(222))
    assertEquals(0, ts.size())
    assertTrue(ts.isEmpty)
    assertFalse(ts.remove(333))
    assertThrows(classOf[NoSuchElementException], ts.first)

    if (factory.allowsNullElement) {
      assertTrue(ts.asInstanceOf[TreeSet[Any]].add(null))
      assertTrue(ts.contains(null))
      assertTrue(ts.remove(null))
      assertFalse(ts.contains(null))
    }
  }

  @Test def addRemoveString(): Unit = {
    val ts = factory.empty[String]

    assertEquals(0, ts.size())
    assertTrue(ts.add("222"))
    assertEquals(1, ts.size())
    assertTrue(ts.add("111"))
    assertEquals(2, ts.size())
    assertEquals("111", ts.first)
    assertTrue(ts.remove("111"))

    assertEquals(1, ts.size())
    assertEquals("222", ts.first)

    assertTrue(ts.remove("222"))
    assertEquals(0, ts.size())
    assertFalse(ts.remove("333"))
    assertTrue(ts.isEmpty)

    if (factory.allowsNullElement) {
      assertTrue(ts.add(null))
      assertTrue(ts.contains(null))
      assertTrue(ts.remove(null))
      assertFalse(ts.contains(null))
    }
  }

  @Test def addRemoveCustomComparator(): Unit = {
    case class Rect(x: Int, y: Int)

    val areaComp = new ju.Comparator[Rect] {
      def compare(a: Rect, b: Rect): Int = (a.x * a.y) - (b.x * b.y)
    }

    val ts = factory.empty[Rect](areaComp)

    assertTrue(ts.add(Rect(1, 2)))
    assertTrue(ts.add(Rect(2, 3)))
    assertTrue(ts.add(Rect(1, 3)))

    val first = ts.first()
    assertEquals(1, first.x)
    assertEquals(2, first.y)

    assertTrue(ts.remove(first))
    assertFalse(ts.remove(first))

    val second = ts.first()
    assertEquals(1, second.x)
    assertEquals(3, second.y)

    assertTrue(ts.remove(second))

    val third = ts.first()
    assertEquals(2, third.x)
    assertEquals(3, third.y)

    assertTrue(ts.remove(third))

    assertTrue(ts.isEmpty)
  }

  @Test def addRemoveDoubleCornerCases(): Unit = {
    val ts = factory.empty[Double]

    assertTrue(ts.add(1.0))
    assertTrue(ts.add(+0.0))
    assertTrue(ts.add(-0.0))
    assertTrue(ts.add(Double.NaN))

    assertTrue(ts.first.equals(-0.0))

    assertTrue(ts.remove(-0.0))

    assertTrue(ts.first.equals(+0.0))

    assertTrue(ts.remove(+0.0))

    assertTrue(ts.first.equals(1.0))

    assertTrue(ts.remove(1.0))

    assertTrue(ts.first.isNaN)

    assertTrue(ts.remove(Double.NaN))

    assertTrue(ts.isEmpty)
  }

  @Test def newFromCollectionInt(): Unit = {
    val l = TrivialImmutableCollection(1, 5, 2, 3, 4)
    val ts = factory.newFrom(l)

    assertEquals(5, ts.size())
    for (i <- 1 to 5) {
      assertEquals(i, ts.first)
      assertTrue(ts.remove(i))
    }
    assertTrue(ts.isEmpty)
  }

  @Test def clearTreeSet(): Unit = {
    val l = TrivialImmutableCollection(1, 5, 2, 3, 4)
    val ts = factory.empty[Int]

    ts.addAll(l)

    assertEquals(5, ts.size())
    ts.clear()
    assertEquals(0, ts.size())
  }

  @Test def addAllCollectionIntAndAddInt(): Unit = {
    val l = TrivialImmutableCollection(1, 5, 2, 3, 4)
    val ts = factory.empty[Int]

    assertEquals(0, ts.size())
    ts.addAll(l)
    assertEquals(5, ts.size())
    ts.add(6)
    assertEquals(6, ts.size())
  }

  @Test def containsDoubleCornerCasesTreeSet(): Unit = {
    val ts = factory.empty[Double]

    assertTrue(ts.add(11111.0))
    assertEquals(1, ts.size())
    assertTrue(ts.contains(11111.0))
    assertEquals(11111.0, ts.iterator.next(), 0.0)

    assertTrue(ts.add(Double.NaN))
    assertEquals(2, ts.size())
    assertTrue(ts.contains(Double.NaN))
    assertFalse(ts.contains(+0.0))
    assertFalse(ts.contains(-0.0))

    assertTrue(ts.remove(Double.NaN))
    assertTrue(ts.add(+0.0))
    assertEquals(2, ts.size())
    assertFalse(ts.contains(Double.NaN))
    assertTrue(ts.contains(+0.0))
    assertFalse(ts.contains(-0.0))

    assertTrue(ts.remove(+0.0))
    assertTrue(ts.add(-0.0))
    assertEquals(2, ts.size())
    assertFalse(ts.contains(Double.NaN))
    assertFalse(ts.contains(+0.0))
    assertTrue(ts.contains(-0.0))

    assertTrue(ts.add(+0.0))
    assertTrue(ts.add(Double.NaN))
    assertTrue(ts.contains(Double.NaN))
    assertTrue(ts.contains(+0.0))
    assertTrue(ts.contains(-0.0))
  }

  @Test def addNullOrNullNotSupportedThrows(): Unit = {
    val hs = factory.empty[String]

    assertTrue(hs.add("ONE"))
    assertTrue(hs.contains("ONE"))
    assertFalse(hs.contains("TWO"))

    if (factory.allowsNullElement) {
      assertTrue(hs.add(null))
      assertTrue(hs.contains(null))
    } else {
      assertThrows(classOf[Exception], hs.add(null))
    }
  }

  @Test def addAllNullOrNullNotSupportedThrows(): Unit = {
    val l = TrivialImmutableCollection("ONE", "TWO", (null: String))
    val ts1 = factory.empty[String]

    if (factory.allowsNullElement) {
      assertTrue(ts1.addAll(l))
      assertTrue(ts1.contains(null))
      assertTrue(ts1.contains("ONE"))
      assertFalse(ts1.contains("THREE"))
    } else {
      assertThrows(classOf[Exception], ts1.addAll(l))
    }
  }

  @Test def addNonComparableObjectThrows(): Unit = {
    assumeTrue("Assumed compliant asInstanceOf", hasCompliantAsInstanceOfs)

    class TestObj(num: Int)

    val ts1 = factory.empty[TestObj]
    assertEquals(0, ts1.size())
    assertThrows(classOf[ClassCastException], ts1.add(new TestObj(111)))
  }

  @Test def headSetTailSetSubSetThrowsOnAddElementOutOfBounds(): Unit = {
    assumeTrue("Assumed compliant asInstanceOf", hasCompliantAsInstanceOfs)

    val l = TrivialImmutableCollection(2, 3, 6)
    val ts = factory.empty[Int]
    ts.addAll(l)

    val hs1 = ts.headSet(5, true)
    assertTrue(hs1.add(4))
    assertTrue(hs1.add(5))
    assertThrows(classOf[IllegalArgumentException], hs1.add(6))

    ts.clear()
    ts.addAll(l)

    val hs2 = ts.headSet(5, false)
    assertTrue(hs2.add(4))
    assertThrows(classOf[IllegalArgumentException], hs2.add(5))

    ts.clear()
    ts.addAll(l)

    val ts1 = ts.tailSet(1, true)
    assertTrue(ts1.add(7))
    assertTrue(ts1.add(1))
    assertThrows(classOf[IllegalArgumentException], ts1.add(0))

    ts.clear()
    ts.addAll(l)

    val ts2 = ts.tailSet(1, false)
    assertTrue(ts2.add(7))
    assertThrows(classOf[IllegalArgumentException], ts2.add(1))

    ts.clear()
    ts.addAll(l)

    val ss1 = ts.subSet(1, true, 5, true)
    assertTrue(ss1.add(4))
    assertTrue(ss1.add(1))
    assertThrows(classOf[IllegalArgumentException], ss1.add(0))
    assertTrue(ss1.add(5))
    assertThrows(classOf[IllegalArgumentException], ss1.add(6))

    ts.clear()
    ts.addAll(l)

    val ss2 = ts.subSet(1, false, 5, false)
    assertTrue(ss2.add(4))
    assertThrows(classOf[IllegalArgumentException], ss2.add(1))
    assertThrows(classOf[IllegalArgumentException], ss2.add(5))
  }
}

class TreeSetFactory
    extends AbstractSetFactory
    with NavigableSetFactory
    with SortedSetFactory {
  def implementationName: String =
    "java.util.TreeSet"

  def empty[E: ClassTag]: ju.TreeSet[E] =
    new TreeSet[E]

  def empty[E](cmp: ju.Comparator[E]): ju.TreeSet[E] =
    new TreeSet[E](cmp)

  def newFrom[E](coll: ju.Collection[E]): ju.TreeSet[E] =
    new TreeSet[E](coll)

  override def allowsNullElement: Boolean = false

  override def allowsNullElementQuery: Boolean = false
}

class TreeSetWithNullFactory extends TreeSetFactory {

  override def implementationName: String =
    super.implementationName + " {allows null}"

  override def empty[E: ClassTag]: ju.TreeSet[E] = {
    val natural = Comparator.comparing[E, Comparable[Any]](
      ((_: E)
        .asInstanceOf[Comparable[Any]]): ju.function.Function[E, Comparable[
        Any
      ]]
    )
    new TreeSet[E](Comparator.nullsFirst(natural))
  }

  override def allowsNullElement: Boolean = true

  override def allowsNullElementQuery: Boolean = true
}
