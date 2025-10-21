package org.scalanative.testsuite.javalib.util.stream

import java.util.function.{Predicate, UnaryOperator}
import java.util.stream._
import java.util.{List, Spliterator}

import org.junit.Assert._
import org.junit.Test

class StreamTestOnJDK9 {

  final val no = false
  final val yes = true

  case class Patron(hasTicket: Boolean, isRowdy: Boolean)

  @Test def streamDropWhile_Empty(): Unit = {
    val s = Stream.empty[Patron]()

    val remaining = s.dropWhile((e) => e.hasTicket)

    assertFalse("stream should be empty", remaining.findFirst().isPresent)
  }

  @Test def streamDropWhile_NoMatch(): Unit = {
    val expectedRemainingCount = 4

    val s = Stream.of(
      Patron(hasTicket = no, isRowdy = no),
      Patron(hasTicket = yes, isRowdy = no),
      Patron(hasTicket = no, isRowdy = no),
      Patron(hasTicket = yes, isRowdy = no)
    )

    val remaining = s.dropWhile((e) => e.isRowdy)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def streamDropWhile_SomeMatch(): Unit = {
    val expectedRemainingCount = 2

    val s = Stream.of(
      Patron(hasTicket = no, isRowdy = yes),
      Patron(hasTicket = yes, isRowdy = yes),
      Patron(hasTicket = no, isRowdy = no),
      Patron(hasTicket = yes, isRowdy = yes)
    )

    val remaining = s.dropWhile((e) => e.isRowdy)

    assertEquals(
      "unexpected remaining count",
      expectedRemainingCount,
      remaining.count()
    )
  }

  @Test def streamIterate_BoundedByPredicate(): Unit = {
    var count = -1
    val limit = 5

    val expectedSeed = "Red bellied woodpecker"
    val s = Stream.iterate[String](
      expectedSeed,
      (str => count < limit): Predicate[String],
      (e => {
        count += 1
        count.toString()
      }): UnaryOperator[String]
    )

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())

    assertEquals(s"seed", expectedSeed, it.next())

    for (j <- 0 until limit)
      assertEquals(s"element: ${j}", String.valueOf(j), it.next())

    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def streamIterate_BoundedByPredicate_Characteristics(): Unit = {
    var count = -1
    val limit = 5

    val expectedSeed = "Red bellied woodpecker"
    val s = Stream.iterate[String](
      expectedSeed,
      (str => count < limit): Predicate[String],
      (e => {
        count += 1
        count.toString()
      }): UnaryOperator[String]
    )

    val spliter = s.spliterator()

    // spliterator should have required characteristics and no others.
    val requiredPresent = Seq(Spliterator.ORDERED, Spliterator.IMMUTABLE)

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

  @Test def streamOfNullable_Empty(): Unit = {
    val s = Stream.ofNullable[String](null)
    val it = s.iterator()

    assertFalse("stream should be empty", it.hasNext())
  }

  @Test def streamOfNullable_Singleton(): Unit = {
    val expected = "Frodo"
    val s = Stream.ofNullable[String](expected)

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())
    assertEquals(s"singleton", expected, it.next())
  }

  @Test def streamOf_TypeDispatch(): Unit = {
    val expected = "Frodo"
    val s = Stream.ofNullable[String](expected)

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())
    assertEquals(s"singleton", expected, it.next())
  }

  @Test def streamTakeWhile_Empty(): Unit = {
    val s = Stream.empty[Patron]()

    val taken = s.takeWhile((e) => e.hasTicket)

    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def streamTakeWhile_NoMatch(): Unit = {
    val s = Stream.of(
      Patron(hasTicket = no, isRowdy = no),
      Patron(hasTicket = yes, isRowdy = no),
      Patron(hasTicket = no, isRowdy = yes),
      Patron(hasTicket = yes, isRowdy = no)
    )

    val taken = s.takeWhile((e) => e.hasTicket)
    assertFalse("stream should be empty", taken.findFirst().isPresent)
  }

  @Test def streamTakeWhile_SomeMatch(): Unit = {
    val expectedTakenCount = 3

    val s = Stream.of(
      Patron(hasTicket = yes, isRowdy = no),
      Patron(hasTicket = yes, isRowdy = yes),
      Patron(hasTicket = yes, isRowdy = no),
      Patron(hasTicket = no, isRowdy = no),
      Patron(hasTicket = yes, isRowdy = no),
      Patron(hasTicket = no, isRowdy = yes),
      Patron(hasTicket = yes, isRowdy = no)
    )

    val taken = s.takeWhile((e) => e.hasTicket)

    assertEquals("unexpected taken count", expectedTakenCount, taken.count())
  }

  // Issue 4007
  @Test def streamSkip_GivesDownstreamAccurateExpectedSize(): Unit = {
    /* Use List.of() in Tests for Issue 4007. That Issue requires
     * a SIZED spliterator with a tryAdvance() which does not
     * change the exactSize() after traversal begins. List.of() provides such.
     *
     * Stream.of() does not exercise the condition being tested.
     * The implementation path it uses does bookkeeping to provide
     * an accurate exactSize() after traversal begins. That work is
     * allowed but not required by the JDK.
     */

    val srcData = List.of("R", "S", "T", "U", "V", "X", "Y", "Z")
    val s = srcData.stream()

    val skipSize = 4
    val expectedSize = srcData.size() - skipSize
    val resultSize = s.skip(skipSize).toArray().size

    assertEquals("expectedSize", expectedSize, resultSize)
  }
}
