/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{ConcurrentNavigableMap, ConcurrentSkipListMap}
import java.util.{
  ArrayList, Arrays, Collection, Iterator, Map, NavigableMap, Set, SortedMap
}

import org.junit.Assert._
import org.junit._

object ConcurrentSkipListSubMapTest extends ConcurrentSkipListItems {
  import JSR166Test.mustEqual

  /** Returns a new map from Items 1-5 to Strings "A"-"E". */
  private def map5(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]
    assertTrue(map.isEmpty)
    map.put(zero, "Z")
    map.put(one, "A")
    map.put(five, "E")
    map.put(three, "C")
    map.put(two, "B")
    map.put(four, "D")
    map.put(seven, "F")
    assertFalse(map.isEmpty)
    mustEqual(7, map.size)
    map.subMap(one, true, seven, false)
  }

  /** Returns a new map from Items -5 to -1 to Strings "A"-"E". */
  private def dmap5(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]
    assertTrue(map.isEmpty)
    map.put(minusOne, "A")
    map.put(minusFive, "E")
    map.put(minusThree, "C")
    map.put(minusTwo, "B")
    map.put(minusFour, "D")
    assertFalse(map.isEmpty)
    mustEqual(5, map.size)
    map.descendingMap()
  }

  private def map0(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]
    assertTrue(map.isEmpty)
    map.tailMap(one, true)
  }

  private def dmap0(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]
    assertTrue(map.isEmpty)
    map
  }
}

class ConcurrentSkipListSubMapTest extends JSR166Test {
  import ConcurrentSkipListSubMapTest._
  // Selective import avoids ambiguity with companion Item zero/one/...
  // shouldThrow is an instance method on JSR166Test (inherited).
  import JSR166Test.{
    SIZE, defaultItems, expensiveTests, fortytwo, itemFor, mustAdd, mustContain,
    mustEqual, mustNotContain, mustNotRemove, mustRemove, ninetynine
  }

  /** clear removes all pairs */
  @Test def testClear(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    map.clear()
    mustEqual(0, map.size)
  }

  /** Maps with same contents are equal */
  @Test def testEquals(): Unit = {
    val map1: ConcurrentNavigableMap[Item, String] = map5()
    val map2: ConcurrentNavigableMap[Item, String] = map5()
    mustEqual(map1, map2)
    mustEqual(map2, map1)
    map1.clear()
    assertFalse(map1.equals(map2))
    assertFalse(map2.equals(map1))
  }

  /** containsKey returns true for contained key */
  @Test def testContainsKey(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    assertTrue(map.containsKey(one))
    assertFalse(map.containsKey(zero))
  }

  /** containsValue returns true for held values */
  @Test def testContainsValue(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    assertTrue(map.containsValue("A"))
    assertFalse(map.containsValue("Z"))
  }

  /** get returns the correct element at the given key, or null if not present
   */
  @Test def testGet(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    mustEqual("A", map.get(one))
    val empty: ConcurrentNavigableMap[Item, String] = map0()
    assertNull(empty.get(one))
  }

  /** isEmpty is true of empty map and false for non-empty */
  @Test def testIsEmpty(): Unit = {
    val empty: ConcurrentNavigableMap[Item, String] = map0()
    val map: ConcurrentNavigableMap[Item, String] = map5()
    assertTrue(empty.isEmpty)
    assertFalse(map.isEmpty)
  }

  /** firstKey returns first key */
  @Test def testFirstKey(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    mustEqual(one, map.firstKey)
  }

  /** lastKey returns last key */
  @Test def testLastKey(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    mustEqual(five, map.lastKey)
  }

  /** keySet returns a Set containing all the keys */
  @Test def testKeySet(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val s: Set[Item] = map.keySet
    mustEqual(5, s.size)
    mustContain(s, one)
    mustContain(s, two)
    mustContain(s, three)
    mustContain(s, four)
    mustContain(s, five)
  }

  /** keySet is ordered */
  @Test def testKeySetOrder(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val s: Set[Item] = map.keySet
    val i: Iterator[_ <: Item] = s.iterator
    var last: Item = i.next
    mustEqual(last, one)
    while (i.hasNext) {
      var k: Item = i.next
      assertTrue(last.compareTo(k) < 0)
      last = k
    }
  }

