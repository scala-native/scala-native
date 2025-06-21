package java.nio.channels

import java.nio.{ByteBuffer, MappedByteBuffer, MappedByteBufferImpl}
import java.nio.channels.FileChannel.MapMode
import java.nio.file.Files
import java.nio.file.WindowsException

import scala.scalanative.nio.fs.unix.UnixException

import java.io.FileDescriptor
import java.io.File
import java.io.IOException

import java.util.Objects

import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.unsafe._

import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.fcntlOps._
import scala.scalanative.posix.string

import scala.scalanative.posix.sys.stat
import scala.scalanative.posix.sys.statOps._

import scala.scalanative.posix.unistd

import scala.scalanative.unsigned._
import scala.scalanative.windows
import scalanative.libc.stdio
import scala.scalanative.libc.errno.errno

import scala.scalanative.windows.ErrorHandlingApi
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.ErrorCodes
import scala.scalanative.windows.MinWinBaseApi._
import scala.scalanative.windows.MinWinBaseApiOps._
import scala.scalanative.windows._

private[java] final class FileChannelImpl(
    fd: FileDescriptor,
    file: Option[File],
    deleteFileOnClose: Boolean,
    openForReading: Boolean,
    openForWriting: Boolean,
    openForAppending: Boolean = false
) extends FileChannel {
  /* Note:
   *   Channels are described in the Java documentation as thread-safe.
   *   This implementation is, most patently _not_ thread-safe.
   *   Use with only one thread accessing the channel, even for READS.
   */

  if (openForAppending)
    seekEOF() // so a position() before first APPEND write() matches JVM.

  private def ensureOpen(): Unit =
    if (!isOpen()) throw new ClosedChannelException()

  private def ensureOpenForWrite(): Unit = {
    ensureOpen()
    if (!openForWriting)
      throw new NonWritableChannelException()
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
      val pos = unistd.lseek(fd.fd, 0, stdio.SEEK_END);
      if (pos < 0)
        throwPosixException("lseek")
    }
  }

  private def throwPosixException(functionName: String): Unit = {
    if (!isWindows) {
      val errnoString = fromCString(string.strerror(errno))
      throw new IOException(s"${functionName} failed: ${errnoString}")
    }
  }

  override def force(metadata: Boolean): Unit =
    fd.sync()

  @inline private def assertIfCanLock(): Unit =
    ensureOpenForWrite()

  override def tryLock(
      position: Long,
      size: Long,
      shared: Boolean
  ): FileLock = {
    assertIfCanLock()
    if (isWindows) {
      val flag = if (shared) 0.toUInt else LOCKFILE_EXCLUSIVE_LOCK
      lockWindows(position, size, flag)
    } else lockUnix(position, size, shared, F_SETLK)
  }

  override def lock(position: Long, size: Long, shared: Boolean): FileLock = {
    assertIfCanLock()
    if (isWindows) {
      val flag: DWord = if (shared) 0.toUInt else LOCKFILE_EXCLUSIVE_LOCK
      lockWindows(position, size, LOCKFILE_FAIL_IMMEDIATELY | flag)
    } else lockUnix(position, size, shared, F_SETLKW)
  }

  @inline private def lockUnix(
      position: Long,
      size: Long,
      shared: Boolean,
      command: CInt
  ): FileLock = {
    val fl = stackalloc[flock]()
    fl.l_start = position.toSize
    fl.l_len = size.toSize
    fl.l_pid = 0
    fl.l_type = F_WRLCK
    fl.l_whence = stdio.SEEK_SET
    if (fcntl(fd.fd, command, fl) == -1) {
      throw new IOException("Could not lock file")
    }
    new FileLockImpl(this, position, size, shared, fd)
  }

  @inline private def lockWindows(
      position: Long,
      size: Long,
      flags: DWord
  ): FileLock = {

    val dummy = stackalloc[DUMMYSTRUCTNAME]()
    dummy.Offset = position.toInt.toUInt
    dummy.OffsetHigh = (position >> 32).toInt.toUInt
    val overlapped = stackalloc[OVERLAPPED]()
    overlapped.Internal = 0.toULong
    overlapped.InternalHigh = 0.toULong
    overlapped.DUMMYSTRUCTNAME = !dummy
    overlapped.hEvent = fd.handle

    if (!LockFileEx(
          fd.handle,
          flags,
          0.toUInt,
          size.toInt.toUInt,
          (size >> 32).toInt.toUInt,
          overlapped
        )) throw new IOException("Could not lock file")
    new FileLockImpl(this, position, size, true, fd)
  }

  override protected def implCloseChannel(): Unit = {
    if (!isOpen()) {
      fd.close()
      if (deleteFileOnClose && file.isDefined) Files.delete(file.get.toPath())
    }
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
     * toInt() would cause such a large value to rollover to a negative value.
     *
     * Call to MappedByteBufferImpl() below truncates its third argument
     * to an Int, knowing this guard is in place.
     *
     * Java is playing pretty fast & loose with its Ints & Longs, but that is
     * the specification & practice that needs to be followed.
     */

    if ((size < 0) || (size > Integer.MAX_VALUE))
      throw new IllegalArgumentException("Negative size")

    ensureOpen()

    if (mode ne MapMode.READ_ONLY) {
      // FileChannel.open() has previously rejected READ + APPEND combination.
      if (!openForWriting)
        throw new NonWritableChannelException

      // This "lengthen" branch is tested/exercised in MappedByteBufferTest.
      // Look in MappedByteBufferTest for tests of this "lengthen" block.
      val currentFileSize = this.size()
      // Detect Long overflow & throw. Room for improvement here.
      val newFileSize = Math.addExact(position, size)
      if (newFileSize > currentFileSize)
        this.lengthen(newFileSize)
    }

    // RE: toInt() truncation safety, see note for "size" arg checking above.
    MappedByteBufferImpl(mode, position, size.toInt, fd)
  }

  // change position, even in APPEND mode. Use _carefully_.
  private def compelPosition(offset: Long): FileChannel = {
    if (isWindows)
      FileApi.SetFilePointerEx(
        fd.handle,
        offset,
        null,
        FILE_BEGIN
      )
    else {
      val pos = unistd.lseek(fd.fd, offset.toSize, stdio.SEEK_SET)
      if (pos < 0)
        throwPosixException("lseek")
    }

    this
  }

  override def position(offset: Long): FileChannel = {
    if (!openForAppending)
      compelPosition(offset)

    this
  }

  override def position(): Long =
    if (isWindows) {
      val filePointer = stackalloc[LargeInteger]()
      FileApi.SetFilePointerEx(
        fd.handle,
        0,
        filePointer,
        FILE_CURRENT
      )
      !filePointer
    } else {
      val pos = unistd.lseek(fd.fd, 0, stdio.SEEK_CUR).toLong
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

    ensureOpen()

    var i = 0
    var partialReadSeen = false
    var totalRead = 0L
    while (i < number && !partialReadSeen) {
      val dst = buffers(start + i)
      val len = dst.remaining()

      val bs = new Array[Byte](len)
      val n = read(bs, 0, len)

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
  }

  override def read(buffer: ByteBuffer, pos: Long): Int = {
    ensureOpen()
    position(pos)
    val bufPosition: Int = buffer.position()
    read(buffer.array(), bufPosition, buffer.limit() - bufPosition) match {
      case bytesRead if bytesRead < 0 =>
        bytesRead
      case bytesRead =>
        buffer.position(bufPosition + bytesRead)
        bytesRead
    }
  }

  override def read(buffer: ByteBuffer): Int = {
    read(buffer, position())
  }

  private[java] def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
    if (buffer == null) {
      throw new NullPointerException
    }
    if (offset < 0 || count < 0 || count > buffer.length - offset) {
      throw new IndexOutOfBoundsException
    }
    if (count == 0) {
      return 0
    }

    // we use the runtime knowledge of the array layout to avoid
    // intermediate buffer, and write straight into the array memory
    val buf = buffer.at(offset)
    if (isWindows) {
      def fail() = throw WindowsException.onPath(file.fold("")(_.toString))

      def tryRead(count: Int)(fallback: => Int) = {
        val readBytes = stackalloc[windows.DWord]()
        if (ReadFile(fd.handle, buf, count.toUInt, readBytes, null)) {
          (!readBytes).toInt match {
            case 0     => -1 // EOF
            case bytes => bytes
          }
        } else fallback
      }

      tryRead(count)(fallback = {
        ErrorHandlingApi.GetLastError() match {
          case ErrorCodes.ERROR_BROKEN_PIPE =>
            // Pipe was closed, but it still can contain some unread data
            available() match {
              case 0     => -1 // EOF
              case count => tryRead(count)(fallback = fail())
            }

          case _ =>
            fail()
        }
      })

    } else {
      val readCount = unistd.read(fd.fd, buf, count.toUInt)
      if (readCount == 0) {
        -1 // end of file
      } else if (readCount < 0) {
        // negative value (typically -1) indicates that read failed
        throw UnixException(file.fold("")(_.toString), errno)
      } else {
        // successfully read readCount bytes
        readCount
      }
    }
  }

  override def size(): Long = {
    if (isWindows) {
      val size = stackalloc[windows.LargeInteger]()
      if (GetFileSizeEx(fd.handle, size)) (!size).toLong
      else 0L
    } else
      Zone.acquire { implicit z =>
        /* statbuf is too large to be thread stack friendly.
         * Even a Zone and an alloc() per size() call should be cheaper than
         * the required three (yes 3 to get it right and not move current
         * position) lseek() calls. Room for performance improvements remain.
         */

        val statBuf = alloc[stat.stat]()

        val err = stat.fstat(fd.fd, statBuf)
        if (err != 0)
          throwPosixException("fstat")

        statBuf.st_size.toLong
      }
  }

  /* The pair transferFrom() and transferTo() and quite similar and
   * beg calling into a common implementation.
   *
   * They differ just enough in how they must handle the position of
   * the 'this' FileChannel that such a common implementation exceeds the
   * day is left for the reader.
   *
   * Useful example:
   *   URL: https://www.happycoders.eu/java/filechannel-memory-mapped-io-locks/
   */

  override def transferFrom(
      src: ReadableByteChannel,
      _position: Long,
      count: Long
  ): Long = {
    if (_position < 0L)
      throw new IllegalArgumentException("Negative position")

    if (count < 0L)
      throw new IllegalArgumentException("Negative count")

    if (_position > this.size()) {
      0L
    } else {
      ensureOpen()

      val maxBufSize = 8 * 1024 // value used by JVM
      val bufSize =
        if (count > Integer.MAX_VALUE) maxBufSize
        else Math.min(count.toInt, maxBufSize)

      val buf = ByteBuffer.allocate(bufSize)

      var totalWritten = 0L
      var done = false

      while ((!done) && (totalWritten < count)) {
        val nRemaining = count - totalWritten
        if ((nRemaining) < bufSize)
          buf.limit(nRemaining.toInt) // Enable next partial buffer short read

        val nRead = src.read(buf)
        if (nRead == -1) { // How should repeating/looping 0 reads be handled?
          done = true
        } else {
          buf.flip()

          /* Using absolute write-at-position takes math but avoids
           * set, save, and then restore of position. That overload
           * does all that work already.
           */
          val nWritten = this.write(buf, _position + totalWritten)
          buf.compact()

          totalWritten = totalWritten + nWritten
        }

      }

      buf.flip()
      while (buf.hasRemaining()) {
        val nDrained = this.write(buf, _position + totalWritten)
        totalWritten += nDrained
      }

      totalWritten
    }
  }

  // See comment about lack of common code before transferFrom()

  override def transferTo(
      _position: Long,
      count: Long,
      target: WritableByteChannel
  ): Long = {
    if (_position < 0L)
      throw new IllegalArgumentException("Negative position")

    if (count < 0L)
      throw new IllegalArgumentException("Negative count")

    if (_position > this.size()) {
      0L
    } else {
      ensureOpen()

      val savedPosition = position()
      if (_position != savedPosition)
        position(_position)

      val maxBufSize = 8 * 1024 // value used by JVM
      val bufSize =
        if (count > Integer.MAX_VALUE) maxBufSize
        else Math.min(count.toInt, maxBufSize)

      val buf = ByteBuffer.allocate(bufSize)

      var totalWritten = 0L
      var done = false

      while ((!done) && (totalWritten < count)) {
        val nRemaining = count - totalWritten
        if (nRemaining < bufSize)
          buf.limit(nRemaining.toInt) // Enable next partial buffer short read

        val nRead = this.read(buf)
        if (nRead == -1) { // How should repeating/looping 0 reads be handled?
          done = true
        } else {
          buf.flip()

          val nWritten = target.write(buf)
          buf.compact()

          totalWritten = totalWritten + nWritten
        }
      }

      buf.flip()
      while (buf.hasRemaining()) {
        val nDrained = target.write(buf)
        totalWritten += nDrained
      }

      position(savedPosition)

      totalWritten
    }
  }

  private def lengthen(newFileSize: Long): Unit = {
    /* Preconditions: only caller, this.map(),  has ensured:
     *   - newFileSize > currentSize
     *   - file was opened for writing.
     *   - "this" channel is open
     */
    if (!isWindows) {
      val status = unistd.ftruncate(fd.fd, newFileSize.toSize)
      if (status < 0)
        throwPosixException("ftruncate")
    } else {
      val currentPosition = position()

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
      write(ByteBuffer.wrap(Array[Byte](0.toByte)), newFileSize - 1)

      position(currentPosition)
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
    force(true)
  }

  override def truncate(newSize: Long): FileChannel = {
    if (newSize < 0)
      throw new IllegalArgumentException("Negative size")

    ensureOpen()

    if (!openForWriting)
      throw new NonWritableChannelException()

    val currentPosition = position()

    if (newSize < size()) {
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
        val err = unistd.ftruncate(fd.fd, newSize.toSize)
        if (err != 0)
          throwPosixException("ftruncate")
      }

    }

    if (currentPosition > newSize)
      compelPosition(newSize)

    this
  }

  private def writeArray(
      array: Array[Byte],
      offset: Int,
      count: Int
  ): Int = {
    // Precondition: caller has checked arguments.

    val nWritten =
      if (count == 0) 0
      else {
        // we use the runtime knowledge of the array layout to avoid an
        // intermediate buffer, and read straight from the array memory.
        val buf = array.at(offset)

        if (isWindows) {
          val hasSucceded =
            WriteFile(fd.handle, buf, count.toUInt, null, null)
          if (!hasSucceded) {
            throw WindowsException.onPath(
              file.fold("<file descriptor>")(_.toString)
            )
          }

          count // Windows will fail on partial write, so nWritten == count
        } else {
          // unix-like may do partial writes, so be robust to them.
          val writeCount = unistd.write(fd.fd, buf, count.toUInt)

          if (writeCount < 0) {
            // negative value (typically -1) indicates that write failed
            throw UnixException(file.fold("")(_.toString), errno)
          }

          writeCount // may be < requested count
        }
      }

    nWritten
  }

  // since all of java package can call this, be stricter with argument checks.
  private[java] def write(
      buffer: Array[Byte],
      offset: Int,
      count: Int
  ): Int = {
    Objects.requireNonNull(buffer, "buffer")

    if ((offset < 0) || (count < 0) || (count > buffer.length - offset))
      throw new IndexOutOfBoundsException

    writeArray(buffer, offset, count)
  }

  private def writeByteBuffer(src: ByteBuffer): Int = {
    // Precondition: caller has ensured that channel is open and open for write
    val srcPos = src.position()
    val srcLim = src.limit()
    val nBytes = srcLim - srcPos // number of bytes in range.

    val (arr, offset) = if (src.hasArray()) {
      (src.array(), srcPos)
    } else {
      val ba = new Array[Byte](nBytes)
      src.get(ba, srcPos, nBytes)
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

    var i = 0
    var partialWriteSeen = false
    var totalWritten = 0
    while (i < length && !partialWriteSeen) {
      val src = srcs(offset + i)
      val len = src.remaining()

      val n = writeByteBuffer(src)

      totalWritten += n
      if (n < len) {
        partialWriteSeen = true
      }
      i += 1
    }

    totalWritten
  }

  /* Write to absolute position, do not change current position.
   *
   * Understanding "does not change current position" when the channel
   * has been opened requires some mind_bending/understanding.
   *
   * "Current position" when file has been opened for APPEND is
   * a logical place, End of File (EOF), not an absolute number.
   * When APPEND mode changes the position it reports as "current" to the
   * new EOF rather than stashed position, according to JVM is is not
   * really changing the "current position".
   */
  override def write(src: ByteBuffer, pos: Long): Int = {
    ensureOpenForWrite()
    val stashPosition = position()
    compelPosition(pos)

    val nBytesWritten = writeByteBuffer(src)

    if (!openForAppending)
      compelPosition(stashPosition)
    else
      seekEOF()

    nBytesWritten
  }

  // Write relative to current position (SEEK_CUR) or, for APPEND, SEEK_END.
  override def write(src: ByteBuffer): Int = {
    ensureOpenForWrite()
    writeByteBuffer(src)
  }

  /* The Scala Native implementation of FileInputStream#available delegates
   * to this method. This method now implements "available()" as described in
   * the Java description of FileInputStream#available. So the delegator
   * now matches the its JDK description and behavior (Issue 3333).
   *
   * There are a couple of fine points to this implemention which might
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
   *       bytes from slow storage to memory. Where is io_uring() when
   *       you need it?
   *
   *    3) The value returned is exactly the "estimate" portion of the JDK
   *       description:
   *
   *       - All bets are off is somebody, even this thread, decreases
   *         size of the file in the interval between when "available()"
   *         returns and "read()" is called.
   *
   *       - This method is defined in FileChannel#available as returning
   *         an Int. This also matches the use above in the Windows
   *         implementation of the private method
   *         "read(buffer: Array[Byte], offset: Int, count: Int)"
   *         Trace the count argument logic.
   *
   *         FileChannel defines "position()" and "size()" as Long values.
   *         For large files and positions < Integer.MAX_VALUE,
   *         The Long difference "lastPosition - currentPosition" might well
   *         be greater than Integer.MAX_VALUE. In that case, the .toInt
   *         truncation will return the low estimate of Integer.MAX_VALUE
   *         not the true (Long) value. Matches the specification, but gotcha!
   */

  // local API extension
  private[java] def available(): Int = {
    ensureOpen()

    val currentPosition = position()
    val lastPosition = size()

    val nAvailable =
      if (currentPosition >= lastPosition) 0
      else lastPosition - currentPosition

    nAvailable.toInt
  }
}
