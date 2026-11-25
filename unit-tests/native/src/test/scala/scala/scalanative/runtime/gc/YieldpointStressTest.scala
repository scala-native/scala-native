package scala.scalanative.runtime.gc

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.{Before, Test}

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.signal._
import scala.scalanative.posix.signalOps._
import scala.scalanative.unsafe._

/* Stops the world many times while more threads than CPUs run call-free
 * loops (reachable only through their back-edge yieldpoints) and allocate
 * linked data that is checked after every step. A thread that never reaches a
 * safepoint makes the GC abort; a lost root shows up as corrupted data.
 */
class YieldpointStressTest {
  import YieldpointStressTest._

  @Before def assumeSafepointGC(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    assumeTrue(
      "requires Immix or Commix",
      LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
    )
  }

  @Test def oversubscribedMutatorsReachSafepoints(): Unit = {
    val cpus = Runtime.getRuntime().availableProcessors()
    val spinners = Math.max(4, cpus * 2)
    val allocators = Math.max(2, cpus / 2)
    stressGC(TimeUnit.SECONDS.toMillis(2))(
      Seq.fill(spinners)(spin _) ++ Seq.fill(allocators)(allocate _)
    )
  }

  /* #5393: without an alternate signal stack the trap's signal frame lands
   * right below the interrupted red zone. Threads normally have one (stack
   * overflow guards), which hides this case.
   */
  @Test def spinnersWithoutAltStackReachSafepoints(): Unit =
    if (!LinktimeInfo.isWindows)
      stressGC(500)(Seq.fill(4)(spinWithoutAltStack _))
}

object YieldpointStressTest {
  final class Control(workers: Int) {
    @volatile var stop = false
    private val ready = new CountDownLatch(workers)
    def started(): Unit = ready.countDown()
    def awaitStarted(): Boolean = ready.await(30, TimeUnit.SECONDS)
  }

  final class Node(val value: Long, val payload: Array[Long], val next: Node)

  @volatile var sink: Long = 0L

  /* Runs each body on its own thread and, once all of them called
   * `control.started()`, forces collections for `millis`.
   */
  def stressGC(millis: Long)(bodies: Seq[Control => Unit]): Unit = {
    val control = new Control(bodies.size)
    val failure = new AtomicReference[Throwable](null)
    val threads = bodies.map { body =>
      val t = new Thread(new Runnable {
        def run(): Unit =
          try body(control)
          catch {
            case e: Throwable =>
              failure.compareAndSet(null, e)
              control.started()
          }
      })
      t.setDaemon(true)
      t.start()
      t
    }

    val allStarted = control.awaitStarted()
    val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis)
    while (allStarted && System.nanoTime() < deadline && failure.get() == null)
      System.gc()
    control.stop = true
    threads.foreach(_.join(TimeUnit.SECONDS.toMillis(30)))

    if (failure.get() != null) throw failure.get()
    assertTrue("workers started", allStarted)
    assertFalse("threads still running", threads.exists(_.isAlive()))
  }

  // No calls in the loop body: only the back-edge yieldpoint stops it.
  def spin(control: Control): Unit = {
    control.started()
    var i = 0L
    while (!control.stop) i += 1
    sink = i
  }

  def spinWithoutAltStack(control: Control): Unit = Zone.acquire { implicit z =>
    val previous = alloc[stack_t]()
    assertEquals("sigaltstack query", 0, sigaltstack(null, previous))
    val hadAltStack = (previous.ss_flags & SS_DISABLE) == 0
    if (hadAltStack) {
      // Darwin rejects SS_DISABLE with ENOMEM unless ss_size >= MINSIGSTKSZ
      val disabled = alloc[stack_t]()
      !disabled = !previous
      disabled.ss_flags = SS_DISABLE
      assertEquals("sigaltstack disable", 0, sigaltstack(disabled, null))
    }
    try spin(control)
    finally if (hadAltStack) sigaltstack(previous, null)
  }

  def allocate(control: Control): Unit = {
    control.started()
    var round = 0L
    while (!control.stop) {
      var head: Node = null
      var i = 0
      while (i < 1000) {
        val v = round * 1000 + i
        head = new Node(v, Array.fill(4)(v), head)
        i += 1
      }
      var node = head
      var expected = round * 1000 + 999
      while (node != null) {
        if (node.value != expected || node.payload.exists(_ != expected))
          throw new AssertionError(
            s"corrupted node: value=${node.value} expected=$expected"
          )
        expected -= 1
        node = node.next
      }
      round += 1
    }
  }
}
