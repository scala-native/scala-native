package org.scalanative.testsuite.javalib.util

import java.util.SplittableRandom

import scala.language.reflectiveCalls

object SplittableRandomTestPlatform {
  private type SplittableRandomWithNextBytes =
    AnyRef { def nextBytes(bytes: Array[Byte]): Unit }

  def hasNextBytes: Boolean =
    true

  def assumeNextBytes(): Unit = ()

  def nextBytes(random: SplittableRandom, bytes: Array[Byte]): Unit =
    random.asInstanceOf[SplittableRandomWithNextBytes].nextBytes(bytes)
}
