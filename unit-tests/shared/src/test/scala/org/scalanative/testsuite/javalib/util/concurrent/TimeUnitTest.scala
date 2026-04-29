/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.TimeUnit._
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.{Ignore, Test}

class TimeUnitTest extends JSR166Test {
  import JSR166Test._

  private def testConversion(
      x: TimeUnit,
      y: TimeUnit,
      n: Long,
      expected: Long
  ): Unit = {
    assertEquals(expected, x.convert(n, y))
    x match {
      case NANOSECONDS  => assertEquals(expected, y.toNanos(n))
      case MICROSECONDS => assertEquals(expected, y.toMicros(n))
      case MILLISECONDS => assertEquals(expected, y.toMillis(n))
      case SECONDS      => assertEquals(expected, y.toSeconds(n))
      case MINUTES      => assertEquals(expected, y.toMinutes(n))
      case HOURS        => assertEquals(expected, y.toHours(n))
      case DAYS         => assertEquals(expected, y.toDays(n))
      case _            => throw new AssertionError()
    }

    if (n > 0L) testConversion(x, y, -n, -expected)
  }

  private def testConversion(x: TimeUnit, y: TimeUnit): Unit = {
    val ratio = x.toNanos(1L) / y.toNanos(1L)
    assertTrue(ratio > 0L)
    val ns = Array(0L, 1L, 2L, Long.MaxValue / ratio, Long.MinValue / ratio)
    ns.foreach { n =>
      testConversion(y, x, n, n * ratio)
      Array(n * ratio, n * ratio + 1L, n * ratio - 1L).foreach { k =>
        testConversion(x, y, k, k / ratio)
      }
    }
  }

  @Test def testConversions(): Unit = {
    assertEquals(1L, NANOSECONDS.toNanos(1L))
    assertEquals(1000L * NANOSECONDS.toNanos(1L), MICROSECONDS.toNanos(1L))
    assertEquals(1000L * MICROSECONDS.toNanos(1L), MILLISECONDS.toNanos(1L))
    assertEquals(1000L * MILLISECONDS.toNanos(1L), SECONDS.toNanos(1L))
    assertEquals(60L * SECONDS.toNanos(1L), MINUTES.toNanos(1L))
    assertEquals(60L * MINUTES.toNanos(1L), HOURS.toNanos(1L))
    assertEquals(24L * HOURS.toNanos(1L), DAYS.toNanos(1L))

    TimeUnit.values().foreach { x =>
      assertEquals(x.toNanos(1L), NANOSECONDS.convert(1L, x))
    }

    for {
      x <- TimeUnit.values()
      y <- TimeUnit.values()
      if x.toNanos(1L) >= y.toNanos(1L)
    } testConversion(x, y)
  }

  @Test def testConvertSaturate(): Unit = {
    assertEquals(
      Long.MaxValue,
      NANOSECONDS.convert(Long.MaxValue / 2L, SECONDS)
    )
    assertEquals(
      Long.MinValue,
      NANOSECONDS.convert(-Long.MaxValue / 4L, SECONDS)
    )
    assertEquals(
      Long.MaxValue,
      NANOSECONDS.convert(Long.MaxValue / 2L, MINUTES)
    )
    assertEquals(
      Long.MinValue,
      NANOSECONDS.convert(-Long.MaxValue / 4L, MINUTES)
    )
    assertEquals(Long.MaxValue, NANOSECONDS.convert(Long.MaxValue / 2L, HOURS))
    assertEquals(Long.MinValue, NANOSECONDS.convert(-Long.MaxValue / 4L, HOURS))
    assertEquals(Long.MaxValue, NANOSECONDS.convert(Long.MaxValue / 2L, DAYS))
    assertEquals(Long.MinValue, NANOSECONDS.convert(-Long.MaxValue / 4L, DAYS))

    for {
      x <- TimeUnit.values()
      y <- TimeUnit.values()
    } {
      val ratio = x.toNanos(1L) / y.toNanos(1L)
      if (ratio >= 1L) {
        assertEquals(ratio, y.convert(1L, x))
        assertEquals(1L, x.convert(ratio, y))
        val max = Long.MaxValue / ratio
        assertEquals(max * ratio, y.convert(max, x))
        assertEquals(-max * ratio, y.convert(-max, x))
        assertEquals(max, x.convert(max * ratio, y))
        assertEquals(-max, x.convert(-max * ratio, y))
        if (max < Long.MaxValue) {
          assertEquals(Long.MaxValue, y.convert(max + 1L, x))
          assertEquals(Long.MinValue, y.convert(-max - 1L, x))
          assertEquals(Long.MinValue, y.convert(Long.MinValue + 1L, x))
        }
        assertEquals(Long.MaxValue, y.convert(Long.MaxValue, x))
        assertEquals(Long.MinValue, y.convert(Long.MinValue, x))
      }
    }
  }

