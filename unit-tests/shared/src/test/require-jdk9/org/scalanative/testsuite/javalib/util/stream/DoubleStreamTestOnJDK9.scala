package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._

import org.junit.Test
import org.junit.Assert._

class DoubleStreamTestOnJDK9 {

  final val epsilon = 0.00001 // tolerance for Floating point comparisons.

  @Test def streamDropWhile_Empty(): Unit = {
    val s = DoubleStream.empty()

    val remaining = s.dropWhile(_ < 0.0)

    assertFalse("stream should be empty", remaining.findFirst().isPresent)
  }

  @Test def streamDropWhile_NoMatch(): Unit = {
    val expectedRemainingCount = 6

    val s = DoubleStream.of(1.1, 2.2, 4.4, 0.1, -0.1, 0.2)

    val remaining = s.dropWhile(_ > 10.0)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def streamDropWhile_SomeMatch(): Unit = {
    val expectedRemainingCount = 4

    val s = DoubleStream.of(1.1, 2.2, 4.4, 0.1, -0.1, 0.2)

    val remaining = s.dropWhile(_ < 3.0)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def streamIterate_BoundedByPredicate(): Unit = {
    var count = -1
    val limit = 5

    val expectedSeed = 2.71828

    val s = DoubleStream.iterate(
      expectedSeed,
      e => count < limit,
      e => {
        count += 1
        e + 1.0
      }
    )

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())

    assertEquals(s"seed", expectedSeed, it.nextDouble(), epsilon)

    for (j <- 1 to limit) {
      assertEquals(s"element: ${j}", expectedSeed + j, it.nextDouble(), epsilon)
    }

    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def streamTakeWhile_Empty(): Unit = {
    val s = DoubleStream.empty()

    val taken = s.takeWhile(_ < 5.23)

    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def streamTakeWhile_NoMatch(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 4.4, 0.1, -0.1, 0.2)

    val taken = s.takeWhile(_ > 10.10)
    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def streamTakeWhile_SomeMatch(): Unit = {
    val expectedTakenCount = 3

    val s = DoubleStream.of(1.1, 2.2, 4.4, 0.1, -0.1, 0.2)

    val taken = s.takeWhile(_ > 0.5)

    assertEquals("unexpected taken count", expectedTakenCount, taken.count())
  }

}
