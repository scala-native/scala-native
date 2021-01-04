package java.nio.file.attribute

import java.util.concurrent.TimeUnit
import java.time.Instant

final class FileTime private (private val value: Long,
                              private val unit: TimeUnit)
    extends Comparable[FileTime] {

  def compareTo(other: FileTime) = {
    if (this.unit == other.unit) {
      this.value.compareTo(other.value)
    } else {
      val compareUnit = TimeUnit.NANOSECONDS
      this.to(compareUnit).compareTo(other.to(compareUnit))
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
    Instant.ofEpochSecond(
      seconds,
      asNanos - TimeUnit.NANOSECONDS.convert(seconds, TimeUnit.SECONDS))
  }

  override def toString(): String = s"FileTime($value, $unit)"
}

object FileTime {
  def from(value: Long, unit: TimeUnit): FileTime = new FileTime(value, unit)

  def fromMillis(value: Long): FileTime = from(value, TimeUnit.MILLISECONDS)

  def from(instant: Instant): FileTime = {
    val secondsAsNanos =
      TimeUnit.NANOSECONDS.convert(instant.getEpochSecond, TimeUnit.SECONDS)
    new FileTime(secondsAsNanos + instant.getNano, TimeUnit.NANOSECONDS)
  }
}
