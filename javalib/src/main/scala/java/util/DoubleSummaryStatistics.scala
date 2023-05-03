package java.util

import java.{lang => jl}

class DoubleSummaryStatistics() {
  var _count: Long = 0L
  var _min: Double = jl.Double.POSITIVE_INFINITY
  var _max: Double = jl.Double.NEGATIVE_INFINITY
  var _sum: Double = 0.0

  def this(count: Long, min: Double, max: Double, sum: Double) = {
    this()
    _count = count
    _min = min
    _max = max
    _sum = sum
  }

  def accept(value: Double): Unit = {
    _count += 1L
    _sum += value

    if (value < _min)
      _min = value

    if (value > _max)
      _max = value
  }

  def combine(other: DoubleSummaryStatistics): Unit = {
    _count += other._count
    _sum += other._sum

    if (other._min < _min)
      _min = other._min

    if (other._max > _max)
      _max = other._max
  }

  final def getAverage(): Double =
    if (_count == 0) 0.0 // as defined by JVM DoubleSummaryStatistics
    else _sum / _count

  final def getCount(): Long = _count

  final def getMax(): Double = _max

  final def getMin(): Double = _min

  final def getSum(): Double = _sum

  override def toString(): String = {
    "DoubleSummaryStatistics{" +
      s"count=${_count}, " +
      s"sum=${_sum}, " +
      s"min=${_min}, " +
      s"average=${getAverage()}, " +
      s"max=${_max}" +
      "}"
  }

}
