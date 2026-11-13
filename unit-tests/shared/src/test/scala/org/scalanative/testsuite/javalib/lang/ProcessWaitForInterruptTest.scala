package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scala.scalanative.junit.utils.AssumesHelper._

object ProcessWaitForInterruptTest {
  import ProcessUtils._

  type Wait = Process => Any

  val waitUntimed: Wait = _.waitFor()
  val waitTimed: Wait = _.waitFor(30, TimeUnit.SECONDS)

  private def withRunningProcess(body: Process => Unit): Unit = {
    val proc = processSleep(30).start()
    try body(proc)
    finally {
      proc.destroyForcibly()
      proc.waitFor(10, TimeUnit.SECONDS)
    }
  }

  def checkInterruptedBeforeWait(wait: Wait): Unit =
    withRunningProcess { proc =>
      Thread.currentThread().interrupt()
      assertThrows(classOf[InterruptedException], wait(proc))
      assertFalse("interrupt status cleared", Thread.interrupted())
      assertTrue("process still alive", proc.isAlive())
    }

  def checkInterruptedBeforeWaitOnExited(wait: Wait): Unit = {
    val proc = processSleep(0).start()
    assertTrue(proc.waitFor(10, TimeUnit.SECONDS))
    Thread.currentThread().interrupt()
    try wait(proc)
    finally assertTrue("interrupt status kept", Thread.interrupted())
  }

  def checkInterruptedDuringWait(
      newThread: Runnable => Thread,
      wait: Wait
  ): Unit = withRunningProcess { proc =>
    val result = new AtomicReference[Any]()
    val thread = newThread { () =>
      val res =
        try { wait(proc); "returned" }
        catch { case ex: InterruptedException => ex }
      result.set((res, Thread.currentThread().isInterrupted()))
    }
    thread.start()
    Thread.sleep(200)
    thread.interrupt()
    thread.join(10000)
    assertFalse("waiting thread did not finish", thread.isAlive())
    result.get() match {
      case (_: InterruptedException, interrupted) =>
        assertEquals("interrupt status cleared", false, interrupted)
      case other => fail(s"expected InterruptedException, got $other")
    }
    assertTrue("process still alive", proc.isAlive())
  }
}

class ProcessWaitForInterruptTest {
  import ProcessWaitForInterruptTest._

  @Test def waitForThrowsIfInterruptedBeforeWait(): Unit =
    checkInterruptedBeforeWait(waitUntimed)

  @Test def timedWaitForThrowsIfInterruptedBeforeWait(): Unit =
    checkInterruptedBeforeWait(waitTimed)

  @Test def zeroTimeoutWaitForIgnoresInterrupt(): Unit = {
    val proc = ProcessUtils.processSleep(30).start()
    try {
      Thread.currentThread().interrupt()
      assertFalse(proc.waitFor(0, TimeUnit.SECONDS))
      assertTrue("interrupt status kept", Thread.interrupted())
    } finally {
      proc.destroyForcibly()
      proc.waitFor(10, TimeUnit.SECONDS)
    }
  }

  @Test def waitForOnExitedProcessIgnoresInterrupt(): Unit = {
    checkInterruptedBeforeWaitOnExited(waitUntimed)
    checkInterruptedBeforeWaitOnExited(waitTimed)
  }

  @Test def waitForThrowsIfInterruptedDuringWait(): Unit = {
    assumeMultithreadingIsEnabled()
    checkInterruptedDuringWait(new Thread(_), waitUntimed)
  }

  @Test def timedWaitForThrowsIfInterruptedDuringWait(): Unit = {
    assumeMultithreadingIsEnabled()
    checkInterruptedDuringWait(new Thread(_), waitTimed)
  }
}
