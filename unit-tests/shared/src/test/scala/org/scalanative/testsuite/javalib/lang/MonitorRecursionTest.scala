package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.Platform

class MonitorRecursionTest {

  @Test def aMonitorEnteredManyTimesIsHeldToTheOutermostExit(): Unit = {
    assumeTrue("needs threads", Platform.isMultithreadingEnabled)
    val lock = new Object
    val depth = 200
    val entered = new AtomicBoolean(false)
    val released = new CountDownLatch(1)
    def nested(n: Int): Unit =
      if (n > 0) lock.synchronized {
        nested(n - 1)
        assertTrue(
          s"not held after an exit at depth $n",
          Thread.holdsLock(lock)
        )
      }
    val other = new Thread(() => {
      released.await()
      lock.synchronized { entered.set(true) }
    })
    other.setDaemon(true)
    other.start()
    lock.synchronized {
      nested(depth)
      assertTrue("held", Thread.holdsLock(lock))
      released.countDown()
      Thread.sleep(100)
      assertFalse("another thread entered meanwhile", entered.get())
    }
    other.join(10000)
    assertTrue("the other thread did not enter after the exit", entered.get())
  }
}
