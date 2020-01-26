package java.nio.file.attribute

import java.util.concurrent.TimeUnit
import java.time.Instant

final class FileTime private (instant: Instant) extends Comparable[FileTime] {
  def compareTo(other: FileTime) =
    instant.compareTo(other.toInstant)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: FileTime => instant == other.toInstant
      case _               => false
    }

  override def hashCode(): Int =
    instant.hashCode

  def to(unit: TimeUnit): Long =
    unit.convert(toMillis(), TimeUnit.MILLISECONDS)

  def toMillis(): Long =
    instant.toEpochMilli

  def toInstant(): Instant =
    instant

  override def toString(): String =
    instant.toString
}

object FileTime {
  def from(value: Long, unit: TimeUnit): FileTime =
    fromMillis(TimeUnit.MILLISECONDS.convert(value, unit))
  def fromMillis(value: Long): FileTime =
    from(Instant.ofEpochMilli(value))
  def from(instant: Instant): FileTime =
    new FileTime(instant)
}
