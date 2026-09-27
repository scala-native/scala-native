/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.ConcurrentSkipListMap
import java.util.{
  ArrayList, Arrays, BitSet, Map, NavigableMap, NavigableSet,
  NoSuchElementException, Random
}

import org.junit.Assert._
import org.junit._

object ConcurrentSkipListMapTest extends ConcurrentSkipListItems {
  import JSR166Test.mustEqual

  /** Returns a new map from Items 1-5 to Strings "A"-"E".
   */
  private def map5(): ConcurrentSkipListMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    assertTrue(map.isEmpty)
    map.put(one, "A")
    map.put(five, "E")
    map.put(three, "C")
    map.put(two, "B")
    map.put(four, "D")
    assertFalse(map.isEmpty)
    mustEqual(5, map.size)
    map
  }

  /** Direct construction (Java uses Class.newInstance via reflection). */
  private def newMap(): NavigableMap[Item, Item] = {
    val result = new ConcurrentSkipListMap[Item, Item]()
    mustEqual(0, result.size)
    assertFalse(result.keySet.iterator.hasNext)
    result
  }

  private def assertEq(i: Item, j: Int): Unit = {
    if (i == null) mustEqual(j, -1)
    else mustEqual(i, j)
  }
}

class ConcurrentSkipListMapTest extends JSR166Test {
  import ConcurrentSkipListMapTest._
  // Selective import avoids ambiguity with companion Item zero/one/...
  // shouldThrow is an instance method on JSR166Test (inherited).
  import JSR166Test.{
    SIZE, defaultItems, expensiveTests, fortytwo, itemFor, mustAdd, mustContain,
    mustEqual, mustNotContain, mustNotRemove, mustRemove, ninetynine
  }

  /** clear removes all pairs
   */
  @Test def testClear(): Unit = {
    val map = map5()
    map.clear()
    mustEqual(0, map.size)
  }

  /** copy constructor creates map equal to source map
   */
  @Test def testConstructFromSorted(): Unit = {
    val map = map5()
    val map2 = new ConcurrentSkipListMap[Item, String](map)
    mustEqual(map, map2)
  }

  /** Maps with same contents are equal
   */
  @Test def testEquals(): Unit = {
    val map1 = map5()
    val map2 = map5()
    mustEqual(map1, map2)
    mustEqual(map2, map1)
    map1.clear()
    assertFalse(map1.equals(map2))
    assertFalse(map2.equals(map1))
  }

  /** containsKey returns true for contained key
   */
  @Test def testContainsKey(): Unit = {
    val map = map5()
    assertTrue(map.containsKey(one))
    assertFalse(map.containsKey(zero))
  }

  /** containsValue returns true for held values
   */
  @Test def testContainsValue(): Unit = {
    val map = map5()
    assertTrue(map.containsValue("A"))
    assertFalse(map.containsValue("Z"))
  }

  /** get returns the correct element at the given key, or null if not present
   */
  @Test def testGet(): Unit = {
    val map = map5()
    mustEqual("A", map.get(one))
    val empty = new ConcurrentSkipListMap[Item, String]()
    assertNull(empty.get(one))
  }

  /** isEmpty is true of empty map and false for non-empty
   */
  @Test def testIsEmpty(): Unit = {
    val empty = new ConcurrentSkipListMap[Item, String]()
    val map = map5()
    assertTrue(empty.isEmpty)
    assertFalse(map.isEmpty)
  }

  /** firstKey returns first key
   */
  @Test def testFirstKey(): Unit = {
    val map = map5()
    mustEqual(one, map.firstKey)
  }

  /** lastKey returns last key
   */
  @Test def testLastKey(): Unit = {
    val map = map5()
    mustEqual(five, map.lastKey)
  }

