package java.nio.channels

import java.io.{File, FileDescriptor, IOException, InputStream, OutputStream}
import java.nio.file.attribute.FileAttribute
import java.nio.file.{
  AccessDeniedException, FileAlreadyExistsException, FileSystemException, Files,
  NoSuchFileException, Path, WindowsException
}
import java.nio.{ByteBuffer, MappedByteBuffer, MappedByteBufferImpl}
import java.util.Objects
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.{LockSupport, ReentrantLock}

import scala.scalanative.libc.errno.errno
import scala.scalanative.libc.{LibcExt, stdio}
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.nio.fs.unix.UnixException
import scala.scalanative.posix.errno.{
  EACCES, EAGAIN, EEXIST, EINTR, ELOOP, ENOENT, EPERM, EWOULDBLOCK
}
import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.fcntlOps._
import scala.scalanative.posix.poll._
import scala.scalanative.posix.pollOps._
import scala.scalanative.posix.stdlib.realpath
import scala.scalanative.posix.sys.statOps._
import scala.scalanative.posix.sys.{ioctl, stat}
import scala.scalanative.posix.{limits, unistd}
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.FileApiOps.ByHandleFileInformationOps
import scala.scalanative.windows.MinWinBaseApi._
import scala.scalanative.windows.MinWinBaseApiOps._
import scala.scalanative.windows._

/* Public operations run in begin()/end(), so an interrupt or close() ends
 * them; stream methods (private[java]) do not. Open checks come first: a
 * closed channel throws ClosedChannelException. Syscalls hold an fd lease
 * (see SharedHandle).
 */
