package org.scalanative.testsuite.javalib.util

import java.{util => ju}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class TreeSubMapTest extends JSR166Test {
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
  private val minusTenItem = itemFor(-10)

  private def map5(): ju.NavigableMap[Item, String] = {
    val map = new ju.TreeMap[Item, String]()
    assertTrue(map.isEmpty())
    map.put(zeroItem, "Z")
    map.put(oneItem, "A")
    map.put(fiveItem, "E")
    map.put(threeItem, "C")
    map.put(twoItem, "B")
    map.put(fourItem, "D")
    map.put(sevenItem, "F")
    assertFalse(map.isEmpty())
    mustEqual(7, map.size())
    map.subMap(oneItem, true, sevenItem, false)
  }

  private def map0(): ju.NavigableMap[Item, String] = {
    val map = new ju.TreeMap[Item, String]()
    assertTrue(map.isEmpty())
    map.tailMap(oneItem, true)
  }

  private def dmap5(): ju.NavigableMap[Item, String] = {
    val map = new ju.TreeMap[Item, String]()
    assertTrue(map.isEmpty())
    map.put(minusOneItem, "A")
    map.put(minusFiveItem, "E")
    map.put(minusThreeItem, "C")
    map.put(minusTwoItem, "B")
    map.put(minusFourItem, "D")
    assertFalse(map.isEmpty())
    mustEqual(5, map.size())
    map.descendingMap()
  }

  private def dmap0(): ju.NavigableMap[Item, String] = {
    val map = new ju.TreeMap[Item, String]()
    assertTrue(map.isEmpty())
    map
  }

  @Test def testClear(): Unit = {
    val map = map5()
    map.clear()
    mustEqual(0, map.size())
  }

  @Test def testEquals(): Unit = {
    val map1 = map5()
    val map2 = map5()
    mustEqual(map1, map2)
    mustEqual(map2, map1)
    map1.clear()
    assertFalse(map1.equals(map2))
    assertFalse(map2.equals(map1))
  }

  @Test def testContainsKey(): Unit = {
    val map = map5()
    assertTrue(map.containsKey(oneItem))
    assertFalse(map.containsKey(zeroItem))
  }

  @Test def testContainsValue(): Unit = {
    val map = map5()
    assertTrue(map.containsValue("A"))
    assertFalse(map.containsValue("Z"))
  }

  @Test def testGet(): Unit = {
    mustEqual("A", map5().get(oneItem))
    assertNull(map0().get(oneItem))
  }

  @Test def testIsEmpty(): Unit = {
    assertTrue(map0().isEmpty())
    assertFalse(map5().isEmpty())
  }

  @Test def testFirstKey(): Unit =
    mustEqual(oneItem, map5().firstKey())

  @Test def testLastKey(): Unit =
    mustEqual(fiveItem, map5().lastKey())

  @Test def testKeySet(): Unit =
    checkKeySet(
      map5().keySet(),
      Array(oneItem, twoItem, threeItem, fourItem, fiveItem)
    )

  @Test def testKeySetOrder(): Unit =
    checkKeyOrder(
      map5().keySet().iterator(),
      Array(oneItem, twoItem, threeItem, fourItem, fiveItem)
    )

  @Test def testValues(): Unit =
    checkValues(map5().values())

  @Test def testEntrySet(): Unit =
    checkEntrySet(
      map5().entrySet(),
      Array(
        oneItem -> "A",
        twoItem -> "B",
        threeItem -> "C",
        fourItem -> "D",
        fiveItem -> "E"
      )
    )

  @Test def testPutAll(): Unit = {
    val empty = map0()
    empty.putAll(map5())
    mustEqual(5, empty.size())
    assertTrue(empty.containsKey(oneItem))
    assertTrue(empty.containsKey(twoItem))
    assertTrue(empty.containsKey(threeItem))
    assertTrue(empty.containsKey(fourItem))
    assertTrue(empty.containsKey(fiveItem))
  }

  @Test def testRemove(): Unit = {
    val map = map5()
    map.remove(fiveItem)
    mustEqual(4, map.size())
    assertFalse(map.containsKey(fiveItem))
  }

  @Test def testLowerEntry(): Unit = {
    val map = map5()
    mustEqual(twoItem, map.lowerEntry(threeItem).getKey())
    mustEqual(fiveItem, map.lowerEntry(sixItem).getKey())
    assertNull(map.lowerEntry(oneItem))
    assertNull(map.lowerEntry(zeroItem))
  }

  @Test def testHigherEntry(): Unit = {
    val map = map5()
    mustEqual(fourItem, map.higherEntry(threeItem).getKey())
    mustEqual(oneItem, map.higherEntry(zeroItem).getKey())
    assertNull(map.higherEntry(fiveItem))
    assertNull(map.higherEntry(sixItem))
  }

  @Test def testFloorEntry(): Unit = {
    val map = map5()
    mustEqual(threeItem, map.floorEntry(threeItem).getKey())
    mustEqual(fiveItem, map.floorEntry(sixItem).getKey())
    mustEqual(oneItem, map.floorEntry(oneItem).getKey())
    assertNull(map.floorEntry(zeroItem))
  }

  @Test def testCeilingEntry(): Unit = {
    val map = map5()
    mustEqual(threeItem, map.ceilingEntry(threeItem).getKey())
    mustEqual(oneItem, map.ceilingEntry(zeroItem).getKey())
    mustEqual(fiveItem, map.ceilingEntry(fiveItem).getKey())
    assertNull(map.ceilingEntry(sixItem))
  }

  @Test def testPollFirstEntry(): Unit = {
    val map = map5()
    var e = map.pollFirstEntry()
    mustEqual(oneItem, e.getKey())
    mustEqual("A", e.getValue())
    e = map.pollFirstEntry()
    mustEqual(twoItem, e.getKey())
    map.put(oneItem, "A")
    e = map.pollFirstEntry()
    mustEqual(oneItem, e.getKey())
    mustEqual("A", e.getValue())
    e = map.pollFirstEntry()
    mustEqual(threeItem, e.getKey())
    map.remove(fourItem)
    e = map.pollFirstEntry()
    mustEqual(fiveItem, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("A"))
    assertTrue(map.isEmpty())
    assertNull(map.firstEntry())
    assertNull(map.pollFirstEntry())
  }

  @Test def testPollLastEntry(): Unit = {
    val map = map5()
    var e = map.pollLastEntry()
    mustEqual(fiveItem, e.getKey())
    mustEqual("E", e.getValue())
    e = map.pollLastEntry()
    mustEqual(fourItem, e.getKey())
    map.put(fiveItem, "E")
    e = map.pollLastEntry()
    mustEqual(fiveItem, e.getKey())
    mustEqual("E", e.getValue())
    e = map.pollLastEntry()
    mustEqual(threeItem, e.getKey())
    map.remove(twoItem)
    e = map.pollLastEntry()
    mustEqual(oneItem, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("E"))
    assertNull(map.pollLastEntry())
  }

  @Test def testSize(): Unit = {
    mustEqual(0, map0().size())
    mustEqual(5, map5().size())
  }

  @Test def testToString(): Unit =
    checkToString(map5().toString(), 1, 5)

  @Test def testGet_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().get(null))

  @Test def testContainsKey_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().containsKey(null))

  @Test def testPut1_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().put(null, "whatever"))

  @Test def testRemove1_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().remove(null))

  @Ignore("scala-native: ObjectInputStream/ObjectOutputStream are unsupported")
  @Test def testSerialization(): Unit = ()

  @Test def testSubMapContents(): Unit =
    checkSubMapContents(map5(), twoItem, fourItem, oneItem, threeItem, fiveItem)

  @Test def testSubMapContents2(): Unit =
    checkSubMapContents2(
      map5(),
      twoItem,
      threeItem,
      oneItem,
      fourItem,
      fiveItem
    )

  @Test def testHeadMapContents(): Unit =
    checkHeadMapContents(
      map5(),
      fourItem,
      Array(oneItem, twoItem, threeItem),
      fiveItem
    )

  @Test def testTailMapContents(): Unit =
    checkTailMapContents(
      map5(),
      twoItem,
      fourItem,
      Array(twoItem -> "B", threeItem -> "C", fourItem -> "D", fiveItem -> "E"),
      oneItem
    )

  @Test def testDescendingClear(): Unit = {
    val map = dmap5()
    map.clear()
    mustEqual(0, map.size())
  }

  @Test def testDescendingEquals(): Unit = {
    val map1 = dmap5()
    val map2 = dmap5()
    mustEqual(map1, map2)
    mustEqual(map2, map1)
    map1.clear()
    assertFalse(map1.equals(map2))
    assertFalse(map2.equals(map1))
  }

  @Test def testDescendingContainsKey(): Unit = {
    val map = dmap5()
    assertTrue(map.containsKey(minusOneItem))
    assertFalse(map.containsKey(zeroItem))
  }

  @Test def testDescendingContainsValue(): Unit = {
    val map = dmap5()
    assertTrue(map.containsValue("A"))
    assertFalse(map.containsValue("Z"))
  }

  @Test def testDescendingGet(): Unit = {
    mustEqual("A", dmap5().get(minusOneItem))
    assertNull(dmap0().get(minusOneItem))
  }

  @Test def testDescendingIsEmpty(): Unit = {
    assertTrue(dmap0().isEmpty())
    assertFalse(dmap5().isEmpty())
  }

  @Test def testDescendingFirstKey(): Unit =
    mustEqual(minusOneItem, dmap5().firstKey())

  @Test def testDescendingLastKey(): Unit =
    mustEqual(minusFiveItem, dmap5().lastKey())

  @Test def testDescendingKeySet(): Unit =
    checkKeySet(
      dmap5().keySet(),
      Array(
        minusOneItem,
        minusTwoItem,
        minusThreeItem,
        minusFourItem,
        minusFiveItem
      )
    )

  @Test def testDescendingKeySetOrder(): Unit =
    checkKeyOrder(
      dmap5().keySet().iterator(),
      Array(
        minusOneItem,
        minusTwoItem,
        minusThreeItem,
        minusFourItem,
        minusFiveItem
      )
    )

  @Test def testDescendingValues(): Unit =
    checkValues(dmap5().values())

  @Test def testDescendingAscendingKeySetToArray(): Unit = {
    val s = dmap5().keySet()
    val ar = s.toArray(new Array[Item](0))
    assertTrue(s.containsAll(ju.Arrays.asList(ar: _*)))
    mustEqual(5, ar.length)
    ar(0) = minusTenItem
    assertFalse(s.containsAll(ju.Arrays.asList(ar: _*)))
  }

  @Test def testDescendingDescendingKeySetToArray(): Unit = {
    val s = dmap5().descendingKeySet()
    val ar = s.toArray(new Array[Item](0))
    mustEqual(5, ar.length)
    assertTrue(s.containsAll(ju.Arrays.asList(ar: _*)))
    ar(0) = minusTenItem
    assertFalse(s.containsAll(ju.Arrays.asList(ar: _*)))
  }

  @Test def testDescendingValuesToArray(): Unit = {
    val v = dmap5().values()
    val ar = v.toArray(new Array[String](0))
    val s = new ju.ArrayList[String](ju.Arrays.asList(ar: _*))
    mustEqual(5, ar.length)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  @Test def testDescendingEntrySet(): Unit =
    checkEntrySet(
      dmap5().entrySet(),
      Array(
        minusOneItem -> "A",
        minusTwoItem -> "B",
        minusThreeItem -> "C",
        minusFourItem -> "D",
        minusFiveItem -> "E"
      )
    )

  @Test def testDescendingPutAll(): Unit = {
    val empty = dmap0()
    empty.putAll(dmap5())
    mustEqual(5, empty.size())
    assertTrue(empty.containsKey(minusOneItem))
    assertTrue(empty.containsKey(minusTwoItem))
    assertTrue(empty.containsKey(minusThreeItem))
    assertTrue(empty.containsKey(minusFourItem))
    assertTrue(empty.containsKey(minusFiveItem))
  }

  @Test def testDescendingRemove(): Unit = {
    val map = dmap5()
    map.remove(minusFiveItem)
    mustEqual(4, map.size())
    assertFalse(map.containsKey(minusFiveItem))
  }

  @Test def testDescendingLowerEntry(): Unit = {
    val map = dmap5()
    mustEqual(minusTwoItem, map.lowerEntry(minusThreeItem).getKey())
    mustEqual(minusFiveItem, map.lowerEntry(minusSixItem).getKey())
    assertNull(map.lowerEntry(minusOneItem))
    assertNull(map.lowerEntry(zeroItem))
  }

  @Test def testDescendingHigherEntry(): Unit = {
    val map = dmap5()
    mustEqual(minusFourItem, map.higherEntry(minusThreeItem).getKey())
    mustEqual(minusOneItem, map.higherEntry(zeroItem).getKey())
    assertNull(map.higherEntry(minusFiveItem))
    assertNull(map.higherEntry(minusSixItem))
  }

  @Test def testDescendingFloorEntry(): Unit = {
    val map = dmap5()
    mustEqual(minusThreeItem, map.floorEntry(minusThreeItem).getKey())
    mustEqual(minusFiveItem, map.floorEntry(minusSixItem).getKey())
    mustEqual(minusOneItem, map.floorEntry(minusOneItem).getKey())
    assertNull(map.floorEntry(zeroItem))
  }

  @Test def testDescendingCeilingEntry(): Unit = {
    val map = dmap5()
    mustEqual(minusThreeItem, map.ceilingEntry(minusThreeItem).getKey())
    mustEqual(minusOneItem, map.ceilingEntry(zeroItem).getKey())
    mustEqual(minusFiveItem, map.ceilingEntry(minusFiveItem).getKey())
    assertNull(map.ceilingEntry(minusSixItem))
  }

  @Test def testDescendingPollFirstEntry(): Unit = {
    val map = dmap5()
    var e = map.pollFirstEntry()
    mustEqual(minusOneItem, e.getKey())
    mustEqual("A", e.getValue())
    e = map.pollFirstEntry()
    mustEqual(minusTwoItem, e.getKey())
    map.put(minusOneItem, "A")
    e = map.pollFirstEntry()
    mustEqual(minusOneItem, e.getKey())
    mustEqual("A", e.getValue())
    e = map.pollFirstEntry()
    mustEqual(minusThreeItem, e.getKey())
    map.remove(minusFourItem)
    e = map.pollFirstEntry()
    mustEqual(minusFiveItem, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("A"))
    assertNull(map.pollFirstEntry())
  }

  @Test def testDescendingPollLastEntry(): Unit = {
    val map = dmap5()
    var e = map.pollLastEntry()
    mustEqual(minusFiveItem, e.getKey())
    mustEqual("E", e.getValue())
    e = map.pollLastEntry()
    mustEqual(minusFourItem, e.getKey())
    map.put(minusFiveItem, "E")
    e = map.pollLastEntry()
    mustEqual(minusFiveItem, e.getKey())
    mustEqual("E", e.getValue())
    e = map.pollLastEntry()
    mustEqual(minusThreeItem, e.getKey())
    map.remove(minusTwoItem)
    e = map.pollLastEntry()
    mustEqual(minusOneItem, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("E"))
    assertNull(map.pollLastEntry())
  }

  @Test def testDescendingSize(): Unit = {
    mustEqual(0, dmap0().size())
    mustEqual(5, dmap5().size())
  }

  @Test def testDescendingToString(): Unit =
    checkToString(dmap5().toString(), 1, 5)

  @Test def testDescendingGet_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], dmap5().get(null))

  @Test def testDescendingPut1_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], dmap5().put(null, "whatever"))

  @Ignore("scala-native: ObjectInputStream/ObjectOutputStream are unsupported")
  @Test def testDescendingSerialization(): Unit = ()

  @Test def testDescendingSubMapContents(): Unit =
    checkSubMapContents(
      dmap5(),
      minusTwoItem,
      minusFourItem,
      minusOneItem,
      minusThreeItem,
      minusFiveItem
    )

  @Test def testDescendingSubMapContents2(): Unit =
    checkSubMapContents2(
      dmap5(),
      minusTwoItem,
      minusThreeItem,
      minusOneItem,
      minusFourItem,
      minusFiveItem
    )

  @Test def testDescendingHeadMapContents(): Unit =
    checkHeadMapContents(
      dmap5(),
      minusFourItem,
      Array(minusOneItem, minusTwoItem, minusThreeItem),
      minusFiveItem
    )

  @Test def testDescendingTailMapContents(): Unit =
    checkTailMapContents(
      dmap5(),
      minusTwoItem,
      minusFourItem,
      Array(
        minusTwoItem -> "B",
        minusThreeItem -> "C",
        minusFourItem -> "D",
        minusFiveItem -> "E"
      ),
      minusOneItem
    )

  private def checkKeySet(s: ju.Set[Item], keys: Array[Item]): Unit = {
    mustEqual(keys.length, s.size())
    var i = 0
    while (i < keys.length) {
      mustContain(s, keys(i))
      i += 1
    }
  }

  private def checkKeyOrder(
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

  private def checkValues(s: ju.Collection[String]): Unit = {
    mustEqual(5, s.size())
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  private def checkEntrySet(
      s: ju.Set[ju.Map.Entry[Item, String]],
      expected: Array[(Item, String)]
  ): Unit = {
    mustEqual(expected.length, s.size())
    val it = s.iterator()
    while (it.hasNext()) {
      val e = it.next()
      assertTrue(expected.exists {
        case (k, v) =>
          e.getKey().equals(k) && e.getValue() == v
      })
    }
  }

  private def checkToString(s: String, from: Int, to: Int): Unit = {
    var i = from
    while (i <= to) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  private def checkSubMapContents(
      map: ju.NavigableMap[Item, String],
      from: Item,
      to: Item,
      excludedLow: Item,
      remaining: Item,
      excludedHigh: Item
  ): Unit = {
    val sm = map.subMap(from, to)
    mustEqual(from, sm.firstKey())
    mustEqual(remaining, sm.lastKey())
    mustEqual(2, sm.size())
    assertFalse(sm.containsKey(excludedLow))
    assertTrue(sm.containsKey(from))
    assertTrue(sm.containsKey(remaining))
    assertFalse(sm.containsKey(to))
    assertFalse(sm.containsKey(excludedHigh))
    checkKeyOrder(sm.keySet().iterator(), Array(from, remaining))

    val j = sm.keySet().iterator()
    j.next()
    j.remove()
    assertFalse(map.containsKey(from))
    mustEqual(4, map.size())
    mustEqual(1, sm.size())
    mustEqual(remaining, sm.firstKey())
    mustEqual(remaining, sm.lastKey())
    mustEqual("C", sm.remove(remaining))
    assertTrue(sm.isEmpty())
    mustEqual(3, map.size())
  }

  private def checkSubMapContents2(
      map: ju.NavigableMap[Item, String],
      from: Item,
      to: Item,
      excludedLow: Item,
      excludedMid: Item,
      excludedHigh: Item
  ): Unit = {
    val sm = map.subMap(from, to)
    mustEqual(1, sm.size())
    mustEqual(from, sm.firstKey())
    mustEqual(from, sm.lastKey())
    assertFalse(sm.containsKey(excludedLow))
    assertTrue(sm.containsKey(from))
    assertFalse(sm.containsKey(to))
    assertFalse(sm.containsKey(excludedMid))
    assertFalse(sm.containsKey(excludedHigh))
    checkKeyOrder(sm.keySet().iterator(), Array(from))

    val j = sm.keySet().iterator()
    j.next()
    j.remove()
    assertFalse(map.containsKey(from))
    mustEqual(4, map.size())
    mustEqual(0, sm.size())
    assertTrue(sm.isEmpty())
    assertNull(sm.remove(to))
    mustEqual(4, map.size())
  }

  private def checkHeadMapContents(
      map: ju.NavigableMap[Item, String],
      to: Item,
      expected: Array[Item],
      excludedHigh: Item
  ): Unit = {
    val sm = map.headMap(to)
    var i = 0
    while (i < expected.length) {
      assertTrue(sm.containsKey(expected(i)))
      i += 1
    }
    assertFalse(sm.containsKey(to))
    assertFalse(sm.containsKey(excludedHigh))
    checkKeyOrder(sm.keySet().iterator(), expected)
    sm.clear()
    assertTrue(sm.isEmpty())
    mustEqual(2, map.size())
    mustEqual(to, map.firstKey())
  }

  private def checkTailMapContents(
      map: ju.NavigableMap[Item, String],
      from: Item,
      subTailFrom: Item,
      expectedEntries: Array[(Item, String)],
      excludedLow: Item
  ): Unit = {
    val sm = map.tailMap(from)
    assertFalse(sm.containsKey(excludedLow))
    var i = 0
    while (i < expectedEntries.length) {
      assertTrue(sm.containsKey(expectedEntries(i)._1))
      i += 1
    }
    checkKeyOrder(sm.keySet().iterator(), expectedEntries.map(_._1))

    val ei = sm.entrySet().iterator()
    i = 0
    while (i < expectedEntries.length) {
      val e = ei.next()
      mustEqual(expectedEntries(i)._1, e.getKey())
      mustEqual(expectedEntries(i)._2, e.getValue())
      i += 1
    }
    assertFalse(ei.hasNext())

    val ssm = sm.tailMap(subTailFrom)
    mustEqual(subTailFrom, ssm.firstKey())
    mustEqual(expectedEntries.last._1, ssm.lastKey())
    mustEqual(
      expectedEntries(expectedEntries.length - 2)._2,
      ssm.remove(subTailFrom)
    )
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, map.size())
  }
}
