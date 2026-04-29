/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ConcurrentSkipListSet
import java.util.{Arrays, Comparator, Iterator, NavigableSet, SortedSet}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

object ConcurrentSkipListSubSetTest {
  import JSR166Test._

  private val iZero = itemFor(0)
  private val iOne = itemFor(1)
  private val iTwo = itemFor(2)
  private val iThree = itemFor(3)
  private val iFour = itemFor(4)
  private val iFive = itemFor(5)
  private val iSix = itemFor(6)
  private val iSeven = itemFor(7)
  private val nOne = itemFor(-1)
  private val nTwo = itemFor(-2)
  private val nThree = itemFor(-3)
  private val nFour = itemFor(-4)
  private val nFive = itemFor(-5)
  private val nSix = itemFor(-6)
  private val nTen = itemFor(-10)

  final class MyReverseComparator extends Comparator[Item] {
    override def compare(x: Item, y: Item): Int =
      y.compareTo(x)
  }

  private def populatedSet(n: Int): NavigableSet[Item] = {
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
    mustAdd(q, -n)
    mustAdd(q, n)
    val s = q.subSet(itemFor(0), true, itemFor(n), false)
    assertFalse(s.isEmpty())
    mustEqual(n, s.size())
    s
  }

  private def set5(): NavigableSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.isEmpty())
    q.add(iOne)
    q.add(iTwo)
    q.add(iThree)
    q.add(iFour)
    q.add(iFive)
    q.add(iZero)
    q.add(iSeven)
    val s = q.subSet(iOne, true, iSeven, false)
    mustEqual(5, s.size())
    s
  }

  private def dset5(): NavigableSet[Item] = {
    val q = new ConcurrentSkipListSet[Item]()
    assertTrue(q.isEmpty())
    q.add(nOne)
    q.add(nTwo)
    q.add(nThree)
    q.add(nFour)
    q.add(nFive)
    val s = q.descendingSet()
    mustEqual(5, s.size())
    s
  }

  private def set0(): NavigableSet[Item] = {
    val set = new ConcurrentSkipListSet[Item]()
    assertTrue(set.isEmpty())
    set.tailSet(nOne, true)
  }

  private def dset0(): NavigableSet[Item] = {
    val set = new ConcurrentSkipListSet[Item]()
    assertTrue(set.isEmpty())
    set
  }
}

class ConcurrentSkipListSubSetTest extends JSR166Test {
  import ConcurrentSkipListSubSetTest._
  import JSR166Test._

  @Test def testConstructor1(): Unit =
    mustEqual(0, set0().size())

  @Test def testEmpty(): Unit = {
    val q = set0()
    assertTrue(q.isEmpty())
    mustAdd(q, iOne)
    assertFalse(q.isEmpty())
    mustAdd(q, iTwo)
    q.pollFirst()
    q.pollFirst()
    assertTrue(q.isEmpty())
  }

  @Test def testSize(): Unit =
    checkSize(populatedSet(SIZE))

  @Test def testAddNull(): Unit =
    assertThrows(classOf[NullPointerException], set0().add(null))

  @Test def testAdd(): Unit = {
    val q = set0()
    assertTrue(q.add(iZero))
    assertTrue(q.add(iOne))
  }

  @Test def testAddDup(): Unit = {
    val q = set0()
    assertTrue(q.add(iZero))
    assertFalse(q.add(iZero))
  }

  @Test def testAddNonComparable(): Unit =
    assertAddNonComparableThrows()

  @Test def testAddAll1(): Unit =
    assertThrows(classOf[NullPointerException], set0().addAll(null))

  @Test def testAddAll2(): Unit = {
    val q = set0()
    val items = new Array[Item](1)
    assertThrows(
      classOf[NullPointerException],
      q.addAll(Arrays.asList(items: _*))
    )
  }

  @Test def testAddAll3(): Unit = {
    val q = set0()
    val items = new Array[Item](2)
    items(0) = iZero
    assertThrows(
      classOf[NullPointerException],
      q.addAll(Arrays.asList(items: _*))
    )
  }

  @Test def testAddAll5(): Unit =
    checkAddAll5(set0(), ascending = true)

  @Test def testPoll(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
    assertNull(q.pollFirst())
  }

  @Test def testRemoveElement(): Unit =
    checkRemoveElement(populatedSet(SIZE), ascending = true)

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

  @Test def testClear(): Unit =
    checkClear(populatedSet(SIZE), iOne)

  @Test def testContainsAll(): Unit =
    checkContainsAll(populatedSet(SIZE), set0(), ascending = true)

  @Test def testRetainAll(): Unit =
    checkRetainAll(populatedSet(SIZE), populatedSet(SIZE))

  @Test def testRemoveAll(): Unit =
    checkRemoveAll(ascending = true)

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

