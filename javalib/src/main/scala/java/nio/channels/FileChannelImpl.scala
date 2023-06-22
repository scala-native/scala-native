package java.nio.channels

import java.nio.file.Files

import java.nio.{ByteBuffer, MappedByteBuffer, MappedByteBufferImpl}
import java.nio.file.WindowsException
import scala.scalanative.nio.fs.unix.UnixException

import java.io.FileDescriptor
import java.io.File

import scala.scalanative.meta.LinktimeInfo.isWindows
import java.io.IOException

import scala.scalanative.posix.fcntl._
import scala.scalanative.posix.fcntlOps._
import scala.scalanative.libc.stdio
import scala.scalanative.unsafe._

import scala.scalanative.posix.unistd
import scala.scalanative.unsigned._
import scala.scalanative.{runtime, windows}
import scalanative.libc.stdio
import scala.scalanative.libc.errno

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
    openForWriting: Boolean
) extends FileChannel {
  override def force(metadata: Boolean): Unit =
    fd.sync()

  @inline private def assertIfCanLock(): Unit = {
    if (!isOpen()) throw new ClosedChannelException()
    if (!openForWriting) throw new NonWritableChannelException()
  }

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
    fl.l_start = position
    fl.l_len = size
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
    if ((mode eq FileChannel.MapMode.READ_ONLY) && !openForReading)
      throw new NonReadableChannelException
    if ((mode eq FileChannel.MapMode.READ_WRITE) && (!openForReading || !openForWriting))
      throw new NonWritableChannelException
    MappedByteBufferImpl(mode, position, size.toInt, fd, this)
  }

  override def position(offset: Long): FileChannel = {
    if (isWindows)
      FileApi.SetFilePointerEx(
        fd.handle,
        offset,
        null,
        FILE_BEGIN
      )
    else unistd.lseek(fd.fd, offset, stdio.SEEK_SET)
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
      unistd.lseek(fd.fd, 0, stdio.SEEK_CUR).toLong
    }

  override def read(
      buffers: Array[ByteBuffer],
      start: Int,
      number: Int
  ): Long = {
    ensureOpen()

    var bytesRead = 0L
    var i = 0

    while (i < number) {
      val startPos = buffers(i).position()
      val len = buffers(i).limit() - startPos
      val dst = new Array[Byte](len)
      val nb = read(dst, 0, dst.length)

      if (nb > 0) {
        buffers(i).put(dst)
        buffers(i).position(startPos + nb)
      }

      bytesRead += nb
      i += 1
    }

    bytesRead
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
        // end of file
        -1
      } else if (readCount < 0) {
        // negative value (typically -1) indicates that read failed
        throw UnixException(file.fold("")(_.toString), errno.errno)
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
    } else {
      val size = unistd.lseek(fd.fd, 0L, stdio.SEEK_END);
      unistd.lseek(fd.fd, 0L, stdio.SEEK_CUR)
      size
    }
  }

  override def transferFrom(
      src: ReadableByteChannel,
      position: Long,
      count: Long
  ): Long = {
    ensureOpen()
    val buf = ByteBuffer.allocate(count.toInt)
    src.read(buf)
    write(buf, position)
  }

  override def transferTo(
      pos: Long,
      count: Long,
      target: WritableByteChannel
  ): Long = {
    ensureOpen()
    position(pos)
    val buf = new Array[Byte](count.toInt)
    val nb = read(buf, 0, buf.length)
    target.write(ByteBuffer.wrap(buf, 0, nb))
    nb
  }

  override def truncate(size: Long): FileChannel =
    if (!openForWriting) {
      throw new IOException("Invalid argument")
    } else {
      ensureOpen()
      val currentPosition = position()
      val hasSucceded =
        if (isWindows) {
          FileApi.SetFilePointerEx(
            fd.handle,
            size,
            null,
            FILE_BEGIN
          ) &&
          FileApi.SetEndOfFile(fd.handle)
        } else {
          unistd.ftruncate(fd.fd, size) == 0
        }
      if (!hasSucceded) {
        throw new IOException("Failed to truncate file")
      }
      if (currentPosition > size) position(size)
      else position(currentPosition)
      this
    }

  override def write(
      buffers: Array[ByteBuffer],
      offset: Int,
      length: Int
  ): Long = {
    ensureOpen()
    var i = 0
    while (i < length) {
      write(buffers(offset + i))
      i += 1
    }
    i
  }

  override def write(buffer: ByteBuffer, pos: Long): Int = {
    ensureOpen()
    position(pos)
    val srcPos: Int = buffer.position()
    val srcLim: Int = buffer.limit()
    val lim = math.abs(srcLim - srcPos)
    val bytes = if (buffer.hasArray()) {
      buffer.array()
    } else {
      val bytes = new Array[Byte](lim)
      buffer.get(bytes)
      bytes
    }
    write(bytes, 0, lim)
    buffer.position(srcPos + lim)
    lim
  }

  override def write(src: ByteBuffer): Int =
    write(src, position())

  private def ensureOpen(): Unit =
    if (!isOpen()) throw new ClosedChannelException()

  private[java] def write(
      buffer: Array[Byte],
      offset: Int,
      count: Int
  ): Unit = {
    if (buffer == null) {
      throw new NullPointerException
    }
    if (offset < 0 || count < 0 || count > buffer.length - offset) {
      throw new IndexOutOfBoundsException
    }
    if (count == 0) {
      return
    }

    // we use the runtime knowledge of the array layout to avoid
    // intermediate buffer, and read straight from the array memory
    val buf = buffer.at(offset)
    if (isWindows) {
      val hasSucceded =
        WriteFile(fd.handle, buf, count.toUInt, null, null)
      if (!hasSucceded) {
        throw WindowsException.onPath(
          file.fold("<file descriptor>")(_.toString)
        )
      }
    } else {
      val writeCount = unistd.write(fd.fd, buf, count.toUInt)

      if (writeCount < 0) {
        // negative value (typically -1) indicates that write failed
        throw UnixException(file.fold("")(_.toString), errno.errno)
      }
    }
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

  def available(): Int = {
    ensureOpen()

    val currentPosition = position()
    val lastPosition = size()

    val nAvailable =
      if (currentPosition >= lastPosition) 0
      else lastPosition - currentPosition

    nAvailable.toInt
  }
}
