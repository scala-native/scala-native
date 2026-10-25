package scala.scalanative.runtime

import scala.jdk.CollectionConverters._

import org.junit.Assert._
import org.junit.{BeforeClass, Test}

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadTimerTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeSupportsVirtualThreads()
}

class VirtualThreadTimerTest {
  @Test def timerThreadsAreShared(): Unit = {
    val cpus = Runtime.getRuntime().availableProcessors()
    val expected = Integer.highestOneBit(cpus / 4).max(1)
    // Sequential sleeps visit every round-robin timer queue, with no reliance
    // on the time taken to drain work or start a platform thread.
    for (_ <- 0 until cpus * 8) {
      val sleeper = Thread.ofVirtual().start(() => Thread.sleep(1))
      sleeper.join(10000)
      assertFalse("sleeper did not terminate", sleeper.isAlive())
    }
    val timerCount = NativeThread.Registry.aliveThreadsIterator.asScala.count(
      _.thread.getName() == "VirtualThread-unparker"
    )
    assertEquals(
      "timer threads should be shared by groups of CPUs",
      expected,
      timerCount
    )
  }
}
