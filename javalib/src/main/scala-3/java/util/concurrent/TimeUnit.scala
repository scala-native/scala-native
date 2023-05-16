// Enums are not source-compatible, make sure to sync this file with Scala 2 implementation

package java.util.concurrent

// Ported from Scala.js

enum TimeUnit extends Enum[TimeUnit] {
  import TimeUnit._
  case NANOSECONDS extends TimeUnit
  case MICROSECONDS extends TimeUnit
  case MILLISECONDS extends TimeUnit
  case SECONDS extends TimeUnit
  case MINUTES extends TimeUnit
  case HOURS extends TimeUnit
  case DAYS extends TimeUnit

  def sleep(timeout: Long): Unit =
    if (timeout > 0) Thread.sleep(toMillis(timeout))
  def timedJoin(thread: Thread, timeout: Long) =
    if (timeout > 0) thread.join(toMillis(timeout))
  def timedWait(obj: Object, timeout: Long) =
    if (timeout > 0) obj.wait(toMillis(timeout))

  def convert(a: Long, u: TimeUnit): Long = this match {
    case TimeUnit.NANOSECONDS  => u.toNanos(a)
    case TimeUnit.MICROSECONDS => u.toMicros(a)
    case TimeUnit.MILLISECONDS => u.toMillis(a)
    case TimeUnit.SECONDS      => u.toSeconds(a)
    case TimeUnit.MINUTES      => u.toMinutes(a)
    case TimeUnit.HOURS        => u.toHours(a)
    case TimeUnit.DAYS         => u.toDays(a)
  }
  def toNanos(a: Long): Long = this match {
    case TimeUnit.NANOSECONDS  => a
    case TimeUnit.MICROSECONDS => x(a, C1 / C0, MAX / (C1 / C0))
    case TimeUnit.MILLISECONDS => x(a, C2 / C0, MAX / (C2 / C0))
    case TimeUnit.SECONDS      => x(a, C3 / C0, MAX / (C3 / C0))
    case TimeUnit.MINUTES      => x(a, C4 / C0, MAX / (C4 / C0))
    case TimeUnit.HOURS        => x(a, C5 / C0, MAX / (C5 / C0))
    case TimeUnit.DAYS         => x(a, C6 / C0, MAX / (C6 / C0))
  }

  def toMicros(a: Long): Long = this match {
    case TimeUnit.NANOSECONDS  => a / (C1 / C0)
    case TimeUnit.MICROSECONDS => a
    case TimeUnit.MILLISECONDS => x(a, C2 / C1, MAX / (C2 / C1))
    case TimeUnit.SECONDS      => x(a, C3 / C1, MAX / (C3 / C1))
    case TimeUnit.MINUTES      => x(a, C4 / C1, MAX / (C4 / C1))
    case TimeUnit.HOURS        => x(a, C5 / C1, MAX / (C5 / C1))
    case TimeUnit.DAYS         => x(a, C6 / C1, MAX / (C6 / C1))
  }

  def toMillis(a: Long): Long = this match {
    case TimeUnit.NANOSECONDS  => a / (C2 / C0)
    case TimeUnit.MICROSECONDS => a / (C2 / C1)
    case TimeUnit.MILLISECONDS => a
    case TimeUnit.SECONDS      => x(a, C3 / C2, MAX / (C3 / C2))
    case TimeUnit.MINUTES      => x(a, C4 / C2, MAX / (C4 / C2))
    case TimeUnit.HOURS        => x(a, C5 / C2, MAX / (C5 / C2))
    case TimeUnit.DAYS         => x(a, C6 / C2, MAX / (C6 / C2))
  }

  def toSeconds(a: Long): Long = this match {
    case TimeUnit.NANOSECONDS  => a / (C3 / C0)
    case TimeUnit.MICROSECONDS => a / (C3 / C1)
    case TimeUnit.MILLISECONDS => a / (C3 / C2)
    case TimeUnit.SECONDS      => a
    case TimeUnit.MINUTES      => x(a, C4 / C3, MAX / (C4 / C3))
    case TimeUnit.HOURS        => x(a, C5 / C3, MAX / (C5 / C3))
    case TimeUnit.DAYS         => x(a, C6 / C3, MAX / (C6 / C3))
  }
  def toMinutes(a: Long): Long = this match {
    case TimeUnit.NANOSECONDS  => a / (C4 / C0)
    case TimeUnit.MICROSECONDS => a / (C4 / C1)
    case TimeUnit.MILLISECONDS => a / (C4 / C2)
    case TimeUnit.SECONDS      => a / (C4 / C3)
    case TimeUnit.MINUTES      => a
    case TimeUnit.HOURS        => x(a, C5 / C4, MAX / (C5 / C4))
    case TimeUnit.DAYS         => x(a, C6 / C4, MAX / (C6 / C4))
  }
  def toHours(a: Long): Long = this match {
    case TimeUnit.NANOSECONDS  => a / (C5 / C0)
    case TimeUnit.MICROSECONDS => a / (C5 / C1)
    case TimeUnit.MILLISECONDS => a / (C5 / C2)
    case TimeUnit.SECONDS      => a / (C5 / C3)
    case TimeUnit.MINUTES      => a / (C5 / C4)
    case TimeUnit.HOURS        => a
    case TimeUnit.DAYS         => x(a, C6 / C5, MAX / (C6 / C5))
  }
  def toDays(a: Long): Long = this match {
    case TimeUnit.NANOSECONDS  => a / (C6 / C0)
    case TimeUnit.MICROSECONDS => a / (C6 / C1)
    case TimeUnit.MILLISECONDS => a / (C6 / C2)
    case TimeUnit.SECONDS      => a / (C6 / C3)
    case TimeUnit.MINUTES      => a / (C6 / C4)
    case TimeUnit.HOURS        => a / (C6 / C5)
    case TimeUnit.DAYS         => a
  }
}

object TimeUnit {
  // deliberately without type ascription to make them compile-time constants
  private final val C0 = 1L
  private final val C1 = C0 * 1000L
  private final val C2 = C1 * 1000L
  private final val C3 = C2 * 1000L
  private final val C4 = C3 * 60L
  private final val C5 = C4 * 60L
  private final val C6 = C5 * 24L
  private final val MAX = Long.MaxValue

  private def x(a: Long, b: Long, max: Long): Long = {
    if (a > max) MAX
    else if (a < -max) -MAX
    else a * b
  }
}
