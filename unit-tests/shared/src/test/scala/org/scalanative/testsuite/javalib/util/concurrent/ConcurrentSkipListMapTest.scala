/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import java.util.concurrent.{CompletableFuture, ConcurrentSkipListMap, ThreadLocalRandom}
import java.util.function.BiFunction
import java.util.{
  ArrayList,
  Arrays,
  BitSet,
  Collection,
  Comparator,
  Iterator,
  Map,
  NavigableMap,
  NavigableSet,
  NoSuchElementException,
  Random,
  Set,
  SortedMap
}

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

object ConcurrentSkipListMapTest {
  import JSR166Test._

  private val iZero = itemFor(0)
  private val iOne = itemFor(1)
  private val iTwo = itemFor(2)
  private val iThree = itemFor(3)
  private val iFour = itemFor(4)
  private val iFive = itemFor(5)
  private val iSix = itemFor(6)
  private val minusTen = itemFor(-10)

  private def map5(): ConcurrentSkipListMap[Item, String] = {
    val map = new ConcurrentSkipListMap[Item, String]()
    assertTrue(map.isEmpty())
    map.put(iOne, "A")
    map.put(iFive, "E")
    map.put(iThree, "C")
    map.put(iTwo, "B")
    map.put(iFour, "D")
    assertFalse(map.isEmpty())
    mustEqual(5, map.size())
    map
  }

  private trait MapImplementation {
    def emptyMap(): Map[Object, Object]
    def makeKey(i: Int): Object = Integer.valueOf(i)
    def makeValue(i: Int): Object = Integer.valueOf(i)
    def valueToInt(value: Object): Int = value.asInstanceOf[Integer].intValue()
    def isConcurrent(): Boolean
    def remappingFunctionCalledAtMostOnce(): Boolean = true
    def permitsNullKeys(): Boolean
    def permitsNullValues(): Boolean
    def supportsSetValue(): Boolean
  }

  private object ConcurrentSkipListMapImplementation extends MapImplementation {
    override def emptyMap(): Map[Object, Object] =
      new ConcurrentSkipListMap[Object, Object]()

    override def isConcurrent(): Boolean = true
    override def remappingFunctionCalledAtMostOnce(): Boolean = false
    override def permitsNullKeys(): Boolean = false
    override def permitsNullValues(): Boolean = false
    override def supportsSetValue(): Boolean = false
  }
}

class ConcurrentSkipListMapTest extends JSR166Test {
  import ConcurrentSkipListMapTest._
  import JSR166Test._

  @Test def testMapImplSanity(): Unit = {
    val impl = ConcurrentSkipListMapImplementation
    val rnd = ThreadLocalRandom.current();
    {
      val m = impl.emptyMap()
      assertTrue(m.isEmpty())
      mustEqual(0, m.size())
      val k = impl.makeKey(rnd.nextInt())
      val v = impl.makeValue(rnd.nextInt())
      m.put(k, v)
      assertFalse(m.isEmpty())
      mustEqual(1, m.size())
      assertTrue(m.containsKey(k))
      assertTrue(m.containsValue(v))
    }
    {
      val m = impl.emptyMap()
      val v = impl.makeValue(rnd.nextInt())
      if (impl.permitsNullKeys()) {
        m.put(null, v)
        assertTrue(m.containsKey(null))
        assertTrue(m.containsValue(v))
      } else {
        assertThrows(classOf[NullPointerException], m.put(null, v))
      }
    }
    {
      val m = impl.emptyMap()
      val k = impl.makeKey(rnd.nextInt())
      if (impl.permitsNullValues()) {
        m.put(k, null)
        assertTrue(m.containsKey(k))
        assertTrue(m.containsValue(null))
      } else {
        assertThrows(classOf[NullPointerException], m.put(k, null))
      }
    }
    {
      val m = impl.emptyMap()
      val k = impl.makeKey(rnd.nextInt())
      val v1 = impl.makeValue(rnd.nextInt())
      val v2 = impl.makeValue(rnd.nextInt())
      m.put(k, v1)
      if (impl.supportsSetValue()) {
        m.entrySet().iterator().next().setValue(v2)
        assertSame(v2, m.get(k))
        assertTrue(m.containsKey(k))
        assertTrue(m.containsValue(v2))
        assertFalse(m.containsValue(v1))
      } else {
        assertThrows(
          classOf[UnsupportedOperationException],
          m.entrySet().iterator().next().setValue(v2)
        )
      }
    }
  }

