package scala.scalanative.runtime
package monitor

import LockWord._
import scala.annotation.tailrec
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.unsafe.{stackalloc => _, _}
import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.runtime.libc._
import scala.scalanative.runtime.libc.memory_order._
import scala.scalanative.meta.LinktimeInfo.{is32BitPlatform => is32bit}

/** Lightweight monitor used for single-threaded execution, upon detection of
 *  access from multiple threads is inflated in ObjectMonitor
 *
 *  @param lockWordRef
 *    Pointer to LockWord, internal field of every object header
 */
@inline
private[runtime] final class BasicMonitor(val lockWordRef: RawPtr)
    extends AnyVal {
  import BasicMonitor._
  type ThreadId = RawPtr

  @alwaysinline def _notify(): Unit = {
    val current = lockWord
    if (current.isInflated) current.getObjectMonitor._notify()
  }

  @alwaysinline def _notifyAll(): Unit = {
    val current = lockWord
    if (current.isInflated) current.getObjectMonitor._notifyAll()
  }

  @alwaysinline def _wait(): Unit =
    getObjectMonitor()._wait()

  @alwaysinline def _wait(timeout: Long): Unit =
    getObjectMonitor()._wait(timeout)

  @alwaysinline def _wait(timeout: Long, nanos: Int): Unit =
    getObjectMonitor()._wait(timeout, nanos)

  @inline def enter(obj: Object): Unit = {
    val thread = Thread.currentThread()
    val threadId = getThreadId(thread)

    if (!tryLock(threadId))
      enterMonitor(thread, threadId) // slow-path
  }

  private def enterMonitor(thread: Thread, threadId: ThreadId) = {
    import NativeThread._
    currentNativeThread.state = State.WaitingOnMonitorEnter
    val current = lockWord
    if (current.isInflated) current.getObjectMonitor.enter(thread)
    else {
      if (threadId == current.threadId) {
        if (current.recursionCount < ThinMonitorMaxRecursion) {
          // No need for atomic operation since we already obtain the lock
          storeRawPtr(lockWordRef, current.withIncreasedRecursion)
        } else inflate(thread)
      } else lockAndInflate(thread, threadId)
    }
    currentNativeThread.state = State.Running
  }

  @inline def exit(obj: Object): Unit = {
    val thread = Thread.currentThread()
    val threadId = getThreadId(thread)
    val current = lockWord
    val lockedOnce = lockedWithThreadId(threadId)
    if (current == lockedOnce)
      atomic_store_intptr(
        lockWordRef,
        castIntToRawPtr(0),
        memory_order_release
      )
    else if (current.isUnlocked) () // can happend only in main thread
    else if (current.isInflated) current.getObjectMonitor.exit(thread)
    else storeRawPtr(lockWordRef, current.withDecresedRecursion)
  }

  @alwaysinline def isLockedBy(thread: Thread): Boolean = {
    val current = lockWord
    if (current.isInflated) current.getObjectMonitor.isLockedBy(thread)
    else current.threadId == getThreadId(thread)
  }

  @alwaysinline private def lockWord: LockWord =
    atomic_load_intptr(lockWordRef, memory_order_acquire)

  @inline private def getObjectMonitor() = {
    val current = lockWord
    if (current.isInflated) current.getObjectMonitor
    else inflate(Thread.currentThread())
  }

  @alwaysinline private def lockedWithThreadId(threadId: ThreadId): RawPtr =
    // lockType=0, recursion=0
    if (is32bit) castIntToRawPtr(castRawPtrToInt(threadId) << ThreadIdOffset)
    else castLongToRawPtr(castRawPtrToLong(threadId) << ThreadIdOffset)

  @alwaysinline private def getThreadId(thread: Thread): ThreadId = {
    val addr = castObjectToRawPtr(thread)
    if (is32bit) castIntToRawPtr(castRawPtrToInt(addr) & LockWord32.ThreadIdMax)
    else castLongToRawPtr(castRawPtrToLong(addr) & LockWord.ThreadIdMax)
  }

  @inline
  private def tryLock(threadId: ThreadId) = {
    val expected = stackalloc(new CSize(sizeOfPtr))
    // ThreadId set to 0, recursion set to 0
    storeRawSize(expected, castIntToRawSize(0))
    atomic_compare_exchange_intptr(
      lockWordRef,
      expected,
      lockedWithThreadId(threadId)
    )
  }

  // Monitor is currently locked by other thread. Wait until getting over owership
  // of this object and transform LockWord to use HeavyWeight monitor
  @inline private def lockAndInflate(
      thread: Thread,
      threadId: ThreadId
  ): Unit = {
    @tailrec @alwaysinline def waitForOwnership(
        yields: Int,
        backoffNanos: Int
    ): Unit = {
      def MaxSleepNanos = 128000
      if (!tryLock(threadId) && !lockWord.isInflated) {
        if (yields > 8) {
          NativeThread.currentNativeThread.sleepNanos(backoffNanos)
          waitForOwnership(
            yields,
            backoffNanos = (backoffNanos * 3 / 2).min(MaxSleepNanos)
          )
        } else {
          NativeThread.onSpinWait()
          waitForOwnership(yields + 1, backoffNanos)
        }
      }
    }
    waitForOwnership(yields = 0, backoffNanos = 1000)

    // // Check if other thread has not inflated lock already
    val current = lockWord
    if (current.isInflated) current.getObjectMonitor.enter(thread)
    else inflate(thread)
  }

  @inline private def inflate(thread: Thread): ObjectMonitor = {
    val objectMonitor = new ObjectMonitor()
    objectMonitor.enter(thread)
    // Increment recursion by basic lock recursion count if present
    objectMonitor.recursion += lockWord.recursionCount

    // Since pointers are always alligned we can safely override N=sizeof(Word) right most bits
    val monitorAddress = castObjectToRawPtr(objectMonitor)
    val inflated =
      if (is32bit) {
        val lockMark = (LockType.Inflated: Int) << LockTypeOffset
        val addr = castRawPtrToInt(monitorAddress)
        castIntToRawPtr(lockMark | addr)
      } else {
        val lockMark = (LockType.Inflated: Long) << LockTypeOffset
        val addr = castRawPtrToLong(monitorAddress)
        castLongToRawPtr(lockMark | addr)
      }
    atomic_store_intptr(lockWordRef, inflated, memory_order_release)
    atomic_thread_fence(memory_order_seq_cst)

    objectMonitor
  }
}
