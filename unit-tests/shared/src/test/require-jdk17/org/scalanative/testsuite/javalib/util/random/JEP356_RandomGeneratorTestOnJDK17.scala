package org.scalanative.testsuite.javalib.util.random

/* JEP356 - Enhanced Pseudo-Random Number Generators
 *
 * Introduced in Java 17.
 */

import org.junit.Test
import org.junit.Assert.*

import java.util.random.RandomGenerator

class JEP356_RandomGeneratorTestOnJDK17 {

  /* The static method getDefault() is tested in one place, here,
   * because it does not vary by algorithm. It also makes it easier to
   * change if/when the JDK default changes.
   *
   * The static RandomGenerator.of() method is tested in each algorithm file
   * to concentrate testing information that changes by algorithm in the
   * file for that algorithm.  That avoids a huge, hard to maintain, table
   * in this file.
   */

  @Test def getDefault(): Unit = {
    val expectedAlgorithm = ".random.L32X64MixRandom"

    val discoveredAlgorithm =
      RandomGenerator.getDefault().getClass().toString()

    assertTrue(
      s"algorithm tail mismatch, expected suffix: ${expectedAlgorithm} " +
        s"got: ${discoveredAlgorithm}",
      discoveredAlgorithm.endsWith(expectedAlgorithm)
    )
  }
}
