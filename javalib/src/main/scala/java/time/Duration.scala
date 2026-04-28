package java.time

import java.time.temporal.{ChronoUnit, TemporalUnit}

final class Duration private (private val seconds: Long, private val nanos: Int)
    extends Serializable {

  def getSeconds(): Long = seconds

  def getNano(): Int = nanos

  def isZero(): Boolean =
    seconds == 0L && nanos == 0

  def isNegative(): Boolean =
    seconds < 0L

  def plus(amountToAdd: Long, unit: TemporalUnit): Duration =
    Duration.fromTotalNanos(
      toNanosBigInt + Duration.nanosFor(amountToAdd, unit)
    )

  def minus(amountToSubtract: Long, unit: TemporalUnit): Duration =
    Duration.fromTotalNanos(
      toNanosBigInt - Duration.nanosFor(amountToSubtract, unit)
    )

  private[java] def toNanosBigInt: BigInt =
    BigInt(seconds) * Duration.NanosPerSecond + BigInt(nanos)

  private[java] def toMillisAndNanos: (Long, Int) = {
    val totalNanos = toNanosBigInt
    val millis = totalNanos / Duration.NanosPerMilli
    val nanos = totalNanos % Duration.NanosPerMilli
    if (millis >= BigInt(Long.MaxValue)) (Long.MaxValue, 0)
    else (millis.toLong, nanos.toInt)
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case that: Duration =>
        seconds == that.seconds && nanos == that.nanos
      case _ => false
    }

  override def hashCode(): Int =
    (seconds ^ (seconds >>> 32)).toInt + 51 * nanos
}

object Duration {
  private[time] final val NanosPerSecond = 1000000000L
  private final val NanosPerMilli = 1000000L
  private final val NanosPerMicro = 1000L
  private final val SecondsPerMinute = 60L
  private final val SecondsPerHour = 60L * SecondsPerMinute
  private final val SecondsPerDay = 24L * SecondsPerHour

  def ofNanos(nanos: Long): Duration =
    fromTotalNanos(BigInt(nanos))

  def ofMillis(millis: Long): Duration =
    fromTotalNanos(BigInt(millis) * NanosPerMilli)

  def ofSeconds(seconds: Long): Duration =
    new Duration(seconds, 0)

  def ofSeconds(seconds: Long, nanoAdjustment: Long): Duration =
    fromTotalNanos(BigInt(seconds) * NanosPerSecond + BigInt(nanoAdjustment))

  def ofMinutes(minutes: Long): Duration =
    ofSecondsExact(minutes, SecondsPerMinute)

  def ofHours(hours: Long): Duration =
    ofSecondsExact(hours, SecondsPerHour)

  def ofDays(days: Long): Duration =
    ofSecondsExact(days, SecondsPerDay)

  def of(amount: Long, unit: TemporalUnit): Duration =
    fromTotalNanos(nanosFor(amount, unit))

  private[time] def nanosFor(amount: Long, unit: TemporalUnit): BigInt = {
    if (unit == null) throw new NullPointerException()
    val perUnitNanos =
      unit match {
        case ChronoUnit.NANOS   => BigInt(1L)
        case ChronoUnit.MICROS  => BigInt(NanosPerMicro)
        case ChronoUnit.MILLIS  => BigInt(NanosPerMilli)
        case ChronoUnit.SECONDS => BigInt(NanosPerSecond)
        case ChronoUnit.MINUTES => BigInt(SecondsPerMinute) * NanosPerSecond
        case ChronoUnit.HOURS   => BigInt(SecondsPerHour) * NanosPerSecond
        case ChronoUnit.DAYS    => BigInt(SecondsPerDay) * NanosPerSecond
        case _ =>
          throw new java.time.DateTimeException("Unsupported unit: " + unit)
      }
    BigInt(amount) * perUnitNanos
  }

  private def ofSecondsExact(amount: Long, secondsPerUnit: Long): Duration =
    fromSeconds(BigInt(amount) * secondsPerUnit)

  private def fromSeconds(seconds: BigInt): Duration = {
    if (seconds < BigInt(Long.MinValue) || seconds > BigInt(Long.MaxValue))
      throw new ArithmeticException("long overflow")
    new Duration(seconds.toLong, 0)
  }

  private[time] def fromTotalNanos(totalNanos: BigInt): Duration = {
    val nanosPerSecond = BigInt(NanosPerSecond)
    var seconds = totalNanos / nanosPerSecond
    var nanos = totalNanos % nanosPerSecond
    if (nanos < 0) {
      nanos += nanosPerSecond
      seconds -= 1
    }
    if (seconds < BigInt(Long.MinValue) || seconds > BigInt(Long.MaxValue))
      throw new ArithmeticException("long overflow")
    new Duration(seconds.toLong, nanos.toInt)
  }
}
