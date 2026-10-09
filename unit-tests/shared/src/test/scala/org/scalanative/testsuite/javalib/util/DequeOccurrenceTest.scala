package org.scalanative.testsuite.javalib.util

import java.util.concurrent.{ConcurrentLinkedDeque, LinkedBlockingDeque}
import java.util.{ArrayDeque, Deque, LinkedList}

import org.junit.Assert._
import org.junit.Test

class DequeOccurrenceTest {
  private def deques(): Seq[Deque[Integer]] = Seq(
    new ArrayDeque[Integer](),
    new LinkedList[Integer](),
    new ConcurrentLinkedDeque[Integer](),
    new LinkedBlockingDeque[Integer]()
  )

  @Test def occurrenceRemovalThroughDeque(): Unit = {
    for (q <- deques()) {
      for (n <- Seq(1, 2, 1, 3, 1)) q.add(Integer.valueOf(n))
      assertFalse(q.removeFirstOccurrence("absent"))
      assertFalse(q.removeLastOccurrence("absent"))
      assertTrue(q.removeFirstOccurrence(1))
      assertTrue(q.removeLastOccurrence(1))
      assertEquals(Integer.valueOf(2), q.removeFirst())
      assertEquals(Integer.valueOf(3), q.removeLast())
      assertEquals(Integer.valueOf(1), q.removeFirst())
      assertTrue(q.isEmpty())
    }
  }

  @Test def collectionRemovalOfBoxedValues(): Unit = {
    for (q <- deques()) {
      q.add(Integer.valueOf(1))
      val value: Any = 1
      assertTrue(q.remove(value))
      assertFalse(q.remove(value))
      assertTrue(q.isEmpty())
    }
  }
}
