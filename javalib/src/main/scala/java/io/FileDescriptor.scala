package java.io

import java.io.FileDescriptor._
import java.util.concurrent.atomic.AtomicInteger

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.javalib.io._
import scala.scalanative.meta.LinktimeInfo.{isMultithreadingEnabled, isWindows}
import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.winnt.AccessRights._
import scala.scalanative.windows.{
  ConsoleApiExt, IoApiSet, ProcessThreadsApi, ProcessThreadsApiExt, SynchApi
}

final class FileDescriptor private[java] (
    private val fileHandle: AtomicObjectHandle,
    readOnly: Boolean
) {
  def this() = this(
    fileHandle = AtomicObjectHandle(ObjectHandle.Invalid),
    readOnly = true
  )

  // ScalaNative private constructors
  private[java] def this(fd: Int, readOnly: Boolean = false) =
    this(AtomicObjectHandle(fd), readOnly)

  private[java] def this(fh: Handle, readOnly: Boolean) =
    this(AtomicObjectHandle(fh), readOnly)

  private[java] def this(fh: ObjectHandle, readOnly: Boolean) =
    this(AtomicObjectHandle(fh), readOnly)

  // Syscalls in flight (low bits) and the Closing bit; the OS handle closes
  // once Closing is set and nothing is in flight.
  private val leases = new AtomicInteger(0)
  @volatile private var afterClose: () => Unit = null
  // Wakes channel operations on close; made by the first one. Guarded by this.
  @volatile private var waker: Waker = null

  override def toString(): String =
    s"FileDescriptor($fd, readOnly=$readOnly)"

  @inline private[java] def get(): ObjectHandle = fileHandle.get()

  /* Unix file descriptor underlying value */
  @alwaysinline private[java] def fd: Int = get().asInt

  /* Windows file handler underlying value */
  @alwaysinline private[java] def handle: Handle = get().asHandle

  def sync(): Unit = {
    def isStdOrInvalidFileDescriptor: Boolean =
      if (isWindows) !valid() || this == in || this == out || this == err
      else fd <= 2

    if (isStdOrInvalidFileDescriptor || !acquire())
      throw new SyncFailedException("sync failed")
    val failed =
      try
        !readOnly && {
          if (isWindows) !FlushFileBuffers(handle) else unistd.fsync(fd) != 0
        }
      finally release()

    if (failed) throw new SyncFailedException("sync failed")
  }

  def valid(): Boolean = !isClosing && get().valid()

  /** Starts a syscall on this fd; false once closing has started. */
  private[java] def acquire(): Boolean = {
    var acquired = false
    var s = leases.get()
    while (!acquired && (s & Closing) == 0 && get().valid()) {
      acquired = leases.compareAndSet(s, s + 1)
      if (!acquired) s = leases.get()
    }
    acquired
  }

  /** Ends a syscall started by acquire(). The last one may close the fd. */
  private[java] def release(): Unit =
    if (leases.decrementAndGet() == Closing) closeNow(deferred = true)

  @alwaysinline private[java] def isClosing: Boolean =
    (leases.get() & Closing) != 0

  /** Closes the fd once, after the syscalls in flight have finished.
   *
   *  Channel operations in flight are woken. Stream calls are not; the OS close
   *  waits for them. `after` runs once the OS handle is closed.
   */
  private[java] def close(after: () => Unit): Unit = {
    var prev = leases.get()
    var won = false
    while (!won && (prev & Closing) == 0) {
      afterClose = after // before the CAS: a release() may close right after it
      won = leases.compareAndSet(prev, prev | Closing)
      if (!won) prev = leases.get()
    }
    if (won) {
      if (prev == 0) closeNow(deferred = false)
      else wakeBlocked()
    }
  }

  private def closeNow(deferred: Boolean): Unit = {
    // Detached under the lock, so a racing wakeOnce() never writes to a
    // closed (and maybe reused) pipe fd.
    synchronized {
      if (waker != null) waker.dispose()
      waker = null
    }
    fileHandle.close()
    val action = afterClose
    afterClose = null
    // A deferred close has no caller to report to.
    if (action != null)
      try action()
      catch { case _: Exception if deferred => () }
  }

  private def obtainWaker(): Waker = {
    val w = waker
    if (w != null) w
    else
      synchronized {
        if (waker == null) {
          waker = if (isWindows) new BlockedThreads() else WakePipe()
          if (isClosing) waker.wake()
        }
        waker
      }
  }

  /** Unix: read end of the wake pipe, readable once closing has started. */
  private[java] def wakeFd(): Int =
    obtainWaker().asInstanceOf[WakePipe].readFd

  /** Windows: marks the current thread as blocked in channel I/O. */
  private[java] def registerBlocked(): Unit = {
    val threads = obtainWaker().asInstanceOf[BlockedThreads]
    synchronized(threads.add())
  }

  private[java] def deregisterBlocked(): Unit = synchronized {
    waker.asInstanceOf[BlockedThreads].remove()
  }

  /* Windows: a registered thread may not have entered its call yet, so keep
   * cancelling until all have left, within CancelTimeoutNanos. A thread still
   * in its call then keeps its lease: the OS close waits for it. Sleep, unlike
   * wait(), ignores our interrupt status.
   */
  private def wakeBlocked(): Unit =
    if (!isWindows) wakeOnce()
    else {
      val deadline = System.nanoTime() + CancelTimeoutNanos
      while (wakeOnce() && System.nanoTime() < deadline)
        SynchApi.Sleep(1.toUInt)
    }

  // Under the lock, so a Windows cancel only reaches a thread still registered.
  private def wakeOnce(): Boolean = synchronized {
    waker != null && waker.wake()
  }
}

