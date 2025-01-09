package org.scalanative.testsuite.javalib.lang

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.BeforeClass

import org.scalanative.testsuite.utils.Platform

import java.io.{PrintStream, File}

class StackOverflowTest {
  def stackoverflow(): Int = 1 + stackoverflow()

  @Test def catchStackOverflowError(): Unit = {
    assertThrows(classOf[StackOverflowError], () => stackoverflow())
  }

  @Test def entersFinallyBlocks(): Unit = {
    var visited = false

    assertThrows(
      classOf[StackOverflowError],
      () =>
        try stackoverflow()
        finally { visited = true }
    )
    assertTrue("finally block not visited", visited)
  }

  @Test def exitsMonitors(): Unit = {
    val devNull = new PrintStream(
      new File(if (Platform.isWindows) "NUL" else "/dev/null")
    )
    // println uses synchronized blocks (recursively)
    def stackoverflow(): Int = {
      devNull.println(".")
      1 + stackoverflow()
    }
    assertThrows(classOf[StackOverflowError], () => stackoverflow())
    assertFalse(Thread.holdsLock(devNull))
  }

  @Test def catchStackOverflowErrorInThread(): Unit = inThreadAwait(() =>
    assertThrows(classOf[StackOverflowError], () => stackoverflow())
  )

  @Test def entersFinallyBlocksInThread(): Unit = inThreadAwait(() => {
    var visited = false

    assertThrows(
      classOf[StackOverflowError],
      () =>
        try stackoverflow()
        finally { visited = true }
    )
    assertTrue("finally block not visited", visited)
  })

  @Test def exitsMonitorsInThread(): Unit = inThreadAwait(() => {
    val devNull = new PrintStream(
      new File(if (Platform.isWindows) "NUL" else "/dev/null")
    )
    // println uses synchronized blocks (recursively)
    def stackoverflow(): Int = {
      devNull.println(".")
      1 + stackoverflow()
    }
    assertThrows(classOf[StackOverflowError], () => stackoverflow())
    assertFalse(Thread.holdsLock(devNull))
  })

  private def inThreadAwait(task: Runnable): Unit = {
    assumeTrue("requires multithreading", Platform.isMultithreadingEnabled)
    val t = new Thread(task)
    t.start()
    t.join()
  }

}
