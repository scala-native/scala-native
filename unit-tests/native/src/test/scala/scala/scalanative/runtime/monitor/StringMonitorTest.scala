package scala.scalanative.runtime.monitor

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{CountDownLatch, TimeUnit}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.{Intrinsics, MemoryLayout}

import Intrinsics._

class StringMonitorTest {
  private def lockWord(obj: AnyRef): LockWord =
    loadRawPtr(
      elemRawPtr(castObjectToRawPtr(obj), MemoryLayout.Object.LockWordOffset)
    )

  @Test def thinLocksAllowRecursionAndNotification(): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    Seq("thin string monitor", new String(Array('h', 'e', 'a', 'p'))).foreach {
      string =>
        string.synchronized {
          string.synchronized {
            string.notifyAll()
            assertFalse(lockWord(string).isInflated)
          }
        }
        assertTrue(lockWord(string).isUnlocked)
    }
  }

  @Test def waitingInflatesStrings(): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    Seq("waiting string monitor", new String(Array('w', 'a', 'i', 't')))
      .foreach { string =>
        string.synchronized {
          string.wait(1)
          assertTrue(lockWord(string).isInflated)
          string.notifyAll()
        }
        string.synchronized(string.notifyAll())
      }
  }

  @Test def contentionInflatesStrings(): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    Seq("contended string monitor", new String(Array('l', 'o', 'c', 'k')))
      .foreach { string =>
        val ready = new CountDownLatch(1)
        val failure = new AtomicReference[Throwable]()
        val contender = new Thread(() => {
          ready.countDown()
          try string.synchronized(string.notifyAll())
          catch { case error: Throwable => failure.set(error) }
        })
        contender.setDaemon(true)
        string.synchronized {
          contender.start()
          assertTrue(ready.await(5, TimeUnit.SECONDS))
          // Waiting also inflates the monitor, allowing the contender to acquire it.
          string.wait(10)
        }
        contender.join(5000)
        assertFalse("contender did not finish", contender.isAlive())
        assertNull(failure.get())
        assertTrue(lockWord(string).isInflated)
      }
  }

  @Test def recursionOverflowInflatesStrings(): Unit = {
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)
    def recurse(obj: AnyRef, depth: Int): Unit = obj.synchronized {
      if (depth > 0) recurse(obj, depth - 1)
    }
    Seq("recursive string monitor", new String(Array('d', 'e', 'e', 'p')))
      .foreach { string =>
        recurse(string, 140)
        assertTrue(lockWord(string).isInflated)
        string.synchronized(string.notifyAll())
      }
  }
}
