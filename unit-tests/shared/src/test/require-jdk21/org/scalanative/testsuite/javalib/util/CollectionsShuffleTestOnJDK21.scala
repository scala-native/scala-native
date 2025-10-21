package org.scalanative.testsuite.javalib.util

import java.util.random.RandomGenerator
import java.util.{ArrayList, Arrays, Collections}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CollectionsShuffleTestOnJDK21 {

// format: off

  // Ease visual verification by starting with an obviously ordered list.
  val referenceCopy = Collections.unmodifiableList(
    Arrays.asList( // Atlantic storm names, 2025
      "Andrea", "Barry", "Chantal", "Dexter", "Erin",
      "Fernand", "Gabrielle", "Humberto", "Imelda", "Jerry",
      "Karen", "Lorenzo", "Melissa", "Nestor", "Olga",
      "Pablo", "Rebekah", "Sebastien", "Tanya", "Van",
      "Wendy"
    )
  )

// format: on

  @Test def shuffle_RandomGenerator(): Unit = {

    // List must be mutable. It is shuffled in-place.
    val shuffleMe = new ArrayList[String](referenceCopy)

    val rng = RandomGenerator.of("L64X128MixRandom")

    Collections.shuffle(shuffleMe, rng)

    // Check only that something changed, not quality of the shuffle.
    assertFalse("shuffled list should differ", shuffleMe.equals(referenceCopy))
  }
}
