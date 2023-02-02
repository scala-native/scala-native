/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util
package concurrent.locks

import java.util.concurrent.TimeUnit

@SerialVersionUID(7373984872572414699L)
object ReentrantLock {

  /** Base of synchronization control for this lock. Subclassed into fair and
   *  nonfair versions below. Uses AQS state to represent the number of holds on
   *  the lock.
   */
  @SerialVersionUID(-5179523762034025860L)
  abstract private[locks] class Sync extends AbstractQueuedSynchronizer {

    /** Performs non-fair tryLock().
     */
    final private[locks] def tryLock(): Boolean = {
      val current = Thread.currentThread()
      var c = getState()
      if (c == 0) {
        if (compareAndSetState(0, 1)) {
          setExclusiveOwnerThread(current)
          return true
        }
      } else if (getExclusiveOwnerThread() eq current) {
        c += 1
        if (c < 0) { // overflow
          throw new Error("Maximum lock count exceeded")
        }
        setState(c)
        return true
      }
      false
    }

    /** Checks for reentrancy and acquires if lock immediately available under
     *  fair vs nonfair rules. Locking methods perform initialTryLock() check
     *  before relaying to corresponding AQS acquire methods.
     */
    private[locks] def initialTryLock(): Boolean

    final private[locks] def lock(): Unit = {
      if (!initialTryLock()) acquire(1)
    }

    @throws[InterruptedException]
    final private[locks] def lockInterruptibly(): Unit = {
      if (Thread.interrupted()) throw new InterruptedException
      if (!initialTryLock()) acquireInterruptibly(1)
    }

    @throws[InterruptedException]
    final private[locks] def tryLockNanos(nanos: Long) = {
      if (Thread.interrupted()) throw new InterruptedException
      initialTryLock() || tryAcquireNanos(1, nanos)
    }

    override final protected def tryRelease(releases: Int): Boolean = {
      val c = getState() - releases
      if (getExclusiveOwnerThread() ne Thread.currentThread())
        throw new IllegalMonitorStateException
      val free = c == 0
      if (free) setExclusiveOwnerThread(null)
      setState(c)
      free
    }

    override final protected[ReentrantLock] def isHeldExclusively(): Boolean = {
      // While we must in general read state before owner,
      // we don't need to do so to check if current thread is owner
      getExclusiveOwnerThread() eq Thread.currentThread()
    }

    final private[locks] def newCondition() = new ConditionObject()
    final private[locks] def getOwner() = {
      if (getState() == 0) null
      else getExclusiveOwnerThread()
    }

    final private[locks] def getHoldCount() = {
      if (isHeldExclusively()) getState()
      else 0
    }

    final private[locks] def isLocked() = getState() != 0

    // /** Reconstitutes the instance from a stream (that is, deserializes it).
    //  */
    // @throws[java.io.IOException]
    // @throws[ClassNotFoundException]
    // private def readObject(s: ObjectInputStream): Unit = {
    //   s.defaultReadObject()
    //   setState(0) // reset to unlocked state
    // }
  }

  /** Sync object for non-fair locks
   */
  @SerialVersionUID(7316153563782823691L)
  final private[locks] class NonfairSync extends ReentrantLock.Sync {
    override final private[locks] def initialTryLock() = {
      val current = Thread.currentThread()
      if (compareAndSetState(0, 1)) { // first attempt is unguarded
        setExclusiveOwnerThread(current)
        true
      } else if (getExclusiveOwnerThread() eq current) {
        val c = getState() + 1
        if (c < 0) throw new Error("Maximum lock count exceeded")
        setState(c)
        true
      } else false
    }

    /** Acquire for non-reentrant cases after initialTryLock() prescreen
     */
    override final protected def tryAcquire(acquires: Int): Boolean = {
      if (getState() == 0 && compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(Thread.currentThread())
        true
      } else false
    }
  }

