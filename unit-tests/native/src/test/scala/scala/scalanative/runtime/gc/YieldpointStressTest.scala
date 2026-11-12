package scala.scalanative.runtime.gc

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo

/* Stops the world many times while more threads than CPUs run call-free
 * loops (reachable only through their back-edge yieldpoints) and allocate
 * linked data that is checked after every step. A thread that never reaches a
 * safepoint makes the GC abort; a lost root shows up as corrupted data.
 */
class YieldpointStressTest {
  import YieldpointStressTest._

  @Test def oversubscribedMutatorsReachSafepoints(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
    assumeTrue(
      "requires Immix or Commix",
      LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
    )
    val cpus = Runtime.getRuntime().availableProcessors()
    val spinners = Math.max(4, cpus * 2)
    val allocators = Math.max(2, cpus / 2)
    val control = new Control
    val failure = new AtomicReference[Throwable](null)
    val finished = new AtomicInteger(0)

    def start(body: => Unit): Thread = {
      val t = new Thread(new Runnable {
        def run(): Unit =
          try body
          catch { case e: Throwable => failure.compareAndSet(null, e) }
          finally finished.incrementAndGet()
      })
      t.setDaemon(true)
      t.start()
      t
    }

    val threads =
      Seq.fill(spinners)(start(spin(control))) ++
        Seq.fill(allocators)(start(allocate(control)))

    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
    var collections = 0
    while (System.nanoTime() < deadline && failure.get() == null) {
      System.gc()
      collections += 1
    }
    control.stop = true
    threads.foreach(_.join(TimeUnit.SECONDS.toMillis(30)))

    if (failure.get() != null) throw failure.get()
    assertEquals("threads finished", threads.size, finished.get())
    assertTrue("collections", collections > 0)
  }
}

object YieldpointStressTest {
  final class Control {
    @volatile var stop = false
  }

  final class Node(val value: Long, val payload: Array[Long], val next: Node)

  @volatile var sink: Long = 0L

  // No calls in the loop body: only the back-edge yieldpoint stops it.
  def spin(control: Control): Unit = {
    var i = 0L
    while (!control.stop) i += 1
    sink = i
  }

  def allocate(control: Control): Unit = {
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
