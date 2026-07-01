package org.scalanative.testsuite.javalib.util
package concurrent
package atomic

import java.util._
import java.util.concurrent.atomic._
import java.util.function.UnaryOperator

import org.junit.Assert._
import org.junit.{Ignore, Test}

import org.scalanative.testsuite.javalib.util.concurrent.JSR166Test

class AtomicReferenceTest extends JSR166Test {
  import JSR166Test._

  // This test suite is INCOMPLETE (obviously!).
  //
  // The get() method is used by getAndUpdate() and updateAndGet().
  // The test is only a shallow probe before use.
  //
  // getAndUpdate() and updateAndGet() test only that the expected
  // values are returned in the success case. It was not evident
  // how to test concurrent and contended access patterns within
  // the scope of unit-tests.

  @Test def get(): Unit = {

    val expected = -1
    val ar = new AtomicReference(expected)

    val result = ar.get()

    assertTrue(
      s"result: ${result} != expected: ${expected}",
      result == expected
    )
  }

  @Test def getAndUpdateUpdateFunction(): Unit = {

    val expectedValue = 100
    val expectedNewValue = expectedValue / 2

    val tax = new UnaryOperator[Int] {
      override def apply(t: Int): Int = t / 2
    }

    val ar = new AtomicReference[Int](expectedValue)

    val value = ar.getAndUpdate(tax)

    assertTrue(
      s"result before function: ${value} != expected: ${expectedValue}",
      value == expectedValue
    )

    val newValue = ar.get()

    assertTrue(
      s"newValue after function: ${newValue} != " +
        s"expected: ${expectedNewValue}",
      newValue == expectedNewValue
    )
  }

  @Test def updateAndGetUpdateFunction(): Unit = {

    val initialValue = 100
    val expected = initialValue * 3

    val reward = new UnaryOperator[Int] {
      override def apply(t: Int): Int = t * 3
    }

    val ar = new AtomicReference[Int](initialValue)

    val result = ar.updateAndGet(reward)

    assertTrue(
      s"result after function: ${result} != expected: ${expected}",
      result == expected
    )
  }

  @Test def testConstructor(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertSame(one, ai.get())
  }

  @Test def testConstructor2(): Unit = {
    val ai = new AtomicReference[Integer]()
    assertNull(ai.get())
  }

  @Test def testGetSet(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertSame(one, ai.get())
    ai.set(two)
    assertSame(two, ai.get())
    ai.set(m3)
    assertSame(m3, ai.get())
  }

  @Test def testGetLazySet(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertSame(one, ai.get())
    ai.lazySet(two)
    assertSame(two, ai.get())
    ai.lazySet(m3)
    assertSame(m3, ai.get())
  }

  @Test def testCompareAndSet(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertTrue(ai.compareAndSet(one, two))
    assertTrue(ai.compareAndSet(two, m4))
    assertSame(m4, ai.get())
    assertFalse(ai.compareAndSet(m5, seven))
    assertSame(m4, ai.get())
    assertTrue(ai.compareAndSet(m4, seven))
    assertSame(seven, ai.get())
  }

  @throws[Exception]
  @Test def testCompareAndSetInMultipleThreads(): Unit = {
    val ai = new AtomicReference[Integer](one)
    val t = new Thread(new CheckedRunnable() {
      override def realRun(): Unit = {
        while (!ai.compareAndSet(two, three)) Thread.`yield`()
      }
    })

    t.start()
    assertTrue(ai.compareAndSet(one, two))
    t.join(LONG_DELAY_MS)
    assertFalse(t.isAlive())
    assertSame(three, ai.get())
  }

  @deprecated
  @Test def testWeakCompareAndSet(): Unit = {
    val ai = new AtomicReference[Integer](one)
    while (!ai.weakCompareAndSet(one, two)) ()
    while (!ai.weakCompareAndSet(two, m4)) ()
    assertSame(m4, ai.get())
    while (!ai.weakCompareAndSet(m4, seven)) ()
    assertSame(seven, ai.get())
  }

  @Test def testGetAndSet(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertSame(one, ai.getAndSet(zero))
    assertSame(zero, ai.getAndSet(m10))
    assertSame(m10, ai.getAndSet(one))
  }

  @Ignore("scala-native#4852: ObjectInputStream is unsupported")
  @Test def testSerialization(): Unit = ()

  @Test def testToString(): Unit = {
    val ai = new AtomicReference[Integer](one)
    assertEquals(one.toString(), ai.toString())
    ai.set(two)
    assertEquals(two.toString(), ai.toString())
  }

}
