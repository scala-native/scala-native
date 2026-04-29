/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{ConcurrentNavigableMap, ConcurrentSkipListMap}
import java.util.{
  ArrayList, Arrays, Collection, Iterator, Map, NavigableMap, Set, SortedMap
}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

object ConcurrentSkipListSubMapTest {
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

  private def map5(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    assertTrue(map.isEmpty())
    map.put(iZero, "Z")
    map.put(iOne, "A")
    map.put(iFive, "E")
    map.put(iThree, "C")
    map.put(iTwo, "B")
    map.put(iFour, "D")
    map.put(iSeven, "F")
    assertFalse(map.isEmpty())
    mustEqual(7, map.size())
    map.subMap(iOne, true, iSeven, false)
  }

  private def dmap5(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    assertTrue(map.isEmpty())
    map.put(nOne, "A")
    map.put(nFive, "E")
    map.put(nThree, "C")
    map.put(nTwo, "B")
    map.put(nFour, "D")
    assertFalse(map.isEmpty())
    mustEqual(5, map.size())
    map.descendingMap()
  }

  private def map0(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    assertTrue(map.isEmpty())
    map.tailMap(iOne, true)
  }

  private def dmap0(): ConcurrentNavigableMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    assertTrue(map.isEmpty())
    map
  }
}

