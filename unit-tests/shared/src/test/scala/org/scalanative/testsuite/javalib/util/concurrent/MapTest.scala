/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import java.util.concurrent.{CompletableFuture, ThreadLocalRandom}
import java.util.function.BiFunction
import java.util.{ArrayList, Map}

import org.junit.Assert._
import org.junit._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

/** Contains tests applicable to all Map implementations. */
abstract class MapTest extends JSR166Test {
  import JSR166Test._

  def impl: MapImplementation

  /** If o is a known Cloneable map, returns a clone; else null. */
  def cloneableClone[T](o: T): T = o match {
    case m: java.util.concurrent.ConcurrentSkipListMap[_, _] =>
      m.clone().asInstanceOf[T]
    case _ =>
      null.asInstanceOf[T]
  }

  @Test def testImplSanity(): Unit = {
    val rnd = ThreadLocalRandom.current()
    locally {
      val m = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
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
    locally {
      val m = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
      val v = impl.makeValue(rnd.nextInt())
      if (impl.permitsNullKeys()) {
        m.put(null, v)
        assertTrue(m.containsKey(null))
        assertTrue(m.containsValue(v))
      } else {
        assertThrows(classOf[NullPointerException], m.put(null, v))
      }
    }
    locally {
      val m = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
      val k = impl.makeKey(rnd.nextInt())
      if (impl.permitsNullValues()) {
        m.put(k, null)
        assertTrue(m.containsKey(k))
        assertTrue(m.containsValue(null))
      } else {
        assertThrows(classOf[NullPointerException], m.put(k, null))
      }
    }
    locally {
      val m = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
      val k = impl.makeKey(rnd.nextInt())
      val v1 = impl.makeValue(rnd.nextInt())
      val v2 = impl.makeValue(rnd.nextInt())
      m.put(k, v1)
      if (impl.supportsSetValue()) {
        m.entrySet()
          .iterator()
          .next()
          .asInstanceOf[Map.Entry[AnyRef, AnyRef]]
          .setValue(v2)
        assertSame(v2, m.get(k))
        assertTrue(m.containsKey(k))
        assertTrue(m.containsValue(v2))
        assertFalse(m.containsValue(v1))
      } else {
        assertThrows(
          classOf[UnsupportedOperationException],
          m.entrySet()
            .iterator()
            .next()
            .asInstanceOf[Map.Entry[AnyRef, AnyRef]]
            .setValue(v2)
        )
      }
    }
  }

  /** Tests scenario from JDK-8186171. */
  @Test def testBug8186171(): Unit = {
    if (!impl.supportsSetValue()) return
    // jdk9 is no longer maintained; always run on SN / JDK 10+
    if (!(Platform.executingInJVMWithJDKIn(10 to 99) ||
          Platform.executingInScalaNative))
      return

    val rnd = ThreadLocalRandom.current()
    val permitsNullValues = impl.permitsNullValues()
    val v1 =
      if (permitsNullValues && rnd.nextBoolean()) null
      else impl.makeValue(1)
    val v2 =
      if (permitsNullValues && rnd.nextBoolean() && v1 != null) null
      else impl.makeValue(2)

    // If true, always lands in first bucket in hash tables.
    val poorHash = rnd.nextBoolean()

    class Key(val i: Int) extends Comparable[Key] {
      override def hashCode(): Int =
        if (poorHash) 0 else super.hashCode()
      def compareTo(x: Key): Int =
        Integer.compare(this.i, x.i)
    }

    // Both HashMap and ConcurrentHashMap have:
    // TREEIFY_THRESHOLD = 8; UNTREEIFY_THRESHOLD = 6;
    val size = rnd.nextInt(1, 25)

    val keys = new ArrayList[Key]()
    var i = size
    while (i > 0) {
      i -= 1
      keys.add(new Key(i))
    }
    val keyToFrob = keys.get(rnd.nextInt(keys.size()))

    val m = impl.emptyMap().asInstanceOf[Map[Key, AnyRef]]
    val keyIt = keys.iterator()
    while (keyIt.hasNext()) {
      m.put(keyIt.next(), v1)
    }

    val it = m.entrySet().iterator()
    while (it.hasNext()) {
      val entry = it.next()
      if (entry.getKey() eq keyToFrob)
        entry.setValue(v2) // does this have the expected effect?
      else
        it.remove()
    }

    assertFalse(m.containsValue(v1))
    assertTrue(m.containsValue(v2))
    assertTrue(m.containsKey(keyToFrob))
    mustEqual(1, m.size())
  }

  /** "Missing" test found while investigating JDK-8210280. */
  @Test def testBug8210280(): Unit = {
    val rnd = ThreadLocalRandom.current()
    val size1 = rnd.nextInt(32)
    val size2 = rnd.nextInt(128)

    val m1 = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
    var i = 0
    while (i < size1) {
      val elt = rnd.nextInt(1024 * i, 1024 * (i + 1))
      assertNull(m1.put(impl.makeKey(elt), impl.makeValue(elt)))
      i += 1
    }

    val m2 = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
    i = 0
    while (i < size2) {
      val elt = rnd.nextInt(
        Integer.MIN_VALUE + 1024 * i,
        Integer.MIN_VALUE + 1024 * (i + 1)
      )
      assertNull(m2.put(impl.makeKey(elt), impl.makeValue(-elt)))
      i += 1
    }

    val m1Copy = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
    m1Copy.putAll(m1)

    m1.putAll(m2)

    val m2Keys = m2.keySet().iterator()
    while (m2Keys.hasNext()) {
      val elt = m2Keys.next()
      mustEqual(m2.get(elt), m1.get(elt))
    }
    val m1CopyKeys = m1Copy.keySet().iterator()
    while (m1CopyKeys.hasNext()) {
      val elt = m1CopyKeys.next()
      assertSame(m1Copy.get(elt), m1.get(elt))
    }
    mustEqual(size1 + size2, m1.size())
  }

  /** 8222930: ConcurrentSkipListMap.clone() shares size between original and
   *  clone
   */
  @Test def testClone(): Unit = {
    val rnd = ThreadLocalRandom.current()
    val size = rnd.nextInt(4)
    val map = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
    var i = 0
    while (i < size) {
      map.put(impl.makeKey(i), impl.makeValue(i))
      i += 1
    }
    val clone = cloneableClone(map)
    if (clone == null) return // not cloneable?

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

  /** Concurrent access by compute methods behaves as expected */
  @Test def testConcurrentAccess(): Unit = {
    val map = impl.emptyMap().asInstanceOf[Map[AnyRef, AnyRef]]
    val testDurationMillis = if (expensiveTests) 1000 else 2
    val nTasks =
      if (impl.isConcurrent())
        ThreadLocalRandom.current().nextInt(1, 10)
      else 1
    val done = new AtomicBoolean(false)
    val remappingFunctionCalledAtMostOnce =
      impl.remappingFunctionCalledAtMostOnce()
    val futures = new ArrayList[CompletableFuture[Void]]()
    val expectedSum = new AtomicLong(0)
    val tasks: Array[Action] = Array(
      // repeatedly increment values using compute()
      new Action {
        def run(): Unit = {
          val invocations = new Array[Long](2)
          val rnd = ThreadLocalRandom.current()
          val incValue = new BiFunction[AnyRef, AnyRef, AnyRef] {
            def apply(k: AnyRef, v: AnyRef): AnyRef = {
              invocations(1) += 1
              val vi = if (v == null) 1 else impl.valueToInt(v) + 1
              impl.makeValue(vi)
            }
          }
          while (!done.getAcquire) {
            invocations(0) += 1
            val key = impl.makeKey(3 * rnd.nextInt(10))
            map.compute(key, incValue)
          }
          if (remappingFunctionCalledAtMostOnce)
            mustEqual(invocations(0), invocations(1))
          expectedSum.getAndAdd(invocations(0))
        }
      },
      // repeatedly increment values using computeIfPresent()
      new Action {
        def run(): Unit = {
          val invocations = new Array[Long](2)
          val rnd = ThreadLocalRandom.current()
          val incValue = new BiFunction[AnyRef, AnyRef, AnyRef] {
            def apply(k: AnyRef, v: AnyRef): AnyRef = {
              invocations(1) += 1
              val vi = impl.valueToInt(v) + 1
              impl.makeValue(vi)
            }
          }
          while (!done.getAcquire) {
            val key = impl.makeKey(3 * rnd.nextInt(10))
            if (map.computeIfPresent(key, incValue) != null)
              invocations(0) += 1
          }
          if (remappingFunctionCalledAtMostOnce)
            mustEqual(invocations(0), invocations(1))
          expectedSum.getAndAdd(invocations(0))
        }
      }
    )
    var ti = nTasks
    while (ti > 0) {
      ti -= 1
      val task = chooseRandomly(tasks)
      futures.add(CompletableFuture.runAsync(checkedRunnable(task)))
    }
    Thread.sleep(testDurationMillis)
    done.setRelease(true)
    var fi = 0
    while (fi < futures.size()) {
      checkTimedGet(futures.get(fi), null.asInstanceOf[Void])
      fi += 1
    }

    var sum = 0L
    val values = map.values().iterator()
    while (values.hasNext()) {
      sum += values.next().asInstanceOf[Integer].intValue()
    }
    mustEqual(expectedSum.get(), sum)
  }
}

class ConcurrentSkipListMap_MapTest extends MapTest {
  val impl: MapImplementation = new MapImplementation {
    def klazz(): Class[_] =
      classOf[java.util.concurrent.ConcurrentSkipListMap[_, _]]
    def emptyMap(): Map[_, _] =
      new java.util.concurrent.ConcurrentSkipListMap[AnyRef, AnyRef]()
    def isConcurrent() = true
    override def remappingFunctionCalledAtMostOnce() = false
    def permitsNullKeys() = false
    def permitsNullValues() = false
    def supportsSetValue() = false
  }
}
