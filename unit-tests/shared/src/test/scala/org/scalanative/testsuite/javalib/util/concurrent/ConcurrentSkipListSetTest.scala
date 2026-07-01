/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ConcurrentSkipListSet
import java.util.{
  Arrays, BitSet, Collection, Comparator, Iterator, NavigableSet,
  NoSuchElementException, Random, Set, SortedSet
}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

object ConcurrentSkipListSetTest {
  import JSR166Test._

  private val iZero = itemFor(0)
  private val iOne = itemFor(1)
  private val iTwo = itemFor(2)
  private val iThree = itemFor(3)
  private val iFour = itemFor(4)
  private val iFive = itemFor(5)
  private val iSix = itemFor(6)
  private val nOne = itemFor(-1)

  final class MyReverseComparator extends Comparator[Item] {
    override def compare(x: Item, y: Item): Int =
      y.compareTo(x)
  }

  private def populatedSet(n: Int): ConcurrentSkipListSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]()
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

  private def set5(): ConcurrentSkipListSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.isEmpty())
    q.add(iOne)
    q.add(iTwo)
    q.add(iThree)
    q.add(iFour)
    q.add(iFive)
    mustEqual(5, q.size())
    q
  }
}

class ConcurrentSkipListSetTest extends JSR166Test {
  import ConcurrentSkipListSetTest._
  import JSR166Test._

  @Test def testConstructor1(): Unit =
    mustEqual(0, new ConcurrentSkipListSet[Item]().size())

  @Test def testConstructor3(): Unit =
    assertThrows(
      classOf[NullPointerException],
      new ConcurrentSkipListSet[Item](null.asInstanceOf[Collection[Item]])
    )

  @Test def testConstructor4(): Unit =
    assertThrows(
      classOf[NullPointerException],
      new ConcurrentSkipListSet[Item](Arrays.asList(new Array[Item](SIZE): _*))
    )

  @Test def testConstructor5(): Unit = {
    val items = new Array[Item](2)
    items(0) = iZero
    assertThrows(
      classOf[NullPointerException],
      new ConcurrentSkipListSet[Item](Arrays.asList(items: _*))
    )
  }

  @Test def testConstructor6(): Unit = {
    val items = defaultItems
    val q = new ConcurrentSkipListSet[Item](Arrays.asList(items: _*))
    var i = 0
    while (i < SIZE) {
      mustEqual(items(i), q.pollFirst())
      i += 1
    }
  }

  @Test def testConstructor7(): Unit = {
    val cmp = new MyReverseComparator()
    val q = new ConcurrentSkipListSet[Item](cmp)
    mustEqual(cmp, q.comparator())
    val items = defaultItems
    q.addAll(Arrays.asList(items: _*))
    var i = SIZE - 1
    while (i >= 0) {
      mustEqual(items(i), q.pollFirst())
      i -= 1
    }
  }

  @Test def testEmpty(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.isEmpty())
    mustAdd(q, iOne)
    assertFalse(q.isEmpty())
    mustAdd(q, iTwo)
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

  @Test def testAddNull(): Unit =
    assertThrows(
      classOf[NullPointerException],
      new ConcurrentSkipListSet[Item]().add(null)
    )