  @Test def testMapBug8210280(): Unit = {
    val impl = ConcurrentSkipListMapImplementation
    val rnd = ThreadLocalRandom.current()
    val size1 = rnd.nextInt(32)
    val size2 = rnd.nextInt(128)

    val m1 = impl.emptyMap()
    var i = 0
    while (i < size1) {
      val elt = rnd.nextInt(1024 * i, 1024 * (i + 1))
      assertNull(m1.put(impl.makeKey(elt), impl.makeValue(elt)))
      i += 1
    }

    val m2 = impl.emptyMap()
    i = 0
    while (i < size2) {
      val elt =
        rnd.nextInt(Integer.MIN_VALUE + 1024 * i, Integer.MIN_VALUE + 1024 * (i + 1))
      assertNull(m2.put(impl.makeKey(elt), impl.makeValue(-elt)))
      i += 1
    }

    val m1Copy = impl.emptyMap()
    m1Copy.putAll(m1)
    m1.putAll(m2)

    val it2 = m2.keySet().iterator()
    while (it2.hasNext()) {
      val elt = it2.next()
      mustEqual(m2.get(elt), m1.get(elt))
    }
    val it1 = m1Copy.keySet().iterator()
    while (it1.hasNext()) {
      val elt = it1.next()
      assertSame(m1Copy.get(elt), m1.get(elt))
    }
    mustEqual(size1 + size2, m1.size())
  }

  @Test def testMapClone(): Unit = {
    val impl = ConcurrentSkipListMapImplementation
    val rnd = ThreadLocalRandom.current()
    val size = rnd.nextInt(4)
    val map = impl.emptyMap()
    var i = 0
    while (i < size) {
      map.put(impl.makeKey(i), impl.makeValue(i))
      i += 1
    }

    val clone =
      map.asInstanceOf[ConcurrentSkipListMap[Object, Object]].clone()

    mustEqual(size, map.size())
    mustEqual(size, clone.size())
    mustEqual(map.isEmpty(), clone.isEmpty())

    clone.put(impl.makeKey(-1), impl.makeValue(-1))
    mustEqual(size, map.size())
    mustEqual(size + 1, clone.size())

    clone.clear()
    mustEqual(size, map.size())
    mustEqual(0, clone.size())
    assertTrue(clone.isEmpty())
  }

  @Test def testMapConcurrentAccess(): Unit = {
    val impl = ConcurrentSkipListMapImplementation
    val map = impl.emptyMap()
    val testDurationMillis = if (expensiveTests) 1000L else 2L
    val nTasks =
      if (impl.isConcurrent()) ThreadLocalRandom.current().nextInt(1, 10)
      else 1
    val done = new AtomicBoolean(false)
    val remappingAtMostOnce = impl.remappingFunctionCalledAtMostOnce()
    val futures = new ArrayList[CompletableFuture[Void]]()
    val expectedSum = new AtomicLong(0L)

    val tasks = Array[Action](
      new Action {
        override def run(): Unit = {
          val invocations = new Array[Long](2)
          val rnd = ThreadLocalRandom.current()
          val incValue =
            new BiFunction[Object, Object, Object] {
              override def apply(k: Object, v: Object): Object = {
                invocations(1) += 1
                val vi =
                  if (v == null) 1
                  else impl.valueToInt(v) + 1
                impl.makeValue(vi)
              }
            }
          while (!done.getAcquire()) {
            invocations(0) += 1
            val key = impl.makeKey(3 * rnd.nextInt(10))
            map.compute(key, incValue)
          }
          if (remappingAtMostOnce)
            mustEqual(invocations(0), invocations(1))
          expectedSum.getAndAdd(invocations(0))
        }
      },
      new Action {
        override def run(): Unit = {
          val invocations = new Array[Long](2)
          val rnd = ThreadLocalRandom.current()
          val incValue =
            new BiFunction[Object, Object, Object] {
              override def apply(k: Object, v: Object): Object = {
                invocations(1) += 1
                impl.makeValue(impl.valueToInt(v) + 1)
              }
            }
          while (!done.getAcquire()) {
            val key = impl.makeKey(3 * rnd.nextInt(10))
            if (map.computeIfPresent(key, incValue) != null)
              invocations(0) += 1
          }
          if (remappingAtMostOnce)
            mustEqual(invocations(0), invocations(1))
          expectedSum.getAndAdd(invocations(0))
        }
      }
    )

    var i = nTasks
    while (i > 0) {
      i -= 1
      futures.add(CompletableFuture.runAsync(checkedRunnable(chooseRandomly(tasks))))
    }
    Thread.sleep(testDurationMillis)
    done.setRelease(true)
    val fit = futures.iterator()
    while (fit.hasNext())
      checkTimedGet(fit.next(), null)

    var sum = 0L
    val values = map.values().iterator()
    while (values.hasNext())
      sum += impl.valueToInt(values.next())
    mustEqual(expectedSum.get(), sum)
  }

