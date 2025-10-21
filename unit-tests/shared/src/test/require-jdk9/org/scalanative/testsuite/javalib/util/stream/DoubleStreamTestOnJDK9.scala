package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._
import java.util.{List, Spliterator}

import org.junit.Assert._
import org.junit.Test

class DoubleStreamTestOnJDK9 {

  final val epsilon = 0.00001 // tolerance for Floating point comparisons.

  @Test def doubleStreamDropWhile_Empty(): Unit = {
    val s = DoubleStream.empty()

    val remaining = s.dropWhile(_ < 0.0)

    assertFalse("stream should be empty", remaining.findFirst().isPresent)
  }

  @Test def doubleStreamDropWhile_NoMatch(): Unit = {
    val expectedRemainingCount = 6

    val s = DoubleStream.of(1.1, 2.2, 4.4, 0.1, -0.1, 0.2)

    val remaining = s.dropWhile(_ > 10.0)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def doubleStreamDropWhile_SomeMatch(): Unit = {
    val expectedRemainingCount = 4

    val s = DoubleStream.of(1.1, 2.2, 4.4, 0.1, -0.1, 0.2)

    val remaining = s.dropWhile(_ < 3.0)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def doubleStreamIterate_BoundedByPredicate(): Unit = {
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

  @Test def doubleStreamIterate_BoundedByPredicate_Characteristics(): Unit = {
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
    val spliter = s.spliterator()

    // spliterator should have required characteristics and no others.
    // Note: DoubleStream requires NONNULL, whereas Stream[T] does not.
    val requiredPresent =
      Seq(Spliterator.ORDERED, Spliterator.IMMUTABLE, Spliterator.NONNULL)

    val requiredAbsent = Seq(
      Spliterator.SORTED,
      Spliterator.SIZED,
      Spliterator.SUBSIZED
    )

    StreamTestHelpers.verifyCharacteristics(
      spliter,
      requiredPresent,
      requiredAbsent
    )

    // If SIZED is really missing, these conditions should hold.
    assertEquals(s"getExactSizeIfKnown", -1L, spliter.getExactSizeIfKnown())
    assertEquals(s"estimateSize", Long.MaxValue, spliter.estimateSize())
  }

  @Test def doubleStreamTakeWhile_Empty(): Unit = {
    val s = DoubleStream.empty()

    val taken = s.takeWhile(_ < 5.23)

    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def doubleStreamTakeWhile_NoMatch(): Unit = {
    val s = DoubleStream.of(1.1, 2.2, 4.4, 0.1, -0.1, 0.2)

    val taken = s.takeWhile(_ > 10.10)
    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def doubleStreamTakeWhile_SomeMatch(): Unit = {
    val expectedTakenCount = 3

    val s = DoubleStream.of(1.1, 2.2, 4.4, 0.1, -0.1, 0.2)

    val taken = s.takeWhile(_ > 0.5)

    assertEquals("unexpected taken count", expectedTakenCount, taken.count())
  }

  // Issue 4007
  @Test def doubleStreamSkip_GivesDownstreamAccurateExpectedSize(): Unit = {
    /* Tests for Issue 4007 require a SIZED spliterator with a tryAdvance()
     * which does not change the exactSize() after traversal begins.
     *
     * This Test is fragile in that it uses intimate knowledge of
     * Scala Native internal implementations to provide such a spliterator. If
     * those implements change, this Test may end up succeeding but
     * not exercising the Issue 4007 path.
     *
     * List.of() followed by mapToDouble() provides a suitable spliterator.
     * DoubleStream.of() by itself does not provoke the defect, its exactSize()
     * bookkeeping is too good.
     */

    val srcData = List.of(1.11, 2.22, 3.33, 4.44, 5.55, 6.66, 7.77)
    val s = srcData.stream()
    val ds = s.mapToDouble(e => e)

    val skipSize = 4
    val expectedSize = srcData.size() - skipSize
    val resultSize = ds.skip(skipSize).toArray().size

    assertEquals("expectedSize", expectedSize, resultSize)
  }
}
