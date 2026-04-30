package org.scalanative.testsuite.javalib.lang

import java.lang.Thread
import java.time.Duration

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform._

class ThreadDurationTestOnJDK19 {
  @Test def sleepDurationIsNoOpForNegativeDuration(): Unit = {
    Thread.sleep(Duration.ofNanos(-1L))
  }

  @Test def joinDurationThrowsForUnstartedThread(): Unit = {
    val thread = new Thread(() => ())
    assertThrows(
      classOf[IllegalThreadStateException],
      thread.join(Duration.ofNanos(1L))
    )
  }

  @Test def joinDurationReturnsTrueForTerminatedThread(): Unit = {
    assumeTrue("requires multithreading", isMultithreadingEnabled)

    val thread = new Thread(() => ())
    thread.start()
    thread.join()

    assertTrue(thread.join(Duration.ofMillis(1L)))
  }
}
