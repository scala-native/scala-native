package java.util

import java.{lang => jl}

class LongSummaryStatistics() {
  private var count: Long = 0L
  private var min: Long = jl.Long.MAX_VALUE
  private var max: Long = jl.Long.MIN_VALUE
  private var sum: Long = 0L

  def this(count: Long, min: Long, max: Long, sum: Long) = {
    this()
    this.count = count
    this.min = min
    this.max = max
    this.sum = sum
  }

  def accept(value: Int): Unit =
    accept(value.toLong)

  def accept(value: Long): Unit = {
    count += 1L
    sum += value

    if (value < min)
      min = value

    if (value > max)
      max = value
  }

  def combine(other: LongSummaryStatistics): Unit = {
    count += other.count
    sum += other.sum

    if (other.min < min)
      min = other.min

    if (other.max > max)
      max = other.max
  }

  final def getAverage(): Double =
    if (count == 0) 0.0 // as defined by JVM LongSummaryStatistics
    else sum.toDouble / count.toDouble

  final def getCount(): Long = count

  final def getMax(): Long = max

  final def getMin(): Long = min

  final def getSum(): Long = sum

  override def toString(): String = {
    "LongSummaryStatistics{" +
      s"count=${count}, " +
      s"sum=${sum}, " +
      s"min=${min}, " +
      s"average=${getAverage()}, " +
      s"max=${max}" +
      "}"
  }

}
