package org.scalanative.testsuite.javalib.util.stream

import java.util.stream.*
import java.util.List
import java.util.Spliterator

import org.junit.Test
import org.junit.Assert.*

class IntStreamTestOnJDK9 {

  final val epsilon = 0.00001 // tolerance for Floating point comparisons.

  @Test def intStreamDropWhile_Empty(): Unit = {
    val s = IntStream.empty()

    val remaining = s.dropWhile(_ < 0)

    assertFalse("stream should be empty", remaining.findFirst().isPresent)
  }

  @Test def intStreamDropWhile_NoMatch(): Unit = {
    val expectedRemainingCount = 6

    val s = IntStream.of(11, 22, 44, 1, -1, 2)

    val remaining = s.dropWhile(_ > 100)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def intStreamDropWhile_SomeMatch(): Unit = {
    val expectedRemainingCount = 4

    val s = IntStream.of(11, 22, 44, 1, -1, 2)

    val remaining = s.dropWhile(_ < 30)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def intStreamIterate_BoundedByPredicate(): Unit = {
    var count = -1
    val limit = 5

    val expectedSeed = 271828

    val s = IntStream.iterate(
      expectedSeed,
      e => count < limit,
      e => {
        count += 1
        e + 1
      }
    )

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())

    assertEquals(s"seed", expectedSeed, it.nextInt())

    for (j <- 1 to limit) {
      assertEquals(s"element: ${j}", expectedSeed + j, it.nextInt())
    }

    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def intStreamIterate_BoundedByPredicate_Characteristics(): Unit = {
    var count = -1
    val limit = 5

    val expectedSeed = 271828

    val s = IntStream.iterate(
      expectedSeed,
      e => count < limit,
      e => {
        count += 1
        e + 1
      }
    )
    val spliter = s.spliterator()

    // spliterator should have required characteristics and no others.
    // Note: IntStream requires NONNULL, whereas Stream[T] does not.
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

  @Test def intStreamTakeWhile_Empty(): Unit = {
    val s = IntStream.empty()

    val taken = s.takeWhile(_ < 523)

    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def intStreamTakeWhile_NoMatch(): Unit = {
    val s = IntStream.of(11, 22, 44, 1, -1, 2)

    val taken = s.takeWhile(_ > 101)
    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def intStreamTakeWhile_SomeMatch(): Unit = {
    val expectedTakenCount = 3

    val s = IntStream.of(11, 22, 44, 1, -1, 2)

    val taken = s.takeWhile(_ > 5)

    assertEquals("unexpected taken count", expectedTakenCount, taken.count())
  }

  // Issue 4007
  @Test def intStreamSkip_GivesDownstreamAccurateExpectedSize(): Unit = {
    /* Tests for Issue 4007 require a SIZED spliterator with a tryAdvance()
     * which does not change the exactSize() after traversal begins.
     *
     * This Test is fragile in that it uses intimate knowledge of
     * Scala Native internal implementations to provide such a spliterator. If
     * those implements change, this Test may end up succeeding but
     * not exercising the Issue 4007 path.
     *
     * List.of() followed by mapToInt() provides a suitable spliterator.
     * IntStream.of() by itself does not provoke the defect, its exactSize()
     * bookkeeping is too good.
     */

    val srcData = List.of(111, 222, 333, 444, 555, 666, 777)
    val s = srcData.stream()
    val is = s.mapToInt(e => e)

    val skipSize = 4
    val expectedSize = srcData.size() - skipSize
    val resultSize = is.skip(skipSize).toArray().size

    assertEquals("expectedSize", expectedSize, resultSize)
  }
}
