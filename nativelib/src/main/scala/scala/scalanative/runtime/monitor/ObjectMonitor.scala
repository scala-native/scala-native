package scala.scalanative.runtime.monitor

import java.lang.Thread
import java.util.concurrent.locks.LockSupport

import scala.annotation.{switch, tailrec}

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.ffi._
import scala.scalanative.runtime.ffi.stdatomic._
import scala.scalanative.runtime.ffi.stdatomic.memory_order._
import scala.scalanative.runtime.{
  Intrinsics, NativeThread, RawPtr, VirtualThread
}
import scala.scalanative.unsafe.{sizeOf => _, stackalloc => _, _}

/** Heavy-weight monitor created only upon detection of access from multiple
 *  threads is inflated in ObjectMonitor.
 *
 *    - Entry list: contenders CAS onto head; successor is the tail (FIFO).
 *    - Handoff succession: the exiting thread does not unlink the successor; it
 *      only nominates and wakes it. The wakee unlinks itself when it acquires.
 *    - Exit protocol: set successor, release owner (release store), fence, then
 *      unpark (or schedule VT via scheduleWithResume). No list modification on
 *      exit.
 */
private[monitor] class ObjectMonitor() {
  import ObjectMonitor._

  /** Thread currently holding the monitor (owner). */
  @volatile private var ownerThread: Thread = _

  /** Nominated successor: thread to unpark on this exit. Set before releasing
   *  owner; cleared by wakee after it acquires. Never used to unlink from list.
   */
  @volatile private var successorThread: Thread = _

  /** Thread selected for timed parking to alleviate rare deadlocks. */
  @volatile private var activeWaiterThread: Thread = _

  /** Contenders CAS onto head; successor is tail (FIFO). Only owner manipulates
   *  for succession; wakee unlinks self on acquire.
   */
  @volatile private var entryList: WaiterNode = _

  /** Cached tail of entry list; set when building DLL on exit or appending on
   *  notify; wakee updates when it unlinks and was the tail.
   */
  @volatile private var entryListTail: WaiterNode = _

  /** Ring list of waiting threads. Access limited by the modification lock.
   *  Wait-set threads are moved to the entry list (tail) on notify; they remove
   *  themselves when they acquire the lock.
   */
  @volatile private var waitQueue: WaiterNode = _
  @volatile private var waiting: Int = 0
  @volatile private var waitListModifcationLock: Byte = 0
  @volatile private[monitor] var recursion: Int = 0

  /** Number of unmounted virtual threads on the entry list. When positive,
   *  platform threads use timed park with recheck to avoid deadlock when all
   *  carriers are busy with pinned VTs.
   */
  @volatile private[monitor] var unmountedVThreads: Long = 0L

  @inline def enter(currentThread: Thread): Unit = {
    if (casOwnerThread(expected = null, currentThread)) ()
    else if (ownerThread eq currentThread) recursion += 1
    else if (trySpinAndLock(currentThread)) ()
    else enterMonitor(currentThread) // slowpath
  }

  @inline def exit(currentThread: Thread): Unit = {
    checkOwnership(currentThread)
    if (recursion != 0) recursion -= 1
    else exitMonitor(currentThread)
  }

  @inline def _notify(): Unit = {
    checkOwnership(Thread.currentThread())
    if (waitQueue != null) notifyImpl(1)
  }

  @inline def _notifyAll(): Unit = {
    checkOwnership(Thread.currentThread())
    if (waitQueue != null) notifyImpl(waiting)
  }

  @alwaysinline def _wait(): Unit = waitImpl(0L)

  @alwaysinline def _wait(timeoutMillis: Long): Unit = _wait(timeoutMillis, 0)

  @inline def _wait(timeoutMillis: Long, nanos: Int): Unit = {
    if (timeoutMillis < 0)
      throw new IllegalArgumentException("timeoutMillis value is negative")
    if (nanos < 0 || nanos > 999999)
      throw new IllegalArgumentException(
        "nanosecond timeout value out of range"
      )
    waitImpl(timeoutMillis * 1000000 + nanos)
  }

  @alwaysinline def isLockedBy(thread: Thread): Boolean = ownerThread eq thread

  // Slow path: join entry list, then park or suspend until the lock is acquired.
  private def enterMonitor(currentThread: Thread): Unit = {
    val isVT = currentThread.isInstanceOf[VirtualThread]
    if (isVT) incrementUnmountedVThreads()
    val node = new WaiterNode(currentThread, WaiterNode.InEnterQueue)
    while ({
      val next = entryList
      node.next = next
      !casWaitList(entryListPtr, next, node)
    }) if (tryLock(currentThread)) {
      if (isVT) decrementUnmountedVThreads()
      return
    }

    enterMonitor(currentThread, node)
  }

  private def enterMonitor(currentThread: Thread, node: WaiterNode): Unit = {
    // Try to lock upon spinning, otherwise park the thread and try again upon wake up.
    // Virtual threads: suspend (blockForMonitorEnter) so carrier can run other VTs; exit will call scheduleWithResume.
    def awaitLock(): Unit = {
      @alwaysinline def tryLockThenSpin() =
        tryLock(currentThread) || trySpinAndLock(currentThread)

      // With unmounted VTs on the entry list, use timed park with growing
      // recheck interval (×8, cap 1s) so this thread periodically retries and
      // can act as successor, avoiding deadlock when carriers are busy with
      // pinned VTs. Otherwise use a shorter fixed-backoff poll.
      var recheckIntervalMs = 1L
      @alwaysinline def MaxRecheckIntervalMs = 1000L
      var pollInterval = 25000L // ns, 0.025ms
      @alwaysinline def MaxPollInterval = 1000000L // ns = 1ms
      var forcedTimedRecheck = node.consumeTimedParkHint()

      NativeThread.currentNativeThread.state =
        NativeThread.State.WaitingOnMonitorEnter
      while (!tryLockThenSpin()) {
        if (activeWaiterThread eq null)
          casActiveWaiterThread(null, currentThread)
        NativeThread.currentNativeThread.state =
          NativeThread.State.WaitingOnMonitorEnter
        if (forcedTimedRecheck || hasUnmountedVThreads()) {
          NativeThread.currentNativeThread.parkNanos(
            recheckIntervalMs * 1000000L
          )
          recheckIntervalMs = (recheckIntervalMs * 8) min MaxRecheckIntervalMs
          forcedTimedRecheck = false
        } else {
          recheckIntervalMs = 1L
          NativeThread.currentNativeThread.parkNanos(pollInterval)
          pollInterval = (pollInterval * 4) min MaxPollInterval
        }
        clearSuccessorAndFenceBeforeRetry(currentThread)
      }

      if (successorThread eq currentThread) successorThread = null
      if (activeWaiterThread eq currentThread) activeWaiterThread = null
      atomic_thread_fence(memory_order_seq_cst)
    }

    val isVT = currentThread.isInstanceOf[VirtualThread]
    if (!tryLock(currentThread)) awaitLock()

    // Current thread now owns the monitor; unlink self from the entry list.
    if (isVT) decrementUnmountedVThreads()
    node.clearTimedParkHint()
    node.resumeForEnter = null
    node.resumeForEnterGeneration = 0L

    // Robust unlink: prefer list scan over cached node.prev to avoid stale-link races.
    @tailrec def unlinkSelfFromEntryList(): Unit = {
      val head = entryList
      if (head == null) ()
      else if (head eq node) {
        val next = node.next
        if (!casWaitList(entryListPtr, node, next)) unlinkSelfFromEntryList()
        else {
          if (next != null) next.prev = null
          else entryListTail = null
        }
      } else {
        var prev = head
        var cur = head.next
        while ((cur != null) && (cur ne node)) {
          prev = cur
          cur = cur.next
        }
        if (cur != null) {
          val next = cur.next
          prev.next = next
          if (next != null) next.prev = prev
          else entryListTail = prev
        }
      }
    }
    unlinkSelfFromEntryList()
    node.next = null
    node.prev = null

    if (successorThread eq currentThread) successorThread = null
    node.state = WaiterNode.Active
    NativeThread.currentNativeThread.state = NativeThread.State.Running
    atomic_thread_fence(memory_order_seq_cst)
  }

  private def exitMonitor(currentThread: Thread): Unit = {
    @alwaysinline def releaseOwnerThread() = {
      atomic_store_intptr(ownerThreadPtr, null, memory_order_release)
      atomic_thread_fence(memory_order_seq_cst)
    }

    /** Return the tail (oldest waiter) of the enter list for FIFO succession */
    @tailrec def tailOf(head: WaiterNode): WaiterNode =
      if (head == null) null
      else {
        val n = head.next
        if (n == null) head else tailOf(n)
      }

    /** Wake the nominated successor. We do not unlink node from entry list. */
    @alwaysinline def onExit(node: WaiterNode): Unit = {
      val wakedThread = node.thread
      successorThread = wakedThread
      releaseOwnerThread()
      var resume = node.resumeForWait
      if (resume eq VirtualThread.RESUME_SENTINEL) {
        // VT suspend callback is about to publish the real resume.
        // Spin until it does, or until the VT clears the sentinel
        // (meaning it skipped blockForMonitorWait because isNotified was true).
        while ({
          resume = node.resumeForWait
          resume eq VirtualThread.RESUME_SENTINEL
        }) NativeThread.onSpinWait()
      }
      if (resume != null) {
        val resumeGeneration = node.resumeForWaitGeneration
        node.resumeForWait = null
        node.resumeForWaitGeneration = 0L
        wakedThread match {
          case vt: VirtualThread =>
            vt.scheduleWithResume(resume, resumeGeneration)
          case _ => LockSupport.unpark(wakedThread)
        }
      } else {
        val resumeEnter = node.resumeForEnter
        if (resumeEnter != null) {
          val resumeEnterGeneration = node.resumeForEnterGeneration
          node.resumeForEnter = null
          node.resumeForEnterGeneration = 0L
          wakedThread match {
            case vt: VirtualThread =>
              vt.scheduleWithResume(resumeEnter, resumeEnterGeneration)
            case _ => LockSupport.unpark(wakedThread)
          }
        } else {
          val threadToUnpark = wakedThread match {
            case vt: VirtualThread => vt.carrierForUnpark
            case _                 => wakedThread
          }
          LockSupport.unpark(
            if (threadToUnpark != null) threadToUnpark else wakedThread
          )
        }
      }
    }

    // Exit loop: hand off to a waiter when needed, release ownership, then
    // either delegate via successor or stop if another thread owns next steps.
    //
    //   if (no successor && entry list non-empty) → build DLL, wake tail, stop
    //   release owner; fence
    //   if (entry list empty || successor set) → stop
    //   if (try reacquire fails) → stop
    //   else loop
    @tailrec def transformToDLL(cur: WaiterNode, prev: WaiterNode): Unit =
      if (cur != null) {
        cur.prev = prev
        transformToDLL(cur.next, cur)
      }

    var looping = true
    while (looping) {
      if (successorThread == null) {
        val w = entryList
        if (w != null) {
          transformToDLL(w, null)
          val tail = tailOf(w)
          onExit(tail)
          looping = false
        }
      }

      if (looping) {
        releaseOwnerThread()
        if (entryList == null || successorThread != null) {
          looping = false
        } else {
          if (!tryLock(currentThread)) {
            looping = false
          }
        }
      }
    }
  }

  def waitImpl(nanos: Long): Unit = {
    val currentThread = Thread.currentThread()
    checkOwnership(currentThread)
    if (Thread.interrupted()) throw new InterruptedException()

    val node = new WaiterNode(currentThread, WaiterNode.Waiting)
    atomic_thread_fence(memory_order_seq_cst)

    acquireWaitList()
    try {
      addToWaitList(node)
      waiting += 1
    } finally releaseWaitList()

    val savedRecursion = this.recursion
    this.recursion = 0
    // For VTs: place a sentinel on resumeForWait BEFORE releasing the monitor.
    // This guarantees (via monitor happens-before) that onExit will see the
    // sentinel and spin-wait for the real continuation resume instead of
    // falling back to LockSupport.unpark which would consume the parking permit.
    if (currentThread.isInstanceOf[VirtualThread]) {
      node.resumeForWait = VirtualThread.RESUME_SENTINEL
      node.resumeForWaitGeneration = 0L
    }
    exitMonitor(currentThread)
    // assert(ownerThread != currentThread)

    // Current thread is no longer the owner, wait for the notification.
    // Virtual threads register blockForMonitorWait so wait() does not consume
    // the LockSupport permit and so the carrier is not parked (many VTs can wait).
    val interruped = currentThread.isInterrupted()
    if (!interruped && !node.isNotified) {
      currentThread match {
        case vt: VirtualThread =>
          vt.blockForMonitorWait(
            nanos,
            (resume: () => Unit, generation: Long) => {
              node.resumeForWait = resume
              node.resumeForWaitGeneration = generation
            }
          )
        case _ =>
          if (nanos == 0) LockSupport.park()
          else LockSupport.parkNanos(nanos)
      }
    }
    waitDebug(
      currentThread,
      s"resumed nanos=$nanos nodeState=${node.state} notified=${node.isNotified} owner=$ownerThread successor=$successorThread"
    )
    // Clear sentinel if VT was notified before entering blockForMonitorWait
    if (node.resumeForWait eq VirtualThread.RESUME_SENTINEL) {
      node.resumeForWait = null
      node.resumeForWaitGeneration = 0L
    }
    if (node.state == WaiterNode.Waiting) {
      waitDebug(
        currentThread,
        s"before-remove nanos=$nanos nodeState=${node.state} waitQueue=$waitQueue"
      )
      acquireWaitList()
      // Skip unlinking node if was moved from waitQueue to entry list by notify
      try
        if (node.state == WaiterNode.Waiting) {
          // Timeout/interrupt won the race, so clear the wait resume before unlinking.
          // This prevents a late notify from re-scheduling the same continuation.
          node.resumeForWait = null
          node.resumeForWaitGeneration = 0L
          removeFromWaitList(node)
          waiting -= 1
          node.state = WaiterNode.Active
        }
      finally releaseWaitList()
      waitDebug(
        currentThread,
        s"after-remove nanos=$nanos nodeState=${node.state} waitQueue=$waitQueue"
      )
    }

    atomic_thread_fence(memory_order_acquire)
    // Save the state of notification after waking up the thread
    val wasNotified = node.isNotified
    clearSuccessorAndFenceBeforeRetry(currentThread)

    // Thread is alive again, wait for ownership
    // assert(ownerThread != currentThread, "before re-renter")
    val nativeThread = NativeThread.currentNativeThread
    // assert(nativeThread.thread eq currentThread)
    nativeThread.state = NativeThread.State.WaitingOnMonitorEnter
    (node.state: @switch) match {
      case WaiterNode.Active =>
        waitDebug(
          currentThread,
          s"before-enter-active nanos=$nanos owner=$ownerThread successor=$successorThread"
        )
        enter(currentThread)
      case WaiterNode.InEnterQueue =>
        waitDebug(
          currentThread,
          s"before-enter-queue nanos=$nanos owner=$ownerThread successor=$successorThread"
        )
        enterMonitor(currentThread, node)
      case _ =>
        throw new IllegalMonitorStateException("internal state of thread")
    }
    waitDebug(
      currentThread,
      s"after-enter nanos=$nanos owner=$ownerThread successor=$successorThread"
    )
    nativeThread.state = NativeThread.State.Running
    this.recursion = savedRecursion
    // assert(ownerThread == currentThread, "reenter")

    if (!wasNotified && Thread.interrupted()) {
      throw new InterruptedException()
    }
  }

  @inline private def notifyImpl(notifiedElements: Int): Unit = {
    @tailrec def iterate(toNotify: Int): Unit = dequeueWaiter() match {
      case null => ()
      case node =>
        node.isNotified = true
        node.state = WaiterNode.InEnterQueue
        val isVT = node.thread.isInstanceOf[VirtualThread]
        var forceTimedRecheck = false
        if (isVT) {
          node.clearTimedParkHint()
          incrementUnmountedVThreads()
        } else {
          forceTimedRecheck = hasUnmountedVThreads()
          node.setTimedParkHint(forceTimedRecheck)
        }
        // Push notifyee to head of entry list. Successor is always the tail
        // (oldest), so existing contenders
        // are woken before the notifyee. CAS because contenders also
        // push to head concurrently.
        node.prev = null
        while ({
          val head = entryList
          node.next = head
          !casWaitList(entryListPtr, head, node)
        }) ()
        if (!isVT && forceTimedRecheck) {
          wakePlatformWaiter(node.thread)
        }
        if (toNotify > 1) iterate(toNotify - 1)
    }

    acquireWaitList()
    try iterate(notifiedElements)
    finally releaseWaitList()
  }

  @alwaysinline private def ownerThreadPtr =
    classFieldRawPtr(this, "ownerThread")
  @alwaysinline private def entryListPtr =
    classFieldRawPtr(this, "entryList")

  @alwaysinline private def waitListModificationLockPtr =
    classFieldRawPtr(this, "waitListModifcationLock")

  @alwaysinline private def unmountedVThreadsPtr =
    classFieldRawPtr(this, "unmountedVThreads")

  @alwaysinline private def activeWaiterThreadPtr =
    classFieldRawPtr(this, "activeWaiterThread")

  @alwaysinline private def hasUnmountedVThreads(): Boolean =
    atomic_load_llong(unmountedVThreadsPtr, memory_order_seq_cst) > 0L

  /** After clearing successor, retry ownership only after a full fence so
   *  successor/queue metadata is observed consistently.
   */
  @alwaysinline private def clearSuccessorAndFenceBeforeRetry(
      currentThread: Thread
  ): Unit = {
    if (successorThread eq currentThread) successorThread = null
    atomic_thread_fence(memory_order_seq_cst)
  }

  @alwaysinline private def incrementUnmountedVThreads(): Unit =
    casUpdateUnmountedVThreads(_ + 1L)

  @alwaysinline private def decrementUnmountedVThreads(): Unit =
    casUpdateUnmountedVThreads { current =>
      assert(current > 0L, "unmountedVThreads underflow")
      current - 1L
    }

  private def casUpdateUnmountedVThreads(f: Long => Long): Unit = {
    val expected = stackalloc[Long]()
    var done = false
    while (!done) {
      val current =
        atomic_load_llong(unmountedVThreadsPtr, memory_order_seq_cst)
      storeLong(expected, current)
      done = atomic_compare_exchange_llong(
        unmountedVThreadsPtr,
        expected,
        f(current)
      )
      if (!done) NativeThread.onSpinWait()
    }
  }

  @alwaysinline private def wakePlatformWaiter(thread: Thread): Unit = {
    val threadToUnpark = thread match {
      case vt: VirtualThread => vt.carrierForUnpark
      case _                 => thread
    }
    LockSupport.unpark(if (threadToUnpark != null) threadToUnpark else thread)
  }

  @alwaysinline private def casOwnerThread(
      expected: Thread,
      value: Thread
  ): Boolean = {
    val expectedPtr = stackalloc[CVoidPtr]()
    storeObject(expectedPtr, expected)
    atomic_compare_exchange_intptr(
      ownerThreadPtr,
      expectedPtr,
      castObjectToRawPtr(value)
    )
  }

  @alwaysinline private def casActiveWaiterThread(
      expected: Thread,
      value: Thread
  ): Boolean = {
    val expectedPtr = stackalloc[CVoidPtr]()
    storeObject(expectedPtr, expected)
    atomic_compare_exchange_intptr(
      activeWaiterThreadPtr,
      expectedPtr,
      castObjectToRawPtr(value)
    )
  }

  @alwaysinline private def casWaitList(
      ref: RawPtr,
      expected: WaiterNode,
      value: WaiterNode
  ): Boolean = {
    val expectedPtr = stackalloc[CVoidPtr]()
    storeObject(expectedPtr, expected)
    atomic_compare_exchange_intptr(ref, expectedPtr, castObjectToRawPtr(value))
  }

  private def acquireWaitList(): Unit = {
    val expected = stackalloc[Byte]()
    def tryAcquire() = {
      storeByte(expected, 0)
      atomic_compare_exchange_byte(
        waitListModificationLockPtr,
        expected,
        1: Byte
      )
    }

    @tailrec def waitForLockRelease(
        yields: Int = 0,
        backoffNanos: Int
    ): Unit = {
      def MaxSleepNanos = 64000
      if (waitListModifcationLock != 0) {
        // Whenever possible try to not lead to context switching
        if (yields > 16) {
          NativeThread.currentNativeThread.sleepNanos(backoffNanos)
          waitForLockRelease(
            yields,
            backoffNanos = (backoffNanos * 3 / 2).min(MaxSleepNanos)
          )
        } else {
          NativeThread.onSpinWait()
          waitForLockRelease(yields + 1, backoffNanos)
        }
      }
    }

    while (!tryAcquire()) {
      waitForLockRelease(0, backoffNanos = 1000)
    }
    atomic_thread_fence(memory_order_seq_cst)
  }

  @alwaysinline private def releaseWaitList() = {
    waitListModifcationLock = 0
    atomic_thread_fence(memory_order_seq_cst)
  }

  @alwaysinline protected def checkOwnership(currentThread: Thread): Unit = {
    atomic_thread_fence(memory_order_seq_cst)
    if (currentThread ne ownerThread) {
      throw new IllegalMonitorStateException(
        "Thread is not an owner of this object"
      )
    }
  }

  @inline @tailrec private def trySpinAndLock(
      thread: Thread,
      remainingSpins: Int = 32
  ): Boolean = {
    if (tryLock(thread)) true
    else if (remainingSpins > 0) {
      NativeThread.onSpinWait()
      trySpinAndLock(thread, remainingSpins - 1)
    } else false
  }

  @alwaysinline private def tryLock(thread: Thread) =
    casOwnerThread(expected = null, value = thread)

  // Adds to waitList implemented as cyclic sequence
  @inline private def addToWaitList(node: WaiterNode) = waitQueue match {
    case null =>
      waitQueue = node
      node.prev = node
      node.next = node
    case head =>
      val tail = head.prev
      tail.next = node
      head.prev = node
      node.next = head
      node.prev = tail
  }

  @alwaysinline private def dequeueWaiter(): WaiterNode = {
    val waiter = waitQueue
    if (waiter != null) removeFromWaitList(waiter)
    waiter
  }

  @inline private def removeFromWaitList(node: WaiterNode) = {
    // assert(node.state == WaiterNode.Waiting)
    node.next match {
      case `node` =>
        waitQueue = null
      case next =>
        val prev = node.prev
        next.prev = prev
        prev.next = next
        if (waitQueue == node) waitQueue = next
    }
    node.next = null
    node.prev = null
  }
}

