package org.scalanative.testsuite.javalib.util.concurrent

import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object TimeUnit8TestPlatform {
  def hasTimeUnitOf: Boolean =
    true

  def assumeTimeUnitOf(): Unit = ()

  def timeUnitOf(chronoUnit: ChronoUnit): TimeUnit =
    TimeUnit.of(chronoUnit)
}
