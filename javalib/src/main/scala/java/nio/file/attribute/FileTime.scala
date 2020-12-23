package java.nio.file.attribute

import java.util.concurrent.TimeUnit
import java.time.Instant

final class FileTime private (private val epochMillis: Long,
                              private val nanos: Int)
    extends Comparable[FileTime] {

  def compareTo(other: FileTime) = {
    val compareMillis = epochMillis.compareTo(other.epochMillis)
    if (compareMillis == 0) {
      nanos.compareTo(other.nanos)
    } else compareMillis
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case other: FileTime => compareTo(other) == 0
      case _               => false
    }

  override def hashCode(): Int =
    epochMillis.## + nanos.##

  def to(unit: TimeUnit): Long =
    unit.convert(toMillis(), TimeUnit.MILLISECONDS)

  def toMillis(): Long = epochMillis

  def toInstant(): Instant = Instant.ofEpochMilli(epochMillis).plusNanos(nanos)

  override def toString(): String = s"FileTime($epochMillis, $nanos)"
}

object FileTime {
  def from(value: Long, unit: TimeUnit): FileTime =
    fromMillis(TimeUnit.MILLISECONDS.convert(value, unit))

  def fromMillis(value: Long): FileTime =
    new FileTime(value, 0)

  def from(instant: Instant): FileTime =
    new FileTime(instant.toEpochMilli, instant.getNano)
}
