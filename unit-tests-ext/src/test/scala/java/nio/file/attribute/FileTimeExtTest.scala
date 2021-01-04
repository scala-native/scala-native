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
