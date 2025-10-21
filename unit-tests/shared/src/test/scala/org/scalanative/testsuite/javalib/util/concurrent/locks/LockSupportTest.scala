/*
 * Written by Doug Lea and Martin Buchholz with assistance from
 * members of JCP JSR-166 Expert Group and released to the public
 * domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package org.scalanative.testsuite.javalib.util.concurrent
package locks

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

import org.junit.Assert._
import org.junit.{Ignore, Test}

import JSR166Test._

object LockSupportTest {

  /** Returns the blocker object used by tests in this file. Any old object will
   *  do; we'll return a convenient one.
   */
  def theBlocker: AnyRef = classOf[LockSupportTest]
  trait ParkMethod {
    def park(): Unit = { park(2 * LONG_DELAY_MS) }
    def park(millis: Long): Unit = { throw new UnsupportedOperationException }
    def parkedState: Thread.State = Thread.State.TIMED_WAITING

    /** Returns a deadline to use with parkUntil. */
    def deadline(millis: Long): Long = {
      // beware of rounding
      System.currentTimeMillis + millis + 1
    }
  }
  object ParkMethod {
    case object Park extends ParkMethod {
      override def park(): Unit = LockSupport.park()
      override def parkedState = Thread.State.WAITING
    }
    case object ParkUntil extends ParkMethod {
      override def park(millis: Long): Unit =
        LockSupport.parkUntil(deadline(millis))
    }
    case object ParkNanos extends ParkMethod {
      override def park(millis: Long): Unit =
        LockSupport.parkNanos(MILLISECONDS.toNanos(millis))
    }
    case object ParkBlocker extends ParkMethod {
      override def park(): Unit = LockSupport.park(theBlocker)
      override def parkedState = Thread.State.WAITING
    }
    case object ParkUntilBlocker extends ParkMethod {
      override def park(millis: Long): Unit =
        LockSupport.parkUntil(theBlocker, deadline(millis))
    }
    case object ParkNanosBlocker extends ParkMethod {
      override def park(millis: Long): Unit =
        LockSupport.parkNanos(theBlocker, MILLISECONDS.toNanos(millis))

    }

    def values() =
      Array(
        Park,
        ParkUntil,
        ParkNanos,
        ParkBlocker,
        ParkUntilBlocker,
        ParkNanosBlocker
      )
  }

}
class LockSupportTest extends JSR166Test {
  import LockSupportTest._
  def repeat(times: Int)(code: => Unit) =
    0.until(times).foreach(_ => code)

