package org.scalanative.testsuite.javalib.nio.file.attribute

import java.nio.file.attribute._

import java.time.Instant
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.Assert._

class FileTimeExtTest {
  private val timestampNanos = 1582230020123456789L
  private val timestampMillis = timestampNanos / 1e6.toLong
  private val timestampSeconds = timestampMillis / 1000L
  private val nanosAdjustment = 123456789L

  @Test def fromInstant(): Unit = {
    def fileTimeFromInstantEquals(ft: FileTime, instant: Instant)(
        extFn: FileTime => Unit = _ => ()
    ) = {
      val fromInstant = FileTime.from(instant)
      assertEquals(0, ft.compareTo(fromInstant))
      assertEquals(ft, fromInstant)
      assertEquals(timestampSeconds, fromInstant.to(TimeUnit.SECONDS))
      extFn(fromInstant)
    }

    fileTimeFromInstantEquals(
      FileTime.from(timestampNanos, TimeUnit.NANOSECONDS),
      Instant.ofEpochSecond(timestampSeconds, nanosAdjustment)
    ) { fromInstant =>
      assertEquals(timestampMillis, fromInstant.toMillis)
      assertEquals(timestampNanos, fromInstant.to(TimeUnit.NANOSECONDS))
    }

    fileTimeFromInstantEquals(
      FileTime.fromMillis(timestampMillis),
      Instant.ofEpochMilli(timestampMillis)
    ) { fromInstant =>
      assertEquals(timestampMillis, fromInstant.toMillis)
    }

    fileTimeFromInstantEquals(
      FileTime.from(timestampSeconds, TimeUnit.SECONDS),
      Instant.ofEpochSecond(timestampSeconds)
    )()
  }

  @Test def toInstant(): Unit = {
    val fromNanos = FileTime.from(timestampNanos, TimeUnit.NANOSECONDS)
    val toInstant = fromNanos.toInstant
    val instantFromNanos =
      Instant.ofEpochSecond(timestampSeconds, nanosAdjustment)

    assertEquals(timestampMillis, fromNanos.toMillis)
    assertEquals(timestampMillis, toInstant.toEpochMilli)
    assertEquals(timestampMillis, instantFromNanos.toEpochMilli)
    assertEquals(0, toInstant.compareTo(instantFromNanos))
    assertEquals(nanosAdjustment, instantFromNanos.getNano)
    assertEquals(timestampSeconds, instantFromNanos.getEpochSecond)
  }

  @Test def hasRangeMatchingAtLeastInstant(): Unit = {
    val fromInstantMax = FileTime.from(Instant.MAX)
    assertEquals(
      Instant.MAX.getEpochSecond,
      fromInstantMax.to(TimeUnit.SECONDS)
    )
    assertEquals(Instant.MAX.getNano, fromInstantMax.toInstant.getNano)

    val fromInstantMin = FileTime.from(Instant.MIN)
    assertEquals(
      Instant.MIN.getEpochSecond,
      fromInstantMin.to(TimeUnit.SECONDS)
    )
    assertEquals(Instant.MIN.getNano, fromInstantMin.toInstant.getNano)
  }

  @Test def toInstantWhenLargerThanInstantRange(): Unit = {
    assert(
      Instant.MAX.getEpochSecond != Long.MaxValue,
      "Instant.MAX seconds != Long.MaxValue"
    )
    assert(
      Instant.MIN.getEpochSecond != Long.MinValue,
      "Instant.MIN seconds != Long.MinValue"
    )

    assertEquals(
      Instant.MAX,
      FileTime
        .from(Instant.MAX.getEpochSecond + 1L, TimeUnit.SECONDS)
        .toInstant
    )

    assertEquals(
      Instant.MIN,
      FileTime
        .from(Instant.MIN.getEpochSecond - 1L, TimeUnit.SECONDS)
        .toInstant
    )

    val almostInstantMax = Instant.ofEpochSecond(Instant.MAX.getEpochSecond - 1)
    val almostInstantMin = Instant.ofEpochSecond(Instant.MIN.getEpochSecond + 1)

    assertEquals(almostInstantMax, FileTime.from(almostInstantMax).toInstant)
    assertEquals(almostInstantMin, FileTime.from(almostInstantMin).toInstant)
  }
}
