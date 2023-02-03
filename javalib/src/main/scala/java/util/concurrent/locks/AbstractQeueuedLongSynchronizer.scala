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
import java.util.concurrent.atomic.{AtomicReference, AtomicInteger}
import scala.scalanative.runtime.{fromRawPtr, Intrinsics}
import scala.scalanative.unsafe._
import scala.scalanative.libc.atomic.{CAtomicInt, CAtomicLongLong, CAtomicRef}
import scala.scalanative.libc.atomic.memory_order._

@SerialVersionUID(7373984972572414692L)
object AbstractQueuedLongSynchronizer { // Node status bits, also used as argument and return values
  private[locks] val WAITING = 1 // must be 1
  private[locks] val CANCELLED = 0x80000000 // must be negative
  private[locks] val COND = 2 // in a condition wait

  /** CLH Nodes */
  abstract private[locks] class Node {
    @volatile var waiter: Thread = _ // visibly nonnull when enqueued
    @volatile var prev: Node = _ // initially attached via casTail
    @volatile var next: Node = _ // visibly nonnull when signallable
    @volatile var status: Int = 0 // written by owner, atomic bit ops by others

    private val prevPtr: Ptr[Node] = fromRawPtr(
      Intrinsics.classFieldRawPtr(this, "prev")
    )
    private val prevAtomic = new CAtomicRef[Node](prevPtr)
    private val nextAtomic = new CAtomicRef[Node](
      fromRawPtr(Intrinsics.classFieldRawPtr(this, "next"))
    )
    private val statusPtr: Ptr[Int] = fromRawPtr(
      Intrinsics.classFieldRawPtr(this, "status")
    )
    private val statusAtomic = new CAtomicInt(statusPtr)

    // methods for atomic operations
    def casPrev(c: Node, v: Node): Boolean = // for cleanQueue
      prevAtomic.compareExchangeWeak(c, v)

    def casNext(c: Node, v: Node): Boolean = // for cleanQueue
      nextAtomic.compareExchangeWeak(c, v)

    def getAndUnsetStatus(v: Int): Int = // for signalling
      statusAtomic.fetchAnd(~v)

    def setPrevRelaxed(p: Node): Unit = // for off-queue assignment
      !prevPtr = p // U.putObject

    def setStatusRelaxed(s: Int) = // for off-queue assignment
      !statusPtr = s // U.putInt

    def clearStatus(): Unit = // for reducing unneeded signals
      statusAtomic.store(0, memory_order_relaxed) // U.putIntOpaque
  }

  // Concrete classes tagged by type
  final private[locks] class ExclusiveNode
      extends AbstractQueuedLongSynchronizer.Node {}

  final private[locks] class SharedNode
      extends AbstractQueuedLongSynchronizer.Node {}

  final private[locks] class ConditionNode
      extends AbstractQueuedLongSynchronizer.Node
      with ForkJoinPool.ManagedBlocker {

    // link to next waiting node
    private[locks] var nextWaiter: ConditionNode = _

    /** Allows Conditions to be used in ForkJoinPools without risking fixed pool
     *  exhaustion. This is usable only for untimed Condition waits, not timed
     *  versions.
     */
    override final def isReleasable(): Boolean =
      status <= 1 || Thread.currentThread().isInterrupted()

    override final def block(): Boolean = {
      while (!isReleasable()) LockSupport.park(this)
      true
    }
  }

  /** Wakes up the successor of given node, if one exists, and unsets its
   *  WAITING status to avoid park race. This may fail to wake up an eligible
   *  thread when one or more have been cancelled, but cancelAcquire ensures
   *  liveness.
   */
  private def signalNext(h: AbstractQueuedLongSynchronizer.Node): Unit =
    if (h != null) h.next match {
      case s: Node if s.status != 0 =>
        s.getAndUnsetStatus(WAITING)
        LockSupport.unpark(s.waiter)
      case _ => ()
    }

  /** Wakes up the given node if in shared mode */
  private def signalNextIfShared(h: Node): Unit =
    if (h != null) h.next match {
      case s: SharedNode if s.status != 0 =>
        s.getAndUnsetStatus(WAITING)
        LockSupport.unpark(s.waiter)
      case _ => ()
    }

}

