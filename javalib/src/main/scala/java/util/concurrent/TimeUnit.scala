package java.util.concurrent

// Ported from Scala.js
// Scala Native adds Java 9 and 11 methods.

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Objects
import java.{lang => jl}

abstract class TimeUnit private (name: String, ordinal: Int)
    extends _Enum[TimeUnit](name, ordinal) {

  def convert(a: Long, u: TimeUnit): Long

  def convert(duration: Duration): Long =
    TimeUnit.convertDuration(duration, this)

  def toNanos(a: Long): Long
  def toMicros(a: Long): Long
  def toMillis(a: Long): Long
  def toSeconds(a: Long): Long
  def toMinutes(a: Long): Long
  def toHours(a: Long): Long
  def toDays(a: Long): Long

  def sleep(timeout: Long): Unit =
    if (timeout > 0) Thread.sleep(toMillis(timeout))
  def timedJoin(thread: Thread, timeout: Long) =
    if (timeout > 0) thread.join(toMillis(timeout))
  def timedWait(obj: Object, timeout: Long) =
    if (timeout > 0) obj.wait(toMillis(timeout))

  def toChronoUnit(): ChronoUnit = {

    this match {
      case TimeUnit.NANOSECONDS => ChronoUnit.NANOS

      case TimeUnit.MICROSECONDS => ChronoUnit.MICROS

      case TimeUnit.MILLISECONDS => ChronoUnit.MILLIS

      case TimeUnit.SECONDS => ChronoUnit.SECONDS

      case TimeUnit.MINUTES => ChronoUnit.MINUTES

      case TimeUnit.HOURS => ChronoUnit.HOURS

      case TimeUnit.DAYS => ChronoUnit.DAYS
    }
  }
}

object TimeUnit {
  final val NANOSECONDS: TimeUnit = new TimeUnit("NANOSECONDS", 0) {
    def convert(a: Long, u: TimeUnit): Long = u.toNanos(a)
    def toNanos(a: Long): Long = a
    def toMicros(a: Long): Long = a / (C1 / C0)
    def toMillis(a: Long): Long = a / (C2 / C0)
    def toSeconds(a: Long): Long = a / (C3 / C0)
    def toMinutes(a: Long): Long = a / (C4 / C0)
    def toHours(a: Long): Long = a / (C5 / C0)
    def toDays(a: Long): Long = a / (C6 / C0)
  }

  final val MICROSECONDS: TimeUnit = new TimeUnit("MICROSECONDS", 1) {
    def convert(a: Long, u: TimeUnit): Long = u.toMicros(a)
    def toNanos(a: Long): Long = x(a, C1 / C0, MAX / (C1 / C0))
    def toMicros(a: Long): Long = a
    def toMillis(a: Long): Long = a / (C2 / C1)
    def toSeconds(a: Long): Long = a / (C3 / C1)
    def toMinutes(a: Long): Long = a / (C4 / C1)
    def toHours(a: Long): Long = a / (C5 / C1)
    def toDays(a: Long): Long = a / (C6 / C1)
  }

  final val MILLISECONDS: TimeUnit = new TimeUnit("MILLISECONDS", 2) {
    def convert(a: Long, u: TimeUnit): Long = u.toMillis(a)
    def toNanos(a: Long): Long = x(a, C2 / C0, MAX / (C2 / C0))
    def toMicros(a: Long): Long = x(a, C2 / C1, MAX / (C2 / C1))
    def toMillis(a: Long): Long = a
    def toSeconds(a: Long): Long = a / (C3 / C2)
    def toMinutes(a: Long): Long = a / (C4 / C2)
    def toHours(a: Long): Long = a / (C5 / C2)
    def toDays(a: Long): Long = a / (C6 / C2)
  }

  final val SECONDS: TimeUnit = new TimeUnit("SECONDS", 3) {
    def convert(a: Long, u: TimeUnit): Long = u.toSeconds(a)
    def toNanos(a: Long): Long = x(a, C3 / C0, MAX / (C3 / C0))
    def toMicros(a: Long): Long = x(a, C3 / C1, MAX / (C3 / C1))
    def toMillis(a: Long): Long = x(a, C3 / C2, MAX / (C3 / C2))
    def toSeconds(a: Long): Long = a
    def toMinutes(a: Long): Long = a / (C4 / C3)
    def toHours(a: Long): Long = a / (C5 / C3)
    def toDays(a: Long): Long = a / (C6 / C3)
  }

