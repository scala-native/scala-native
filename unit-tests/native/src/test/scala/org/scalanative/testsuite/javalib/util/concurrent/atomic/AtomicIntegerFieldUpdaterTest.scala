/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

// Uses custom Scala Native intrinsic based field updaters instead of reflection based used in JVM

package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater

import org.junit._
import org.junit.Assert._

import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.{RawPtr, fromRawPtr}
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.atomic.{CAtomicInt, memory_order}

object AtomicIntegerFieldUpdaterTest {
  class IntrinsicBasedImpl[T <: AnyRef](atomicRef: T => CAtomicInt)
      extends AtomicIntegerFieldUpdater[T]() {
    def compareAndSet(obj: T, expect: Int, update: Int): Boolean =
      atomicRef(obj).compareExchangeStrong(expect, update)

    def weakCompareAndSet(obj: T, expect: Int, update: Int): Boolean =
      atomicRef(obj).compareExchangeWeak(expect, update)

    def set(obj: T, newIntalue: Int): Unit = atomicRef(obj).store(newIntalue)

    def lazySet(obj: T, newIntalue: Int): Unit =
      atomicRef(obj).store(newIntalue, memory_order.memory_order_release)
    def get(obj: T): Int = atomicRef(obj).load()
  }
}

class AtomicIntegerFieldUpdaterTest extends JSR166Test {
  import JSR166Test._
  import AtomicIntegerFieldUpdaterTest._

  @volatile var x = 0
  @volatile protected var protectedField = 0

  def updaterForX = new IntrinsicBasedImpl[AtomicIntegerFieldUpdaterTest](obj =>
    new CAtomicInt(
      fromRawPtr(
        classFieldRawPtr(obj, "x")
      )
    )
  )
  def updaterForProtectedField =
    new IntrinsicBasedImpl[AtomicIntegerFieldUpdaterTest](obj =>
      new CAtomicInt(
        fromRawPtr(
          classFieldRawPtr(obj, "protectedField")
        )
      )
    )

  // Platform limitatios: following cases would not compile / would not be checked
  /** Construction with non-existent field throws RuntimeException */
  // @Test def testConstructor(): Unit = ???

  /** construction with field not of given type throws IllegalArgumentException
   */
  // @Test def testConstructor2(): Unit = ???

  /** construction with non-volatile field throws IllegalArgumentException
   */
  // @Test def testConstructor3(): Unit = ???

  /** construction using private field from subclass throws RuntimeException */
  // @Test def testPrivateFieldInSubclass(): Unit = ???

  /** construction from unrelated class; package access is allowed, private
   *  access is not
   */
  @Test def testUnrelatedClassAccess(): Unit = {
    new NonNestmates().checkPackageAccess(this)
    // would not compile - cannot have intrinsic field ptr to private field
    // new NonNestmates().checkPrivateAccess(this)
  }

  /** get returns the last value set or assigned
   */
  @Test def testGetSet(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(1, a.get(this))
    a.set(this, 2)
    assertEquals(2, a.get(this))
    a.set(this, -3)
    assertEquals(-3, a.get(this))
  }

  /** get returns the last value lazySet by same thread
   */
  @Test def testGetLazySet(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(1, a.get(this))
    a.lazySet(this, 2)
    assertEquals(2, a.get(this))
    a.lazySet(this, -3)
    assertEquals(-3, a.get(this))
  }

  /** compareAndSet succeeds in changing value if equal to expected else fails
   */
  @Test def testCompareAndSet(): Unit = {
    val a = updaterForX
    x = 1
    assertTrue(a.compareAndSet(this, 1, 2))
    assertTrue(a.compareAndSet(this, 2, -4))
    assertEquals(-4, a.get(this))
    assertFalse(a.compareAndSet(this, -5, 7))
    assertEquals(-4, a.get(this))
    assertTrue(a.compareAndSet(this, -4, 7))
    assertEquals(7, a.get(this))
  }