/** Provides a framework for implementing blocking locks and related
 *  synchronizers (semaphores, events, etc) that rely on first-in-first-out
 *  (FIFO) wait queues. This class is designed to be a useful basis for most
 *  kinds of synchronizers that rely on a single atomic {@code int} value to
 *  represent state. Subclasses must define the protected methods that change
 *  this state, and which define what that state means in terms of this object
 *  being acquired or released. Given these, the other methods in this class
 *  carry out all queuing and blocking mechanics. Subclasses can maintain other
 *  state fields, but only the atomically updated {@code int} value manipulated
 *  using methods [[getState]], [[setState]] and [[compareAndSetState]] is
 *  tracked with respect to synchronization.
 *
 *  <p>Subclasses should be defined as non-public internal helper classes that
 *  are used to implement the synchronization properties of their enclosing
 *  class. Class {@code AbstractQueuedLongSynchronizer} does not implement any
 *  synchronization interface. Instead it defines methods such as
 *  [[acquireInterruptibly]] that can be invoked as appropriate by concrete
 *  locks and related synchronizers to implement their public methods.
 *
 *  <p>This class supports either or both a default <em>exclusive</em> mode and
 *  a <em>shared</em> mode. When acquired in exclusive mode, attempted acquires
 *  by other threads cannot succeed. Shared mode acquires by multiple threads
 *  may (but need not) succeed. This class does not &quot;understand&quot; these
 *  differences except in the mechanical sense that when a shared mode acquire
 *  succeeds, the next waiting thread (if one exists) must also determine
 *  whether it can acquire as well. Threads waiting in the different modes share
 *  the same FIFO queue. Usually, implementation subclasses support only one of
 *  these modes, but both can come into play for example in a [[ReadWriteLock]].
 *  Subclasses that support only exclusive or only shared modes need not define
 *  the methods supporting the unused mode.
 *
 *  <p>This class defines a nested [[ConditionObject]] class that can be used as
 *  a [[Condition]] implementation by subclasses supporting exclusive mode for
 *  which method [[isHeldExclusively]] reports whether synchronization is
 *  exclusively held with respect to the current thread, method [[release]]
 *  invoked with the current [[getState]] value fully releases this object, and
 *  [[acquire]], given this saved state value, eventually restores this object
 *  to its previous acquired state. No `AbstractQueuedLongSynchronizer` method
 *  otherwise creates such a condition, so if this constraint cannot be met, do
 *  not use it. The behavior of [[ConditionObject]] depends of course on the
 *  semantics of its synchronizer implementation.
 *
 *  <p>This class provides inspection, instrumentation, and monitoring methods
 *  for the internal queue, as well as similar methods for condition objects.
 *  These can be exported as desired into classes using an {@code
 *  AbstractQueuedLongSynchronizer} for their synchronization mechanics.
 *
 *  <p>Serialization of this class stores only the underlying atomic integer
 *  maintaining state, so deserialized objects have empty thread queues. Typical
 *  subclasses requiring serializability will define a {@code readObject} method
 *  that restores this to a known initial state upon deserialization.
 *
 *  <h2>Usage</h2>
 *
 *  <p>To use this class as the basis of a synchronizer, redefine the following
 *  methods, as applicable, by inspecting and/or modifying the synchronization
 *  state using [[getState]], [[setState]] and/or [[compareAndSetState]]:
 *
 *  <ul> <li>[[tryAcquire]] <li>[[tryRelease]] <li>[[tryAcquireShared]]
 *  <li>[[tryReleaseShared]] <li>[[isHeldExclusively]] </ul>
 *
 *  Each of these methods by default throws
 *  [[java.lang.UnsupportedOperationException]]. Implementations of these
 *  methods must be internally thread-safe, and should in general be short and
 *  not block. Defining these methods is the <em>only</em> supported means of
 *  using this class. All other methods are declared {@code final} because they
 *  cannot be independently varied.
 *
 *  <p>You may also find the inherited methods from
 *  [[AbstractOwnableSynchronizer]] useful to keep track of the thread owning an
 *  exclusive synchronizer. You are encouraged to use them
 *  -- this enables monitoring and diagnostic tools to assist users in
 *  determining which threads hold locks.
 *
 *  <p>Even though this class is based on an internal FIFO queue, it does not
 *  automatically enforce FIFO acquisition policies. The core of exclusive
 *  synchronization takes the form:
 *
 *  <pre> Acquire: while (!tryAcquire(arg)) { <em>enqueue thread if it is not
 *  already queued</em>; <em>possibly block current thread</em>; }
 *
 *  Release: if (tryRelease(arg)) <em>unblock the first queued thread</em>;
 *  </pre>
 *
 *  (Shared mode is similar but may involve cascading signals.)
 *
 *  <p id="barging">Because checks in acquire are invoked before enqueuing, a
 *  newly acquiring thread may <em>barge</em> ahead of others that are blocked
 *  and queued. However, you can, if desired, define {@code tryAcquire} and/or
 *  {@code tryAcquireShared} to disable barging by internally invoking one or
 *  more of the inspection methods, thereby providing a <em>fair</em> FIFO
 *  acquisition order. In particular, most fair synchronizers can define {@code
 *  tryAcquire} to return {@code false} if [[hasQueuedPredecessors]] (a method
 *  specifically designed to be used by fair synchronizers) returns {@code
 *  true}. Other variations are possible.
 *
 *  <p>Throughput and scalability are generally highest for the default barging
 *  (also known as <em>greedy</em>, <em>renouncement</em>, and
 *  <em>convoy-avoidance</em>) strategy. While this is not guaranteed to be fair
 *  or starvation-free, earlier queued threads are allowed to recontend before
 *  later queued threads, and each recontention has an unbiased chance to
 *  succeed against incoming threads. Also, while acquires do not
 *  &quot;spin&quot; in the usual sense, they may perform multiple invocations
 *  of {@code tryAcquire} interspersed with other computations before blocking.
 *  This gives most of the benefits of spins when exclusive synchronization is
 *  only briefly held, without most of the liabilities when it isn't. If so
 *  desired, you can augment this by preceding calls to acquire methods with
 *  "fast-path" checks, possibly prechecking [[hasContended]] and/or
 *  [[hasQueuedThreads]] to only do so if the synchronizer is likely not to be
 *  contended.
 *
 *  <p>This class provides an efficient and scalable basis for synchronization
 *  in part by specializing its range of use to synchronizers that can rely on
 *  {@code int} state, acquire, and release parameters, and an internal FIFO
 *  wait queue. When this does not suffice, you can build synchronizers from a
 *  lower level using [[java.util.concurrent.atomic atomic]] classes, your own
 *  custom [[java.util.Queue]] classes, and [[LockSupport]] blocking support.
 *
 *  <h2>Usage Examples</h2>
 *
 *  <p>Here is a non-reentrant mutual exclusion lock class that uses the value
 *  zero to represent the unlocked state, and one to represent the locked state.
 *  While a non-reentrant lock does not strictly require recording of the
 *  current owner thread, this class does so anyway to make usage easier to
 *  monitor. It also supports conditions and exposes some instrumentation
 *  methods:
 *
 *  <pre> {@code class Mutex implements Lock, java.io.Serializable {
 *
 *  // Our internal helper class private static class Sync extends
 *  AbstractQueuedLongSynchronizer { // Acquires the lock if state is zero
 *  public boolean tryAcquire(int acquires) { assert acquires == 1; // Otherwise
 *  unused if (compareAndSetState(0, 1)) {
 *  setExclusiveOwnerThread(Thread.currentThread()); return true; } return
 *  false; }
 *
 *  // Releases the lock by setting state to zero protected boolean
 *  tryRelease(int releases) { assert releases == 1; // Otherwise unused if
 *  (!isHeldExclusively()) throw new IllegalMonitorStateException();
 *  setExclusiveOwnerThread(null); setState(0); return true; }
 *
 *  // Reports whether in locked state public boolean isLocked() { return
 *  getState() != 0; }
 *
 *  public boolean isHeldExclusively() { // a data race, but safe due to
 *  out-of-thin-air guarantees return getExclusiveOwnerThread() ==
 *  Thread.currentThread(); }
 *
 *  // Provides a Condition public Condition newCondition() { return new
 *  ConditionObject(); }
 *
 *  // Deserializes properly private void readObject(ObjectInputStream s) throws
 *  IOException, ClassNotFoundException { s.defaultReadObject(); setState(0); //
 *  reset to unlocked state } }
 *
 *  // The sync object does all the hard work. We just forward to it. private
 *  final Sync sync = new Sync();
 *
 *  public void lock() { sync.acquire(1); } public boolean tryLock() { return
 *  sync.tryAcquire(1); } public void unlock() { sync.release(1); } public
 *  Condition newCondition() { return sync.newCondition(); } public boolean
 *  isLocked() { return sync.isLocked(); } public boolean
 *  isHeldByCurrentThread() { return sync.isHeldExclusively(); } public boolean
 *  hasQueuedThreads() { return sync.hasQueuedThreads(); } public void
 *  lockInterruptibly() throws InterruptedException {
 *  sync.acquireInterruptibly(1); } public boolean tryLock(long timeout,
 *  TimeUnit unit) throws InterruptedException { return sync.tryAcquireNanos(1,
 *  unit.toNanos(timeout)); } }}</pre>
 *
 *  <p>Here is a latch class that is like a
 *  [[java.util.concurrent.CountDownLatch CountDownLatch]] except that it only
 *  requires a single {@code signal} to fire. Because a latch is non-exclusive,
 *  it uses the {@code shared} acquire and release methods.
 *
 *  <pre> {@code class BooleanLatch {
 *
 *  private static class Sync extends AbstractQueuedLongSynchronizer { boolean
 *  isSignalled() { return getState() != 0; }
 *
 *  protected int tryAcquireShared(int ignore) { return isSignalled() ? 1 : -1;
 *  }
 *
 *  protected boolean tryReleaseShared(int ignore) { setState(1); return true; }
 *  }
 *
 *  private final Sync sync = new Sync(); public boolean isSignalled() { return
 *  sync.isSignalled(); } public void signal() { sync.releaseShared(1); } public
 *  void await() throws InterruptedException {
 *  sync.acquireSharedInterruptibly(1); } }}</pre>
 *
 *  @since 1.5
 *  @author
 *    Doug Lea
 */
