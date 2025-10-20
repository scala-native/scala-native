package org.scalanative.testsuite.javalib.util.random

/* JEP356 - Enhanced Pseudo-Random Number Generators
 *
 * Introduced in Java 17.
 */

import org.junit.Test
import org.junit.Assert.*
import org.junit.Assume.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

import java.util.HashSet
import java.util.List

import java.util.random.RandomGenerator
import java.util.random.RandomGeneratorFactory

import java.util.stream.Collectors

class JEP356_RandomGeneratorFactoryTestOnJDK17 {

  // rgf is short for RandomGeneratorFactory

  @Test def rgf_all(): Unit = {
    /*  13 is the eventual expected count current in JVM 23.
     *  Scala Native does not support SecureRandom at all, so that
     *  reduces the eventual expected count to 12.
     */

    val expectedAlgNames = List.of(
      "L32X64MixRandom",
      "L64X128MixRandom",
      "Random",
      "SplittableRandom",
      "Xoroshiro128PlusPlus",
      "Xoshiro256PlusPlus"
    )

    assumeFalse(
      s"SN has only ${expectedAlgNames.size()} of the 13 JVM algorithms",
      Platform.executingInJVM
    )

    val expectedCount = expectedAlgNames.size()

    assertEquals(
      "all()",
      expectedCount,
      RandomGeneratorFactory.all().count()
    )

    /* The order of names returned by all() may differ from that in
     * expectedAlgNames. We care about existence or not. Order is not
     * a concern. Sets are easier to implement than sorting lists
     *
     * Scala 3 can use a lambda with .collect(Collectors.toSet())
     * to do the test more succiently. Labor through the limited
     * capabilities of Scala 2 here.
     */

    val foundAlgNames = new HashSet[String]()
    RandomGeneratorFactory
      .all()
      .forEach((rgf: RandomGeneratorFactory[?]) =>
        foundAlgNames.add(rgf.name())
      )

    assertEquals(
      "algorithm names",
      new HashSet(expectedAlgNames),
      foundAlgNames
    )
  }

  @Test def rgf_getDefault(): Unit = {
    val expectedName = "L32X64MixRandom"
    val factory = RandomGeneratorFactory.getDefault()

    assertEquals(
      "getDefault L32X64MixRandom",
      expectedName,
      factory.name()
    )
  }

  @Test def rgf_of_NonexistentRandom(): Unit = {
    val expectedName = "NonexistentRandom"

    assertThrows(
      expectedName,
      classOf[IllegalArgumentException],
      RandomGeneratorFactory.of[RandomGenerator](expectedName)
    )
  }

  @Test def rgf_of_L32X64MixRandom(): Unit = {
    val expectedName = "L32X64MixRandom"
    val factory = RandomGeneratorFactory.of[RandomGenerator](expectedName)

    assertEquals("of L32X64MixRandom", expectedName, factory.name())
  }

  @Test def rgf_of_Random(): Unit = {
    val expectedName = "Random"
    val factory = RandomGeneratorFactory.of[RandomGenerator](expectedName)

    assertEquals("of Random", expectedName, factory.name())
  }

}
