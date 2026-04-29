/*
 * Ported from JSR-166 TCK tests and released to the public domain, as
 * explained at http://creativecommons.org/publicdomain/zero/1.0/
 *
 * Modified for Scala Native.
 */

package org.scalanative.testsuite.javalib.util.concurrent

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit._
import java.util.concurrent.{ThreadLocalRandom, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class TimeUnit8Test extends JSR166Test {
  @Test def testToChronoUnit(): Unit = {
    assertSame(ChronoUnit.NANOS, NANOSECONDS.toChronoUnit())
    assertSame(ChronoUnit.MICROS, MICROSECONDS.toChronoUnit())
    assertSame(ChronoUnit.MILLIS, MILLISECONDS.toChronoUnit())
    assertSame(ChronoUnit.SECONDS, SECONDS.toChronoUnit())
    assertSame(ChronoUnit.MINUTES, MINUTES.toChronoUnit())
    assertSame(ChronoUnit.HOURS, HOURS.toChronoUnit())
    assertSame(ChronoUnit.DAYS, DAYS.toChronoUnit())

    TimeUnit.values().foreach { x =>
      assertSame(x, TimeUnit.of(x.toChronoUnit()))
    }
  }

  @Test def testTimeUnitOf(): Unit = {
    assertSame(NANOSECONDS, TimeUnit.of(ChronoUnit.NANOS))
    assertSame(MICROSECONDS, TimeUnit.of(ChronoUnit.MICROS))
    assertSame(MILLISECONDS, TimeUnit.of(ChronoUnit.MILLIS))
    assertSame(SECONDS, TimeUnit.of(ChronoUnit.SECONDS))
    assertSame(MINUTES, TimeUnit.of(ChronoUnit.MINUTES))
    assertSame(HOURS, TimeUnit.of(ChronoUnit.HOURS))
    assertSame(DAYS, TimeUnit.of(ChronoUnit.DAYS))

    assertThrows(classOf[NullPointerException], TimeUnit.of(null))

    ChronoUnit.values().foreach { cu =>
      try {
        val tu = TimeUnit.of(cu)
        assertSame(cu, tu.toChronoUnit())
      } catch {
        case _: IllegalArgumentException => ()
      }
    }
  }

  @Test def testConvertDuration_roundtripDurationOf(): Unit = {
    var n = ThreadLocalRandom.current().nextLong()

    assertEquals(n, NANOSECONDS.convert(Duration.ofNanos(n)))
    assertEquals(n, NANOSECONDS.convert(Duration.of(n, ChronoUnit.NANOS)))
    assertEquals(n, MILLISECONDS.convert(Duration.ofMillis(n)))
    assertEquals(n, MILLISECONDS.convert(Duration.of(n, ChronoUnit.MILLIS)))
    assertEquals(n, SECONDS.convert(Duration.ofSeconds(n)))
    assertEquals(n, SECONDS.convert(Duration.of(n, ChronoUnit.SECONDS)))
    n /= 60L
    assertEquals(n, MINUTES.convert(Duration.ofMinutes(n)))
    assertEquals(n, MINUTES.convert(Duration.of(n, ChronoUnit.MINUTES)))
    n /= 60L
    assertEquals(n, HOURS.convert(Duration.ofHours(n)))
    assertEquals(n, HOURS.convert(Duration.of(n, ChronoUnit.HOURS)))
    n /= 24L
    assertEquals(n, DAYS.convert(Duration.ofDays(n)))
    assertEquals(n, DAYS.convert(Duration.of(n, ChronoUnit.DAYS)))
  }

  @Test def testConvertDuration_roundtripDurationOfNanos(): Unit = {
    val unitNanos = TimeUnit.values().map(_.toNanos(1L))
    val baseValues = unitNanos ++ Array(Long.MaxValue, Long.MinValue)
    for {
      n0 <- baseValues
      n1 <- Array(n0, n0 + 1L, n0 - 1L)
      n2 <- Array(n1, n1 + 1000000000L, n1 - 1000000000L)
      n <- Array(n2, -n2)
      u <- TimeUnit.values()
    } {
      assertEquals(u.convert(n, NANOSECONDS), u.convert(Duration.ofNanos(n)))
    }
  }

  @Test def testConvertDuration_nearOverflow(): Unit = {
    val nanos = ChronoUnit.NANOS
    val maxDuration = Duration.ofSeconds(Long.MaxValue, 999999999L)
    val minDuration = Duration.ofSeconds(Long.MinValue, 0L)

    TimeUnit.values().foreach { u =>
      val cu = u.toChronoUnit()
      val r =
        if (u.toNanos(1L) > SECONDS.toNanos(1L)) {
          val ratio = u.toNanos(1L) / SECONDS.toNanos(1L)
          assertThrows(
            classOf[ArithmeticException],
            Duration.of(Long.MaxValue, cu)
          )
          assertThrows(
            classOf[ArithmeticException],
            Duration.of(Long.MinValue, cu)
          )
          ratio
        } else {
          val max = Duration.of(Long.MaxValue, cu)
          val min = Duration.of(Long.MinValue, cu)
          assertEquals(Long.MaxValue, u.convert(max))
          assertEquals(Long.MaxValue - 1L, u.convert(max.minus(1L, nanos)))
          assertEquals(Long.MaxValue - 1L, u.convert(max.minus(1L, cu)))
          assertEquals(Long.MinValue, u.convert(min))
          assertEquals(Long.MinValue + 1L, u.convert(min.plus(1L, nanos)))
          assertEquals(Long.MinValue + 1L, u.convert(min.plus(1L, cu)))
          assertEquals(Long.MaxValue, u.convert(max.plus(1L, nanos)))
          if (u != SECONDS) {
            assertEquals(Long.MaxValue, u.convert(max.plus(1L, cu)))
            assertEquals(Long.MinValue, u.convert(min.minus(1L, nanos)))
            assertEquals(Long.MinValue, u.convert(min.minus(1L, cu)))
          }
          1L
        }

      assertEquals(Long.MaxValue / r, u.convert(maxDuration))
      assertEquals(Long.MinValue / r, u.convert(minDuration))
    }
  }
}
