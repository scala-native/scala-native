package java.nio.file.attribute

import java.util.concurrent.TimeUnit
import java.time.Instant

final class FileTime private (private val value: Long,
                              private val unit: TimeUnit)
    extends Comparable[FileTime] {
  import java.nio.file.attribute.FileTime.NanosToSecond

  @inline
  private def compareTo(other: FileTime, unit: TimeUnit) =
    this.to(unit).compareTo(other.to(unit))

  def compareTo(other: FileTime): Int = {
    if (this.unit == other.unit) {
      this.value.compareTo(other.value)
    } else {
      val daysComp = compareTo(other, TimeUnit.DAYS)
      if (daysComp != 0) daysComp
      else compareTo(other, TimeUnit.NANOSECONDS)
    }
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case other: FileTime => compareTo(other) == 0
      case _               => false
    }

  override def hashCode(): Int =
    value.## + unit.##

  def to(unit: TimeUnit): Long = unit.convert(value, this.unit)

  def toMillis(): Long = to(TimeUnit.MILLISECONDS)

  def toInstant(): Instant = {
    val asNanos = to(TimeUnit.NANOSECONDS)
    val seconds = to(TimeUnit.SECONDS)
    Instant.ofEpochSecond(seconds, Math.floorMod(asNanos, NanosToSecond))
  }

  override def toString(): String = s"FileTime($value, $unit)"
}

object FileTime {
  private final val NanosToSecond        = 1000000000L
  private final val MicrosToSecond       = 1000000L
  private final val MillisToSecond       = 1000L
  private final val MaxSecondsWithMillis = Long.MaxValue / MillisToSecond - 1L
  private final val MaxSecondsWithMicros = Long.MaxValue / MicrosToSecond - 1L
  private final val MaxSecondsWithNanos  = Long.MaxValue / NanosToSecond - 1L

  def from(value: Long, unit: TimeUnit): FileTime = new FileTime(value, unit)

  def fromMillis(value: Long): FileTime = from(value, TimeUnit.MILLISECONDS)

  def from(instant: Instant): FileTime = {
    val s = instant.getEpochSecond

    /* Direct conversion of instant seconds to nanoseconds with their nanos adjustment might cause overflow.
     * To handle such cases precision of TimeFile is limited for large values
     */
    def withPrecision(unit: TimeUnit) = {
      val adjustment  = unit.convert(instant.getNano, TimeUnit.NANOSECONDS)
      val fromSeconds = unit.convert(s, TimeUnit.SECONDS)
      new FileTime(fromSeconds + adjustment, unit)
    }

    if (s <= MaxSecondsWithNanos) withPrecision(TimeUnit.NANOSECONDS)
    else if (s <= MaxSecondsWithMicros) withPrecision(TimeUnit.MICROSECONDS)
    else if (s <= MaxSecondsWithMillis) withPrecision(TimeUnit.MILLISECONDS)
    else withPrecision(TimeUnit.SECONDS)
  }
}
