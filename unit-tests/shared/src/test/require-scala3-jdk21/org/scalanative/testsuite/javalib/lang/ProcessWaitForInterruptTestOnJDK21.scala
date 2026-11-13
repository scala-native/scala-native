package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssumesHelper._

class ProcessWaitForInterruptTestOnJDK21 {
  import ProcessWaitForInterruptTest._

  private def onVirtualThread(body: => Unit): Unit = {
    val failure = new AtomicReference[Throwable]()
    val thread = Thread.ofVirtual().start { () =>
      try body
      catch { case ex: Throwable => failure.set(ex) }
    }
    thread.join(30000)
    assertFalse("virtual thread did not finish", thread.isAlive())
    if (failure.get() != null) throw failure.get()
  }

  @Test def waitForOnVirtualThreadThrowsIfInterruptedBeforeWait(): Unit = {
    assumeSupportsVirtualThreads()
    onVirtualThread {
      checkInterruptedBeforeWait(waitUntimed)
      checkInterruptedBeforeWait(waitTimed)
      checkInterruptedBeforeWaitOnExited(waitUntimed, observed = true)
    }
  }

  @Test def waitForOnVirtualThreadThrowsIfInterruptedDuringWait(): Unit = {
    assumeSupportsVirtualThreads()
    checkInterruptedDuringWait(Thread.ofVirtual().unstarted(_), waitUntimed)
    checkInterruptedDuringWait(Thread.ofVirtual().unstarted(_), waitTimed)
  }
}
