package java.util

import java.{lang => jl}

class LongSummaryStatistics() {
  var _count: Long = 0L
  var _min: Long = jl.Long.MAX_VALUE
  var _max: Long = jl.Long.MIN_VALUE
  var _sum: Long = 0L

  def this(count: Long, min: Long, max: Long, sum: Long) = {
    this()
    _count = count
    _min = min
    _max = max
    _sum = sum
  }

  def accept(value: Int): Unit =
    accept(value.toLong)

  def accept(value: Long): Unit = {
    _count += 1L
    _sum += value

    if (value < _min)
      _min = value

    if (value > _max)
      _max = value
  }

  def combine(other: LongSummaryStatistics): Unit = {
    _count += other._count
    _sum += other._sum

    if (other._min < _min)
      _min = other._min

    if (other._max > _max)
      _max = other._max
  }

  final def getAverage(): Double =
    if (_count == 0) 0.0 // as defined by JVM LongSummaryStatistics
    else _sum.toDouble / _count.toDouble

  final def getCount(): Long = _count

  final def getMax(): Long = _max

  final def getMin(): Long = _min

  final def getSum(): Long = _sum

  override def toString(): String = {
    "LongSummaryStatistics{" +
      s"count=${_count}, " +
      s"sum=${_sum}, " +
      s"min=${_min}, " +
      s"average=${getAverage()}, " +
      s"max=${_max}" +
      "}"
  }

}
