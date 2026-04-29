package org.scalanative.testsuite.javalib.util

import java.util.SplittableRandom

import org.junit.Assume.assumeTrue

object SplittableRandomTestPlatform {
  def hasNextBytes: Boolean =
    false

  def assumeNextBytes(): Unit =
    assumeTrue(
      "SplittableRandom.nextBytes is covered by require-jdk17 tests",
      false
    )

  def nextBytes(random: SplittableRandom, bytes: Array[Byte]): Unit =
    throw new AssertionError("unreachable")
}
