/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent
import java.util.Collection
import java.util.concurrent.locks.AbstractQueuedSynchronizer
import scala.annotation.tailrec
import scala.scalanative.annotation.safePublish

object Semaphore {

  /** Synchronization implementation for semaphore. Uses AQS state to represent
   *  permits. Subclassed into fair and nonfair versions.
   */
  @SerialVersionUID(1192457210091910933L)
  private[concurrent] abstract class Sync private[concurrent] (val permits: Int)
      extends AbstractQueuedSynchronizer {
    setState(permits)
    private[concurrent] final def getPermits(): Int = getState()
    @tailrec
    private[concurrent] final def nonfairTryAcquireShared(
        acquires: Int
    ): Int = {
      val available: Int = getState()
      val remaining: Int = available - acquires
      if (remaining < 0 || compareAndSetState(available, remaining)) remaining
      else nonfairTryAcquireShared(acquires)
    }

    @tailrec
    override protected final def tryReleaseShared(releases: Int): Boolean = {
      val current: Int = getState()
      val next: Int = current + releases
      if (next < current) { // overflow
        throw new Error("Maximum permit count exceeded")
      }
      if (compareAndSetState(current, next)) true
      else tryReleaseShared(releases)
    }

    @tailrec
    private[concurrent] final def reducePermits(reductions: Int): Unit = {
      val current: Int = getState()
      val next: Int = current - reductions
      if (next > current) { // underflow
        throw new Error("Permit count underflow")
      }
      if (!compareAndSetState(current, next))
        reducePermits(reductions)
    }

    @tailrec
    private[concurrent] final def drainPermits(): Int = {
      val current: Int = getState()
      if (current == 0 || compareAndSetState(current, 0)) current
      else drainPermits()
    }
  }

  /** NonFair version
   */
  @SerialVersionUID(-2694183684443567898L)
  private[concurrent] final class NonfairSync private[concurrent] (
      override val permits: Int
  ) extends Semaphore.Sync(permits) {
    override protected def tryAcquireShared(acquires: Int): Int =
      nonfairTryAcquireShared(acquires)
  }

  /** Fair version
   */
  @SerialVersionUID(2014338818796000944L)
  private[concurrent] final class FairSync private[concurrent] (
      override val permits: Int
  ) extends Semaphore.Sync(permits) {
    override protected def tryAcquireShared(acquires: Int): Int = {
      if (hasQueuedPredecessors()) -1
      else {
        val available: Int = getState()
        val remaining: Int = available - acquires
        if (remaining < 0 || compareAndSetState(available, remaining))
          remaining
        else tryAcquireShared(acquires)
      }
    }
  }
}

@SerialVersionUID(-3222578661600680210L)
class Semaphore private (@safePublish sync: Semaphore.Sync)
    extends Serializable {

  def this(permits: Int) = {
    this(sync = new Semaphore.NonfairSync(permits))
  }

  def this(permits: Int, fair: Boolean) = {
    this(
      sync =
        if (fair) new Semaphore.FairSync(permits)
        else new Semaphore.NonfairSync(permits)
    )
  }

  @throws[InterruptedException]
  def acquire(): Unit = sync.acquireSharedInterruptibly(1)

  def acquireUninterruptibly(): Unit = sync.acquireShared(1)

  def tryAcquire(): Boolean = sync.nonfairTryAcquireShared(1) >= 0

  @throws[InterruptedException]
  def tryAcquire(timeout: Long, unit: TimeUnit): Boolean =
    sync.tryAcquireSharedNanos(1, unit.toNanos(timeout))

  def release(): Unit = sync.releaseShared(1)

  @throws[InterruptedException]
  def acquire(permits: Int): Unit = {
    if (permits < 0) throw new IllegalArgumentException
    sync.acquireSharedInterruptibly(permits)
  }

  def acquireUninterruptibly(permits: Int): Unit = {
    if (permits < 0) throw new IllegalArgumentException
    sync.acquireShared(permits)
  }

  def tryAcquire(permits: Int): Boolean = {
    if (permits < 0) { throw new IllegalArgumentException }
    sync.nonfairTryAcquireShared(permits) >= 0
  }

  @throws[InterruptedException]
  def tryAcquire(permits: Int, timeout: Long, unit: TimeUnit): Boolean = {
    if (permits < 0) throw new IllegalArgumentException
    sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout))
  }

  def release(permits: Int): Unit = {
    if (permits < 0) throw new IllegalArgumentException
    sync.releaseShared(permits)
  }

  def availablePermits(): Int = sync.getPermits()

  def drainPermits(): Int = sync.drainPermits()

  protected def reducePermits(reduction: Int): Unit = {
    if (reduction < 0) { throw new IllegalArgumentException }
    sync.reducePermits(reduction)
  }

  def isFair(): Boolean = sync.isInstanceOf[Semaphore.FairSync]

  final def hasQueuedThreads(): Boolean = sync.hasQueuedThreads()

  final def getQueueLength(): Int = sync.getQueueLength()

  protected def getQueuedThreads(): Collection[Thread] = sync.getQueuedThreads()

  override def toString(): String = {
    super.toString() + "[Permits = " + sync.getPermits() + "]"
  }
}
