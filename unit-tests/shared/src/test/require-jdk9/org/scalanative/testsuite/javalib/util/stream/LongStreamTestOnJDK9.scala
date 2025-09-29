package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._
import java.util.{List, Spliterator}

import org.junit.Assert._
import org.junit.Test

class LongStreamTestOnJDK9 {

  final val epsilon = 0.00001 // tolerance for Floating point comparisons.

  @Test def longStreamDropWhile_Empty(): Unit = {
    val s = LongStream.empty()

    val remaining = s.dropWhile(_ < 0L)

    assertFalse("stream should be empty", remaining.findFirst().isPresent)
  }

  @Test def longStreamDropWhile_NoMatch(): Unit = {
    val expectedRemainingCount = 6

    val s = LongStream.of(11, 22, 44, 1, -1, 2L)

    val remaining = s.dropWhile(_ > 100L)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def longStreamDropWhile_SomeMatch(): Unit = {
    val expectedRemainingCount = 4

    val s = LongStream.of(11, 22, 44, 1, -1, 2L)

    val remaining = s.dropWhile(_ < 30L)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def longStreamIterate_BoundedByPredicate(): Unit = {
    var count = -1
    val limit = 5

    val expectedSeed = 271828L

    val s = LongStream.iterate(
      expectedSeed,
      e => count < limit,
      e => {
        count += 1
        e + 1
      }
    )

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())

    assertEquals(s"seed", expectedSeed, it.nextLong())

    for (j <- 1 to limit) {
      assertEquals(s"element: ${j}", expectedSeed + j, it.nextLong())
    }

    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def longStreamIterate_BoundedByPredicate_Characteristics(): Unit = {
    var count = -1
    val limit = 5

    val expectedSeed = 271828L

    val s = LongStream.iterate(
      expectedSeed,
      e => count < limit,
      e => {
        count += 1
        e + 1
      }
    )
    val spliter = s.spliterator()

    // spliterator should have required characteristics and no others.
    // Note: LongStream requires NONNULL, whereas Stream[T] does not.
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

  @Test def longStreamTakeWhile_Empty(): Unit = {
    val s = LongStream.empty()

    val taken = s.takeWhile(_ < 523L)

    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def longStreamTakeWhile_NoMatch(): Unit = {
    val s = LongStream.of(11, 22, 44, 1, -1, 2L)

    val taken = s.takeWhile(_ > 101L)
    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def intStreamTakeWhile_SomeMatch(): Unit = {
    val expectedTakenCount = 3

    val s = LongStream.of(11, 22, 44, 1, -1, 2L)

    val taken = s.takeWhile(_ > 5L)

    assertEquals("unexpected taken count", expectedTakenCount, taken.count())
  }

  // Issue 4007
  @Test def longStreamSkip_GivesDownstreamAccurateExpectedSize(): Unit = {
    /* Tests for Issue 4007 require a SIZED spliterator with a tryAdvance()
     * which does not change the exactSize() after traversal begins.
     *
     * This Test is fragile in that it uses intimate knowledge of
     * Scala Native internal implementations to provide such a spliterator. If
     * those implements change, this Test may end up succeeding but
     * not exercising the Issue 4007 path.
     *
     * List.of() followed by mapToLong() provides a suitable spliterator.
     * LongStream.of() by itself does not provoke the defect, its exactSize()
     * bookkeeping is too good.
     */

    val srcData = List.of(111L, 222L, 333L, 444L, 555L, 666L, 777L)
    val s = srcData.stream()
    val is = s.mapToLong(e => e)

    val skipSize = 4
    val expectedSize = srcData.size() - skipSize
    val resultSize = is.skip(skipSize).toArray().size

    assertEquals("expectedSize", expectedSize, resultSize)
  }
}
