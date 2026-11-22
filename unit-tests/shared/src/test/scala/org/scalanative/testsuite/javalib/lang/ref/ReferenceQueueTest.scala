package org.scalanative.testsuite.javalib.lang.ref

import java.lang.ref._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ReferenceQueueTest {

  @Test def removeWithTimeoutOnEmptyQueueReturnsNull(): Unit = {
    val queue = new ReferenceQueue[AnyRef]
    @volatile var returned = 0
    val calls = 200
    val worker = new Thread(() =>
      while (returned < calls) {
        assertNull(queue.remove(returned % 20 + 1))
        returned += 1
      }
    )
    worker.setDaemon(true)
    worker.start()
    worker.join(30000)
    assertFalse(
      s"remove(timeout) hung after $returned of $calls calls",
      worker.isAlive()
    )
    assertEquals(calls, returned)
  }

  @Test def removeWithTimeoutWaitsAtLeastTheTimeout(): Unit = {
    val queue = new ReferenceQueue[AnyRef]
    val start = System.nanoTime()
    assertNull(queue.remove(50))
    val elapsedMillis = (System.nanoTime() - start) / 1000000L
    assertTrue(s"returned after $elapsedMillis ms", elapsedMillis >= 49)
  }

  @Test def removeWithHugeTimeoutWaitsForEnqueue(): Unit = {
    for (timeout <- Seq(Long.MaxValue, Long.MaxValue / 1000000L)) {
      val queue = new ReferenceQueue[AnyRef]
      val ref = new WeakReference[AnyRef](new Object, queue)
      @volatile var result: AnyRef = null
      @volatile var failure: Throwable = null
      val worker = new Thread(() =>
        try result = queue.remove(timeout)
        catch { case t: Throwable => failure = t }
      )
      worker.setDaemon(true)
      worker.start()
      worker.join(100)
      ref.enqueue()
      worker.join(10000)
      if (failure != null) throw failure
      assertFalse(s"remove($timeout) did not return", worker.isAlive())
      assertSame(s"remove($timeout)", ref, result)
    }
  }

  @Test def removeWithNegativeTimeoutThrows(): Unit = {
    val queue = new ReferenceQueue[AnyRef]
    assertThrows(classOf[IllegalArgumentException], queue.remove(-1))
  }
}
