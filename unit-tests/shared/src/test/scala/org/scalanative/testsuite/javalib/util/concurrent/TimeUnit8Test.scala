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
    TimeUnit8TestPlatform.assumeToChronoUnit()

    assertSame(
      ChronoUnit.NANOS,
      TimeUnit8TestPlatform.toChronoUnit(NANOSECONDS)
    )
    assertSame(
      ChronoUnit.MICROS,
      TimeUnit8TestPlatform.toChronoUnit(MICROSECONDS)
    )
    assertSame(
      ChronoUnit.MILLIS,
      TimeUnit8TestPlatform.toChronoUnit(MILLISECONDS)
    )
    assertSame(ChronoUnit.SECONDS, TimeUnit8TestPlatform.toChronoUnit(SECONDS))
    assertSame(ChronoUnit.MINUTES, TimeUnit8TestPlatform.toChronoUnit(MINUTES))
    assertSame(ChronoUnit.HOURS, TimeUnit8TestPlatform.toChronoUnit(HOURS))
    assertSame(ChronoUnit.DAYS, TimeUnit8TestPlatform.toChronoUnit(DAYS))

    if (TimeUnit8TestPlatform.hasTimeUnitOf) {
      TimeUnit.values().foreach { x =>
        assertSame(
          x,
          TimeUnit8TestPlatform
            .timeUnitOf(TimeUnit8TestPlatform.toChronoUnit(x))
        )
      }
    }
  }

  @Test def testTimeUnitOf(): Unit = {
    TimeUnit8TestPlatform.assumeTimeUnitOf()
    assertSame(NANOSECONDS, TimeUnit8TestPlatform.timeUnitOf(ChronoUnit.NANOS))
    assertSame(
      MICROSECONDS,
      TimeUnit8TestPlatform.timeUnitOf(ChronoUnit.MICROS)
    )
    assertSame(
      MILLISECONDS,
      TimeUnit8TestPlatform.timeUnitOf(ChronoUnit.MILLIS)
    )
    assertSame(SECONDS, TimeUnit8TestPlatform.timeUnitOf(ChronoUnit.SECONDS))
    assertSame(MINUTES, TimeUnit8TestPlatform.timeUnitOf(ChronoUnit.MINUTES))
    assertSame(HOURS, TimeUnit8TestPlatform.timeUnitOf(ChronoUnit.HOURS))
    assertSame(DAYS, TimeUnit8TestPlatform.timeUnitOf(ChronoUnit.DAYS))

    assertThrows(
      classOf[NullPointerException],
      TimeUnit8TestPlatform.timeUnitOf(null)
    )

    ChronoUnit.values().foreach { cu =>
      try {
        val tu = TimeUnit8TestPlatform.timeUnitOf(cu)
        assertSame(cu, TimeUnit8TestPlatform.toChronoUnit(tu))
      } catch {
        case _: IllegalArgumentException => ()
      }
    }
  }

  @Test def testConvertDuration_roundtripDurationOf(): Unit = {
    TimeUnit8TestPlatform.assumeConvertDuration()

    var n = ThreadLocalRandom.current().nextLong()

    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(NANOSECONDS, Duration.ofNanos(n))
    )
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(
        NANOSECONDS,
        Duration.of(n, ChronoUnit.NANOS)
      )
    )
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(MILLISECONDS, Duration.ofMillis(n))
    )
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(
        MILLISECONDS,
        Duration.of(n, ChronoUnit.MILLIS)
      )
    )
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(SECONDS, Duration.ofSeconds(n))
    )
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(
        SECONDS,
        Duration.of(n, ChronoUnit.SECONDS)
      )
    )
    n /= 60L
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(MINUTES, Duration.ofMinutes(n))
    )
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(
        MINUTES,
        Duration.of(n, ChronoUnit.MINUTES)
      )
    )
    n /= 60L
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(HOURS, Duration.ofHours(n))
    )
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(
        HOURS,
        Duration.of(n, ChronoUnit.HOURS)
      )
    )
    n /= 24L
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(DAYS, Duration.ofDays(n))
    )
    assertEquals(
      n,
      TimeUnit8TestPlatform.convertDuration(
        DAYS,
        Duration.of(n, ChronoUnit.DAYS)
      )
    )
  }

  @Test def testConvertDuration_roundtripDurationOfNanos(): Unit = {
    TimeUnit8TestPlatform.assumeConvertDuration()

    val unitNanos = TimeUnit.values().map(_.toNanos(1L))
    val baseValues = unitNanos ++ Array(Long.MaxValue, Long.MinValue)
    for {
      n0 <- baseValues
      n1 <- Array(n0, n0 + 1L, n0 - 1L)
      n2 <- Array(n1, n1 + 1000000000L, n1 - 1000000000L)
      n <- Array(n2, -n2)
      u <- TimeUnit.values()
    } {
      assertEquals(
        u.convert(n, NANOSECONDS),
        TimeUnit8TestPlatform.convertDuration(u, Duration.ofNanos(n))
      )
    }
  }

  @Test def testConvertDuration_nearOverflow(): Unit = {
    TimeUnit8TestPlatform.assumeConvertDuration()
    TimeUnit8TestPlatform.assumeToChronoUnit()

    val nanos = ChronoUnit.NANOS
    val maxDuration = Duration.ofSeconds(Long.MaxValue, 999999999L)
    val minDuration = Duration.ofSeconds(Long.MinValue, 0L)

    TimeUnit.values().foreach { u =>
      val cu = TimeUnit8TestPlatform.toChronoUnit(u)
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
          assertEquals(
            Long.MaxValue,
            TimeUnit8TestPlatform.convertDuration(u, max)
          )
          assertEquals(
            Long.MaxValue - 1L,
            TimeUnit8TestPlatform.convertDuration(u, max.minus(1L, nanos))
          )
          assertEquals(
            Long.MaxValue - 1L,
            TimeUnit8TestPlatform.convertDuration(u, max.minus(1L, cu))
          )
          assertEquals(
            Long.MinValue,
            TimeUnit8TestPlatform.convertDuration(u, min)
          )
          assertEquals(
            Long.MinValue + 1L,
            TimeUnit8TestPlatform.convertDuration(u, min.plus(1L, nanos))
          )
          assertEquals(
            Long.MinValue + 1L,
            TimeUnit8TestPlatform.convertDuration(u, min.plus(1L, cu))
          )
          assertEquals(
            Long.MaxValue,
            TimeUnit8TestPlatform.convertDuration(u, max.plus(1L, nanos))
          )
          if (u != SECONDS) {
            assertEquals(
              Long.MaxValue,
              TimeUnit8TestPlatform.convertDuration(u, max.plus(1L, cu))
            )
            assertEquals(
              Long.MinValue,
              TimeUnit8TestPlatform.convertDuration(u, min.minus(1L, nanos))
            )
            assertEquals(
              Long.MinValue,
              TimeUnit8TestPlatform.convertDuration(u, min.minus(1L, cu))
            )
          }
          1L
        }

      assertEquals(
        Long.MaxValue / r,
        TimeUnit8TestPlatform.convertDuration(u, maxDuration)
      )
      assertEquals(
        Long.MinValue / r,
        TimeUnit8TestPlatform.convertDuration(u, minDuration)
      )
    }
  }
}
