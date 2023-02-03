/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package org.scalanative.testsuite.javalib.util.concurrent
package atomic

import java.util.concurrent.atomic._
import java.lang.{Long => jlLong}

import org.junit.Test
import org.junit.Assert._

object Atomic8Test {
  def addLong17(x: Long): Long = x + 17
  def addInt17(x: Int): Int = x + 17
  def addInteger17(x: Integer): Integer = x.intValue + 17
  def sumInteger(x: Integer, y: Integer): Integer = x.intValue + y.intValue
}

/** Tests of atomic class methods accepting lambdas introduced in JDK8.
 */
class Atomic8Test extends JSR166Test {
  import JSR166Test._

  /** AtomicLong getAndUpdate returns previous value and updates result of
   *  supplied function
   */
  @Test def testLongGetAndUpdate(): Unit = {
    val a = new AtomicLong(1L)
    assertEquals(1L, a.getAndUpdate(Atomic8Test.addLong17))
    assertEquals(18L, a.getAndUpdate(Atomic8Test.addLong17))
    assertEquals(35L, a.get)
  }

  /** AtomicLong updateAndGet updates with supplied function and returns result.
   */
  @Test def testLongUpdateAndGet(): Unit = {
    val a = new AtomicLong(1L)
    assertEquals(18L, a.updateAndGet(Atomic8Test.addLong17))
    assertEquals(35L, a.updateAndGet(Atomic8Test.addLong17))
  }

  /** AtomicLong getAndAccumulate returns previous value and updates with
   *  supplied function.
   */
  @Test def testLongGetAndAccumulate(): Unit = {
    val a = new AtomicLong(1L)
    assertEquals(1L, a.getAndAccumulate(2L, java.lang.Long.sum))
    assertEquals(3L, a.getAndAccumulate(3L, java.lang.Long.sum))
    assertEquals(6L, a.get)
  }

  /** AtomicLong accumulateAndGet updates with supplied function and returns
   *  result.
   */
  @Test def testLongAccumulateAndGet(): Unit = {
    val a = new AtomicLong(1L)
    assertEquals(7L, a.accumulateAndGet(6L, java.lang.Long.sum))
    assertEquals(10L, a.accumulateAndGet(3L, java.lang.Long.sum))
    assertEquals(10L, a.get)
  }

  /** AtomicInteger getAndUpdate returns previous value and updates result of
   *  supplied function
   */
  @Test def testIntGetAndUpdate(): Unit = {
    val a = new AtomicInteger(1)
    assertEquals(1, a.getAndUpdate(Atomic8Test.addInt17))
    assertEquals(18, a.getAndUpdate(Atomic8Test.addInt17))
    assertEquals(35, a.get)
  }

  /** AtomicInteger updateAndGet updates with supplied function and returns
   *  result.
   */
  @Test def testIntUpdateAndGet(): Unit = {
    val a = new AtomicInteger(1)
    assertEquals(18, a.updateAndGet(Atomic8Test.addInt17))
    assertEquals(35, a.updateAndGet(Atomic8Test.addInt17))
    assertEquals(35, a.get)
  }

  /** AtomicInteger getAndAccumulate returns previous value and updates with
   *  supplied function.
   */
  @Test def testIntGetAndAccumulate(): Unit = {
    val a = new AtomicInteger(1)
    assertEquals(1, a.getAndAccumulate(2, Integer.sum))
    assertEquals(3, a.getAndAccumulate(3, Integer.sum))
    assertEquals(6, a.get)
  }

  /** AtomicInteger accumulateAndGet updates with supplied function and returns
   *  result.
   */
  @Test def testIntAccumulateAndGet(): Unit = {
    val a = new AtomicInteger(1)
    assertEquals(7, a.accumulateAndGet(6, Integer.sum))
    assertEquals(10, a.accumulateAndGet(3, Integer.sum))
    assertEquals(10, a.get)
  }

  /** AtomicReference getAndUpdate returns previous value and updates result of
   *  supplied function
   */
  @Test def testReferenceGetAndUpdate(): Unit = {
    val a = new AtomicReference[Integer](one)
    assertEquals(
      1.asInstanceOf[Integer],
      a.getAndUpdate(Atomic8Test.addInteger17)
    )
    assertEquals(
      18.asInstanceOf[Integer],
      a.getAndUpdate(Atomic8Test.addInteger17)
    )
    assertEquals(35.asInstanceOf[Integer], a.get)
  }

