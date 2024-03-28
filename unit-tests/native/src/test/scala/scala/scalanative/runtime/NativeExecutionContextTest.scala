// Needs to be defined in this package to allow for accessing package-private members
package scala.scalanative.runtime

import org.junit.Assert._
import org.junit.Test

class NativeExecutionContextTest {
  @Test def executeSingleTaskTest(): Unit = {
    var i = 0
    val runnable = new Runnable {
      def run(): Unit = i += 1
    }

    val queue = NativeExecutionContext.QueueExecutionContext
    queue.execute(runnable)
    queue.execute(runnable)

    assertTrue(queue.hasNextTask)
    assertEquals(2, queue.availableTasks)
    queue.executeNextTask()
    assertEquals(1, i)

    assertTrue(queue.hasNextTask)
    assertEquals(1, queue.availableTasks)
    queue.executeNextTask()
    assertEquals(2, i)

    assertFalse(queue.hasNextTask)
    assertEquals(0, queue.availableTasks)
    queue.executeNextTask()
    assertEquals(2, i)
  }
}