  /** compareAndSet succeeds in changing protected field value if equal to
   *  expected else fails
   */
  @Test def testCompareAndSetProtected(): Unit = {
    val a = updaterForProtectedField
    protectedField = 1
    assertTrue(a.compareAndSet(this, 1, 2))
    assertTrue(a.compareAndSet(this, 2, -4))
    assertEquals(-4, a.get(this))
    assertFalse(a.compareAndSet(this, -5, 7))
    assertEquals(-4, a.get(this))
    assertTrue(a.compareAndSet(this, -4, 7))
    assertEquals(7, a.get(this))
  }

  /** compareAndSet succeeds in changing protected field value if equal to
   *  expected else fails
   */
  @Test def testCompareAndSetProtectedInSubclass(): Unit = {
    new NonNestmates.AtomicIntegerFieldUpdaterTestSubclass()
      .checkCompareAndSetProtectedSub()
  }

  /** compareAndSet in one thread enables another waiting for value to succeed
   */
  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    def self = this
    x = 1
    val a = updaterForX
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while (!a.compareAndSet(self, 2, 3))
          Thread.`yield`()
      }
    })
    t.start()
    assertTrue(a.compareAndSet(this, 1, 2))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertEquals(3, a.get(this))
  }

  /** repeated weakCompareAndSet succeeds in changing value when equal to
   *  expected
   */
  @Test def testWeakCompareAndSet(): Unit = {
    val a = updaterForX
    x = 1
    while (!a.weakCompareAndSet(this, 1, 2)) ()
    while (!a.weakCompareAndSet(this, 2, -(4))) ()
    assertEquals(-4, a.get(this))
    while (!a.weakCompareAndSet(this, -(4), 7)) ()
    assertEquals(7, a.get(this))
  }

  /** getAndSet returns previous value and sets to given value
   */
  @Test def testGetAndSet(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(1, a.getAndSet(this, 0))
    assertEquals(0, a.getAndSet(this, -10))
    assertEquals(-10, a.getAndSet(this, 1))
  }

  /** getAndAdd returns previous value and adds given value
   */
  @Test def testGetAndAdd(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(1, a.getAndAdd(this, 2))
    assertEquals(3, a.get(this))
    assertEquals(3, a.getAndAdd(this, -4))
    assertEquals(-1, a.get(this))
  }

  /** getAndDecrement returns previous value and decrements
   */
  @Test def testGetAndDecrement(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(1, a.getAndDecrement(this))
    assertEquals(0, a.getAndDecrement(this))
    assertEquals(-1, a.getAndDecrement(this))
  }

  /** getAndIncrement returns previous value and increments
   */
  @Test def testGetAndIncrement(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(1, a.getAndIncrement(this))
    assertEquals(2, a.get(this))
    a.set(this, -2)
    assertEquals(-2, a.getAndIncrement(this))
    assertEquals(-1, a.getAndIncrement(this))
    assertEquals(0, a.getAndIncrement(this))
    assertEquals(1, a.get(this))
  }

  /** addAndGet adds given value to current, and returns current value
   */
  @Test def testAddAndGet(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(3, a.addAndGet(this, 2))
    assertEquals(3, a.get(this))
    assertEquals(-1, a.addAndGet(this, -4))
    assertEquals(-1, a.get(this))
  }

  /** decrementAndGet decrements and returns current value
   */
  @Test def testDecrementAndGet(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(0, a.decrementAndGet(this))
    assertEquals(-1, a.decrementAndGet(this))
    assertEquals(-2, a.decrementAndGet(this))
    assertEquals(-2, a.get(this))
  }

  /** incrementAndGet increments and returns current value
   */
  @Test def testIncrementAndGet(): Unit = {
    val a = updaterForX
    x = 1
    assertEquals(2, a.incrementAndGet(this))
    assertEquals(2, a.get(this))
    a.set(this, -2)
    assertEquals(-1, a.incrementAndGet(this))
    assertEquals(0, a.incrementAndGet(this))
    assertEquals(1, a.incrementAndGet(this))
    assertEquals(1, a.get(this))
  }
}
