package org.scalanative.testsuite.javalib.util.concurrent

import java.lang.reflect.InvocationTargetException
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import org.junit.Assume.assumeTrue

object TimeUnit8TestPlatform {
  private val timeUnitOfMethod =
    try Some(classOf[TimeUnit].getMethod("of", classOf[ChronoUnit]))
    catch {
      case _: NoSuchMethodException => None
    }

  def hasTimeUnitOf: Boolean =
    timeUnitOfMethod.isDefined

  def assumeTimeUnitOf(): Unit =
    assumeTrue("TimeUnit.of requires JDK 19+", hasTimeUnitOf)

  def timeUnitOf(chronoUnit: ChronoUnit): TimeUnit =
    try timeUnitOfMethod.get
        .invoke(null, chronoUnit.asInstanceOf[AnyRef])
        .asInstanceOf[TimeUnit]
    catch {
      case e: InvocationTargetException =>
        throw e.getCause()
    }
}
