package org.scalanative.testsuite.javalib.util

import java.util.{ArrayList, Arrays, Collections, LinkedList, Random}

import org.junit.Assert._
import org.junit.{
  Ignore, // FIXME
  Test
} // FIXME

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CollectionsShuffleTest {

// format: off

  // Ease visual debug by starting with an obviously ordered list.
  val referenceCopy = Collections.unmodifiableList(
    Arrays.asList( // Atlantic storm names, 2026
        "Arthur", "Bertha", "Cristobal", "Dolly", "Edouard",
        "Fay", "Gonzalo", "Hanna", "Isaias", "Josephine",
        "Kyle", "Leah", "Marco", "Nana", "Omar",
        "Paulette", "Rene", "Sally", "Teddy", "Vicky",
        "Wilfred "
      )
    )

// format: on

  @Test def shuffle_DefaultRNG(): Unit = {

    /* List must be mutable. It is shuffled in-place.
     * Use a linked list to exercise copy-then-shuffle path in shuffle().
     */
    val shuffleMe = new LinkedList[String](referenceCopy)

    Collections.shuffle(shuffleMe)

    // Check only that something changed, not quality of the shuffle.
    assertFalse("shuffled list should differ", shuffleMe.equals(referenceCopy))
  }

  @Test def shuffle_Random(): Unit = {

    // List must be mutable. It is shuffled in-place.
    val shuffleMe = new ArrayList[String](referenceCopy)

    val seed = 1721657971693L // An arbitrary odd number, set for repeatability
    val rng = new Random(seed)

    Collections.shuffle(shuffleMe, rng)

    // Check only that something changed, not quality of the shuffle.
    assertFalse("shuffled list should differ", shuffleMe.equals(referenceCopy))
  }
}
