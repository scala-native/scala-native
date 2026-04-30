package org.scalanative.testsuite.javalib.util.concurrent

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object TimeUnit8TestPlatform {
  private final val NanosPerSecond = 1000000000L

  def hasTimeUnitOf: Boolean =
    true

  def assumeConvertDuration(): Unit = ()

  def assumeToChronoUnit(): Unit = ()

  def assumeTimeUnitOf(): Unit = ()

  def convertDuration(unit: TimeUnit, duration: Duration): Long = {
    if (duration == null) throw new NullPointerException()
    var seconds = duration.getSeconds()
    var nanos = duration.getNano()
    if (seconds < 0L && nanos > 0) {
      seconds += 1L
      nanos -= NanosPerSecond.toInt
    }

    val convertedSeconds = unit.convert(seconds, TimeUnit.SECONDS)
    if (convertedSeconds == Long.MinValue || convertedSeconds == Long.MaxValue)
      convertedSeconds
    else
      saturatingAdd(
        convertedSeconds,
        unit.convert(nanos.toLong, TimeUnit.NANOSECONDS)
      )
  }

  def toChronoUnit(unit: TimeUnit): ChronoUnit =
    unit match {
      case TimeUnit.NANOSECONDS  => ChronoUnit.NANOS
      case TimeUnit.MICROSECONDS => ChronoUnit.MICROS
      case TimeUnit.MILLISECONDS => ChronoUnit.MILLIS
      case TimeUnit.SECONDS      => ChronoUnit.SECONDS
      case TimeUnit.MINUTES      => ChronoUnit.MINUTES
      case TimeUnit.HOURS        => ChronoUnit.HOURS
      case TimeUnit.DAYS         => ChronoUnit.DAYS
    }

  def timeUnitOf(chronoUnit: ChronoUnit): TimeUnit =
    chronoUnit match {
      case null               => throw new NullPointerException()
      case ChronoUnit.NANOS   => TimeUnit.NANOSECONDS
      case ChronoUnit.MICROS  => TimeUnit.MICROSECONDS
      case ChronoUnit.MILLIS  => TimeUnit.MILLISECONDS
      case ChronoUnit.SECONDS => TimeUnit.SECONDS
      case ChronoUnit.MINUTES => TimeUnit.MINUTES
      case ChronoUnit.HOURS   => TimeUnit.HOURS
      case ChronoUnit.DAYS    => TimeUnit.DAYS
      case unsupported        =>
        throw new IllegalArgumentException(
          "No TimeUnit equivalent for " + unsupported
        )
    }

  private def saturatingAdd(a: Long, b: Long): Long =
    if (b > 0L && a > Long.MaxValue - b) Long.MaxValue
    else if (b < 0L && a < Long.MinValue - b) Long.MinValue
    else a + b
}
