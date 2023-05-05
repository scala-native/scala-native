package org.scalanative.testsuite.javalib.util.stream

import java.util.stream._

import org.junit.Test
import org.junit.Assert._

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

    // Use old style predicate rather than lambda to keep Scala 2.12 happy.
    val predicate = new java.util.function.Predicate[String] {
      def test(str: String): Boolean = count < limit
    }

    val expectedSeed = "Red bellied woodpecker"
    val s = Stream.iterate[String](
      expectedSeed,
      predicate,
      (e: String) => { // Specify parameter type to keep keep Scala 2.12 happy.
        count += 1
        count.toString()
      }
    )

    val it = s.iterator()

    assertTrue("stream should not be empty", it.hasNext())

    assertEquals(s"seed", expectedSeed, it.next())

    for (j <- 0 until limit)
      assertEquals(s"element: ${j}", String.valueOf(j), it.next())

    assertFalse("stream should be empty", it.hasNext())
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

}