private object ObjectMonitor {
  private val waitDebugEnabled = System.getenv("SN_VT_WAIT_DEBUG") != null

  private def waitDebug(thread: Thread, message: => String): Unit =
    if (waitDebugEnabled) {
      System.err.println(
        s"[MONITOR-WAIT threadId=${thread.getId()} name=${thread.getName()}] $message"
      )
    }

  object WaiterNode {

    /** Current state and expected placement of the node in the queues */
    type NodeState = Short
    final val Active = 0x00 // Monitor owner
    final val InArriveQueue = 0x01
    final val InEnterQueue = 0x02
    final val Waiting = 0x04
  }

  class WaiterNode(
      val thread: Thread,
      @volatile var state: WaiterNode.NodeState,
      @volatile var isNotified: Boolean = false,
      @volatile var next: WaiterNode = null,
      @volatile var prev: WaiterNode = null
  ) {

    /** Set by VT blockForMonitorWait so notify can schedule the VT without
     *  parking the carrier.
     */
    @volatile var resumeForWait: () => Unit = null
    @volatile var resumeForWaitGeneration: Long = 0L

    /** Set by VT blockForMonitorEnter so monitor exit can schedule the VT */
    @volatile var resumeForEnter: () => Unit = null
    @volatile var resumeForEnterGeneration: Long = 0L

    /** Notify-side liveness hint: force timed recheck for platform waiter once.
     */
    @volatile private var forceTimedPark: Boolean = false

    @alwaysinline def setTimedParkHint(value: Boolean): Unit =
      forceTimedPark = value

    @alwaysinline def clearTimedParkHint(): Unit =
      forceTimedPark = false

    @alwaysinline def consumeTimedParkHint(): Boolean = {
      val value = forceTimedPark
      forceTimedPark = false
      value
    }
  }
}
