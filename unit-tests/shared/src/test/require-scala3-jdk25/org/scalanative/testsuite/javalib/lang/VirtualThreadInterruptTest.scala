package org.scalanative.testsuite.javalib.lang

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.locks.LockSupport

import org.junit.Assert._
import org.junit._

import scala.scalanative.junit.utils.AssumesHelper

object VirtualThreadInterruptTest {
  @BeforeClass def checkRuntime(): Unit =
    AssumesHelper.assumeMultithreadingIsEnabled()
}

class VirtualThreadInterruptTest {
  private val Timeout = 5000L

  @Test def interruptSetsFlag(): Unit = {
    val parked = new CountDownLatch(1)
    val flagSeen = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      flagSeen.set(Thread.currentThread().isInterrupted)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue("isInterrupted should be true after interrupt", flagSeen.get())
  }

  @Test def interruptDuringSleep(): Unit = {
    val started = new CountDownLatch(1)
    val threw = new AtomicBoolean(false)
    val interruptCleared = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      started.countDown()
      try Thread.sleep(5000)
      catch {
        case _: InterruptedException =>
          threw.set(true)
          interruptCleared.set(!Thread.currentThread().isInterrupted)
      }
    }
    assertTrue(started.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue("sleep should throw InterruptedException", threw.get())
    assertTrue(
      "interrupt status should be cleared after InterruptedException",
      interruptCleared.get()
    )
  }

  @Test def interruptDuringPark(): Unit = {
    val parked = new CountDownLatch(1)
    val interruptSet = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      interruptSet.set(Thread.currentThread().isInterrupted)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue(
      "park should return with interrupt status set (not thrown)",
      interruptSet.get()
    )
  }

  @Test def interruptDuringJoin(): Unit = {
    val blocker = new CountDownLatch(1)
    val target = Thread.ofVirtual().start { () =>
      blocker.await(Timeout, TimeUnit.MILLISECONDS)
    }
    val threw = new AtomicBoolean(false)
    val joiner = Thread.ofVirtual().start { () =>
      try target.join()
      catch {
        case _: InterruptedException => threw.set(true)
      }
    }
    Thread.sleep(50)
    joiner.interrupt()
    joiner.join(Timeout)
    blocker.countDown()
    target.join(Timeout)
    assertTrue(
      "join should throw InterruptedException on interrupt",
      threw.get()
    )
  }

  @Test def interruptDuringObjectWait(): Unit = {
    val lock = new Object
    val inWait = new CountDownLatch(1)
    val threw = new AtomicBoolean(false)
    val reacquired = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      lock.synchronized {
        inWait.countDown()
        try lock.wait()
        catch {
          case _: InterruptedException =>
            threw.set(true)
            reacquired.set(Thread.holdsLock(lock))
        }
      }
    }
    assertTrue(inWait.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue("Object.wait should throw InterruptedException", threw.get())
    assertTrue(
      "monitor should be re-acquired after InterruptedException",
      reacquired.get()
    )
  }

  @Test def interruptDuringLockSupportPark(): Unit = {
    val parked = new CountDownLatch(1)
    val interruptStatus = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      interruptStatus.set(Thread.currentThread().isInterrupted)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    vt.interrupt()
    vt.join(Timeout)
    assertTrue(
      "LockSupport.park should return with interrupt status set",
      interruptStatus.get()
    )
  }

  @Test def threadDotInterruptedClearsFlag(): Unit = {
    val result = new AtomicReference[Array[Boolean]]()
    val vt = Thread.ofVirtual().start { () =>
      Thread.currentThread().interrupt()
      val first = Thread.interrupted()
      val second = Thread.interrupted()
      result.set(Array(first, second))
    }
    vt.join(Timeout)
    val r = result.get()
    assertNotNull(r)
    assertTrue("first Thread.interrupted() should return true", r(0))
    assertFalse(
      "second Thread.interrupted() should return false (cleared)",
      r(1)
    )
  }

  @Test def selfInterrupt(): Unit = {
    val wasInterrupted = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      Thread.currentThread().interrupt()
      wasInterrupted.set(Thread.currentThread().isInterrupted)
    }
    vt.join(Timeout)
    assertTrue("self-interrupt should set interrupt flag", wasInterrupted.get())
  }

  @Test def interruptBeforeBlockingOperation(): Unit = {
    val threw = new AtomicBoolean(false)
    val lock = new Object
    val vt = Thread.ofVirtual().start { () =>
      Thread.currentThread().interrupt()
      lock.synchronized {
        try lock.wait()
        catch {
          case _: InterruptedException => threw.set(true)
        }
      }
    }
    vt.join(Timeout)
    assertTrue(
      "wait() should throw immediately if already interrupted",
      threw.get()
    )
  }

  @Test def interruptFromPlatformThread(): Unit = {
    val parked = new CountDownLatch(1)
    val interrupted = new AtomicBoolean(false)
    val vt = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      interrupted.set(Thread.currentThread().isInterrupted)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    // Interrupt from the current (platform) thread
    vt.interrupt()
    vt.join(Timeout)
    assertTrue(
      "platform thread should be able to interrupt VT",
      interrupted.get()
    )
  }

  @Test def interruptFromAnotherVirtualThread(): Unit = {
    val parked = new CountDownLatch(1)
    val interrupted = new AtomicBoolean(false)
    val target = Thread.ofVirtual().start { () =>
      parked.countDown()
      LockSupport.park()
      interrupted.set(Thread.currentThread().isInterrupted)
    }
    assertTrue(parked.await(Timeout, TimeUnit.MILLISECONDS))
    Thread.sleep(50)
    val interrupter = Thread.ofVirtual().start { () =>
      target.interrupt()
    }
    interrupter.join(Timeout)
    target.join(Timeout)
    assertTrue(
      "one VT should be able to interrupt another VT",
      interrupted.get()
    )
  }
}