  /** keySet.toArray returns contains all keys
   */
  @Test def testKeySetToArray(): Unit = {
    val map = map5()
    val s = map.keySet
    val ar = s.toArray
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    mustEqual(5, ar.length)
    ar(0) = minusTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  /** descendingkeySet.toArray returns contains all keys
   */
  @Test def testDescendingKeySetToArray(): Unit = {
    val map = map5()
    val s = map.descendingKeySet
    val ar = s.toArray
    mustEqual(5, ar.length)
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    ar(0) = minusTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  /** keySet returns a Set containing all the keys
   */
  @Test def testKeySet(): Unit = {
    val map = map5()
    val s = map.keySet
    mustEqual(5, s.size)
    mustContain(s, one)
    mustContain(s, two)
    mustContain(s, three)
    mustContain(s, four)
    mustContain(s, five)
  }

  /** keySet is ordered
   */
  @Test def testKeySetOrder(): Unit = {
    val map = map5()
    val s = map.keySet
    val i = s.iterator
    var last = i.next()
    mustEqual(last, one)
    var count = 1
    while (i.hasNext) {
      val k = i.next()
      assertTrue(last.compareTo(k) < 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  /** descending iterator of key set is inverse ordered
   */
  @Test def testKeySetDescendingIteratorOrder(): Unit = {
    val map = map5()
    val s: NavigableSet[Item] = map.navigableKeySet
    val i = s.descendingIterator
    var last = i.next()
    mustEqual(last, five)
    var count = 1
    while (i.hasNext) {
      val k = i.next()
      assertTrue(last.compareTo(k) > 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  /** descendingKeySet is ordered
   */
  @Test def testDescendingKeySetOrder(): Unit = {
    val map = map5()
    val s = map.descendingKeySet
    val i = s.iterator
    var last = i.next()
    mustEqual(last, five)
    var count = 1
    while (i.hasNext) {
      val k = i.next()
      assertTrue(last.compareTo(k) > 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  /** descending iterator of descendingKeySet is ordered
   */
  @Test def testDescendingKeySetDescendingIteratorOrder(): Unit = {
    val map = map5()
    val s: NavigableSet[Item] = map.descendingKeySet
    val i = s.descendingIterator
    var last = i.next()
    mustEqual(last, one)
    var count = 1
    while (i.hasNext) {
      val k = i.next()
      assertTrue(last.compareTo(k) < 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  /** Values.toArray contains all values
   */
  @Test def testValuesToArray(): Unit = {
    val map = map5()
    val v = map.values
    val ar = v.toArray(new Array[String](0))
    val s = new ArrayList[String](Arrays.asList(ar: _*))
    mustEqual(5, ar.length)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  /** values collection contains all values
   */
  @Test def testValues(): Unit = {
    val map = map5()
    val s = map.values
    mustEqual(5, s.size)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  /** entrySet contains all pairs
   */
  @Test def testEntrySet(): Unit = {
    val map = map5()
    val s = map.entrySet
    mustEqual(5, s.size)
    val it = s.iterator
    while (it.hasNext) {
      val e = it.next()
      assertTrue(
        (e.getKey.equals(one) && e.getValue.equals("A")) ||
        (e.getKey.equals(two) && e.getValue.equals("B")) ||
        (e.getKey.equals(three) && e.getValue.equals("C")) ||
        (e.getKey.equals(four) && e.getValue.equals("D")) ||
        (e.getKey.equals(five) && e.getValue.equals("E"))
      )
    }
  }

  /** descendingEntrySet contains all pairs
   */
  @Test def testDescendingEntrySet(): Unit = {
    val map = map5()
    val s = map.descendingMap.entrySet
    mustEqual(5, s.size)
    val it = s.iterator
    while (it.hasNext) {
      val e = it.next()
      assertTrue(
        (e.getKey.equals(one) && e.getValue.equals("A")) ||
        (e.getKey.equals(two) && e.getValue.equals("B")) ||
        (e.getKey.equals(three) && e.getValue.equals("C")) ||
        (e.getKey.equals(four) && e.getValue.equals("D")) ||
        (e.getKey.equals(five) && e.getValue.equals("E"))
      )
    }
  }

  /** entrySet.toArray contains all entries
   */
  @Test def testEntrySetToArray(): Unit = {
    val map = map5()
    val s = map.entrySet
    val ar = s.toArray
    mustEqual(5, ar.length)
    var i = 0
    while (i < 5) {
      val e = ar(i).asInstanceOf[Map.Entry[_, _]]
      assertTrue(map.containsKey(e.getKey))
      assertTrue(map.containsValue(e.getValue))
      i += 1
    }
  }

  /** descendingEntrySet.toArray contains all entries
   */
  @Test def testDescendingEntrySetToArray(): Unit = {
    val map = map5()
    val s = map.descendingMap.entrySet
    val ar = s.toArray
    mustEqual(5, ar.length)
    var i = 0
    while (i < 5) {
      val e = ar(i).asInstanceOf[Map.Entry[_, _]]
      assertTrue(map.containsKey(e.getKey))
      assertTrue(map.containsValue(e.getValue))
      i += 1
    }
  }

  /** putAll adds all key-value pairs from the given map
   */
  @Test def testPutAll(): Unit = {
    val p = new ConcurrentSkipListMap[Item, String]()
    val map = map5()
    p.putAll(map)
    mustEqual(5, p.size)
    assertTrue(p.containsKey(one))
    assertTrue(p.containsKey(two))
    assertTrue(p.containsKey(three))
    assertTrue(p.containsKey(four))
    assertTrue(p.containsKey(five))
  }

  /** putIfAbsent works when the given key is not present
   */
  @Test def testPutIfAbsent(): Unit = {
    val map = map5()
    map.putIfAbsent(six, "Z")
    assertTrue(map.containsKey(six))
  }

  /** putIfAbsent does not add the pair if the key is already present
   */
  @Test def testPutIfAbsent2(): Unit = {
    val map = map5()
    mustEqual("A", map.putIfAbsent(one, "Z"))
  }

  /** replace fails when the given key is not present
   */
  @Test def testReplace(): Unit = {
    val map = map5()
    assertNull(map.replace(six, "Z"))
    assertFalse(map.containsKey(six))
  }

  /** replace succeeds if the key is already present
   */
  @Test def testReplace2(): Unit = {
    val map = map5()
    assertNotNull(map.replace(one, "Z"))
    mustEqual("Z", map.get(one))
  }

  /** replace value fails when the given key not mapped to expected value
   */
  @Test def testReplaceValue(): Unit = {
    val map = map5()
    mustEqual("A", map.get(one))
    assertFalse(map.replace(one, "Z", "Z"))
    mustEqual("A", map.get(one))
  }

  /** replace value succeeds when the given key mapped to expected value
   */
  @Test def testReplaceValue2(): Unit = {
    val map = map5()
    mustEqual("A", map.get(one))
    assertTrue(map.replace(one, "A", "Z"))
    mustEqual("Z", map.get(one))
  }

  /** remove removes the correct key-value pair from the map
   */
  @Test def testRemove(): Unit = {
    val map = map5()
    map.remove(five)
    mustEqual(4, map.size)
    assertFalse(map.containsKey(five))
  }

  /** remove(key,value) removes only if pair present
   */
  @Test def testRemove2(): Unit = {
    val map = map5()
    assertTrue(map.containsKey(five))
    mustEqual("E", map.get(five))
    map.remove(five, "E")
    mustEqual(4, map.size)
    assertFalse(map.containsKey(five))
    map.remove(four, "A")
    mustEqual(4, map.size)
    assertTrue(map.containsKey(four))
  }

  /** lowerEntry returns preceding entry.
   */
  @Test def testLowerEntry(): Unit = {
    val map = map5()
    val e1 = map.lowerEntry(three)
    mustEqual(two, e1.getKey)

    val e2 = map.lowerEntry(six)
    mustEqual(five, e2.getKey)

    val e3 = map.lowerEntry(one)
    assertNull(e3)

    val e4 = map.lowerEntry(zero)
    assertNull(e4)
  }

  /** higherEntry returns next entry.
   */
  @Test def testHigherEntry(): Unit = {
    val map = map5()
    val e1 = map.higherEntry(three)
    mustEqual(four, e1.getKey)

    val e2 = map.higherEntry(zero)
    mustEqual(one, e2.getKey)

    val e3 = map.higherEntry(five)
    assertNull(e3)

    val e4 = map.higherEntry(six)
    assertNull(e4)
  }

  /** floorEntry returns preceding entry.
   */
  @Test def testFloorEntry(): Unit = {
    val map = map5()
    val e1 = map.floorEntry(three)
    mustEqual(three, e1.getKey)

    val e2 = map.floorEntry(six)
    mustEqual(five, e2.getKey)

    val e3 = map.floorEntry(one)
    mustEqual(one, e3.getKey)

    val e4 = map.floorEntry(zero)
    assertNull(e4)
  }

  /** ceilingEntry returns next entry.
   */
  @Test def testCeilingEntry(): Unit = {
    val map = map5()
    val e1 = map.ceilingEntry(three)
    mustEqual(three, e1.getKey)

    val e2 = map.ceilingEntry(zero)
    mustEqual(one, e2.getKey)

    val e3 = map.ceilingEntry(five)
    mustEqual(five, e3.getKey)

    val e4 = map.ceilingEntry(six)
    assertNull(e4)
  }

  /** lowerEntry, higherEntry, ceilingEntry, and floorEntry return immutable
   *  entries
   */
  @Test def testEntryImmutability(): Unit = {
    val map = map5()
    var e = map.lowerEntry(three)
    mustEqual(two, e.getKey)
    try {
      e.setValue("X")
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
    e = map.higherEntry(zero)
    mustEqual(one, e.getKey)
    try {
      e.setValue("X")
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
    e = map.floorEntry(one)
    mustEqual(one, e.getKey)
    try {
      e.setValue("X")
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
    e = map.ceilingEntry(five)
    mustEqual(five, e.getKey)
    try {
      e.setValue("X")
      shouldThrow()
    } catch {
      case success: UnsupportedOperationException =>
    }
  }

  /** lowerKey returns preceding element
   */
  @Test def testLowerKey(): Unit = {
    val q = map5()
    val e1 = q.lowerKey(three)
    mustEqual(two, e1)

    val e2 = q.lowerKey(six)
    mustEqual(five, e2)

    val e3 = q.lowerKey(one)
    assertNull(e3)

    val e4 = q.lowerKey(zero)
    assertNull(e4)
  }

  /** higherKey returns next element
   */
  @Test def testHigherKey(): Unit = {
    val q = map5()
    val e1 = q.higherKey(three)
    mustEqual(four, e1)

    val e2 = q.higherKey(zero)
    mustEqual(one, e2)

    val e3 = q.higherKey(five)
    assertNull(e3)

    val e4 = q.higherKey(six)
    assertNull(e4)
  }

  /** floorKey returns preceding element
   */
  @Test def testFloorKey(): Unit = {
    val q = map5()
    val e1 = q.floorKey(three)
    mustEqual(three, e1)

    val e2 = q.floorKey(six)
    mustEqual(five, e2)

    val e3 = q.floorKey(one)
    mustEqual(one, e3)

    val e4 = q.floorKey(zero)
    assertNull(e4)
  }

  /** ceilingKey returns next element
   */
  @Test def testCeilingKey(): Unit = {
    val q = map5()
    val e1 = q.ceilingKey(three)
    mustEqual(three, e1)

    val e2 = q.ceilingKey(zero)
    mustEqual(one, e2)

    val e3 = q.ceilingKey(five)
    mustEqual(five, e3)

    val e4 = q.ceilingKey(six)
    assertNull(e4)
  }

  /** pollFirstEntry returns entries in order
   */
  @Test def testPollFirstEntry(): Unit = {
    val map = map5()
    var e = map.pollFirstEntry()
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

  /** pollLastEntry returns entries in order
   */
  @Test def testPollLastEntry(): Unit = {
    val map = map5()
    var e = map.pollLastEntry()
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

  /** size returns the correct values
   */
  @Test def testSize(): Unit = {
    val map = map5()
    val empty = new ConcurrentSkipListMap[Item, String]()
    mustEqual(0, empty.size)
    mustEqual(5, map.size)
  }

  /** toString contains toString of elements
   */
  @Test def testToString(): Unit = {
    val map = map5()
    val s = map.toString
    var i = 1
    while (i <= 5) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  // Exception tests

  /** get(null) of nonempty map throws NPE
   */
  @Test def testGet_NullPointerException(): Unit = {
    val c = map5()
    try {
      c.get(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** containsKey(null) of nonempty map throws NPE
   */
  @Test def testContainsKey_NullPointerException(): Unit = {
    val c = map5()
    try {
      c.containsKey(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** containsValue(null) throws NPE
   */
  @Test def testContainsValue_NullPointerException(): Unit = {
    val c = new ConcurrentSkipListMap[Item, String]()
    try {
      c.containsValue(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** put(null,x) throws NPE
   */
  @Test def testPut1_NullPointerException(): Unit = {
    val c = map5()
    try {
      c.put(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** putIfAbsent(null, x) throws NPE
   */
  @Test def testPutIfAbsent1_NullPointerException(): Unit = {
    val c = map5()
    try {
      c.putIfAbsent(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** replace(null, x) throws NPE
   */
  @Test def testReplace_NullPointerException(): Unit = {
    val c = map5()
    try {
      c.replace(null, "A")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** replace(null, x, y) throws NPE
   */
  @Test def testReplaceValue_NullPointerException(): Unit = {
    val c = map5()
    try {
      c.replace(null, "A", "B")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** remove(null) throws NPE
   */
  @Test def testRemove1_NullPointerException(): Unit = {
    val c = new ConcurrentSkipListMap[Item, String]()
    c.put(zero, "A")
    try {
      c.remove(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** remove(null, x) throws NPE
   */
  @Test def testRemove2_NullPointerException(): Unit = {
    val c = new ConcurrentSkipListMap[Item, String]()
    c.put(zero, "asdads")
    try {
      c.remove(null, "whatever")
      shouldThrow()
    } catch {
      case success: NullPointerException =>
    }
  }

  /** remove(x, null) returns false
   */
  @Test def testRemove3(): Unit = {
    val c = new ConcurrentSkipListMap[Item, String]()
    c.put(zero, "asdads")
    assertFalse(c.remove("sadsdf", null))
  }

  /** A cloned map equals original
   */
  @Test def testClone(): Unit = {
    val x = map5()
    val y = x.clone()

    assertNotSame(x, y)
    mustEqual(x.size, y.size)
    mustEqual(x.toString, y.toString)
    mustEqual(x, y)
    mustEqual(y, x)
    y.clear()
    assertTrue(y.isEmpty)
    assertFalse(x.equals(y))
  }

  // /**
  //  * A deserialized/reserialized map equals original
  //  * Unsupported: serialClone / ObjectInputStream unavailable on Scala Native
  //  */
  // @Test def testSerialization(): Unit = {
  //   val x: NavigableMap[Item, String] = map5()
  //   val y: NavigableMap[Item, String] = serialClone(x)
  //
  //   assertNotSame(x, y)
  //   mustEqual(x.size, y.size)
  //   mustEqual(x.toString, y.toString)
  //   mustEqual(x, y)
  //   mustEqual(y, x)
  //   y.clear()
  //   assertTrue(y.isEmpty)
  //   assertFalse(x.equals(y))
  // }

  /** subMap returns map with keys in requested range
   */
  @Test def testSubMapContents(): Unit = {
    val map = map5()
    val sm = map.subMap(two, true, four, false)
    mustEqual(two, sm.firstKey)
    mustEqual(three, sm.lastKey)
    mustEqual(2, sm.size)
    assertFalse(sm.containsKey(one))
    assertTrue(sm.containsKey(two))
    assertTrue(sm.containsKey(three))
    assertFalse(sm.containsKey(four))
    assertFalse(sm.containsKey(five))
    val i = sm.keySet.iterator
    var k = i.next()
    mustEqual(two, k)
    k = i.next()
    mustEqual(three, k)
    assertFalse(i.hasNext)
    val r = sm.descendingKeySet.iterator
    k = r.next()
    mustEqual(three, k)
    k = r.next()
    mustEqual(two, k)
    assertFalse(r.hasNext)

    val j = sm.keySet.iterator
    j.next()
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
    val map = map5()
    val sm = map.subMap(two, true, three, false)
    mustEqual(1, sm.size)
    mustEqual(two, sm.firstKey)
    mustEqual(two, sm.lastKey)
    assertFalse(sm.containsKey(one))
    assertTrue(sm.containsKey(two))
    assertFalse(sm.containsKey(three))
    assertFalse(sm.containsKey(four))
    assertFalse(sm.containsKey(five))
    val i = sm.keySet.iterator
    var k = i.next()
    mustEqual(two, k)
    assertFalse(i.hasNext)
    val r = sm.descendingKeySet.iterator
    k = r.next()
    mustEqual(two, k)
    assertFalse(r.hasNext)

    val j = sm.keySet.iterator
    j.next()
    j.remove()
    assertFalse(map.containsKey(two))
    mustEqual(4, map.size)
    mustEqual(0, sm.size)
    assertTrue(sm.isEmpty)
    assertSame(sm.remove(three), null)
    mustEqual(4, map.size)
  }

  /** headMap returns map with keys in requested range
   */
  @Test def testHeadMapContents(): Unit = {
    val map = map5()
    val sm = map.headMap(four, false)
    assertTrue(sm.containsKey(one))
    assertTrue(sm.containsKey(two))
    assertTrue(sm.containsKey(three))
    assertFalse(sm.containsKey(four))
    assertFalse(sm.containsKey(five))
    val i = sm.keySet.iterator
    var k = i.next()
    mustEqual(one, k)
    k = i.next()
    mustEqual(two, k)
    k = i.next()
    mustEqual(three, k)
    assertFalse(i.hasNext)
    sm.clear()
    assertTrue(sm.isEmpty)
    mustEqual(2, map.size)
    mustEqual(four, map.firstKey)
  }

  /** tailMap returns map with keys in requested range
   */
  @Test def testTailMapContents(): Unit = {
    val map = map5()
    val sm = map.tailMap(two, true)
    assertFalse(sm.containsKey(one))
    assertTrue(sm.containsKey(two))
    assertTrue(sm.containsKey(three))
    assertTrue(sm.containsKey(four))
    assertTrue(sm.containsKey(five))
    val i = sm.keySet.iterator
    var k = i.next()
    mustEqual(two, k)
    k = i.next()
    mustEqual(three, k)
    k = i.next()
    mustEqual(four, k)
    k = i.next()
    mustEqual(five, k)
    assertFalse(i.hasNext)
    val r = sm.descendingKeySet.iterator
    k = r.next()
    mustEqual(five, k)
    k = r.next()
    mustEqual(four, k)
    k = r.next()
    mustEqual(three, k)
    k = r.next()
    mustEqual(two, k)
    assertFalse(r.hasNext)

    val ei = sm.entrySet.iterator
    var e = ei.next()
    mustEqual(two, e.getKey)
    mustEqual("B", e.getValue)
    e = ei.next()
    mustEqual(three, e.getKey)
    mustEqual("C", e.getValue)
    e = ei.next()
    mustEqual(four, e.getKey)
    mustEqual("D", e.getValue)
    e = ei.next()
    mustEqual(five, e.getKey)
    mustEqual("E", e.getValue)
    assertFalse(i.hasNext)

    val ssm = sm.tailMap(four, true)
    mustEqual(four, ssm.firstKey)
    mustEqual(five, ssm.lastKey)
    mustEqual("D", ssm.remove(four))
    mustEqual(1, ssm.size)
    mustEqual(3, sm.size)
    mustEqual(4, map.size)
  }

  var rnd = new Random(666)
  var bs: BitSet = _

  /** Submaps of submaps subdivide correctly
   */
  @Test def testRecursiveSubMaps(): Unit = {
    val mapSize = if (expensiveTests) 1000 else 100
    val map = newMap()
    bs = new BitSet(mapSize)

    populate(map, mapSize)
    check(map, 0, mapSize - 1, true)
    check(map.descendingMap, 0, mapSize - 1, false)

    mutateMap(map, 0, mapSize - 1)
    check(map, 0, mapSize - 1, true)
    check(map.descendingMap, 0, mapSize - 1, false)

    bashSubMap(
      map.subMap(zero, true, itemFor(mapSize), false),
      0,
      mapSize - 1,
      true
    )
  }

  def populate(map: NavigableMap[Item, Item], limit: Int): Unit = {
    var i = 0
    val n = 2 * limit / 3
    while (i < n) {
      val key = rnd.nextInt(limit)
      put(map, key)
      i += 1
    }
  }

  def mutateMap(map: NavigableMap[Item, Item], min: Int, max: Int): Unit = {
    val size = map.size
    val rangeSize = max - min + 1

    // Remove a bunch of entries directly
    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(map, min - 5 + rnd.nextInt(rangeSize + 10))
      i += 1
    }

    // Remove a bunch of entries with iterator
    val it = map.keySet.iterator
    while (it.hasNext) {
      if (rnd.nextBoolean) {
        bs.clear(it.next().value)
        it.remove()
      }
    }

    // Add entries till we're back to original size
    while (map.size < size) {
      val key = min + rnd.nextInt(rangeSize)
      assertTrue(key >= min && key <= max)
      put(map, key)
    }
  }

  def mutateSubMap(
      map: NavigableMap[Item, Item],
      min: Int,
      max: Int
  ): Unit = {
    val size = map.size
    val rangeSize = max - min + 1

    // Remove a bunch of entries directly
    var i = 0
    val n = rangeSize / 2
    while (i < n) {
      remove(map, min - 5 + rnd.nextInt(rangeSize + 10))
      i += 1
    }

    // Remove a bunch of entries with iterator
    val it = map.keySet.iterator
    while (it.hasNext) {
      if (rnd.nextBoolean) {
        bs.clear(it.next().value)
        it.remove()
      }
    }

    // Add entries till we're back to original size
    while (map.size < size) {
      val key = min - 5 + rnd.nextInt(rangeSize + 10)
      if (key >= min && key <= max) {
        put(map, key)
      } else {
        try {
          map.put(itemFor(key), itemFor(2 * key))
          shouldThrow()
        } catch {
          case success: IllegalArgumentException =>
        }
      }
    }
  }

  def put(map: NavigableMap[Item, Item], key: Int): Unit = {
    if (map.put(itemFor(key), itemFor(2 * key)) == null)
      bs.set(key)
  }

  def remove(map: NavigableMap[Item, Item], key: Int): Unit = {
    if (map.remove(itemFor(key)) != null)
      bs.clear(key)
  }

  def bashSubMap(
      map: NavigableMap[Item, Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): Unit = {
    check(map, min, max, ascending)
    check(map.descendingMap, min, max, !ascending)

    mutateSubMap(map, min, max)
    check(map, min, max, ascending)
    check(map.descendingMap, min, max, !ascending)

    // Recurse
    if (max - min < 2) return
    val midPoint = (min + max) / 2

    // headMap - pick direction and endpoint inclusion randomly
    var incl = rnd.nextBoolean
    val hm = map.headMap(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean)
        bashSubMap(hm, min, midPoint - (if (incl) 0 else 1), true)
      else
        bashSubMap(
          hm.descendingMap,
          min,
          midPoint - (if (incl) 0 else 1),
          false
        )
    } else {
      if (rnd.nextBoolean)
        bashSubMap(hm, midPoint + (if (incl) 0 else 1), max, false)
      else
        bashSubMap(
          hm.descendingMap,
          midPoint + (if (incl) 0 else 1),
          max,
          true
        )
    }

    // tailMap - pick direction and endpoint inclusion randomly
    incl = rnd.nextBoolean
    val tm = map.tailMap(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean)
        bashSubMap(tm, midPoint + (if (incl) 0 else 1), max, true)
      else
        bashSubMap(
          tm.descendingMap,
          midPoint + (if (incl) 0 else 1),
          max,
          false
        )
    } else {
      if (rnd.nextBoolean) {
        bashSubMap(tm, min, midPoint - (if (incl) 0 else 1), false)
      } else {
        bashSubMap(
          tm.descendingMap,
          min,
          midPoint - (if (incl) 0 else 1),
          true
        )
      }
    }

    // subMap - pick direction and endpoint inclusion randomly
    val rangeSize = max - min + 1
    val endpoints = new Array[Int](2)
    endpoints(0) = min + rnd.nextInt(rangeSize)
    endpoints(1) = min + rnd.nextInt(rangeSize)
    Arrays.sort(endpoints)
    val lowIncl = rnd.nextBoolean
    val highIncl = rnd.nextBoolean
    if (ascending) {
      val sm = map.subMap(
        itemFor(endpoints(0)),
        lowIncl,
        itemFor(endpoints(1)),
        highIncl
      )
      if (rnd.nextBoolean)
        bashSubMap(
          sm,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          true
        )
      else
        bashSubMap(
          sm.descendingMap,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          false
        )
    } else {
      val sm = map.subMap(
        itemFor(endpoints(1)),
        highIncl,
        itemFor(endpoints(0)),
        lowIncl
      )
      if (rnd.nextBoolean)
        bashSubMap(
          sm,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          false
        )
      else
        bashSubMap(
          sm.descendingMap,
          endpoints(0) + (if (lowIncl) 0 else 1),
          endpoints(1) - (if (highIncl) 0 else 1),
          true
        )
    }
  }

  /** min and max are both inclusive. If max < min, interval is empty.
   */
  def check(
      map: NavigableMap[Item, Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): Unit = {
    class ReferenceSet {
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
      def lowerAscending(key: Int): Int =
        floorAscending(key - 1)
      def floorAscending(key0: Int): Int = {
        var key = key0
        if (key < min) return -1
        else if (key > max) key = max

        // BitSet should support this! Test would run much faster
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
      def higherAscending(key: Int): Int =
        ceilingAscending(key + 1)
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

    // Test contents using containsKey
    var size = 0
    var i = min
    while (i <= max) {
      val bsContainsI = bs.get(i)
      mustEqual(bsContainsI, map.containsKey(itemFor(i)))
      if (bsContainsI) size += 1
      i += 1
    }
    mustEqual(size, map.size)

    // Test contents using contains keySet iterator
    var size2 = 0
    var previousKey = -1
    val kit = map.keySet.iterator
    while (kit.hasNext) {
      val key = kit.next()
      assertTrue(bs.get(key.value))
      size2 += 1
      assertTrue(
        previousKey < 0 ||
        (if (ascending) key.value - previousKey > 0
         else key.value - previousKey < 0)
      )
      previousKey = key.value
    }
    mustEqual(size2, size)

    // Test navigation ops
    var key = min - 1
    while (key <= max + 1) {
      val k = itemFor(key)
      assertEq(map.lowerKey(k), rs.lower(key))
      assertEq(map.floorKey(k), rs.floor(key))
      assertEq(map.higherKey(k), rs.higher(key))
      assertEq(map.ceilingKey(k), rs.ceiling(key))
      key += 1
    }

    // Test extrema
    if (map.size != 0) {
      assertEq(map.firstKey, rs.first())
      assertEq(map.lastKey, rs.last())
    } else {
      mustEqual(rs.first(), -1)
      mustEqual(rs.last(), -1)
      try {
        map.firstKey
        shouldThrow()
      } catch {
        case success: NoSuchElementException =>
      }
      try {
        map.lastKey
        shouldThrow()
      } catch {
        case success: NoSuchElementException =>
      }
    }
  }
}
