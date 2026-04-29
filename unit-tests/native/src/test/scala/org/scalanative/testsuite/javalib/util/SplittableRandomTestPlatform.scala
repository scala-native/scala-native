package org.scalanative.testsuite.javalib.util

import java.util.SplittableRandom

object SplittableRandomTestPlatform {
  def hasNextBytes: Boolean =
    true

  def assumeNextBytes(): Unit = ()

  def nextBytes(random: SplittableRandom, bytes: Array[Byte]): Unit =
    random.nextBytes(bytes)
}
