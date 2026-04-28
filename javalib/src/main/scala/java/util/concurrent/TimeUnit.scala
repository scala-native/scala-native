package java.util.concurrent

import java.time.Duration
import java.time.temporal.ChronoUnit

// Ported from Scala.js

abstract class TimeUnit private (name: String, ordinal: Int)
    extends _Enum[TimeUnit](name, ordinal) {

  def convert(a: Long, u: TimeUnit): Long
  def convert(duration: Duration): Long

  def toNanos(a: Long): Long
  def toMicros(a: Long): Long
  def toMillis(a: Long): Long
  def toSeconds(a: Long): Long
  def toMinutes(a: Long): Long
  def toHours(a: Long): Long
  def toDays(a: Long): Long
  def toChronoUnit(): ChronoUnit

  def sleep(timeout: Long): Unit =
    if (timeout > 0) Thread.sleep(toMillis(timeout))
  def timedJoin(thread: Thread, timeout: Long) =
    if (timeout > 0) thread.join(toMillis(timeout))
  def timedWait(obj: Object, timeout: Long) =
    if (timeout > 0) obj.wait(toMillis(timeout))
}

object TimeUnit {
  final val NANOSECONDS: TimeUnit = new TimeUnit("NANOSECONDS", 0) {
    def convert(a: Long, u: TimeUnit): Long = u.toNanos(a)
    def convert(duration: Duration): Long = convertDuration(duration, this)
    def toNanos(a: Long): Long = a
    def toMicros(a: Long): Long = a / (C1 / C0)
    def toMillis(a: Long): Long = a / (C2 / C0)
    def toSeconds(a: Long): Long = a / (C3 / C0)
    def toMinutes(a: Long): Long = a / (C4 / C0)
    def toHours(a: Long): Long = a / (C5 / C0)
    def toDays(a: Long): Long = a / (C6 / C0)
    def toChronoUnit(): ChronoUnit = ChronoUnit.NANOS
  }

  final val MICROSECONDS: TimeUnit = new TimeUnit("MICROSECONDS", 1) {
    def convert(a: Long, u: TimeUnit): Long = u.toMicros(a)
    def convert(duration: Duration): Long = convertDuration(duration, this)
    def toNanos(a: Long): Long = x(a, C1 / C0, MAX / (C1 / C0))
    def toMicros(a: Long): Long = a
    def toMillis(a: Long): Long = a / (C2 / C1)
    def toSeconds(a: Long): Long = a / (C3 / C1)
    def toMinutes(a: Long): Long = a / (C4 / C1)
    def toHours(a: Long): Long = a / (C5 / C1)
    def toDays(a: Long): Long = a / (C6 / C1)
    def toChronoUnit(): ChronoUnit = ChronoUnit.MICROS
  }

  final val MILLISECONDS: TimeUnit = new TimeUnit("MILLISECONDS", 2) {
    def convert(a: Long, u: TimeUnit): Long = u.toMillis(a)
    def convert(duration: Duration): Long = convertDuration(duration, this)
    def toNanos(a: Long): Long = x(a, C2 / C0, MAX / (C2 / C0))
    def toMicros(a: Long): Long = x(a, C2 / C1, MAX / (C2 / C1))
    def toMillis(a: Long): Long = a
    def toSeconds(a: Long): Long = a / (C3 / C2)
    def toMinutes(a: Long): Long = a / (C4 / C2)
    def toHours(a: Long): Long = a / (C5 / C2)
    def toDays(a: Long): Long = a / (C6 / C2)
    def toChronoUnit(): ChronoUnit = ChronoUnit.MILLIS
  }

  final val SECONDS: TimeUnit = new TimeUnit("SECONDS", 3) {
    def convert(a: Long, u: TimeUnit): Long = u.toSeconds(a)
    def convert(duration: Duration): Long = convertDuration(duration, this)
    def toNanos(a: Long): Long = x(a, C3 / C0, MAX / (C3 / C0))
    def toMicros(a: Long): Long = x(a, C3 / C1, MAX / (C3 / C1))
    def toMillis(a: Long): Long = x(a, C3 / C2, MAX / (C3 / C2))
    def toSeconds(a: Long): Long = a
    def toMinutes(a: Long): Long = a / (C4 / C3)
    def toHours(a: Long): Long = a / (C5 / C3)
    def toDays(a: Long): Long = a / (C6 / C3)
    def toChronoUnit(): ChronoUnit = ChronoUnit.SECONDS
  }

