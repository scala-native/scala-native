// Ported from Scala.js commit def516f dated: 2023-01-22

package org.scalanative.testsuite.javalib.util

import java.util.function.{BiConsumer, BiFunction, Function}
import java.{util => ju}

import scala.reflect.ClassTag

import org.junit.Assert._
import org.junit.Assume._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.javalib.util.concurrent.ConcurrentMapFactory
import org.scalanative.testsuite.javalib.util.concurrent.{Item, JSR166Test}
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform._

import Utils._

abstract class TreeMapTest(val factory: TreeMapFactory)
    extends AbstractMapTest
    with NavigableMapTest {
  import JSR166Test._

  private val zeroItem = itemFor(0)
  private val oneItem = itemFor(1)
  private val twoItem = itemFor(2)
  private val threeItem = itemFor(3)
  private val fourItem = itemFor(4)
  private val fiveItem = itemFor(5)
  private val sixItem = itemFor(6)
  private val minusTenItem = itemFor(-10)

  private val rnd = new ju.Random(666L)
  private var bs: ju.BitSet = _

  private def map5(): ju.TreeMap[Item, String] = {
    val map = new ju.TreeMap[Item, String]()
    assertTrue(map.isEmpty())
    map.put(oneItem, "A")
    map.put(fiveItem, "E")
    map.put(threeItem, "C")
    map.put(twoItem, "B")
    map.put(fourItem, "D")
    assertFalse(map.isEmpty())
    mustEqual(5, map.size())
    map
  }

  @Test override def testClear(): Unit = {
    val map = map5()
    map.clear()
    mustEqual(0, map.size())
  }

  @Test def testConstructFromSorted(): Unit = {
    val map = map5()
    val map2 = new ju.TreeMap[Item, String](map)
    mustEqual(map, map2)
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

  @Test override def testContainsKey(): Unit = {
    val map = map5()
    assertTrue(map.containsKey(oneItem))
    assertFalse(map.containsKey(zeroItem))
  }

  @Test override def testContainsValue(): Unit = {
    val map = map5()
    assertTrue(map.containsValue("A"))
    assertFalse(map.containsValue("Z"))
  }

  @Test def testGet(): Unit = {
    val map = map5()
    mustEqual("A", map.get(oneItem))
    assertNull(new ju.TreeMap[Item, String]().get(oneItem))
  }

  @Test def testIsEmpty(): Unit = {
    assertTrue(new ju.TreeMap[Item, String]().isEmpty())
    assertFalse(map5().isEmpty())
  }

  @Test def testFirstKey(): Unit =
    mustEqual(oneItem, map5().firstKey())

  @Test def testLastKey(): Unit =
    mustEqual(fiveItem, map5().lastKey())

  @Test def testKeySetToArray(): Unit = {
    val s = map5().keySet()
    val ar = s.toArray()
    assertTrue(s.containsAll(ju.Arrays.asList(ar: _*)))
    mustEqual(5, ar.length)
    ar(0) = minusTenItem
    assertFalse(s.containsAll(ju.Arrays.asList(ar: _*)))
  }

  @Test def testDescendingKeySetToArray(): Unit = {
    val s = map5().descendingKeySet()
    val ar = s.toArray()
    mustEqual(5, ar.length)
    assertTrue(s.containsAll(ju.Arrays.asList(ar: _*)))
    ar(0) = minusTenItem
    assertFalse(s.containsAll(ju.Arrays.asList(ar: _*)))
  }

  @Test def testKeySet(): Unit = {
    val s = map5().keySet()
    mustEqual(5, s.size())
    mustContain(s, oneItem)
    mustContain(s, twoItem)
    mustContain(s, threeItem)
    mustContain(s, fourItem)
    mustContain(s, fiveItem)
  }

  @Test def testKeySetOrder(): Unit = {
    val it = map5().keySet().iterator()
    var last = it.next()
    mustEqual(last, oneItem)
    var count = 1
    while (it.hasNext()) {
      val k = it.next()
      assertTrue(last.compareTo(k) < 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  @Test def testKeySetDescendingIteratorOrder(): Unit = {
    val it = map5().navigableKeySet().descendingIterator()
    var last = it.next()
    mustEqual(last, fiveItem)
    var count = 1
    while (it.hasNext()) {
      val k = it.next()
      assertTrue(last.compareTo(k) > 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  @Test def testDescendingKeySetOrder(): Unit = {
    val it = map5().descendingKeySet().iterator()
    var last = it.next()
    mustEqual(last, fiveItem)
    var count = 1
    while (it.hasNext()) {
      val k = it.next()
      assertTrue(last.compareTo(k) > 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  @Test def testDescendingKeySetDescendingIteratorOrder(): Unit = {
    val it = map5().descendingKeySet().descendingIterator()
    var last = it.next()
    mustEqual(last, oneItem)
    var count = 1
    while (it.hasNext()) {
      val k = it.next()
      assertTrue(last.compareTo(k) < 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  @Test def testValues(): Unit = {
    val s = map5().values()
    mustEqual(5, s.size())
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  @Test def testEntrySet(): Unit =
    checkEntries(map5().entrySet())

  @Test def testDescendingEntrySet(): Unit =
    checkEntries(map5().descendingMap().entrySet())

  private def checkEntries(s: ju.Set[ju.Map.Entry[Item, String]]): Unit = {
    mustEqual(5, s.size())
    val it = s.iterator()
    while (it.hasNext()) {
      val e = it.next()
      assertTrue(
        (e.getKey().equals(oneItem) && e.getValue() == "A") ||
          (e.getKey().equals(twoItem) && e.getValue() == "B") ||
          (e.getKey().equals(threeItem) && e.getValue() == "C") ||
          (e.getKey().equals(fourItem) && e.getValue() == "D") ||
          (e.getKey().equals(fiveItem) && e.getValue() == "E")
      )
    }
  }

  @Test def testEntrySetToArray(): Unit =
    checkEntrySetToArray(map5().entrySet())

  @Test def testDescendingEntrySetToArray(): Unit =
    checkEntrySetToArray(map5().descendingMap().entrySet())

  private def checkEntrySetToArray(
      s: ju.Set[ju.Map.Entry[Item, String]]
  ): Unit = {
    val map = map5()
    val ar = s.toArray()
    mustEqual(5, ar.length)
    var i = 0
    while (i < 5) {
      val e = ar(i).asInstanceOf[ju.Map.Entry[Item, String]]
      assertTrue(map.containsKey(e.getKey()))
      assertTrue(map.containsValue(e.getValue()))
      i += 1
    }
  }

  @Test override def testPutAll(): Unit = {
    val empty = new ju.TreeMap[Item, String]()
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

  @Test def testLowerKey(): Unit = {
    val q = map5()
    mustEqual(twoItem, q.lowerKey(threeItem))
    mustEqual(fiveItem, q.lowerKey(sixItem))
    assertNull(q.lowerKey(oneItem))
    assertNull(q.lowerKey(zeroItem))
  }

  @Test def testHigherKey(): Unit = {
    val q = map5()
    mustEqual(fourItem, q.higherKey(threeItem))
    mustEqual(oneItem, q.higherKey(zeroItem))
    assertNull(q.higherKey(fiveItem))
    assertNull(q.higherKey(sixItem))
  }

  @Test def testFloorKey(): Unit = {
    val q = map5()
    mustEqual(threeItem, q.floorKey(threeItem))
    mustEqual(fiveItem, q.floorKey(sixItem))
    mustEqual(oneItem, q.floorKey(oneItem))
    assertNull(q.floorKey(zeroItem))
  }

  @Test def testCeilingKey(): Unit = {
    val q = map5()
    mustEqual(threeItem, q.ceilingKey(threeItem))
    mustEqual(oneItem, q.ceilingKey(zeroItem))
    mustEqual(fiveItem, q.ceilingKey(fiveItem))
    assertNull(q.ceilingKey(sixItem))
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
    mustEqual(0, new ju.TreeMap[Item, String]().size())
    mustEqual(5, map5().size())
  }

  @Test def testToString(): Unit = {
    val s = map5().toString()
    var i = 1
    while (i <= 5) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  @Test def testGet_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().get(null))

  @Test def testContainsKey_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().containsKey(null))

  @Test def testRemove1_NullPointerException(): Unit = {
    val c = new ju.TreeMap[Item, String]()
    c.put(oneItem, "asdads")
    assertThrows(classOf[NullPointerException], c.remove(null))
  }

  @Ignore("scala-native: ObjectInputStream/ObjectOutputStream are unsupported")
  @Test def testSerialization(): Unit = ()

  @Test def testSubMapContents(): Unit = {
    val map = map5()
    val sm = map.subMap(twoItem, true, fourItem, false)
    mustEqual(twoItem, sm.firstKey())
    mustEqual(threeItem, sm.lastKey())
    mustEqual(2, sm.size())
    assertFalse(sm.containsKey(oneItem))
    assertTrue(sm.containsKey(twoItem))
    assertTrue(sm.containsKey(threeItem))
    assertFalse(sm.containsKey(fourItem))
    assertFalse(sm.containsKey(fiveItem))
    checkKeyOrder(sm.keySet().iterator(), Array(twoItem, threeItem))
    checkKeyOrder(sm.descendingKeySet().iterator(), Array(threeItem, twoItem))
    val j = sm.keySet().iterator()
    j.next()
    j.remove()
    assertFalse(map.containsKey(twoItem))
    mustEqual(4, map.size())
    mustEqual(1, sm.size())
    mustEqual(threeItem, sm.firstKey())
    mustEqual(threeItem, sm.lastKey())
    mustEqual("C", sm.remove(threeItem))
    assertTrue(sm.isEmpty())
    mustEqual(3, map.size())
  }

  @Test def testSubMapContents2(): Unit = {
    val map = map5()
    val sm = map.subMap(twoItem, true, threeItem, false)
    mustEqual(1, sm.size())
    mustEqual(twoItem, sm.firstKey())
    mustEqual(twoItem, sm.lastKey())
    assertFalse(sm.containsKey(oneItem))
    assertTrue(sm.containsKey(twoItem))
    assertFalse(sm.containsKey(threeItem))
    assertFalse(sm.containsKey(fourItem))
    assertFalse(sm.containsKey(fiveItem))
    checkKeyOrder(sm.keySet().iterator(), Array(twoItem))
    checkKeyOrder(sm.descendingKeySet().iterator(), Array(twoItem))
    val j = sm.keySet().iterator()
    j.next()
    j.remove()
    assertFalse(map.containsKey(twoItem))
    mustEqual(4, map.size())
    mustEqual(0, sm.size())
    assertTrue(sm.isEmpty())
    assertSame(null, sm.remove(threeItem))
    mustEqual(4, map.size())
  }

  @Test def testHeadMapContents(): Unit = {
    val map = map5()
    val sm = map.headMap(fourItem, false)
    assertTrue(sm.containsKey(oneItem))
    assertTrue(sm.containsKey(twoItem))
    assertTrue(sm.containsKey(threeItem))
    assertFalse(sm.containsKey(fourItem))
    assertFalse(sm.containsKey(fiveItem))
    checkKeyOrder(sm.keySet().iterator(), Array(oneItem, twoItem, threeItem))
    sm.clear()
    assertTrue(sm.isEmpty())
    mustEqual(2, map.size())
    mustEqual(fourItem, map.firstKey())
  }

  @Test def testTailMapContents(): Unit = {
    val map = map5()
    val sm = map.tailMap(twoItem, true)
    assertFalse(sm.containsKey(oneItem))
    assertTrue(sm.containsKey(twoItem))
    assertTrue(sm.containsKey(threeItem))
    assertTrue(sm.containsKey(fourItem))
    assertTrue(sm.containsKey(fiveItem))
    checkKeyOrder(
      sm.keySet().iterator(),
      Array(twoItem, threeItem, fourItem, fiveItem)
    )
    checkKeyOrder(
      sm.descendingKeySet().iterator(),
      Array(fiveItem, fourItem, threeItem, twoItem)
    )
    val ei = sm.entrySet().iterator()
    for ((k, v) <- Array(twoItem -> "B", threeItem -> "C", fourItem -> "D", fiveItem -> "E")) {
      val e = ei.next()
      mustEqual(k, e.getKey())
      mustEqual(v, e.getValue())
    }
    assertFalse(ei.hasNext())
    val ssm = sm.tailMap(fourItem, true)
    mustEqual(fourItem, ssm.firstKey())
    mustEqual(fiveItem, ssm.lastKey())
    mustEqual("D", ssm.remove(fourItem))
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, map.size())
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

  @Test def testRecursiveSubMaps(): Unit = {
    val mapSize = if (executingInJVM) 1000 else 100
    val map = new ju.TreeMap[Item, Item]()
    bs = new ju.BitSet(mapSize)
    populate(map, mapSize)
    check(map, 0, mapSize - 1, ascending = true)
    check(map.descendingMap(), 0, mapSize - 1, ascending = false)
    mutateMap(map, 0, mapSize - 1)
    check(map, 0, mapSize - 1, ascending = true)
    check(map.descendingMap(), 0, mapSize - 1, ascending = false)
    bashSubMap(
      map.subMap(zeroItem, true, itemFor(mapSize), false),
      0,
      mapSize - 1,
      ascending = true
    )
  }

  private def populate(map: ju.NavigableMap[Item, Item], limit: Int): Unit = {
    var i = 0
    val n = 2 * limit / 3
    while (i < n) {
      put(map, rnd.nextInt(limit))
      i += 1
    }
  }

  private def mutateMap(map: ju.NavigableMap[Item, Item], min: Int, max: Int): Unit = {
    val size = map.size()
    val rangeSize = max - min + 1
    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(map, min - 5 + rnd.nextInt(rangeSize + 10))
      i += 1
    }
    val it = map.keySet().iterator()
    while (it.hasNext()) {
      val key = it.next()
      if (rnd.nextBoolean()) {
        bs.clear(key.value)
        it.remove()
      }
    }
    while (map.size() < size) {
      val key = min + rnd.nextInt(rangeSize)
      assertTrue(key >= min && key <= max)
      put(map, key)
    }
  }

  private def mutateSubMap(
      map: ju.NavigableMap[Item, Item],
      min: Int,
      max: Int
  ): Unit = {
    val size = map.size()
    val rangeSize = max - min + 1
    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(map, min - 5 + rnd.nextInt(rangeSize + 10))
      i += 1
    }
    val it = map.keySet().iterator()
    while (it.hasNext()) {
      val key = it.next()
      if (rnd.nextBoolean()) {
        bs.clear(key.value)
        it.remove()
      }
    }
    while (map.size() < size) {
      val key = min - 5 + rnd.nextInt(rangeSize + 10)
      if (key >= min && key <= max) put(map, key)
      else
        assertThrows(
          classOf[IllegalArgumentException],
          map.put(itemFor(key), itemFor(2 * key))
        )
    }
  }

  private def put(map: ju.NavigableMap[Item, Item], key: Int): Unit =
    if (map.put(itemFor(key), itemFor(2 * key)) == null)
      bs.set(key)

  private def remove(map: ju.NavigableMap[Item, Item], key: Int): Unit =
    if (map.remove(itemFor(key)) != null)
      bs.clear(key)

  private def bashSubMap(
      map: ju.NavigableMap[Item, Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): Unit = {
    check(map, min, max, ascending)
    check(map.descendingMap(), min, max, !ascending)
    mutateSubMap(map, min, max)
    check(map, min, max, ascending)
    check(map.descendingMap(), min, max, !ascending)
    if (max - min < 2) return

    val midPoint = (min + max) / 2
    var incl = rnd.nextBoolean()
    val hm = map.headMap(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean())
        bashSubMap(hm, min, midPoint - (if (incl) 0 else 1), ascending = true)
      else
        bashSubMap(hm.descendingMap(), min, midPoint - (if (incl) 0 else 1), ascending = false)
    } else {
      if (rnd.nextBoolean())
        bashSubMap(hm, midPoint + (if (incl) 0 else 1), max, ascending = false)
      else
        bashSubMap(hm.descendingMap(), midPoint + (if (incl) 0 else 1), max, ascending = true)
    }

    incl = rnd.nextBoolean()
    val tm = map.tailMap(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean())
        bashSubMap(tm, midPoint + (if (incl) 0 else 1), max, ascending = true)
      else
        bashSubMap(tm.descendingMap(), midPoint + (if (incl) 0 else 1), max, ascending = false)
    } else {
      if (rnd.nextBoolean())
        bashSubMap(tm, min, midPoint - (if (incl) 0 else 1), ascending = false)
      else
        bashSubMap(tm.descendingMap(), min, midPoint - (if (incl) 0 else 1), ascending = true)
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
      val sm = map.subMap(itemFor(endpoints(0)), lowIncl, itemFor(endpoints(1)), highIncl)
      if (rnd.nextBoolean())
        bashSubMap(
          sm,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = true
        )
      else
        bashSubMap(
          sm.descendingMap(),
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = false
        )
    } else {
      val sm = map.subMap(itemFor(endpoints(1)), highIncl, itemFor(endpoints(0)), lowIncl)
      if (rnd.nextBoolean())
        bashSubMap(
          sm,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = false
        )
      else
        bashSubMap(
          sm.descendingMap(),
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          ascending = true
        )
    }
  }

  private def check(
      map: ju.NavigableMap[Item, Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): Unit = {
    def lowerAscending(key: Int): Int = floorAscending(key - 1)
    def floorAscending(key0: Int): Int = {
      var key = key0
      if (key < min) return -1
      else if (key > max) key = max
      while (key >= min) {
        if (bs.get(key)) return key
        key -= 1
      }
      -1
    }
    def ceilingAscending(key0: Int): Int = {
      var key = key0
      if (key < min) key = min
      else if (key > max) return -1
      val result = bs.nextSetBit(key)
      if (result > max) -1 else result
    }
    def higherAscending(key: Int): Int = ceilingAscending(key + 1)
    def firstAscending(): Int = {
      val result = ceilingAscending(min)
      if (result > max) -1 else result
    }
    def lastAscending(): Int = {
      val result = floorAscending(max)
      if (result < min) -1 else result
    }
    def lower(key: Int): Int =
      if (ascending) lowerAscending(key) else higherAscending(key)
    def floor(key: Int): Int =
      if (ascending) floorAscending(key) else ceilingAscending(key)
    def ceiling(key: Int): Int =
      if (ascending) ceilingAscending(key) else floorAscending(key)
    def higher(key: Int): Int =
      if (ascending) higherAscending(key) else lowerAscending(key)
    def first(): Int =
      if (ascending) firstAscending() else lastAscending()
    def last(): Int =
      if (ascending) lastAscending() else firstAscending()

    var size = 0
    var key = min
    while (key <= max) {
      val contains = bs.get(key)
      assertEquals(
        s"containsKey($key), min=$min max=$max ascending=$ascending",
        contains,
        map.containsKey(itemFor(key))
      )
      if (contains) size += 1
      key += 1
    }
    assertEquals(s"size, min=$min max=$max ascending=$ascending", size, map.size())

    var size2 = 0
    var previousKey = -1
    val it = map.keySet().iterator()
    while (it.hasNext()) {
      val k = it.next()
      assertTrue(s"unexpected key ${k.value}", bs.get(k.value))
      size2 += 1
      assertTrue(
        s"iteration order key=${k.value} previous=$previousKey ascending=$ascending",
        previousKey < 0 ||
          (if (ascending) k.value - previousKey > 0
           else k.value - previousKey < 0)
      )
      previousKey = k.value
    }
    assertEquals(s"iterated size, min=$min max=$max ascending=$ascending", size, size2)

    key = min - 1
    while (key <= max + 1) {
      val k = itemFor(key)
      assertEq(map.lowerKey(k), lower(key))
      assertEq(map.floorKey(k), floor(key))
      assertEq(map.higherKey(k), higher(key))
      assertEq(map.ceilingKey(k), ceiling(key))
      key += 1
    }

    if (map.size() != 0) {
      assertEq(map.firstKey(), first())
      assertEq(map.lastKey(), last())
    } else {
      mustEqual(first(), -1)
      mustEqual(last(), -1)
      assertThrows(classOf[NoSuchElementException], map.firstKey())
      assertThrows(classOf[NoSuchElementException], map.lastKey())
    }
  }

  private def assertEq(i: Item, j: Int): Unit =
    if (i == null) mustEqual(j, -1)
    else mustEqual(i, j)

  @Test
  def comparator(): Unit = {
    assertNull(new ju.TreeMap[String, String]().comparator())

    val cmp = ju.Comparator.naturalOrder[String]()

    assertSame(cmp, new ju.TreeMap[String, String](cmp).comparator())
  }
}

class TreeMapWithoutNullTest extends TreeMapTest(new TreeMapFactory)

class TreeMapWithNullTest extends TreeMapTest(new TreeMapWithNullFactory)

class TreeMapFactory extends AbstractMapFactory with NavigableMapFactory {
  def implementationName: String = "java.util.TreeMap"

  def empty[K: ClassTag, V: ClassTag]: ju.TreeMap[K, V] =
    new ju.TreeMap[K, V]

  def allowsNullKeys: Boolean = false

  def allowsNullValues: Boolean = true

  override def allowsNullKeysQueries: Boolean = false

  override def allowsSupertypeKeyQueries: Boolean = false
}

class TreeMapWithNullFactory extends TreeMapFactory {
  override def implementationName: String =
    super.implementationName + " (allows nulls)"

  override def empty[K: ClassTag, V: ClassTag]: ju.TreeMap[K, V] = {
    val natural = ju.Comparator.comparing[K, Comparable[Any]](
      ((_: K).asInstanceOf[Comparable[Any]]): Function[K, Comparable[Any]]
    )
    new ju.TreeMap[K, V](ju.Comparator.nullsFirst(natural))
  }

  override def allowsNullKeys: Boolean = true

  override def allowsNullKeysQueries: Boolean = true

  override def supportsJSR166MapTests: Boolean = false
}
