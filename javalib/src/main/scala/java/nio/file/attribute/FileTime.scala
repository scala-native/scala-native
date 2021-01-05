package java.nio.file.attribute

import java.util.concurrent.TimeUnit
import java.time.Instant

final class FileTime private (private val epochDays: Long,
                              private val dayNanos: Long)
    extends Comparable[FileTime] {
  import java.nio.file.attribute.FileTime.NanosToSecond

  def compareTo(other: FileTime): Int = {
    val daysComp = epochDays.compareTo(other.epochDays)
    if (daysComp == 0) dayNanos.compareTo(other.dayNanos)
    else daysComp
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case other: FileTime => compareTo(other) == 0
      case _               => false
    }

  override def hashCode(): Int =
    epochDays.## + dayNanos.##

  def to(unit: TimeUnit): Long = {
    val fromDays  = unit.convert(epochDays, TimeUnit.DAYS)
    val fromNanos = unit.convert(dayNanos, TimeUnit.NANOSECONDS)
    try {
      Math.addExact(fromDays, fromNanos)
    } catch {
      // In case of overflow return Long.MaxValue if positive or else Long.MinValue
      case _: ArithmeticException =>
        if (fromDays > 0) Long.MaxValue
        else Long.MinValue
    }
  }

  def toMillis(): Long = to(TimeUnit.MILLISECONDS)

  def toInstant(): Instant = {
    val seconds = to(TimeUnit.SECONDS)
    Instant.ofEpochSecond(seconds, Math.floorMod(dayNanos, NanosToSecond))
  }

  override def toString(): String = s"FileTime($epochDays, $dayNanos)"
}

object FileTime {
  private final val SecondsInDay  = 86400L
  private final val NanosInDay    = 86400000000000L
  private final val NanosToSecond = 1000000000L

  def from(value: Long, unit: TimeUnit): FileTime =
    new FileTime(unit.toDays(value),
                 Math.floorMod(unit.toNanos(value), NanosInDay))

  def fromMillis(value: Long): FileTime = from(value, TimeUnit.MILLISECONDS)

  def from(instant: Instant): FileTime = {
    val s          = instant.getEpochSecond
    val daySeconds = Math.floorMod(s, SecondsInDay)
    val days       = TimeUnit.SECONDS.toDays(s)
    val dayNanos   = TimeUnit.SECONDS.toNanos(daySeconds) + instant.getNano
    new FileTime(days, dayNanos)
  }
}