  /** AtomicReference updateAndGet updates with supplied function and returns
   *  result.
   */
  @Test def testReferenceUpdateAndGet(): Unit = {
    val a = new AtomicReference[Integer](one)
    assertEquals(
      18.asInstanceOf[Integer],
      a.updateAndGet(Atomic8Test.addInteger17)
    )
    assertEquals(
      35.asInstanceOf[Integer],
      a.updateAndGet(Atomic8Test.addInteger17)
    )
    assertEquals(35.asInstanceOf[Integer], a.get)
  }

  /** AtomicReference getAndAccumulate returns previous value and updates with
   *  supplied function.
   */
  @Test def testReferenceGetAndAccumulate(): Unit = {
    val a = new AtomicReference[Integer](one)
    assertEquals(
      1.asInstanceOf[Integer],
      a.getAndAccumulate(2, Atomic8Test.sumInteger)
    )
    assertEquals(
      3.asInstanceOf[Integer],
      a.getAndAccumulate(3, Atomic8Test.sumInteger)
    )
    assertEquals(6.asInstanceOf[Integer], a.get)
  }

  /** AtomicReference accumulateAndGet updates with supplied function and
   *  returns result.
   */
  @Test def testReferenceAccumulateAndGet(): Unit = {
    val a = new AtomicReference[Integer](one)
    assertEquals(
      7.asInstanceOf[Integer],
      a.accumulateAndGet(6, Atomic8Test.sumInteger)
    )
    assertEquals(
      10.asInstanceOf[Integer],
      a.accumulateAndGet(3, Atomic8Test.sumInteger)
    )
    assertEquals(10.asInstanceOf[Integer], a.get)
  }

  /** AtomicLongArray getAndUpdate returns previous value and updates result of
   *  supplied function
   */
  @Test def testLongArrayGetAndUpdate(): Unit = {
    val a = new AtomicLongArray(1)
    a.set(0, 1)
    assertEquals(1L, a.getAndUpdate(0, Atomic8Test.addLong17))
    assertEquals(18L, a.getAndUpdate(0, Atomic8Test.addLong17))
    assertEquals(35L, a.get(0))
  }

  /** AtomicLongArray updateAndGet updates with supplied function and returns
   *  result.
   */
  @Test def testLongArrayUpdateAndGet(): Unit = {
    val a = new AtomicLongArray(1)
    a.set(0, 1)
    assertEquals(18L, a.updateAndGet(0, Atomic8Test.addLong17))
    assertEquals(35L, a.updateAndGet(0, Atomic8Test.addLong17))
    assertEquals(35L, a.get(0))
  }

  /** AtomicLongArray getAndAccumulate returns previous value and updates with
   *  supplied function.
   */
  @Test def testLongArrayGetAndAccumulate(): Unit = {
    val a = new AtomicLongArray(1)
    a.set(0, 1)
    assertEquals(1L, a.getAndAccumulate(0, 2L, java.lang.Long.sum))
    assertEquals(3L, a.getAndAccumulate(0, 3L, java.lang.Long.sum))
    assertEquals(6L, a.get(0))
  }

  /** AtomicLongArray accumulateAndGet updates with supplied function and
   *  returns result.
   */
  @Test def testLongArrayAccumulateAndGet(): Unit = {
    val a = new AtomicLongArray(1)
    a.set(0, 1)
    assertEquals(7L, a.accumulateAndGet(0, 6L, java.lang.Long.sum))
    assertEquals(10L, a.accumulateAndGet(0, 3L, java.lang.Long.sum))
    assertEquals(10L, a.get(0))
  }

  /** AtomicIntegerArray getAndUpdate returns previous value and updates result
   *  of supplied function
   */
  @Test def testIntArrayGetAndUpdate(): Unit = {
    val a = new AtomicIntegerArray(1)
    a.set(0, 1)
    assertEquals(1, a.getAndUpdate(0, Atomic8Test.addInt17))
    assertEquals(18, a.getAndUpdate(0, Atomic8Test.addInt17))
    assertEquals(35, a.get(0))
  }

