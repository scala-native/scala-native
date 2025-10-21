package org.scalanative.testsuite.javalib.nio.file.attribute

import java.nio.file.attribute._
import java.util.concurrent.TimeUnit

import org.junit.Assert._
import org.junit.Test

class FileTimeTest {

  @Test def isComparableAgainstDiffTimeUnits(): Unit = {
    val timestamp = 1582230020000L
    val timestampSeconds = timestamp / 1000L

    val ft1 = FileTime.fromMillis(timestamp)
    val ft2 = FileTime.from(timestamp, TimeUnit.MILLISECONDS)
    val ft3 = FileTime.from(timestampSeconds, TimeUnit.SECONDS)

    val ft4 = FileTime.fromMillis(timestamp + 1)
    val ft5 = FileTime.from(timestampSeconds + 1, TimeUnit.SECONDS)

    assertEquals(0, ft1.compareTo(ft2))
    assertEquals(0, ft1.compareTo(ft3))
    assertEquals(0, ft2.compareTo(ft3))

    assertEquals(ft1, ft2)
    assertEquals(ft1, ft3)
    assertEquals(ft2, ft3)

    assertEquals(-1, ft1.compareTo(ft4))
    assertEquals(1, ft4.compareTo(ft1))
    assertNotEquals(ft1, ft4)

    assertEquals(-1, ft1.compareTo(ft5))
    assertEquals(1, ft5.compareTo(ft1))
    assertNotEquals(ft1, ft5)
  }

  @Test def handlesToTimeUnitConversion(): Unit = {
    val possibleOverflowValue = Long.MaxValue - 10L

    val asDays = FileTime.from(possibleOverflowValue, TimeUnit.DAYS)
    TimeUnit.values().foreach {
      case TimeUnit.DAYS =>
        assertEquals(possibleOverflowValue, asDays.to(TimeUnit.DAYS))
      case unit =>
        assertEquals(Long.MaxValue, asDays.to(unit))
    }

    val asNanos = FileTime.from(possibleOverflowValue, TimeUnit.NANOSECONDS)
    assertEquals(possibleOverflowValue, asNanos.to(TimeUnit.NANOSECONDS))
    assertEquals(9223372036854775L, asNanos.to(TimeUnit.MICROSECONDS))
    assertEquals(9223372036854L, asNanos.to(TimeUnit.MILLISECONDS))
    assertEquals(9223372036L, asNanos.to(TimeUnit.SECONDS))
    assertEquals(153722867L, asNanos.to(TimeUnit.MINUTES))
    assertEquals(2562047L, asNanos.to(TimeUnit.HOURS))
    assertEquals(106751L, asNanos.to(TimeUnit.DAYS))
  }

  @Test def handlesLargeValues(): Unit = {
    def unitIdentityEquals(value: Long, unit: TimeUnit) = {
      assertEquals(s"$unit", value, FileTime.from(value, unit).to(unit))
    }

    TimeUnit
      .values()
      .foreach { unit =>
        unitIdentityEquals(Long.MaxValue - 1, unit)
        unitIdentityEquals(Long.MinValue + 1, unit)
      }

    assertEquals(
      Long.MaxValue - 1,
      FileTime.fromMillis(Long.MaxValue - 1).toMillis
    )
    assertEquals(
      Long.MinValue + 1,
      FileTime.fromMillis(Long.MinValue + 1).toMillis
    )
  }
}