  @Test def testToArray(): Unit =
    checkToArray(populatedSet(SIZE))

  @Test def testToArray2(): Unit =
    checkToArray2(populatedSet(SIZE))

  @Test def testIterator(): Unit =
    checkIterator(populatedSet(SIZE))

  @Test def testEmptyIterator(): Unit =
    assertIteratorExhausted(set0().iterator())

  @Test def testIteratorRemove(): Unit =
    checkIteratorRemove(set0(), iOne, iTwo, iThree)

  @Test def testToString(): Unit =
    checkToString(populatedSet(SIZE), 0 until SIZE)

  @Ignore("No ObjectInputStream in Scala Native")
  @Test def testSerialization(): Unit = {}

  @Test def testSubSetContents(): Unit =
    checkSubSetContents(set5(), iTwo, iFour, iOne, iThree, iFive)

  @Test def testSubSetContents2(): Unit =
    checkSubSetContents2(set5(), iTwo, iThree, iOne, iFour, iFive)

  @Test def testHeadSetContents(): Unit =
    checkHeadSetContents(set5(), iFour, iOne, iTwo, iThree, iFive)

  @Test def testTailSetContents(): Unit =
    checkTailSetContents(set5(), iTwo, iFour, iOne, iThree, iFive)

  @Test def testDescendingSize(): Unit =
    checkSize(populatedSet(SIZE))

  @Test def testDescendingAddNull(): Unit =
    assertThrows(classOf[NullPointerException], dset0().add(null))

  @Test def testDescendingAdd(): Unit = {
    val q = dset0()
    assertTrue(q.add(nSix))
  }

  @Test def testDescendingAddDup(): Unit = {
    val q = dset0()
    assertTrue(q.add(nSix))
    assertFalse(q.add(nSix))
  }

  @Test def testDescendingAddNonComparable(): Unit =
    assertAddNonComparableThrows()

  @Test def testDescendingAddAll1(): Unit =
    assertThrows(classOf[NullPointerException], dset0().addAll(null))

  @Test def testDescendingAddAll2(): Unit = {
    val q = dset0()
    val items = new Array[Item](1)
    assertThrows(
      classOf[NullPointerException],
      q.addAll(Arrays.asList(items: _*))
    )
  }

  @Test def testDescendingAddAll3(): Unit = {
    val q = dset0()
    val items = new Array[Item](2)
    items(0) = iZero
    assertThrows(
      classOf[NullPointerException],
      q.addAll(Arrays.asList(items: _*))
    )
  }

  @Test def testDescendingAddAll5(): Unit =
    checkAddAll5(dset0(), ascending = true)

  @Test def testDescendingPoll(): Unit = {
    val q = dset5()
    var i = 1
    while (i <= 5) {
      mustEqual(-i, q.pollFirst())
      i += 1
    }
    assertNull(q.pollFirst())
  }

  @Test def testDescendingRemoveElement(): Unit =
    checkDescendingRemoveElement(dset5())

  @Test def testDescendingContains(): Unit = {
    val q = dset5()
    var i = 1
    while (i <= 5) {
      mustContain(q, -i)
      q.pollFirst()
      mustNotContain(q, -i)
      i += 1
    }
  }

  @Test def testDescendingClear(): Unit =
    checkClear(dset5(), nOne)

  @Test def testDescendingContainsAll(): Unit =
    checkDescendingContainsAll()

  @Test def testDescendingRetainAll(): Unit =
    checkRetainAll(dset5(), dset5())

  @Test def testDescendingRemoveAll(): Unit =
    checkDescendingRemoveAll()

  @Test def testDescendingLower(): Unit = {
    val q = dset5()
    mustEqual(nTwo, q.lower(nThree))
    mustEqual(nFive, q.lower(nSix))
    assertNull(q.lower(nOne))
    assertNull(q.lower(iZero))
  }

  @Test def testDescendingHigher(): Unit = {
    val q = dset5()
    mustEqual(nFour, q.higher(nThree))
    mustEqual(nOne, q.higher(iZero))
    assertNull(q.higher(nFive))
    assertNull(q.higher(nSix))
  }

  @Test def testDescendingFloor(): Unit = {
    val q = dset5()
    mustEqual(nThree, q.floor(nThree))
    mustEqual(nFive, q.floor(nSix))
    mustEqual(nOne, q.floor(nOne))
    assertNull(q.floor(iZero))
  }

  @Test def testDescendingCeiling(): Unit = {
    val q = dset5()
    mustEqual(nThree, q.ceiling(nThree))
    mustEqual(nOne, q.ceiling(iZero))
    mustEqual(nFive, q.ceiling(nFive))
    assertNull(q.ceiling(nSix))
  }

