package org.scalanative.testsuite.javalib.util.concurrent

import java.lang.reflect.InvocationTargetException
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import org.junit.Assume.assumeTrue

object TimeUnit8TestPlatform {
  private val convertDurationMethod =
    try Some(classOf[TimeUnit].getMethod("convert", classOf[Duration]))
    catch {
      case _: NoSuchMethodException => None
    }

  private val toChronoUnitMethod =
    try Some(classOf[TimeUnit].getMethod("toChronoUnit"))
    catch {
      case _: NoSuchMethodException => None
    }

  private val timeUnitOfMethod =
    try Some(classOf[TimeUnit].getMethod("of", classOf[ChronoUnit]))
    catch {
      case _: NoSuchMethodException => None
    }

  def hasTimeUnitOf: Boolean =
    timeUnitOfMethod.isDefined

  def assumeConvertDuration(): Unit =
    assumeTrue(
      "TimeUnit.convert(Duration) requires JDK 11+",
      convertDurationMethod.isDefined
    )

  def assumeToChronoUnit(): Unit =
    assumeTrue(
      "TimeUnit.toChronoUnit requires JDK 9+",
      toChronoUnitMethod.isDefined
    )

  def assumeTimeUnitOf(): Unit =
    assumeTrue("TimeUnit.of requires JDK 9+", hasTimeUnitOf)

  def convertDuration(unit: TimeUnit, duration: Duration): Long =
    try
      convertDurationMethod.get
        .invoke(unit, duration)
        .asInstanceOf[java.lang.Long]
        .longValue()
    catch {
      case e: InvocationTargetException =>
        throw e.getCause()
    }

  def toChronoUnit(unit: TimeUnit): ChronoUnit =
    try
      toChronoUnitMethod.get
        .invoke(unit)
        .asInstanceOf[ChronoUnit]
    catch {
      case e: InvocationTargetException =>
        throw e.getCause()
    }

  def timeUnitOf(chronoUnit: ChronoUnit): TimeUnit =
    try
      timeUnitOfMethod.get
        .invoke(null, chronoUnit.asInstanceOf[AnyRef])
        .asInstanceOf[TimeUnit]
    catch {
      case e: InvocationTargetException =>
        throw e.getCause()
    }
}
