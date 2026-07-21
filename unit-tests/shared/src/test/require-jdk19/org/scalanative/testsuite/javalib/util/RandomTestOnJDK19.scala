package org.scalanative.testsuite.javalib.util

// import java.util._
// import java.util.function.{DoubleConsumer, IntConsumer, LongConsumer}

import java.{lang => jl, util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class RandomTestOnJDK19 {

  @Test def test_from_RandomClassRNG(): Unit = {

    val srcRng = new ju.Random()
    val uutRng = ju.Random.from(srcRng)

    assertTrue("rngs should be reference equal", uutRng.eq(srcRng))

    uutRng.setSeed(jl.Long.MAX_VALUE) // should not throw
  }

  @Test def test_from_NonRandomClassRNG(): Unit = {
    val srcRng = ju.random.RandomGenerator.of("L32X64MixRandom")
    val uutRng = ju.Random.from(srcRng)

    assertFalse("rngs should not be reference equal", uutRng.eq(srcRng))

    assertThrows(
      classOf[UnsupportedOperationException],
      uutRng.setSeed(jl.Long.MAX_VALUE)
    )

    // Someday, when time is abundant, test the various passthru methods.
  }
}