  @Test def testDescendingToArray(): Unit =
    checkToArray(dset5())

  @Test def testDescendingToArray2(): Unit =
    checkToArray2(dset5())

  @Test def testDescendingIterator(): Unit =
    checkDescendingIterator(dset5())

  @Test def testDescendingEmptyIterator(): Unit = {
    val s = dset0()
    assertIteratorExhausted(s.iterator())
    assertIteratorExhausted(s.descendingSet().iterator())
  }

  @Test def testDescendingIteratorRemove(): Unit =
    checkIteratorRemove(dset0(), iOne, iTwo, iThree)

  @Test def testDescendingToString(): Unit =
    checkToString(dset5(), -5 to -1)

  @Ignore("No ObjectInputStream in Scala Native")
  @Test def testDescendingSerialization(): Unit = {}

  @Test def testDescendingSubSetContents(): Unit =
    checkSubSetContents(dset5(), nTwo, nFour, nOne, nThree, nFive)

  @Test def testDescendingSubSetContents2(): Unit =
    checkSubSetContents2(dset5(), nTwo, nThree, nOne, nFour, nFive)

  @Test def testDescendingHeadSetContents(): Unit =
    checkHeadSetContents(dset5(), nFour, nOne, nTwo, nThree, nFive)

  @Test def testDescendingTailSetContents(): Unit =
    checkTailSetContents(dset5(), nTwo, nFour, nOne, nThree, nFive)

  private def checkSize(q: NavigableSet[Item]): Unit = {
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

  private def assertAddNonComparableThrows(): Unit = {
    val q = new ConcurrentSkipListSet[Object]()
    assertThrows(
      classOf[ClassCastException], {
        q.add(new Object())
        q.add(new Object())
      }
    )
  }

  private def checkAddAll5(q: NavigableSet[Item], ascending: Boolean): Unit = {
    val empty = new Array[Item](0)
    val items = defaultItems
    assertFalse(q.addAll(Arrays.asList(empty: _*)))
    assertTrue(q.addAll(Arrays.asList(items: _*)))
    if (ascending) {
      var i = 0
      while (i < SIZE) {
        mustEqual(i, q.pollFirst())
        i += 1
      }
    }
  }

  private def checkRemoveElement(
      q: NavigableSet[Item],
      ascending: Boolean
  ): Unit = {
    var i = if (ascending) 1 else -1
    while (if (ascending) i < SIZE else i >= -5) {
      mustContain(q, i)
      mustRemove(q, i)
      mustNotContain(q, i)
      if (ascending) i += 2
      else i -= 2
    }
  }

  private def checkDescendingRemoveElement(q: NavigableSet[Item]): Unit = {
    var i = -1
    while (i >= -5) {
      mustContain(q, i)
      mustRemove(q, i)
      mustNotContain(q, i)
      i -= 2
    }
    i = -2
    while (i >= -5) {
      if (q.contains(itemFor(i))) mustRemove(q, i)
      i -= 2
    }
    assertTrue(q.isEmpty())
  }

  private def checkClear(q: NavigableSet[Item], sample: Item): Unit = {
    q.clear()
    assertTrue(q.isEmpty())
    mustEqual(0, q.size())
    mustAdd(q, sample)
    assertFalse(q.isEmpty())
    q.clear()
    assertTrue(q.isEmpty())
  }

  private def checkContainsAll(
      q: NavigableSet[Item],
      p: NavigableSet[Item],
      ascending: Boolean
  ): Unit = {
    var i = 0
    while (i < SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, if (ascending) i else -i)
      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  private def checkDescendingContainsAll(): Unit = {
    val q = dset5()
    val p = dset0()
    var i = 1
    while (i <= 5) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, -i)
      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  private def checkRetainAll(
      q: NavigableSet[Item],
      p: NavigableSet[Item]
  ): Unit = {
    var i = 0
    val size = q.size()
    while (i < size) {
      val changed = q.retainAll(p)
      if (i == 0) assertFalse(changed)
      else assertTrue(changed)
      assertTrue(q.containsAll(p))
      mustEqual(size - i, q.size())
      p.pollFirst()
      i += 1
    }
  }

  private def checkRemoveAll(ascending: Boolean): Unit = {
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

  private def checkDescendingRemoveAll(): Unit = {
    var i = 1
    while (i <= 5) {
      val q = dset5()
      val p = new ConcurrentSkipListSet[Item]().descendingSet()
      var j = 1
      while (j <= i) {
        mustAdd(p, -j)
        j += 1
      }
      assertTrue(q.removeAll(p))
      mustEqual(5 - i, q.size())
      j = 1
      while (j <= i) {
        mustNotContain(q, -j)
        j += 1
      }
      i += 1
    }
  }

  private def checkToArray(q: NavigableSet[Item]): Unit = {
    val a = q.toArray()
    assertSame(classOf[Array[Object]], a.getClass())
    for (o <- a)
      assertSame(o, q.pollFirst())
    assertTrue(q.isEmpty())
  }

  private def checkToArray2(q: NavigableSet[Item]): Unit = {
    val items = new Array[Item](q.size())
    assertSame(items, q.toArray(items))
    for (o <- items)
      assertSame(o, q.pollFirst())
    assertTrue(q.isEmpty())
  }

  private def checkIterator(q: NavigableSet[Item]): Unit = {
    val it = q.iterator()
    var i = 0
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
    assertIteratorExhausted(it)
  }

  private def checkDescendingIterator(q: NavigableSet[Item]): Unit = {
    val it = q.iterator()
    var i = 0
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, 5)
    assertIteratorExhausted(it)
  }

  private def checkIteratorRemove(
      q: NavigableSet[Item],
      first: Item,
      second: Item,
      third: Item
  ): Unit = {
    q.add(second)
    q.add(first)
    q.add(third)
    var it = q.iterator()
    it.next()
    it.remove()
    it = q.iterator()
    mustEqual(it.next(), second)
    mustEqual(it.next(), third)
    assertFalse(it.hasNext())
  }

  private def checkToString(
      q: NavigableSet[Item],
      values: Iterable[Int]
  ): Unit = {
    val s = q.toString()
    for (i <- values)
      assertTrue(s.contains(String.valueOf(i)))
  }

  private def checkSubSetContents(
      set: NavigableSet[Item],
      from: Item,
      to: Item,
      before: Item,
      middle: Item,
      after: Item
  ): Unit = {
    val sm = set.subSet(from, to)
    mustEqual(from, sm.first())
    mustEqual(middle, sm.last())
    mustEqual(2, sm.size())
    mustNotContain(sm, before)
    mustContain(sm, from)
    mustContain(sm, middle)
    mustNotContain(sm, to)
    mustNotContain(sm, after)
    val i = sm.iterator()
    mustEqual(from, i.next())
    mustEqual(middle, i.next())
    assertFalse(i.hasNext())
    val j = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, from)
    mustEqual(4, set.size())
    mustEqual(1, sm.size())
    mustEqual(middle, sm.first())
    mustEqual(middle, sm.last())
    mustRemove(sm, middle)
    assertTrue(sm.isEmpty())
    mustEqual(3, set.size())
  }

  private def checkSubSetContents2(
      set: NavigableSet[Item],
      from: Item,
      to: Item,
      before: Item,
      after1: Item,
      after2: Item
  ): Unit = {
    val sm = set.subSet(from, to)
    mustEqual(1, sm.size())
    mustEqual(from, sm.first())
    mustEqual(from, sm.last())
    mustNotContain(sm, before)
    mustContain(sm, from)
    mustNotContain(sm, to)
    mustNotContain(sm, after1)
    mustNotContain(sm, after2)
    val i = sm.iterator()
    mustEqual(from, i.next())
    assertFalse(i.hasNext())
    val j = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, from)
    mustEqual(4, set.size())
    mustEqual(0, sm.size())
    assertTrue(sm.isEmpty())
    mustNotRemove(sm, to)
    mustEqual(4, set.size())
  }

