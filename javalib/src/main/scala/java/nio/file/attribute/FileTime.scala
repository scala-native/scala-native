package java.nio.file.attribute

import java.time.Instant
import java.util.concurrent.TimeUnit

import scalanative.posix.{time, timeOps}
import scalanative.unsafe._

import timeOps.timespecOps

final class FileTime private (
    private val epochDays: Long,
    private val dayNanos: Long
) extends Comparable[FileTime] {
  import FileTime._

  assert(dayNanos <= FileTime.NanosInDay)

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

  /* From JDK 8 API
   *
   * Conversion from a coarser granularity that would numerically overflow
   * saturate to Long.MIN_VALUE if negative or Long.MAX_VALUE if positive.
   */
  def to(unit: TimeUnit): Long = {
    val fromDays = unit.convert(epochDays, TimeUnit.DAYS)
    val fromNanos = unit.convert(dayNanos, TimeUnit.NANOSECONDS)

    // TimeUnit conversion returns -Long.MaxValue in case of negative overflow instead of Long.MinValue
    val fromDaysOverflow =
      fromDays == Long.MaxValue || fromDays <= -Long.MaxValue

    if (fromDaysOverflow) fromDays
    else {
      try {
        Math.addExact(fromDays, fromNanos)
      } catch {
        case _: ArithmeticException =>
          if (fromDays > 0) Long.MaxValue
          else Long.MinValue
      }
    }
  }

  def toMillis(): Long = to(TimeUnit.MILLISECONDS)

  /* From JDK 8 API
   *
   * FileTime can store points on the time-line further in the future and further in the past than Instant.
   * Conversion from such further time points saturates to Instant.MIN if earlier than Instant.MIN
   * or Instant.MAX if later than Instant.MAX.
   */
  def toInstant(): Instant = {
    val seconds = to(TimeUnit.SECONDS)
    if (seconds < Instant.MIN.getEpochSecond) Instant.MIN
    else if (seconds > Instant.MAX.getEpochSecond) Instant.MAX
    else {
      Instant.ofEpochSecond(seconds, Math.floorMod(dayNanos, NanosToSecond))
    }
  }

  override def toString(): String = s"FileTime($epochDays, $dayNanos)"

  // Fill and return the provided timespec
  private[attribute] def toTimespec(
      tsPtr: Ptr[time.timespec]
  ): Ptr[time.timespec] = {
    tsPtr.tv_sec = to(TimeUnit.SECONDS).toSize
    tsPtr.tv_nsec = Math.floorMod(dayNanos, NanosToSecond).toSize

    tsPtr
  }
}

object FileTime {
  private final val SecondsInDay = 86400L
  private final val NanosInDay = 86400000000000L
  private final val NanosToSecond = 1000000000L

  def from(value: Long, unit: TimeUnit): FileTime = {
    val div = unit.convert(NanosInDay, TimeUnit.NANOSECONDS)
    val days = Math.floorDiv(value, div)
    val nanos = unit.toNanos(Math.floorMod(value, div))
    new FileTime(days, nanos)
  }

  def fromMillis(value: Long): FileTime = from(value, TimeUnit.MILLISECONDS)

  def from(instant: Instant): FileTime = {
    val s = instant.getEpochSecond
    val days = Math.floorDiv(s, SecondsInDay)
    val daySeconds = Math.floorMod(s, SecondsInDay)
    val dayNanos = (daySeconds * NanosToSecond) + instant.getNano
    new FileTime(days, dayNanos)
  }

  // Pre-condition: POSIX timespec st_mtim.tv_nsec guarantees nanos < 1 second
  private[attribute] def from(
      seconds: scala.Long,
      nanos: scala.Long
  ): FileTime = {
    /* Math is similar to that in method 'from(instant)' above.
     * Open code to preserve full range of File time and
     * avoid SN java.time support complexity.
     */
    val days = Math.floorDiv(seconds, SecondsInDay)
    val daySeconds = Math.floorMod(seconds, SecondsInDay)
    val dayNanos = (daySeconds * NanosToSecond) + nanos

    new FileTime(days, dayNanos)
  }
}
