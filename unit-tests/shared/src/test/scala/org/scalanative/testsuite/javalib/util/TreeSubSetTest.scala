package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class TreeSubSetTest extends JSR166Test {
  import JSR166Test._

  private val zeroItem = itemFor(0)
  private val oneItem = itemFor(1)
  private val twoItem = itemFor(2)
  private val threeItem = itemFor(3)
  private val fourItem = itemFor(4)
  private val fiveItem = itemFor(5)
  private val sixItem = itemFor(6)
  private val sevenItem = itemFor(7)
  private val minusOneItem = itemFor(-1)
  private val minusTwoItem = itemFor(-2)
  private val minusThreeItem = itemFor(-3)
  private val minusFourItem = itemFor(-4)
  private val minusFiveItem = itemFor(-5)
  private val minusSixItem = itemFor(-6)
  private val ninetyNineItem = itemFor(99)

  private def populatedSet(n: Int): ju.NavigableSet[Item] = {
    val q = new ju.TreeSet[Item]()
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
    mustAdd(q, itemFor(-n))
    mustAdd(q, itemFor(n))

    val s = q.subSet(zeroItem, true, itemFor(n), false)
    assertFalse(s.isEmpty())
    mustEqual(n, s.size())
    s
  }

  private def set5(): ju.NavigableSet[Item] = {
    val q = new ju.TreeSet[Item]()
    assertTrue(q.isEmpty())
    q.add(oneItem)
    q.add(twoItem)
    q.add(threeItem)
    q.add(fourItem)
    q.add(fiveItem)
    q.add(zeroItem)
    q.add(sevenItem)
    val s = q.subSet(oneItem, true, sevenItem, false)
    mustEqual(5, s.size())
    s
  }

  private def dset5(): ju.NavigableSet[Item] = {
    val q = new ju.TreeSet[Item]()
    assertTrue(q.isEmpty())
    q.add(minusOneItem)
    q.add(minusTwoItem)
    q.add(minusThreeItem)
    q.add(minusFourItem)
    q.add(minusFiveItem)
    val s = q.descendingSet()
    mustEqual(5, s.size())
    s
  }

  private def set0(): ju.NavigableSet[Item] = {
    val set = new ju.TreeSet[Item]()
    assertTrue(set.isEmpty())
    set.tailSet(minusOneItem, false)
  }

  private def dset0(): ju.NavigableSet[Item] = {
    val set = new ju.TreeSet[Item]()
    assertTrue(set.isEmpty())
    set
  }

  @Test def testConstructor1(): Unit =
    mustEqual(0, set0().size())

  @Test def testEmpty(): Unit = {
    val q = set0()
    assertTrue(q.isEmpty())
    assertTrue(q.add(oneItem))
    assertFalse(q.isEmpty())
    assertTrue(q.add(twoItem))
    q.pollFirst()
    q.pollFirst()
    assertTrue(q.isEmpty())
  }

  @Test def testSize(): Unit =
    checkSize(populatedSet(SIZE))

  @Test def testAddNull(): Unit =
    assertThrows(classOf[NullPointerException], set0().add(null))

  @Test def testAdd(): Unit =
    assertTrue(set0().add(sixItem))

  @Test def testAddDup(): Unit = {
    val q = set0()
    assertTrue(q.add(sixItem))
    assertFalse(q.add(sixItem))
  }

  @Test def testAddNonComparable(): Unit = {
    val q = new ju.TreeSet[Object]()
    assertThrows(
      classOf[ClassCastException], {
        q.add(new Object())
        q.add(new Object())
      }
    )
  }

  @Test def testAddAll1(): Unit =
    assertThrows(classOf[NullPointerException], set0().addAll(null))

  @Test def testAddAll2(): Unit = {
    val items = new Array[Item](2)
    assertThrows(
      classOf[NullPointerException],
      set0().addAll(ju.Arrays.asList(items: _*))
    )
  }

  @Test def testAddAll3(): Unit = {
    val items = new Array[Item](2)
    items(0) = zeroItem
    assertThrows(
      classOf[NullPointerException],
      set0().addAll(ju.Arrays.asList(items: _*))
    )
  }

  @Test def testAddAll5(): Unit =
    checkAddAll5(set0())

  @Test def testPoll(): Unit =
    checkPoll(populatedSet(SIZE))

  @Test def testRemoveElement(): Unit =
    checkRemoveElement(populatedSet(SIZE), verifyContains = true)

  @Test def testContains(): Unit = {
    val q = populatedSet(SIZE)
    var i = 0
    while (i < SIZE) {
      mustContain(q, i)
      mustNotContain(q, q.pollFirst())
      i += 1
    }
  }

  @Test def testClear(): Unit =
    checkClear(populatedSet(SIZE), oneItem)

  @Test def testContainsAll(): Unit =
    checkContainsAll(populatedSet(SIZE), set0())

  @Test def testRetainAll(): Unit =
    checkRetainAll(populatedSet(SIZE), populatedSet(SIZE))

  @Test def testRemoveAll(): Unit =
    checkRemoveAll()

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
    for (o <- a) assertSame(o, q.pollFirst())
    assertTrue(q.isEmpty())
  }

  @Test def testToArray2(): Unit = {
    val q = populatedSet(SIZE)
    val items = seqItems(SIZE)
    val array = q.toArray(items)
    assertSame(items, array)
    for (o <- items) assertSame(o, q.pollFirst())
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

  @Test def testEmptyIterator(): Unit =
    assertIteratorExhausted(set0().iterator())

  @Test def testIteratorRemove(): Unit =
    checkIteratorRemove(set0())

  @Test def testToString(): Unit =
    checkToString(populatedSet(SIZE).toString())

  @Ignore("scala-native: ObjectInputStream/ObjectOutputStream are unsupported")
  @Test def testSerialization(): Unit = ()

  @Test def testSubSetContents(): Unit =
    checkSubSetContents(set5(), twoItem, fourItem, oneItem, threeItem, fiveItem)

  @Test def testSubSetContents2(): Unit =
    checkSubSetContents2(
      set5(),
      twoItem,
      threeItem,
      oneItem,
      fourItem,
      fiveItem
    )

  @Test def testHeadSetContents(): Unit =
    checkHeadSetContents(
      set5(),
      fourItem,
      Array(oneItem, twoItem, threeItem),
      fiveItem
    )

  @Test def testTailSetContents(): Unit =
    checkTailSetContents(
      set5(),
      twoItem,
      fourItem,
      Array(twoItem, threeItem, fourItem, fiveItem),
      oneItem
    )

  @Test def testDescendingSize(): Unit =
    checkSize(populatedSet(SIZE))

  @Test def testDescendingAdd(): Unit =
    assertTrue(dset0().add(minusSixItem))

  @Test def testDescendingAddDup(): Unit = {
    val q = dset0()
    assertTrue(q.add(minusSixItem))
    assertFalse(q.add(minusSixItem))
  }

  @Test def testDescendingAddNonComparable(): Unit = {
    val q = new ju.TreeSet[Object]()
    assertThrows(
      classOf[ClassCastException], {
        q.add(new Object())
        q.add(new Object())
      }
    )
  }

  @Test def testDescendingAddAll1(): Unit =
    assertThrows(classOf[NullPointerException], dset0().addAll(null))

  @Test def testDescendingAddAll2(): Unit = {
    val items = new Array[Item](2)
    items(0) = zeroItem
    assertThrows(
      classOf[NullPointerException],
      dset0().addAll(ju.Arrays.asList(items: _*))
    )
  }

  @Test def testDescendingAddAll3(): Unit = {
    val items = new Array[Item](2)
    items(0) = ninetyNineItem
    assertThrows(
      classOf[NullPointerException],
      dset0().addAll(ju.Arrays.asList(items: _*))
    )
  }

  @Test def testDescendingAddAll5(): Unit =
    checkAddAll5(dset0())

  @Test def testDescendingPoll(): Unit =
    checkPoll(populatedSet(SIZE))

  @Test def testDescendingRemoveElement(): Unit =
    checkRemoveElement(populatedSet(SIZE), verifyContains = false)

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

  @Test def testDescendingClear(): Unit =
    checkClear(populatedSet(SIZE), oneItem)

  @Test def testDescendingContainsAll(): Unit =
    checkContainsAll(populatedSet(SIZE), dset0())

  @Test def testDescendingRetainAll(): Unit =
    checkRetainAll(populatedSet(SIZE), populatedSet(SIZE))

  @Test def testDescendingRemoveAll(): Unit =
    checkRemoveAll()

  @Test def testDescendingLower(): Unit = {
    val q = dset5()
    mustEqual(minusTwoItem, q.lower(minusThreeItem))
    mustEqual(minusFiveItem, q.lower(minusSixItem))
    assertNull(q.lower(minusOneItem))
    assertNull(q.lower(zeroItem))
  }

  @Test def testDescendingHigher(): Unit = {
    val q = dset5()
    mustEqual(minusFourItem, q.higher(minusThreeItem))
    mustEqual(minusOneItem, q.higher(zeroItem))
    assertNull(q.higher(minusFiveItem))
    assertNull(q.higher(minusSixItem))
  }

  @Test def testDescendingFloor(): Unit = {
    val q = dset5()
    mustEqual(minusThreeItem, q.floor(minusThreeItem))
    mustEqual(minusFiveItem, q.floor(minusSixItem))
    mustEqual(minusOneItem, q.floor(minusOneItem))
    assertNull(q.floor(zeroItem))
  }

  @Test def testDescendingCeiling(): Unit = {
    val q = dset5()
    mustEqual(minusThreeItem, q.ceiling(minusThreeItem))
    mustEqual(minusOneItem, q.ceiling(zeroItem))
    mustEqual(minusFiveItem, q.ceiling(minusFiveItem))
    assertNull(q.ceiling(minusSixItem))
  }

  @Test def testDescendingToArray(): Unit = {
    val q = populatedSet(SIZE)
    val a = q.toArray()
    ju.Arrays.sort(a.asInstanceOf[Array[Object]])
    for (o <- a) mustEqual(o, q.pollFirst())
  }

  @Test def testDescendingToArray2(): Unit = {
    val q = populatedSet(SIZE)
    val items = new Array[Item](SIZE)
    assertSame(items, q.toArray(items))
    ju.Arrays.sort(items.asInstanceOf[Array[Object]])
    for (o <- items) mustEqual(o, q.pollFirst())
  }

  @Test def testDescendingIterator(): Unit = {
    val q = populatedSet(SIZE)
    val it = q.iterator()
    var i = 0
    while (it.hasNext()) {
      mustContain(q, it.next())
      i += 1
    }
    mustEqual(i, SIZE)
  }

  @Test def testDescendingEmptyIterator(): Unit = {
    val it = dset0().iterator()
    var i = 0
    while (it.hasNext()) {
      mustContain(dset0(), it.next())
      i += 1
    }
    mustEqual(0, i)
  }

  @Test def testDescendingIteratorRemove(): Unit =
    checkIteratorRemove(dset0())

  @Test def testDescendingToString(): Unit =
    checkToString(populatedSet(SIZE).toString())

  @Ignore("scala-native: ObjectInputStream/ObjectOutputStream are unsupported")
  @Test def testDescendingSerialization(): Unit = ()

  @Test def testDescendingSubSetContents(): Unit =
    checkSubSetContents(
      dset5(),
      minusTwoItem,
      minusFourItem,
      minusOneItem,
      minusThreeItem,
      minusFiveItem
    )

  @Test def testDescendingSubSetContents2(): Unit =
    checkSubSetContents2(
      dset5(),
      minusTwoItem,
      minusThreeItem,
      minusOneItem,
      minusFourItem,
      minusFiveItem
    )

  @Test def testDescendingHeadSetContents(): Unit =
    checkHeadSetContents(
      dset5(),
      minusFourItem,
      Array(minusOneItem, minusTwoItem, minusThreeItem),
      minusFiveItem
    )

  @Test def testDescendingTailSetContents(): Unit =
    checkTailSetContents(
      dset5(),
      minusTwoItem,
      minusFourItem,
      Array(minusTwoItem, minusThreeItem, minusFourItem, minusFiveItem),
      minusOneItem
    )

  @Test def testAddAll_idempotent(): Unit = {
    val x: ju.Set[Item] = populatedSet(SIZE)
    val y = new ju.TreeSet[Item](x)
    y.addAll(x)
    mustEqual(x, y)
    mustEqual(y, x)
  }

  private def checkSize(q: ju.NavigableSet[Item]): Unit = {
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

  private def checkAddAll5(q: ju.NavigableSet[Item]): Unit = {
    val empty = new Array[Item](0)
    val items = new Array[Item](SIZE)
    var i = 0
    while (i < SIZE) {
      items(i) = itemFor(SIZE - 1 - i)
      i += 1
    }
    assertFalse(q.addAll(ju.Arrays.asList(empty: _*)))
    assertTrue(q.addAll(ju.Arrays.asList(items: _*)))
    i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
  }

  private def checkPoll(q: ju.NavigableSet[Item]): Unit = {
    var i = 0
    while (i < SIZE) {
      mustEqual(i, q.pollFirst())
      i += 1
    }
    assertNull(q.pollFirst())
  }

  private def checkRemoveElement(
      q: ju.NavigableSet[Item],
      verifyContains: Boolean
  ): Unit = {
    var i = 1
    while (i < SIZE) {
      if (verifyContains) mustContain(q, i)
      mustRemove(q, i)
      if (verifyContains) {
        mustNotContain(q, i)
        mustContain(q, i - 1)
      }
      i += 2
    }
    i = 0
    while (i < SIZE) {
      if (verifyContains) mustContain(q, i)
      mustRemove(q, i)
      if (verifyContains) mustNotContain(q, i)
      mustNotRemove(q, i + 1)
      if (verifyContains) mustNotContain(q, i + 1)
      i += 2
    }
    assertTrue(q.isEmpty())
  }

  private def checkClear(q: ju.NavigableSet[Item], item: Item): Unit = {
    q.clear()
    assertTrue(q.isEmpty())
    mustEqual(0, q.size())
    mustAdd(q, item)
    assertFalse(q.isEmpty())
    q.clear()
    assertTrue(q.isEmpty())
  }

  private def checkContainsAll(
      q: ju.NavigableSet[Item],
      p: ju.NavigableSet[Item]
  ): Unit = {
    var i = 0
    while (i < SIZE) {
      assertTrue(q.containsAll(p))
      assertFalse(p.containsAll(q))
      mustAdd(p, i)
      i += 1
    }
    assertTrue(p.containsAll(q))
  }

  private def checkRetainAll(
      q: ju.NavigableSet[Item],
      p: ju.NavigableSet[Item]
  ): Unit = {
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

  private def checkRemoveAll(): Unit = {
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

  private def checkIteratorRemove(q: ju.NavigableSet[Item]): Unit = {
    q.add(twoItem)
    q.add(oneItem)
    q.add(threeItem)

    var it = q.iterator()
    it.next()
    it.remove()

    it = q.iterator()
    mustEqual(twoItem, it.next())
    mustEqual(threeItem, it.next())
    assertFalse(it.hasNext())
  }

  private def checkToString(s: String): Unit = {
    var i = 0
    while (i < SIZE) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  private def checkOrder(
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

  private def checkSubSetContents(
      set: ju.NavigableSet[Item],
      from: Item,
      to: Item,
      excludedLow: Item,
      remaining: Item,
      excludedHigh: Item
  ): Unit = {
    val sm = set.subSet(from, to)
    mustEqual(from, sm.first())
    mustEqual(remaining, sm.last())
    mustEqual(2, sm.size())
    mustNotContain(sm, excludedLow)
    mustContain(sm, from)
    mustContain(sm, remaining)
    mustNotContain(sm, to)
    mustNotContain(sm, excludedHigh)
    checkOrder(sm.iterator(), Array(from, remaining))

    val j = sm.iterator()
    j.next()
    j.remove()
    mustNotContain(set, from)
    mustEqual(4, set.size())
    mustEqual(1, sm.size())
    mustEqual(remaining, sm.first())
    mustEqual(remaining, sm.last())
    mustRemove(sm, remaining)
    assertTrue(sm.isEmpty())
    mustEqual(3, set.size())
  }

  private def checkSubSetContents2(
      set: ju.NavigableSet[Item],
      from: Item,
      to: Item,
      excludedLow: Item,
      excludedMid: Item,
      excludedHigh: Item
  ): Unit = {
    val sm = set.subSet(from, to)
    mustEqual(1, sm.size())
    mustEqual(from, sm.first())
    mustEqual(from, sm.last())
    mustNotContain(sm, excludedLow)
    mustContain(sm, from)
    mustNotContain(sm, to)
    mustNotContain(sm, excludedMid)
    mustNotContain(sm, excludedHigh)
    checkOrder(sm.iterator(), Array(from))

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
      set: ju.NavigableSet[Item],
      to: Item,
      expected: Array[Item],
      excludedHigh: Item
  ): Unit = {
    val sm = set.headSet(to)
    var i = 0
    while (i < expected.length) {
      mustContain(sm, expected(i))
      i += 1
    }
    mustNotContain(sm, to)
    mustNotContain(sm, excludedHigh)
    checkOrder(sm.iterator(), expected)
    sm.clear()
    assertTrue(sm.isEmpty())
    mustEqual(2, set.size())
    mustEqual(to, set.first())
  }

  private def checkTailSetContents(
      set: ju.NavigableSet[Item],
      from: Item,
      subTailFrom: Item,
      expected: Array[Item],
      excludedLow: Item
  ): Unit = {
    val sm = set.tailSet(from)
    mustNotContain(sm, excludedLow)
    var i = 0
    while (i < expected.length) {
      mustContain(sm, expected(i))
      i += 1
    }
    checkOrder(sm.iterator(), expected)

    val ssm = sm.tailSet(subTailFrom)
    mustEqual(subTailFrom, ssm.first())
    mustEqual(expected.last, ssm.last())
    mustRemove(ssm, subTailFrom)
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, set.size())
  }
}