  @Test def testToNanosSaturate(): Unit = {
    assertEquals(Long.MaxValue, MILLISECONDS.toNanos(Long.MaxValue / 2L))
    assertEquals(Long.MinValue, MILLISECONDS.toNanos(-Long.MaxValue / 3L))

    TimeUnit.values().foreach { x =>
      val ratio = x.toNanos(1L) / NANOSECONDS.toNanos(1L)
      if (ratio >= 1L) {
        val max = Long.MaxValue / ratio
        Array(0L, 1L, -1L, max, -max).foreach { z =>
          assertEquals(z * ratio, x.toNanos(z))
        }
        if (max < Long.MaxValue) {
          assertEquals(Long.MaxValue, x.toNanos(max + 1L))
          assertEquals(Long.MinValue, x.toNanos(-max - 1L))
          assertEquals(Long.MinValue, x.toNanos(Long.MinValue + 1L))
        }
        assertEquals(Long.MaxValue, x.toNanos(Long.MaxValue))
        assertEquals(Long.MinValue, x.toNanos(Long.MinValue))
        if (max < Int.MaxValue) {
          assertEquals(Long.MaxValue, x.toNanos(Int.MaxValue.toLong))
          assertEquals(Long.MinValue, x.toNanos(Int.MinValue.toLong))
        }
      }
    }
  }

  @Test def testToMicrosSaturate(): Unit =
    testSaturatingTo(MICROSECONDS, _.toMicros(_))

  @Test def testToMillisSaturate(): Unit =
    testSaturatingTo(MILLISECONDS, _.toMillis(_))

  @Test def testToSecondsSaturate(): Unit =
    testSaturatingTo(SECONDS, _.toSeconds(_))

  private def testSaturatingTo(
      target: TimeUnit,
      convert: (TimeUnit, Long) => Long
  ): Unit = {
    TimeUnit.values().foreach { x =>
      val ratio = x.toNanos(1L) / target.toNanos(1L)
      if (ratio >= 1L) {
        val max = Long.MaxValue / ratio
        Array(0L, 1L, -1L, max, -max).foreach { z =>
          assertEquals(z * ratio, convert(x, z))
        }
        if (max < Long.MaxValue) {
          assertEquals(Long.MaxValue, convert(x, max + 1L))
          assertEquals(Long.MinValue, convert(x, -max - 1L))
          assertEquals(Long.MinValue, convert(x, Long.MinValue + 1L))
        }
        assertEquals(Long.MaxValue, convert(x, Long.MaxValue))
        assertEquals(Long.MinValue, convert(x, Long.MinValue))
        if (max < Int.MaxValue) {
          assertEquals(Long.MaxValue, convert(x, Int.MaxValue.toLong))
          assertEquals(Long.MinValue, convert(x, Int.MinValue.toLong))
        }
      }
    }
  }

  @Test def testToMinutesSaturate(): Unit = {
    TimeUnit.values().foreach { x =>
      val ratio = x.toNanos(1L) / MINUTES.toNanos(1L)
      if (ratio > 1L) {
        val max = Long.MaxValue / ratio
        Array(0L, 1L, -1L, max, -max).foreach { z =>
          assertEquals(z * ratio, x.toMinutes(z))
        }
        assertEquals(Long.MaxValue, x.toMinutes(max + 1L))
        assertEquals(Long.MinValue, x.toMinutes(-max - 1L))
        assertEquals(Long.MaxValue, x.toMinutes(Long.MaxValue))
        assertEquals(Long.MinValue, x.toMinutes(Long.MinValue))
        assertEquals(Long.MinValue, x.toMinutes(Long.MinValue + 1L))
      }
    }
  }

