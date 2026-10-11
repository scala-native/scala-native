package org.scalanative.testsuite.javalib.lang

import java.lang.Thread
import java.time.Duration

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scala.scalanative.junit.utils.AssumesHelper

class ThreadDurationTestOnJDK19 {
  @Test def sleepDurationThrowsNullPointerException(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      Thread.sleep(null.asInstanceOf[Duration])
    )
  }

  @Test def sleepDurationIsNoOpForNegativeDuration(): Unit = {
    Thread.sleep(Duration.ofNanos(-1L))
  }

  @Test def sleepDurationAcceptsZeroDuration(): Unit = {
    Thread.sleep(Duration.ZERO)
  }

  @Test def joinDurationThrowsNullPointerException(): Unit = {
    val thread = new Thread(() => ())
    assertThrows(
      classOf[NullPointerException],
      thread.join(null.asInstanceOf[Duration])
    )
  }

  @Test def joinDurationThrowsForUnstartedThread(): Unit = {
    val thread = new Thread(() => ())
    assertThrows(
      classOf[IllegalThreadStateException],
      thread.join(Duration.ofNanos(1L))
    )
  }

  @Test def joinDurationReturnsTrueForTerminatedThread(): Unit = {
    AssumesHelper.assumeMultithreadingIsEnabled()

    val thread = new Thread(() => ())
    thread.start()
    thread.join()

    assertTrue(thread.join(Duration.ofMillis(1L)))
  }
}