  @Test def testClear(): Unit = {
    val map = map5()
    map.clear()
    mustEqual(0, map.size())
  }

  @Test def testConstructFromSorted(): Unit = {
    val map = map5()
    val map2 = new ConcurrentSkipListMap[Item, String](map)
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
    val empty = new ConcurrentSkipListMap[Item, String]()
    assertNull(empty.get(iOne))
  }

  @Test def testIsEmpty(): Unit = {
    val empty = new ConcurrentSkipListMap[Item, String]()
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

  @Test def testKeySetToArray(): Unit = {
    val map = map5()
    val s = map.keySet()
    val ar = s.toArray()
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    mustEqual(5, ar.length)
    ar(0) = minusTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
  }

  @Test def testDescendingKeySetToArray(): Unit = {
    val map = map5()
    val s = map.descendingKeySet()
    val ar = s.toArray()
    mustEqual(5, ar.length)
    assertTrue(s.containsAll(Arrays.asList(ar: _*)))
    ar(0) = minusTen
    assertFalse(s.containsAll(Arrays.asList(ar: _*)))
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
    var count = 1
    while (i.hasNext()) {
      val k = i.next()
      assertTrue(last.compareTo(k) < 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  @Test def testKeySetDescendingIteratorOrder(): Unit = {
    val map = map5()
    val i = map.navigableKeySet().descendingIterator()
    var last = i.next()
    mustEqual(last, iFive)
    var count = 1
    while (i.hasNext()) {
      val k = i.next()
      assertTrue(last.compareTo(k) > 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  @Test def testDescendingKeySetOrder(): Unit = {
    val map = map5()
    val i = map.descendingKeySet().iterator()
    var last = i.next()
    mustEqual(last, iFive)
    var count = 1
    while (i.hasNext()) {
      val k = i.next()
      assertTrue(last.compareTo(k) > 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
  }

  @Test def testDescendingKeySetDescendingIteratorOrder(): Unit = {
    val map = map5()
    val i = map.descendingKeySet().descendingIterator()
    var last = i.next()
    mustEqual(last, iOne)
    var count = 1
    while (i.hasNext()) {
      val k = i.next()
      assertTrue(last.compareTo(k) < 0)
      last = k
      count += 1
    }
    mustEqual(5, count)
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

  @Test def testDescendingEntrySet(): Unit = {
    val map = map5()
    val s = map.descendingMap().entrySet()
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

  @Test def testEntrySetToArray(): Unit = {
    val map = map5()
    val ar = map.entrySet().toArray()
    mustEqual(5, ar.length)
    for (i <- 0 until 5) {
      val e = ar(i).asInstanceOf[Map.Entry[Item, String]]
      assertTrue(map.containsKey(e.getKey()))
      assertTrue(map.containsValue(e.getValue()))
    }
  }

  @Test def testDescendingEntrySetToArray(): Unit = {
    val map = map5()
    val ar = map.descendingMap().entrySet().toArray()
    mustEqual(5, ar.length)
    for (i <- 0 until 5) {
      val e = ar(i).asInstanceOf[Map.Entry[Item, String]]
      assertTrue(map.containsKey(e.getKey()))
      assertTrue(map.containsValue(e.getValue()))
    }
  }

  @Test def testPutAll(): Unit = {
    val p = new ConcurrentSkipListMap[Item, String]()
    val map = map5()
    p.putAll(map)
    mustEqual(5, p.size())
    assertTrue(p.containsKey(iOne))
    assertTrue(p.containsKey(iTwo))
    assertTrue(p.containsKey(iThree))
    assertTrue(p.containsKey(iFour))
    assertTrue(p.containsKey(iFive))
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

  @Test def testEntryImmutability(): Unit = {
    val map = map5()
    var e = map.lowerEntry(iThree)
    mustEqual(iTwo, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("X"))
    e = map.higherEntry(iZero)
    mustEqual(iOne, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("X"))
    e = map.floorEntry(iOne)
    mustEqual(iOne, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("X"))
    e = map.ceilingEntry(iFive)
    mustEqual(iFive, e.getKey())
    assertThrows(classOf[UnsupportedOperationException], e.setValue("X"))
  }

  @Test def testLowerKey(): Unit = {
    val q = map5()
    mustEqual(iTwo, q.lowerKey(iThree))
    mustEqual(iFive, q.lowerKey(iSix))
    assertNull(q.lowerKey(iOne))
    assertNull(q.lowerKey(iZero))
  }

  @Test def testHigherKey(): Unit = {
    val q = map5()
    mustEqual(iFour, q.higherKey(iThree))
    mustEqual(iOne, q.higherKey(iZero))
    assertNull(q.higherKey(iFive))
    assertNull(q.higherKey(iSix))
  }

  @Test def testFloorKey(): Unit = {
    val q = map5()
    mustEqual(iThree, q.floorKey(iThree))
    mustEqual(iFive, q.floorKey(iSix))
    mustEqual(iOne, q.floorKey(iOne))
    assertNull(q.floorKey(iZero))
  }

  @Test def testCeilingKey(): Unit = {
    val q = map5()
    mustEqual(iThree, q.ceilingKey(iThree))
    mustEqual(iOne, q.ceilingKey(iZero))
    mustEqual(iFive, q.ceilingKey(iFive))
    assertNull(q.ceilingKey(iSix))
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
    val empty = new ConcurrentSkipListMap[Item, String]()
    mustEqual(0, empty.size())
    mustEqual(5, map.size())
  }

  @Test def testToString(): Unit = {
    val map = map5()
    val s = map.toString()
    for (i <- 1 to 5)
      assertTrue(s.contains(String.valueOf(i)))
  }

  @Test def testGet_NullPointerException(): Unit = {
    val c = map5()
    assertThrows(classOf[NullPointerException], c.get(null))
  }

  @Test def testContainsKey_NullPointerException(): Unit = {
    val c = map5()
    assertThrows(classOf[NullPointerException], c.containsKey(null))
  }

  @Test def testContainsValue_NullPointerException(): Unit = {
    val c = new ConcurrentSkipListMap[Item, String]()
    assertThrows(classOf[NullPointerException], c.containsValue(null))
  }

  @Test def testPut1_NullPointerException(): Unit = {
    val c = map5()
    assertThrows(classOf[NullPointerException], c.put(null, "whatever"))
  }

  @Test def testPutIfAbsent1_NullPointerException(): Unit = {
    val c = map5()
    assertThrows(classOf[NullPointerException], c.putIfAbsent(null, "whatever"))
  }

  @Test def testReplace_NullPointerException(): Unit = {
    val c = map5()
    assertThrows(classOf[NullPointerException], c.replace(null, "A"))
  }

  @Test def testReplaceValue_NullPointerException(): Unit = {
    val c = map5()
    assertThrows(classOf[NullPointerException], c.replace(null, "A", "B"))
  }

  @Test def testRemove1_NullPointerException(): Unit = {
    val c = new ConcurrentSkipListMap[Item, String]()
    c.put(iZero, "A")
    assertThrows(classOf[NullPointerException], c.remove(null))
  }

  @Test def testRemove2_NullPointerException(): Unit = {
    val c = new ConcurrentSkipListMap[Item, String]()
    c.put(iZero, "asdads")
    assertThrows(classOf[NullPointerException], c.remove(null, "whatever"))
  }

  @Test def testRemove3(): Unit = {
    val c = new ConcurrentSkipListMap[Item, String]()
    c.put(iZero, "asdads")
    assertFalse(c.remove("sadsdf", null))
  }

  @Test def testClone(): Unit = {
    val x = map5()
    val y = x.clone()
    assertNotSame(x, y)
    mustEqual(x.size(), y.size())
    mustEqual(x.toString(), y.toString())
    mustEqual(x, y)
    mustEqual(y, x)
    y.clear()
    assertTrue(y.isEmpty())
    assertFalse(x.equals(y))
  }

  @Ignore("No ObjectInputStream in Scala Native")
  @Test def testSerialization(): Unit = {}

  @Test def testSubMapContents(): Unit = {
    val map = map5()
    val sm = map.subMap(iTwo, true, iFour, false)
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
    val r = sm.descendingKeySet().iterator()
    mustEqual(iThree, r.next())
    mustEqual(iTwo, r.next())
    assertFalse(r.hasNext())

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
    val sm = map.subMap(iTwo, true, iThree, false)
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
    val r = sm.descendingKeySet().iterator()
    mustEqual(iTwo, r.next())
    assertFalse(r.hasNext())

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
    val sm = map.headMap(iFour, false)
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
    val sm = map.tailMap(iTwo, true)
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
    val r = sm.descendingKeySet().iterator()
    mustEqual(iFive, r.next())
    mustEqual(iFour, r.next())
    mustEqual(iThree, r.next())
    mustEqual(iTwo, r.next())
    assertFalse(r.hasNext())

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

    val ssm = sm.tailMap(iFour, true)
    mustEqual(iFour, ssm.firstKey())
    mustEqual(iFive, ssm.lastKey())
    mustEqual("D", ssm.remove(iFour))
    mustEqual(1, ssm.size())
    mustEqual(3, sm.size())
    mustEqual(4, map.size())
  }

  private val rnd = new Random(666)
  private var bs: BitSet = _

  @Test def testRecursiveSubMaps(): Unit = {
    val mapSize = if (expensiveTests) 1000 else 100
    val map = newMap()
    bs = new BitSet(mapSize)

    populate(map, mapSize)
    check(map, 0, mapSize - 1, ascending = true)
    check(map.descendingMap(), 0, mapSize - 1, ascending = false)

    mutateMap(map, 0, mapSize - 1)
    check(map, 0, mapSize - 1, ascending = true)
    check(map.descendingMap(), 0, mapSize - 1, ascending = false)

    bashSubMap(
      map.subMap(iZero, true, itemFor(mapSize), false),
      0,
      mapSize - 1,
      ascending = true
    )
  }

  private def newMap(): NavigableMap[Item, Item] = {
    val result = new ConcurrentSkipListMap[Item, Item]()
    mustEqual(0, result.size())
    assertFalse(result.keySet().iterator().hasNext())
    result
  }

  private def populate(map: NavigableMap[Item, Item], limit: Int): Unit = {
    var i = 0
    val n = 2 * limit / 3
    while (i < n) {
      put(map, rnd.nextInt(limit))
      i += 1
    }
  }

  private def mutateMap(map: NavigableMap[Item, Item], min: Int, max: Int): Unit = {
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
      if (rnd.nextBoolean()) {
        bs.clear(it.next().value)
        it.remove()
      } else {
        it.next()
      }
    }

    while (map.size() < size) {
      val key = min + rnd.nextInt(rangeSize)
      assertTrue(key >= min && key <= max)
      put(map, key)
    }
  }

  private def mutateSubMap(map: NavigableMap[Item, Item], min: Int, max: Int): Unit = {
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
      if (rnd.nextBoolean()) {
        bs.clear(it.next().value)
        it.remove()
      } else {
        it.next()
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

  private def put(map: NavigableMap[Item, Item], key: Int): Unit = {
    if (map.put(itemFor(key), itemFor(2 * key)) == null)
      bs.set(key)
  }

  private def remove(map: NavigableMap[Item, Item], key: Int): Unit = {
    if (map.remove(itemFor(key)) != null)
      bs.clear(key)
  }

  private def bashSubMap(
      map: NavigableMap[Item, Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): Unit = {
    check(map, min, max, ascending)
    check(map.descendingMap(), min, max, !ascending)

    mutateSubMap(map, min, max)
    check(map, min, max, ascending)
    check(map.descendingMap(), min, max, !ascending)

    if (max - min < 2)
      return
    val midPoint = (min + max) / 2

    var incl = rnd.nextBoolean()
    val hm = map.headMap(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean())
        bashSubMap(hm, min, midPoint - (if (incl) 0 else 1), ascending = true)
      else
        bashSubMap(
          hm.descendingMap(),
          min,
          midPoint - (if (incl) 0 else 1),
          ascending = false
        )
    } else {
      if (rnd.nextBoolean())
        bashSubMap(hm, midPoint + (if (incl) 0 else 1), max, ascending = false)
      else
        bashSubMap(
          hm.descendingMap(),
          midPoint + (if (incl) 0 else 1),
          max,
          ascending = true
        )
    }

    incl = rnd.nextBoolean()
    val tm = map.tailMap(itemFor(midPoint), incl)
    if (ascending) {
      if (rnd.nextBoolean())
        bashSubMap(tm, midPoint + (if (incl) 0 else 1), max, ascending = true)
      else
        bashSubMap(
          tm.descendingMap(),
          midPoint + (if (incl) 0 else 1),
          max,
          ascending = false
        )
    } else {
      if (rnd.nextBoolean())
        bashSubMap(tm, min, midPoint - (if (incl) 0 else 1), ascending = false)
      else
        bashSubMap(
          tm.descendingMap(),
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
    Arrays.sort(endpoints)
    val lowIncl = rnd.nextBoolean()
    val highIncl = rnd.nextBoolean()
    if (ascending) {
      val sm =
        map.subMap(itemFor(endpoints(0)), lowIncl, itemFor(endpoints(1)), highIncl)
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
      val sm =
        map.subMap(itemFor(endpoints(1)), highIncl, itemFor(endpoints(0)), lowIncl)
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
      map: NavigableMap[Item, Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): Unit = {
    final class ReferenceSet {
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
      def lowerAscending(key: Int): Int = floorAscending(key - 1)
      def floorAscending(initial: Int): Int = {
        var key = initial
        if (key < min) return -1
        else if (key > max) key = max
        while (key >= min) {
          if (bs.get(key)) return key
          key -= 1
        }
        -1
      }
      def ceilingAscending(initial: Int): Int = {
        var key = initial
        if (key < min) key = min
        else if (key > max) return -1
        val result = bs.nextSetBit(key)
        if (result > max) -1 else result
      }
      def higherAscending(key: Int): Int = ceilingAscending(key + 1)
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
      mustEqual(bsContainsI, map.containsKey(itemFor(i)))
      if (bsContainsI) size += 1
      i += 1
    }
    mustEqual(size, map.size())

    var size2 = 0
    var previousKey = -1
    val keyIt = map.keySet().iterator()
    while (keyIt.hasNext()) {
      val key = keyIt.next()
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

    var key = min - 1
    while (key <= max + 1) {
      val k = itemFor(key)
      assertEq(s"lowerKey($key) ${viewState(map, min, max, ascending)}", map.lowerKey(k), rs.lower(key))
      assertEq(s"floorKey($key) ${viewState(map, min, max, ascending)}", map.floorKey(k), rs.floor(key))
      assertEq(s"higherKey($key) ${viewState(map, min, max, ascending)}", map.higherKey(k), rs.higher(key))
      assertEq(s"ceilingKey($key) ${viewState(map, min, max, ascending)}", map.ceilingKey(k), rs.ceiling(key))
      key += 1
    }

    if (map.size() != 0) {
      assertEq(s"firstKey ${viewState(map, min, max, ascending)}", map.firstKey(), rs.first())
      assertEq(s"lastKey ${viewState(map, min, max, ascending)}", map.lastKey(), rs.last())
    } else {
      mustEqual(rs.first(), -1)
      mustEqual(rs.last(), -1)
      assertThrows(classOf[NoSuchElementException], map.firstKey())
      assertThrows(classOf[NoSuchElementException], map.lastKey())
    }
  }

  private def viewState(
      map: NavigableMap[Item, Item],
      min: Int,
      max: Int,
      ascending: Boolean
  ): String = {
    val keys = new StringBuilder()
    val it = map.keySet().iterator()
    while (it.hasNext()) {
      if (keys.nonEmpty) keys.append(',')
      keys.append(it.next().value)
    }
    s"range=[$min,$max] ascending=$ascending keys=[$keys]"
  }

  private def assertEq(message: String, i: Item, j: Int): Unit = {
    if (i == null) assertEquals(message, -1, j)
    else assertEquals(message, i.value, j)
  }
}
