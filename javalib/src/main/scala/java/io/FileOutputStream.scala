package java.io

import scala.scalanative.libc._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.winnt.AccessRights._
import java.nio.channels.{FileChannelImpl, FileChannel}

class FileOutputStream(fd: FileDescriptor, file: Option[File])
    extends OutputStream {
  def this(fd: FileDescriptor) = this(fd, None)
  def this(file: File, append: Boolean) =
    this(FileOutputStream.fileDescriptor(file, append), Some(file))
  def this(file: File) = this(file, false)
  def this(name: String, append: Boolean) = this(new File(name), append)
  def this(name: String) = this(new File(name))

  private val channel: FileChannelImpl =
    new FileChannelImpl(
      fd,
      file,
      deleteFileOnClose = false,
      openForReading = false,
      openForWriting = true
    )

  override def close(): Unit =
    channel.close()

  override protected def finalize(): Unit = close()

  final def getFD(): FileDescriptor =
    fd

  override def write(buffer: Array[Byte]): Unit = {
    if (buffer == null) {
      throw new NullPointerException
    }
    write(buffer, 0, buffer.length)
  }

  override def write(buffer: Array[Byte], offset: Int, count: Int): Unit =
    channel.write(buffer, offset, count)

  override def write(b: Int): Unit =
    write(Array(b.toByte))

  def getChannel(): FileChannel = channel
}

object FileOutputStream {
  private def fileDescriptor(file: File, append: Boolean) =
    Zone.acquire { implicit z =>
      if (isWindows) {
        val handle = CreateFileW(
          toCWideStringUTF16LE(file.getPath()),
          desiredAccess = if (append) FILE_APPEND_DATA else FILE_GENERIC_WRITE,
          shareMode = FILE_SHARE_READ | FILE_SHARE_WRITE,
          securityAttributes = null,
          creationDisposition =
            if (append) OPEN_ALWAYS
            else CREATE_ALWAYS,
          flagsAndAttributes = 0.toUInt,
          templateFile = null
        )

        if (handle == INVALID_HANDLE_VALUE) {
          throw new FileNotFoundException(file.toString())
        }
        new FileDescriptor(FileDescriptor.FileHandle(handle), readOnly = false)
      } else {
        import scala.scalanative.posix.sys.stat._
        import scala.scalanative.posix.fcntl._
        val flags = O_CREAT | O_WRONLY | (if (append) O_APPEND else O_TRUNC)
        val mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH
        val fd = open(toCString(file.getPath()), flags, mode)
        if (fd == -1)
          throw new FileNotFoundException(
            s"$file (${fromCString(string.strerror(errno.errno))})"
          )
        else
          new FileDescriptor(fd, readOnly = false)
      }
    }
}