  private def checkHeadSetContents(
      set: NavigableSet[Item],
      to: Item,
      first: Item,
      second: Item,
      third: Item,
      after: Item
  ): Unit = {
    val sm = set.headSet(to)
    mustContain(sm, first)
    mustContain(sm, second)
    mustContain(sm, third)
    mustNotContain(sm, to)
    mustNotContain(sm, after)
    val i = sm.iterator()
    mustEqual(first, i.next())
    mustEqual(second, i.next())
    mustEqual(third, i.next())
    assertFalse(i.hasNext())
    sm.clear()
    assertTrue(sm.isEmpty())
    mustEqual(2, set.size())
    mustEqual(to, set.first())
  }

  private def checkTailSetContents(
      set: NavigableSet[Item],
      from: Item,
      split: Item,
      before: Item,
      second: Item,
      last: Item
  ): Unit = {
    val sm = set.tailSet(from)
    mustNotContain(sm, before)
    mustContain(sm, from)
    mustContain(sm, second)
    mustContain(sm, split)
    mustContain(sm, last)
    mustContain(sm, from)
    val i = sm.iterator()
    mustEqual(from, i.next())
    mustEqual(second, i.next())
    mustEqual(split, i.next())
    mustEqual(last, i.next())
    assertFalse(i.hasNext())

    val ssm = sm.tailSet(split)
    mustEqual(split, ssm.first())
    mustEqual(last, ssm.last())
    mustRemove(ssm, split)
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, set.size())
  }
}
