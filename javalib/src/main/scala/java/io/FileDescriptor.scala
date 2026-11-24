package java.io

import java.io.FileDescriptor._
import java.util.concurrent.atomic.AtomicInteger

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.javalib.io._
import scala.scalanative.meta.LinktimeInfo.{isMultithreadingEnabled, isWindows}
import scala.scalanative.posix.sys.stat
import scala.scalanative.posix.sys.statOps._
import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.winnt.AccessRights._
import scala.scalanative.windows.{
  ConsoleApiExt, IoApi, ProcessThreadsApi, ProcessThreadsApiExt, SynchApi
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

  /* In-flight syscalls (low bits) plus the Closing bit. The OS handle is
   * closed once Closing is set and the count drops to zero, so no syscall
   * ever runs on a released fd number.
   */
  private val leases = new AtomicInteger(0)
  @volatile private var afterClose: () => Unit = null

  // Set by FileChannel.open: an O_CLOEXEC fd that no one else shares.
  @volatile private var privateDescription = false
  @volatile private var kindCache = KindUnknown
  @volatile private var nonBlocking = false

  // Unix: pipe that wakes channel operations polling this fd. Guarded by this.
  private var wakeRead = -1
  private var wakeWrite = -1

  // Windows: ids of threads in channel I/O on this handle. Guarded by this.
  private var blockedThreads: Array[Int] = null
  private var blockedCount = 0

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

  private[java] def markPrivateDescription(): this.type = {
    privateDescription = true
    this
  }

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
  private[java] def close(after: () => Unit = null): Unit = {
    var prev = leases.get()
    var done = false
    while (!done && (prev & Closing) == 0) {
      afterClose = after
      done = leases.compareAndSet(prev, prev | Closing)
      if (!done) prev = leases.get()
    }
    if (done) {
      if (prev == 0) closeNow(deferred = false)
      else wakeBlocked()
    }
  }

  private def closeNow(deferred: Boolean): Unit = {
    if (!isWindows) synchronized {
      if (wakeRead >= 0) {
        unistd.close(wakeRead)
        unistd.close(wakeWrite)
        wakeRead = -1
        wakeWrite = -1
      }
    }
    fileHandle.close()
    val action = afterClose
    afterClose = null
    if (action != null) {
      if (!deferred) action()
      else
        try action()
        catch { case _: Exception => () }
    }
  }

  /** Kind of file, see KindRegular. Call with a lease held. */
  private[java] def kind: Int = {
    if (kindCache == KindUnknown) kindCache = computeKind()
    kindCache
  }

  private def computeKind(): Int =
    if (isWindows) {
      if (GetFileType(handle) == FILE_TYPE_DISK) KindRegular
      else KindCancellable
    } else {
      val isRegular = Zone.acquire { implicit z =>
        val st = alloc[stat.stat]()
        stat.fstat(fd, st) == 0 && stat.S_ISREG(st.st_mode) != 0
      }
      if (isRegular) KindRegular
      else if (privateDescription) KindNonBlocking
      else KindPollFirst
    }

  /** Sets O_NONBLOCK, once. Only for KindNonBlocking. */
  private[java] def enableNonBlocking(): Unit =
    if (!nonBlocking) synchronized {
      if (!nonBlocking) {
        val flags = fcntl.fcntl(fd, fcntl.F_GETFL, 0)
        if (flags == -1 ||
            fcntl.fcntl(fd, fcntl.F_SETFL, flags | fcntl.O_NONBLOCK) == -1)
          throw new IOException("Cannot make the file descriptor non-blocking")
        nonBlocking = true
      }
    }

  /** Unix: read end of the wake pipe, readable once closing has started. */
  private[java] def wakeFd(): Int = synchronized {
    if (wakeRead < 0) {
      val fds = stackalloc[CInt](2)
      if (unistd.pipe(fds) != 0)
        throw new IOException("Cannot create a wake-up pipe")
      wakeRead = fds(0)
      wakeWrite = fds(1)
      fcntl.fcntl(wakeRead, fcntl.F_SETFD, fcntl.FD_CLOEXEC)
      fcntl.fcntl(wakeWrite, fcntl.F_SETFD, fcntl.FD_CLOEXEC)
      val flags = fcntl.fcntl(wakeWrite, fcntl.F_GETFL, 0)
      fcntl.fcntl(wakeWrite, fcntl.F_SETFL, flags | fcntl.O_NONBLOCK)
      if (isClosing) signalWakeFd()
    }
    wakeRead
  }

  private def signalWakeFd(): Unit = {
    val b = stackalloc[Byte]()
    unistd.write(wakeWrite, b, 1.toUInt)
  }

  /** Windows: marks the current thread as blocked in channel I/O. */
  private[java] def registerBlocked(): Unit = synchronized {
    if (blockedThreads == null) blockedThreads = new Array[Int](4)
    else if (blockedCount == blockedThreads.length)
      blockedThreads = java.util.Arrays.copyOf(blockedThreads, blockedCount * 2)
    blockedThreads(blockedCount) = currentThreadId()
    blockedCount += 1
  }

  private[java] def deregisterBlocked(): Unit = synchronized {
    val id = currentThreadId()
    var i = 0
    while (i < blockedCount && blockedThreads(i) != id) i += 1
    if (i < blockedCount) {
      blockedCount -= 1
      blockedThreads(i) = blockedThreads(blockedCount)
    }
  }

  private def currentThreadId(): Int =
    ProcessThreadsApi.GetCurrentThreadId().toInt

  private def wakeBlocked(): Unit =
    if (!isWindows) synchronized {
      if (wakeWrite >= 0) signalWakeFd()
    }
    else if (isMultithreadingEnabled) {
      /* A thread may be just about to start its call, so cancelling once
       * can be too early. Repeat until all have left, within a bound.
       * Sleep, unlike wait(), ignores the closing thread's interrupt status.
       */
      var rounds = 0
      while (cancelBlocked() && rounds < MaxCancelRounds) {
        SynchApi.Sleep(1.toUInt)
        rounds += 1
      }
    }

  // Windows: cancels the I/O of registered threads; false if there are none.
  private def cancelBlocked(): Boolean = synchronized {
    var i = 0
    while (i < blockedCount) {
      val thread = ProcessThreadsApi.OpenThread(
        ProcessThreadsApiExt.THREAD_TERMINATE,
        false,
        blockedThreads(i).toUInt
      )
      if (thread != null) {
        IoApi.CancelSynchronousIo(thread)
        CloseHandle(thread)
      }
      i += 1
    }
    blockedCount > 0
  }
}

object FileDescriptor {
  private final val Closing = 1 << 30
  private final val MaxCancelRounds = 2000

  private[java] final val KindUnknown = 0
  // Regular or disk file: I/O finishes on its own, no waking needed.
  private[java] final val KindRegular = 1
  // Unix, private description: O_NONBLOCK and poll with the wake pipe.
  private[java] final val KindNonBlocking = 2
  // Unix, shared description: poll with the wake pipe before each call.
  private[java] final val KindPollFirst = 3
  // Windows pipe or console: woken by CancelSynchronousIo.
  private[java] final val KindCancellable = 4

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
