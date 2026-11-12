package scala.scalanative.runtime

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{
  AtomicBoolean, AtomicInteger, AtomicReference
}

import org.junit.Assert._
import org.junit.Assume.assumeTrue
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper
import scala.scalanative.meta.LinktimeInfo

/* Stops the world many times while virtual threads keep yielding in the middle
 * of a function that holds live data, so their continuations resume on other
 * carriers between yieldpoints of the same frame.
 */
class VirtualThreadYieldpointStressTest {
  import VirtualThreadYieldpointStressTest._

  @Test def migratingVirtualThreadsReachSafepoints(): Unit = {
    AssumesHelper.assumeSupportsVirtualThreads()
    assumeTrue(
      "requires Immix or Commix",
      LinktimeInfo.gc.isImmix || LinktimeInfo.gc.isCommix
    )
    val count = Math.max(8, Runtime.getRuntime().availableProcessors() * 4)
    val stop = new AtomicBoolean(false)
    val failure = new AtomicReference[java.lang.Throwable](null)
    val rounds = new AtomicInteger(0)

    val threads = Seq.fill(count) {
      Thread
        .ofVirtual()
        .start(new Runnable {
          def run(): Unit =
            try
              while (!stop.get()) {
                buildYieldAndCheck()
                rounds.incrementAndGet()
              }
            catch {
              case e: java.lang.Throwable => failure.compareAndSet(null, e)
            }
        })
    }

    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
    while (System.nanoTime() < deadline && failure.get() == null)
      System.gc()
    stop.set(true)
    threads.foreach { t =>
      t.join(TimeUnit.SECONDS.toMillis(30))
      assertFalse("virtual thread finished", t.isAlive())
    }

    if (failure.get() != null) throw failure.get()
    assertTrue("rounds", rounds.get() > 0)
  }
}

object VirtualThreadYieldpointStressTest {
  final class Node(
      val value: Int,
      val payload: scala.Array[Int],
      val next: Node
  )

  def buildYieldAndCheck(): Unit = {
    var head: Node = null
    var i = 0
    while (i < 200) {
      head = new Node(i, scala.Array.fill(4)(i), head)
      if (i % 50 == 0) Thread.`yield`()
      i += 1
    }
    var node = head
    var expected = 199
    while (node != null) {
      if (node.value != expected || node.payload.exists(_ != expected))
        throw new AssertionError(
          s"corrupted node: value=${node.value} expected=$expected"
        )
      expected -= 1
      node = node.next
    }
  }
}