  /** values collection contains all values */
  @Test def testValues(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val s: Collection[String] = map.values
    mustEqual(5, s.size)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  /** keySet.toArray returns contains all keys */
  @Test def testKeySetToArray(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val s: Set[Item] = map.keySet
    val ar: Array[AnyRef] = s.toArray
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    mustEqual(5, ar.length)
    ar(0) = minusTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  /** descendingkeySet.toArray returns contains all keys */
  @Test def testDescendingKeySetToArray(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val s: Set[Item] = map.descendingKeySet
    val ar: Array[Item] = s.toArray(new Array[Item](0))
    mustEqual(5, ar.length)
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    ar(0) = minusTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  /** Values.toArray contains all values */
  @Test def testValuesToArray(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val v: Collection[String] = map.values
    val ar: Array[String] = v.toArray(new Array[String](0))
    val s: ArrayList[String] = new ArrayList[String](Arrays.asList(ar: _*))
    mustEqual(5, ar.length)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  /** entrySet contains all pairs */
  @Test def testEntrySet(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val s: Set[Map.Entry[Item, String]] = map.entrySet
    mustEqual(5, s.size)
    val it: Iterator[Map.Entry[Item, String]] = s.iterator
    while (it.hasNext) {
      var e: Map.Entry[Item, String] = it.next
      assertTrue(
        (e.getKey.equals(one) && e.getValue.equals("A")) ||
        (e.getKey.equals(two) && e.getValue.equals("B")) ||
        (e.getKey.equals(three) && e.getValue.equals("C")) ||
        (e.getKey.equals(four) && e.getValue.equals("D")) ||
        (e.getKey.equals(five) && e.getValue.equals("E"))
      )
    }
  }

  /** putAll adds all key-value pairs from the given map */
  @Test def testPutAll(): Unit = {
    val empty: ConcurrentNavigableMap[Item, String] = map0()
    val map: ConcurrentNavigableMap[Item, String] = map5()
    empty.putAll(map)
    mustEqual(5, empty.size)
    assertTrue(empty.containsKey(one))
    assertTrue(empty.containsKey(two))
    assertTrue(empty.containsKey(three))
    assertTrue(empty.containsKey(four))
    assertTrue(empty.containsKey(five))
  }

  /** putIfAbsent works when the given key is not present */
  @Test def testPutIfAbsent(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    map.putIfAbsent(six, "Z")
    assertTrue(map.containsKey(six))
  }

  /** putIfAbsent does not add the pair if the key is already present */
  @Test def testPutIfAbsent2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    mustEqual("A", map.putIfAbsent(one, "Z"))
  }

  /** replace fails when the given key is not present */
  @Test def testReplace(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    assertNull(map.replace(six, "Z"))
    assertFalse(map.containsKey(six))
  }

  /** replace succeeds if the key is already present */
  @Test def testReplace2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    assertNotNull(map.replace(one, "Z"))
    mustEqual("Z", map.get(one))
  }

  /** replace value fails when the given key not mapped to expected value */
  @Test def testReplaceValue(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    mustEqual("A", map.get(one))
    assertFalse(map.replace(one, "Z", "Z"))
    mustEqual("A", map.get(one))
  }

  /** replace value succeeds when the given key mapped to expected value */
  @Test def testReplaceValue2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    mustEqual("A", map.get(one))
    assertTrue(map.replace(one, "A", "Z"))
    mustEqual("Z", map.get(one))
  }

  /** remove removes the correct key-value pair from the map */
  @Test def testRemove(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    map.remove(five)
    mustEqual(4, map.size)
    assertFalse(map.containsKey(five))
  }

  /** remove(key,value) removes only if pair present */
  @Test def testRemove2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    assertTrue(map.containsKey(five))
    mustEqual("E", map.get(five))
    map.remove(five, "E")
    mustEqual(4, map.size)
    assertFalse(map.containsKey(five))
    map.remove(four, "A")
    mustEqual(4, map.size)
    assertTrue(map.containsKey(four))
  }