  /** park is released by subsequent unpark
   */
  @Test def testParkBeforeUnpark_park(): Unit = {
    testParkBeforeUnpark(ParkMethod.Park)
  }
  @Test def testParkBeforeUnpark_parkNanos(): Unit = {
    testParkBeforeUnpark(ParkMethod.ParkNanos)
  }
  @Test def testParkBeforeUnpark_parkUntil(): Unit = {
    testParkBeforeUnpark(ParkMethod.ParkUntil)
  }
  @Test def testParkBeforeUnpark_parkBlocker(): Unit = {
    testParkBeforeUnpark(ParkMethod.ParkBlocker)
  }
  @Test def testParkBeforeUnpark_parkNanosBlocker(): Unit = {
    testParkBeforeUnpark(ParkMethod.ParkNanosBlocker)
  }
  @Test def testParkBeforeUnpark_parkUntilBlocker(): Unit = {
    testParkBeforeUnpark(ParkMethod.ParkUntilBlocker)
  }
  def testParkBeforeUnpark(parkMethod: ParkMethod): Unit = repeat(10) {
    val pleaseUnpark = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        pleaseUnpark.countDown()
        parkMethod.park()
      }
    })
    await(pleaseUnpark)
    LockSupport.unpark(t)
    awaitTermination(t)
  }

  /** park is released by preceding unpark
   */
  @Test def testParkAfterUnpark_park(): Unit = {
    testParkAfterUnpark(ParkMethod.Park)
  }
  @Test def testParkAfterUnpark_parkNanos(): Unit = {
    testParkAfterUnpark(ParkMethod.ParkNanos)
  }
  @Test def testParkAfterUnpark_parkUntil(): Unit = {
    testParkAfterUnpark(ParkMethod.ParkUntil)
  }
  @Test def testParkAfterUnpark_parkBlocker(): Unit = {
    testParkAfterUnpark(ParkMethod.ParkBlocker)
  }
  @Test def testParkAfterUnpark_parkNanosBlocker(): Unit = {
    testParkAfterUnpark(ParkMethod.ParkNanosBlocker)
  }
  @Test def testParkAfterUnpark_parkUntilBlocker(): Unit = {
    testParkAfterUnpark(ParkMethod.ParkUntilBlocker)
  }
  def testParkAfterUnpark(parkMethod: ParkMethod): Unit = repeat(10) {
    val pleaseUnpark = new CountDownLatch(1)
    val pleasePark = new AtomicBoolean(false)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        pleaseUnpark.countDown()
        while ({ !pleasePark.get }) Thread.`yield`()
        parkMethod.park()
      }
    })
    await(pleaseUnpark)
    LockSupport.unpark(t)
    pleasePark.set(true)
    awaitTermination(t)
  }

  /** park is released by subsequent interrupt
   */
  @Test def testParkBeforeInterrupt_park(): Unit = {
    testParkBeforeInterrupt(ParkMethod.Park)
  }
  @Test def testParkBeforeInterrupt_parkNanos(): Unit = {
    testParkBeforeInterrupt(ParkMethod.ParkNanos)
  }
  @Test def testParkBeforeInterrupt_parkUntil(): Unit = {
    testParkBeforeInterrupt(ParkMethod.ParkUntil)
  }
  @Test def testParkBeforeInterrupt_parkBlocker(): Unit = {
    testParkBeforeInterrupt(ParkMethod.ParkBlocker)
  }
  @Test def testParkBeforeInterrupt_parkNanosBlocker(): Unit = {
    testParkBeforeInterrupt(ParkMethod.ParkNanosBlocker)
  }
  @Test def testParkBeforeInterrupt_parkUntilBlocker(): Unit = {
    testParkBeforeInterrupt(ParkMethod.ParkUntilBlocker)
  }
  def testParkBeforeInterrupt(parkMethod: ParkMethod): Unit = repeat(10) {
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        pleaseInterrupt.countDown()
        var tries = MAX_SPURIOUS_WAKEUPS
        while ({ { tries -= 1; tries + 1 } > 0 }) {
          parkMethod.park()
          if (Thread.interrupted) return
        }
        fail("too many consecutive spurious wakeups?")
      }
    })
    await(pleaseInterrupt)
    assertThreadBlocks(t, parkMethod.parkedState)
    t.interrupt()
    awaitTermination(t)
  }

  /** park is released by preceding interrupt
   */
  @Test def testParkAfterInterrupt_park(): Unit = {
    testParkAfterInterrupt(ParkMethod.Park)
  }
  @Test def testParkAfterInterrupt_parkNanos(): Unit = {
    testParkAfterInterrupt(ParkMethod.ParkNanos)
  }
  @Test def testParkAfterInterrupt_parkUntil(): Unit = {
    testParkAfterInterrupt(ParkMethod.ParkUntil)
  }
  @Test def testParkAfterInterrupt_parkBlocker(): Unit = {
    testParkAfterInterrupt(ParkMethod.ParkBlocker)
  }
  @Test def testParkAfterInterrupt_parkNanosBlocker(): Unit = {
    testParkAfterInterrupt(ParkMethod.ParkNanosBlocker)
  }
  @Test def testParkAfterInterrupt_parkUntilBlocker(): Unit = {
    testParkAfterInterrupt(ParkMethod.ParkUntilBlocker)
  }
  def testParkAfterInterrupt(parkMethod: ParkMethod): Unit = repeat(10) {
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      @throws[Exception]
      override def realRun(): Unit = {
        pleaseInterrupt.countDown()
        while ({ !Thread.currentThread.isInterrupted }) Thread.`yield`()
        parkMethod.park()
        assertTrue(Thread.interrupted)
      }
    })
    await(pleaseInterrupt)
    t.interrupt()
    awaitTermination(t)
  }

  /** timed park times out if not unparked
   */
  @Test def testParkTimesOut_parkNanos(): Unit = {
    testParkTimesOut(ParkMethod.ParkNanos)
  }
  @Test def testParkTimesOut_parkUntil(): Unit = {
    testParkTimesOut(ParkMethod.ParkUntil)
  }
  @Test def testParkTimesOut_parkNanosBlocker(): Unit = {
    testParkTimesOut(ParkMethod.ParkNanosBlocker)
  }
  @Test def testParkTimesOut_parkUntilBlocker(): Unit = {
    testParkTimesOut(ParkMethod.ParkUntilBlocker)
  }
  def testParkTimesOut(parkMethod: ParkMethod): Unit = repeat(10) {
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        var tries = MAX_SPURIOUS_WAKEUPS
        while ({ { tries -= 1; tries + 1 } > 0 }) {
          val startTime = System.nanoTime
          parkMethod.park(timeoutMillis())
          if (millisElapsedSince(startTime) >= timeoutMillis()) return
        }
        fail("too many consecutive spurious wakeups?")
      }
    })
    awaitTermination(t)
  }

  /** getBlocker(null) throws NullPointerException
   */
  @Test def testGetBlockerNull(): Unit = {
    try {
      LockSupport.getBlocker(null)
      shouldThrow()
    } catch {
      case success: NullPointerException =>

    }
  }

  /** getBlocker returns the blocker object passed to park
   */
  @Test def testGetBlocker_parkBlocker(): Unit = {
    testGetBlocker(ParkMethod.ParkBlocker)
  }
  @Test def testGetBlocker_parkNanosBlocker(): Unit = {
    testGetBlocker(ParkMethod.ParkNanosBlocker)
  }
  @Test def testGetBlocker_parkUntilBlocker(): Unit = {
    testGetBlocker(ParkMethod.ParkUntilBlocker)
  }
  def testGetBlocker(parkMethod: ParkMethod): Unit = repeat(10) {
    val started = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        val t = Thread.currentThread
        started.countDown()
        var tries = MAX_SPURIOUS_WAKEUPS
        while ({ { tries -= 1; tries + 1 } > 0 }) {
          assertNull(LockSupport.getBlocker(t))
          parkMethod.park()
          assertNull(LockSupport.getBlocker(t))
          if (Thread.interrupted) return
        }
        fail("too many consecutive spurious wakeups?")
      }
    })
    val startTime = System.nanoTime
    await(started)

    var break = false
    while (!break) {
      val x = LockSupport.getBlocker(t)
      if (x eq theBlocker) { // success
        t.interrupt()
        awaitTermination(t)
        assertNull(LockSupport.getBlocker(t))
        break = true
      } else {
        assertNull(x) // ok
        if (millisElapsedSince(startTime) > LONG_DELAY_MS) fail("timed out")
        if (t.getState eq Thread.State.TERMINATED) break = true
        else Thread.`yield`()
      }
    }
  }

  /** timed park(0) returns immediately.
   *
   *  Requires hotspot fix for: 6763959
   *  java.util.concurrent.locks.LockSupport.parkUntil(0) blocks forever which
   *  is in jdk7-b118 and 6u25.
   */
  @Test def testPark0_parkNanos(): Unit = {
    testPark0(ParkMethod.ParkNanos)
  }
  @Test def testPark0_parkUntil(): Unit = {
    testPark0(ParkMethod.ParkUntil)
  }
  @Test def testPark0_parkNanosBlocker(): Unit = {
    testPark0(ParkMethod.ParkNanosBlocker)
  }
  @Test def testPark0_parkUntilBlocker(): Unit = {
    testPark0(ParkMethod.ParkUntilBlocker)
  }
  def testPark0(parkMethod: ParkMethod): Unit = {
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = { parkMethod.park(0L) }
    })
    awaitTermination(t)
  }

  /** timed park(Long.MIN_VALUE) returns immediately.
   */
  @Test def testParkNeg_parkNanos(): Unit = {
    testParkNeg(ParkMethod.ParkNanos)
  }
  @Test def testParkNeg_parkUntil(): Unit = {
    testParkNeg(ParkMethod.ParkUntil)
  }
  @Test def testParkNeg_parkNanosBlocker(): Unit = {
    testParkNeg(ParkMethod.ParkNanosBlocker)
  }
  @Test def testParkNeg_parkUntilBlocker(): Unit = {
    testParkNeg(ParkMethod.ParkUntilBlocker)
  }
  def testParkNeg(parkMethod: ParkMethod): Unit = repeat(10) {
    val t = newStartedThread(new CheckedRunnable() {
      override def realRun(): Unit = {
        parkMethod.park(java.lang.Long.MIN_VALUE)
      }
    })
    awaitTermination(t)
  }
}
