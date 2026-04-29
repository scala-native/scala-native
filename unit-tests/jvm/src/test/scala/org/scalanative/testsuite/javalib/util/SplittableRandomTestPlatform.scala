package org.scalanative.testsuite.javalib.util

import java.lang.reflect.InvocationTargetException
import java.util.SplittableRandom

import org.junit.Assume.assumeTrue

object SplittableRandomTestPlatform {
  private val nextBytesMethod =
    try Some(classOf[SplittableRandom].getMethod("nextBytes", classOf[Array[Byte]]))
    catch {
      case _: NoSuchMethodException => None
    }

  def hasNextBytes: Boolean =
    nextBytesMethod.isDefined

  def assumeNextBytes(): Unit =
    assumeTrue("SplittableRandom.nextBytes requires JDK 17+", hasNextBytes)

  def nextBytes(random: SplittableRandom, bytes: Array[Byte]): Unit =
    try nextBytesMethod.get.invoke(random, bytes.asInstanceOf[AnyRef])
    catch {
      case e: InvocationTargetException =>
        throw e.getCause()
    }
}
