// Ported from Scala.js

package org.scalanative.testsuite.javalib.util.concurrent

import java.util._
import java.util.concurrent._

import org.junit.Assert._
import org.junit.Test

class TimeUnitTest {

  @Test def toNanos(): Unit = {
    assertTrue(42L == TimeUnit.NANOSECONDS.toNanos(42L))
    assertTrue(42000L == TimeUnit.MICROSECONDS.toNanos(42L))
    assertTrue(42000000L == TimeUnit.MILLISECONDS.toNanos(42L))
    assertTrue(42000000000L == TimeUnit.SECONDS.toNanos(42L))
    assertTrue(2520000000000L == TimeUnit.MINUTES.toNanos(42L))
    assertTrue(151200000000000L == TimeUnit.HOURS.toNanos(42L))
    assertTrue(3628800000000000L == TimeUnit.DAYS.toNanos(42L))
  }

  @Test def toMicros(): Unit = {
    assertTrue(0L == TimeUnit.NANOSECONDS.toMicros(42L))
    assertTrue(42L == TimeUnit.NANOSECONDS.toMicros(42123L))
    assertTrue(42L == TimeUnit.MICROSECONDS.toMicros(42L))
    assertTrue(42000L == TimeUnit.MILLISECONDS.toMicros(42L))
    assertTrue(42000000L == TimeUnit.SECONDS.toMicros(42L))
    assertTrue(2520000000L == TimeUnit.MINUTES.toMicros(42L))
    assertTrue(151200000000L == TimeUnit.HOURS.toMicros(42L))
    assertTrue(3628800000000L == TimeUnit.DAYS.toMicros(42L))
  }

  @Test def toMillis(): Unit = {
    assertTrue(0L == TimeUnit.NANOSECONDS.toMillis(42L))
    assertTrue(42L == TimeUnit.NANOSECONDS.toMillis(42000123L))
    assertTrue(0L == TimeUnit.MICROSECONDS.toMillis(42L))
    assertTrue(42L == TimeUnit.MICROSECONDS.toMillis(42123L))
    assertTrue(42L == TimeUnit.MILLISECONDS.toMillis(42L))
    assertTrue(42000L == TimeUnit.SECONDS.toMillis(42L))
    assertTrue(2520000L == TimeUnit.MINUTES.toMillis(42L))
    assertTrue(151200000L == TimeUnit.HOURS.toMillis(42L))
    assertTrue(3628800000L == TimeUnit.DAYS.toMillis(42L))
  }

  @Test def toSeconds(): Unit = {
    assertTrue(0L == TimeUnit.NANOSECONDS.toSeconds(42L))
    assertTrue(42L == TimeUnit.NANOSECONDS.toSeconds(42000000123L))
    assertTrue(0L == TimeUnit.MICROSECONDS.toSeconds(42L))
    assertTrue(42L == TimeUnit.MICROSECONDS.toSeconds(42000123L))
    assertTrue(0L == TimeUnit.MILLISECONDS.toSeconds(42L))
    assertTrue(42L == TimeUnit.MILLISECONDS.toSeconds(42123L))
    assertTrue(42L == TimeUnit.SECONDS.toSeconds(42L))
    assertTrue(2520L == TimeUnit.MINUTES.toSeconds(42L))
    assertTrue(151200L == TimeUnit.HOURS.toSeconds(42L))
    assertTrue(3628800L == TimeUnit.DAYS.toSeconds(42L))
  }

  @Test def toMinutes(): Unit = {
    assertTrue(0L == TimeUnit.NANOSECONDS.toMinutes(42L))
    assertTrue(42L == TimeUnit.NANOSECONDS.toMinutes(2520000007380L))
    assertTrue(0L == TimeUnit.MICROSECONDS.toMinutes(42L))
    assertTrue(42L == TimeUnit.MICROSECONDS.toMinutes(2520007380L))
    assertTrue(0L == TimeUnit.MILLISECONDS.toMinutes(42L))
    assertTrue(42L == TimeUnit.MILLISECONDS.toMinutes(2520738L))
    assertTrue(0L == TimeUnit.SECONDS.toMinutes(42L))
    assertTrue(42L == TimeUnit.SECONDS.toMinutes(2520L))
    assertTrue(42L == TimeUnit.MINUTES.toMinutes(42L))
    assertTrue(2520L == TimeUnit.HOURS.toMinutes(42L))
    assertTrue(60480L == TimeUnit.DAYS.toMinutes(42L))
  }

