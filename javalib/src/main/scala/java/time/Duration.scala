package java.time

import java.time.temporal.TemporalAmount

@SerialVersionUID(3078945930695997490L)
final class Duration private (private val seconds: Long, private val nanos: Int)
    extends TemporalAmount
    with Ordered[Duration]
    with Serializable {

  def toMillis: Long = {
    val result: Long = Math.multiplyExact(seconds, 1000)
    Math.addExact(result, nanos / Duration.NANOS_PER_MILLI)
  }

  def compare(otherDuration: Duration): Int = {
    val cmp: Int = java.lang.Long.compare(seconds, otherDuration.seconds)
    if (cmp != 0) cmp
    else nanos - otherDuration.nanos
  }
}

@SerialVersionUID(3078945930695997490L)
object Duration {
  private val NANOS_PER_MILLI: Int = 1000000
}