  @Test def testAdd(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.add(iZero))
    assertTrue(q.add(iOne))
  }

  @Test def testAddDup(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.add(iZero))
    assertFalse(q.add(iZero))
  }

  @Test def testAddNonComparable(): Unit = {
    val q = new ConcurrentSkipListSet[Object]()
    try {
      q.add(new Object())
      q.add(new Object())
      shouldThrow()
    } catch {
      case _: ClassCastException =>
        assertTrue(q.size() < 2)
        var i = 0
        val size = q.size()
        while (i < size) {
          assertSame(classOf[Object], q.pollFirst().getClass())
          i += 1
        }
        assertNull(q.pollFirst())
        assertTrue(q.isEmpty())
        mustEqual(0, q.size())
    }
  }

  @Test def testAddAll1(): Unit =
    assertThrows(
      classOf[NullPointerException],
      new ConcurrentSkipListSet[Item]().addAll(null)
    )

  @Test def testAddAll2(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
    val items = new Array[Item](SIZE)
    assertThrows(
      classOf[NullPointerException],
      q.addAll(Arrays.asList(items: _*))
    )
  }

  @Test def testAddAll3(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
    val items = new Array[Item](2)
    items(0) = iZero
    assertThrows(
      classOf[NullPointerException],
      q.addAll(Arrays.asList(items: _*))
    )
  }

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
    mustAdd(q, iOne)
    assertFalse(q.isEmpty())
    q.clear()
    assertTrue(q.isEmpty())
  }

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
    mustEqual(iTwo, q.lower(iThree))
    mustEqual(iFive, q.lower(iSix))
    assertNull(q.lower(iOne))
    assertNull(q.lower(iZero))
  }

  @Test def testHigher(): Unit = {
    val q = set5()
    mustEqual(iFour, q.higher(iThree))
    mustEqual(iOne, q.higher(iZero))
    assertNull(q.higher(iFive))
    assertNull(q.higher(iSix))
  }

  @Test def testFloor(): Unit = {
    val q = set5()
    mustEqual(iThree, q.floor(iThree))
    mustEqual(iFive, q.floor(iSix))
    mustEqual(iOne, q.floor(iOne))
    assertNull(q.floor(iZero))
  }

  @Test def testCeiling(): Unit = {
    val q = set5()
    mustEqual(iThree, q.ceiling(iThree))
    mustEqual(iOne, q.ceiling(iZero))
    mustEqual(iFive, q.ceiling(iFive))
    assertNull(q.ceiling(iSix))
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
    assertSame(items, q.toArray(items))
    for (o <- items)
      assertSame(o, q.pollFirst())
    assertTrue(q.isEmpty())
  }

  @Test def testIterator(): Unit = {
    val q = populatedSet(SIZE)
    val it = q.iterator()
    var i = 0
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  @Test def testEmptyIterator(): Unit = {
    val s = new ConcurrentSkipListSet[Item]()
    assertIteratorExhausted(s.iterator())
    assertIteratorExhausted(s.descendingSet().iterator())
  }

  @Test def testIteratorRemove(): Unit = {
    val q = new ConcurrentSkipListSet[Item]()
    q.add(iTwo)
    q.add(iOne)
    q.add(iThree)
    var it = q.iterator()
    it.next()
    it.remove()
    it = q.iterator()
    mustEqual(it.next(), iTwo)
    mustEqual(it.next(), iThree)
    assertFalse(it.hasNext())
  }

  @Test def testToString(): Unit = {
    val q = populatedSet(SIZE)
    val s = q.toString()
    var i = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  @Test def testClone(): Unit = {
    val x = populatedSet(SIZE)
    val y = x.clone()
    assertNotSame(x, y)
    mustEqual(x.size(), y.size())
    mustEqual(x, y)
    mustEqual(y, x)
    while (!x.isEmpty()) {
      assertFalse(y.isEmpty())
      mustEqual(x.pollFirst(), y.pollFirst())
    }
    assertTrue(y.isEmpty())
  }

  @Ignore("scala-native#4852: ObjectInputStream is unsupported")
  @Test def testSerialization(): Unit = {}

  @Test def testSubSetContents(): Unit = {
    val set = set5()
    val sm = set.subSet(iTwo, iFour)
    mustEqual(iTwo, sm.first())
    mustEqual(iThree, sm.last())
    mustEqual(2, sm.size())
    mustNotContain(sm, iOne)
    mustContain(sm, iTwo)
    mustContain(sm, iThree)
    mustNotContain(sm, iFour)
    mustNotContain(sm, iFive)
    val i = sm.iterator()
    mustEqual(iTwo, i.next())
    mustEqual(iThree, i.next())
    assertFalse(i.hasNext())
    val j = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, iTwo)
    mustEqual(4, set.size())
    mustEqual(1, sm.size())
    mustEqual(iThree, sm.first())
    mustEqual(iThree, sm.last())
    mustRemove(sm, iThree)
    assertTrue(sm.isEmpty())
    mustEqual(3, set.size())
  }

  @Test def testSubSetContents2(): Unit = {
    val set = set5()
    val sm = set.subSet(iTwo, iThree)
    mustEqual(1, sm.size())
    mustEqual(iTwo, sm.first())
    mustEqual(iTwo, sm.last())
    mustNotContain(sm, iOne)
    mustContain(sm, iTwo)
    mustNotContain(sm, iThree)
    mustNotContain(sm, iFour)
    mustNotContain(sm, iFive)
    val i = sm.iterator()
    mustEqual(iTwo, i.next())
    assertFalse(i.hasNext())
    val j = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, iTwo)
    mustEqual(4, set.size())
    mustEqual(0, sm.size())
    assertTrue(sm.isEmpty())
    mustNotRemove(sm, iThree)
    mustEqual(4, set.size())
  }

  @Test def testHeadSetContents(): Unit = {
    val set = set5()
    val sm = set.headSet(iFour)
    mustContain(sm, iOne)
    mustContain(sm, iTwo)
    mustContain(sm, iThree)
    mustNotContain(sm, iFour)
    mustNotContain(sm, iFive)
    val i = sm.iterator()
    mustEqual(iOne, i.next())
    mustEqual(iTwo, i.next())
    mustEqual(iThree, i.next())
    assertFalse(i.hasNext())
    sm.clear()
    assertTrue(sm.isEmpty())
    mustEqual(2, set.size())
    mustEqual(iFour, set.first())
  }

  @Test def testTailSetContents(): Unit = {
    val set = set5()
    val sm = set.tailSet(iTwo)
    mustNotContain(sm, iOne)
    mustContain(sm, iTwo)
    mustContain(sm, iThree)
    mustContain(sm, iFour)
    mustContain(sm, iFive)
    mustContain(sm, iTwo)
    val i = sm.iterator()
    mustEqual(iTwo, i.next())
    mustEqual(iThree, i.next())
    mustEqual(iFour, i.next())
    mustEqual(iFive, i.next())
    assertFalse(i.hasNext())
    val ssm = sm.tailSet(iFour)
    mustEqual(iFour, ssm.first())
    mustEqual(iFive, ssm.last())
    mustRemove(ssm, iFour)
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, set.size())
  }

  private val rnd = new Random(666)

  @Test def testRecursiveSubSets(): Unit = {
    val setSize = if (expensiveTests) 1000 else 100
    val set = newSet()
    val bs = new BitSet(setSize)

    populate(set, setSize, bs)
    check(set, 0, setSize - 1, ascending = true, bs)
    check(set.descendingSet(), 0, setSize - 1, ascending = false, bs)

    mutateSet(set, 0, setSize - 1, bs)
    check(set, 0, setSize - 1, ascending = true, bs)
    check(set.descendingSet(), 0, setSize - 1, ascending = false, bs)

    bashSubSet(
      set.subSet(iZero, true, itemFor(setSize), false),
      0,
      setSize - 1,
      ascending = true,
      bs
    )
  }

  @Test def testAddAll_idempotent(): Unit = {
    val x: Set[Item] = populatedSet(SIZE)
    val y = new ConcurrentSkipListSet[Item](x)
    y.addAll(x)
    mustEqual(x, y)
    mustEqual(y, x)
  }

  private def newSet(): NavigableSet[Item] = {
    val result = new ConcurrentSkipListSet[Item]()
    mustEqual(0, result.size())
    assertFalse(result.iterator().hasNext())
    result
  }

  private def populate(
      set: NavigableSet[Item],
      limit: Int,
      bs: BitSet
  ): Unit = {
    var i = 0
    val n = 2 * limit / 3
    while (i < n) {
      put(set, rnd.nextInt(limit), bs)
      i += 1
    }
  }

  private def mutateSet(
      set: NavigableSet[Item],
      min: Int,
      max: Int,
      bs: BitSet
  ): Unit = {
    val size = set.size()
    val rangeSize = max - min + 1

    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(set, min - 5 + rnd.nextInt(rangeSize + 10), bs)
      i += 1
    }

    val it = set.iterator()
    while (it.hasNext()) {
      if (rnd.nextBoolean()) {
        bs.clear(it.next().value)
        it.remove()
      }
    }

    while (set.size() < size) {
      val element = min + rnd.nextInt(rangeSize)
      assertTrue(element >= min && element <= max)
      put(set, element, bs)
    }
  }

  private def mutateSubSet(
      set: NavigableSet[Item],
      min: Int,
      max: Int,
      bs: BitSet
  ): Unit = {
    val size = set.size()
    val rangeSize = max - min + 1

    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(set, min - 5 + rnd.nextInt(rangeSize + 10), bs)
      i += 1
    }

    val it = set.iterator()
    while (it.hasNext()) {
      if (rnd.nextBoolean()) {
        bs.clear(it.next().value)
        it.remove()
      }
    }

    while (set.size() < size) {
      val element = min - 5 + rnd.nextInt(rangeSize + 10)
      if (element >= min && element <= max) put(set, element, bs)
      else
        assertThrows(
          classOf[IllegalArgumentException],
          set.add(itemFor(element))
        )
    }
  }

  private def put(set: NavigableSet[Item], element: Int, bs: BitSet): Unit = {
    if (set.add(itemFor(element)))
      bs.set(element)
  }

  private def remove(
      set: NavigableSet[Item],
      element: Int,
      bs: BitSet
  ): Unit = {
    if (set.remove(itemFor(element)))
      bs.clear(element)
  }

  private def bashSubSet(
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

    if (max - min < 2)
      return
    val midPoint = (min + max) / 2

    var incl = rnd.nextBoolean()
    val hm = set.headSet(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean())
        bashSubSet(
          hm,
          min,
          midPoint - (if (incl) 0 else 1),
          ascending = true,
          bs
        )
      else
        bashSubSet(
          hm.descendingSet(),
          min,
          midPoint - (if (incl) 0 else 1),
          ascending = false,
          bs
        )
    } else {
      if (rnd.nextBoolean())
        bashSubSet(
          hm,
          midPoint + (if (incl) 0 else 1),
          max,
          ascending = false,
          bs
        )
      else
        bashSubSet(
          hm.descendingSet(),
          midPoint + (if (incl) 0 else 1),
          max,
          ascending = true,
          bs
        )
    }

    incl = rnd.nextBoolean()
    val tm = set.tailSet(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean())
        bashSubSet(
          tm,
          midPoint + (if (incl) 0 else 1),
          max,
          ascending = true,
          bs
        )
      else
        bashSubSet(
          tm.descendingSet(),
          midPoint + (if (incl) 0 else 1),
          max,
          ascending = false,
          bs
        )
    } else {
      if (rnd.nextBoolean())
        bashSubSet(
          tm,
          min,
          midPoint - (if (incl) 0 else 1),
          ascending = false,
          bs
        )
      else
        bashSubSet(
          tm.descendingSet(),
          min,
          midPoint - (if (incl) 0 else 1),
          ascending = true,
          bs
        )
    }

    val rangeSize = max - min + 1
    val endpoints = Array(
      min + rnd.nextInt(rangeSize),
      min + rnd.nextInt(rangeSize)
    )
    Arrays.sort(endpoints)
    val lowIncl = rnd.nextBoolean()
    val highIncl = rnd.nextBoolean()
    if (ascending) {
      val sm =
        set.subSet(
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
          ascending = true,
          bs
        )
      else
        bashSubSet(
          sm.descendingSet(),
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = false,
          bs
        )
    } else {
      val sm =
        set.subSet(
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
          ascending = false,
          bs
        )
      else
        bashSubSet(
          sm.descendingSet(),
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = true,
          bs
        )
    }
  }

  private def check(
      set: NavigableSet[Item],
      min: Int,
      max: Int,
      ascending: Boolean,
      bs: BitSet
  ): Unit = {
    final class ReferenceSet {
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
      def lowerAscending(element: Int): Int = floorAscending(element - 1)
      def floorAscending(initial: Int): Int = {
        var element = initial
        if (element < min) return -1
        else if (element > max) element = max
        while (element >= min) {
          if (bs.get(element)) return element
          element -= 1
        }
        -1
      }
      def ceilingAscending(initial: Int): Int = {
        var element = initial
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
    val rs = new ReferenceSet()

    var size = 0
    var i = min
    while (i <= max) {
      val bsContainsI = bs.get(i)
      mustEqual(bsContainsI, set.contains(itemFor(i)))
      if (bsContainsI) size += 1
      i += 1
    }
    mustEqual(size, set.size())

    var size2 = 0
    var previousElement = -1
    val it = set.iterator()
    while (it.hasNext()) {
      val element = it.next()
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

    var element = min - 1
    while (element <= max + 1) {
      val e = itemFor(element)
      assertEq(set.lower(e), rs.lower(element))
      assertEq(set.floor(e), rs.floor(element))
      assertEq(set.higher(e), rs.higher(element))
      assertEq(set.ceiling(e), rs.ceiling(element))
      element += 1
    }

    if (set.size() != 0) {
      assertEq(set.first(), rs.first())
      assertEq(set.last(), rs.last())
    } else {
      mustEqual(rs.first(), -1)
      mustEqual(rs.last(), -1)
      assertThrows(classOf[NoSuchElementException], set.first())
      assertThrows(classOf[NoSuchElementException], set.last())
    }
  }

  private def assertEq(i: Item, j: Int): Unit = {
    if (i == null) mustEqual(j, -1)
    else mustEqual(i, j)
  }
}
