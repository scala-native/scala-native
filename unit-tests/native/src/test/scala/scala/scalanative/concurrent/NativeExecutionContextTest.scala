// Needs to be defined in this package to allow for accessing package-private members
package scala.scalanative.concurrent

import org.junit.Assert._
import org.junit.Test

class NativeExecutionContextTest {
  @Test def executeSingleTaskTest(): Unit = {
    var i = 0
    val runnable = new Runnable {
      def run(): Unit = i += 1
    }

    val queue = NativeExecutionContext.queue.asInstanceOf[QueueExecutionContext]
    queue.execute(runnable)
    queue.execute(runnable)

    assertTrue(queue.isWorkStealingPossible)
    assertEquals(2, queue.availableTasks)
    queue.stealWork(1)
    assertEquals(1, i)

    assertTrue(queue.isWorkStealingPossible)
    assertEquals(1, queue.availableTasks)
    queue.stealWork(1)
    assertEquals(2, i)

    assertFalse(queue.isWorkStealingPossible)
    assertEquals(0, queue.availableTasks)
    queue.stealWork(1)
    assertEquals(2, i)
  }
}
