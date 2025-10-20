package java.io

import scala.scalanative.libc.stdio._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.unistd.lseek
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import java.nio.channels.{FileChannel, FileChannelImpl}

class FileInputStream(fd: FileDescriptor, file: Option[File])
    extends InputStream {

  def this(fd: FileDescriptor) = this(fd, None)
  def this(file: File) = this(FileDescriptor.openReadOnly(file), Some(file))
  def this(str: String) = this(new File(str))

  private val channel: FileChannelImpl =
    new FileChannelImpl(
      fd,
      file,
      deleteFileOnClose = false,
      openForReading = true,
      openForWriting = false
    )

  override def available(): Int = channel.available()

  override def close(): Unit = {
    channel.close()
  }

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

  override def read(buffer: Array[Byte], offset: Int, count: Int): Int =
    channel.read(buffer, offset, count)

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
        lseek(fd.fd, bytesToSkip.toSize, SEEK_CUR)
      bytesToSkip
    }

  def getChannel: FileChannel = channel
}
