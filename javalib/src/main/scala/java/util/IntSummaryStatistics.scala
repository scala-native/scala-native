package java.util

import java.{lang => jl}

class IntSummaryStatistics() {
  private var count: Long = 0L
  private var min: Int = jl.Integer.MAX_VALUE
  private var max: Int = jl.Integer.MIN_VALUE
  private var sum: Long = 0L

  def this(count: Long, min: Int, max: Int, sum: Long) = {
    this()
    this.count = count
    this.min = min
    this.max = max
    this.sum = sum
  }

  def accept(value: Int): Unit = {
    count += 1L
    sum += value

    if (value < min)
      min = value

    if (value > max)
      max = value
  }

  def combine(other: IntSummaryStatistics): Unit = {
    count += other.count
    sum += other.sum

    if (other.min < min)
      min = other.min

    if (other.max > max)
      max = other.max
  }

  final def getAverage(): Double =
    if (count == 0) 0.0 // as defined by JVM IntSummaryStatistics
    else sum.toDouble / count.toDouble

  final def getCount(): Long = count

  final def getMax(): Int = max

  final def getMin(): Int = min

  final def getSum(): Long = sum

  override def toString(): String = {
    "IntSummaryStatistics{" +
      s"count=${count}, " +
      s"sum=${sum}, " +
      s"min=${min}, " +
      s"average=${getAverage()}, " +
      s"max=${max}" +
      "}"
  }

}
