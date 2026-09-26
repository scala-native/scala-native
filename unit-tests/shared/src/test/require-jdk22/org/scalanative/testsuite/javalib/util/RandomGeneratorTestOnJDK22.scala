package org.scalanative.testsuite.javalib.util.random

import java.util.Spliterator
import java.util.random.RandomGenerator
import java.{lang => jl}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* This file uses RandomGenerator.getDefault(), which is, as of this writing
 * L32X64MixRandom.
 *
 * This is not because that rng is well equidistibuted or has a long period
 * but because it is the default.
 *
 * People using this method in the real world, or doing tests for
 * equidistribution probably want to specify a 'better' rng, such as
 * L64X128MixRandom.
 */
class RandomGeneratorTestOnJDK22 {

  @Test def equiDoubles_Exceptions_JDK(): Unit = {
    val rng = RandomGenerator.getDefault()

    assertThrows(
      "left is not finite",
      classOf[IllegalArgumentException],
      rng.equiDoubles(jl.Double.NEGATIVE_INFINITY, 0.0, true, false)
    )

    assertThrows(
      "right is not finite",
      classOf[IllegalArgumentException],
      rng.equiDoubles(0.0, jl.Double.POSITIVE_INFINITY, false, true)
    )

    assertThrows(
      "interval is empty",
      classOf[IllegalArgumentException],
      rng.equiDoubles(1.0, 1.0, false, true)
    )
  }

  @Test def equiDoubles_Spliterator(): Unit = {
    val rng = RandomGenerator.getDefault()

    val equiDoublesSpliterator =
      rng
        .equiDoubles(-jl.Double.MAX_VALUE, jl.Double.MAX_VALUE, true, true)
        .spliterator()

    // Same as for doubles()
    val expectedStreamCharacteristics =
      Spliterator.IMMUTABLE //  0x400, decimal 1024

    val equiDoublesStreamCharacteristics =
      equiDoublesSpliterator.characteristics()

    assertEquals(
      "characteristics",
      expectedStreamCharacteristics,
      equiDoublesStreamCharacteristics
    )

    assertEquals(
      "estimated size",
      jl.Long.MAX_VALUE,
      equiDoublesSpliterator.estimateSize()
    )
  }

  @Test def equiDoubles_SmokeTest(): Unit = {
    /* This is a simple test, to see if the entry point executes and
     * produces output that is within bounds and distinct.
     * It does not test the 'goodness-of-fit' to a uniform distribution
     * or for the 'clumpliness' of doubles() described in the JDK 22
     * release notes.
     *
     * Tests such as the Anderson-Darling goodness-of-fit test are not
     * suitable for CI and need to be run manually.
     */

    val rng = RandomGenerator.getDefault()

    // low is an exact multiple of delta, kl becomes that whole multiple
    val lowBound = 0.0
    val highBound = 128.0 // Exclusive
    val limit = 100

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      equiDoubles
        .limit(limit)
        .forEach(d => {
          assertTrue(
            s"$d should be >= low bound ${lowBound}",
            d >= lowBound
          )
          assertTrue(
            s"$d should be < high bound ${highBound}",
            d < highBound
          )
        })
    }

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      assertEquals(
        s"number distinct",
        limit,
        equiDoubles.limit(limit).distinct().count()
      )
    }
  }

  @Test def equiDoubles_SnapTo_UlpHighGrid(): Unit = {
    val rng = RandomGenerator.getDefault()

    // low is not an exact multiple of delta, kl becomes the whole multiple + 1
    val lowBound = Math.nextDown(1.0)
    val highBound = 10.0 // Exclusive
    val limit = 100

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      equiDoubles
        .limit(limit)
        .forEach(d => {
          assertTrue(
            s"$d should be >= low bound ${lowBound}",
            d >= lowBound
          )
          assertTrue(
            s"$d should be < high bound ${highBound}",
            d < highBound
          )
        })
    }

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      assertEquals(
        s"number distinct",
        limit,
        equiDoubles.limit(limit).distinct().count()
      )
    }
  }

  @Test def equiDoubles_0Inclusive_To_1Exclusive(): Unit = {
    val rng = RandomGenerator.getDefault()

    val lowBound = 0.0
    val highBound = 1.0 // Exclusive
    val limit = 100

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      equiDoubles
        .limit(limit)
        .forEach(d => {
          assertTrue(
            s"$d should be >= low bound ${lowBound}",
            d >= lowBound
          )
          assertTrue(
            s"$d should be < high bound ${highBound}",
            d < highBound
          )
        })
    }

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      assertEquals(
        s"number distinct",
        limit,
        equiDoubles.limit(limit).distinct().count()
      )
    }
  }

  @Test def equiDoubles_Negative_Bounds(): Unit = {
    val rng = RandomGenerator.getDefault()

    val lowBound = -10.0
    val highBound = -0.0 // Exclusive, negative 0.0 to complicate life.
    val limit = 100

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      equiDoubles
        .limit(limit)
        .forEach(d => {
          assertTrue(
            s"$d should be >= low bound ${lowBound}",
            d >= lowBound
          )
          assertTrue(
            s"$d should be < high bound ${highBound}",
            d < highBound
          )
        })
    }

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      assertEquals(
        s"number distinct",
        limit,
        equiDoubles.limit(limit).distinct().count()
      )
    }
  }

  @Test def equiDoubles_Huge_Range(): Unit = {
    val rng = RandomGenerator.getDefault()

    val lowBound = -jl.Double.MAX_VALUE
    val highBound = jl.Double.MAX_VALUE // Inclusive
    val limit = 100

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, true)

      equiDoubles
        .limit(limit)
        .forEach(d => {
          assertTrue(
            s"$d should be >= low bound ${lowBound}",
            d >= lowBound
          )
          assertTrue(
            s"$d should be < high bound ${highBound}",
            d < highBound
          )
        })
    }

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      assertEquals(
        s"number distinct",
        limit,
        equiDoubles.limit(limit).distinct().count()
      )
    }
  }

  @Test def equiDoubles_SmokeTest_IEEE754_Subnormals(): Unit = {
    val rng = RandomGenerator.getDefault()

    val lowBound = jl.Double.MIN_VALUE
    val highBound = jl.Double.MIN_NORMAL - Math.ulp(jl.Double.MIN_VALUE)
    val limit = 100

    locally {
      val equiDoubles = rng.equiDoubles(lowBound, highBound, true, false)

      equiDoubles
        .limit(limit)
        .forEach(d => {
          assertTrue(
            s"$d should be >= low bound ${lowBound}",
            d >= lowBound
          )
          assertTrue(
            s"$d should be < high bound ${highBound}",
            d < highBound
          )
        })
    }
  }
}
