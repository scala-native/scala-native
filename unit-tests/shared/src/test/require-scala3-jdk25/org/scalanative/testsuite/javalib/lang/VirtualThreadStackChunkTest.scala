package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadStackChunkTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()
}

class VirtualThreadStackChunkTest {
  private val Timeout = 10000L

  @Test def mixedStackSizesCompleteAfterBlockingOnReentrantLock(): Unit = {
    val gate = new ReentrantLock()
    val threadCount = (Runtime.getRuntime().availableProcessors() max 2) * 2
    val ready = new CountDownLatch(threadCount)
    val threads = Array.tabulate(threadCount) { index =>
      Thread.ofVirtual().unstarted { () =>
        val depth = if ((index & 1) == 0) 300 else 1
        recurse(depth, ready, gate)
        ()
      }
    }

    try {
      gate.lock()
      threads.foreach(_.start())
      assertTrue(
        "all VTs should reach the contended lock",
        ready.await(Timeout, TimeUnit.MILLISECONDS)
      )
    } finally {
      if (gate.isHeldByCurrentThread()) {
        gate.unlock()
      }
      threads.foreach(_.join(Timeout))
    }

    threads.foreach { thread =>
      assertEquals(
        "all VTs should terminate after the lock is released",
        Thread.State.TERMINATED,
        thread.getState()
      )
    }
  }

  private def recurse(
      depth: Int,
      ready: CountDownLatch,
      gate: ReentrantLock
  ): Int = {
    val i1 = depth
    val i2 = i1 + 1
    val i3 = i2 + 1
    val i4 = i3 + 1
    val i5 = i4 + 1
    val i6 = i5 + 1
    val i7 = i6 + 1
    val ll = 2L * i1
    val ff = ll + 1.2f
    val dd = ff + 1.3d

    if (depth > 0) {
      recurse(depth - 1, ready, gate) +
        i1 + i2 + i3 + i4 + i5 + i6 + i7 +
        ll.toInt + ff.toInt + dd.toInt
    } else {
      ready.countDown()
      gate.lock()
      try {
        i1 + i2 + i3 + i4 + i5 + i6 + i7 + ll.toInt + ff.toInt + dd.toInt
      } finally {
        gate.unlock()
      }
    }
  }
}