class ConcurrentSkipListSubMapTest extends JSR166Test {
  import ConcurrentSkipListSubMapTest._
  import JSR166Test._

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
    assertTrue(map.containsKey(iOne))
    assertFalse(map.containsKey(iZero))
  }

  @Test def testContainsValue(): Unit = {
    val map = map5()
    assertTrue(map.containsValue("A"))
    assertFalse(map.containsValue("Z"))
  }

  @Test def testGet(): Unit = {
    val map = map5()
    mustEqual("A", map.get(iOne))
    val empty = map0()
    assertNull(empty.get(iOne))
  }

  @Test def testIsEmpty(): Unit = {
    val empty = map0()
    val map = map5()
    assertTrue(empty.isEmpty())
    assertFalse(map.isEmpty())
  }

  @Test def testFirstKey(): Unit = {
    val map = map5()
    mustEqual(iOne, map.firstKey())
  }

  @Test def testLastKey(): Unit = {
    val map = map5()
    mustEqual(iFive, map.lastKey())
  }

  @Test def testKeySet(): Unit = {
    val map = map5()
    val s = map.keySet()
    mustEqual(5, s.size())
    mustContain(s, iOne)
    mustContain(s, iTwo)
    mustContain(s, iThree)
    mustContain(s, iFour)
    mustContain(s, iFive)
  }

  @Test def testKeySetOrder(): Unit = {
    val map = map5()
    val i = map.keySet().iterator()
    var last = i.next()
    mustEqual(last, iOne)
    while (i.hasNext()) {
      val k = i.next()
      assertTrue(last.compareTo(k) < 0)
      last = k
    }
  }

  @Test def testValues(): Unit = {
    val map = map5()
    val s = map.values()
    mustEqual(5, s.size())
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  @Test def testKeySetToArray(): Unit = {
    val map = map5()
    val s = map.keySet()
    val ar = s.toArray()
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    mustEqual(5, ar.length)
    ar(0) = nTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  @Test def testDescendingKeySetToArray(): Unit = {
    val map = map5()
    val s = map.descendingKeySet()
    val ar = s.toArray(new Array[Item](0))
    mustEqual(5, ar.length)
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    ar(0) = nTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  @Test def testValuesToArray(): Unit = {
    val map = map5()
    val ar = map.values().toArray(new Array[String](0))
    val s = new ArrayList[String]()
    ar.foreach(s.add)
    mustEqual(5, ar.length)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  @Test def testEntrySet(): Unit = {
    val map = map5()
    val s = map.entrySet()
    mustEqual(5, s.size())
    val it = s.iterator()
    while (it.hasNext()) {
      val e = it.next()
      assertTrue(
        (e.getKey().equals(iOne) && e.getValue().equals("A")) ||
        (e.getKey().equals(iTwo) && e.getValue().equals("B")) ||
        (e.getKey().equals(iThree) && e.getValue().equals("C")) ||
        (e.getKey().equals(iFour) && e.getValue().equals("D")) ||
        (e.getKey().equals(iFive) && e.getValue().equals("E"))
      )
    }
  }

  @Test def testPutAll(): Unit = {
    val empty = map0()
    val map = map5()
    empty.putAll(map)
    mustEqual(5, empty.size())
    assertTrue(empty.containsKey(iOne))
    assertTrue(empty.containsKey(iTwo))
    assertTrue(empty.containsKey(iThree))
    assertTrue(empty.containsKey(iFour))
    assertTrue(empty.containsKey(iFive))
  }

  @Test def testPutIfAbsent(): Unit = {
    val map = map5()
    map.putIfAbsent(iSix, "Z")
    assertTrue(map.containsKey(iSix))
  }

  @Test def testPutIfAbsent2(): Unit = {
    val map = map5()
    mustEqual("A", map.putIfAbsent(iOne, "Z"))
  }

  @Test def testReplace(): Unit = {
    val map = map5()
    assertNull(map.replace(iSix, "Z"))
    assertFalse(map.containsKey(iSix))
  }

  @Test def testReplace2(): Unit = {
    val map = map5()
    assertNotNull(map.replace(iOne, "Z"))
    mustEqual("Z", map.get(iOne))
  }

  @Test def testReplaceValue(): Unit = {
    val map = map5()
    mustEqual("A", map.get(iOne))
    assertFalse(map.replace(iOne, "Z", "Z"))
    mustEqual("A", map.get(iOne))
  }

  @Test def testReplaceValue2(): Unit = {
    val map = map5()
    mustEqual("A", map.get(iOne))
    assertTrue(map.replace(iOne, "A", "Z"))
    mustEqual("Z", map.get(iOne))
  }

  @Test def testRemove(): Unit = {
    val map = map5()
    map.remove(iFive)
    mustEqual(4, map.size())
    assertFalse(map.containsKey(iFive))
  }

  @Test def testRemove2(): Unit = {
    val map = map5()
    assertTrue(map.containsKey(iFive))
    mustEqual("E", map.get(iFive))
    map.remove(iFive, "E")
    mustEqual(4, map.size())
    assertFalse(map.containsKey(iFive))
    map.remove(iFour, "A")
    mustEqual(4, map.size())
    assertTrue(map.containsKey(iFour))
  }

  @Test def testLowerEntry(): Unit = {
    val map = map5()
    mustEqual(iTwo, map.lowerEntry(iThree).getKey())
    mustEqual(iFive, map.lowerEntry(iSix).getKey())
    assertNull(map.lowerEntry(iOne))
    assertNull(map.lowerEntry(iZero))
  }

  @Test def testHigherEntry(): Unit = {
    val map = map5()
    mustEqual(iFour, map.higherEntry(iThree).getKey())
    mustEqual(iOne, map.higherEntry(iZero).getKey())
    assertNull(map.higherEntry(iFive))
    assertNull(map.higherEntry(iSix))
  }

  @Test def testFloorEntry(): Unit = {
    val map = map5()
    mustEqual(iThree, map.floorEntry(iThree).getKey())
    mustEqual(iFive, map.floorEntry(iSix).getKey())
    mustEqual(iOne, map.floorEntry(iOne).getKey())
    assertNull(map.floorEntry(iZero))
  }

  @Test def testCeilingEntry(): Unit = {
    val map = map5()
    mustEqual(iThree, map.ceilingEntry(iThree).getKey())
    mustEqual(iOne, map.ceilingEntry(iZero).getKey())
    mustEqual(iFive, map.ceilingEntry(iFive).getKey())
    assertNull(map.ceilingEntry(iSix))
  }

  @Test def testPollFirstEntry(): Unit = {
    val map = map5()
    var e = map.pollFirstEntry()
    mustEqual(iOne, e.getKey())
    mustEqual("A", e.getValue())
    e = map.pollFirstEntry()
    mustEqual(iTwo, e.getKey())
    map.put(iOne, "A")
    e = map.pollFirstEntry()
    mustEqual(iOne, e.getKey())
    mustEqual("A", e.getValue())
    e = map.pollFirstEntry()
    mustEqual(iThree, e.getKey())
    map.remove(iFour)
    e = map.pollFirstEntry()
    mustEqual(iFive, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("A"))
    e = map.pollFirstEntry()
    assertNull(e)
  }

  @Test def testPollLastEntry(): Unit = {
    val map = map5()
    var e = map.pollLastEntry()
    mustEqual(iFive, e.getKey())
    mustEqual("E", e.getValue())
    e = map.pollLastEntry()
    mustEqual(iFour, e.getKey())
    map.put(iFive, "E")
    e = map.pollLastEntry()
    mustEqual(iFive, e.getKey())
    mustEqual("E", e.getValue())
    e = map.pollLastEntry()
    mustEqual(iThree, e.getKey())
    map.remove(iTwo)
    e = map.pollLastEntry()
    mustEqual(iOne, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("E"))
    e = map.pollLastEntry()
    assertNull(e)
  }

  @Test def testSize(): Unit = {
    val map = map5()
    val empty = map0()
    mustEqual(0, empty.size())
    mustEqual(5, map.size())
  }

  @Test def testToString(): Unit = {
    val map = map5()
    val s = map.toString()
    for (i <- 1 to 5)
      assertTrue(s.contains(String.valueOf(i)))
  }

  @Test def testGet_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().get(null))

  @Test def testContainsKey_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().containsKey(null))

  @Test def testContainsValue_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map0().containsValue(null))

  @Test def testPut1_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().put(null, "whatever"))

  @Test def testPutIfAbsent1_NullPointerException(): Unit =
    assertThrows(
      classOf[NullPointerException],
      map5().putIfAbsent(null, "whatever")
    )

  @Test def testReplace_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().replace(null, "A"))

  @Test def testReplaceValue_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().replace(null, "A", "B"))

  @Test def testRemove1_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().remove(null))

  @Test def testRemove2_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], map5().remove(null, "whatever"))

  @Ignore("scala-native#4852: ObjectInputStream is unsupported")
  @Test def testSerialization(): Unit = {}

  @Test def testSubMapContents(): Unit = {
    val map = map5()
    val sm = map.subMap(iTwo, iFour)
    mustEqual(iTwo, sm.firstKey())
    mustEqual(iThree, sm.lastKey())
    mustEqual(2, sm.size())
    assertFalse(sm.containsKey(iOne))
    assertTrue(sm.containsKey(iTwo))
    assertTrue(sm.containsKey(iThree))
    assertFalse(sm.containsKey(iFour))
    assertFalse(sm.containsKey(iFive))
    val i = sm.keySet().iterator()
    mustEqual(iTwo, i.next())
    mustEqual(iThree, i.next())
    assertFalse(i.hasNext())
    val j = sm.keySet().iterator()
    j.next()
    j.remove()
    assertFalse(map.containsKey(iTwo))
    mustEqual(4, map.size())
    mustEqual(1, sm.size())
    mustEqual(iThree, sm.firstKey())
    mustEqual(iThree, sm.lastKey())
    mustEqual("C", sm.remove(iThree))
    assertTrue(sm.isEmpty())
    mustEqual(3, map.size())
  }

  @Test def testSubMapContents2(): Unit = {
    val map = map5()
    val sm = map.subMap(iTwo, iThree)
    mustEqual(1, sm.size())
    mustEqual(iTwo, sm.firstKey())
    mustEqual(iTwo, sm.lastKey())
    assertFalse(sm.containsKey(iOne))
    assertTrue(sm.containsKey(iTwo))
    assertFalse(sm.containsKey(iThree))
    assertFalse(sm.containsKey(iFour))
    assertFalse(sm.containsKey(iFive))
    val i = sm.keySet().iterator()
    mustEqual(iTwo, i.next())
    assertFalse(i.hasNext())
    val j = sm.keySet().iterator()
    j.next()
    j.remove()
    assertFalse(map.containsKey(iTwo))
    mustEqual(4, map.size())
    mustEqual(0, sm.size())
    assertTrue(sm.isEmpty())
    assertSame(sm.remove(iThree), null)
    mustEqual(4, map.size())
  }

  @Test def testHeadMapContents(): Unit = {
    val map = map5()
    val sm = map.headMap(iFour)
    assertTrue(sm.containsKey(iOne))
    assertTrue(sm.containsKey(iTwo))
    assertTrue(sm.containsKey(iThree))
    assertFalse(sm.containsKey(iFour))
    assertFalse(sm.containsKey(iFive))
    val i = sm.keySet().iterator()
    mustEqual(iOne, i.next())
    mustEqual(iTwo, i.next())
    mustEqual(iThree, i.next())
    assertFalse(i.hasNext())
    sm.clear()
    assertTrue(sm.isEmpty())
    mustEqual(2, map.size())
    mustEqual(iFour, map.firstKey())
  }

  @Test def testTailMapContents(): Unit = {
    val map = map5()
    val sm = map.tailMap(iTwo)
    assertFalse(sm.containsKey(iOne))
    assertTrue(sm.containsKey(iTwo))
    assertTrue(sm.containsKey(iThree))
    assertTrue(sm.containsKey(iFour))
    assertTrue(sm.containsKey(iFive))
    val i = sm.keySet().iterator()
    mustEqual(iTwo, i.next())
    mustEqual(iThree, i.next())
    mustEqual(iFour, i.next())
    mustEqual(iFive, i.next())
    assertFalse(i.hasNext())

    val ei = sm.entrySet().iterator()
    var e = ei.next()
    mustEqual(iTwo, e.getKey())
    mustEqual("B", e.getValue())
    e = ei.next()
    mustEqual(iThree, e.getKey())
    mustEqual("C", e.getValue())
    e = ei.next()
    mustEqual(iFour, e.getKey())
    mustEqual("D", e.getValue())
    e = ei.next()
    mustEqual(iFive, e.getKey())
    mustEqual("E", e.getValue())
    assertFalse(ei.hasNext())

    val ssm = sm.tailMap(iFour)
    mustEqual(iFour, ssm.firstKey())
    mustEqual(iFive, ssm.lastKey())
    mustEqual("D", ssm.remove(iFour))
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, map.size())
  }

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
    assertTrue(map.containsKey(nOne))
    assertFalse(map.containsKey(iZero))
  }

  @Test def testDescendingContainsValue(): Unit = {
    val map = dmap5()
    assertTrue(map.containsValue("A"))
    assertFalse(map.containsValue("Z"))
  }

  @Test def testDescendingGet(): Unit = {
    val map = dmap5()
    mustEqual("A", map.get(nOne))
    val empty = dmap0()
    assertNull(empty.get(nOne))
  }

  @Test def testDescendingIsEmpty(): Unit = {
    val empty = dmap0()
    val map = dmap5()
    assertTrue(empty.isEmpty())
    assertFalse(map.isEmpty())
  }

  @Test def testDescendingFirstKey(): Unit = {
    val map = dmap5()
    mustEqual(nOne, map.firstKey())
  }

  @Test def testDescendingLastKey(): Unit = {
    val map = dmap5()
    mustEqual(nFive, map.lastKey())
  }

  @Test def testDescendingKeySet(): Unit = {
    val map = dmap5()
    val s = map.keySet()
    mustEqual(5, s.size())
    mustContain(s, nOne)
    mustContain(s, nTwo)
    mustContain(s, nThree)
    mustContain(s, nFour)
    mustContain(s, nFive)
  }

  @Test def testDescendingKeySetOrder(): Unit = {
    val map = dmap5()
    val i = map.keySet().iterator()
    var last = i.next()
    mustEqual(last, nOne)
    while (i.hasNext()) {
      val k = i.next()
      assertTrue(last.compareTo(k) > 0)
      last = k
    }
  }

  @Test def testDescendingValues(): Unit = {
    val map = dmap5()
    val s = map.values()
    mustEqual(5, s.size())
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  @Test def testDescendingAscendingKeySetToArray(): Unit = {
    val map = dmap5()
    val s = map.keySet()
    val ar = s.toArray(new Array[Item](0))
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    mustEqual(5, ar.length)
    ar(0) = nTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  @Test def testDescendingDescendingKeySetToArray(): Unit = {
    val map = dmap5()
    val s = map.descendingKeySet()
    val ar = s.toArray(new Array[Item](0))
    mustEqual(5, ar.length)
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    ar(0) = nTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  @Test def testDescendingValuesToArray(): Unit = {
    val map = dmap5()
    val ar = map.values().toArray(new Array[String](0))
    val s = new ArrayList[String]()
    ar.foreach(s.add)
    mustEqual(5, ar.length)
    assertTrue(s.contains("A"))
    assertTrue(s.contains("B"))
    assertTrue(s.contains("C"))
    assertTrue(s.contains("D"))
    assertTrue(s.contains("E"))
  }

  @Test def testDescendingEntrySet(): Unit = {
    val map = dmap5()
    val s = map.entrySet()
    mustEqual(5, s.size())
    val it = s.iterator()
    while (it.hasNext()) {
      val e = it.next()
      assertTrue(
        (e.getKey().equals(nOne) && e.getValue().equals("A")) ||
        (e.getKey().equals(nTwo) && e.getValue().equals("B")) ||
        (e.getKey().equals(nThree) && e.getValue().equals("C")) ||
        (e.getKey().equals(nFour) && e.getValue().equals("D")) ||
        (e.getKey().equals(nFive) && e.getValue().equals("E"))
      )
    }
  }

  @Test def testDescendingPutAll(): Unit = {
    val empty = dmap0()
    val map = dmap5()
    empty.putAll(map)
    mustEqual(5, empty.size())
    assertTrue(empty.containsKey(nOne))
    assertTrue(empty.containsKey(nTwo))
    assertTrue(empty.containsKey(nThree))
    assertTrue(empty.containsKey(nFour))
    assertTrue(empty.containsKey(nFive))
  }

  @Test def testDescendingPutIfAbsent(): Unit = {
    val map = dmap5()
    map.putIfAbsent(iSix, "Z")
    assertTrue(map.containsKey(iSix))
  }

  @Test def testDescendingPutIfAbsent2(): Unit = {
    val map = dmap5()
    mustEqual("A", map.putIfAbsent(nOne, "Z"))
  }

  @Test def testDescendingReplace(): Unit = {
    val map = dmap5()
    assertNull(map.replace(iSix, "Z"))
    assertFalse(map.containsKey(iSix))
  }

  @Test def testDescendingReplace2(): Unit = {
    val map = dmap5()
    assertNotNull(map.replace(nOne, "Z"))
    mustEqual("Z", map.get(nOne))
  }

  @Test def testDescendingReplaceValue(): Unit = {
    val map = dmap5()
    mustEqual("A", map.get(nOne))
    assertFalse(map.replace(nOne, "Z", "Z"))
    mustEqual("A", map.get(nOne))
  }

  @Test def testDescendingReplaceValue2(): Unit = {
    val map = dmap5()
    mustEqual("A", map.get(nOne))
    assertTrue(map.replace(nOne, "A", "Z"))
    mustEqual("Z", map.get(nOne))
  }

  @Test def testDescendingRemove(): Unit = {
    val map = dmap5()
    map.remove(nFive)
    mustEqual(4, map.size())
    assertFalse(map.containsKey(nFive))
  }

  @Test def testDescendingRemove2(): Unit = {
    val map = dmap5()
    assertTrue(map.containsKey(nFive))
    mustEqual("E", map.get(nFive))
    map.remove(nFive, "E")
    mustEqual(4, map.size())
    assertFalse(map.containsKey(nFive))
    map.remove(nFour, "A")
    mustEqual(4, map.size())
    assertTrue(map.containsKey(nFour))
  }

  @Test def testDescendingLowerEntry(): Unit = {
    val map = dmap5()
    mustEqual(nTwo, map.lowerEntry(nThree).getKey())
    mustEqual(nFive, map.lowerEntry(nSix).getKey())
    assertNull(map.lowerEntry(nOne))
    assertNull(map.lowerEntry(iZero))
  }

  @Test def testDescendingHigherEntry(): Unit = {
    val map = dmap5()
    mustEqual(nFour, map.higherEntry(nThree).getKey())
    mustEqual(nOne, map.higherEntry(iZero).getKey())
    assertNull(map.higherEntry(nFive))
    assertNull(map.higherEntry(nSix))
  }

  @Test def testDescendingFloorEntry(): Unit = {
    val map = dmap5()
    mustEqual(nThree, map.floorEntry(nThree).getKey())
    mustEqual(nFive, map.floorEntry(nSix).getKey())
    mustEqual(nOne, map.floorEntry(nOne).getKey())
    assertNull(map.floorEntry(iZero))
  }

  @Test def testDescendingCeilingEntry(): Unit = {
    val map = dmap5()
    mustEqual(nThree, map.ceilingEntry(nThree).getKey())
    mustEqual(nOne, map.ceilingEntry(iZero).getKey())
    mustEqual(nFive, map.ceilingEntry(nFive).getKey())
    assertNull(map.ceilingEntry(nSix))
  }

  @Test def testDescendingPollFirstEntry(): Unit = {
    val map = dmap5()
    var e = map.pollFirstEntry()
    mustEqual(nOne, e.getKey())
    mustEqual("A", e.getValue())
    e = map.pollFirstEntry()
    mustEqual(nTwo, e.getKey())
    map.put(nOne, "A")
    e = map.pollFirstEntry()
    mustEqual(nOne, e.getKey())
    mustEqual("A", e.getValue())
    e = map.pollFirstEntry()
    mustEqual(nThree, e.getKey())
    map.remove(nFour)
    e = map.pollFirstEntry()
    mustEqual(nFive, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("A"))
    e = map.pollFirstEntry()
    assertNull(e)
  }

  @Test def testDescendingPollLastEntry(): Unit = {
    val map = dmap5()
    var e = map.pollLastEntry()
    mustEqual(nFive, e.getKey())
    mustEqual("E", e.getValue())
    e = map.pollLastEntry()
    mustEqual(nFour, e.getKey())
    map.put(nFive, "E")
    e = map.pollLastEntry()
    mustEqual(nFive, e.getKey())
    mustEqual("E", e.getValue())
    e = map.pollLastEntry()
    mustEqual(nThree, e.getKey())
    map.remove(nTwo)
    e = map.pollLastEntry()
    mustEqual(nOne, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("E"))
    e = map.pollLastEntry()
    assertNull(e)
  }

  @Test def testDescendingSize(): Unit = {
    val map = dmap5()
    val empty = dmap0()
    mustEqual(0, empty.size())
    mustEqual(5, map.size())
  }

  @Test def testDescendingToString(): Unit = {
    val map = dmap5()
    val s = map.toString()
    for (i <- 1 to 5)
      assertTrue(s.contains(String.valueOf(i)))
  }

  @Test def testDescendingGet_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], dmap5().get(null))

  @Test def testDescendingContainsKey_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], dmap5().containsKey(null))

  @Test def testDescendingContainsValue_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], dmap0().containsValue(null))

  @Test def testDescendingPut1_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], dmap5().put(null, "whatever"))

  @Test def testDescendingPutIfAbsent1_NullPointerException(): Unit =
    assertThrows(
      classOf[NullPointerException],
      dmap5().putIfAbsent(null, "whatever")
    )

  @Test def testDescendingReplace_NullPointerException(): Unit =
    assertThrows(
      classOf[NullPointerException],
      dmap5().replace(null, "whatever")
    )

  @Test def testDescendingReplaceValue_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], dmap5().replace(null, "A", "B"))

  @Test def testDescendingRemove1_NullPointerException(): Unit =
    assertThrows(classOf[NullPointerException], dmap5().remove(null))

  @Test def testDescendingRemove2_NullPointerException(): Unit =
    assertThrows(
      classOf[NullPointerException],
      dmap5().remove(null, "whatever")
    )

  @Ignore("scala-native#4852: ObjectInputStream is unsupported")
  @Test def testDescendingSerialization(): Unit = {}

  @Test def testDescendingSubMapContents(): Unit = {
    val map = dmap5()
    val sm = map.subMap(nTwo, nFour)
    mustEqual(nTwo, sm.firstKey())
    mustEqual(nThree, sm.lastKey())
    mustEqual(2, sm.size())
    assertFalse(sm.containsKey(nOne))
    assertTrue(sm.containsKey(nTwo))
    assertTrue(sm.containsKey(nThree))
    assertFalse(sm.containsKey(nFour))
    assertFalse(sm.containsKey(nFive))
    val i = sm.keySet().iterator()
    mustEqual(nTwo, i.next())
    mustEqual(nThree, i.next())
    assertFalse(i.hasNext())
    val j = sm.keySet().iterator()
    j.next()
    j.remove()
    assertFalse(map.containsKey(nTwo))
    mustEqual(4, map.size())
    mustEqual(1, sm.size())
    mustEqual(nThree, sm.firstKey())
    mustEqual(nThree, sm.lastKey())
    mustEqual("C", sm.remove(nThree))
    assertTrue(sm.isEmpty())
    mustEqual(3, map.size())
  }

  @Test def testDescendingSubMapContents2(): Unit = {
    val map = dmap5()
    val sm = map.subMap(nTwo, nThree)
    mustEqual(1, sm.size())
    mustEqual(nTwo, sm.firstKey())
    mustEqual(nTwo, sm.lastKey())
    assertFalse(sm.containsKey(nOne))
    assertTrue(sm.containsKey(nTwo))
    assertFalse(sm.containsKey(nThree))
    assertFalse(sm.containsKey(nFour))
    assertFalse(sm.containsKey(nFive))
    val i = sm.keySet().iterator()
    mustEqual(nTwo, i.next())
    assertFalse(i.hasNext())
    val j = sm.keySet().iterator()
    j.next()
    j.remove()
    assertFalse(map.containsKey(nTwo))
    mustEqual(4, map.size())
    mustEqual(0, sm.size())
    assertTrue(sm.isEmpty())
    assertSame(sm.remove(nThree), null)
    mustEqual(4, map.size())
  }

  @Test def testDescendingHeadMapContents(): Unit = {
    val map = dmap5()
    val sm = map.headMap(nFour)
    assertTrue(sm.containsKey(nOne))
    assertTrue(sm.containsKey(nTwo))
    assertTrue(sm.containsKey(nThree))
    assertFalse(sm.containsKey(nFour))
    assertFalse(sm.containsKey(nFive))
    val i = sm.keySet().iterator()
    mustEqual(nOne, i.next())
    mustEqual(nTwo, i.next())
    mustEqual(nThree, i.next())
    assertFalse(i.hasNext())
    sm.clear()
    assertTrue(sm.isEmpty())
    mustEqual(2, map.size())
    mustEqual(nFour, map.firstKey())
  }

  @Test def testDescendingTailMapContents(): Unit = {
    val map = dmap5()
    val sm = map.tailMap(nTwo)
    assertFalse(sm.containsKey(nOne))
    assertTrue(sm.containsKey(nTwo))
    assertTrue(sm.containsKey(nThree))
    assertTrue(sm.containsKey(nFour))
    assertTrue(sm.containsKey(nFive))
    val i = sm.keySet().iterator()
    mustEqual(nTwo, i.next())
    mustEqual(nThree, i.next())
    mustEqual(nFour, i.next())
    mustEqual(nFive, i.next())
    assertFalse(i.hasNext())

    val ei = sm.entrySet().iterator()
    var e = ei.next()
    mustEqual(nTwo, e.getKey())
    mustEqual("B", e.getValue())
    e = ei.next()
    mustEqual(nThree, e.getKey())
    mustEqual("C", e.getValue())
    e = ei.next()
    mustEqual(nFour, e.getKey())
    mustEqual("D", e.getValue())
    e = ei.next()
    mustEqual(nFive, e.getKey())
    mustEqual("E", e.getValue())
    assertFalse(ei.hasNext())

    val ssm = sm.tailMap(nFour)
    mustEqual(nFour, ssm.firstKey())
    mustEqual(nFive, ssm.lastKey())
    mustEqual("D", ssm.remove(nFour))
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, map.size())
  }
}
