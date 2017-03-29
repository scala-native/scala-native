package java.time

final class Instant(private val epochMilli: Long) extends Comparable[Instant] {
  def toEpochMilli(): Long = epochMilli

  override def compareTo(other: Instant) =
    epochMilli.compareTo(other.epochMilli)
}

object Instant {
  def ofEpochMilli(epochMilli: Long): Instant =
    new Instant(epochMilli)
}
