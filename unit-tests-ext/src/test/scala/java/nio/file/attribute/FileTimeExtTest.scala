package java.nio.file.attribute

import java.time.Instant
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.Assert._

class FileTimeExtTest {
  private val timestamp          = 1582230020000L
  private val timestampAsSeconds = timestamp / 1000L
  private val timestampAsNanos   = timestamp * 1e6.toLong
  private val nanosAdjustment    = 1234L

  @Test def fromInstant(): Unit = {
    def fileTimeFromInstantEquals(ft: FileTime, instant: Instant) = {
      assertEquals(timestamp, instant.toEpochMilli)
      assertEquals(timestamp, ft.toMillis)

      val fromInstant = FileTime.from(instant)
      assertEquals(0, ft.compareTo(fromInstant))
      assertEquals(timestamp, fromInstant.toMillis)
      assertEquals(timestampAsSeconds, fromInstant.to(TimeUnit.SECONDS))
      assertEquals(ft, fromInstant)
    }

    fileTimeFromInstantEquals(FileTime.fromMillis(timestamp),
                              Instant.ofEpochMilli(timestamp))

    fileTimeFromInstantEquals(
      FileTime.from(timestampAsSeconds, TimeUnit.SECONDS),
      Instant.ofEpochSecond(timestampAsSeconds))

    fileTimeFromInstantEquals(
      FileTime.from(timestampAsNanos + nanosAdjustment, TimeUnit.NANOSECONDS),
      Instant.ofEpochSecond(timestampAsSeconds, nanosAdjustment))
  }

  @Test def fromInstantMaxPrecision(): Unit = {
    val maxSecondsWithNanos  = Long.MaxValue / 1e9.toLong - 1L
    val naxSecondsWithMicros = Long.MaxValue / 1e6.toLong - 1L
    val maxSecondsWithMillis = Long.MaxValue / 1e3.toLong - 1L

    def toUnit(seconds: Long, nanos: Long, unit: TimeUnit) = {
      val fromSeconds = unit.convert(seconds, TimeUnit.SECONDS)
      val fromNanos   = unit.convert(nanos, TimeUnit.NANOSECONDS)
      fromSeconds + fromNanos
    }

    def fromSeconds(seconds: Long, unit: TimeUnit) =
      FileTime.from(toUnit(seconds, Instant.MAX.getNano, unit), unit)

    def testPrecision(seconds: Long,
                      expectedPrecision: TimeUnit,
                      lowerPrecision: TimeUnit) = {
      val overflowSeconds = seconds + 1L

      val fileTimeMax = FileTime.from(
        Instant.ofEpochSecond(seconds, Instant.MAX.getNano)
      )
      val fileTimeOverflow = FileTime.from(
        Instant.ofEpochSecond(overflowSeconds, Instant.MAX.getNano)
      )

      val expectedMax   = fromSeconds(seconds, expectedPrecision)
      val overflowValue = fromSeconds(overflowSeconds, lowerPrecision)

      assertEquals(expectedMax, fileTimeMax)
      assertEquals(overflowValue, fileTimeOverflow)

      assertNotEquals(Long.MaxValue, fileTimeMax.to(expectedPrecision))
      if (expectedPrecision == TimeUnit.MILLISECONDS) {
        // in this case it does not overflow yet (it's exactly Long.MaxValue - 807L), but any greater precision would cause overflow
        assertNotEquals(Long.MaxValue,
                        fileTimeOverflow.to(TimeUnit.MILLISECONDS))
        assertEquals(Long.MaxValue, fileTimeOverflow.to(TimeUnit.MICROSECONDS))
      } else {
        assertEquals(Long.MaxValue, fileTimeOverflow.to(expectedPrecision))
      }
    }

    testPrecision(maxSecondsWithNanos,
                  TimeUnit.NANOSECONDS,
                  TimeUnit.MICROSECONDS)

    testPrecision(naxSecondsWithMicros,
                  TimeUnit.MICROSECONDS,
                  TimeUnit.MILLISECONDS)

    testPrecision(maxSecondsWithMillis, TimeUnit.MILLISECONDS, TimeUnit.SECONDS)

  }
  @Test def toInstant(): Unit = {
    val fromNanos =
      FileTime.from(timestampAsNanos + nanosAdjustment, TimeUnit.NANOSECONDS)
    val toInstant = fromNanos.toInstant
    val instantFromNanos =
      Instant.ofEpochSecond(timestampAsSeconds, nanosAdjustment)

    assertEquals(timestamp, fromNanos.toMillis)
    assertEquals(timestamp, toInstant.toEpochMilli)
    assertEquals(timestamp, instantFromNanos.toEpochMilli)
    assertEquals(0, toInstant.compareTo(instantFromNanos))
    assertEquals(nanosAdjustment, instantFromNanos.getNano)
    assertEquals(timestampAsSeconds, instantFromNanos.getEpochSecond)
  }
}