  /** Sync object for fair locks
   */
  @SerialVersionUID(-3000897897090466540L)
  final private[locks] class FairSync extends ReentrantLock.Sync {

    /** Acquires only if reentrant or queue is empty.
     */
    override final private[locks] def initialTryLock(): Boolean = {
      val current = Thread.currentThread()
      var c = getState()
      if (c == 0) {
        if (!hasQueuedThreads() && compareAndSetState(0, 1)) {
          setExclusiveOwnerThread(current)
          return true
        }
      } else if (getExclusiveOwnerThread() eq current) {
        if ({ c += 1; c } < 0) throw new Error("Maximum lock count exceeded")
        setState(c)
        return true
      }
      false
    }

    /** Acquires only if thread is first waiter or empty
     */
    override final protected def tryAcquire(acquires: Int): Boolean = {
      if (getState() == 0 &&
          !hasQueuedPredecessors() &&
          compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(Thread.currentThread())
        true
      } else false
    }
  }
}

/** A reentrant mutual exclusion [[Lock]] with the same basic behavior and
 *  semantics as the implicit monitor lock accessed using {@code synchronized}
 *  methods and statements, but with extended capabilities.
 *
 *  <p>A {@code ReentrantLock} is <em>owned</em> by the thread last successfully
 *  locking, but not yet unlocking it. A thread invoking {@code lock} will
 *  return, successfully acquiring the lock, when the lock is not owned by
 *  another thread. The method will return immediately if the current thread
 *  already owns the lock. This can be checked using methods
 *  [[isHeldByCurrentThread]], and [[getHoldCount]].
 *
 *  <p>The constructor for this class accepts an optional <em>fairness</em>
 *  parameter. When set {@code true}, under contention, locks favor granting
 *  access to the longest-waiting thread. Otherwise this lock does not guarantee
 *  any particular access order. Programs using fair locks accessed by many
 *  threads may display lower overall throughput (i.e., are slower; often much
 *  slower) than those using the default setting, but have smaller variances in
 *  times to obtain locks and guarantee lack of starvation. Note however, that
 *  fairness of locks does not guarantee fairness of thread scheduling. Thus,
 *  one of many threads using a fair lock may obtain it multiple times in
 *  succession while other active threads are not progressing and not currently
 *  holding the lock. Also note that the untimed [[tryLock()*]] method does not
 *  honor the fairness setting. It will succeed if the lock is available even if
 *  other threads are waiting.
 *
 *  <p>It is recommended practice to <em>always</em> immediately follow a call
 *  to {@code lock} with a {@code try} block, most typically in a before/after
 *  construction such as:
 *
 *  <pre> {@code class X { private final ReentrantLock lock = new
 *  ReentrantLock(); // ...
 *
 *  public void m() { lock.lock(); // block until condition holds try { // ...
 *  method body } finally { lock.unlock(); } } }}</pre>
 *
 *  <p>In addition to implementing the [[Lock]] interface, this class defines a
 *  number of {@code public} and {@code protected} methods for inspecting the
 *  state of the lock. Some of these methods are only useful for instrumentation
 *  and monitoring.
 *
 *  <p>Serialization of this class behaves in the same way as built-in locks: a
 *  deserialized lock is in the unlocked state, regardless of its state when
 *  serialized.
 *
 *  <p>This lock supports a maximum of 2147483647 recursive locks by the same
 *  thread. Attempts to exceed this limit result in [[java.lang.Error]] throws
 *  from locking methods.
 *
 *  @since 1.5
 *  @author
 *    Doug Lea
 */