  /** lowerEntry returns preceding entry. */
  @Test def testLowerEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val e1: Map.Entry[Item, String] = map.lowerEntry(three)
    mustEqual(two, e1.getKey)
    val e2: Map.Entry[Item, String] = map.lowerEntry(six)
    mustEqual(five, e2.getKey)
    val e3: Map.Entry[Item, String] = map.lowerEntry(one)
    assertNull(e3)
    val e4: Map.Entry[Item, String] = map.lowerEntry(zero)
    assertNull(e4)
  }

  /** higherEntry returns next entry. */
  @Test def testHigherEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val e1: Map.Entry[Item, String] = map.higherEntry(three)
    mustEqual(four, e1.getKey)
    val e2: Map.Entry[Item, String] = map.higherEntry(zero)
    mustEqual(one, e2.getKey)
    val e3: Map.Entry[Item, String] = map.higherEntry(five)
    assertNull(e3)
    val e4: Map.Entry[Item, String] = map.higherEntry(six)
    assertNull(e4)
  }

  /** floorEntry returns preceding entry. */
  @Test def testFloorEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val e1: Map.Entry[Item, String] = map.floorEntry(three)
    mustEqual(three, e1.getKey)
    val e2: Map.Entry[Item, String] = map.floorEntry(six)
    mustEqual(five, e2.getKey)
    val e3: Map.Entry[Item, String] = map.floorEntry(one)
    mustEqual(one, e3.getKey)
    val e4: Map.Entry[Item, String] = map.floorEntry(zero)
    assertNull(e4)
  }

  /** ceilingEntry returns next entry. */
  @Test def testCeilingEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val e1: Map.Entry[Item, String] = map.ceilingEntry(three)
    mustEqual(three, e1.getKey)
    val e2: Map.Entry[Item, String] = map.ceilingEntry(zero)
    mustEqual(one, e2.getKey)
    val e3: Map.Entry[Item, String] = map.ceilingEntry(five)
    mustEqual(five, e3.getKey)
    val e4: Map.Entry[Item, String] = map.ceilingEntry(six)
    assertNull(e4)
  }

  /** pollFirstEntry returns entries in order */
  @Test def testPollFirstEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    var e: Map.Entry[Item, String] = map.pollFirstEntry()
    mustEqual(one, e.getKey)
    mustEqual("A", e.getValue)
    e = map.pollFirstEntry()
    mustEqual(two, e.getKey)
    map.put(one, "A")
    e = map.pollFirstEntry()
    mustEqual(one, e.getKey)
    mustEqual("A", e.getValue)
    e = map.pollFirstEntry()
    mustEqual(three, e.getKey)
    map.remove(four)
    e = map.pollFirstEntry()
    mustEqual(five, e.getKey)
    try {
      e.setValue("A")
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
    e = map.pollFirstEntry()
    assertNull(e)
  }

  /** pollLastEntry returns entries in order */
  @Test def testPollLastEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    var e: Map.Entry[Item, String] = map.pollLastEntry()
    mustEqual(five, e.getKey)
    mustEqual("E", e.getValue)
    e = map.pollLastEntry()
    mustEqual(four, e.getKey)
    map.put(five, "E")
    e = map.pollLastEntry()
    mustEqual(five, e.getKey)
    mustEqual("E", e.getValue)
    e = map.pollLastEntry()
    mustEqual(three, e.getKey)
    map.remove(two)
    e = map.pollLastEntry()
    mustEqual(one, e.getKey)
    try {
      e.setValue("E")
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
    e = map.pollLastEntry()
    assertNull(e)
  }

  /** size returns the correct values */
  @Test def testSize(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val empty: ConcurrentNavigableMap[Item, String] = map0()
    mustEqual(0, empty.size)
    mustEqual(5, map.size)
  }

  /** toString contains toString of elements */
  @Test def testToString(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val s: String = map.toString
    for (i <- 1 to 5) {
      assertTrue(s.contains(String.valueOf(i)))
    }
  }

  /** get(null) of nonempty map throws NPE */
  @Test def testGet_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map5()
      c.get(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** containsKey(null) of nonempty map throws NPE */
  @Test def testContainsKey_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map5()
      c.containsKey(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** containsValue(null) throws NPE */
  @Test def testContainsValue_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map0()
      c.containsValue(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** put(null,x) throws NPE */
  @Test def testPut1_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map5()
      c.put(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** putIfAbsent(null, x) throws NPE */
  @Test def testPutIfAbsent1_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map5()
      c.putIfAbsent(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** replace(null, x) throws NPE */
  @Test def testReplace_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map5()
      c.replace(null, "A")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** replace(null, x, y) throws NPE */
  @Test def testReplaceValue_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map5()
      c.replace(null, "A", "B")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** remove(null) throws NPE */
  @Test def testRemove1_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map5()
      c.remove(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** remove(null, x) throws NPE */
  @Test def testRemove2_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = map5()
      c.remove(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** A deserialized/reserialized map equals original
   *
   *  serialClone is unavailable on Scala Native (no ObjectInputStream).
   */
  // @Test def testSerialization(): Unit = {
  //   val x: NavigableMap[Item, String] = map5()
  //   val y: NavigableMap[Item, String] = serialClone(x)
  //
  //   assertNotSame(x, y)
  //   mustEqual(x.size, y.size)
  //   mustEqual(x.toString, y.toString)
  //   mustEqual(x, y)
  //   mustEqual(y, x)
  // }

  /** subMap returns map with keys in requested range */
  @Test def testSubMapContents(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val sm: SortedMap[Item, String] = map.subMap(two, four)
    mustEqual(two, sm.firstKey)
    mustEqual(three, sm.lastKey)
    mustEqual(2, sm.size)
    assertFalse(sm.containsKey(one))
    assertTrue(sm.containsKey(two))
    assertTrue(sm.containsKey(three))
    assertFalse(sm.containsKey(four))
    assertFalse(sm.containsKey(five))
    val i: Iterator[_ <: Item] = sm.keySet.iterator
    var k: Item = null.asInstanceOf[Item]
    k = i.next
    mustEqual(two, k)
    k = i.next
    mustEqual(three, k)
    assertFalse(i.hasNext)
    val j: Iterator[_ <: Item] = sm.keySet.iterator
    j.next
    j.remove()
    assertFalse(map.containsKey(two))
    mustEqual(4, map.size)
    mustEqual(1, sm.size)
    mustEqual(three, sm.firstKey)
    mustEqual(three, sm.lastKey)
    mustEqual("C", sm.remove(three))
    assertTrue(sm.isEmpty)
    mustEqual(3, map.size)
  }

  @Test def testSubMapContents2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val sm: SortedMap[Item, String] = map.subMap(two, three)
    mustEqual(1, sm.size)
    mustEqual(two, sm.firstKey)
    mustEqual(two, sm.lastKey)
    assertFalse(sm.containsKey(one))
    assertTrue(sm.containsKey(two))
    assertFalse(sm.containsKey(three))
    assertFalse(sm.containsKey(four))
    assertFalse(sm.containsKey(five))
    val i: Iterator[_ <: Item] = sm.keySet.iterator
    var k: Item = null.asInstanceOf[Item]
    k = i.next
    mustEqual(two, k)
    assertFalse(i.hasNext)
    val j: Iterator[_ <: Item] = sm.keySet.iterator
    j.next
    j.remove()
    assertFalse(map.containsKey(two))
    mustEqual(4, map.size)
    mustEqual(0, sm.size)
    assertTrue(sm.isEmpty)
    assertSame(sm.remove(three), null)
    mustEqual(4, map.size)
  }

  /** headMap returns map with keys in requested range */
  @Test def testHeadMapContents(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val sm: SortedMap[Item, String] = map.headMap(four)
    assertTrue(sm.containsKey(one))
    assertTrue(sm.containsKey(two))
    assertTrue(sm.containsKey(three))
    assertFalse(sm.containsKey(four))
    assertFalse(sm.containsKey(five))
    val i: Iterator[_ <: Item] = sm.keySet.iterator
    var k: Item = i.next
    mustEqual(one, k)
    k = i.next
    mustEqual(two, k)
    k = i.next
    mustEqual(three, k)
    assertFalse(i.hasNext)
    sm.clear()
    assertTrue(sm.isEmpty)
    mustEqual(2, map.size)
    mustEqual(four, map.firstKey)
  }

  /** headMap returns map with keys in requested range */
  @Test def testTailMapContents(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = map5()
    val sm: SortedMap[Item, String] = map.tailMap(two)
    assertFalse(sm.containsKey(one))
    assertTrue(sm.containsKey(two))
    assertTrue(sm.containsKey(three))
    assertTrue(sm.containsKey(four))
    assertTrue(sm.containsKey(five))
    val i: Iterator[Item] = sm.keySet.iterator
    var k: Item = i.next
    mustEqual(two, k)
    k = i.next
    mustEqual(three, k)
    k = i.next
    mustEqual(four, k)
    k = i.next
    mustEqual(five, k)
    assertFalse(i.hasNext)
    val ei: Iterator[Map.Entry[Item, String]] = sm.entrySet.iterator
    var e: Map.Entry[Item, String] = null.asInstanceOf[Map.Entry[Item, String]]
    e = ei.next
    mustEqual(two, e.getKey)
    mustEqual("B", e.getValue)
    e = ei.next
    mustEqual(three, e.getKey)
    mustEqual("C", e.getValue)
    e = ei.next
    mustEqual(four, e.getKey)
    mustEqual("D", e.getValue)
    e = ei.next
    mustEqual(five, e.getKey)
    mustEqual("E", e.getValue)
    assertFalse(i.hasNext)
    val ssm: SortedMap[Item, String] = sm.tailMap(four)
    mustEqual(four, ssm.firstKey)
    mustEqual(five, ssm.lastKey)
    mustEqual("D", ssm.remove(four))
    mustEqual(1, ssm.size)
    mustEqual(3, sm.size)
    mustEqual(4, map.size)
  }

  /** clear removes all pairs */
  @Test def testDescendingClear(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    map.clear()
    mustEqual(0, map.size)
  }

  /** Maps with same contents are equal */
  @Test def testDescendingEquals(): Unit = {
    val map1: ConcurrentNavigableMap[Item, String] = dmap5()
    val map2: ConcurrentNavigableMap[Item, String] = dmap5()
    mustEqual(map1, map2)
    mustEqual(map2, map1)
    map1.clear()
    assertFalse(map1.equals(map2))
    assertFalse(map2.equals(map1))
  }

  /** containsKey returns true for contained key */
  @Test def testDescendingContainsKey(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    assertTrue(map.containsKey(minusOne))
    assertFalse(map.containsKey(zero))
  }

  /** containsValue returns true for held values */
  @Test def testDescendingContainsValue(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    assertTrue(map.containsValue("A"))
    assertFalse(map.containsValue("Z"))
  }

  /** get returns the correct element at the given key, or null if not present
   */
  @Test def testDescendingGet(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    mustEqual("A", map.get(minusOne))
    val empty: ConcurrentNavigableMap[Item, String] = dmap0()
    assertNull(empty.get(minusOne))
  }

  /** isEmpty is true of empty map and false for non-empty */
  @Test def testDescendingIsEmpty(): Unit = {
    val empty: ConcurrentNavigableMap[Item, String] = dmap0()
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    assertTrue(empty.isEmpty)
    assertFalse(map.isEmpty)
  }

  /** firstKey returns first key */
  @Test def testDescendingFirstKey(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    mustEqual(minusOne, map.firstKey)
  }

  /** lastKey returns last key */
  @Test def testDescendingLastKey(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    mustEqual(minusFive, map.lastKey)
  }

  /** keySet returns a Set containing all the keys */
  @Test def testDescendingKeySet(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val s: Set[Item] = map.keySet
    mustEqual(5, s.size)
    mustContain(s, minusOne)
    mustContain(s, minusTwo)
    mustContain(s, minusThree)
    mustContain(s, minusFour)
    mustContain(s, minusFive)
  }

  /** keySet is ordered */
  @Test def testDescendingKeySetOrder(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val s: Set[Item] = map.keySet
    val i: Iterator[_ <: Item] = s.iterator
    var last: Item = i.next
    mustEqual(last, minusOne)
    while (i.hasNext) {
      var k: Item = i.next
      assertTrue(last.compareTo(k) > 0)
      last = k
    }
  }

  /** values collection contains all values */
  @Test def testDescendingValues(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val s: Collection[String] = map.values
    mustEqual(5, s.size)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  /** keySet.toArray returns contains all keys */
  @Test def testDescendingAscendingKeySetToArray(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val s: Set[Item] = map.keySet
    val ar: Array[Item] = s.toArray(new Array[Item](0))
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    mustEqual(5, ar.length)
    ar(0) = minusTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  /** descendingkeySet.toArray returns contains all keys */
  @Test def testDescendingDescendingKeySetToArray(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val s: Set[Item] = map.descendingKeySet
    val ar: Array[Item] = s.toArray(new Array[Item](0))
    mustEqual(5, ar.length)
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    ar(0) = minusTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  /** Values.toArray contains all values */
  @Test def testDescendingValuesToArray(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val v: Collection[String] = map.values
    val ar: Array[String] = v.toArray(new Array[String](0))
    val s: ArrayList[String] = new ArrayList[String](Arrays.asList(ar: _*))
    mustEqual(5, ar.length)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  /** entrySet contains all pairs */
  @Test def testDescendingEntrySet(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val s: Set[Map.Entry[Item, String]] = map.entrySet
    mustEqual(5, s.size)
    val it: Iterator[Map.Entry[Item, String]] = s.iterator
    while (it.hasNext) {
      var e: Map.Entry[Item, String] = it.next
      assertTrue(
        (e.getKey.equals(minusOne) && e.getValue.equals("A")) ||
        (e.getKey.equals(minusTwo) && e.getValue.equals("B")) ||
        (e.getKey.equals(minusThree) && e.getValue.equals("C")) ||
        (e.getKey.equals(minusFour) && e.getValue.equals("D")) ||
        (e.getKey.equals(minusFive) && e.getValue.equals("E"))
      )
    }
  }

  /** putAll adds all key-value pairs from the given map */
  @Test def testDescendingPutAll(): Unit = {
    val empty: ConcurrentNavigableMap[Item, String] = dmap0()
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    empty.putAll(map)
    mustEqual(5, empty.size)
    assertTrue(empty.containsKey(minusOne))
    assertTrue(empty.containsKey(minusTwo))
    assertTrue(empty.containsKey(minusThree))
    assertTrue(empty.containsKey(minusFour))
    assertTrue(empty.containsKey(minusFive))
  }

  /** putIfAbsent works when the given key is not present */
  @Test def testDescendingPutIfAbsent(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    map.putIfAbsent(six, "Z")
    assertTrue(map.containsKey(six))
  }

  /** putIfAbsent does not add the pair if the key is already present */
  @Test def testDescendingPutIfAbsent2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    mustEqual("A", map.putIfAbsent(minusOne, "Z"))
  }

  /** replace fails when the given key is not present */
  @Test def testDescendingReplace(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    assertNull(map.replace(six, "Z"))
    assertFalse(map.containsKey(six))
  }

  /** replace succeeds if the key is already present */
  @Test def testDescendingReplace2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    assertNotNull(map.replace(minusOne, "Z"))
    mustEqual("Z", map.get(minusOne))
  }

  /** replace value fails when the given key not mapped to expected value */
  @Test def testDescendingReplaceValue(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    mustEqual("A", map.get(minusOne))
    assertFalse(map.replace(minusOne, "Z", "Z"))
    mustEqual("A", map.get(minusOne))
  }

  /** replace value succeeds when the given key mapped to expected value */
  @Test def testDescendingReplaceValue2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    mustEqual("A", map.get(minusOne))
    assertTrue(map.replace(minusOne, "A", "Z"))
    mustEqual("Z", map.get(minusOne))
  }

  /** remove removes the correct key-value pair from the map */
  @Test def testDescendingRemove(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    map.remove(minusFive)
    mustEqual(4, map.size)
    assertFalse(map.containsKey(minusFive))
  }

  /** remove(key,value) removes only if pair present */
  @Test def testDescendingRemove2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    assertTrue(map.containsKey(minusFive))
    mustEqual("E", map.get(minusFive))
    map.remove(minusFive, "E")
    mustEqual(4, map.size)
    assertFalse(map.containsKey(minusFive))
    map.remove(minusFour, "A")
    mustEqual(4, map.size)
    assertTrue(map.containsKey(minusFour))
  }

  /** lowerEntry returns preceding entry. */
  @Test def testDescendingLowerEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val e1: Map.Entry[Item, String] = map.lowerEntry(minusThree)
    mustEqual(minusTwo, e1.getKey)
    val e2: Map.Entry[Item, String] = map.lowerEntry(minusSix)
    mustEqual(minusFive, e2.getKey)
    val e3: Map.Entry[Item, String] = map.lowerEntry(minusOne)
    assertNull(e3)
    val e4: Map.Entry[Item, String] = map.lowerEntry(zero)
    assertNull(e4)
  }

  /** higherEntry returns next entry. */
  @Test def testDescendingHigherEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val e1: Map.Entry[Item, String] = map.higherEntry(minusThree)
    mustEqual(minusFour, e1.getKey)
    val e2: Map.Entry[Item, String] = map.higherEntry(zero)
    mustEqual(minusOne, e2.getKey)
    val e3: Map.Entry[Item, String] = map.higherEntry(minusFive)
    assertNull(e3)
    val e4: Map.Entry[Item, String] = map.higherEntry(minusSix)
    assertNull(e4)
  }

  /** floorEntry returns preceding entry. */
  @Test def testDescendingFloorEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val e1: Map.Entry[Item, String] = map.floorEntry(minusThree)
    mustEqual(minusThree, e1.getKey)
    val e2: Map.Entry[Item, String] = map.floorEntry(minusSix)
    mustEqual(minusFive, e2.getKey)
    val e3: Map.Entry[Item, String] = map.floorEntry(minusOne)
    mustEqual(minusOne, e3.getKey)
    val e4: Map.Entry[Item, String] = map.floorEntry(zero)
    assertNull(e4)
  }

  /** ceilingEntry returns next entry. */
  @Test def testDescendingCeilingEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val e1: Map.Entry[Item, String] = map.ceilingEntry(minusThree)
    mustEqual(minusThree, e1.getKey)
    val e2: Map.Entry[Item, String] = map.ceilingEntry(zero)
    mustEqual(minusOne, e2.getKey)
    val e3: Map.Entry[Item, String] = map.ceilingEntry(minusFive)
    mustEqual(minusFive, e3.getKey)
    val e4: Map.Entry[Item, String] = map.ceilingEntry(minusSix)
    assertNull(e4)
  }

  /** pollFirstEntry returns entries in order */
  @Test def testDescendingPollFirstEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    var e: Map.Entry[Item, String] = map.pollFirstEntry()
    mustEqual(minusOne, e.getKey)
    mustEqual("A", e.getValue)
    e = map.pollFirstEntry()
    mustEqual(minusTwo, e.getKey)
    map.put(minusOne, "A")
    e = map.pollFirstEntry()
    mustEqual(minusOne, e.getKey)
    mustEqual("A", e.getValue)
    e = map.pollFirstEntry()
    mustEqual(minusThree, e.getKey)
    map.remove(minusFour)
    e = map.pollFirstEntry()
    mustEqual(minusFive, e.getKey)
    try {
      e.setValue("A")
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
    e = map.pollFirstEntry()
    assertNull(e)
  }

  /** pollLastEntry returns entries in order */
  @Test def testDescendingPollLastEntry(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    var e: Map.Entry[Item, String] = map.pollLastEntry()
    mustEqual(minusFive, e.getKey)
    mustEqual("E", e.getValue)
    e = map.pollLastEntry()
    mustEqual(minusFour, e.getKey)
    map.put(minusFive, "E")
    e = map.pollLastEntry()
    mustEqual(minusFive, e.getKey)
    mustEqual("E", e.getValue)
    e = map.pollLastEntry()
    mustEqual(minusThree, e.getKey)
    map.remove(minusTwo)
    e = map.pollLastEntry()
    mustEqual(minusOne, e.getKey)
    try {
      e.setValue("E")
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
    e = map.pollLastEntry()
    assertNull(e)
  }

  /** size returns the correct values */
  @Test def testDescendingSize(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val empty: ConcurrentNavigableMap[Item, String] = dmap0()
    mustEqual(0, empty.size)
    mustEqual(5, map.size)
  }

  /** toString contains toString of elements */
  @Test def testDescendingToString(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val s: String = map.toString
    for (i <- 1 to 5) {
      assertTrue(s.contains(String.valueOf(i)))
    }
  }

  /** get(null) of empty map throws NPE */
  @Test def testDescendingGet_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap5()
      c.get(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** containsKey(null) of empty map throws NPE */
  @Test def testDescendingContainsKey_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap5()
      c.containsKey(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** containsValue(null) throws NPE */
  @Test def testDescendingContainsValue_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap0()
      c.containsValue(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** put(null,x) throws NPE */
  @Test def testDescendingPut1_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap5()
      c.put(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** putIfAbsent(null, x) throws NPE */
  @Test def testDescendingPutIfAbsent1_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap5()
      c.putIfAbsent(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** replace(null, x) throws NPE */
  @Test def testDescendingReplace_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap5()
      c.replace(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** replace(null, x, y) throws NPE */
  @Test def testDescendingReplaceValue_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap5()
      c.replace(null, "A", "B")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** remove(null) throws NPE */
  @Test def testDescendingRemove1_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap5()
      c.remove(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** remove(null, x) throws NPE */
  @Test def testDescendingRemove2_NullPointerException(): Unit = {
    try {
      val c: ConcurrentNavigableMap[Item, String] = dmap5()
      c.remove(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** A deserialized/reserialized map equals original
   *
   *  serialClone is unavailable on Scala Native (no ObjectInputStream).
   */
  // @Test def testDescendingSerialization(): Unit = {
  //   val x: NavigableMap[Item, String] = dmap5()
  //   val y: NavigableMap[Item, String] = serialClone(x)
  //
  //   assertNotSame(x, y)
  //   mustEqual(x.size, y.size)
  //   mustEqual(x.toString, y.toString)
  //   mustEqual(x, y)
  //   mustEqual(y, x)
  // }

  /** subMap returns map with keys in requested range */
  @Test def testDescendingSubMapContents(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val sm: SortedMap[Item, String] = map.subMap(minusTwo, minusFour)
    mustEqual(minusTwo, sm.firstKey)
    mustEqual(minusThree, sm.lastKey)
    mustEqual(2, sm.size)
    assertFalse(sm.containsKey(minusOne))
    assertTrue(sm.containsKey(minusTwo))
    assertTrue(sm.containsKey(minusThree))
    assertFalse(sm.containsKey(minusFour))
    assertFalse(sm.containsKey(minusFive))
    val i: Iterator[_ <: Item] = sm.keySet.iterator
    var k: Item = null.asInstanceOf[Item]
    k = i.next
    mustEqual(minusTwo, k)
    k = i.next
    mustEqual(minusThree, k)
    assertFalse(i.hasNext)
    val j: Iterator[_ <: Item] = sm.keySet.iterator
    j.next
    j.remove()
    assertFalse(map.containsKey(minusTwo))
    mustEqual(4, map.size)
    mustEqual(1, sm.size)
    mustEqual(minusThree, sm.firstKey)
    mustEqual(minusThree, sm.lastKey)
    mustEqual("C", sm.remove(minusThree))
    assertTrue(sm.isEmpty)
    mustEqual(3, map.size)
  }

  @Test def testDescendingSubMapContents2(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val sm: SortedMap[Item, String] = map.subMap(minusTwo, minusThree)
    mustEqual(1, sm.size)
    mustEqual(minusTwo, sm.firstKey)
    mustEqual(minusTwo, sm.lastKey)
    assertFalse(sm.containsKey(minusOne))
    assertTrue(sm.containsKey(minusTwo))
    assertFalse(sm.containsKey(minusThree))
    assertFalse(sm.containsKey(minusFour))
    assertFalse(sm.containsKey(minusFive))
    val i: Iterator[_ <: Item] = sm.keySet.iterator
    var k: Item = null.asInstanceOf[Item]
    k = i.next
    mustEqual(minusTwo, k)
    assertFalse(i.hasNext)
    val j: Iterator[_ <: Item] = sm.keySet.iterator
    j.next
    j.remove()
    assertFalse(map.containsKey(minusTwo))
    mustEqual(4, map.size)
    mustEqual(0, sm.size)
    assertTrue(sm.isEmpty)
    assertSame(sm.remove(minusThree), null)
    mustEqual(4, map.size)
  }

  /** headMap returns map with keys in requested range */
  @Test def testDescendingHeadMapContents(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val sm: SortedMap[Item, String] = map.headMap(minusFour)
    assertTrue(sm.containsKey(minusOne))
    assertTrue(sm.containsKey(minusTwo))
    assertTrue(sm.containsKey(minusThree))
    assertFalse(sm.containsKey(minusFour))
    assertFalse(sm.containsKey(minusFive))
    val i: Iterator[_ <: Item] = sm.keySet.iterator
    var k: Item = null.asInstanceOf[Item]
    k = i.next
    mustEqual(minusOne, k)
    k = i.next
    mustEqual(minusTwo, k)
    k = i.next
    mustEqual(minusThree, k)
    assertFalse(i.hasNext)
    sm.clear()
    assertTrue(sm.isEmpty)
    mustEqual(2, map.size)
    mustEqual(minusFour, map.firstKey)
  }

  /** headMap returns map with keys in requested range */
  @Test def testDescendingTailMapContents(): Unit = {
    val map: ConcurrentNavigableMap[Item, String] = dmap5()
    val sm: SortedMap[Item, String] = map.tailMap(minusTwo)
    assertFalse(sm.containsKey(minusOne))
    assertTrue(sm.containsKey(minusTwo))
    assertTrue(sm.containsKey(minusThree))
    assertTrue(sm.containsKey(minusFour))
    assertTrue(sm.containsKey(minusFive))
    val i: Iterator[_ <: Item] = sm.keySet.iterator
    var k: Item = i.next
    mustEqual(minusTwo, k)
    k = i.next
    mustEqual(minusThree, k)
    k = i.next
    mustEqual(minusFour, k)
    k = i.next
    mustEqual(minusFive, k)
    assertFalse(i.hasNext)
    val ei: Iterator[Map.Entry[Item, String]] = sm.entrySet.iterator
    var e: Map.Entry[Item, String] = null.asInstanceOf[Map.Entry[Item, String]]
    e = ei.next
    mustEqual(minusTwo, e.getKey)
    mustEqual("B", e.getValue)
    e = ei.next
    mustEqual(minusThree, e.getKey)
    mustEqual("C", e.getValue)
    e = ei.next
    mustEqual(minusFour, e.getKey)
    mustEqual("D", e.getValue)
    e = ei.next
    mustEqual(minusFive, e.getKey)
    mustEqual("E", e.getValue)
    assertFalse(i.hasNext)
    val ssm: SortedMap[Item, String] = sm.tailMap(minusFour)
    mustEqual(minusFour, ssm.firstKey)
    mustEqual(minusFive, ssm.lastKey)
    mustEqual("D", ssm.remove(minusFour))
    mustEqual(1, ssm.size)
    mustEqual(3, sm.size)
    mustEqual(4, map.size)
  }

}