  /** AtomicIntegerArray updateAndGet updates with supplied function and returns
   *  result.
   */
  @Test def testIntArrayUpdateAndGet(): Unit = {
    val a = new AtomicIntegerArray(1)
    a.set(0, 1)
    assertEquals(18, a.updateAndGet(0, Atomic8Test.addInt17))
    assertEquals(35, a.updateAndGet(0, Atomic8Test.addInt17))
    assertEquals(35, a.get(0))
  }

  /** AtomicIntegerArray getAndAccumulate returns previous value and updates
   *  with supplied function.
   */
  @Test def testIntArrayGetAndAccumulate(): Unit = {
    val a = new AtomicIntegerArray(1)
    a.set(0, 1)
    assertEquals(1, a.getAndAccumulate(0, 2, Integer.sum))
    assertEquals(3, a.getAndAccumulate(0, 3, Integer.sum))
    assertEquals(6, a.get(0))
  }

  /** AtomicIntegerArray accumulateAndGet updates with supplied function and
   *  returns result.
   */
  @Test def testIntArrayAccumulateAndGet(): Unit = {
    val a = new AtomicIntegerArray(1)
    a.set(0, 1)
    assertEquals(7, a.accumulateAndGet(0, 6, Integer.sum))
    assertEquals(10, a.accumulateAndGet(0, 3, Integer.sum))
  }

  /** AtomicReferenceArray getAndUpdate returns previous value and updates
   *  result of supplied function
   */
  @Test def testReferenceArrayGetAndUpdate(): Unit = {
    val a = new AtomicReferenceArray[Integer](1)
    a.set(0, one)
    assertEquals(
      1.asInstanceOf[Integer],
      a.getAndUpdate(0, Atomic8Test.addInteger17)
    )
    assertEquals(
      18.asInstanceOf[Integer],
      a.getAndUpdate(0, Atomic8Test.addInteger17)
    )
    assertEquals(35.asInstanceOf[Integer], a.get(0))
  }

  /** AtomicReferenceArray updateAndGet updates with supplied function and
   *  returns result.
   */
  @Test def testReferenceArrayUpdateAndGet(): Unit = {
    val a = new AtomicReferenceArray[Integer](1)
    a.set(0, one)
    assertEquals(
      18.asInstanceOf[Integer],
      a.updateAndGet(0, Atomic8Test.addInteger17)
    )
    assertEquals(
      35.asInstanceOf[Integer],
      a.updateAndGet(0, Atomic8Test.addInteger17)
    )
  }

  /** AtomicReferenceArray getAndAccumulate returns previous value and updates
   *  with supplied function.
   */
  @Test def testReferenceArrayGetAndAccumulate(): Unit = {
    val a = new AtomicReferenceArray[Integer](1)
    a.set(0, one)
    assertEquals(
      1.asInstanceOf[Integer],
      a.getAndAccumulate(0, 2, Atomic8Test.sumInteger)
    )
    assertEquals(
      3.asInstanceOf[Integer],
      a.getAndAccumulate(0, 3, Atomic8Test.sumInteger)
    )
    assertEquals(6.asInstanceOf[Integer], a.get(0))
  }

  /** AtomicReferenceArray accumulateAndGet updates with supplied function and
   *  returns result.
   */
  @Test def testReferenceArrayAccumulateAndGet(): Unit = {
    val a = new AtomicReferenceArray[Integer](1)
    a.set(0, one)
    assertEquals(
      7.asInstanceOf[Integer],
      a.accumulateAndGet(0, 6, Atomic8Test.sumInteger)
    )
    assertEquals(
      10.asInstanceOf[Integer],
      a.accumulateAndGet(0, 3, Atomic8Test.sumInteger)
    )
  }

  // Tests not ported, FieldUpdated is reflection based
  // @Test def testLongFieldUpdaterGetAndUpdate(): Unit = {}
  // @Test def testLongFieldUpdaterUpdateAndGet(): Unit = {}
  // @Test def testLongFieldUpdaterGetAndAccumulate(): Unit = {}
  // @Test def testLongFieldUpdaterAccumulateAndGet(): Unit = {}
  // @Test def testIntegerFieldUpdaterGetAndUpdate(): Unit = {}
  // @Test def testIntegerFieldUpdaterUpdateAndGet(): Unit = {}
  // @Test def testIntegerFieldUpdaterGetAndAccumulate(): Unit = {}
  // @Test def testIntegerFieldUpdaterAccumulateAndGet(): Unit = {}
  // @Test def testReferenceFieldUpdaterGetAndUpdate(): Unit = {}
  // @Test def testReferenceFieldUpdaterUpdateAndGet(): Unit = {}
  // @Test def testReferenceFieldUpdaterGetAndAccumulate(): Unit = {}
  // @Test def testReferenceFieldUpdaterAccumulateAndGet(): Unit = {}

