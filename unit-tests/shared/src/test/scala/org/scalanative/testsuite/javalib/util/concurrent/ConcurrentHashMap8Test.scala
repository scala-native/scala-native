/* Ported from public-domain JSR166 TCK.
 * URL:
 *   https://gee.cs.oswego.edu/dl/concurrency-interest/
 *
 * Modified for Scala Native.
 */

/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.lang.{Long => JLong}
import java.util.concurrent.atomic.LongAdder
import java.util.concurrent.{ConcurrentHashMap, Executors}
import java.util.function.BiFunction
import java.{util => ju}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ConcurrentHashMap8Test extends JSR166Test {
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

  private def map5(): ConcurrentHashMap[Item, String] = {
    val map = new ConcurrentHashMap[Item, String](5)
    assertTrue(map.isEmpty())
    map.put(oneItem, "A")
    map.put(twoItem, "B")
    map.put(threeItem, "C")
    map.put(fourItem, "D")
    map.put(fiveItem, "E")
    assertFalse(map.isEmpty())
    mustEqual(5, map.size())
    map
  }

  private def populatedSet(n: Int): ju.Set[Item] = {
    val a = ConcurrentHashMap.newKeySet[Item]()
    assertTrue(a.isEmpty())
    var i = 0
    while (i < n) {
      mustAdd(a, i)
      i += 1
    }
    mustEqual(n == 0, a.isEmpty())
    mustEqual(n, a.size())
    a
  }

  private def populatedSet(elements: Array[Item]): ju.Set[Item] = {
    val a = ConcurrentHashMap.newKeySet[Item]()
    assertTrue(a.isEmpty())
    elements.foreach(e => assertTrue(a.add(e)))
    assertFalse(a.isEmpty())
    mustEqual(elements.length, a.size())
    a
  }

  @Test def testGetOrDefault(): Unit = {
    val map = map5()
    mustEqual(map.getOrDefault(oneItem, "Z"), "A")
    mustEqual(map.getOrDefault(sixItem, "Z"), "Z")
  }

  @Test def testComputeIfAbsent(): Unit = {
    val map = map5()
    map.computeIfAbsent(sixItem, _ => "Z")
    assertTrue(map.containsKey(sixItem))
  }

  @Test def testComputeIfAbsent2(): Unit =
    mustEqual("A", map5().computeIfAbsent(oneItem, _ => "Z"))

  @Test def testComputeIfAbsent3(): Unit = {
    val map = map5()
    map.computeIfAbsent(sixItem, _ => null)
    assertFalse(map.containsKey(sixItem))
  }

  @Test def testComputeIfPresent(): Unit = {
    val map = map5()
    map.computeIfPresent(sixItem, (_, _) => "Z")
    assertFalse(map.containsKey(sixItem))
  }

  @Test def testComputeIfPresent2(): Unit =
    mustEqual("Z", map5().computeIfPresent(oneItem, (_, _) => "Z"))

  @Test def testCompute(): Unit = {
    val map = map5()
    map.compute(sixItem, (_, _) => null)
    assertFalse(map.containsKey(sixItem))
  }

  @Test def testCompute2(): Unit =
    mustEqual("Z", map5().compute(sixItem, (_, _) => "Z"))

  @Test def testCompute3(): Unit =
    mustEqual("Z", map5().compute(oneItem, (_, _) => "Z"))

  @Test def testCompute4(): Unit = {
    val map = map5()
    map.compute(oneItem, (_, _) => null)
    assertFalse(map.containsKey(oneItem))
  }

  @Test def testMerge1(): Unit =
    mustEqual("Y", map5().merge(sixItem, "Y", (_, _) => "Z"))

  @Test def testMerge2(): Unit =
    mustEqual("Z", map5().merge(oneItem, "Y", (_, _) => "Z"))

  @Test def testMerge3(): Unit = {
    val map = map5()
    map.merge(oneItem, "Y", (_, _) => null)
    assertFalse(map.containsKey(oneItem))
  }

  @Test def testReplaceAll(): Unit = {
    val map = map5()
    map.replaceAll((x, y) => if (x.value > 3) "Z" else y)
    mustEqual("A", map.get(oneItem))
    mustEqual("B", map.get(twoItem))
    mustEqual("C", map.get(threeItem))
    mustEqual("Z", map.get(fourItem))
    mustEqual("Z", map.get(fiveItem))
  }

  @Test def testNewKeySet(): Unit =
    assertTrue(ConcurrentHashMap.newKeySet[Item]().isEmpty())

  @Test def testKeySetAddRemove(): Unit = {
    val map = map5()
    val set1 = map.keySet()
    val set2 = map.keySet("added")
    set2.add(sixItem)
    assertSame(
      map,
      set2.asInstanceOf[ConcurrentHashMap.KeySetView[Item, String]].getMap()
    )
    assertSame(
      map,
      set1.asInstanceOf[ConcurrentHashMap.KeySetView[Item, String]].getMap()
    )
    mustEqual(set2.size(), map.size())
    mustEqual(set1.size(), map.size())
    assertEquals(map.get(sixItem), "added")
    mustContain(set1, sixItem)
    mustContain(set2, sixItem)
    mustRemove(set2, sixItem)
    assertNull(map.get(sixItem))
    mustNotContain(set1, sixItem)
    mustNotContain(set2, sixItem)
  }

  @Test def testAddAll(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.addAll(ju.Arrays.asList(threeItem, fourItem, fiveItem)))
    mustEqual(6, full.size())
    assertFalse(full.addAll(ju.Arrays.asList(threeItem, fourItem, fiveItem)))
    mustEqual(6, full.size())
  }

  @Test def testAddAll2(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.addAll(ju.Arrays.asList(threeItem, fourItem, oneItem)))
    mustEqual(5, full.size())
    assertFalse(full.addAll(ju.Arrays.asList(threeItem, fourItem, oneItem)))
    mustEqual(5, full.size())
  }

  @Test def testAdd2(): Unit = {
    val full = populatedSet(3)
    assertFalse(full.add(oneItem))
    mustEqual(3, full.size())
  }

  @Test def testAdd3(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.add(threeItem))
    mustContain(full, threeItem)
    assertFalse(full.add(threeItem))
    mustContain(full, threeItem)
  }

  @Test def testAdd4(): Unit =
    assertThrows(
      classOf[UnsupportedOperationException],
      map5().keySet().add(threeItem)
    )

  @Test def testAdd5(): Unit =
    assertThrows(classOf[NullPointerException], populatedSet(3).add(null))

  @Test def testGetMappedValue(): Unit = {
    val map = map5()
    assertNull(map.keySet().getMappedValue())
    assertThrows(classOf[NullPointerException], map.keySet(null))
    val added = "added"
    val set = map.keySet(added)
    assertFalse(set.add(oneItem))
    assertTrue(set.add(sixItem))
    assertTrue(set.add(sevenItem))
    assertSame(added, set.getMappedValue())
    assertNotSame(added, map.get(oneItem))
    assertSame(added, map.get(sixItem))
    assertSame(added, map.get(sevenItem))
  }

  private def checkSpliteratorCharacteristics(
      sp: ju.Spliterator[_],
      required: Int
  ): Unit =
    mustEqual(required, required & sp.characteristics())

  @Test def testKeySetSpliterator(): Unit = {
    val adder = new LongAdder()
    val map = map5()
    val sp = map.keySet().spliterator()
    checkSpliteratorCharacteristics(
      sp,
      ju.Spliterator.CONCURRENT | ju.Spliterator.DISTINCT | ju.Spliterator.NONNULL
    )
    mustEqual(sp.estimateSize(), map.size().toLong)
    val sp2 = sp.trySplit()
    sp.forEachRemaining((x: Item) => adder.add(x.longValue()))
    val v = adder.sumThenReset()
    if (sp2 != null) sp2.forEachRemaining((x: Item) => adder.add(x.longValue()))
    mustEqual(v + adder.sum(), 15L)
  }

  @Test def testClear(): Unit = {
    val full = populatedSet(3)
    full.clear()
    mustEqual(0, full.size())
  }

  @Test def testContains(): Unit = {
    val full = populatedSet(3)
    mustContain(full, oneItem)
    mustNotContain(full, fiveItem)
  }

  @Test def testEquals(): Unit = {
    val a = populatedSet(3)
    val b = populatedSet(3)
    assertTrue(a.equals(b))
    assertTrue(b.equals(a))
    mustEqual(a.hashCode(), b.hashCode())
    a.add(minusOneItem)
    assertFalse(a.equals(b))
    assertFalse(b.equals(a))
    b.add(minusOneItem)
    assertTrue(a.equals(b))
    assertTrue(b.equals(a))
    mustEqual(a.hashCode(), b.hashCode())
  }

  @Test def testContainsAll(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.containsAll(ju.Arrays.asList()))
    assertTrue(full.containsAll(ju.Arrays.asList(oneItem)))
    assertTrue(full.containsAll(ju.Arrays.asList(oneItem, twoItem)))
    assertFalse(full.containsAll(ju.Arrays.asList(oneItem, twoItem, sixItem)))
    assertFalse(full.containsAll(ju.Arrays.asList(sixItem)))
  }

  @Test def testIsEmpty(): Unit = {
    assertTrue(populatedSet(0).isEmpty())
    assertFalse(populatedSet(3).isEmpty())
  }

  @Test def testIterator(): Unit = {
    val empty = ConcurrentHashMap.newKeySet[Item]()
    assertFalse(empty.iterator().hasNext())
    assertThrows(classOf[NoSuchElementException], empty.iterator().next())
    val elements = seqItems(20)
    shuffle(elements)
    val full = populatedSet(elements)
    val it = full.iterator()
    var j = 0
    while (j < 20) {
      assertTrue(it.hasNext())
      it.next()
      j += 1
    }
    assertIteratorExhausted(it)
  }

  @Test def testEmptyIterator(): Unit = {
    assertIteratorExhausted(ConcurrentHashMap.newKeySet[Item]().iterator())
    assertIteratorExhausted(
      new ConcurrentHashMap[Item, String]().entrySet().iterator()
    )
    assertIteratorExhausted(
      new ConcurrentHashMap[Item, String]().values().iterator()
    )
    assertIteratorExhausted(
      new ConcurrentHashMap[Item, String]().keySet().iterator()
    )
  }

  @Test def testIteratorRemove(): Unit = {
    val q = populatedSet(3)
    var it = q.iterator()
    val removed = it.next()
    it.remove()
    it = q.iterator()
    assertFalse(it.next().equals(removed))
    assertFalse(it.next().equals(removed))
    assertFalse(it.hasNext())
  }

  @Test def testToString(): Unit = {
    mustEqual("[]", ConcurrentHashMap.newKeySet[Item]().toString())
    val s = populatedSet(3).toString()
    var i = 0
    while (i < 3) {
      assertTrue(s.contains(String.valueOf(i)))
      i += 1
    }
  }

  @Test def testRemoveAll(): Unit = {
    val full = populatedSet(3)
    assertTrue(full.removeAll(ju.Arrays.asList(oneItem, twoItem)))
    mustEqual(1, full.size())
    assertFalse(full.removeAll(ju.Arrays.asList(oneItem, twoItem)))
    mustEqual(1, full.size())
  }

  @Test def testRemove(): Unit = {
    val full = populatedSet(3)
    full.remove(oneItem)
    mustNotContain(full, oneItem)
    mustEqual(2, full.size())
  }

  @Test def testSize(): Unit = {
    mustEqual(3, populatedSet(3).size())
    mustEqual(0, ConcurrentHashMap.newKeySet[Item]().size())
  }

  @Test def testToArray(): Unit = {
    val empty = ConcurrentHashMap.newKeySet[Item]().toArray()
    assertTrue(ju.Arrays.equals(new Array[Object](0), empty))
    assertSame(classOf[Array[Object]], empty.getClass())
    val elements = seqItems(20)
    shuffle(elements)
    val full = populatedSet(elements)
    assertTrue(
      ju.Arrays
        .asList(elements: _*)
        .containsAll(ju.Arrays.asList(full.toArray(): _*))
    )
    assertTrue(full.containsAll(ju.Arrays.asList(full.toArray(): _*)))
    assertSame(classOf[Array[Object]], full.toArray().getClass())
  }

  @Test def testToArray2(): Unit = {
    val empty = ConcurrentHashMap.newKeySet[Item]()
    var a = new Array[Item](0)
    assertSame(a, empty.toArray(a))
    a = new Array[Item](10)
    ju.Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    assertSame(a, empty.toArray(a))
    assertNull(a(0))
    var i = 1
    while (i < a.length) {
      mustEqual(42, a(i))
      i += 1
    }
    val elements = seqItems(20)
    shuffle(elements)
    val full = populatedSet(elements)
    ju.Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    assertTrue(
      ju.Arrays
        .asList(elements: _*)
        .containsAll(ju.Arrays.asList(full.toArray(a): _*))
    )
    i = 0
    while (i < a.length) {
      mustEqual(42, a(i))
      i += 1
    }
    assertSame(classOf[Array[Item]], full.toArray(a).getClass())
    a = new Array[Item](20)
    ju.Arrays.fill(a.asInstanceOf[Array[Object]], fortytwo)
    assertSame(a, full.toArray(a))
    assertTrue(
      ju.Arrays
        .asList(elements: _*)
        .containsAll(ju.Arrays.asList(full.toArray(a): _*))
    )
  }

  @Ignore("scala-native: ObjectInputStream/ObjectOutputStream are unsupported")
  @Test def testSerialization(): Unit = ()

  private final val BulkSize = 1000
  private lazy val bulkMap: ConcurrentHashMap[JLong, JLong] = {
    val m = new ConcurrentHashMap[JLong, JLong](BulkSize)
    var i = 0
    while (i < BulkSize) {
      m.put(JLong.valueOf(i.toLong), JLong.valueOf(2L * i))
      i += 1
    }
    m
  }
  private def expectedKeys: Long = BulkSize.toLong * (BulkSize - 1L) / 2L
  private def expectedValues: Long = BulkSize.toLong * (BulkSize - 1L)
  private def expectedMappings: Long = 3L * BulkSize * (BulkSize - 1L) / 2L

  private final class AddKeys
      extends BiFunction[
        ju.Map.Entry[JLong, JLong],
        ju.Map.Entry[JLong, JLong],
        ju.Map.Entry[JLong, JLong]
      ] {
    override def apply(
        x: ju.Map.Entry[JLong, JLong],
        y: ju.Map.Entry[JLong, JLong]
    ): ju.Map.Entry[JLong, JLong] =
      new ju.AbstractMap.SimpleEntry[JLong, JLong](
        JLong.valueOf(x.getKey().longValue() + y.getKey().longValue()),
        JLong.valueOf(1L)
      )
  }

  private def forEachKey(threshold: Long): Long = {
    val adder = new LongAdder()
    bulkMap.forEachKey(threshold, (x: JLong) => adder.add(x.longValue()))
    adder.sum()
  }
  private def forEachValue(threshold: Long): Long = {
    val adder = new LongAdder()
    bulkMap.forEachValue(threshold, (x: JLong) => adder.add(x.longValue()))
    adder.sum()
  }
  private def forEachMapping(threshold: Long): Long = {
    val adder = new LongAdder()
    bulkMap.forEach(
      threshold,
      (x: JLong, y: JLong) => adder.add(x.longValue() + y.longValue())
    )
    adder.sum()
  }
  private def forEachEntry(threshold: Long): Long = {
    val adder = new LongAdder()
    bulkMap.forEachEntry(
      threshold,
      (e: ju.Map.Entry[JLong, JLong]) =>
        adder.add(e.getKey().longValue() + e.getValue().longValue())
    )
    adder.sum()
  }

  @Test def testForEachKeySequentially(): Unit =
    mustEqual(forEachKey(Long.MaxValue), expectedKeys)
  @Test def testForEachValueSequentially(): Unit =
    mustEqual(forEachValue(Long.MaxValue), expectedValues)
  @Test def testForEachSequentially(): Unit =
    mustEqual(forEachMapping(Long.MaxValue), expectedMappings)
  @Test def testForEachEntrySequentially(): Unit =
    mustEqual(forEachEntry(Long.MaxValue), expectedMappings)
  @Test def testForEachKeyInParallel(): Unit =
    mustEqual(forEachKey(1L), expectedKeys)
  @Test def testForEachValueInParallel(): Unit =
    mustEqual(forEachValue(1L), expectedValues)
  @Test def testForEachInParallel(): Unit =
    mustEqual(forEachMapping(1L), expectedMappings)
  @Test def testForEachEntryInParallel(): Unit =
    mustEqual(forEachEntry(1L), expectedMappings)

  private def mappedForEachKey(threshold: Long): Long = {
    val adder = new LongAdder()
    bulkMap.forEachKey(
      threshold,
      (x: JLong) => JLong.valueOf(4L * x.longValue()),
      (x: JLong) => adder.add(x.longValue())
    )
    adder.sum()
  }
  private def mappedForEachValue(threshold: Long): Long = {
    val adder = new LongAdder()
    bulkMap.forEachValue(
      threshold,
      (x: JLong) => JLong.valueOf(4L * x.longValue()),
      (x: JLong) => adder.add(x.longValue())
    )
    adder.sum()
  }
  private def mappedForEach(threshold: Long): Long = {
    val adder = new LongAdder()
    bulkMap.forEach(
      threshold,
      (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue()),
      (x: JLong) => adder.add(x.longValue())
    )
    adder.sum()
  }
  private def mappedForEachEntry(threshold: Long): Long = {
    val adder = new LongAdder()
    bulkMap.forEachEntry(
      threshold,
      (e: ju.Map.Entry[JLong, JLong]) =>
        JLong.valueOf(e.getKey().longValue() + e.getValue().longValue()),
      (x: JLong) => adder.add(x.longValue())
    )
    adder.sum()
  }

  @Test def testMappedForEachKeySequentially(): Unit =
    mustEqual(mappedForEachKey(Long.MaxValue), 4L * expectedKeys)
  @Test def testMappedForEachValueSequentially(): Unit =
    mustEqual(mappedForEachValue(Long.MaxValue), 4L * expectedValues)
  @Test def testMappedForEachSequentially(): Unit =
    mustEqual(mappedForEach(Long.MaxValue), expectedMappings)
  @Test def testMappedForEachEntrySequentially(): Unit =
    mustEqual(mappedForEachEntry(Long.MaxValue), expectedMappings)
  @Test def testMappedForEachKeyInParallel(): Unit =
    mustEqual(mappedForEachKey(1L), 4L * expectedKeys)
  @Test def testMappedForEachValueInParallel(): Unit =
    mustEqual(mappedForEachValue(1L), 4L * expectedValues)
  @Test def testMappedForEachInParallel(): Unit =
    mustEqual(mappedForEach(1L), expectedMappings)
  @Test def testMappedForEachEntryInParallel(): Unit =
    mustEqual(mappedForEachEntry(1L), expectedMappings)

  @Test def testReduceKeysSequentially(): Unit =
    mustEqual(
      bulkMap
        .reduceKeys(
          Long.MaxValue,
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      expectedKeys
    )
  @Test def testReduceValuesSequentially(): Unit =
    mustEqual(
      bulkMap
        .reduceKeys(
          Long.MaxValue,
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      expectedKeys
    )
  @Test def testReduceEntriesSequentially(): Unit =
    mustEqual(
      bulkMap.reduceEntries(Long.MaxValue, new AddKeys()).getKey().longValue(),
      expectedKeys
    )
  @Test def testReduceKeysInParallel(): Unit =
    mustEqual(
      bulkMap
        .reduceKeys(
          1L,
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      expectedKeys
    )
  @Test def testReduceValuesInParallel(): Unit =
    mustEqual(
      bulkMap
        .reduceValues(
          1L,
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      expectedValues
    )
  @Test def testReduceEntriesInParallel(): Unit =
    mustEqual(
      bulkMap.reduceEntries(1L, new AddKeys()).getKey().longValue(),
      expectedKeys
    )

  @Test def testMapReduceKeysSequentially(): Unit =
    mustEqual(
      bulkMap
        .reduceKeys(
          Long.MaxValue,
          (x: JLong) => JLong.valueOf(4L * x.longValue()),
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      4L * expectedKeys
    )
  @Test def testMapReduceValuesSequentially(): Unit =
    mustEqual(
      bulkMap
        .reduceValues(
          Long.MaxValue,
          (x: JLong) => JLong.valueOf(4L * x.longValue()),
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      4L * expectedValues
    )
  @Test def testMappedReduceSequentially(): Unit =
    mustEqual(
      bulkMap
        .reduce(
          Long.MaxValue,
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue()),
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      expectedMappings
    )
  @Test def testMapReduceKeysInParallel(): Unit =
    mustEqual(
      bulkMap
        .reduceKeys(
          1L,
          (x: JLong) => JLong.valueOf(4L * x.longValue()),
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      4L * expectedKeys
    )
  @Test def testMapReduceValuesInParallel(): Unit =
    mustEqual(
      bulkMap
        .reduceValues(
          1L,
          (x: JLong) => JLong.valueOf(4L * x.longValue()),
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      4L * expectedValues
    )
  @Test def testMappedReduceInParallel(): Unit =
    mustEqual(
      bulkMap
        .reduce(
          1L,
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue()),
          (x: JLong, y: JLong) => JLong.valueOf(x.longValue() + y.longValue())
        )
        .longValue(),
      expectedMappings
    )

  @Test def testReduceKeysToLongSequentially(): Unit = mustEqual(
    bulkMap.reduceKeysToLong(
      Long.MaxValue,
      (x: JLong) => x.longValue(),
      0L,
      (x: Long, y: Long) => x + y
    ),
    expectedKeys
  )
  @Test def testReduceKeysToIntSequentially(): Unit = mustEqual(
    bulkMap.reduceKeysToInt(
      Long.MaxValue,
      (x: JLong) => x.intValue(),
      0,
      (x: Int, y: Int) => x + y
    ),
    expectedKeys.toInt
  )
  @Test def testReduceKeysToDoubleSequentially(): Unit = mustEqual(
    bulkMap.reduceKeysToDouble(
      Long.MaxValue,
      (x: JLong) => x.doubleValue(),
      0.0,
      (x: Double, y: Double) => x + y
    ),
    expectedKeys.toDouble
  )
  @Test def testReduceValuesToLongSequentially(): Unit = mustEqual(
    bulkMap.reduceValuesToLong(
      Long.MaxValue,
      (x: JLong) => x.longValue(),
      0L,
      (x: Long, y: Long) => x + y
    ),
    expectedValues
  )
  @Test def testReduceValuesToIntSequentially(): Unit = mustEqual(
    bulkMap.reduceValuesToInt(
      Long.MaxValue,
      (x: JLong) => x.intValue(),
      0,
      (x: Int, y: Int) => x + y
    ),
    expectedValues.toInt
  )
  @Test def testReduceValuesToDoubleSequentially(): Unit = mustEqual(
    bulkMap.reduceValuesToDouble(
      Long.MaxValue,
      (x: JLong) => x.doubleValue(),
      0.0,
      (x: Double, y: Double) => x + y
    ),
    expectedValues.toDouble
  )
  @Test def testReduceKeysToLongInParallel(): Unit = mustEqual(
    bulkMap.reduceKeysToLong(
      1L,
      (x: JLong) => x.longValue(),
      0L,
      (x: Long, y: Long) => x + y
    ),
    expectedKeys
  )
  @Test def testReduceKeysToIntInParallel(): Unit = mustEqual(
    bulkMap.reduceKeysToInt(
      1L,
      (x: JLong) => x.intValue(),
      0,
      (x: Int, y: Int) => x + y
    ),
    expectedKeys.toInt
  )
  @Test def testReduceKeysToDoubleInParallel(): Unit = mustEqual(
    bulkMap.reduceKeysToDouble(
      1L,
      (x: JLong) => x.doubleValue(),
      0.0,
      (x: Double, y: Double) => x + y
    ),
    expectedKeys.toDouble
  )
  @Test def testReduceValuesToLongInParallel(): Unit = mustEqual(
    bulkMap.reduceValuesToLong(
      1L,
      (x: JLong) => x.longValue(),
      0L,
      (x: Long, y: Long) => x + y
    ),
    expectedValues
  )
  @Test def testReduceValuesToIntInParallel(): Unit = mustEqual(
    bulkMap.reduceValuesToInt(
      1L,
      (x: JLong) => x.intValue(),
      0,
      (x: Int, y: Int) => x + y
    ),
    expectedValues.toInt
  )
  @Test def testReduceValuesToDoubleInParallel(): Unit = mustEqual(
    bulkMap.reduceValuesToDouble(
      1L,
      (x: JLong) => x.doubleValue(),
      0.0,
      (x: Double, y: Double) => x + y
    ),
    expectedValues.toDouble
  )

  private def checkSearch(threshold: Long): Unit = {
    val half = BulkSize / 2L
    mustEqual(
      bulkMap
        .searchKeys(
          threshold,
          (x: JLong) => if (x.longValue() == half) x else null
        )
        .longValue(),
      half
    )
    assertNull(
      bulkMap.searchKeys(
        threshold,
        (x: JLong) => if (x.longValue() < 0L) x else null
      )
    )
    mustEqual(
      bulkMap
        .searchValues(
          threshold,
          (x: JLong) => if (x.longValue() == half) x else null
        )
        .longValue(),
      half
    )
    assertNull(
      bulkMap.searchValues(
        threshold,
        (x: JLong) => if (x.longValue() < 0L) x else null
      )
    )
    mustEqual(
      bulkMap
        .search(
          threshold,
          (x: JLong, _: JLong) => if (x.longValue() == half) x else null
        )
        .longValue(),
      half
    )
    assertNull(
      bulkMap.search(
        threshold,
        (x: JLong, _: JLong) => if (x.longValue() < 0L) x else null
      )
    )
    mustEqual(
      bulkMap
        .searchEntries(
          threshold,
          (e: ju.Map.Entry[JLong, JLong]) =>
            if (e.getKey().longValue() == half) e.getKey() else null
        )
        .longValue(),
      half
    )
    assertNull(
      bulkMap.searchEntries(
        threshold,
        (e: ju.Map.Entry[JLong, JLong]) =>
          if (e.getKey().longValue() < 0L) e.getKey() else null
      )
    )
  }

  @Test def testSearchKeysSequentially(): Unit = checkSearch(Long.MaxValue)
  @Test def testSearchValuesSequentially(): Unit = checkSearch(Long.MaxValue)
  @Test def testSearchSequentially(): Unit = checkSearch(Long.MaxValue)
  @Test def testSearchEntriesSequentially(): Unit = checkSearch(Long.MaxValue)
  @Test def testSearchKeysInParallel(): Unit = checkSearch(1L)
  @Test def testSearchValuesInParallel(): Unit = checkSearch(1L)
  @Test def testSearchInParallel(): Unit = checkSearch(1L)
  @Test def testSearchEntriesInParallel(): Unit = checkSearch(1L)

  @Test def testcomputeIfAbsent_performance(): Unit = {
    val mapSize = 20
    val iterations = mapSize * 2
    val map = new ConcurrentHashMap[Item, Item]()
    var i = 0
    while (i < mapSize) {
      val item = itemFor(i)
      map.put(item, item)
      i += 1
    }
    val pool = Executors.newFixedThreadPool(2)
    try {
      val r: Runnable = () => {
        var result = 0
        var j = 0
        while (j < iterations) {
          result += map
            .computeIfAbsent(itemFor(j % mapSize), k => itemFor(k.value * 2))
            .value
          j += 1
        }
        if (result == -42) throw new Error()
      }
      pool.execute(r)
      pool.execute(r)
    } finally joinPool(pool)
  }
}