@SerialVersionUID(7373984872572414699L)
class ReentrantLock private (sync: ReentrantLock.Sync)
    extends Lock
    with Serializable {

  /** Creates an instance of {@code ReentrantLock} with the given fairness
   *  policy.
   *
   *  @param fair
   *    {@code true} if this lock should use a fair ordering policy
   */
  def this(fair: Boolean) = {
    this(
      if (fair) new ReentrantLock.FairSync
      else new ReentrantLock.NonfairSync
    )
  }

  /** Creates an instance of {@code ReentrantLock}. This is equivalent to using
   *  {@code ReentrantLock(false)}.
   */
  def this() = this(false)

  /** Acquires the lock.
   *
   *  <p>Acquires the lock if it is not held by another thread and returns
   *  immediately, setting the lock hold count to one.
   *
   *  <p>If the current thread already holds the lock then the hold count is
   *  incremented by one and the method returns immediately.
   *
   *  <p>If the lock is held by another thread then the current thread becomes
   *  disabled for thread scheduling purposes and lies dormant until the lock
   *  has been acquired, at which time the lock hold count is set to one.
   */
  override def lock(): Unit = sync.lock()

  /** Acquires the lock unless the current thread is
   *  [[java.lang.Thread.interrupt interrupted]].
   *
   *  <p>Acquires the lock if it is not held by another thread and returns
   *  immediately, setting the lock hold count to one.
   *
   *  <p>If the current thread already holds this lock then the hold count is
   *  incremented by one and the method returns immediately.
   *
   *  <p>If the lock is held by another thread then the current thread becomes
   *  disabled for thread scheduling purposes and lies dormant until one of two
   *  things happens:
   *
   *  <ul>
   *
   *  <li>The lock is acquired by the current thread; or
   *
   *  <li>Some other thread {@linkplain java.lang.Thread.interrupt interrupts}
   *  the current thread.
   *
   *  </ul>
   *
   *  <p>If the lock is acquired by the current thread then the lock hold count
   *  is set to one.
   *
   *  <p>If the current thread:
   *
   *  <ul>
   *
   *  <li>has its interrupted status set on entry to this method; or
   *
   *  <li>is {@linkplain java.lang.Thread.interrupt interrupted} while acquiring
   *  the lock,
   *
   *  </ul>
   *
   *  then [[java.lang.InterruptedException]] is thrown and the current thread's
   *  interrupted status is cleared.
   *
   *  <p>In this implementation, as this method is an explicit interruption
   *  point, preference is given to responding to the interrupt over normal or
   *  reentrant acquisition of the lock.
   *
   *  @throws java.lang.InterruptedException
   *    if the current thread is interrupted
   */
  @throws[InterruptedException]
  override def lockInterruptibly(): Unit = sync.lockInterruptibly()

  /** Acquires the lock only if it is not held by another thread at the time of
   *  invocation.
   *
   *  <p>Acquires the lock if it is not held by another thread and returns
   *  immediately with the value {@code true}, setting the lock hold count to
   *  one. Even when this lock has been set to use a fair ordering policy, a
   *  call to {@code tryLock()} <em>will</em> immediately acquire the lock if it
   *  is available, whether or not other threads are currently waiting for the
   *  lock. This &quot;barging&quot; behavior can be useful in certain
   *  circumstances, even though it breaks fairness. If you want to honor the
   *  fairness setting for this lock, then use `tryLock(Long,TimeUnit)` which is
   *  almost equivalent (it also detects interruption).
   *
   *  <p>If the current thread already holds this lock then the hold count is
   *  incremented by one and the method returns {@code true}.
   *
   *  <p>If the lock is held by another thread then this method will return
   *  immediately with the value {@code false}.
   *
   *  @return
   *    {@code true} if the lock was free and was acquired by the current
   *    thread, or the lock was already held by the current thread; and {@code
   *    false} otherwise
   */
  override def tryLock(): Boolean = sync.tryLock()

  /** Acquires the lock if it is not held by another thread within the given
   *  waiting time and the current thread has not been
   *  [[java.lang.Thread.interrupt interrupted]].
   *
   *  <p>Acquires the lock if it is not held by another thread and returns
   *  immediately with the value {@code true}, setting the lock hold count to
   *  one. If this lock has been set to use a fair ordering policy then an
   *  available lock <em>will not</em> be acquired if any other threads are
   *  waiting for the lock. This is in contrast to the [[tryLock()*]] method. If
   *  you want a timed {@code tryLock()} that does permit barging on a fair lock
   *  then combine the timed and un-timed forms together:
   *
   *  <pre> {@code if (lock.tryLock() || lock.tryLock()(timeout, unit)) { ...
   *  }}</pre>
   *
   *  <p>If the current thread already holds this lock then the hold count is
   *  incremented by one and the method returns {@code true}.
   *
   *  <p>If the lock is held by another thread then the current thread becomes
   *  disabled for thread scheduling purposes and lies dormant until one of
   *  three things happens:
   *
   *  <ul>
   *
   *  <li>The lock is acquired by the current thread; or
   *
   *  <li>Some other thread {@linkplain java.lang.Thread.interrupt interrupts}
   *  the current thread; or
   *
   *  <li>The specified waiting time elapses
   *
   *  </ul>
   *
   *  <p>If the lock is acquired then the value {@code true} is returned and the
   *  lock hold count is set to one.
   *
   *  <p>If the current thread:
   *
   *  <ul>
   *
   *  <li>has its interrupted status set on entry to this method; or
   *
   *  <li>is {@linkplain java.lang.Thread.interrupt interrupted} while acquiring
   *  the lock,
   *
   *  </ul> then [[java.lang.InterruptedException]] is thrown and the current
   *  thread's interrupted status is cleared.
   *
   *  <p>If the specified waiting time elapses then the value {@code false} is
   *  returned. If the time is less than or equal to zero, the method will not
   *  wait at all.
   *
   *  <p>In this implementation, as this method is an explicit interruption
   *  point, preference is given to responding to the interrupt over normal or
   *  reentrant acquisition of the lock, and over reporting the elapse of the
   *  waiting time.
   *
   *  @param timeout
   *    the time to wait for the lock
   *  @param unit
   *    the time unit of the timeout argument
   *  @return
   *    {@code true} if the lock was free and was acquired by the current
   *    thread, or the lock was already held by the current thread; and {@code
   *    false} if the waiting time elapsed before the lock could be acquired
   *  @throws java.lang.InterruptedException
   *    if the current thread is interrupted
   *  @throws java.lang.NullPointerException
   *    if the time unit is null
   */
  @throws[InterruptedException]
  override def tryLock(timeout: Long, unit: TimeUnit): Boolean =
    sync.tryLockNanos(unit.toNanos(timeout))

  /** Attempts to release this lock.
   *
   *  <p>If the current thread is the holder of this lock then the hold count is
   *  decremented. If the hold count is now zero then the lock is released. If
   *  the current thread is not the holder of this lock then
   *  [[java.lang.IllegalMonitorStateException]] is thrown.
   *
   *  @throws java.lang.IllegalMonitorStateException
   *    if the current thread does not hold this lock
   */
  override def unlock(): Unit = { sync.release(1) }

  /** Returns a [[Condition]] instance for use with this [[Lock]] instance.
   *
   *  <p>The returned [[Condition]] instance supports the same usages as do the
   *  [[java.lang.Object]] monitor methods (wait, notify and notifyAll) when
   *  used with the built-in monitor lock.
   *
   *  <ul>
   *
   *  <li>If this lock is not held when any of the [[Condition]]
   *  [[Condition.await()* waiting]] or {@linkplain Condition#signal signalling}
   *  methods are called, then an [[java.lang.IllegalMonitorStateException]] is
   *  thrown.
   *
   *  <li>When the condition [[Condition.await()* waiting]] methods are called
   *  the lock is released and, before they return, the lock is reacquired and
   *  the lock hold count restored to what it was when the method was called.
   *
   *  <li>If a thread is {@linkplain java.lang.Thread.interrupt interrupted}
   *  while waiting then the wait will terminate, an
   *  [[java.lang.InterruptedException]] will be thrown, and the thread's
   *  interrupted status will be cleared.
   *
   *  <li>Waiting threads are signalled in FIFO order.
   *
   *  <li>The ordering of lock reacquisition for threads returning from waiting
   *  methods is the same as for threads initially acquiring the lock, which is
   *  in the default case not specified, but for <em>fair</em> locks favors
   *  those threads that have been waiting the longest.
   *
   *  </ul>
   *
   *  @return
   *    the Condition object
   */
  override def newCondition(): Condition = sync.newCondition()

  /** Queries the number of holds on this lock by the current thread.
   *
   *  <p>A thread has a hold on a lock for each lock action that is not matched
   *  by an unlock action.
   *
   *  <p>The hold count information is typically only used for testing and
   *  debugging purposes. For example, if a certain section of code should not
   *  be entered with the lock already held then we can assert that fact:
   *
   *  <pre> {@code class X { ReentrantLock lock = new ReentrantLock(); // ...
   *  public void m() { assert lock.getHoldCount() == 0; lock.lock(); try { //
   *  ... method body } finally { lock.unlock(); } } }}</pre>
   *
   *  @return
   *    the number of holds on this lock by the current thread, or zero if this
   *    lock is not held by the current thread
   */
  def getHoldCount(): Int = sync.getHoldCount()

  /** Queries if this lock is held by the current thread.
   *
   *  <p>Analogous to the [[java.lang.Thread.holdsLock]] method for built-in
   *  monitor locks, this method is typically used for debugging and testing.
   *  For example, a method that should only be called while a lock is held can
   *  assert that this is the case:
   *
   *  <pre> {@code class X { ReentrantLock lock = new ReentrantLock(); // ...
   *
   *  public void m() { assert lock.isHeldByCurrentThread(); // ... method body
   *  } }}</pre>
   *
   *  <p>It can also be used to ensure that a reentrant lock is used in a
   *  non-reentrant manner, for example:
   *
   *  <pre> {@code class X { ReentrantLock lock = new ReentrantLock(); // ...
   *
   *  public void m() { assert !lock.isHeldByCurrentThread(); lock.lock(); try {
   *  // ... method body } finally { lock.unlock(); } } }}</pre>
   *
   *  @return
   *    {@code true} if current thread holds this lock and {@code false}
   *    otherwise
   */
  def isHeldByCurrentThread(): Boolean = sync.isHeldExclusively()

  /** Queries if this lock is held by any thread. This method is designed for
   *  use in monitoring of the system state, not for synchronization control.
   *
   *  @return
   *    {@code true} if any thread holds this lock and {@code false} otherwise
   */
  def isLocked(): Boolean = sync.isLocked()

  /** Returns {@code true} if this lock has fairness set true.
   *
   *  @return
   *    {@code true} if this lock has fairness set true
   */
  final def isFair(): Boolean = sync.isInstanceOf[ReentrantLock.FairSync]

  /** Returns the thread that currently owns this lock, or {@code null} if not
   *  owned. When this method is called by a thread that is not the owner, the
   *  return value reflects a best-effort approximation of current lock status.
   *  For example, the owner may be momentarily {@code null} even if there are
   *  threads trying to acquire the lock but have not yet done so. This method
   *  is designed to facilitate construction of subclasses that provide more
   *  extensive lock monitoring facilities.
   *
   *  @return
   *    the owner, or {@code null} if not owned
   */
  protected def getOwner(): Thread = sync.getOwner()

  /** Queries whether any threads are waiting to acquire this lock. Note that
   *  because cancellations may occur at any time, a {@code true} return does
   *  not guarantee that any other thread will ever acquire this lock. This
   *  method is designed primarily for use in monitoring of the system state.
   *
   *  @return
   *    {@code true} if there may be other threads waiting to acquire the lock
   */
  final def hasQueuedThreads(): Boolean = sync.hasQueuedThreads()

  /** Queries whether the given thread is waiting to acquire this lock. Note
   *  that because cancellations may occur at any time, a {@code true} return
   *  does not guarantee that this thread will ever acquire this lock. This
   *  method is designed primarily for use in monitoring of the system state.
   *
   *  @param thread
   *    the thread
   *  @return
   *    {@code true} if the given thread is queued waiting for this lock
   *  @throws java.lang.NullPointerException
   *    if the thread is null
   */
  final def hasQueuedThread(thread: Thread): Boolean = sync.isQueued(thread)

  /** Returns an estimate of the number of threads waiting to acquire this lock.
   *  The value is only an estimate because the number of threads may change
   *  dynamically while this method traverses internal data structures. This
   *  method is designed for use in monitoring system state, not for
   *  synchronization control.
   *
   *  @return
   *    the estimated number of threads waiting for this lock
   */
  final def getQueueLength(): Int = sync.getQueueLength()

  /** Returns a collection containing threads that may be waiting to acquire
   *  this lock. Because the actual set of threads may change dynamically while
   *  constructing this result, the returned collection is only a best-effort
   *  estimate. The elements of the returned collection are in no particular
   *  order. This method is designed to facilitate construction of subclasses
   *  that provide more extensive monitoring facilities.
   *
   *  @return
   *    the collection of threads
   */
  protected def getQueuedThreads(): Collection[Thread] = sync.getQueuedThreads()

  /** Queries whether any threads are waiting on the given condition associated
   *  with this lock. Note that because timeouts and interrupts may occur at any
   *  time, a {@code true} return does not guarantee that a future {@code
   *  signal} will awaken any threads. This method is designed primarily for use
   *  in monitoring of the system state.
   *
   *  @param condition
   *    the condition
   *  @return
   *    {@code true} if there are any waiting threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if this lock is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this lock
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  def hasWaiters(condition: Condition): Boolean = condition match {
    case null => throw new NullPointerException()
    case cond: AbstractQueuedSynchronizer#ConditionObject =>
      sync.hasWaiters(cond)
    case _ => throw new IllegalArgumentException("not owner")
  }

  /** Returns an estimate of the number of threads waiting on the given
   *  condition associated with this lock. Note that because timeouts and
   *  interrupts may occur at any time, the estimate serves only as an upper
   *  bound on the actual number of waiters. This method is designed for use in
   *  monitoring of the system state, not for synchronization control.
   *
   *  @param condition
   *    the condition
   *  @return
   *    the estimated number of waiting threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if this lock is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this lock
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  def getWaitQueueLength(condition: Condition): Int = condition match {
    case null => throw new NullPointerException
    case cond: AbstractQueuedSynchronizer#ConditionObject =>
      sync.getWaitQueueLength(cond)
    case _ => throw new IllegalArgumentException("not owner")
  }

  /** Returns a collection containing those threads that may be waiting on the
   *  given condition associated with this lock. Because the actual set of
   *  threads may change dynamically while constructing this result, the
   *  returned collection is only a best-effort estimate. The elements of the
   *  returned collection are in no particular order. This method is designed to
   *  facilitate construction of subclasses that provide more extensive
   *  condition monitoring facilities.
   *
   *  @param condition
   *    the condition
   *  @return
   *    the collection of threads
   *  @throws java.lang.IllegalMonitorStateException
   *    if this lock is not held
   *  @throws java.lang.IllegalArgumentException
   *    if the given condition is not associated with this lock
   *  @throws java.lang.NullPointerException
   *    if the condition is null
   */
  protected def getWaitingThreads(condition: Condition): Collection[Thread] =
    condition match {
      case null => throw new NullPointerException
      case cond: AbstractQueuedSynchronizer#ConditionObject =>
        sync.getWaitingThreads(cond)
      case _ => throw new IllegalArgumentException("not owner")
    }

  /** Returns a string identifying this lock, as well as its lock state. The
   *  state, in brackets, includes either the String {@code "Unlocked"} or the
   *  String {@code "Locked by"} followed by the
   *  [[java.lang.Thread.getName name]] of the owning thread.
   *
   *  @return
   *    a string identifying this lock, as well as its lock state
   */
  override def toString(): String = {
    val o = sync.getOwner()
    super.toString() + (if (o == null) "[Unlocked]"
                        else s"[Locked by thread ${o.getName()}]")
  }
}
