/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks

import java.util.ArrayList
import java.util.Collection
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RejectedExecutionException
import scala.annotation.tailrec
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import scala.scalanative.libc.atomic.{CAtomicInt, CAtomicLongLong, CAtomicRef}
import scala.scalanative.libc.atomic.memory_order._

@SerialVersionUID(7373984972572414692L)
object AbstractQueuedLongSynchronizer { // Node status bits, also used as argument and return values
  private[locks] val WAITING = 1 // must be 1
  private[locks] val CANCELLED = 0x80000000 // must be negative
  private[locks] val COND = 2 // in a condition wait

  abstract private[locks] class Node {
    var waiter: Thread = _ // visibly nonnull when enqueued
    @volatile var prev: Node = _ // initially attached via casTail
    @volatile var next: Node = _ // visibly nonnull when signallable
    @volatile var status: Int = 0 // written by owner, atomic bit ops by others

    private val prevAtomic = new CAtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "prev"))
    )
    private val nextAtomic = new CAtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
    )
    private val statusAtomic = new CAtomicInt(
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "status"))
    )

    // methods for atomic operations
    def casPrev(c: Node, v: Node): Boolean = // for cleanQueue
      prevAtomic.compareExchangeWeak(c, v)

    def casNext(c: Node, v: Node): Boolean = // for cleanQueue
      nextAtomic.compareExchangeWeak(c, v)

    def getAndUnsetStatus(v: Int): Int = // for signalling
      statusAtomic.fetchAnd(~v)

    def setPrevRelaxed(p: Node): Unit = // for off-queue assignment
      prevAtomic.store(p) // U.putObject

    def setStatusRelaxed(s: Int) = // for off-queue assignment
      statusAtomic.store(s) // U.putInt

    def clearStatus(): Unit = // for reducing unneeded signals
      statusAtomic.store(0, memory_order_relaxed) // U.putIntOpaque
  }

  // Concrete classes tagged by type
  final private[locks] class ExclusiveNode extends Node {}

  final private[locks] class SharedNode extends Node {}

  final private[locks] class ConditionNode
      extends Node
      with ForkJoinPool.ManagedBlocker {

    // link to next waiting node
    private[locks] var nextWaiter: ConditionNode = _

    override final def isReleasable(): Boolean =
      status <= 1 || Thread.currentThread().isInterrupted()

    override final def block(): Boolean = {
      while (!isReleasable()) LockSupport.park(this)
      true
    }
  }

  private def signalNext(h: Node): Unit =
    if (h != null) h.next match {
      case s: Node if s.status != 0 =>
        s.getAndUnsetStatus(WAITING)
        LockSupport.unpark(s.waiter)
      case _ => ()
    }

  private def signalNextIfShared(h: Node): Unit =
    if (h != null) h.next match {
      case s: SharedNode if s.status != 0 =>
        s.getAndUnsetStatus(WAITING)
        LockSupport.unpark(s.waiter)
      case _ => ()
    }

}