private[java] final class FileChannelImpl(
    fd: FileDescriptor,
    file: Option[File],
    deleteFileOnClose: Boolean,
    openForReading: Boolean,
    openForWriting: Boolean,
    openForAppending: Boolean = false,
    initialKind: Int = FileChannelImpl.KindUnknown
) extends FileChannel {
  import FileChannelImpl._

  private val shared = fd.shared
  // Idempotent caches: a race only repeats the work.
  private var kindCache = initialKind
  private var pipeBufCache = 0
  private var nonBlocking = false

  // Held by operations that use or move the position, or change the size.
  private val positionLock = new ReentrantLock()

  // Rechecks after locking, as close() may run while we wait.
  private def withPositionLock[T](intr: Boolean)(body: => T): T = {
    lockPosition(intr)
    try {
      ensureOpen()
      body
    } finally positionLock.unlock()
  }

  // close() cannot wake a lock waiter, so channel calls wait in slices.
  private def lockPosition(intr: Boolean): Unit =
    if (!intr) positionLock.lock()
    else if (!positionLock.tryLock()) {
      while (!tryLockPosition()) ensureOpen()
    }

  private def tryLockPosition(): Boolean =
    try positionLock.tryLock(10L, TimeUnit.MILLISECONDS)
    catch {
      case _: InterruptedException =>
        // Restoring the flag closes this channel: begin() is active.
        Thread.currentThread().interrupt()
        false
    }

  @inline private def interruptibly[T](body: => T): T = {
    begin()
    var completed = false
    try {
      val result = body
      completed = true
      result
    } finally end(completed)
  }

  // Leases the fd; `intr` marks a channel call, which close() wakes.
  @inline private def leased[T](intr: Boolean)(body: => T): T = {
    if (!shared.acquire())
      throw {
        if (intr) new ClosedChannelException()
        else new IOException("Stream Closed")
      }
    var registered = false
    try {
      if (isWindows) {
        if (intr && kind == KindCancellable) {
          shared.registerBlocked()
          registered = true
        }
      }
      body
    } finally {
      if (registered) shared.deregisterBlocked()
      shared.release()
    }
  }

  private def locked[T](intr: Boolean)(body: => T): T =
    withPositionLock(intr)(leased(intr)(body))

  @inline private def closing(intr: Boolean): Boolean =
    intr && shared.isClosing

  // Unix uses O_APPEND, Windows emulates it.
  if (isWindows) {
    if (openForAppending) seekEOF()
  }

  private def ensureOpen(): Unit =
    if (!isOpen()) throw new ClosedChannelException()

  private def ensureOpenForWrite(): Unit = {
    ensureOpen()
    if (!openForWriting)
      throw new NonWritableChannelException()
  }

  private def ensureOpenForRead(): Unit = {
    ensureOpen()
    if (!openForReading)
      throw new NonReadableChannelException()
  }

  @inline private def seekEOFBeforeRelativeWrite(): Unit =
    if (isWindows) {
      if (openForAppending) seekEOF()
    }

  private def seekEOF(): Unit = {
    if (isWindows) {
      SetFilePointerEx(
        fd.handle,
        distanceToMove = 0,
        newFilePointer = null,
        moveMethod = FILE_END
      )
    } else {
      val pos = FileChannelNative.lseek(fd.fd, 0, stdio.SEEK_END);
      if (pos < 0)
        throwPosixException("lseek")
    }
  }

  private def throwPosixException(functionName: String): Unit = {
    if (!isWindows) {
      val errnoString = LibcExt.strError()
      throw new IOException(s"${functionName} failed: ${errnoString}")
    }
  }

  override def force(metadata: Boolean): Unit = {
    ensureOpen()
    interruptibly(leased(intr = true)(forceNow(metadata)))
  }

  private def forceNow(metadata: Boolean): Unit =
    if (isWindows) {
      // FlushFileBuffers needs write access.
      if (openForWriting && !FlushFileBuffers(fd.handle))
        throw WindowsException.onPath(getFileName())
    } else {
      val res =
        if (metadata) unistd.fsync(fd.fd)
        else unistd.fdatasync(fd.fd)
      if (res != 0) throw UnixException(getFileName(), errno)
    }

  // File locks: see FileLockTable and FileLockImpl.

  // File identity for FileLockTable; computed with a lease held.
  @volatile private var lockKey: FileLockTable.Key = null

  override def tryLock(
      position: Long,
      size: Long,
      shared: Boolean
  ): FileLock = {
    // No begin(): tryLock ignores the interrupt status.
    val lock = newLock(position, size, shared, waiter = null)
    var granted = false
    try granted = attemptLock(lock)
    finally if (!granted) FileLockTable.cancel(lock)
    if (granted) lock else null
  }

  override def lock(position: Long, size: Long, shared: Boolean): FileLock = {
    val lock = newLock(position, size, shared, Thread.currentThread())
    try awaitLock(lock)
    finally FileLockTable.cancel(lock) // no-op once granted
    lock
  }

  /* Retries a non-blocking attempt with capped backoff: no carrier thread
   * blocks in the OS, and no lease is held while parked. close() and an
   * interrupt (which closes the channel) unpark the waiter.
   */
  private def awaitLock(lock: FileLockImpl): Unit = {
    var completed = false
    begin()
    try {
      var delay = LockMinDelayNanos
      while (!attemptLock(lock)) {
        LockSupport.parkNanos(this, delay)
        delay = Math.min(delay * 2, LockMaxDelayNanos)
      }
      completed = true
    } finally {
      try end(completed)
      catch {
        case _: ClosedByInterruptException =>
          throw new FileLockInterruptionException()
      }
    }
  }

  // Checks, then registers a pending lock; throws on overlap.
  private def newLock(
      position: Long,
      size: Long,
      shared: Boolean,
      waiter: Thread
  ): FileLockImpl = {
    ensureOpen()
    if (shared) {
      if (!openForReading) throw new NonReadableChannelException()
    } else if (!openForWriting) throw new NonWritableChannelException()
    if (position < 0) throw new IllegalArgumentException("Negative position")
    if (size < 0) throw new IllegalArgumentException("Negative size")
    if (position + size < 0)
      throw new IllegalArgumentException("Negative position + size")
    // Like JDK 25, size 0 means to the end of any file.
    val length = if (size == 0) Long.MaxValue - position else size
    val lock =
      new FileLockImpl(this, position, length, shared, fileKey(), waiter)
    FileLockTable.add(lock)
    lock
  }

  private def fileKey(): FileLockTable.Key = {
    if (lockKey == null) lockKey = leased(intr = true)(computeFileKey())
    lockKey
  }

  private def computeFileKey(): FileLockTable.Key =
    if (isWindows) {
      val info = stackalloc[ByHandleFileInformation]()
      if (!GetFileInformationByHandle(fd.handle, info))
        throw WindowsException.onPath(getFileName())
      FileLockTable.Key(
        info.volumeSerialNumber.toLong,
        info.fileIndex.toLong
      )
    } else
      Zone.acquire { implicit z =>
        val st = alloc[stat.stat]()
        if (stat.fstat(fd.fd, st) != 0)
          throw UnixException(getFileName(), errno)
        FileLockTable.Key(st.st_dev.toLong, st.st_ino.toLong)
      }

  /* One non-blocking OS attempt. True once granted; a lock that close()
   * cancelled meanwhile is unlocked again.
   */
  private def attemptLock(lock: FileLockImpl): Boolean = {
    if (lock.state != FileLockImpl.Pending) throw new ClosedChannelException()
    leased(intr = true) {
      val granted = osLock(lock, unlock = false)
      if (granted && !FileLockTable.grant(lock)) {
        osLock(lock, unlock = true)
        throw new AsynchronousCloseException()
      }
      granted
    }
  }

  // From FileLockImpl.release(), after FileLockTable.startRelease.
  private[channels] def unlock(lock: FileLockImpl): Unit =
    try leased(intr = true)(osLock(lock, unlock = true))
    finally FileLockTable.remove(lock)

  // False if another process holds a conflicting lock.
  private def osLock(lock: FileLockImpl, unlock: Boolean): Boolean =
    if (isWindows) windowsLock(lock, unlock)
    else unixLock(lock, unlock)

  /* POSIX record locks, like the JDK: they belong to the process, so closing
   * any fd of the file drops them while isValid() stays true. Linux OFD
   * locks would avoid that, but would differ from the JDK.
   */
  private def unixLock(lock: FileLockImpl, unlock: Boolean): Boolean = {
    val mode =
      if (unlock) LockUnlock
      else if (lock.isShared()) LockShared
      else LockExclusive
    // A lock reaching Long.MaxValue covers any growth: l_len 0.
    val end = lock.position + lock.size
    val len = if (end == Long.MaxValue) 0L else lock.size
    val err = FileChannelNative.lock(fd.fd, mode, lock.position, len)
    if (err == 0) true
    else if (!unlock && (err == EAGAIN || err == EACCES)) false
    else throw UnixException(getFileName(), err)
  }

  private def windowsLock(lock: FileLockImpl, unlock: Boolean): Boolean = {
    val pos = lock.position
    val size = lock.size
    val overlapped = stackalloc[OVERLAPPED]()
    overlapped.Internal = 0.toULong
    overlapped.InternalHigh = 0.toULong
    overlapped.at3.Offset = low(pos)
    overlapped.at3.OffsetHigh = high(pos)
    overlapped.hEvent = null
    val ok =
      if (unlock)
        UnlockFileEx(fd.handle, 0.toUInt, low(size), high(size), overlapped)
      else {
        // lock() retries, so never wait in the OS.
        var flags = LOCKFILE_FAIL_IMMEDIATELY
        if (!lock.isShared()) flags |= LOCKFILE_EXCLUSIVE_LOCK
        LockFileEx(
          fd.handle,
          flags,
          0.toUInt,
          low(size),
          high(size),
          overlapped
        )
      }
    ok || {
      val err = ErrorHandlingApi.GetLastError()
      if (!unlock && err == ErrorCodes.ERROR_LOCK_VIOLATION) false
      else throw WindowsException.onPathWithLastEror(getFileName(), err)
    }
  }

  // Called by close(), before the fd closes.
  private def releaseLocks(): Unit = {
    val key = lockKey
    if (key != null) {
      val locks = FileLockTable.closeChannel(key, this)
      var i = 0
      while (i < locks.size()) {
        val lock = locks.get(i)
        if (lock.state == FileLockImpl.Releasing) {
          try {
            if (shared.acquire())
              try osLock(lock, unlock = true)
              finally shared.release()
          } catch { case _: IOException => () }
          finally FileLockTable.remove(lock)
        } else LockSupport.unpark(lock.waiter)
        i += 1
      }
    }
  }

  // See SharedHandle.close.
  protected def implCloseChannel(): Unit = {
    val delete: () => Unit =
      if (deleteFileOnClose && file.isDefined)
        () => Files.delete(file.get.toPath())
      else null
    releaseLocks()
    shared.close(delete)
  }

  override def map(
      mode: FileChannel.MapMode,
      position: Long,
      size: Long
  ): MappedByteBuffer = {
    if (!openForReading)
      throw new NonReadableChannelException

    // JVM states position is non-negative, hence 0 is allowed.
    if (position < 0)
      throw new IllegalArgumentException("Negative position")

    /* JVM requires the "size" argument to be a long, but throws
     * an exception if that long is greater than Integer.MAX_VALUE.
     * toInt() would cause such a large value to roll over to a negative value.
     *
     * Call to MappedByteBufferImpl() below truncates its third argument
     * to an Int, knowing this guard is in place.
     */

    if ((size < 0) || (size > Integer.MAX_VALUE))
      throw new IllegalArgumentException("Negative size")

    ensureOpen()

    val readOnly = mode eq FileChannel.MapMode.READ_ONLY
    // FileChannel.open() has previously rejected READ + APPEND combination.
    if (!readOnly && !openForWriting)
      throw new NonWritableChannelException

    interruptibly(leased(intr = true) {
      if (!readOnly) {
        // This "lengthen" branch is tested/exercised in MappedByteBufferTest.
        // Detect Long overflow & throw. Room for improvement here.
        val newFileSize = Math.addExact(position, size)
        withPositionLock(intr = true) {
          if (newFileSize > sizeNow())
            lengthen(newFileSize)
        }
      }
      // RE: toInt() truncation safety, see note for "size" arg checking above.
      MappedByteBufferImpl(mode, position, size.toInt, fd)
    })
  }

  // change position, even in APPEND mode. Use _carefully_.
  private def compelPosition(offset: Long): Unit = {
    if (isWindows)
      FileApi.SetFilePointerEx(
        fd.handle,
        offset,
        null,
        FILE_BEGIN
      )
    else {
      val pos = FileChannelNative.lseek(fd.fd, offset, stdio.SEEK_SET)
      if (pos < 0)
        throwPosixException("lseek")
    }
  }

  override def position(offset: Long): FileChannel = {
    checkPosition(offset)
    interruptibly(setPosition(offset, intr = true))
    this
  }

  private def checkPosition(offset: Long): Unit = {
    ensureOpen()
    if (offset < 0)
      throw new IllegalArgumentException("Negative position")
  }

  private def checkSize(newSize: Long): Unit = {
    ensureOpenForWrite()
    if (newSize < 0)
      throw new IllegalArgumentException("Negative size")
  }

  private def setPosition(offset: Long, intr: Boolean): Unit =
    if (!openForAppending)
      locked(intr)(compelPosition(offset))

  override def position(): Long = {
    ensureOpen()
    interruptibly(getPosition(intr = true))
  }

  private def getPosition(intr: Boolean): Long =
    locked(intr)(currentPosition())

  // RandomAccessFile: position operations that ignore interrupts.
  private[java] def streamPosition(): Long = {
    ensureOpen()
    getPosition(intr = false)
  }

  private[java] def streamSeek(offset: Long): Unit = {
    checkPosition(offset)
    setPosition(offset, intr = false)
  }

  private[java] def streamTruncate(newSize: Long): Unit = {
    checkSize(newSize)
    truncateNow(newSize, intr = false)
  }

  private def currentPosition(): Long =
    if (isWindows) {
      // FILE_END, unlike GetFileSizeEx, needs no read-attributes access.
      val filePointer = stackalloc[LargeInteger]()
      if (!FileApi.SetFilePointerEx(
            fd.handle,
            0,
            filePointer,
            if (openForAppending) FILE_END else FILE_CURRENT
          ))
        throw WindowsException.onPath(getFileName())
      !filePointer
    } else if (openForAppending) sizeNow()
    else {
      val pos = FileChannelNative.lseek(fd.fd, 0, stdio.SEEK_CUR)
      if (pos < 0)
        throwPosixException("lseek")
      pos
    }

  override def read(
      buffers: Array[ByteBuffer],
      start: Int,
      number: Int
  ): Long = {
    Objects.requireNonNull(buffers, "dsts")
    Objects.checkFromIndexSize(start, number, buffers.length)

    ensureOpenForRead()

    interruptibly(locked(intr = true) {
      var i = 0
      var partialReadSeen = false
      var totalRead = 0L
      while (i < number && !partialReadSeen) {
        val dst = buffers(start + i)
        val len = dst.remaining()

        val bs = new Array[Byte](len)
        val n =
          try readArray(bs, 0, len, intr = true)
          catch { case _: AsynchronousCloseException if totalRead > 0 => 0 }

        if (n > 0) {
          dst.put(bs, 0, n)
          totalRead += n
        }

        if (n < len) {
          partialReadSeen = true
        }
        i += 1
      }

      totalRead
    })
  }

  private def readBuffer(dst: ByteBuffer): Int =
    readIntoBuffer(dst)(readArray(_, _, _, intr = true))

  private def readIntoBuffer(dst: ByteBuffer)(
      readArray: (Array[Byte], Int, Int) => Int
  ): Int = {
    if (dst.isReadOnly())
      throw new IllegalArgumentException("Read-only buffer")

    val dstPos = dst.position()
    val len = dst.limit() - dstPos

    if (dst.hasArray()) {
      val n = readArray(dst.array(), dst.arrayOffset() + dstPos, len)
      if (n > 0) dst.position(dstPos + n)
      n
    } else {
      val tmp = new Array[Byte](len)
      val n = readArray(tmp, 0, len)
      if (n > 0) dst.put(tmp, 0, n)
      n
    }
  }

  // Read at absolute position, do not change current position.
  override def read(buffer: ByteBuffer, pos: Long): Int = {
    if (pos < 0)
      throw new IllegalArgumentException("Negative position")

    ensureOpenForRead()
    interruptibly(readAt(buffer, pos))
  }

  private def readAt(buffer: ByteBuffer, pos: Long): Int =
    if (isWindows)
      atPosition(pos)(readBuffer(buffer))
    else
      leased(intr = true) {
        readIntoBuffer(buffer)(preadArray(_, _, _, pos))
      }

  // Windows has no positional I/O on synchronous handles: seek, then restore.
  private def atPosition[T](pos: Long)(body: => T): T =
    locked(intr = true) {
      val stashPosition = currentPosition()
      compelPosition(pos)
      try body
      finally restorePosition(stashPosition)
    }

  private def restorePosition(stashPosition: Long): Unit =
    if (openForAppending) seekEOF()
    else compelPosition(stashPosition)

  override def read(buffer: ByteBuffer): Int = {
    ensureOpenForRead()
    interruptibly(locked(intr = true) {
      readBuffer(buffer)
    })
  }

  private def preadArray(
      buffer: Array[Byte],
      offset: Int,
      count: Int,
      pos: Long
  ): Int =
    if (count == 0) 0
    else unixRead(buffer.at(offset), count, pos, intr = true)

  private def pwriteArray(
      array: Array[Byte],
      offset: Int,
      count: Int,
      pos: Long
  ): Int =
    if (count == 0) 0
    else unixWrite(array.at(offset), count, pos, intr = true)

  private def getFileName(orElse: => String = ""): String =
    file.fold(orElse)(_.toString())

  private[java] def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
    if (buffer == null) {
      throw new NullPointerException
    }
    if (offset < 0 || count < 0 || count > buffer.length - offset) {
      throw new IndexOutOfBoundsException
    }
    if (count == 0) 0
    else
      locked(intr = false) {
        readArray(buffer, offset, count, intr = false)
      }
  }

  // Precondition: the fd is leased.
  private def readArray(
      buffer: Array[Byte],
      offset: Int,
      count: Int,
      intr: Boolean
  ): Int = {
    if (count == 0) 0
    else if (isWindows) {
      val readBytes = stackalloc[DWord]()

      def readAll(off: Int, len: Int): Int = {
        var result = -1
        while (result < 0) {
          if (closing(intr)) throw new AsynchronousCloseException()
          if (ReadFile(fd.handle, buffer.at(off), len.toUInt, readBytes, null))
            result = (!readBytes).toInt
          else {
            val error = ErrorHandlingApi.GetLastError()
            if (error == ErrorCodes.ERROR_BROKEN_PIPE) result = 0
            // Cancelled by close(): the loop rechecks closing.
            else if (error != ErrorCodes.ERROR_OPERATION_ABORTED)
              throw WindowsException.onPathWithLastEror(getFileName(), error)
          }
        }
        result
      }

      // readAll blocks until everything is read
      // but we are OK with less so we need to see what's available
      val avail = if (file.isEmpty) availableNow() else count
      if (avail > 0) {
        val readCount = readAll(offset, avail.min(count))
        if (readCount == 0) -1 else readCount
      } else {
        val readCount = readAll(offset, 1)
        if (readCount == 0) -1 // EOF
        else {
          val toRead = if (count == 1) 0 else availableNow()
          if (toRead <= 0) 1
          else
            try 1 + readAll(offset + 1, toRead.min(count - 1))
            catch { case _: AsynchronousCloseException => 1 } // keep the byte
        }
      }
    } else unixRead(buffer.at(offset), count, -1L, intr)
  }

  // read, or pread if pos >= 0. Returns -1 at end of file.
  private def unixRead(
      buf: Ptr[Byte],
      count: Int,
      pos: Long,
      intr: Boolean
  ): Int = {
    val pollFirst = prepareBlockingCall(intr, pos)
    val n = blockingCall(write = false, buf, count, pos, pollFirst, intr)
    if (n < 0) throw new AsynchronousCloseException()
    else if (n == 0) -1
    else n.toInt
  }

  /* write, or pwrite if pos >= 0, until done, a 0-byte write or close().
   * Throws AsynchronousCloseException if nothing was written.
   */
  private def unixWrite(
      buf: Ptr[Byte],
      count: Int,
      pos: Long,
      intr: Boolean
  ): Int = {
    val pollFirst = prepareBlockingCall(intr, pos)
    // POLLOUT promises PIPE_BUF bytes to a pipe, not to a terminal.
    val chunk = if (pollFirst) pipeBuf else count
    var written = 0
    var n = 1L
    while (n > 0 && written < count) {
      val len = Math.min(count - written, chunk)
      val at = if (pos < 0) pos else pos + written
      n = blockingCall(write = true, buf + written, len, at, pollFirst, intr)
      if (n > 0) written += n.toInt
    }
    if (n < 0 && written == 0) throw new AsynchronousCloseException()
    written
  }

  /* One read or write (pread/pwrite if pos >= 0), retried on EINTR. Channel
   * calls also wait out EAGAIN; -1 once close() stops them.
   */
  private def blockingCall(
      write: Boolean,
      buf: Ptr[Byte],
      len: Int,
      pos: Long,
      pollFirst: Boolean,
      intr: Boolean
  ): Long = {
    val fdNum = fd.fd
    val events = if (write) POLLOUT else POLLIN
    var result = Retry
    while (result == Retry) {
      if (pollFirst) awaitReady(fdNum, events)
      if (closing(intr)) result = -1L
      else {
        val n =
          if (write) {
            if (pos < 0) unistd.write(fdNum, buf, len.toUInt).toLong
            else FileChannelNative.pwrite(fdNum, buf, len.toCSize, pos).toLong
          } else {
            if (pos < 0) unistd.read(fdNum, buf, len.toUInt).toLong
            else FileChannelNative.pread(fdNum, buf, len.toCSize, pos).toLong
          }
        if (n >= 0) result = n
        else {
          val e = errno
          // Streams fail on EAGAIN: close() could not wake their poll().
          if (intr && (e == EAGAIN || e == EWOULDBLOCK))
            awaitReady(fdNum, events)
          else if (e != EINTR) throw UnixException(getFileName(), e)
        }
      }
    }
    result
  }

  /* Makes a relative channel call wakeable: sets O_NONBLOCK, or returns true
   * to poll first. pread/pwrite need neither; they fail at once on pipes.
   */
  private def prepareBlockingCall(intr: Boolean, pos: Long): Boolean =
    intr && pos < 0 && {
      val k = kind
      if (k == KindNonBlocking) enableNonBlocking()
      k == KindPollFirst
    }

  // See Kind*. Call with a lease held.
  private def kind: Int = {
    if (kindCache == KindUnknown) kindCache = computeKind()
    kindCache
  }

  private def computeKind(): Int =
    if (isWindows) {
      if (GetFileType(fd.handle) == FILE_TYPE_DISK) KindRegular
      else KindCancellable
    } else if (isRegularFd(fd.fd)) KindRegular
    else KindPollFirst

  // Unix: PIPE_BUF for this fd. Call with a lease held.
  private def pipeBuf: Int = {
    if (pipeBufCache == 0) {
      val n = unistd.fpathconf(fd.fd, unistd._PC_PIPE_BUF)
      pipeBufCache = if (n > 0) n.toInt else 512
    }
    pipeBufCache
  }

  // KindNonBlocking only. Racing callers may both set it; harmless.
  private def enableNonBlocking(): Unit =
    if (!nonBlocking) {
      val flags = fcntl(fd.fd, F_GETFL, 0)
      if (flags == -1 || fcntl(fd.fd, F_SETFL, flags | O_NONBLOCK) == -1)
        throw new IOException("Cannot make the file descriptor non-blocking")
      nonBlocking = true
    }

  // Waits until fdNum is ready or close() starts.
  private def awaitReady(fdNum: Int, events: Int): Unit = {
    val fds = stackalloc[struct_pollfd](2)
    fds.fd = fdNum
    fds.events = events.toShort
    val wake = fds + 1
    wake.fd = shared.wakeFd()
    wake.events = POLLIN.toShort
    var ready = false
    while (!ready && !shared.isClosing) {
      val n = poll(fds, 2.toUInt, -1)
      if (n > 0) ready = true
      else if (n < 0 && errno != EINTR)
        throw UnixException(getFileName(), errno)
    }
  }

  override def size(): Long = {
    ensureOpen()
    interruptibly(leased(intr = true)(sizeNow()))
  }

  private def sizeNow(): Long =
    if (isWindows) {
      val size = stackalloc[windows.LargeInteger]()
      if (!GetFileSizeEx(fd.handle, size))
        throw WindowsException.onPath(getFileName())
      (!size).toLong
    } else
      Zone.acquire { implicit z =>
        val statBuf = alloc[stat.stat]() // too large for the stack
        if (stat.fstat(fd.fd, statBuf) != 0)
          throwPosixException("fstat")
        statBuf.st_size.toLong
      }

  /* An interrupt during a transfer closes both channels. Only this channel's
   * steps hold its lease, so waiting on the other never keeps it open.
   */
  private def closeBothIfInterrupted[T](other: Channel)(body: => T): T =
    try body
    catch {
      case e: ClosedByInterruptException =>
        closeQuietly(this)
        closeQuietly(other)
        throw e
    }

  private def closeQuietly(channel: Channel): Unit =
    try channel.close()
    catch { case _: IOException => () }

  override def transferFrom(
      src: ReadableByteChannel,
      _position: Long,
      count: Long
  ): Long = {
    ensureOpenForWrite()

    if (_position < 0L)
      throw new IllegalArgumentException("Negative position")

    if (count < 0L)
      throw new IllegalArgumentException("Negative count")

    closeBothIfInterrupted(src)(transferFromNow(src, _position, count))
  }

  private def transferFromNow(
      src: ReadableByteChannel,
      _position: Long,
      count: Long
  ): Long = {
    // An interrupt closes the channels even if nothing is transferred.
    interruptibly(())

    // Positional writes leave the channel position unchanged. Like the JVM,
    // a position past the end of file grows the file.
    val maxBufSize = 8 * 1024 // value used by JVM
    val bufSize =
      if (count > Integer.MAX_VALUE) maxBufSize
      else Math.min(count.toInt, maxBufSize)

    val buf = ByteBuffer.allocate(bufSize)
    var totalWritten = 0L
    var done = false

    while ((!done) && (totalWritten < count)) {
      buf.clear()
      // Bounding the limit is key to not reading/writing too many bytes.
      val nRemaining = count - totalWritten
      if (nRemaining < bufSize)
        buf.limit(nRemaining.toInt)

      // Stop at end of stream, or when a nonblocking source has no data.
      if (src.read(buf) <= 0) {
        done = true
      } else {
        buf.flip()
        while (!done && buf.hasRemaining()) {
          val n = interruptibly(writeAt(buf, _position + totalWritten))
          totalWritten += n
          if (n == 0) done = true
        }
      }
    }

    totalWritten
  }

  override def transferTo(
      _position: Long,
      count: Long,
      target: WritableByteChannel
  ): Long = {
    ensureOpenForRead()

    if (_position < 0L)
      throw new IllegalArgumentException("Negative position")

    if (count < 0L)
      throw new IllegalArgumentException("Negative count")

    closeBothIfInterrupted(target)(transferToNow(_position, count, target))
  }

  private def transferToNow(
      _position: Long,
      count: Long,
      target: WritableByteChannel
  ): Long =
    if (_position > size()) {
      0L
    } else {
      val maxBufSize = 8 * 1024 // value used by JVM
      val bufSize =
        if (count > Integer.MAX_VALUE) maxBufSize
        else Math.min(count.toInt, maxBufSize)

      val buf = ByteBuffer.allocate(bufSize)

      var totalWritten = 0L
      var done = false

      while ((!done) && (totalWritten < count)) {
        buf.clear()
        // Bounding the limit is key to not reading/writing too many bytes.
        val nRemaining = count - totalWritten
        if (nRemaining < bufSize)
          buf.limit(nRemaining.toInt)

        if (interruptibly(readAt(buf, _position + totalWritten)) == -1) {
          done = true
        } else {
          buf.flip()
          while (buf.hasRemaining())
            totalWritten += target.write(buf)
        }
      }

      totalWritten
    }

  private def lengthen(newFileSize: Long): Unit = {
    /* Preconditions: only caller, this.map(),  has ensured:
     *   - newFileSize > currentSize
     *   - file was opened for writing.
     *   - "this" channel is open, the fd is leased, the position lock held.
     */
    if (!isWindows) {
      val status = FileChannelNative.ftruncate(fd.fd, newFileSize)
      if (status < 0)
        throwPosixException("ftruncate")
    } else {
      val currentPos = currentPosition()

      val hasSucceded =
        FileApi.SetFilePointerEx(
          fd.handle,
          newFileSize,
          null,
          FILE_BEGIN
        ) &&
          FileApi.SetEndOfFile(fd.handle)

      if (!hasSucceded)
        throw new IOException("Failed to lengthen file")

      /* Windows doc states that the content of the bytes between the
       * currentPosition and the new end of file is undefined.
       * In practice, NTFS will zero those bytes. The next step is redundant
       * if one is _sure_ the file system is NTFS.
       *
       * Write a single byte to just before EOF to convince the
       * Windows file systems to actualize and zero the undefined blocks.
       */
      compelPosition(newFileSize - 1)
      writeArray(Array[Byte](0.toByte), 0, 1, intr = false)
      restorePosition(currentPos)
    }

    /* This next step may not be strictly necessary; it is included for the
     * sake of robustness across as yet unseen Operating & File systems.
     * The sync can be re-visited and  micro-optimized if performance becomes a
     * concern.
     *
     * Most contemporary Operating and File systems will have ensured that
     * the changes above are in non-volatile storage by the time execution
     * reaches here.
     *
     * Give those corner cases where this is not so a strong hint that it
     * should be. If the data is already non-volatile, this should be as
     * fast as a kernel call can be.
     */
    forceNow(true)
  }

  override def truncate(newSize: Long): FileChannel = {
    checkSize(newSize)
    interruptibly(truncateNow(newSize, intr = true))
    this
  }

  private def truncateNow(newSize: Long, intr: Boolean): Unit =
    locked(intr) {
      val position = currentPosition()

      if (newSize < sizeNow()) {
        if (isWindows) {
          val hasSucceded =
            FileApi.SetFilePointerEx(
              fd.handle,
              newSize,
              null,
              FILE_BEGIN
            ) &&
              FileApi.SetEndOfFile(fd.handle)
          if (!hasSucceded)
            throw new IOException("Failed to truncate file")
        } else {
          val err = FileChannelNative.ftruncate(fd.fd, newSize)
          if (err != 0)
            throwPosixException("ftruncate")
        }
      }

      // Windows moved the file pointer to newSize.
      compelPosition(Math.min(position, newSize))
    }

  // Precondition: arguments checked, the fd is leased.
  private def writeArray(
      array: Array[Byte],
      offset: Int,
      count: Int,
      intr: Boolean
  ): Int =
    if (count == 0) 0
    else if (isWindows) {
      val buf = array.at(offset) // no intermediate buffer
      val n = stackalloc[DWord]()
      // Loop until all is written or close() stops a channel operation.
      var written = 0
      var closed = false
      while (!closed && written < count) {
        if (closing(intr)) closed = true
        else if (!WriteFile(
              fd.handle,
              buf + written,
              (count - written).toUInt,
              n,
              null
            )) {
          val error = ErrorHandlingApi.GetLastError()
          // Cancelled by close(): the loop rechecks closing.
          if (error != ErrorCodes.ERROR_OPERATION_ABORTED)
            throw WindowsException.onPathWithLastEror(
              getFileName("<file descriptor>"),
              error
            )
        } else {
          written += (!n).toInt
          // A full nonblocking pipe accepts nothing; wait for the reader.
          if (!n == 0.toUInt) SynchApi.Sleep(1.toUInt)
        }
      }
      if (closed && written == 0) throw new AsynchronousCloseException()
      written
    } else unixWrite(array.at(offset), count, -1L, intr)

  // since all of Java packages can call this, be stricter with argument checks.
  private[java] def write(
      buffer: Array[Byte],
      offset: Int,
      count: Int
  ): Int = {
    Objects.requireNonNull(buffer, "buffer")

    if ((offset < 0) || (count < 0) || (count > buffer.length - offset))
      throw new IndexOutOfBoundsException

    val written = locked(intr = false) {
      seekEOFBeforeRelativeWrite()
      writeArray(buffer, offset, count, intr = false)
    }
    // Streams must write everything; channels may return a short count.
    if (written < count)
      throw new IOException(s"Wrote only $written of $count bytes")
    written
  }

  private def writeByteBuffer(src: ByteBuffer): Int =
    writeByteBuffer(src, writeArray(_, _, _, intr = true))

  private def writeByteBuffer(
      src: ByteBuffer,
      writeArray: (Array[Byte], Int, Int) => Int
  ): Int = {
    // Precondition: caller has ensured that channel is open and open for write
    val srcPos = src.position()
    val srcLim = src.limit()
    val nBytes = srcLim - srcPos // number of bytes in range.

    val (arr, offset) = if (src.hasArray()) {
      (src.array(), src.arrayOffset() + srcPos)
    } else {
      val ba = new Array[Byte](nBytes)
      src.get(srcPos, ba, 0, nBytes) // src moves only by what is written
      (ba, 0)
    }

    val nWritten = writeArray(arr, offset, nBytes)

    /* Advance the srcPos only by the number of bytes actually written.
     * This allows higher level callers to re-try partial writes
     * in a 'natural' manner (no buffer futzing required).
     */
    src.position(srcPos + nWritten)

    nWritten
  }

  override def write(
      srcs: Array[ByteBuffer],
      offset: Int,
      length: Int
  ): Long = {
    Objects.requireNonNull(srcs, "srcs")
    Objects.checkFromIndexSize(offset, length, srcs.length)

    ensureOpenForWrite()

    interruptibly(locked(intr = true) {
      var i = 0
      var partialWriteSeen = false
      var totalWritten = 0L
      while (i < length && !partialWriteSeen) {
        val src = srcs(offset + i)
        val len = src.remaining()

        seekEOFBeforeRelativeWrite()
        val n =
          try writeByteBuffer(src)
          catch { case _: AsynchronousCloseException if totalWritten > 0 => 0 }

        totalWritten += n
        if (n < len) {
          partialWriteSeen = true
        }
        i += 1
      }

      totalWritten
    })
  }

  /* Write to absolute position, do not change current position.
   *
   * Understanding "does not change current position" when the channel
   * has been opened requires some mind_bending/understanding.
   *
   * "Current position" when file has been opened for APPEND is
   * a logical place, End of File (EOF), not an absolute number.
   * When APPEND mode changes the position it reports as "current" to the
   * new EOF rather than stashed position, according to JVM it is not
   * really changing the "current position".
   *
   * With APPEND, Linux appends and macOS writes at `pos`, as on the JVM.
   */
  override def write(src: ByteBuffer, pos: Long): Int = {
    if (pos < 0)
      throw new IllegalArgumentException("Negative position")

    ensureOpenForWrite()
    interruptibly(writeAt(src, pos))
  }

  private def writeAt(src: ByteBuffer, pos: Long): Int =
    if (isWindows)
      atPosition(pos)(writeByteBuffer(src))
    else
      leased(intr = true) {
        writeByteBuffer(src, pwriteArray(_, _, _, pos))
      }

  // Write relative to current position (SEEK_CUR) or, for APPEND, SEEK_END.
  override def write(src: ByteBuffer): Int = {
    ensureOpenForWrite()
    interruptibly(locked(intr = true) {
      seekEOFBeforeRelativeWrite()
      writeByteBuffer(src)
    })
  }

  /* The Scala Native implementation of FileInputStream#available delegates
   * to this method. This method now implements "available()" as described in
   * the Java description of FileInputStream#available. So the delegator
   * now matches its JDK description and behavior (Issue 3333).
   *
   * There are a couple of fine points to this implementation which might
   * be helpful to know:
   *    1) There is no requirement that this method itself not block.
   *       Indeed, depending upon what, if anything, is in the underlying
   *       file system cache, this method may do so.
   *
   *       The current position should already be in the underlying OS fd but
   *       calling "size()" may require reading an inode or equivalent.
   *
   *    2) Given JVM actual behavior, the "read (or skipped over) from this
   *       input stream without blocking" clause of the JDK description might
   *       be better read as "without blocking for additional data bytes".
   *
   *       A "skip()" should be a fast update of existing memory. Conceptually,
   *       and by JDK definition FileChannel "read()"s may block transferring
   *       bytes from slow storage to memory.
   *
   *    3) The value returned is exactly the "estimate" portion of the JDK
   *       description:
   *
   *       - All bets are off is somebody, even this thread, decreases
   *         size of the file in the interval between when "available()"
   *         returns and "read()" is called.
   *
   *         FileChannel reads() after such truncation may violate the
   *         description & contract by blocking for significant amounts
   *         of time
   *
   *         Lifting this restriction is not easy and is left as an
   *         exercise for the reader.
   */

  // local API extension, but follows description of FileInputStream#available
  private[java] def available(): Int = {
    ensureOpen()
    // Windows files compute it from the position, which needs the lock.
    val usesPosition = if (isWindows) file.isDefined else false
    if (usesPosition) locked(intr = false)(availableNow())
    else leased(intr = false)(availableNow())
  }

  // Precondition: the fd is leased; for files on Windows, the position lock.
  private def availableNow(): Int =
    if (!isWindows) {
      val res = stackalloc[CInt]()
      val resByte = res.asInstanceOf[Ptr[scala.Byte]]
      val failed = ioctl.ioctl(fd.fd, ioctl.FIONREAD, resByte) == -1
      if (failed) 0 else Math.max(!res, 0)
    } else if (file.isEmpty) { // pipe
      val availableTotal = stackalloc[DWord]()
      val failed = !NamedPipeApi.PeekNamedPipe(
        pipe = fd.handle,
        buffer = null,
        bufferSize = 0.toUInt,
        bytesRead = null,
        totalBytesAvailable = availableTotal,
        bytesLeftThisMessage = null
      )
      if (failed) 0
      else Math.min((!availableTotal).toLong, Int.MaxValue).toInt
    } else {
      Math.clamp((sizeNow() - currentPosition()), 0, Int.MaxValue)
    }

  // FileInputStream.skip seeks only regular files, like JDK 25.
  private[java] def isRegularFile: Boolean =
    leased(intr = false)(kind == KindRegular)

  // Moves the position by n, possibly past the end of file or backward.
  private[java] def skip(n: Long): Long =
    locked(intr = false) {
      if (isWindows) {
        if (!SetFilePointerEx(fd.handle, n, null, FILE_CURRENT))
          throw WindowsException.onPath(getFileName())
      } else if (FileChannelNative.lseek(fd.fd, n, stdio.SEEK_CUR) < 0)
        throw UnixException(getFileName(), errno)
      n
    }
}

