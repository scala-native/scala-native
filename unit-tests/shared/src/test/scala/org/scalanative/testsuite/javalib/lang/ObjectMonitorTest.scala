package org.scalanative.testsuite.javalib.lang

import org.junit.{BeforeClass, Test}
import org.junit.Assert._
import org.junit.Assume._
import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform
import scala.scalanative.junit.utils.AssumesHelper

object ObjectMonitorTest {
  @BeforeClass def checkRuntime(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()
  }
}

class ObjectMonitorTest {
  val availableCPU = java.lang.Runtime.getRuntime().availableProcessors()
  val testedThreads = Seq(1, 2, 3, availableCPU, availableCPU * 2).distinct
  val maxIterations = 100

  @Test def `should fairly acquire ownership in enter/exit`(): Unit = {
    @volatile var counter = 0
    val lock = new {}
    def lockedOrderedExec(threadId: Int, threadsCount: Int) =
      while (counter <= maxIterations) {
        lock.synchronized {
          if (counter % threadsCount == threadId)
            counter += 1
        }
      }

    for (threadsCount <- testedThreads) {
      counter = 0
      val threads = Seq.tabulate(threadsCount) { threadId =>
        simpleStartedThread(threadId.toString)(
          lockedOrderedExec(threadId, threadsCount)
        )
      }
      try
        waitWhenMakesProgress(
          "await synchronization cycles",
          maxIterations * 100 /*ms*/
        )(counter)(
          counter >= maxIterations
        )
      finally ensureTerminatesThreads(threads, lock)
    }
  }

  @Test def `should not deadlock in wait/notify`(): Unit = {
    @volatile var counter = 0
    val lock = new {}
    def lockedOrderedExec(threadId: Int, threadsCount: Int) =
      while (counter <= maxIterations) {
        lock.synchronized {
          if (counter % threadsCount != threadId) lock.wait()
          else counter += 1
          lock.notify()
        }
        // To mitigate effects of rentering the same thread every time by the same thread on the JVM
        Thread.`yield`
      }

    for (threadsCount <- testedThreads) {
      counter = 0
      val threads = Seq.tabulate(threadsCount) { threadId =>
        simpleStartedThread(threadId.toString)(
          lockedOrderedExec(threadId, threadsCount)
        )
      }
      try
        waitWhenMakesProgress(
          "await synchronization cycles",
          maxIterations * 100 /*ms*/
        )(counter)(
          counter >= maxIterations
        )
      finally ensureTerminatesThreads(threads, lock)
    }
  }

  @Test def `should not deadlock in wait/notifyAll`(): Unit = {
    @volatile var counter = 0
    val lock = new {}
    def lockedOrderedExec(threadId: Int, threadsCount: Int): Unit =
      lock.synchronized {
        while (counter <= maxIterations) {
          while (counter % threadsCount != threadId) lock.wait()
          counter += 1
          lock.notifyAll()
        }
      }

    for (threadsCount <- testedThreads) {
      counter = 0
      val threads = Seq.tabulate(threadsCount) { threadId =>
        simpleStartedThread(threadId.toString)(
          lockedOrderedExec(threadId, threadsCount)
        )
      }

      try
        waitWhenMakesProgress(
          "await synchronization cycles",
          maxIterations * 100 /*ms*/
        )(counter)(
          counter >= maxIterations
        )
      finally ensureTerminatesThreads(threads, lock)
    }
  }

  @Test def `keeps recursions track after wait when inflated`(): Unit = {
    @volatile var released = false
    @volatile var canRelease = false
    @volatile var done = false
    val lock = new {}
    val thread = simpleStartedThread("t1") {
      lock.synchronized {
        lock.synchronized {
          canRelease = true
          // Until this point lock should be not inflated
          while (!released) lock.wait()
        }
        lock.notify()
      }
      assertThrows(
        classOf[IllegalMonitorStateException],
        lock.notify()
      )
      done = true
    }
    simpleStartedThread("t2") {
      // Wait for inflation of object montior to start
      while (!canRelease) ()
      lock.synchronized {
        released = true
        lock.notify()
      }
    }
    thread.join(500)
    assertTrue("done", done)
  }

  @Test def `keeps recursions track after wait when already inflated`()
      : Unit = {
    @volatile var released = false
    @volatile var canRelease = false
    @volatile var done = false
    @volatile var startedThreads = 0
    val lock = new {}
    val thread = simpleStartedThread("t1") {
      // wait for start of t2 and inflation of object monitor
      startedThreads += 1
      while (startedThreads != 2) ()
      // should be inflated already
      lock.synchronized {
        lock.synchronized {
          canRelease = true
          while (!released) lock.wait()
        }
        lock.notify()
      }
      assertThrows(
        classOf[IllegalMonitorStateException],
        lock.notify()
      )
      done = true
    }

    simpleStartedThread("t2") {
      lock.synchronized {
        startedThreads += 1
        // Force inflation of object monitor
        lock.wait(10)
        while (startedThreads != 2 && !canRelease) lock.wait(10)
        released = true
        lock.notify()
      }
    }
    thread.join(500)
    assertTrue("done", done)
  }

  private def waitWhenMakesProgress[State](
      clue: => String,
      deadlineMillis: Long
  )(progressCheck: => State)(finishCondition: => Boolean) = {
    val deadline = System.currentTimeMillis() + deadlineMillis
    var lastState = progressCheck
    while ({
      if (System.currentTimeMillis() > deadline)
        fail(s"timeout waiting for condition: $clue")
      val state = progressCheck
      val hasMadeProgress = lastState != state
      lastState = state
      !finishCondition && hasMadeProgress
    }) Thread.sleep(100)
  }

  private def simpleStartedThread(label: String)(block: => Unit) = {
    val t = new Thread {
      override def run(): Unit = block
    }
    t.setName(label)
    t.start()
    t
  }

  private def ensureTerminatesThreads(
      threads: Seq[Thread],
      lock: AnyRef
  ): Unit = {
    var iteration = 0
    while (threads.exists(_.isAlive()) && iteration < 5) {
      iteration += 1
      lock.synchronized {
        lock.notifyAll()
      }
      Thread.sleep(iteration * 500)
    }
    if (threads.exists(_.isAlive())) {
      threads.foreach(t => if (t.isAlive()) t.interrupt())
      System.err.println(
        "Failed to gracefully terminate synchronized threads" +
          s"${threads.count(_.isAlive)}/${threads.size}"
      )
    }
  }

}
