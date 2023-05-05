package java.util

import java.{lang => jl}

class DoubleSummaryStatistics() {
  private var count: Long = 0L
  private var min: Double = jl.Double.POSITIVE_INFINITY
  private var max: Double = jl.Double.NEGATIVE_INFINITY
  private var sum: Double = 0.0

  def this(count: Long, min: Double, max: Double, sum: Double) = {
    this()
    this.count = count
    this.min = min
    this.max = max
    this.sum = sum
  }

  def accept(value: Double): Unit = {
    count += 1L
    sum += value

    if (value < min)
      min = value

    if (value > max)
      max = value
  }

  def combine(other: DoubleSummaryStatistics): Unit = {
    count += other.count
    sum += other.sum

    if (other.min < min)
      min = other.min

    if (other.max > max)
      max = other.max
  }

  final def getAverage(): Double =
    if (count == 0) 0.0 // as defined by JVM DoubleSummaryStatistics
    else sum / count

  final def getCount(): Long = count

  final def getMax(): Double = max

  final def getMin(): Double = min

  final def getSum(): Double = sum

  override def toString(): String = {
    "DoubleSummaryStatistics{" +
      s"count=${count}, " +
      s"sum=${sum}, " +
      s"min=${min}, " +
      s"average=${getAverage()}, " +
      s"max=${max}" +
      "}"
  }

}
