package scala.scalanative.runtime.monitor

import scala.annotation.{tailrec, switch}
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.runtime.NativeThread
import scala.scalanative.runtime.Intrinsics
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.{RawPtr, sizeOfPtr}
import scala.scalanative.runtime.libc._
import scala.scalanative.runtime.libc.memory_order._
import scala.scalanative.unsafe.{stackalloc => _, sizeOf => _, _}
import java.util.concurrent.locks.LockSupport

/** Heavy weight monitor created only upon detection of access from multiple
 *  threads is inflated in ObjectMonitor
 */
private[monitor] class ObjectMonitor() {
  import ObjectMonitor._

  /** Thread currently locking ownership over given object */
  @volatile private var ownerThread: Thread = _

  /** Thread nominated to be the next owner of the monitor. If not null
   *  successorThread would be unparked upon exit
   */
  @volatile private var successorThread: Thread = _

  /** Thread selected for active acquiring the lock. A selected thread is the
   *  only thread which would be parked in a timed manner. It is done to prevent
   *  rare cases of deadlocks.
   */
  @volatile private var activeWaiterThread: Thread = _

  /** Linked list of threads waiting to enter the monitor. It's head would be
   *  modified using CAS from InEnterQueue threads. Can be detached and
   *  transferred to enterQueue by the owner thread upon exit.
   */
  @volatile private var arriveQueue: WaiterNode = _

  /** Double-linked list of threads waiting to enter the monitor. Can be
   *  modified only by the owner thread. Head of the queue might be nominated to
   *  become successor thread.
   */
  @volatile private var enterQueue: WaiterNode = _

  /** Ring list of waiting threads. Access limited by the modification lock.
   *  Upon InEnterQueue the wait zone threads would enqueue to the queue, and
   *  would remove themself upon exiting the zone. Threads would be notified
   *  sequentially based on their order in the queue. Nodes from waitQueue can
   *  be detached and moved to the enterQueue
   */
  @volatile private var waitQueue: WaiterNode = _
  @volatile private var waiting: Int = 0
  @volatile private var waitListModifcationLock: Byte = 0
  @volatile private[monitor] var recursion: Int = 0

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

  // enter slow-path
  private def enterMonitor(currentThread: Thread): Unit = {
    // Enqueue the node the node to the arriveQueue using CAS
    val node = new WaiterNode(currentThread, WaiterNode.InArriveQueue)
    while ({
      val next = arriveQueue
      node.next = next
      !casWaitList(arriveQueuePtr, next, node)
    }) if (tryLock(currentThread)) return

    enterMonitor(currentThread, node)
  }

  private def enterMonitor(currentThread: Thread, node: WaiterNode) = {
    // Try to lock upon spinning, otherwise park the thread and try again upon wake up
    def awaitLock(): Unit = {
      var isActive = false
      var pollInterval = 25000L // ns, 0.25ms
      @alwaysinline def MaxPoolInterval = 1000000000L // ns = 1s
      @alwaysinline def tryLockThenSpin() =
        tryLock(currentThread) || trySpinAndLock(currentThread)

      while (!tryLockThenSpin()) {
        isActive ||= casActiveWaiterThread(null, currentThread)
        if (!isActive) LockSupport.park(this)
        else {
          LockSupport.parkNanos(this, pollInterval)
          pollInterval = (pollInterval * 4) min MaxPoolInterval
        }
        if (successorThread eq currentThread) successorThread = null
        atomic_thread_fence(memory_order_seq_cst)
      }

      if (successorThread eq currentThread) successorThread = null
      if (activeWaiterThread eq currentThread) activeWaiterThread = null
      atomic_thread_fence(memory_order_seq_cst)
    }

    if (!tryLock(currentThread)) awaitLock()

    // Current thread is now owner of the monitor, unlink it from the queue
    // assert(currentThread eq ownerThread)
    if (node.state == WaiterNode.InEnterQueue) {
      // enterQ can be only modified by the owner thread
      val next = node.next
      val prev = node.prev
      if (next != null) next.prev = prev
      if (prev != null) prev.next = next
      if (node == enterQueue) enterQueue = next
    } else {
      // assert(node.state == WaiterNode.InArriveQueue)
      val head = arriveQueue
      if ((head ne node) || !casWaitList(arriveQueuePtr, head, node.next)) {
        // Find and remove the node from queue
        // No need for atomic ops - only head of the queue might be modified using CAS
        @tailrec def loop(current: WaiterNode, prev: WaiterNode): Unit =
          if (current != null && (current ne node))
            loop(current.next, current)
          else {
            assert(current eq node, s"not found node $node in queue")
            prev.next = current.next
          }
        loop(if (head eq node) arriveQueue else head, null)
      }
    }
    if (successorThread eq currentThread) successorThread = null
    node.state = WaiterNode.Active
    atomic_thread_fence(memory_order_seq_cst)
  }

  @tailrec private def exitMonitor(currentThread: Thread): Unit = {
    @alwaysinline def releaseOwnerThread() = {
      atomic_store_intptr(ownerThreadPtr, null, memory_order_release)
      atomic_thread_fence(memory_order_seq_cst)
    }

    @alwaysinline def onExit(node: WaiterNode): Unit = {
      val wakedThread = node.thread
      successorThread = wakedThread
      releaseOwnerThread()
      LockSupport.unpark(wakedThread)
    }

    releaseOwnerThread()
    // If there is no successor or entry queus are empty we can finish here
    val queuesAreEmpty = enterQueue == null && arriveQueue == null
    if (queuesAreEmpty || successorThread != null) ()
    // If other thread has already taken ownership over monitor it would be responsible for selecting successor
    else if (tryLock(currentThread)) {
      enterQueue match {
        case null =>
          // enterQueue is empty, try to detach and transfer arriveQueue to it
          arriveQueue match {
            // both queues are empty, it conflicts with previous check. Mutation accoured, so restart loop
            case null => exitMonitor(currentThread)
            case node =>
              @tailrec def detachNodes(head: WaiterNode): WaiterNode = {
                if (casWaitList(arriveQueuePtr, head, null)) head
                else detachNodes(arriveQueue)
              }

              @tailrec def transformToDLL(
                  cur: WaiterNode,
                  prev: WaiterNode
              ): Unit = if (cur != null) {
                cur.state = WaiterNode.InEnterQueue
                cur.prev = prev
                transformToDLL(cur.next, cur)
              }

              val detached = detachNodes(node)
              transformToDLL(detached, prev = null)
              enterQueue = detached

              // conficts with the previous condition, mutation accoured, restart
              if (successorThread != null) exitMonitor(currentThread)
              else onExit(detached)
          }
        case node => onExit(node)
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
    exitMonitor(currentThread)
    // assert(ownerThread != currentThread)

    // Current thread is no longer the owner, wait for the notification
    val interruped = currentThread.isInterrupted()
    if (!interruped && !node.isNotified) {
      if (nanos == 0) LockSupport.park(this)
      else LockSupport.parkNanos(this, nanos)
    }
    if (node.state == WaiterNode.Waiting) {
      acquireWaitList()
      // Skip unlinking node if was moved from waitQueue to enterQueue by notify call
      try
        if (node.state == WaiterNode.Waiting) {
          removeFromWaitList(node)
          waiting -= 1
          node.state = WaiterNode.Active
        }
      finally releaseWaitList()
    }

    atomic_thread_fence(memory_order_acquire)
    if (successorThread eq currentThread) successorThread = null
    // Save the state of notification after waking up the thread
    val wasNotified = node.isNotified
    atomic_thread_fence(memory_order_seq_cst)

    // Thread is alive again, wait for ownership
    // assert(ownerThread != currentThread, "before re-renter")
    val nativeThread = NativeThread.currentNativeThread
    // assert(nativeThread.thread eq currentThread)
    nativeThread.state = NativeThread.State.WaitingOnMonitorEnter
    (node.state: @switch) match {
      case WaiterNode.Active =>
        enter(currentThread)
      case WaiterNode.InArriveQueue | WaiterNode.InEnterQueue =>
        enterMonitor(currentThread, node)
      case _ =>
        throw new IllegalMonitorStateException("internal state of thread")
    }
    nativeThread.state = NativeThread.State.Running
    this.recursion = savedRecursion
    // assert(ownerThread == currentThread, "reenter")

    if (!wasNotified && Thread.interrupted()) {
      throw new InterruptedException()
    }
  }

  @inline private def notifyImpl(notifiedElements: Int): Unit = {
    var tail: WaiterNode = null
    @tailrec def iterate(toNotify: Int): Unit = dequeueWaiter() match {
      case null => ()
      case node =>
        node.isNotified = true
        node.state = WaiterNode.InEnterQueue
        // Move from waitList to tail of enterQueue
        enterQueue match {
          case null =>
            node.next = null
            node.prev = null
            enterQueue = node
          case head =>
            if (tail == null) {
              tail = head
              while (tail.next != null) tail = tail.next
            }
            tail.next = node
            node.prev = tail
            node.next = null
            tail = node
        }
        if (toNotify > 0) iterate(toNotify - 1)
    }

    acquireWaitList()
    try iterate(notifiedElements)
    finally releaseWaitList()
  }

  @alwaysinline private def ownerThreadPtr =
    classFieldRawPtr(this, "ownerThread")
  @alwaysinline private def arriveQueuePtr =
    classFieldRawPtr(this, "arriveQueue")

  @alwaysinline private def waitListModificationLockPtr =
    classFieldRawPtr(this, "waitListModifcationLock")

  @alwaysinline private def activeWaiterThreadPtr =
    classFieldRawPtr(this, "activeWaiterThread")

  @alwaysinline private def casOwnerThread(
      expected: Thread,
      value: Thread
  ): Boolean = {
    val expectedPtr = stackalloc[Ptr[_]]()
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
    val expectedPtr = stackalloc[Ptr[_]]()
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
    val expectedPtr = stackalloc[Ptr[_]]()
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

  @alwaysinline def SizeOfPtr = new CSize(sizeOfPtr)
}

private object ObjectMonitor {
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
  )
}
