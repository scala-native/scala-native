package scala.scalanative
package runtime

import org.junit.Assert._
import org.junit.Test

class LoopTest {
  @Test def loopRunOnceTest(): Unit = {
    var i = 0
    val runnable = new Runnable {
      def run(): Unit = i += 1
    }
    ExecutionContext.global.execute(runnable)
    ExecutionContext.global.execute(runnable)
    assertEquals(loopRunOnce(), 1)
    assertEquals(1, i)
    assertEquals(loopRunOnce(), 0)
    assertEquals(2, i)
    assertEquals(loopRunOnce(), 0)
    assertEquals(2, i)
  }
}