  final val MINUTES: TimeUnit = new TimeUnit("MINUTES", 4) {
    def convert(a: Long, u: TimeUnit): Long = u.toMinutes(a)
    def convert(duration: Duration): Long = convertDuration(duration, this)
    def toNanos(a: Long): Long = x(a, C4 / C0, MAX / (C4 / C0))
    def toMicros(a: Long): Long = x(a, C4 / C1, MAX / (C4 / C1))
    def toMillis(a: Long): Long = x(a, C4 / C2, MAX / (C4 / C2))
    def toSeconds(a: Long): Long = x(a, C4 / C3, MAX / (C4 / C3))
    def toMinutes(a: Long): Long = a
    def toHours(a: Long): Long = a / (C5 / C4)
    def toDays(a: Long): Long = a / (C6 / C4)
    def toChronoUnit(): ChronoUnit = ChronoUnit.MINUTES
  }

  final val HOURS: TimeUnit = new TimeUnit("HOURS", 5) {
    def convert(a: Long, u: TimeUnit): Long = u.toHours(a)
    def convert(duration: Duration): Long = convertDuration(duration, this)
    def toNanos(a: Long): Long = x(a, C5 / C0, MAX / (C5 / C0))
    def toMicros(a: Long): Long = x(a, C5 / C1, MAX / (C5 / C1))
    def toMillis(a: Long): Long = x(a, C5 / C2, MAX / (C5 / C2))
    def toSeconds(a: Long): Long = x(a, C5 / C3, MAX / (C5 / C3))
    def toMinutes(a: Long): Long = x(a, C5 / C4, MAX / (C5 / C4))
    def toHours(a: Long): Long = a
    def toDays(a: Long): Long = a / (C6 / C5)
    def toChronoUnit(): ChronoUnit = ChronoUnit.HOURS
  }

  final val DAYS: TimeUnit = new TimeUnit("DAYS", 6) {
    def convert(a: Long, u: TimeUnit): Long = u.toDays(a)
    def convert(duration: Duration): Long = convertDuration(duration, this)
    def toNanos(a: Long): Long = x(a, C6 / C0, MAX / (C6 / C0))
    def toMicros(a: Long): Long = x(a, C6 / C1, MAX / (C6 / C1))
    def toMillis(a: Long): Long = x(a, C6 / C2, MAX / (C6 / C2))
    def toSeconds(a: Long): Long = x(a, C6 / C3, MAX / (C6 / C3))
    def toMinutes(a: Long): Long = x(a, C6 / C4, MAX / (C6 / C4))
    def toHours(a: Long): Long = x(a, C6 / C5, MAX / (C6 / C5))
    def toDays(a: Long): Long = a
    def toChronoUnit(): ChronoUnit = ChronoUnit.DAYS
  }

  private val _values: Array[TimeUnit] =
    Array(
      NANOSECONDS,
      MICROSECONDS,
      MILLISECONDS,
      SECONDS,
      MINUTES,
      HOURS,
      DAYS
    )

  // deliberately without type ascription to make them compile-time constants
  private final val C0 = 1L
  private final val C1 = C0 * 1000L
  private final val C2 = C1 * 1000L
  private final val C3 = C2 * 1000L
  private final val C4 = C3 * 60L
  private final val C5 = C4 * 60L
  private final val C6 = C5 * 24L
  private final val MAX = Long.MaxValue

  def values(): Array[TimeUnit] = _values.clone()

  def valueOf(name: String): TimeUnit = {
    _values.find(_.name() == name).getOrElse {
      throw new IllegalArgumentException("No enum const TimeUnit." + name)
    }
  }

  def of(chronoUnit: ChronoUnit): TimeUnit =
    chronoUnit match {
      case null                 => throw new NullPointerException()
      case ChronoUnit.NANOS     => NANOSECONDS
      case ChronoUnit.MICROS    => MICROSECONDS
      case ChronoUnit.MILLIS    => MILLISECONDS
      case ChronoUnit.SECONDS   => SECONDS
      case ChronoUnit.MINUTES   => MINUTES
      case ChronoUnit.HOURS     => HOURS
      case ChronoUnit.DAYS      => DAYS
      case unsupported: AnyRef =>
        throw new IllegalArgumentException("No TimeUnit equivalent for " + unsupported)
    }

  private def x(a: Long, b: Long, max: Long): Long = {
    if (a > max) MAX
    else if (a < -max) Long.MinValue
    else a * b
  }

  private def convertDuration(duration: Duration, unit: TimeUnit): Long = {
    if (duration == null) throw new NullPointerException()
    var seconds = duration.getSeconds()
    var nanos = duration.getNano()
    if (seconds < 0 && nanos > 0) {
      seconds += 1L
      nanos = nanos - C3.toInt
    }

    val convertedSeconds = unit.convert(seconds, SECONDS)
    if (convertedSeconds == Long.MinValue || convertedSeconds == Long.MaxValue)
      convertedSeconds
    else saturatingAdd(convertedSeconds, unit.convert(nanos.toLong, NANOSECONDS))
  }

  private def saturatingAdd(a: Long, b: Long): Long = {
    if (b > 0L && a > Long.MaxValue - b) Long.MaxValue
    else if (b < 0L && a < Long.MinValue - b) Long.MinValue
    else a + b
  }
}
