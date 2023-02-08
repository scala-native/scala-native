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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

import org.junit._
import org.junit.Assert._

import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.{RawPtr, fromRawPtr}
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.atomic.{CAtomicRef, memory_order}

object AtomicReferenceFieldUpdaterTest {
  class IntrinsicBasedImpl[T <: AnyRef, V <: AnyRef](
      atomicRef: T => CAtomicRef[V]
  ) extends AtomicReferenceFieldUpdater[T, V]() {
    def compareAndSet(obj: T, expect: V, update: V): Boolean =
      atomicRef(obj).compareExchangeStrong(expect, update)

    def weakCompareAndSet(obj: T, expect: V, update: V): Boolean =
      atomicRef(obj).compareExchangeWeak(expect, update)

    def set(obj: T, newIntalue: V): Unit = atomicRef(obj).store(newIntalue)

    def lazySet(obj: T, newIntalue: V): Unit =
      atomicRef(obj).store(newIntalue, memory_order.memory_order_release)
    def get(obj: T): V = atomicRef(obj).load()
  }
}

class AtomicReferenceFieldUpdaterTest extends JSR166Test {
  import AtomicReferenceFieldUpdaterTest._
  import JSR166Test._

  @volatile var x: Integer = null
  @volatile protected var protectedField: Integer = null

  def updaterForX =
    new IntrinsicBasedImpl[AtomicReferenceFieldUpdaterTest, Integer](obj =>
      new CAtomicRef(
        fromRawPtr(
          classFieldRawPtr(obj, "x")
        )
      )
    )
  def updaterForProtectedField =
    new IntrinsicBasedImpl[AtomicReferenceFieldUpdaterTest, Integer](obj =>
      new CAtomicRef(
        fromRawPtr(
          classFieldRawPtr(obj, "protectedField")
        )
      )
    )

  // Platform limitatios: following cases would not compile / would not be checked
  // Construction with non-existent field throws RuntimeException
  // @Test def testConstructor(): Unit = ???

  // construction with field not of given type throws IllegalArgumentException
  // @Test def testConstructor2(): Unit = ???

  // construction with non-volatile field throws IllegalArgumentException
  // @Test def testConstructor3(): Unit = ???

  // construction using private field from subclass throws RuntimeException
  // @Test def testPrivateFieldInSubclass(): Unit = ???

  // Constructor with non-reference field throws ClassCastException
  // @Test def testConstructor4(): Unit = ???

  /** construction from unrelated class; package access is allowed, private
   *  access is not
   */
  @Test def testUnrelatedClassAccess(): Unit = {
    new NonNestmates().checkPackageAccess(this)
    // Imposible to create
    // new NonNestmates().checkPrivateAccess(this)
  }

  /** get returns the last value set or assigned
   */
  @Test def testGetSet(): Unit = {
    val a = updaterForX
    x = one
    assertSame(one, a.get(this))
    a.set(this, two)
    assertSame(two, a.get(this))
    a.set(this, m3)
    assertSame(m3, a.get(this))
  }

  /** get returns the last value lazySet by same thread
   */
  @Test def testGetLazySet(): Unit = {
    val a = updaterForX
    x = one
    assertSame(one, a.get(this))
    a.lazySet(this, two)
    assertSame(two, a.get(this))
    a.lazySet(this, m3)
    assertSame(m3, a.get(this))
  }

  /** compareAndSet succeeds in changing value if same as expected else fails
   */
  @Test def testCompareAndSet(): Unit = {
    val a = updaterForX
    x = one
    assertTrue(a.compareAndSet(this, one, two))
    assertTrue(a.compareAndSet(this, two, m4))
    assertSame(m4, a.get(this))
    assertFalse(a.compareAndSet(this, m5, seven))
    assertNotSame(seven, a.get(this))
    assertTrue(a.compareAndSet(this, m4, seven))
    assertSame(seven, a.get(this))
  }

  /** compareAndSet succeeds in changing protected field value if same as
   *  expected else fails
   */
  @Test def testCompareAndSetProtectedInSubclass(): Unit = {
    new NonNestmates.AtomicReferenceFieldUpdaterTestSubclass()
      .checkCompareAndSetProtectedSub()
  }

  /** compareAndSet in one thread enables another waiting for value to succeed
   */
  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    val self = this
    x = one
    val a = updaterForX
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while (!a.compareAndSet(self, two, three)) Thread.`yield`()
      }
    })
    t.start()
    assertTrue(a.compareAndSet(this, one, two))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive)
    assertSame(three, a.get(this))
  }

  /** repeated weakCompareAndSet succeeds in changing value when same as
   *  expected
   */
  @Test def testWeakCompareAndSet(): Unit = {
    val a = updaterForX
    x = one
    while (!a.weakCompareAndSet(this, one, two)) ()
    while (!a.weakCompareAndSet(this, two, m4)) ()
    assertSame(m4, a.get(this))
    while (!a.weakCompareAndSet(this, m4, seven)) ()
    assertSame(seven, a.get(this))
  }

  /** getAndSet returns previous value and sets to given value
   */
  @Test def testGetAndSet(): Unit = {
    val a = updaterForX
    x = one
    assertSame(one, a.getAndSet(this, zero))
    assertSame(zero, a.getAndSet(this, m10))
    assertSame(m10, a.getAndSet(this, 1))
  }
}
