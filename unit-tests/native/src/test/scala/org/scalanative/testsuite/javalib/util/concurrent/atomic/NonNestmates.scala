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

import org.junit.Assert._
import JSR166Test._

import java.util.concurrent.atomic._
import scala.scalanative.runtime.Intrinsics.classFieldRawPtr

/** This source file contains test code deliberately not contained in the same
 *  source file as the tests that use them, to avoid making them nestmates,
 *  which affects accessibility rules (see JEP 181).
 */
object NonNestmates {
  class AtomicIntegerFieldUpdaterTestSubclass
      extends AtomicIntegerFieldUpdaterTest {
    // Impossible to test
    // Intrinsic based field updater does not allow to access private fields at compile time
    // In JVM it fails at runtime
    // def checkPrivateAccess(): Unit = ???

    def checkCompareAndSetProtectedSub(): Unit = {
      // AtomicIntegerFieldUpdater.newUpdater(
      //   classOf[AtomicIntegerFieldUpdaterTest],
      //   "protectedField"
      // )
      val a = new AtomicIntegerFieldUpdaterTest.IntrinsicBasedImpl[
        AtomicIntegerFieldUpdaterTest
      ](
        classFieldRawPtr(_, "protectedField")
      )
      this.protectedField = 1
      assertTrue(a.compareAndSet(this, 1, 2))
      assertTrue(a.compareAndSet(this, 2, -4))
      assertEquals(-4, a.get(this))
      assertFalse(a.compareAndSet(this, -5, 7))
      assertEquals(-4, a.get(this))
      assertTrue(a.compareAndSet(this, -4, 7))
      assertEquals(7, a.get(this))
    }
  }

  class AtomicLongFieldUpdaterTestSubclass extends AtomicLongFieldUpdaterTest {
    // Impossible, see AtomicIntFieldUpdaterTestSubclass
    // def checkPrivateAccess(): Unit = ???
    def checkCompareAndSetProtectedSub(): Unit = {
      val a = new AtomicIntegerFieldUpdaterTest.IntrinsicBasedImpl[
        AtomicLongFieldUpdaterTest
      ](
        classFieldRawPtr(_, "protectedField")
      )
      this.protectedField = 1
      assertTrue(a.compareAndSet(this, 1, 2))
      assertTrue(a.compareAndSet(this, 2, -4))
      assertEquals(-4, a.get(this))
      assertFalse(a.compareAndSet(this, -5, 7))
      assertEquals(-4, a.get(this))
      assertTrue(a.compareAndSet(this, -4, 7))
      assertEquals(7, a.get(this))
    }
  }
  class AtomicReferenceFieldUpdaterTestSubclass
      extends AtomicReferenceFieldUpdaterTest {
    // Impossible, see AtomicIntFieldUpdaterTestSubclass
    // def checkPrivateAccess(): Unit = ???
    def checkCompareAndSetProtectedSub(): Unit = {
      // val a = AtomicReferenceFieldUpdater.newUpdater(
      //   classOf[AtomicReferenceFieldUpdaterTest],
      //   classOf[Integer],
      //   "protectedField"
      // )
      val a = new AtomicReferenceFieldUpdaterTest.IntrinsicBasedImpl[
        AtomicReferenceFieldUpdaterTest,
        Integer
      ](classFieldRawPtr(_, "protectedField"))
      this.protectedField = one
      assertTrue(a.compareAndSet(this, one, two))
      assertTrue(a.compareAndSet(this, two, m4))
      assertSame(m4, a.get(this))
      assertFalse(a.compareAndSet(this, m5, seven))
      assertNotSame(seven, a.get(this))
      assertTrue(a.compareAndSet(this, m4, seven))
      assertSame(seven, a.get(this))
    }
  }
}

class NonNestmates {
  def checkPackageAccess(obj: AtomicIntegerFieldUpdaterTest): Unit = {
    obj.x = 72
    val a = new AtomicIntegerFieldUpdaterTest.IntrinsicBasedImpl(
      classFieldRawPtr[AtomicIntegerFieldUpdaterTest](_, "x")
    )
    assertEquals(72, a.get(obj))
    assertTrue(a.compareAndSet(obj, 72, 73))
    assertEquals(73, a.get(obj))
  }
  def checkPackageAccess(obj: AtomicLongFieldUpdaterTest): Unit = {
    obj.x = 72L
    val a = new AtomicLongFieldUpdaterTest.IntrinsicBasedImpl(
      classFieldRawPtr[AtomicLongFieldUpdaterTest](_, "x")
    )
    assertEquals(72L, a.get(obj))
    assertTrue(a.compareAndSet(obj, 72L, 73L))
    assertEquals(73L, a.get(obj))
  }
  def checkPackageAccess(obj: AtomicReferenceFieldUpdaterTest): Unit = {
    val one = Integer.valueOf(1)
    val two = Integer.valueOf(2)
    obj.x = one
    val a = new AtomicReferenceFieldUpdaterTest.IntrinsicBasedImpl[
      AtomicReferenceFieldUpdaterTest,
      Integer
    ](
      classFieldRawPtr(_, "x")
    )
    assertSame(one, a.get(obj))
    assertTrue(a.compareAndSet(obj, one, two))
    assertSame(two, a.get(obj))
  }

  // Impossible to test, would not compile
  // def checkPrivateAccess(obj: AtomicIntegerFieldUpdaterTest): Unit = ???
  // def checkPrivateAccess(obj: AtomicLongFieldUpdaterTest): Unit = ???
  // def checkPrivateAccess(obj: AtomicReferenceFieldUpdaterTest): Unit = ???
}
