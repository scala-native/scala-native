/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent

import java.util.concurrent.locks.AbstractQueuedSynchronizer

import scala.annotation.tailrec

import scala.scalanative.annotation.safePublish

object CountDownLatch {

  /** Synchronization control For CountDownLatch. Uses AQS state to represent
   *  count.
   */
  @SerialVersionUID(4982264981922014374L)
  private final class Sync(val count: Int) extends AbstractQueuedSynchronizer {
    setState(count)

    private[concurrent] def getCount() = getState()

    override protected def tryAcquireShared(acquires: Int): Int = {
      if (getState() == 0) 1
      else -1
    }

    override protected def tryReleaseShared(releases: Int): Boolean = { // Decrement count; signal when transition to zero
      @tailrec
      def loop(): Boolean = getState() match {
        case 0     => false
        case state =>
          val nextState = state - 1
          if (compareAndSetState(state, nextState)) {
            nextState == 0
          } else loop()
      }
      loop()
    }
  }
}

class CountDownLatch private (@safePublish sync: CountDownLatch.Sync) {
  def this(count: Int) = {
    this(sync = {
      if (count < 0) throw new IllegalArgumentException("count < 0")
      new CountDownLatch.Sync(count)
    })
  }

  @throws[InterruptedException]
  def await(): Unit = {
    sync.acquireSharedInterruptibly(1)
  }

  @throws[InterruptedException]
  def await(timeout: Long, unit: TimeUnit): Boolean =
    sync.tryAcquireSharedNanos(1, unit.toNanos(timeout))

  def countDown(): Unit = {
    sync.releaseShared(1)
  }

  def getCount(): Long = sync.getCount()

  override def toString(): String =
    super.toString + "[Count = " + sync.getCount() + "]"
}