  final val MINUTES: TimeUnit = new TimeUnit("MINUTES", 4) {
    def convert(a: Long, u: TimeUnit): Long = u.toMinutes(a)
    def toNanos(a: Long): Long = x(a, C4 / C0, MAX / (C4 / C0))
    def toMicros(a: Long): Long = x(a, C4 / C1, MAX / (C4 / C1))
    def toMillis(a: Long): Long = x(a, C4 / C2, MAX / (C4 / C2))
    def toSeconds(a: Long): Long = x(a, C4 / C3, MAX / (C4 / C3))
    def toMinutes(a: Long): Long = a
    def toHours(a: Long): Long = a / (C5 / C4)
    def toDays(a: Long): Long = a / (C6 / C4)
  }

  final val HOURS: TimeUnit = new TimeUnit("HOURS", 5) {
    def convert(a: Long, u: TimeUnit): Long = u.toHours(a)
    def toNanos(a: Long): Long = x(a, C5 / C0, MAX / (C5 / C0))
    def toMicros(a: Long): Long = x(a, C5 / C1, MAX / (C5 / C1))
    def toMillis(a: Long): Long = x(a, C5 / C2, MAX / (C5 / C2))
    def toSeconds(a: Long): Long = x(a, C5 / C3, MAX / (C5 / C3))
    def toMinutes(a: Long): Long = x(a, C5 / C4, MAX / (C5 / C4))
    def toHours(a: Long): Long = a
    def toDays(a: Long): Long = a / (C6 / C5)
  }

  final val DAYS: TimeUnit = new TimeUnit("DAYS", 6) {
    def convert(a: Long, u: TimeUnit): Long = u.toDays(a)
    def toNanos(a: Long): Long = x(a, C6 / C0, MAX / (C6 / C0))
    def toMicros(a: Long): Long = x(a, C6 / C1, MAX / (C6 / C1))
    def toMillis(a: Long): Long = x(a, C6 / C2, MAX / (C6 / C2))
    def toSeconds(a: Long): Long = x(a, C6 / C3, MAX / (C6 / C3))
    def toMinutes(a: Long): Long = x(a, C6 / C4, MAX / (C6 / C4))
    def toHours(a: Long): Long = x(a, C6 / C5, MAX / (C6 / C5))
    def toDays(a: Long): Long = a
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

  private def x(a: Long, b: Long, max: Long): Long = {
    if (a > max) MAX
    else if (a < -max) -MAX
    else a * b
  }

  def of(chronoUnit: ChronoUnit): TimeUnit = {
    Objects.requireNonNull(chronoUnit, "chronoUnit")

    chronoUnit match {
      case ChronoUnit.NANOS =>
        TimeUnit.NANOSECONDS

      case ChronoUnit.MICROS =>
        TimeUnit.MICROSECONDS

      case ChronoUnit.MILLIS =>
        TimeUnit.MILLISECONDS

      case ChronoUnit.SECONDS =>
        TimeUnit.SECONDS

      case ChronoUnit.MINUTES =>
        TimeUnit.MINUTES

      case ChronoUnit.HOURS =>
        TimeUnit.HOURS

      case ChronoUnit.DAYS =>
        TimeUnit.DAYS

      case _ =>
        throw new IllegalArgumentException(
          s"No TimeUnit equivalent for ${chronoUnit}"
        )
    }
  }

  private def convertDuration(duration: Duration, unit: TimeUnit): Long = {
    val seconds = duration.getSeconds()
    val nano = duration.getNano()

    val convertedSeconds = unit.convert(seconds, TimeUnit.SECONDS)
    val convertedNano = unit.convert(nano, TimeUnit.NANOSECONDS)

    try {
      Math.addExact(convertedSeconds, convertedNano)
    } catch {
      case _: ArithmeticException =>
        if (seconds > 0) jl.Long.MAX_VALUE
        else jl.Long.MIN_VALUE
    }
  }
}
