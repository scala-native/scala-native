package org.scalanative.testsuite.javalib.util.concurrent

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object TimeUnit8TestPlatform {
  def hasTimeUnitOf: Boolean =
    true

  def assumeConvertDuration(): Unit = ()

  def assumeToChronoUnit(): Unit = ()

  def assumeTimeUnitOf(): Unit = ()

  def convertDuration(unit: TimeUnit, duration: Duration): Long =
    unit.convert(duration)

  def toChronoUnit(unit: TimeUnit): ChronoUnit =
    unit.toChronoUnit()

  def timeUnitOf(chronoUnit: ChronoUnit): TimeUnit =
    TimeUnit.of(chronoUnit)
}
