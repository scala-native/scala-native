package java.util

import java.{lang => jl}

class IntSummaryStatistics() {
  var _count: Long = 0L
  var _min: Int = jl.Integer.MAX_VALUE
  var _max: Int = jl.Integer.MIN_VALUE
  var _sum: Long = 0L

  def this(count: Long, min: Int, max: Int, sum: Long) = {
    this()
    _count = count
    _min = min
    _max = max
    _sum = sum
  }

  def accept(value: Int): Unit = {
    _count += 1L
    _sum += value

    if (value < _min)
      _min = value

    if (value > _max)
      _max = value
  }

  def combine(other: IntSummaryStatistics): Unit = {
    _count += other._count
    _sum += other._sum

    if (other._min < _min)
      _min = other._min

    if (other._max > _max)
      _max = other._max
  }

  final def getAverage(): Double =
    if (_count == 0) 0.0 // as defined by JVM IntSummaryStatistics
    else _sum.toDouble / _count.toDouble

  final def getCount(): Long = _count

  final def getMax(): Int = _max

  final def getMin(): Int = _min

  final def getSum(): Long = _sum

  override def toString(): String = {
    "IntSummaryStatistics{" +
      s"count=${_count}, " +
      s"sum=${_sum}, " +
      s"min=${_min}, " +
      s"average=${getAverage()}, " +
      s"max=${_max}" +
      "}"
  }

}