  @Test def testToHoursSaturate(): Unit = {
    TimeUnit.values().foreach { x =>
      val ratio = x.toNanos(1L) / HOURS.toNanos(1L)
      if (ratio >= 1L) {
        val max = Long.MaxValue / ratio
        Array(0L, 1L, -1L, max, -max).foreach { z =>
          assertEquals(z * ratio, x.toHours(z))
        }
        if (max < Long.MaxValue) {
          assertEquals(Long.MaxValue, x.toHours(max + 1L))
          assertEquals(Long.MinValue, x.toHours(-max - 1L))
          assertEquals(Long.MinValue, x.toHours(Long.MinValue + 1L))
        }
        assertEquals(Long.MaxValue, x.toHours(Long.MaxValue))
        assertEquals(Long.MinValue, x.toHours(Long.MinValue))
      }
    }
  }

  @Test def testToString(): Unit = {
    assertEquals("NANOSECONDS", NANOSECONDS.toString())
    assertEquals("MICROSECONDS", MICROSECONDS.toString())
    assertEquals("MILLISECONDS", MILLISECONDS.toString())
    assertEquals("SECONDS", SECONDS.toString())
    assertEquals("MINUTES", MINUTES.toString())
    assertEquals("HOURS", HOURS.toString())
    assertEquals("DAYS", DAYS.toString())
  }

  @Test def testName(): Unit = {
    TimeUnit.values().foreach { x =>
      assertEquals(x.toString(), x.name())
    }
  }

  @Ignore("scala-native#4847")
  @Test def testTimedWait_IllegalMonitorException(): Unit = {
    val t = newStartedThread(new CheckedRunnable {
      override protected def realRun(): Unit = {
        val o = new Object()
        try {
          MILLISECONDS.timedWait(o, LONGER_DELAY_MS)
          threadShouldThrow()
        } catch {
          case _: IllegalMonitorStateException => ()
        }
      }
    })

    awaitTermination(t)
  }

  @Ignore("scala-native#4847")
  @Test def testTimedWait_Interruptible(): Unit = {
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override protected def realRun(): Unit = {
        val o = new Object()

        Thread.currentThread().interrupt()
        try {
          o.synchronized {
            MILLISECONDS.timedWait(o, LONGER_DELAY_MS)
          }
          shouldThrow()
        } catch {
          case _: InterruptedException => ()
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          o.synchronized {
            MILLISECONDS.timedWait(o, LONGER_DELAY_MS)
          }
          shouldThrow()
        } catch {
          case _: InterruptedException => ()
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  @Test def testTimedJoin_Interruptible(): Unit = {
    val pleaseInterrupt = new CountDownLatch(1)
    val s = newStartedThread(new CheckedInterruptedRunnable {
      override protected def realRun(): Unit = {
        Thread.sleep(LONGER_DELAY_MS)
      }
    })
    val t = newStartedThread(new CheckedRunnable {
      override protected def realRun(): Unit = {
        Thread.currentThread().interrupt()
        try {
          MILLISECONDS.timedJoin(s, LONGER_DELAY_MS)
          shouldThrow()
        } catch {
          case _: InterruptedException => ()
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          MILLISECONDS.timedJoin(s, LONGER_DELAY_MS)
          shouldThrow()
        } catch {
          case _: InterruptedException => ()
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
    s.interrupt()
    awaitTermination(s)
  }

  @Test def testTimedSleep_Interruptible(): Unit = {
    val pleaseInterrupt = new CountDownLatch(1)
    val t = newStartedThread(new CheckedRunnable {
      override protected def realRun(): Unit = {
        Thread.currentThread().interrupt()
        try {
          MILLISECONDS.sleep(LONGER_DELAY_MS)
          shouldThrow()
        } catch {
          case _: InterruptedException => ()
        }
        assertFalse(Thread.interrupted())

        pleaseInterrupt.countDown()
        try {
          MILLISECONDS.sleep(LONGER_DELAY_MS)
          shouldThrow()
        } catch {
          case _: InterruptedException => ()
        }
        assertFalse(Thread.interrupted())
      }
    })

    await(pleaseInterrupt)
    if (randomBoolean()) assertThreadBlocks(t, Thread.State.TIMED_WAITING)
    t.interrupt()
    awaitTermination(t)
  }

  @Test def testTimedSleep_nonPositive(): Unit = {
    val interrupt = randomBoolean()
    if (interrupt) Thread.currentThread().interrupt()
    randomTimeUnit().sleep(0L)
    randomTimeUnit().sleep(-1L)
    randomTimeUnit().sleep(Long.MinValue)
    if (interrupt) assertTrue(Thread.interrupted())
  }

  @Ignore("No ObjectInputStreams in Scala Native")
  @Test def testSerialization(): Unit = ()
}
