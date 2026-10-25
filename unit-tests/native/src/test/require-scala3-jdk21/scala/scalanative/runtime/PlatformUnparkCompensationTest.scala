package scala.scalanative.runtime

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper

class PlatformUnparkCompensationTest {
  @Test def compensationMustNotRunWhileHoldingUnparkLocks(): Unit = {
    AssumesHelper.assumeSupportsVirtualThreads()
    val stop = new AtomicBoolean(false)
    val targets = scala.Array.tabulate(2) { _ =>
      val thread = new Thread(() => {
        while (!stop.get()) LockSupport.parkNanos(1000000L)
      })
      thread.setDaemon(true)
      thread.start()
      thread
    }
    val entered = new AtomicInteger()
    val waking = new AtomicInteger()
    val done = new CountDownLatch(2)
    val insideCompensation = new ThreadLocal[Boolean] {
      override def initialValue(): Boolean = false
    }
    def scheduler(index: Int) = new VirtualThreadScheduler {
      def execute(task: Runnable): Unit = {
        val carrier = new Thread(task)
        carrier.setDaemon(true)
        carrier.start()
      }
      def schedule(
          task: Runnable,
          delay: Long,
          unit: TimeUnit
      ): VirtualThreadScheduler.Cancellable =
        throw new UnsupportedOperationException()

      override def beginCarrierCompensatedBlock(carrier: Thread): Boolean = {
        if (!insideCompensation.get()) {
          insideCompensation.set(true)
          try {
            entered.incrementAndGet()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (entered.get() < 2 && System.nanoTime() < deadline)
              Thread.`yield`()
            // Wake the other worker, as carrier compensation would.
            waking.incrementAndGet()
            LockSupport.unpark(targets(1 - index))
          } finally insideCompensation.set(false)
        }
        false
      }
    }

    val previous = VirtualThread.defaultScheduler
    val threads = new scala.Array[Thread](2)
    try {
      for (i <- 0 until 2) {
        VirtualThread.setDefaultScheduler(scheduler(i))
        threads(i) = Thread
          .ofVirtual()
          .unstarted(() => {
            LockSupport.unpark(targets(i))
            // Check that compensation works again after unpark.
            val attempted = Blocker.begin()
            Blocker.end(attempted)
            done.countDown()
          })
      }
    } finally VirtualThread.setDefaultScheduler(previous)

    try {
      threads.foreach(_.start())
      val completed = done.await(10, TimeUnit.SECONDS)
      assertTrue(
        s"unpark deadlocked: ${entered.get()} compensation callbacks, " +
          s"${waking.get()} waking another worker",
        completed
      )
      assertEquals("ordinary blocking must still compensate", 2, entered.get())
    } finally stop.set(true)
  }
}