@SerialVersionUID(7373984972572414692L)
abstract class AbstractQueuedLongSynchronizer protected ()
    extends AbstractOwnableSynchronizer
    with Serializable {
  import AbstractQueuedLongSynchronizer._

  @volatile private var head: Node = _
  @volatile private var tail: Node = _
  @volatile private var state: Long = 0

  // Support for atomic ops
  private val headAtomic = new CAtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "head"))
  )
  private val tailAtomic = new CAtomicRef[Node](
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "tail"))
  )
  private val stateAtomic = new CAtomicLongLong(
    fromRawPtr(Intrinsics.classFieldRawPtr(this, "state"))
  )

  final protected def getState(): Long = state

  final protected def setState(newState: Long): Unit = state = newState

  final protected def compareAndSetState(c: Long, v: Long): Boolean =
    stateAtomic.compareExchangeStrong(c, v)

  private def casTail(c: Node, v: Node) =
    tailAtomic.compareExchangeStrong(c, v)

  private def tryInitializeHead(): Unit = {
    val h = new ExclusiveNode()
    val isInitialized = headAtomic.compareExchangeStrong(null: Node, h)
    if (isInitialized)
      tail = h
  }

  final private[locks] def enqueue(
      node: Node
  ): Unit = {
    @tailrec
    def tryEnqueue(): Unit = {
      val t = tail
      node.setPrevRelaxed(t) // avoid unnecessary fence
      t match {
        case null =>
          // initialize
          tryInitializeHead()
          tryEnqueue()

        case t if casTail(t, node) =>
          t.next = node
          if (t.status < 0) // wake up to clean link
            LockSupport.unpark(node.waiter)
        case _ => tryEnqueue()
      }
    }
    if (node != null) tryEnqueue()
  }

  final private[locks] def isEnqueued(
      node: Node
  ): Boolean = {
    @tailrec
    def checkLoop(t: Node): Boolean = {
      if (t == null) false
      else if (t eq node) true
      else checkLoop(t.prev)
    }
    checkLoop(tail)
  }

  final private[locks] def acquire(
      _node: Node,
      arg: Long,
      shared: Boolean,
      interruptible: Boolean,
      timed: Boolean,
      time: Long
  ): Long = {
    val current = Thread.currentThread()

    var node: Node = _node
    var spins, postSpins = 0 // retries upon unpark of first thread
    var interrupted, first = false
    var pred: Node = null // predecessor of node when enqueued

    /*
     * Repeatedly:
     *  Check if node now first
     *    if so, ensure head stable, else ensure valid predecessor
     *  if node is first or not yet enqueued, try acquiring
     *  else if node not yet created, create it
     *  else if not yet enqueued, try once to enqueue
     *  else if woken from park, retry (up to postSpins times)
     *  else if WAITING status not set, set and retry
     *  else park and clear WAITING status, and check cancellation
     */

    def getPred() = {
      pred = if (node != null) node.prev else null
      pred
    }
    def isFirst() = {
      first = head eq pred
      first
    }
    while (true) {
      var continue = false
      if (!first &&
          getPred() != null &&
          !isFirst()) {
        if (pred.status < 0) {
          cleanQueue()
          continue = true
        } else if (pred.prev == null) {
          Thread.onSpinWait()
          continue = true
        }
      }

      if (!continue) {
        if (first || pred == null) {
          val acquired =
            try
              if (shared) tryAcquireShared(arg) >= 0
              else tryAcquire(arg)
            catch {
              case ex: Throwable =>
                cancelAcquire(node, interrupted, false)
                throw ex
            }
          if (acquired) {
            if (first) {
              node.prev = null
              head = node
              pred.next = null
              node.waiter = null
              if (shared) signalNextIfShared(node)
              if (interrupted) current.interrupt()
            }
            return 1
          }
        }

        if (node == null) { // allocate; retry before enqueue
          node =
            if (shared) new SharedNode()
            else new ExclusiveNode()
        } else if (pred == null) { // try to enqueue
          node.waiter = current
          val t = tail
          node.setPrevRelaxed(t) // avoid unnecessary fence
          if (t == null) tryInitializeHead()
          else if (!casTail(t, node)) node.setPrevRelaxed(null) // back out
          else t.next = node
        } else if (first && spins != 0) {
          spins -= 1 // reduce unfairness on rewaits
          Thread.onSpinWait()
        } else if (node.status == 0)
          node.status = WAITING // enable signal and recheck
        else {
          postSpins = ((postSpins << 1) | 1).toByte
          spins = postSpins
          if (!timed) LockSupport.park(this)
          else {
            val nanos = time - System.nanoTime()
            if (nanos > 0L) LockSupport.parkNanos(this, nanos)
            else return cancelAcquire(node, interrupted, interruptible)
          }
          node.clearStatus()
          interrupted |= Thread.interrupted()
          if (interrupted && interruptible)
            return cancelAcquire(node, interrupted, interruptible)
        }
      }
    }
    -1 // unreachable
  }

  private def cleanQueue(): Unit = {
    while (true) {
      var break = false
      var n: Node = null
      var s: Node = null
      var q = tail
      // restart point
      while (!break) {
        val p = if (q != null) q.prev else null
        if (p == null) return () // end of list

        val isIncosisient =
          if (s == null) tail ne q
          else (s.prev ne q) || s.status < 0
        if (isIncosisient) break = true
        else if (q.status < 0) { // canceled
          val casNode =
            if (s == null) casTail(q, p)
            else s.casPrev(q, p)
          if (casNode && (q.prev eq p)) {
            p.casNext(q, s) // OK if fails
            if (p.prev == null)
              signalNext(p)
          }
          break = true
        } else {
          n = p.next
          if (n != q) { // help finish
            if (n != null && q.prev == p) {
              p.casNext(n, q)
              if (p.prev == null)
                signalNext(p)
            }
            break = true
          }
        }

        s = q
        q = q.prev
      }
    }
  }

  private def cancelAcquire(
      node: Node,
      interrupted: Boolean,
      interruptible: Boolean
  ): Int = {
    if (node != null) {
      node.waiter = null
      node.status = CANCELLED
      if (node.prev != null)
        cleanQueue()
    }

    if (interrupted) {
      if (interruptible) return CANCELLED
      else Thread.currentThread().interrupt()
    }
    0
  }

  protected def tryAcquire(arg: Long): Boolean =
    throw new UnsupportedOperationException

  protected def tryRelease(arg: Long): Boolean =
    throw new UnsupportedOperationException

  protected def tryAcquireShared(arg: Long): Long =
    throw new UnsupportedOperationException

  protected def tryReleaseShared(arg: Long): Boolean =
    throw new UnsupportedOperationException

  protected def isHeldExclusively(): Boolean =
    throw new UnsupportedOperationException

  final def acquire(arg: Long): Unit = {
    if (!tryAcquire(arg))
      acquire(null, arg, false, false, false, 0L)
  }

  @throws[InterruptedException]
  final def acquireInterruptibly(arg: Long): Unit = {
    if (Thread.interrupted() ||
        (!tryAcquire(arg) && acquire(null, arg, false, true, false, 0L) < 0))
      throw new InterruptedException
  }

  @throws[InterruptedException]
  final def tryAcquireNanos(arg: Long, nanosTimeout: Long): Boolean = {
    if (!Thread.interrupted()) {
      if (tryAcquire(arg)) return true
      if (nanosTimeout <= 0L) return false
      val stat =
        acquire(null, arg, false, true, true, System.nanoTime() + nanosTimeout)
      if (stat > 0) return true
      if (stat == 0) return false
    }
    throw new InterruptedException
  }

  final def release(arg: Long): Boolean = {
    if (tryRelease(arg)) {
      signalNext(head)
      true
    } else false
  }

  final def acquireShared(arg: Long): Unit = {
    if (tryAcquireShared(arg) < 0)
      acquire(null, arg, true, false, false, 0L)
  }

  @throws[InterruptedException]
  final def acquireSharedInterruptibly(arg: Long): Unit = {
    if (Thread.interrupted() || {
          tryAcquireShared(arg) < 0 &&
          acquire(null, arg, true, true, false, 0L) < 0
        }) {
      throw new InterruptedException
    }
  }

  @throws[InterruptedException]
  final def tryAcquireSharedNanos(arg: Long, nanosTimeout: Long): Boolean = {
    if (!Thread.interrupted()) {
      if (tryAcquireShared(arg) >= 0) true
      else if (nanosTimeout <= 0L) false
      else {
        val stat =
          acquire(null, arg, true, true, true, System.nanoTime() + nanosTimeout)
        if (stat > 0) true
        else if (stat == 0) false
        else throw new InterruptedException()
      }
    } else throw new InterruptedException()
  }

  final def releaseShared(arg: Long): Boolean = {
    if (tryReleaseShared(arg)) {
      signalNext(head)
      true
    } else false
  }

  final def hasQueuedThreads(): Boolean = {
    val h = head
    @tailrec
    def loop(p: Node): Boolean = {
      if ((p ne h) && p != null) {
        if (p.status >= 0) true
        else loop(p.prev)
      } else false
    }
    loop(tail)
  }

  final def hasContended(): Boolean = head != null

  final def getFirstQueuedThread(): Thread = {
    // traverse from tail on stale reads
    var first: Thread = null
    val h = head
    val s = if (h != null) h.next else null
    if (h != null && {
          s == null || { first = s.waiter; first == null } ||
          s.prev == null
        }) {

      // traverse from tail on stale reads
      var p = tail
      var q: Node = null
      while (p != null && { q = p.prev; q != null }) {
        val w = p.waiter
        if (w != null) first = w
        p = q
      }
    }
    first
  }

  final def isQueued(thread: Thread): Boolean = {
    if (thread == null) throw new NullPointerException
    var p = tail
    while (p != null) {
      if (p.waiter eq thread) return true
      p = p.prev
    }
    false
  }

  final private[locks] def apparentlyFirstQueuedIsExclusive() = {
    val h = head
    val s = if (h != null) h.next else null

    s != null &&
      !s.isInstanceOf[SharedNode] &&
      s.waiter != null
  }

  final def hasQueuedPredecessors(): Boolean = {
    val h = head
    val s = if (h != null) h.next else null
    var first = if (s != null) s.waiter else null
    if (h != null && (s == null ||
          first == null ||
          s.prev == null)) {
      first = getFirstQueuedThread()
    }
    first != null && (first ne Thread.currentThread())
  }

  final def getQueueLength(): Int = {
    def loop(p: Node, acc: Int): Int = {
      p match {
        case null => acc
        case p =>
          val n =
            if (p.waiter != null) acc + 1
            else acc
          loop(p.prev, n)
      }
    }
    loop(tail, 0)
  }

  private def getThreads(pred: Node => Boolean): Collection[Thread] = {
    val list = new ArrayList[Thread]
    var p = tail
    while (p != null) {
      if (pred(p)) {
        val t = p.waiter
        if (t != null) list.add(t)
      }
      p = p.prev
    }
    list
  }

  final def getQueuedThreads(): Collection[Thread] = getThreads(_ => true)

  final def getExclusiveQueuedThreads(): Collection[Thread] = getThreads { p =>
    !p.isInstanceOf[SharedNode]
  }

  final def getSharedQueuedThreads(): Collection[Thread] = getThreads {
    _.isInstanceOf[SharedNode]
  }

  override def toString(): String =
    super.toString() + "[State = " + getState() + ", " +
      (if (hasQueuedThreads()) "non" else "") + "empty queue]"

  final def owns(
      condition: AbstractQueuedLongSynchronizer#ConditionObject
  ): Boolean = condition.isOwnedBy(this)

  final def hasWaiters(
      condition: AbstractQueuedLongSynchronizer#ConditionObject
  ): Boolean = {
    if (!owns(condition)) throw new IllegalArgumentException("Not owner")
    condition.hasWaiters()
  }

  final def getWaitQueueLength(
      condition: AbstractQueuedLongSynchronizer#ConditionObject
  ): Int = {
    if (!owns(condition)) throw new IllegalArgumentException("Not owner")
    condition.getWaitQueueLength()
  }

  final def getWaitingThreads(
      condition: AbstractQueuedLongSynchronizer#ConditionObject
  ): Collection[Thread] = {
    if (!owns(condition)) throw new IllegalArgumentException("Not owner")
    condition.getWaitingThreads()
  }

  @SerialVersionUID(1173984872572414699L)
  class ConditionObject() extends Condition with Serializable {

    private var firstWaiter: ConditionNode = _

    private var lastWaiter: ConditionNode = _

    @tailrec
    private def doSignal(
        first: ConditionNode,
        all: Boolean
    ): Unit = {
      if (first != null) {
        val next = first.nextWaiter
        firstWaiter = next
        if (firstWaiter == null) lastWaiter = null
        if ((first.getAndUnsetStatus(COND) & COND) != 0) {
          enqueue(first)
          if (all) doSignal(next, all)
        }
      }
    }

    override final def signal(): Unit = {
      val first = firstWaiter
      if (!isHeldExclusively()) throw new IllegalMonitorStateException
      if (first != null) doSignal(first, false)
    }

    override final def signalAll(): Unit = {
      val first = firstWaiter
      if (!isHeldExclusively()) throw new IllegalMonitorStateException
      if (first != null) doSignal(first, true)
    }

    private def enableWait(node: ConditionNode): Long = {
      if (isHeldExclusively()) {
        node.waiter = Thread.currentThread()
        node.setStatusRelaxed(COND | WAITING)
        val last = lastWaiter
        if (last == null) firstWaiter = node
        else last.nextWaiter = node
        lastWaiter = node
        val savedState = getState()
        if (release(savedState))
          return savedState
      }
      node.status = CANCELLED // lock not held or inconsistent
      throw new IllegalMonitorStateException()
    }

    private def canReacquire(node: ConditionNode) = {
      // check links, not status to avoid enqueue race
      var p: Node = null
      node != null && {
        p = node.prev; p != null
      } && ((p.next eq node) || isEnqueued(node))
    }

    private def unlinkCancelledWaiters(
        node: ConditionNode
    ): Unit = {
      if (node == null || node.nextWaiter != null || (node eq lastWaiter)) {
        var w = firstWaiter
        var trail: ConditionNode = null

        while (w != null) {
          val next = w.nextWaiter
          if ((w.status & COND) == 0) {
            w.nextWaiter = null
            if (trail == null) firstWaiter = next
            else trail.nextWaiter = next
            if (next == null) lastWaiter = trail
          } else trail = w
          w = next
        }
      }
    }

    override final def awaitUninterruptibly(): Unit = {
      val node = new ConditionNode
      val savedState = enableWait(node)
      LockSupport.setCurrentBlocker(this) // for back-compatibility
      var interrupted, rejected = false
      while (!canReacquire(node)) {
        if (Thread.interrupted()) interrupted = true
        else if ((node.status & COND) != 0)
          try
            if (rejected) node.block()
            else ForkJoinPool.managedBlock(node)
          catch {
            case ex: RejectedExecutionException => rejected = true
            case ie: InterruptedException       => interrupted = true
          }
        else Thread.onSpinWait() // awoke while enqueuing
      }
      LockSupport.setCurrentBlocker(null)
      node.clearStatus()
      acquire(node, savedState, false, false, false, 0L)

      if (interrupted)
        Thread.currentThread().interrupt()
    }

    @throws[InterruptedException]
    override final def await(): Unit = {
      if (Thread.interrupted()) throw new InterruptedException

      val node = new ConditionNode
      val savedState = enableWait(node)
      LockSupport.setCurrentBlocker(this)
      var interrupted, cancelled, break, rejected = false
      while (!break && !canReacquire(node)) {
        interrupted |= Thread.interrupted()
        if (interrupted) {
          cancelled = (node.getAndUnsetStatus(COND) & COND) != 0
          if (cancelled) break = true
        } else if ((node.status & COND) != 0) { // else interrupted after signal
          try
            if (rejected) node.block()
            else ForkJoinPool.managedBlock(node)
          catch {
            case ex: RejectedExecutionException => rejected = true
            case ie: InterruptedException       => interrupted = true
          }
        } else Thread.onSpinWait() // awoke while enqueuing
      }

      LockSupport.setCurrentBlocker(null)
      node.clearStatus()
      acquire(node, savedState, false, false, false, 0L)
      if (interrupted) {
        if (cancelled) {
          unlinkCancelledWaiters(node)
          throw new InterruptedException
        }
        Thread.currentThread().interrupt()
      }
    }

    @throws[InterruptedException]
    override final def awaitNanos(nanosTimeout: Long): Long = {
      if (Thread.interrupted()) throw new InterruptedException

      val node = new ConditionNode()
      val savedState = enableWait(node)

      var nanos = nanosTimeout.max(0L)
      val deadline = System.nanoTime() + nanos

      var cancelled, interrupted, break = false
      while (!break && !canReacquire(node)) {
        interrupted |= Thread.interrupted()
        if (interrupted || {
              nanos = deadline - System.nanoTime()
              nanos <= 0L
            }) {
          cancelled = (node.getAndUnsetStatus(COND) & COND) != 0
          if (cancelled) break = true
        } else LockSupport.parkNanos(this, nanos)
      }

      node.clearStatus()
      acquire(node, savedState, false, false, false, 0L)

      if (cancelled) {
        unlinkCancelledWaiters(node)
        if (interrupted) throw new InterruptedException
      } else if (interrupted) Thread.currentThread().interrupt()

      val remaining = deadline - System.nanoTime() // avoid overflow
      if (remaining <= nanosTimeout) remaining
      else java.lang.Long.MIN_VALUE
    }

    @throws[InterruptedException]
    override final def awaitUntil(deadline: Date): Boolean = {
      val abstime = deadline.getTime()
      if (Thread.interrupted()) throw new InterruptedException

      val node = new ConditionNode
      val savedState = enableWait(node)

      var cancelled, interrupted, break = false
      while (!break && !canReacquire(node)) {
        interrupted |= Thread.interrupted()
        if (interrupted || System.currentTimeMillis() >= abstime) {
          cancelled = (node.getAndUnsetStatus(COND) & COND) != 0
          if (cancelled) break = true
        } else LockSupport.parkUntil(this, abstime)
      }

      node.clearStatus()
      acquire(node, savedState, false, false, false, 0L)
      if (cancelled) {
        unlinkCancelledWaiters(node)
        if (interrupted) throw new InterruptedException
      } else if (interrupted) Thread.currentThread().interrupt()

      !cancelled
    }

    @throws[InterruptedException]
    override final def await(time: Long, unit: TimeUnit): Boolean = {
      val nanosTimeout = unit.toNanos(time)
      if (Thread.interrupted()) throw new InterruptedException
      val node = new ConditionNode
      val savedState = enableWait(node)
      var nanos = nanosTimeout.max(0L)
      val deadline = System.nanoTime() + nanos

      var cancelled, interrupted, break = false
      while (!break && !canReacquire(node)) {
        interrupted |= Thread.interrupted()
        if (interrupted || {
              nanos = deadline - System.nanoTime()
              nanos <= 0L
            }) {
          cancelled = (node.getAndUnsetStatus(COND) & COND) != 0
          if (cancelled) break = true
        } else LockSupport.parkNanos(this, nanos)
      }

      node.clearStatus()
      acquire(node, savedState, false, false, false, 0L)
      if (cancelled) {
        unlinkCancelledWaiters(node)
        if (interrupted) throw new InterruptedException
      } else if (interrupted) Thread.currentThread().interrupt()
      !cancelled
    }

    final private[locks] def isOwnedBy(sync: AbstractQueuedLongSynchronizer) = {
      sync eq AbstractQueuedLongSynchronizer.this
    }

    final private[locks] def hasWaiters(): Boolean = {
      if (!isHeldExclusively()) throw new IllegalMonitorStateException

      var w = firstWaiter
      while (w != null) {
        if ((w.status & COND) != 0) return true
        w = w.nextWaiter
      }
      false
    }

    final private[locks] def getWaitQueueLength(): Int = {
      if (!isHeldExclusively())
        throw new IllegalMonitorStateException

      var n = 0
      var w = firstWaiter
      while (w != null) {
        if ((w.status & COND) != 0) n += 1
        w = w.nextWaiter
      }
      n
    }

    final private[locks] def getWaitingThreads(): Collection[Thread] = {
      if (!isHeldExclusively()) throw new IllegalMonitorStateException
      val list = new ArrayList[Thread]
      var w = firstWaiter
      while (w != null) {
        if ((w.status & COND) != 0) {
          val t = w.waiter
          if (t != null) list.add(t)
        }

        w = w.nextWaiter
      }
      list
    }
  }
}
