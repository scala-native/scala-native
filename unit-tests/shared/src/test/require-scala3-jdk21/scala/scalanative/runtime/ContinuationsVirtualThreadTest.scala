package scala.scalanative.runtime

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isContinuationsSupported

class ContinuationsVirtualThreadTest:
  @Test def vtMonitorContention_5x50(): Unit =
    if isContinuationsSupported then {
      vtMonitorContentionImpl(5, 50)
    }

  @Test def vtMonitorContention_10x100(): Unit =
    if isContinuationsSupported then {
      vtMonitorContentionImpl(10, 100)
    }

  @Test def vtMonitorContention_15x100(): Unit =
    if isContinuationsSupported then {
      vtMonitorContentionImpl(15, 100)
    }

  @Test def vtMonitorContention_15x100_run2(): Unit =
    if isContinuationsSupported then {
      vtMonitorContentionImpl(15, 100)
    }

  @Test def vtMonitorContention_15x100_run3(): Unit =
    if isContinuationsSupported then {
      vtMonitorContentionImpl(15, 100)
    }

  @Test def vtMonitorContention_20x200(): Unit =
    if isContinuationsSupported then {
      vtMonitorContentionImpl(20, 200)
    }

  @Test def vtDeepStackInMonitor(): Unit =
    if isContinuationsSupported then {
      val depth = 100
      val threads = 8
      val iterations = 50
      val lock = new Object
      val latch = new CountDownLatch(threads)
      val errors = new AtomicInteger(0)

      @noinline def deepWork(d: Int, salt: Long): Long =
        val local = salt * 31 + d
        if d == 0 then lock.synchronized { local }
        else
          val sub = deepWork(d - 1, local)
          if local != salt * 31 + d then errors.incrementAndGet()
          sub + local

      for i <- 0 until threads do
        Thread.ofVirtual().name(s"deep-$i").start { () =>
          for _ <- 0 until iterations do deepWork(depth, i.toLong * 997)
          latch.countDown()
        }

      assertTrue(
        s"vtDeepStackInMonitor timed out; errors=${errors.get()}",
        latch.await(30, TimeUnit.SECONDS)
      )
      assertEquals("stack local corruption", 0, errors.get())
    }

  @Test def vtMixedContention(): Unit =
    if isContinuationsSupported then {
      val numVirtual = 10
      val numPlatform = 5
      val iterations = 200
      val total = numVirtual + numPlatform
      val counter = new AtomicInteger(0)
      val lock = new Object
      val latch = new CountDownLatch(total)

      for i <- 0 until numVirtual do
        Thread.ofVirtual().name(s"vt-$i").start { () =>
          for _ <- 0 until iterations do
            lock.synchronized { counter.incrementAndGet() }
          latch.countDown()
        }
      for i <- 0 until numPlatform do
        Thread.ofPlatform().daemon(true).name(s"pt-$i").start { () =>
          for _ <- 0 until iterations do
            lock.synchronized { counter.incrementAndGet() }
          latch.countDown()
        }

      assertTrue(
        s"vtMixedContention timed out; counter=${counter.get()}/${total * iterations}",
        latch.await(30, TimeUnit.SECONDS)
      )
      assertEquals("counter mismatch", total * iterations, counter.get())
    }

  @Test def vtRepeatedLockUnlock(): Unit =
    if isContinuationsSupported then {
      val lock = new Object
      val iterations = 2000
      val contenders = 4
      val latch = new CountDownLatch(1 + contenders)

      for i <- 0 until contenders do
        Thread.ofVirtual().name(s"bg-$i").start { () =>
          for _ <- 0 until iterations do lock.synchronized { Thread.`yield`() }
          latch.countDown()
        }

      Thread.ofVirtual().name("rapid").start { () =>
        for _ <- 0 until iterations do lock.synchronized { () }
        latch.countDown()
      }

      assertTrue(
        "vtRepeatedLockUnlock timed out",
        latch.await(30, TimeUnit.SECONDS)
      )
    }

  private def vtMonitorContentionImpl(numThreads: Int, iterations: Int): Unit =
    val counter = new AtomicInteger(0)
    val lock = new Object
    val latch = new CountDownLatch(numThreads)

    for i <- 0 until numThreads do
      Thread.ofVirtual().name(s"vt-$i").start { () =>
        for _ <- 0 until iterations do
          lock.synchronized { counter.incrementAndGet() }
        latch.countDown()
      }

    assertTrue(
      s"vtMonitorContention timed out; counter=${counter.get()}/${numThreads * iterations}",
      latch.await(30, TimeUnit.SECONDS)
    )
    assertEquals("counter mismatch", numThreads * iterations, counter.get())
