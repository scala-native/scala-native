package scala.scalanative.runtime.monitor

import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.{Intrinsics, MemoryLayout}

import Intrinsics._

class StringMonitorTest {
  private def strings: Seq[String] =
    Seq("thin string monitor", new String(Array('h', 'e', 'a', 'p')))

  private def lockWord(obj: AnyRef): LockWord =
    loadRawPtr(
      elemRawPtr(castObjectToRawPtr(obj), MemoryLayout.Object.LockWordOffset)
    )

  private def assumeMultithreading(): Unit =
    assumeTrue(LinktimeInfo.isMultithreadingEnabled)

  @Test def thinLocksAllowRecursionAndNotification(): Unit = {
    assumeMultithreading()
    strings.foreach { string =>
      string.synchronized {
        string.synchronized {
          string.notify()
          string.notifyAll()
          assertFalse(lockWord(string).isInflated)
        }
      }
      assertTrue(lockWord(string).isUnlocked)
    }
  }

  @Test def waitingCannotInflateStrings(): Unit = {
    assumeMultithreading()
    strings.foreach { string =>
      string.synchronized {
        assertThrows(classOf[IllegalStateException], string.wait(1))
        assertFalse(lockWord(string).isInflated)
        string.notifyAll()
      }
      assertTrue(lockWord(string).isUnlocked)
    }
  }

  @Test def contentionCannotInflateOrLeakOwnership(): Unit = {
    assumeMultithreading()
    strings.foreach { string =>
      val failure = new AtomicReference[Throwable]()
      val contender = new Thread(() => {
        try {
          string.synchronized(())
          failure.set(new AssertionError("contended string lock was accepted"))
        } catch {
          case _: IllegalStateException => ()
          case error: Throwable         => failure.set(error)
        }
      })
      contender.setDaemon(true)
      string.synchronized {
        contender.start()
        contender.join(5000)
        assertFalse("contender did not reject inflation", contender.isAlive())
        assertFalse(lockWord(string).isInflated)
      }
      assertNull(failure.get())
      assertTrue(
        "failed monitor entry leaked ownership",
        lockWord(string).isUnlocked
      )
      string.synchronized(string.notifyAll())
    }
  }

  @Test def recursionOverflowCannotInflateOrLeakOwnership(): Unit = {
    assumeMultithreading()
    def recurse(obj: AnyRef, depth: Int): Unit = obj.synchronized {
      if (depth > 0) recurse(obj, depth - 1)
    }
    strings.foreach { string =>
      assertThrows(classOf[IllegalStateException], recurse(string, 140))
      assertTrue(
        "failed recursive entry leaked ownership",
        lockWord(string).isUnlocked
      )
    }
    // Other object types retain the ordinary inflation path.
    val ordinary = new Object()
    recurse(ordinary, 140)
    assertTrue(lockWord(ordinary).isInflated)
  }
}
