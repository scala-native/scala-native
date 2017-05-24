package java.time

final class Instant(private val epochMilli: Long) extends Comparable[Instant] {

  override def equals(other: Any): Boolean =
    other match {
      case that: Instant => epochMilli == that.epochMilli
      case _             => false
    }

  def toEpochMilli(): Long = epochMilli

  override def compareTo(other: Instant) =
    epochMilli.compareTo(other.epochMilli)
}

object Instant {
  def ofEpochMilli(epochMilli: Long): Instant =
    new Instant(epochMilli)

  def now(): Instant =
    new Instant(System.currentTimeMillis())
}