  @Test def toHours(): Unit = {
    assertTrue(0L == TimeUnit.NANOSECONDS.toHours(42L))
    assertTrue(42L == TimeUnit.NANOSECONDS.toHours(151200000442800L))
    assertTrue(0L == TimeUnit.MICROSECONDS.toHours(42L))
    assertTrue(42L == TimeUnit.MICROSECONDS.toHours(151200442800L))
    assertTrue(0L == TimeUnit.MILLISECONDS.toHours(42L))
    assertTrue(42L == TimeUnit.MILLISECONDS.toHours(151244280L))
    assertTrue(0L == TimeUnit.SECONDS.toHours(42L))
    assertTrue(42L == TimeUnit.SECONDS.toHours(151200L))
    assertTrue(0L == TimeUnit.MINUTES.toHours(42L))
    assertTrue(42L == TimeUnit.MINUTES.toHours(2520L))
    assertTrue(42L == TimeUnit.HOURS.toHours(42L))
    assertTrue(1008L == TimeUnit.DAYS.toHours(42L))
  }

  @Test def toDays(): Unit = {
    assertTrue(0L == TimeUnit.NANOSECONDS.toDays(42L))
    assertTrue(42L == TimeUnit.NANOSECONDS.toDays(3628800010627200L))
    assertTrue(0L == TimeUnit.MICROSECONDS.toDays(42L))
    assertTrue(42L == TimeUnit.MICROSECONDS.toDays(3628810627200L))
    assertTrue(0L == TimeUnit.MILLISECONDS.toDays(42L))
    assertTrue(42L == TimeUnit.MILLISECONDS.toDays(3629862720L))
    assertTrue(0L == TimeUnit.SECONDS.toDays(42L))
    assertTrue(42L == TimeUnit.SECONDS.toDays(3628800L))
    assertTrue(0L == TimeUnit.MINUTES.toDays(42L))
    assertTrue(42L == TimeUnit.MINUTES.toDays(60480L))
    assertTrue(1L == TimeUnit.HOURS.toDays(42L))
    assertTrue(42L == TimeUnit.HOURS.toDays(1008L))
    assertTrue(42L == TimeUnit.DAYS.toDays(42L))
  }

  @Test def values(): Unit = {
    val values = TimeUnit.values()

    assertTrue(7 == values.length)
    assertTrue(TimeUnit.NANOSECONDS == values(0))
    assertTrue(TimeUnit.MICROSECONDS == values(1))
    assertTrue(TimeUnit.MILLISECONDS == values(2))
    assertTrue(TimeUnit.SECONDS == values(3))
    assertTrue(TimeUnit.MINUTES == values(4))
    assertTrue(TimeUnit.HOURS == values(5))
    assertTrue(TimeUnit.DAYS == values(6))
  }

  @Test def valueOf(): Unit = {
    assertTrue(TimeUnit.NANOSECONDS == TimeUnit.valueOf("NANOSECONDS"))
    assertTrue(TimeUnit.MICROSECONDS == TimeUnit.valueOf("MICROSECONDS"))
    assertTrue(TimeUnit.MILLISECONDS == TimeUnit.valueOf("MILLISECONDS"))
    assertTrue(TimeUnit.SECONDS == TimeUnit.valueOf("SECONDS"))
    assertTrue(TimeUnit.MINUTES == TimeUnit.valueOf("MINUTES"))
    assertTrue(TimeUnit.HOURS == TimeUnit.valueOf("HOURS"))
    assertTrue(TimeUnit.DAYS == TimeUnit.valueOf("DAYS"))
  }
}
