package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform._

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

  /* The JVM on Windows throws even when the process has already exited.
   * Elsewhere, and in Scala Native, waitFor returns and keeps the status.
   */
  def checkInterruptedBeforeWaitOnExited(wait: Wait, observed: Boolean) = {
    val proc = processForCommand("echo").start()
    if (observed) assertTrue(proc.waitFor(30, TimeUnit.SECONDS))
    else Thread.sleep(500)
    Thread.currentThread().interrupt()
    val res =
      try { wait(proc); "returned" }
      catch { case _: InterruptedException => "threw" }
    val interrupted = Thread.interrupted()
    if (executingInJVM && isWindows) {
      assertEquals("interrupt status", res == "returned", interrupted)
    } else {
      assertEquals("waitFor result", "returned", res)
      assertTrue("interrupt status kept", interrupted)
    }
    assertFalse("process alive", proc.isAlive())
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

  @Test def waitForOnExitedProcessIgnoresInterrupt(): Unit =
    for (observed <- Seq(true, false)) {
      checkInterruptedBeforeWaitOnExited(waitUntimed, observed)
      checkInterruptedBeforeWaitOnExited(waitTimed, observed)
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
