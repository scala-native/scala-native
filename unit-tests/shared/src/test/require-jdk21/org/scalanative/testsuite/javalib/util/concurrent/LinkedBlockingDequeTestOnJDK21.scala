package org.scalanative.testsuite.javalib.util.concurrent

import java.util.concurrent.{BlockingDeque, LinkedBlockingDeque}

import org.junit.Assert._
import org.junit.Test

class LinkedBlockingDequeTestOnJDK21 {
  @Test def reversedViewThroughBlockingDeque(): Unit = {
    val q: BlockingDeque[String] = new LinkedBlockingDeque[String](3)
    q.addFirst("first")
    q.addLast("last")

    val reversed = q.reversed()
    assertEquals("last", reversed.removeFirst())
    reversed.addLast("new first")
    assertEquals("new first", q.takeFirst())
    assertEquals("first", q.takeLast())
    assertTrue(q.isEmpty())
  }

  @Test def occurrenceRemovalThroughReversedView(): Unit = {
    val q: BlockingDeque[Integer] = new LinkedBlockingDeque[Integer]()
    for (n <- Seq(1, 2, 1, 3, 1)) q.add(Integer.valueOf(n))

    val reversed = q.reversed()
    assertTrue(reversed.removeFirstOccurrence(1))
    assertEquals(Integer.valueOf(3), q.getLast())
    assertTrue(reversed.removeLastOccurrence(1))
    assertEquals(Integer.valueOf(2), q.getFirst())
    assertEquals(3, q.size())
  }
}
