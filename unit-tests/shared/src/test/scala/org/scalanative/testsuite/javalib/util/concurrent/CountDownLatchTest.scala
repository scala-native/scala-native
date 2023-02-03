/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */
package org.scalanative.testsuite.javalib.util.concurrent

import org.junit.Assert._
import org.junit.Test
import JSR166Test._

import java.util.concurrent._
import java.util.concurrent.TimeUnit.MILLISECONDS

class CountDownLatchTest extends JSR166Test {

  /** negative constructor argument throws IllegalArgumentException
   */
  @Test def testConstructor(): Unit = {
    try {
      new CountDownLatch(-1)
      shouldThrow()
    } catch {
      case success: IllegalArgumentException =>

    }
  }

  /** getCount returns initial count and decreases after countDown
   */
  @Test def testGetCount(): Unit = {
    val l = new CountDownLatch(2)
    assertEquals(2, l.getCount)
    l.countDown()
    assertEquals(1, l.getCount)
  }

  /** countDown decrements count when positive and has no effect when zero
   */
  @Test def testCountDown(): Unit = {
    val l = new CountDownLatch(1)
    assertEquals(1, l.getCount)
    l.countDown()
    assertEquals(0, l.getCount)
    l.countDown()
    assertEquals(0, l.getCount)
  }

  /** await returns after countDown to zero, but not before
   */
  @Test def testAwait(): Unit = {
    val l = new CountDownLatch(2)
    val pleaseCountDown = new CountDownLatch(1)
    val t = newStartedThread({ () =>
      assertEquals(2, l.getCount)
      pleaseCountDown.countDown()
      l.await()
      assertEquals(0, l.getCount)
    }: CheckedRunnable)
    await(pleaseCountDown)
    assertEquals(2, l.getCount)
    l.countDown()
    assertEquals(1, l.getCount)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    l.countDown()
    assertEquals(0, l.getCount)
    awaitTermination(t)
  }

  /** timed await returns after countDown to zero
   */
  @Test def testTimedAwait(): Unit = {
    val l = new CountDownLatch(2)
    val pleaseCountDown = new CountDownLatch(1)
    val t = newStartedThread({ () =>
      assertEquals(2, l.getCount)
      pleaseCountDown.countDown()
      assertTrue(l.await(LONG_DELAY_MS, MILLISECONDS))
      assertEquals(0, l.getCount)
    }: CheckedRunnable)
    await(pleaseCountDown)
    assertEquals(2, l.getCount)
    l.countDown()
    assertEquals(1, l.getCount)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    l.countDown()
    assertEquals(0, l.getCount)
    awaitTermination(t)
  }

  /** await throws InterruptedException if interrupted before counted down
   */
  @Test def testAwait_Interruptible(): Unit = {
    val l = new CountDownLatch(1)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread({ () =>
      Thread.currentThread.interrupt()
      assertThrows(classOf[InterruptedException], () => l.await())
      assertFalse(Thread.interrupted)
      pleaseInterrupt.countDown()
      assertThrows(classOf[InterruptedException], () => l.await())
      assertFalse(Thread.interrupted)
      assertEquals(1, l.getCount)
    }: CheckedRunnable)
    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** timed await throws InterruptedException if interrupted before counted down
   */
  @Test def testTimedAwait_Interruptible(): Unit = {
    val initialCount = ThreadLocalRandom.current.nextInt(1, 3)
    val l = new CountDownLatch(initialCount)
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread({ () =>
      Thread.currentThread.interrupt()
      assertThrows(
        classOf[InterruptedException],
        () => l.await(randomTimeout(), randomTimeUnit())
      )
      assertFalse(Thread.interrupted)
      pleaseInterrupt.countDown()
      assertThrows(
        classOf[InterruptedException],
        () => l.await(LONGER_DELAY_MS, MILLISECONDS)
      )
      assertFalse(Thread.interrupted)
      assertEquals(initialCount, l.getCount)
    }: CheckedRunnable)
    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  /** timed await times out if not counted down before timeout
   */
  @throws[InterruptedException]
  @Test def testAwaitTimeout(): Unit = {
    val l = new CountDownLatch(1)
    val t = newStartedThread({ () =>
      assertEquals(1, l.getCount)
      val startTime = System.nanoTime
      assertFalse(l.await(timeoutMillis(), MILLISECONDS))
      assertTrue(millisElapsedSince(startTime) >= timeoutMillis())
      assertEquals(1, l.getCount)
    }: CheckedRunnable)
    awaitTermination(t)
    assertEquals(1, l.getCount)
  }

  /** toString indicates current count
   */
  @Test def testToString(): Unit = {
    val s = new CountDownLatch(2)
    assertTrue(s.toString.contains("Count = 2"))
    s.countDown()
    assertTrue(s.toString.contains("Count = 1"))
    s.countDown()
    assertTrue(s.toString.contains("Count = 0"))
  }
}