object FileDescriptor {
  private final val Closing = 1 << 30
  private final val CancelTimeoutNanos = 2000000000L

  private[java] val none: FileDescriptor = new FileDescriptor()

  val in: FileDescriptor =
    if (isWindows)
      new FileDescriptor(ConsoleApiExt.stdIn, readOnly = true)
    else
      new FileDescriptor(unistd.STDIN_FILENO, readOnly = true)

  val out: FileDescriptor =
    if (isWindows)
      new FileDescriptor(ConsoleApiExt.stdOut, readOnly = false)
    else
      new FileDescriptor(unistd.STDOUT_FILENO, readOnly = false)

  val err: FileDescriptor =
    if (isWindows)
      new FileDescriptor(ConsoleApiExt.stdErr, readOnly = false)
    else
      new FileDescriptor(unistd.STDERR_FILENO, readOnly = false)

  /* Wakes channel operations in flight once closing has started. Called with
   * the FileDescriptor lock held.
   */
  private abstract class Waker {
    // True if a wake should be retried: a thread may not be in its call yet.
    def wake(): Boolean
    def dispose(): Unit
  }

  // Unix: channel operations poll the read end with their fd.
  private final class WakePipe(val readFd: Int, writeFd: Int) extends Waker {
    def wake(): Boolean = {
      val b = stackalloc[Byte]()
      unistd.write(writeFd, b, 1.toUInt)
      false
    }
    def dispose(): Unit = {
      unistd.close(readFd)
      unistd.close(writeFd)
    }
  }

  private object WakePipe {
    def apply(): WakePipe = {
      val fds = stackalloc[CInt](2)
      if (unistd.pipe(fds) != 0)
        throw new IOException("Cannot create a wake-up pipe")
      fcntl.fcntl(fds(0), fcntl.F_SETFD, fcntl.FD_CLOEXEC)
      fcntl.fcntl(fds(1), fcntl.F_SETFD, fcntl.FD_CLOEXEC)
      val flags = fcntl.fcntl(fds(1), fcntl.F_GETFL, 0)
      fcntl.fcntl(fds(1), fcntl.F_SETFL, flags | fcntl.O_NONBLOCK)
      new WakePipe(fds(0), fds(1))
    }
  }

  // Windows: ids of threads in channel I/O, woken by CancelSynchronousIo.
  private final class BlockedThreads extends Waker {
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

    /* Cancels the I/O of the other threads; false if none could be. The
     * caller may be registered itself (an interrupt while waiting to lock).
     */
    def wake(): Boolean = isMultithreadingEnabled && {
      val self = currentThreadId()
      var others = 0
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
            others += 1
          }
        }
        i += 1
      }
      others > 0
    }

    def dispose(): Unit = ()

    private def currentThreadId(): Int =
      ProcessThreadsApi.GetCurrentThreadId().toInt
  }

  private[io] def openReadOnly(file: File): FileDescriptor =
    Zone.acquire { implicit z =>
      def fail() =
        throw new FileNotFoundException("No such file " + file.getPath())

      val fileHandle = if (isWindows) {
        val handle = CreateFileW(
          filename = toCWideStringUTF16LE(file.getPath()),
          desiredAccess = FILE_GENERIC_READ,
          shareMode = FILE_SHARE_READ | FILE_SHARE_WRITE,
          securityAttributes = null,
          creationDisposition = OPEN_EXISTING,
          flagsAndAttributes = 0.toUInt,
          templateFile = null
        )
        if (handle == INVALID_HANDLE_VALUE) {
          fail()
        }
        ObjectHandle(handle)
      } else {
        val fd = fcntl.open(toCString(file.getPath()), fcntl.O_RDONLY, 0.toUInt)
        if (fd == -1) {
          fail()
        }
        ObjectHandle(fd)
      }

      new FileDescriptor(fileHandle, true)
    }
}