private[java] object FileChannelImpl {
  private final val Retry = -2L

  // Backoff of a lock() waiting for another process.
  private final val LockMinDelayNanos = 1000L * 1000
  private final val LockMaxDelayNanos = 50L * 1000 * 1000

  // Modes of FileChannelNative.lock.
  private final val LockUnlock = 0
  private final val LockShared = 1
  private final val LockExclusive = 2

  @inline private def low(v: Long): DWord = (v & 0xffffffffL).toInt.toUInt
  @inline private def high(v: Long): DWord = (v >>> 32).toInt.toUInt

  private final val KindUnknown = 0
  // Regular or disk file: I/O finishes on its own, no waking needed.
  private final val KindRegular = 1
  // Unix, private description: O_NONBLOCK and poll with the wake pipe.
  private final val KindNonBlocking = 2
  // Unix, shared description: poll with the wake pipe before each call.
  private final val KindPollFirst = 3
  // Windows pipe or console: woken by CancelSynchronousIo.
  private final val KindCancellable = 4

  // Streams over a channel (Files, RandomAccessFile) ignore interrupts.
  private[java] def newInputStream(channel: FileChannelImpl): InputStream =
    new InputStream {
      override def read(): Int = {
        val b = new Array[Byte](1)
        if (channel.read(b, 0, 1) <= 0) -1 else b(0) & 0xff
      }
      override def read(b: Array[Byte], off: Int, len: Int): Int =
        channel.read(b, off, len)
      override def available(): Int = channel.available()
      override def close(): Unit = channel.close()
    }

  private[java] def newOutputStream(channel: FileChannelImpl): OutputStream =
    new OutputStream {
      override def write(b: Int): Unit =
        channel.write(Array(b.toByte), 0, 1)
      override def write(b: Array[Byte], off: Int, len: Int): Unit =
        channel.write(b, off, len)
      override def close(): Unit = channel.close()
    }

  private[java] final class OpenOptions(
      val read: Boolean,
      val write: Boolean,
      val append: Boolean,
      val truncate: Boolean,
      val create: Boolean,
      val createNew: Boolean,
      val sync: Boolean,
      val dsync: Boolean,
      val noFollowLinks: Boolean
  )

  private[java] def openPath(
      path: Path,
      options: OpenOptions,
      attrs: Array[FileAttribute[_]],
      deleteFileOnClose: Boolean
  ): FileChannelImpl = {
    val creating = options.create || options.createNew
    val mode = if (creating) Files.creationMode(attrs, default = 0x1b6) else 0
    val pathString = path.toString()
    val fd =
      if (isWindows) openWindows(pathString, options)
      else openUnix(pathString, options, mode)
    // Opened here with O_CLOEXEC, so O_NONBLOCK is safe unless shared.
    val kind =
      if (isWindows) KindUnknown
      else if (isRegularFd(fd.fd)) KindRegular
      else if (Zone.acquire(implicit z => reopensDescription(pathString)))
        KindPollFirst
      else KindNonBlocking
    new FileChannelImpl(
      fd,
      Some(path.toFile()),
      deleteFileOnClose = deleteFileOnClose,
      openForReading = options.read,
      openForWriting = options.write,
      openForAppending = options.append,
      initialKind = kind
    )
  }

  private def openUnix(
      path: String,
      options: OpenOptions,
      mode: Int
  ): FileDescriptor = Zone.acquire { implicit z =>
    var flags =
      if (options.read && options.write) O_RDWR
      else if (options.write) O_WRONLY
      else O_RDONLY
    flags |= O_CLOEXEC
    if (options.append) flags |= O_APPEND
    if (options.truncate) flags |= O_TRUNC
    if (options.createNew) flags |= O_CREAT | O_EXCL
    else if (options.create) flags |= O_CREAT
    if (options.sync) flags |= O_SYNC
    else if (options.dsync) flags |= O_DSYNC
    if (options.noFollowLinks) flags |= O_NOFOLLOW

    val fd = open(toCString(path), flags, mode.toUInt)
    if (fd == -1) {
      val err = errno
      throw {
        if (err == EACCES || err == EPERM) new AccessDeniedException(path)
        else if (err == ENOENT) new NoSuchFileException(path)
        else if (err == EEXIST) new FileAlreadyExistsException(path)
        else if (err == ELOOP && options.noFollowLinks)
          new FileSystemException(
            path,
            null,
            s"${LibcExt.strError(err)} (NOFOLLOW_LINKS specified)"
          )
        else new FileSystemException(path, null, LibcExt.strError(err))
      }
    }
    new FileDescriptor(fd, readOnly = !options.write)
  }

  private def isRegularFd(fd: Int): Boolean = Zone.acquire { implicit z =>
    val st = alloc[stat.stat]()
    stat.fstat(fd, st) == 0 && stat.S_ISREG(st.st_mode) != 0
  }

  /* macOS and BSD: opening /dev/fd/N (or /dev/stdin, or a link to them)
   * shares the description of fd N. Unresolvable paths count as shared.
   */
  private def reopensDescription(path: String)(implicit z: Zone): Boolean = {
    val resolved = alloc[Byte](limits.PATH_MAX)
    realpath(toCString(path), resolved) == null || {
      val real = fromCString(resolved)
      real.startsWith("/dev/fd/") || real.startsWith("/dev/std")
    }
  }

  private def openWindows(
      path: String,
      options: OpenOptions
  ): FileDescriptor = Zone.acquire { implicit z =>
    import scala.scalanative.windows.winnt.AccessRights._

    // APPEND keeps write access so that truncate() works.
    var access = 0.toUInt
    if (options.read) access |= FILE_GENERIC_READ
    if (options.write) access |= FILE_GENERIC_WRITE
    // The symbolic link check reads file attributes.
    if (options.noFollowLinks) access |= FILE_READ_ATTRIBUTES

    /* Truncation happens after opening: CREATE_ALWAYS fails on hidden and
     * system files, and a NOFOLLOW_LINKS check must come first.
     */
    val disposition =
      if (options.createNew) CREATE_NEW
      else if (options.create) OPEN_ALWAYS
      else OPEN_EXISTING

    var flagsAndAttributes = FILE_ATTRIBUTE_NORMAL
    if (options.sync || options.dsync)
      flagsAndAttributes |= FILE_FLAG_WRITE_THROUGH
    if (options.noFollowLinks)
      flagsAndAttributes |= FILE_FLAG_OPEN_REPARSE_POINT

    val handle = CreateFileW(
      toCWideStringUTF16LE(path),
      desiredAccess = access,
      shareMode = FILE_SHARE_READ | FILE_SHARE_WRITE,
      securityAttributes = null,
      creationDisposition = disposition,
      flagsAndAttributes = flagsAndAttributes,
      templateFile = null
    )
    if (handle == HandleApiExt.INVALID_HANDLE_VALUE) {
      val err = ErrorHandlingApi.GetLastError()
      throw {
        if (err == ErrorCodes.ERROR_FILE_EXISTS ||
            err == ErrorCodes.ERROR_ALREADY_EXISTS)
          new FileAlreadyExistsException(path)
        else WindowsException.onPathWithLastEror(path, err)
      }
    }

    def fail(e: Exception): Nothing = {
      HandleApi.CloseHandle(handle)
      throw e
    }
    if (options.noFollowLinks) {
      // Only symbolic links; other reparse points are regular files.
      val isLink = FileApi.isSymbolicLink(handle)
      if (isLink < 0) fail(WindowsException.onPath(path))
      if (isLink == 1)
        fail(
          new FileSystemException(
            path,
            null,
            "Is a symbolic link (NOFOLLOW_LINKS specified)"
          )
        )
    }
    if (options.truncate && !SetEndOfFile(handle))
      fail(WindowsException.onPath(path))

    new FileDescriptor(handle, readOnly = !options.write)
  }
}