@SerialVersionUID(7373984972572414692L)
abstract class AbstractQueuedLongSynchronizer protected ()
    extends AbstractOwnableSynchronizer
    with Serializable {
  import AbstractQueuedLongSynchronizer._

  /** Head of the wait queue, lazily initialized.
   */
  @volatile private var head: Node = _

  /** Tail of the wait queue. After initialization, modified only via casTail.
   */
  @volatile private var tail: Node = _

  /** The synchronization state.
   */
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

  /** Returns the current value of synchronization state. This operation has
   *  memory semantics of a {@code volatile} read.
   *  @return
   *    current state value
   */
  final protected def getState(): Long = state

  /** Sets the value of synchronization state. This operation has memory
   *  semantics of a {@code volatile} write.
   *  @param newState
   *    the new state value
   */
  final protected def setState(newState: Long): Unit = state = newState

  /** Atomically sets synchronization state to the given updated value if the
   *  current state value equals the expected value. This operation has memory
   *  semantics of a {@code volatile} read and write.
   *
   *  @param expect
   *    the expected value
   *  @param update
   *    the new value
   *  @return
   *    {@code true} if successful. False return indicates that the actual value
   *    was not equal to the expected value.
   */
  final protected def compareAndSetState(c: Long, v: Long): Boolean =
    stateAtomic.compareExchangeStrong(c, v)

  private def casTail(c: Node, v: Node) = tailAtomic.compareExchangeStrong(c, v)

  /** tries once to CAS a new dummy node for head */
  private def tryInitializeHead(): Unit = {
    val h = new AbstractQueuedLongSynchronizer.ExclusiveNode()
    val isInitialized = headAtomic.compareExchangeStrong(null: Node, h)
    if (isInitialized)
      tail = h
  }

  /** Enqueues the node unless null. (Currently used only for ConditionNodes;
   *  other cases are interleaved with acquires.)
   */
  final private[locks] def enqueue(
      node: AbstractQueuedLongSynchronizer.Node
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
          if (t.status < 0) { // wake up to clean link
            LockSupport.unpark(node.waiter)
          }
        case _ => tryEnqueue()
      }
    }
    if (node != null) tryEnqueue()
  }

  /** Returns true if node is found in traversal from tail */
  final private[locks] def isEnqueued(
      node: AbstractQueuedLongSynchronizer.Node
  ): Boolean = {
    @tailrec
    def checkLoop(t: AbstractQueuedLongSynchronizer.Node): Boolean = {
      if (t == null) false
      else if (t eq node) true
      else checkLoop(t.prev)
    }
    checkLoop(tail)
  }

  /** Main acquire method, invoked by all exported acquire methods.
   *
   *  @param node
   *    null unless a reacquiring Condition
   *  @param arg
   *    the acquire argument
   *  @param shared
   *    true if shared mode else exclusive
   *  @param interruptible
   *    if abort and return negative on interrupt
   *  @param timed
   *    if true use timed waits
   *  @param time
   *    if timed, the System.nanoTime value to timeout
   *  @return
   *    positive if acquired, 0 if timed out, negative if interrupted
   */
  final private[locks] def acquire(
      _node: AbstractQueuedLongSynchronizer.Node,
      arg: Long,
      shared: Boolean,
      interruptible: Boolean,
      timed: Boolean,
      time: Long
  ): Long = {
    val current = Thread.currentThread()

    var node: Node = _node
    var spins = 0
    var postSpins = 0 // retries upon unpark of first thread
    var interrupted = false
    var first = false
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
      var shouldReset = false
      if (!first &&
          getPred() != null &&
          !isFirst()) {
        if (pred.status < 0) {
          cleanQueue()
          shouldReset = true
        } else if (pred.prev == null) {
          Thread.onSpinWait()
          shouldReset = true
        }
      }

      if (!shouldReset) {
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

  /** Possibly repeatedly traverses from tail, unsplicing cancelled nodes until
   *  none are found. Unparks nodes that may have been relinked to be next
   *  eligible acquirer.
   */
  private def cleanQueue(): Unit = {
    var break = false
    while (!break) {
      // restart point
      var q = tail
      var s: Node = null
      var n: Node = null
      while (true) {
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
            if (p.prev == null) signalNext(p)
          }
          break = true
        } else {
          n = p.next
          if (n != q) { // help finish
            if (n != null && q.prev == p) {
              p.casNext(n, q)
              if (p.prev == null) signalNext(p)
            }
            break = true
          }
        }

        s = q
        q = q.prev
      }
    }
  }

  /** Cancels an ongoing attempt to acquire.
   *
   *  @param node
   *    the node (may be null if cancelled before enqueuing)
   *  @param interrupted
   *    true if thread interrupted
   *  @param interruptible
   *    if should report interruption vs reset
   */
  private def cancelAcquire(
      node: AbstractQueuedLongSynchronizer.Node,
      interrupted: Boolean,
      interruptible: Boolean
  ): Int = {
    if (node != null) {
      node.waiter = null
      node.status = CANCELLED
      if (node.prev != null) cleanQueue()
    }

    if (interrupted) {
      if (interruptible) return CANCELLED
      else Thread.currentThread().interrupt()
    }
    0
  }

  /** Attempts to acquire in exclusive mode. This method should query if the
   *  state of the object permits it to be acquired in the exclusive mode, and
   *  if so to acquire it.
   *
   *  <p>This method is always invoked by the thread performing acquire. If this
   *  method reports failure, the acquire method may queue the thread, if it is
   *  not already queued, until it is signalled by a release from some other
   *  thread. This can be used to implement method [[Lock.tryLock()*]].
   *
   *  <p>The default implementation throws
   *  [[java.lang.UnsupportedOperationException]].
   *
   *  @param arg
   *    the acquire argument. This value is always the one passed to an acquire
   *    method, or is the value saved on entry to a condition wait. The value is
   *    otherwise uninterpreted and can represent anything you like.
   *  @return
   *    {@code true} if successful. Upon success, this object has been acquired.
   *  @throws java.lang.IllegalMonitorStateException
   *    if acquiring would place this synchronizer in an illegal state. This
   *    exception must be thrown in a consistent fashion for synchronization to
   *    work correctly.
   *  @throws java.lang.UnsupportedOperationException
   *    if exclusive mode is not supported
   */
  protected def tryAcquire(arg: Long): Boolean =
    throw new UnsupportedOperationException

  /** Attempts to set the state to reflect a release in exclusive mode.
   *
   *  <p>This method is always invoked by the thread performing release.
   *
   *  <p>The default implementation throws
   *  [[java.lang.UnsupportedOperationException]].
   *
   *  @param arg
   *    the release argument. This value is always the one passed to a release
   *    method, or the current state value upon entry to a condition wait. The
   *    value is otherwise uninterpreted and can represent anything you like.
   *  @return
   *    {@code true} if this object is now in a fully released state, so that
   *    any waiting threads may attempt to acquire; and {@code false} otherwise.
   *  @throws java.lang.IllegalMonitorStateException
   *    if releasing would place this synchronizer in an illegal state. This
   *    exception must be thrown in a consistent fashion for synchronization to
   *    work correctly.
   *  @throws java.lang.UnsupportedOperationException
   *    if exclusive mode is not supported
   */
  protected def tryRelease(arg: Long): Boolean =
    throw new UnsupportedOperationException

  /** Attempts to acquire in shared mode. This method should query if the state
   *  of the object permits it to be acquired in the shared mode, and if so to
   *  acquire it.
   *
   *  <p>This method is always invoked by the thread performing acquire. If this
   *  method reports failure, the acquire method may queue the thread, if it is
   *  not already queued, until it is signalled by a release from some other
   *  thread.
   *
   *  <p>The default implementation throws
   *  [[java.lang.UnsupportedOperationException]].
   *
   *  @param arg
   *    the acquire argument. This value is always the one passed to an acquire
   *    method, or is the value saved on entry to a condition wait. The value is
   *    otherwise uninterpreted and can represent anything you like.
   *  @return
   *    a negative value on failure; zero if acquisition in shared mode
   *    succeeded but no subsequent shared-mode acquire can succeed; and a
   *    positive value if acquisition in shared mode succeeded and subsequent
   *    shared-mode acquires might also succeed, in which case a subsequent
   *    waiting thread must check availability. (Support for three different
   *    return values enables this method to be used in contexts where acquires
   *    only sometimes act exclusively.) Upon success, this object has been
   *    acquired.
   *  @throws java.lang.IllegalMonitorStateException
   *    if acquiring would place this synchronizer in an illegal state. This
   *    exception must be thrown in a consistent fashion for synchronization to
   *    work correctly.
   *  @throws java.lang.UnsupportedOperationException
   *    if shared mode is not supported
   */
  protected def tryAcquireShared(arg: Long): Long =
    throw new UnsupportedOperationException

  /** Attempts to set the state to reflect a release in shared mode.
   *
   *  <p>This method is always invoked by the thread performing release.
   *
   *  <p>The default implementation throws
   *  [[java.lang.UnsupportedOperationException]].
   *
   *  @param arg
   *    the release argument. This value is always the one passed to a release
   *    method, or the current state value upon entry to a condition wait. The
   *    value is otherwise uninterpreted and can represent anything you like.
   *  @return
   *    {@code true} if this release of shared mode may permit a waiting acquire
   *    (shared or exclusive) to succeed; and {@code false} otherwise
   *  @throws java.lang.IllegalMonitorStateException
   *    if releasing would place this synchronizer in an illegal state. This
   *    exception must be thrown in a consistent fashion for synchronization to
   *    work correctly.
   *  @throws java.lang.UnsupportedOperationException
   *    if shared mode is not supported
   */
  protected def tryReleaseShared(arg: Long): Boolean =
    throw new UnsupportedOperationException

  /** Returns {@code true} if synchronization is held exclusively with respect
   *  to the current (calling) thread. This method is invoked upon each call to
   *  a [[ConditionObject]] method.
   *
   *  <p>The default implementation throws
   *  [[java.lang.UnsupportedOperationException]]. This method is invoked
   *  internally only within [[ConditionObject]] methods, so need not be defined
   *  if conditions are not used.
   *
   *  @return
   *    {@code true} if synchronization is held exclusively; {@code false}
   *    otherwise
   *  @throws java.lang.UnsupportedOperationException
   *    if conditions are not supported
   */
  protected def isHeldExclusively(): Boolean =
    throw new UnsupportedOperationException

  /** Acquires in exclusive mode, ignoring interrupts. Implemented by invoking
   *  at least once [[tryAcquire]], returning on success. Otherwise the thread
   *  is queued, possibly repeatedly blocking and unblocking, invoking
   *  [[tryAcquire]] until success. This method can be used to implement method
   *  [[Lock#lock]].
   *
   *  @param arg
   *    the acquire argument. This value is conveyed to [[tryAcquire]] but is
   *    otherwise uninterpreted and can represent anything you like.
   */
  final def acquire(arg: Long): Unit = {
    if (!tryAcquire(arg)) acquire(null, arg, false, false, false, 0L)
  }

  /** Acquires in exclusive mode, aborting if interrupted. Implemented by first
   *  checking interrupt status, then invoking at least once [[tryAcquire]],
   *  returning on success. Otherwise the thread is queued, possibly repeatedly
   *  blocking and unblocking, invoking [[tryAcquire]] until success or the
   *  thread is interrupted. This method can be used to implement method
   *  [[Lock#lockInterruptibly]].
   *
   *  @param arg
   *    the acquire argument. This value is conveyed to [[tryAcquire]] but is
   *    otherwise uninterpreted and can represent anything you like.
   *  @throws java.lang.InterruptedException
   *    if the current thread is interrupted
   */
  @throws[InterruptedException]
  final def acquireInterruptibly(arg: Long): Unit = {
    if (Thread.interrupted() ||
        (!tryAcquire(arg) && acquire(null, arg, false, true, false, 0L) < 0))
      throw new InterruptedException
  }

  /** Attempts to acquire in exclusive mode, aborting if interrupted, and
   *  failing if the given timeout elapses. Implemented by first checking
   *  interrupt status, then invoking at least once [[tryAcquire]], returning on
   *  success. Otherwise, the thread is queued, possibly repeatedly blocking and
   *  unblocking, invoking [[tryAcquire]] until success or the thread is
   *  interrupted or the timeout elapses. This method can be used to implement
   *  method Lock#tryLock(Long,TimeUnit).
   *
   *  @param arg
   *    the acquire argument. This value is conveyed to [[tryAcquire]] but is
   *    otherwise uninterpreted and can represent anything you like.
   *  @param nanosTimeout
   *    the maximum number of nanoseconds to wait
   *  @return
   *    {@code true} if acquired; {@code false} if timed out
   *  @throws java.lang.InterruptedException
   *    if the current thread is interrupted
   */
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

  /** Releases in exclusive mode. Implemented by unblocking one or more threads
   *  if [[tryRelease]] returns true. This method can be used to implement
   *  method [[Lock#unlock]].
   *
   *  @param arg
   *    the release argument. This value is conveyed to [[tryRelease]] but is
   *    otherwise uninterpreted and can represent anything you like.
   *  @return
   *    the value returned from [[tryRelease]]
   */
  final def release(arg: Long): Boolean = {
    if (tryRelease(arg)) {
      AbstractQueuedLongSynchronizer.signalNext(head)
      true
    } else false
  }

  /** Acquires in shared mode, ignoring interrupts. Implemented by first
   *  invoking at least once [[tryAcquireShared]], returning on success.
   *  Otherwise the thread is queued, possibly repeatedly blocking and
   *  unblocking, invoking [[tryAcquireShared]] until success.
   *
   *  @param arg
   *    the acquire argument. This value is conveyed to [[tryAcquireShared]] but
   *    is otherwise uninterpreted and can represent anything you like.
   */
  final def acquireShared(arg: Long): Unit = {
    if (tryAcquireShared(arg) < 0) acquire(null, arg, true, false, false, 0L)
  }

  /** Acquires in shared mode, aborting if interrupted. Implemented by first
   *  checking interrupt status, then invoking at least once
   *  [[tryAcquireShared]], returning on success. Otherwise the thread is
   *  queued, possibly repeatedly blocking and unblocking, invoking
   *  [[tryAcquireShared]] until success or the thread is interrupted.
   *  @param arg
   *    the acquire argument. This value is conveyed to [[tryAcquireShared]] but
   *    is otherwise uninterpreted and can represent anything you like.
   *  @throws java.lang.InterruptedException
   *    if the current thread is interrupted
   */
  @throws[InterruptedException]
  final def acquireSharedInterruptibly(arg: Long): Unit = {
    if (Thread.interrupted() || {
          tryAcquireShared(arg) < 0 &&
          acquire(null, arg, true, true, false, 0L) < 0
        }) {
      throw new InterruptedException
    }
  }

  /** Attempts to acquire in shared mode, aborting if interrupted, and failing
   *  if the given timeout elapses. Implemented by first checking interrupt
   *  status, then invoking at least once [[tryAcquireShared]], returning on
   *  success. Otherwise, the thread is queued, possibly repeatedly blocking and
   *  unblocking, invoking [[tryAcquireShared]] until success or the thread is
   *  interrupted or the timeout elapses.
   *
   *  @param arg
   *    the acquire argument. This value is conveyed to [[tryAcquireShared]] but
   *    is otherwise uninterpreted and can represent anything you like.
   *  @param nanosTimeout
   *    the maximum number of nanoseconds to wait
   *  @return
   *    {@code true} if acquired; {@code false} if timed out
   *  @throws java.lang.InterruptedException
   *    if the current thread is interrupted
   */
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

  /** Releases in shared mode. Implemented by unblocking one or more threads if
   *  [[tryReleaseShared]] returns true.
   *
   *  @param arg
   *    the release argument. This value is conveyed to [[tryReleaseShared]] but
   *    is otherwise uninterpreted and can represent anything you like.
   *  @return
   *    the value returned from [[tryReleaseShared]]
   */
  final def releaseShared(arg: Long): Boolean = {
    if (tryReleaseShared(arg)) {
      AbstractQueuedLongSynchronizer.signalNext(head)
      true
    } else false
  }

  /** Queries whether any threads are waiting to acquire. Note that because
   *  cancellations due to interrupts and timeouts may occur at any time, a
   *  {@code true} return does not guarantee that any other thread will ever
   *  acquire.
   *
   *  @return
   *    {@code true} if there may be other threads waiting to acquire
   */
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

  /** Queries whether any threads have ever contended to acquire this
   *  synchronizer; that is, if an acquire method has ever blocked.
   *
   *  <p>In this implementation, this operation returns in constant time.
   *
   *  @return
   *    {@code true} if there has ever been contention
   */
  final def hasContended(): Boolean = head != null

  /** Returns the first (longest-waiting) thread in the queue, or {@code null}
   *  if no threads are currently queued.
   *
   *  <p>In this implementation, this operation normally returns in constant
   *  time, but may iterate upon contention if other threads are concurrently
   *  modifying the queue.
   *
   *  @return
   *    the first (longest-waiting) thread in the queue, or {@code null} if no
   *    threads are currently queued
   */
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

  /** Returns true if the given thread is currently queued.
   *
   *  <p>This implementation traverses the queue to determine presence of the
   *  given thread.
   *
   *  @param thread
   *    the thread
   *  @return
   *    {@code true} if the given thread is on the queue
   *  @throws java.lang.NullPointerException
   *    if the thread is null
   */
  final def isQueued(thread: Thread): Boolean = {
    if (thread == null) throw new NullPointerException
    var p = tail
    while ({ p != null }) {
      if (p.waiter eq thread) return true
      p = p.prev
    }
    false
  }

  /** Returns {@code true} if the apparent first queued thread, if one exists,
   *  is waiting in exclusive mode. If this method returns {@code true}, and the
   *  current thread is attempting to acquire in shared mode (that is, this
   *  method is invoked from [[tryAcquireShared]]) then it is guaranteed that
   *  the current thread is not the first queued thread. Used only as a
   *  heuristic in ReentrantReadWriteLock.
   */
  final private[locks] def apparentlyFirstQueuedIsExclusive() = {
    val isNotShared = for {
      h <- Option(head)
      s <- Option(h.next)
      _ <- Option(s.waiter)
    } yield !s.isInstanceOf[AbstractQueuedLongSynchronizer.SharedNode]

    isNotShared.getOrElse(false)
  }

  /** Queries whether any threads have been waiting to acquire longer than the
   *  current thread.
   *
   *  <p>An invocation of this method is equivalent to (but may be more
   *  efficient than): <pre> {@code getFirstQueuedThread() !=
   *  Thread.currentThread() && hasQueuedThreads()}</pre>
   *
   *  <p>Note that because cancellations due to interrupts and timeouts may
   *  occur at any time, a {@code true} return does not guarantee that some
   *  other thread will acquire before the current thread. Likewise, it is
   *  possible for another thread to win a race to enqueue after this method has
   *  returned {@code false}, due to the queue being empty.
   *
   *  <p>This method is designed to be used by a fair synchronizer to avoid <a
   *  href="AbstractQueuedLongSynchronizer.html#barging">barging</a>. Such a
   *  synchronizer's [[tryAcquire]] method should return {@code false}, and its
   *  [[tryAcquireShared]] method should return a negative value, if this method
   *  returns {@code true} (unless this is a reentrant acquire). For example,
   *  the {@code tryAcquire} method for a fair, reentrant, exclusive mode
   *  synchronizer might look like this:
   *
   *  <pre> {@code protected boolean tryAcquire(int arg) { if
   *  (isHeldExclusively()) { // A reentrant acquire; increment hold count
   *  return true; } else if (hasQueuedPredecessors()) { return false; } else {
   *  // try to acquire normally } }}</pre>
   *
   *  @return
   *    {@code true} if there is a queued thread preceding the current thread,
   *    and {@code false} if the current thread is at the head of the queue or
   *    the queue is empty
   *  @since 1.7
   */
  final def hasQueuedPredecessors(): Boolean = {
    val h = head
    val s = if (h != null) h.next else null
    val first = if (s != null) s.waiter else null
    val current =
      if (h != null && (s == null || first == null || s.prev == null)) {
        getFirstQueuedThread()
      } else first
    current != null && first != Thread.currentThread()
  }

  /** Returns an estimate of the number of threads waiting to acquire. The value
   *  is only an estimate because the number of threads may change dynamically
   *  while this method traverses internal data structures. This method is
   *  designed for use in monitoring system state, not for synchronization
   *  control.
   *
   *  @return
   *    the estimated number of threads waiting to acquire
   */
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

  /** Returns a collection containing threads that may be waiting to acquire.
   *  Because the actual set of threads may change dynamically while
   *  constructing this result, the returned collection is only a best-effort
   *  estimate. The elements of the returned collection are in no particular
   *  order. This method is designed to facilitate construction of subclasses
   *  that provide more extensive monitoring facilities.
   *
   *  @return
   *    the collection of threads
   */
  final def getQueuedThreads(): Collection[Thread] = getThreads(_ => true)

  /** Returns a collection containing threads that may be waiting to acquire in
   *  exclusive mode. This has the same properties as [[getQueuedThreads]]
   *  except that it only returns those threads waiting due to an exclusive
   *  acquire.
   *
   *  @return
   *    the collection of threads
   */
  final def getExclusiveQueuedThreads(): Collection[Thread] = getThreads { p =>
    !p.isInstanceOf[AbstractQueuedLongSynchronizer.SharedNode]
  }

  /** Returns a collection containing threads that may be waiting to acquire in
   *  shared mode. This has the same properties as [[getQueuedThreads]] except
   *  that it only returns those threads waiting due to a shared acquire.
   *
   *  @return
   *    the collection of threads
   */
  final def getSharedQueuedThreads(): Collection[Thread] = getThreads {
    _.isInstanceOf[AbstractQueuedLongSynchronizer.SharedNode]
  }

  /** Returns a string identifying this synchronizer, as well as its state. The
   *  state, in brackets, includes the String {@code "State ="} followed by the
   *  current value of [[getState]], and either {@code "nonempty"} or {@code
   *  "empty"} depending on whether the queue is empty.
   *
   *  @return
   *    a string identifying this synchronizer, as well as its state
   */
  override def toString(): String =
    super.toString() + "[State = " + getState() + ", " +
      (if (hasQueuedThreads()) "non" else "") + "empty queue]"

  /** Queries whether the given ConditionObject uses this synchronizer as its
   *  lock.
   *
   *  @param condition
   *    the condition
   *  @return
   *    {@code true} if owned
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  final def owns(
      condition: AbstractQueuedLongSynchronizer#ConditionObject
  ): Boolean = condition.isOwnedBy(this)

  /** Queries whether any threads are waiting on the given condition associated
   *  with this synchronizer. Note that because timeouts and interrupts may
   *  occur at any time, a {@code true} return does not guarantee that a future
   *  {@code signal} will awaken any threads. This method is designed primarily
   *  for use in monitoring of the system state.
   *
   *  @param condition
   *    the condition
   *  @return
   *    {@code true} if there are any waiting threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if exclusive synchronization is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this synchronizer
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  final def hasWaiters(
      condition: AbstractQueuedLongSynchronizer#ConditionObject
  ): Boolean = {
    if (!owns(condition)) throw new IllegalArgumentException("Not owner")
    condition.hasWaiters()
  }

  /** Returns an estimate of the number of threads waiting on the given
   *  condition associated with this synchronizer. Note that because timeouts
   *  and interrupts may occur at any time, the estimate serves only as an upper
   *  bound on the actual number of waiters. This method is designed for use in
   *  monitoring system state, not for synchronization control.
   *
   *  @param condition
   *    the condition
   *  @return
   *    the estimated number of waiting threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if exclusive synchronization is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this synchronizer
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  final def getWaitQueueLength(
      condition: AbstractQueuedLongSynchronizer#ConditionObject
  ): Int = {
    if (!owns(condition)) throw new IllegalArgumentException("Not owner")
    condition.getWaitQueueLength()
  }

  /** Returns a collection containing those threads that may be waiting on the
   *  given condition associated with this synchronizer. Because the actual set
   *  of threads may change dynamically while constructing this result, the
   *  returned collection is only a best-effort estimate. The elements of the
   *  returned collection are in no particular order.
   *
   *  @param condition
   *    the condition
   *  @return
   *    the collection of threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if exclusive synchronization is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this synchronizer
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  final def getWaitingThreads(
      condition: AbstractQueuedLongSynchronizer#ConditionObject
  ): Collection[Thread] = {
    if (!owns(condition)) throw new IllegalArgumentException("Not owner")
    condition.getWaitingThreads()
  }

  /** Condition implementation for a [[AbstractQueuedLongSynchronizer]] serving
   *  as the basis of a [[Lock]] implementation.
   *
   *  <p>Method documentation for this class describes mechanics, not behavioral
   *  specifications from the point of view of Lock and Condition users.
   *  Exported versions of this class will in general need to be accompanied by
   *  documentation describing condition semantics that rely on those of the
   *  associated {@code AbstractQueuedLongSynchronizer}.
   *
   *  <p>This class is Serializable, but all fields are transient, so
   *  deserialized conditions have no waiters.
   */
  @SerialVersionUID(1173984872572414699L)
  class ConditionObject() extends Condition with Serializable {

    /** First node of condition queue. */
    private var firstWaiter: ConditionNode = _

    /** Last node of condition queue. */
    private var lastWaiter: ConditionNode = _

    /** Removes and transfers one or all waiters to sync queue.
     */
    @tailrec
    private def doSignal(
        first: AbstractQueuedLongSynchronizer.ConditionNode,
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

    /** Moves the longest-waiting thread, if one exists, from the wait queue for
     *  this condition to the wait queue for the owning lock.
     *
     *  @throws java.lang.IllegalMonitorStateException
     *    if [[isHeldExclusively]] returns {@code false}
     */
    override final def signal(): Unit = {
      val first = firstWaiter
      if (!isHeldExclusively()) throw new IllegalMonitorStateException
      if (first != null) doSignal(first, false)
    }

    /** Moves all threads from the wait queue for this condition to the wait
     *  queue for the owning lock.
     *
     *  @throws java.lang.IllegalMonitorStateException
     *    if [[isHeldExclusively]] returns {@code false}
     */
    override final def signalAll(): Unit = {
      val first = firstWaiter
      if (!isHeldExclusively()) throw new IllegalMonitorStateException
      if (first != null) doSignal(first, true)
    }

    /** Adds node to condition list and releases lock.
     *
     *  @param node
     *    the node
     *  @return
     *    savedState to reacquire after wait
     */
    private def enableWait(
        node: AbstractQueuedLongSynchronizer.ConditionNode
    ): Long = {
      if (isHeldExclusively()) {
        node.waiter = Thread.currentThread()
        node.setStatusRelaxed(
          AbstractQueuedLongSynchronizer.COND | AbstractQueuedLongSynchronizer.WAITING
        )
        val last = lastWaiter
        if (last == null) firstWaiter = node
        else last.nextWaiter = node
        lastWaiter = node
        val savedState = getState()
        if (release(savedState)) return savedState
      }
      node.status = CANCELLED // lock not held or inconsistent
      throw new IllegalMonitorStateException()
    }

    /** Returns true if a node that was initially placed on a condition queue is
     *  now ready to reacquire on sync queue.
     *  @param node
     *    the node
     *  @return
     *    true if is reacquiring
     */
    private def canReacquire(
        node: AbstractQueuedLongSynchronizer.ConditionNode
    ) = { // check links, not status to avoid enqueue race
      node != null && node.prev != null && isEnqueued(node)
    }

    /** Unlinks the given node and other non-waiting nodes from condition queue
     *  unless already unlinked.
     */
    private def unlinkCancelledWaiters(
        node: AbstractQueuedLongSynchronizer.ConditionNode
    ): Unit = {
      if (node == null || node.nextWaiter != null || (node == lastWaiter)) {
        var w = firstWaiter
        var trail: AbstractQueuedLongSynchronizer.ConditionNode = null

        while (w != null) {
          val next = w.nextWaiter
          if ((w.status & AbstractQueuedLongSynchronizer.COND) == 0) {
            w.nextWaiter = null
            if (trail == null) firstWaiter = next
            else trail.nextWaiter = next
            if (next == null) lastWaiter = trail
          } else trail = w
          w = next
        }
      }
    }

    /** Implements uninterruptible condition wait. <ol> <li>Save lock state
     *  returned by [[getState]]. <li>Invoke [[release]] with saved state as
     *  argument, throwing IllegalMonitorStateException if it fails. <li>Block
     *  until signalled. <li>Reacquire by invoking specialized version of
     *  [[acquire]] with saved state as argument. </ol>
     */
    override final def awaitUninterruptibly(): Unit = {
      val node = new AbstractQueuedLongSynchronizer.ConditionNode
      val savedState = enableWait(node)

      var interrupted = false
      var rejected = false
      LockSupport.setCurrentBlocker(this) // for back-compatibility
      while (!canReacquire(node)) {
        if (Thread.interrupted()) interrupted = true
        else if ((node.status & COND) != 0)
          try ForkJoinPool.managedBlock(node)
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

    /** Implements interruptible condition wait. <ol> <li>If current thread is
     *  interrupted, throw InterruptedException. <li>Save lock state returned by
     *  [[getState]]. <li>Invoke [[release]] with saved state as argument,
     *  throwing IllegalMonitorStateException if it fails. <li>Block until
     *  signalled or interrupted. <li>Reacquire by invoking specialized version
     *  of [[acquire]] with saved state as argument. <li>If interrupted while
     *  blocked in step 4, throw InterruptedException. </ol>
     */
    @throws[InterruptedException]
    override final def await(): Unit = {
      if (Thread.interrupted()) throw new InterruptedException

      val node = new AbstractQueuedLongSynchronizer.ConditionNode
      val savedState = enableWait(node)
      LockSupport.setCurrentBlocker(this)
      var interrupted = false
      var cancelled = false
      var break = false
      while (!break && !canReacquire(node)) {
        interrupted |= Thread.interrupted()
        if (interrupted) {
          cancelled = (node.getAndUnsetStatus(COND) & COND) != 0
          if (cancelled) break = true
        } else if ((node.status & COND) != 0) { // else interrupted after signal
          try ForkJoinPool.managedBlock(node)
          catch { case ie: InterruptedException => interrupted = true }
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

    /** Implements timed condition wait. <ol> <li>If current thread is
     *  interrupted, throw InterruptedException. <li>Save lock state returned by
     *  [[getState]]. <li>Invoke [[release]] with saved state as argument,
     *  throwing IllegalMonitorStateException if it fails. <li>Block until
     *  signalled, interrupted, or timed out. <li>Reacquire by invoking
     *  specialized version of [[acquire]] with saved state as argument. <li>If
     *  interrupted while blocked in step 4, throw InterruptedException. </ol>
     */
    @throws[InterruptedException]
    override final def awaitNanos(nanosTimeout: Long): Long = {
      if (Thread.interrupted()) throw new InterruptedException

      val node = new AbstractQueuedLongSynchronizer.ConditionNode()
      val savedState = enableWait(node)

      var nanos = nanosTimeout.max(0L)
      val deadline = System.nanoTime() + nanos

      var cancelled = false
      var interrupted = false
      var break = false
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

    /** Implements absolute timed condition wait. <ol> <li>If current thread is
     *  interrupted, throw InterruptedException. <li>Save lock state returned by
     *  [[getState]]. <li>Invoke [[release]] with saved state as argument,
     *  throwing IllegalMonitorStateException if it fails. <li>Block until
     *  signalled, interrupted, or timed out. <li>Reacquire by invoking
     *  specialized version of [[acquire]] with saved state as argument. <li>If
     *  interrupted while blocked in step 4, throw InterruptedException. <li>If
     *  timed out while blocked in step 4, return false, else true. </ol>
     */
    @throws[InterruptedException]
    override final def awaitUntil(deadline: Date): Boolean = {
      val abstime = deadline.getTime()
      if (Thread.interrupted()) throw new InterruptedException

      val node = new AbstractQueuedLongSynchronizer.ConditionNode
      val savedState = enableWait(node)

      var cancelled = false
      var interrupted = false
      var break = false
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

    /** Implements timed condition wait. <ol> <li>If current thread is
     *  interrupted, throw InterruptedException. <li>Save lock state returned by
     *  [[getState]]. <li>Invoke [[release]] with saved state as argument,
     *  throwing IllegalMonitorStateException if it fails. <li>Block until
     *  signalled, interrupted, or timed out. <li>Reacquire by invoking
     *  specialized version of [[acquire]] with saved state as argument. <li>If
     *  interrupted while blocked in step 4, throw InterruptedException. <li>If
     *  timed out while blocked in step 4, return false, else true. </ol>
     */
    @throws[InterruptedException]
    override final def await(time: Long, unit: TimeUnit): Boolean = {
      val nanosTimeout = unit.toNanos(time)
      if (Thread.interrupted()) throw new InterruptedException
      val node = new AbstractQueuedLongSynchronizer.ConditionNode
      val savedState = enableWait(node)
      var nanos = nanosTimeout.max(0L)
      val deadline = System.nanoTime() + nanos

      var cancelled = false
      var interrupted = false
      var break = false
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

    /** Returns true if this condition was created by the given synchronization
     *  object.
     *
     *  @return
     *    {@code true} if owned
     */
    final private[locks] def isOwnedBy(sync: AbstractQueuedLongSynchronizer) = {
      sync eq AbstractQueuedLongSynchronizer.this
    }

    /** Queries whether any threads are waiting on this condition. Implements
     *  [[AbstractQueuedLongSynchronizer#hasWaiters(ConditionObject)]].
     *
     *  @return
     *    {@code true} if there are any waiting threads
     *  @throws java.lang.IllegalMonitorStateException
     *    if [[isHeldExclusively]] returns {@code false}
     */
    final private[locks] def hasWaiters(): Boolean = {
      if (!isHeldExclusively()) throw new IllegalMonitorStateException

      var w = firstWaiter
      while (w != null) {
        if ((w.status & COND) != 0) return true
        w = w.nextWaiter
      }
      false
    }

    /** Returns an estimate of the number of threads waiting on this condition.
     *  Implements
     *  [[AbstractQueuedLongSynchronizer#getWaitQueueLength(ConditionObject)]].
     *
     *  @return
     *    the estimated number of waiting threads
     *  @throws java.lang.IllegalMonitorStateException
     *    if [[isHeldExclusively]] returns {@code false}
     */
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

    /** Returns a collection containing those threads that may be waiting on
     *  this Condition. Implements
     *  [[AbstractQueuedLongSynchronizer#getWaitingThreads(ConditionObject)]].
     *
     *  @return
     *    the collection of threads
     *  @throws java.lang.IllegalMonitorStateException
     *    if [[isHeldExclusively]] returns {@code false}
     */
    final private[locks] def getWaitingThreads(): Collection[Thread] = {
      if (!isHeldExclusively()) throw new IllegalMonitorStateException
      val list = new ArrayList[Thread]
      var w = firstWaiter
      while (w != null) {
        if ((w.status & AbstractQueuedLongSynchronizer.COND) != 0) {
          val t = w.waiter
          if (t != null) list.add(t)
        }

        w = w.nextWaiter
      }
      list
    }
  }
}
