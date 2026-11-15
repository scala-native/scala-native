package scala.scalanative.runtime

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe._

object VirtualThreadStackallocTest {
  // Not constants, so that the allocations cannot be folded or moved
  @volatile var enabled = true
  @volatile var iterations = 8

  @noinline def read(p: Ptr[Long]): Long = !p

  /* Has no exception handlers, so that it is optimized in release mode. The
   * optimizer guards the stack allocations done in the loop with a stack
   * state restored in each iteration.
   */
  @noinline def stackallocInLoopWithPark(parkNanos: Long): String = {
    var sum = 0L
    var zeroed = true
    var i = 0
    while (i < iterations) {
      val p = stackalloc[Long]()
      if (read(p) != 0L) zeroed = false
      !p = i.toLong + 1
      sum += read(p)
      // Pointers to the stack are not valid after the thread is resumed
      LockSupport.parkNanos(parkNanos)
      i += 1
    }
    val n = iterations.toLong
    if (!zeroed) "memory was not zeroed in each iteration"
    else if (sum == n * (n + 1) / 2) "ok"
    else s"unexpected sum: $sum"
  }
}

/* The stack of a virtual thread is relocated when it is resumed. A frame which
 * allocates stack memory outside of its entry block used to be addressed by
 * the frame pointer, which was stale after the relocation.
 */
class VirtualThreadStackallocTest {
  import VirtualThreadStackallocTest._

  private val Timeout = 10000L
  private def parkNanos = 5 * 1000 * 1000L

  private def runInVirtualThread(body: () => String): Unit = {
    AssumesHelper.assumeSupportsVirtualThreads()
    val result = new AtomicReference[String]("not completed")
    val thread = Thread.ofVirtual().start { () =>
      // Exceptions have to be dispatched in the relocated frame as well
      try result.set(body())
      catch { case ex: IllegalStateException => result.set(ex.getMessage()) }
    }
    thread.join(Timeout)
    assertEquals("ok", result.get())
  }

  @Test def conditionalStackallocAroundPark(): Unit = runInVirtualThread { () =>
    var sum = 0L
    if (enabled) {
      val p = stackalloc[Long]()
      !p = 42L
      sum += read(p)
    }
    LockSupport.parkNanos(parkNanos)
    if (enabled) {
      val p = stackalloc[Long]()
      !p = 1L
      sum += read(p)
    }
    if (sum == 43L) throw new IllegalStateException("ok")
    s"unexpected sum: $sum"
  }

  @Test def conditionalStackallocOfArrayAroundPark(): Unit =
    runInVirtualThread { () =>
      var sum = 0L
      if (enabled) {
        val p = stackalloc[Long](16)
        var i = 0
        while (i < 16) {
          sum += read(p + i) // zeroed
          p(i) = i.toLong
          i += 1
        }
        sum += read(p + 15)
      }
      LockSupport.parkNanos(parkNanos)
      if (enabled) {
        val p = stackalloc[Long](16)
        sum += read(p + 15) // zeroed
        p(3) = 3L
        sum += read(p + 3)
      }
      if (sum == 18L) throw new IllegalStateException("ok")
      s"unexpected sum: $sum"
    }

  @Test def stackallocInLoopWithPark(): Unit = {
    // Allocations done in loops are static only if the optimizer has proven
    // that the memory does not escape the iteration
    assumeTrue(
      "stack allocations in loops are dynamic in debug mode",
      LinktimeInfo.releaseMode
    )
    runInVirtualThread { () =>
      VirtualThreadStackallocTest.stackallocInLoopWithPark(parkNanos / 10)
    }
  }
}
