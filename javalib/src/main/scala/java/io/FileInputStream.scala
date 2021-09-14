package java.io

import java.nio.file.WindowsException
import scala.scalanative.annotation.stub
import scala.scalanative.libc._
import scala.scalanative.libc.stdio._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.unistd
import scala.scalanative.posix.unistd.lseek
import scala.scalanative.nio.fs.unix.UnixException
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.{runtime, windows}
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.{ErrorCodes, ErrorHandlingApi}

class FileInputStream(fd: FileDescriptor, file: Option[File])
    extends InputStream {

  def this(fd: FileDescriptor) = this(fd, None)
  def this(file: File) = this(FileDescriptor.openReadOnly(file), Some(file))
  def this(str: String) = this(new File(str))

  override def available(): Int = {
    if (isWindows) {
      val currentPosition, lastPosition = stackalloc[windows.LargeInteger]
      SetFilePointerEx(
        fd.handle,
        distanceToMove = 0,
        newFilePointer = currentPosition,
        moveMethod = FILE_CURRENT
      )
      SetFilePointerEx(
        fd.handle,
        distanceToMove = 0,
        newFilePointer = lastPosition,
        moveMethod = FILE_END
      )
      SetFilePointerEx(
        fd.handle,
        distanceToMove = !currentPosition,
        newFilePointer = null,
        moveMethod = FILE_BEGIN
      )

      (!lastPosition - !currentPosition).toInt
    } else {
      val currentPosition = lseek(fd.fd, 0, SEEK_CUR)
      val lastPosition = lseek(fd.fd, 0, SEEK_END)
      lseek(fd.fd, currentPosition, SEEK_SET)
      (lastPosition - currentPosition).toInt
    }
  }

  override def close(): Unit = fd.close()

  override protected def finalize(): Unit =
    close()

  final def getFD(): FileDescriptor =
    fd

  override def read(): Int = {
    val buffer = new Array[Byte](1)
    if (read(buffer) <= 0) -1
    else buffer(0).toUInt.toInt
  }

  override def read(buffer: Array[Byte]): Int = {
    if (buffer == null) {
      throw new NullPointerException
    }
    read(buffer, 0, buffer.length)
  }

  override def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
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
    val buf = buffer.asInstanceOf[runtime.ByteArray].at(offset)
    if (isWindows) {
      def fail() = throw WindowsException.onPath(file.fold("")(_.toString))

      def tryRead(count: Int)(fallback: => Int) = {
        val readBytes = stackalloc[windows.DWord]
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
              case 0     => -1 //EOF
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

  override def skip(n: Long): Long =
    if (n < 0) {
      throw new IOException()
    } else {
      val bytesToSkip = Math.min(n, available())
      if (isWindows) {
        SetFilePointerEx(
          fd.handle,
          distanceToMove = bytesToSkip,
          newFilePointer = null,
          moveMethod = FILE_CURRENT
        )
      } else
        lseek(fd.fd, bytesToSkip, SEEK_CUR)
      bytesToSkip
    }

  @stub
  def getChannel: java.nio.channels.FileChannel = ???
}
