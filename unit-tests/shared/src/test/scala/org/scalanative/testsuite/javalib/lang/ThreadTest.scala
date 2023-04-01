// Ported, with thanks & gratitude, from Scala.js
// 2020-09-20
// Scala.js Repository Info
//   commit: 9dc4d5b36ff2b2a3dfe2e91d5c6b1ef6d10d3e51
//   commit date: 2018-10-11
//
// Slightly modified for Scala Native.

package org.scalanative.testsuite.javalib.lang

import java.lang._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.Platform._
import scala.scalanative.junit.utils.AssumesHelper._

class ThreadTest {

  @Test def getNameAndSetName(): Unit = {
    val t = Thread.currentThread()
    val originalName = t.getName()
    val newName = "foo"
    assertNotEquals(newName, originalName) // default name of the main thread
    t.setName(newName)
    try assertEquals(newName, t.getName())
    // don't pollute the rest of the world with this test
    finally t.setName(originalName)
    assertEquals(originalName, t.getName())
  }

  @Test def currentThreadGetStackTrace(): Unit = {
    val trace = Thread.currentThread().getStackTrace()
    if (executingInScalaNative) {
      assertEquals(trace.length, 0)
    }
  }

  @deprecated
  @Test def getId(): Unit = {
    val id = Thread.currentThread().getId
    if (isMultithreadingEnabled) assertTrue(id >= 0)
    else assertEquals(0, id)
  }

  @Test def interruptExistAndTheStatusIsProperlyReflected(): Unit = {
    val t = Thread.currentThread()
    assertFalse(t.isInterrupted())
    assertFalse(Thread.interrupted())
    assertFalse(t.isInterrupted())
    t.interrupt()
    assertTrue(t.isInterrupted())
    assertTrue(Thread.interrupted())
    assertFalse(t.isInterrupted())
    assertFalse(Thread.interrupted())
  }

  @Test def sleepShouldSuspendForAtLeastSpecifiedMillis(): Unit = {
    val sleepForMillis = 10
    val start = System.currentTimeMillis()
    Thread.sleep(sleepForMillis)
    val elapsedMillis = System.currentTimeMillis() - start
    assertTrue("Slept for less then expected", elapsedMillis >= sleepForMillis)
  }

  @Test def sleepShouldSuspendForAtLeastSpecifiedNanos(): Unit = {
    if (isWindows) {
      // Behaviour for Thread.sleep(0, nanos) is not well documented on the JVM
      // when executing on Windows. Local tests have proven that sleep might
      // take undefined amount of time, in multiple cases less then expected.
      // In SN for Windows we assume minimal granuality of sleep to be 1ms
      assumeNotJVMCompliant()
    }
    val sleepForNanos = 500000 // 0.5ms
    val start = System.nanoTime()
    Thread.sleep(0, sleepForNanos)
    val elapsedNanos = System.nanoTime() - start
    assertTrue("Slept for less then expected", elapsedNanos >= sleepForNanos)
  }

  // Ported from JSR-166
  class MyHandler extends Thread.UncaughtExceptionHandler {
    override def uncaughtException(t: Thread, e: Throwable) = {
      e.printStackTrace()
    }
  }

  /** getUncaughtExceptionHandler returns ThreadGroup unless set, otherwise
   *  returning value of last setUncaughtExceptionHandler.
   */
  def testGetAndSetUncaughtExceptionHandler(): Unit = {
    // these must be done all at once to avoid state
    // dependencies across tests
    val current = Thread.currentThread()
    val tg = current.getThreadGroup()
    val eh = new MyHandler()
    assertSame(tg, current.getUncaughtExceptionHandler())
    current.setUncaughtExceptionHandler(eh)
    try assertSame(eh, current.getUncaughtExceptionHandler())
    finally current.setUncaughtExceptionHandler(null)
    assertSame(tg, current.getUncaughtExceptionHandler())
  }

  /** getDefaultUncaughtExceptionHandler returns value of last
   *  setDefaultUncaughtExceptionHandler.
   */
  @deprecated def testGetAndSetDefaultUncaughtExceptionHandler(): Unit = {
    assertNull(Thread.getDefaultUncaughtExceptionHandler())
    // failure due to SecurityException is OK.
    // Would be nice to explicitly test both ways, but cannot yet.
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    val eh = new MyHandler()
    try {
      Thread.setDefaultUncaughtExceptionHandler(eh)
      try assertSame(eh, Thread.getDefaultUncaughtExceptionHandler())
      finally Thread.setDefaultUncaughtExceptionHandler(defaultHandler)
    } catch {
      case ok: SecurityException =>
        assertNotNull(System.getSecurityManager())
    }
    assertSame(defaultHandler, Thread.getDefaultUncaughtExceptionHandler())
  }

}
