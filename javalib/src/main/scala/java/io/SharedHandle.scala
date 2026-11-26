package java.io

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.javalib.io.{AtomicObjectHandle, ObjectHandle}
import scala.scalanative.libc.stdatomic.AtomicInt
import scala.scalanative.meta.LinktimeInfo._
import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.runtime.{Intrinsics, fromRawPtr}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.HandleApi.CloseHandle
import scala.scalanative.windows.{
  IoApiSet, ProcessThreadsApi, ProcessThreadsApiExt, SynchApi
}

/* The OS handle of a FileDescriptor, shared by its streams and channels.
 * Syscalls lease it: close() wakes blocked channel calls, and the OS close
 * waits for the last lease.
 */
private[java] final class SharedHandle(handle: AtomicObjectHandle) {
  import SharedHandle._

  // Leases in flight (low bits) and the Closing bit.
  @volatile private var state = 0
  // Runs after the OS close.
  @volatile private var afterClose: () => Unit = null
  // Lazy; written under this lock.
  @volatile private var wakePipe: WakePipe = null // Unix
  @volatile private var blocked: BlockedThreads = null // Windows

  @alwaysinline private def atomicState =
    new AtomicInt(fromRawPtr(Intrinsics.classFieldRawPtr(this, "state")))

  @alwaysinline def get(): ObjectHandle = handle.get()

  @alwaysinline def isClosing: Boolean = (state & Closing) != 0

  def valid(): Boolean = !isClosing && get().valid()

  /** Starts a syscall; false once closing has started. */
  def acquire(): Boolean = {
    var acquired = false
    var s = state
    while (!acquired && (s & Closing) == 0 && get().valid()) {
      acquired = atomicState.compareExchangeStrong(s, s + 1)
      if (!acquired) s = state
    }
    acquired
  }

  /** Ends a syscall; the last one after close() closes the OS handle. */
  def release(): Unit =
    if (atomicState.fetchSub(1) - 1 == Closing) closeNow(deferred = true)

  /** Wakes channel calls, then closes the OS handle once no syscall is in
   *  flight. `after` runs last.
   */
  def close(after: () => Unit): Unit = {
    var prev = state
    var won = false
    while (!won && (prev & Closing) == 0) {
      afterClose = after // before the CAS: the last release() may use it
      won = atomicState.compareExchangeStrong(prev, prev | Closing)
      if (!won) prev = state
    }
    if (won) {
      if (prev == 0) closeNow(deferred = false)
      else wakeBlocked()
    }
  }

  private def closeNow(deferred: Boolean): Unit = {
    // Under the lock, so a racing wake never writes to a reused fd.
    if (!isWindows) synchronized {
      if (wakePipe != null) wakePipe.close()
      wakePipe = null
    }
    handle.close()
    val action = afterClose
    afterClose = null
    // A deferred close has no caller to report to.
    if (action != null)
      try action()
      catch { case _: Exception if deferred => () }
  }

  /** Unix: wake pipe read end, readable once closing starts. */
  def wakeFd(): Int = {
    val p = wakePipe
    if (p != null) p.readFd
    else
      synchronized {
        if (wakePipe == null) {
          wakePipe = WakePipe()
          if (isClosing) wakePipe.signal()
        }
        wakePipe.readFd
      }
  }

  /** Windows: lets close() cancel the current thread's I/O. */
  def registerBlocked(): Unit = if (isWindows) synchronized {
    if (blocked == null) blocked = new BlockedThreads()
    blocked.add()
  }

  def deregisterBlocked(): Unit = if (isWindows) synchronized {
    blocked.remove()
  }

  private def wakeBlocked(): Unit =
    if (!isWindows) synchronized {
      if (wakePipe != null) wakePipe.signal()
    }
    else {
      /* A cancel misses a thread not yet in its call: retry until none is
       * left or the timeout passes. Sleep, as our interrupt breaks wait().
       */
      val deadline = System.nanoTime() + CancelTimeoutNanos
      while (cancelBlocked() && System.nanoTime() < deadline)
        SynchApi.Sleep(1.toUInt)
    }

  // Under the lock, so a cancel only reaches a thread still registered.
  private def cancelBlocked(): Boolean = synchronized {
    blocked != null && blocked.cancelOthers()
  }
}

private[java] object SharedHandle {
  private final val Closing = 1 << 30
  private final val CancelTimeoutNanos = 2L * 1000 * 1000 * 1000

  // Unix: channel operations poll the read end with their fd.
  private final class WakePipe(val readFd: Int, writeFd: Int) {
    def signal(): Unit = {
      val b = stackalloc[Byte]()
      unistd.write(writeFd, b, 1.toUInt)
    }
    def close(): Unit = {
      unistd.close(readFd)
      unistd.close(writeFd)
    }
  }

  private object WakePipe {
    def apply(): WakePipe = {
      val fds = stackalloc[CInt](2)
      // pipe2 sets close-on-exec atomically; Apple has no pipe2.
      val hasPipe2 = isLinux || isFreeBSD || isOpenBSD || isNetBSD
      val created =
        if (hasPipe2) unistd.pipe2(fds, fcntl.O_CLOEXEC) == 0
        else
          unistd.pipe(fds) == 0 && {
            val ok =
              fcntl.fcntl(fds(0), fcntl.F_SETFD, fcntl.FD_CLOEXEC) != -1 &&
                fcntl.fcntl(fds(1), fcntl.F_SETFD, fcntl.FD_CLOEXEC) != -1
            if (!ok) closeBoth(fds)
            ok
          }
      if (!created) throw new IOException("Cannot create a wake-up pipe")
      val flags = fcntl.fcntl(fds(1), fcntl.F_GETFL, 0)
      if (flags == -1 ||
          fcntl.fcntl(fds(1), fcntl.F_SETFL, flags | fcntl.O_NONBLOCK) == -1) {
        closeBoth(fds)
        throw new IOException("Cannot create a wake-up pipe")
      }
      new WakePipe(fds(0), fds(1))
    }

    private def closeBoth(fds: Ptr[CInt]): Unit = {
      unistd.close(fds(0))
      unistd.close(fds(1))
    }
  }

  // Windows: ids of threads in channel I/O, woken by CancelSynchronousIo.
  private final class BlockedThreads {
    private var ids = new Array[Int](4)
    private var count = 0

    def add(): Unit = {
      if (count == ids.length) ids = java.util.Arrays.copyOf(ids, count * 2)
      ids(count) = currentThreadId()
      count += 1
    }

    def remove(): Unit = {
      val id = currentThreadId()
      var i = 0
      while (i < count && ids(i) != id) i += 1
      if (i < count) {
        count -= 1
        ids(i) = ids(count)
      }
    }

    /* Cancels all but the caller, which map() may have registered before
     * it waits for the position lock. False if none.
     */
    def cancelOthers(): Boolean = isMultithreadingEnabled && {
      val self = currentThreadId()
      var cancelled = 0
      var i = 0
      while (i < count) {
        val id = ids(i)
        if (id != self) {
          // CancelSynchronousIo needs THREAD_TERMINATE.
          val thread = ProcessThreadsApi.OpenThread(
            ProcessThreadsApiExt.THREAD_TERMINATE,
            false,
            id.toUInt
          )
          if (thread != null) {
            IoApiSet.CancelSynchronousIo(thread)
            CloseHandle(thread)
            cancelled += 1
          }
        }
        i += 1
      }
      cancelled > 0
    }

    private def currentThreadId(): Int =
      ProcessThreadsApi.GetCurrentThreadId().toInt
  }
}
