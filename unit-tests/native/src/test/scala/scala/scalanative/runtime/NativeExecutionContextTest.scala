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
    
    val ctx = NativeExecutionContext.queue
    ctx.execute(runnable)
    ctx.execute(runnable)

    assertTrue(NativeExecutionContext.hasNext)
    assertEquals(2, NativeExecutionContext.scheduled)
    NativeExecutionContext.runNext()
    assertEquals(1, i)

    assertTrue(NativeExecutionContext.hasNext)
    assertEquals(1, NativeExecutionContext.scheduled)
    NativeExecutionContext.runNext()
    assertEquals(2, i)

    assertFalse(NativeExecutionContext.hasNext)
    assertEquals(0, NativeExecutionContext.scheduled)
    NativeExecutionContext.runNext()
    assertEquals(2, i)
  }
}
