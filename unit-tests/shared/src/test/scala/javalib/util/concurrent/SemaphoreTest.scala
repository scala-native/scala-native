/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package javalib.utils.concurrent

import java.util.{Collection, Collections}
import java.util.concurrent.Semaphore

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.junit.utils.AssertThrows.assertThrows

class SemaphoreTest {

  @Test def ctorUnfair(): Unit = {
    val sem = new Semaphore(1)
    assertFalse(sem.isFair())
  }

  @Test def ctorNegativePermits(): Unit = {
    val sem = new Semaphore(-1)
    assertEquals(-1, sem.availablePermits())
    assertFalse(sem.tryAcquire())
    sem.release()
    assertEquals(0, sem.availablePermits())
  }

  @Test def drain(): Unit = {
    val sem = new Semaphore(3)
    assertEquals(3, sem.drainPermits())
    assertEquals(0, sem.availablePermits())
  }

  @Test def drainNegative(): Unit = {
    val sem = new Semaphore(-3)
    assertEquals(-3, sem.drainPermits())
    assertEquals(0, sem.availablePermits())
  }

  @Test def tryAcquire(): Unit = {
    val sem = new Semaphore(1)
    assertTrue(sem.tryAcquire())
    assertEquals(0, sem.availablePermits())
    assertFalse(sem.tryAcquire())
    assertEquals(0, sem.availablePermits())
  }

  @Test def tryAcquirePermits(): Unit = {
    val sem = new Semaphore(5)
    assertTrue(sem.tryAcquire(3))
    assertEquals(2, sem.availablePermits())
    assertFalse(sem.tryAcquire(3))
    assertEquals(2, sem.availablePermits())
    assertTrue(sem.tryAcquire(2))
    assertEquals(0, sem.availablePermits())
    assertThrows(classOf[IllegalArgumentException], sem.tryAcquire(-1))
    assertEquals(0, sem.availablePermits())
  }

  @Test def release(): Unit = {
    val sem = new Semaphore(0)
    assertEquals(0, sem.availablePermits())
    sem.release()
    assertEquals(1, sem.availablePermits())
  }

  @Test def releasePermits(): Unit = {
    val sem = new Semaphore(1)
    assertEquals(1, sem.availablePermits())
    sem.release(2)
    assertEquals(3, sem.availablePermits())
    assertThrows(classOf[IllegalArgumentException], sem.release(-1))
    assertEquals(3, sem.availablePermits())
  }

  @Test def reducePermitsIntoNegative(): Unit = {
    class ReducibleSemaphore(permits: Int) extends Semaphore(permits) {
      // Simply expose the method.
      override def reducePermits(reduction: Int): Unit =
        super.reducePermits(reduction)
    }

    val sem = new ReducibleSemaphore(1)
    assertEquals(1, sem.availablePermits())
    assertTrue(sem.tryAcquire())
    assertFalse(sem.tryAcquire())
    assertEquals(0, sem.availablePermits())

    sem.reducePermits(2)
    assertEquals(-2, sem.availablePermits())
    assertFalse(sem.tryAcquire())

    sem.release(3)
    assertEquals(1, sem.availablePermits())

    assertThrows(classOf[IllegalArgumentException], sem.reducePermits(-1))
    assertEquals(1, sem.availablePermits())

    assertTrue(sem.tryAcquire())
  }

  @Test def queuedThreads(): Unit = {
    val sem = new Semaphore(0)

    assertFalse(sem.hasQueuedThreads())
    assertEquals(0, sem.getQueueLength())
  }

  @Test def overrideQueuedThreads(): Unit = {
    /* Check that the accessor methods *do not* delegate to `getQueuedThreads`.
     * See the comment in the implementation of Semaphore for why.
     */

    class EternallyQueuedSemaphore extends Semaphore(0) {
      override protected def getQueuedThreads(): Collection[Thread] =
        Collections.singleton(Thread.currentThread())
    }

    val sem = new EternallyQueuedSemaphore

    assertFalse(sem.hasQueuedThreads())
    assertEquals(0, sem.getQueueLength())
  }
}