  /** All Atomic getAndUpdate methods throw NullPointerException on null
   *  function argument
   */
  @Test def testGetAndUpdateNPE(): Unit =
    assertEachThrows(
      classOf[NullPointerException],
      () => new AtomicLong().getAndUpdate(null),
      () => new AtomicInteger().getAndUpdate(null),
      () => new AtomicReference[Any]().getAndUpdate(null),
      () => new AtomicLongArray(1).getAndUpdate(0, null),
      () => new AtomicIntegerArray(1).getAndUpdate(0, null),
      () => new AtomicReferenceArray[Any](1).getAndUpdate(0, null)
      // () => aLongFieldUpdater.getAndUpdate(this, null),
      // () => anIntFieldUpdater.getAndUpdate(this, null),
      // () => anIntegerFieldUpdater.getAndUpdate(this, null)
    )

  /** All Atomic updateAndGet methods throw NullPointerException on null
   *  function argument
   */
  @Test def testUpdateAndGetNPE(): Unit =
    assertEachThrows(
      classOf[NullPointerException],
      () => new AtomicLong().updateAndGet(null),
      () => new AtomicInteger().updateAndGet(null),
      () => new AtomicReference[Any]().updateAndGet(null),
      () => new AtomicLongArray(1).updateAndGet(0, null),
      () => new AtomicIntegerArray(1).updateAndGet(0, null),
      () => new AtomicReferenceArray[Any](1).updateAndGet(0, null)
      // () => aLongFieldUpdater.updateAndGet(this, null),
      // () => anIntFieldUpdater.updateAndGet(this, null),
      // () => anIntegerFieldUpdater.updateAndGet(this, null)
    )

  /** All Atomic getAndAccumulate methods throw NullPointerException on null
   *  function argument
   */
  @Test def testGetAndAccumulateNPE(): Unit =
    assertEachThrows(
      classOf[NullPointerException],
      () => new AtomicLong().getAndAccumulate(1L, null),
      () => new AtomicInteger().getAndAccumulate(1, null),
      () => new AtomicReference[Any]().getAndAccumulate(one, null),
      () => new AtomicLongArray(1).getAndAccumulate(0, 1L, null),
      () => new AtomicIntegerArray(1).getAndAccumulate(0, 1, null),
      () => new AtomicReferenceArray[Any](1).getAndAccumulate(0, one, null)
      // () => aLongFieldUpdater.getAndAccumulate(this, 1L, null),
      // () => anIntFieldUpdater.getAndAccumulate(this, 1, null),
      // () => anIntegerFieldUpdater.getAndAccumulate(this, one, null)
    )

  /** All Atomic accumulateAndGet methods throw NullPointerException on null
   *  function argument
   */
  @Test def testAccumulateAndGetNPE(): Unit =
    assertEachThrows(
      classOf[NullPointerException],
      () => new AtomicLong().accumulateAndGet(1L, null),
      () => new AtomicInteger().accumulateAndGet(1, null),
      () => new AtomicReference[Any]().accumulateAndGet(one, null),
      () => new AtomicLongArray(1).accumulateAndGet(0, 1L, null),
      () => new AtomicIntegerArray(1).accumulateAndGet(0, 1, null),
      () => new AtomicReferenceArray[Any](1).accumulateAndGet(0, one, null)
      // () => aLongFieldUpdater.accumulateAndGet(this, 1L, null),
      // () => anIntFieldUpdater.accumulateAndGet(this, 1, null),
      // () => anIntegerFieldUpdater.accumulateAndGet(this, one, null)
    )

  /** Object arguments for parameters of type T that are not instances of the
   *  class passed to the newUpdater call will result in a ClassCastException
   *  being thrown.
   */
  // @Test def testFieldUpdaters_ClassCastException(): Unit = {}
}
