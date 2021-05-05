package java.io

import java.nio.file.WindowsException
import scala.scalanative.nio.fs.UnixException
import scala.scalanative.libc._
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.unistd
import scala.scalanative.runtime
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.ErrorHandlingApi._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.winnt.AccessRights

class FileOutputStream(fd: FileDescriptor, file: Option[File] = None)
    extends OutputStream {
  def this(file: File, append: Boolean) =
    this(FileOutputStream.fileDescriptor(file, append), Some(file))
  def this(file: File) = this(file, false)
  def this(name: String, append: Boolean) = this(new File(name), append)
  def this(name: String) = this(new File(name))

  override def close(): Unit = fd.close()

  override protected def finalize(): Unit = close()

  final def getFD(): FileDescriptor =
    fd

  override def write(buffer: Array[Byte]): Unit = {
    if (buffer == null) {
      throw new NullPointerException
    }
    write(buffer, 0, buffer.length)
  }

  override def write(buffer: Array[Byte], offset: Int, count: Int): Unit = {
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
    val buf = buffer.asInstanceOf[runtime.ByteArray].at(offset)
    if (isWindows) {
      val hasSucceded =
        WriteFile(fd.handle, buf, count.toUInt, null, null)
      if (!hasSucceded) {
        throw WindowsException.onPath(
          file.fold("<file descriptor>")(_.toString))
      }
    } else {
      val writeCount = unistd.write(fd.fd, buf, count.toUInt)

      if (writeCount < 0) {
        // negative value (typically -1) indicates that write failed
        throw UnixException(file.fold("")(_.toString), errno.errno)
      }
    }
  }

  override def write(b: Int): Unit =
    write(Array(b.toByte))

  // TODO:
  // def getChannel(): FileChannel
}

object FileOutputStream {
  private def fileDescriptor(file: File, append: Boolean) =
    Zone { implicit z =>
      val fileName = toCString(file.getPath())
      if (isWindows) {
        val handle = CreateFileA(
          fileName,
          desiredAccess = AccessRights.FILE_GENERIC_WRITE,
          shareMode = FILE_SHARE_READ | FILE_SHARE_WRITE,
          securityAttributes = null,
          creationDisposition =
            if (append) OPEN_ALWAYS
            else CREATE_ALWAYS,
          flagsAndAttributes = 0.toUInt,
          templateFile = null
        )
        if (handle == InvalidHandleValue) {
          throw new FileNotFoundException(s"$file (${GetLastError()})")
        }
        new FileDescriptor(FileDescriptor.FileHandle(handle))
      } else {
        import scala.scalanative.posix.sys.stat._
        import scala.scalanative.posix.fcntl._
        val flags = O_CREAT | O_WRONLY | (if (append) O_APPEND else O_TRUNC)
        val mode  = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH
        val fd    = open(toCString(file.getPath()), flags, mode)
        if (fd == -1)
          throw new FileNotFoundException(
            s"$file (${fromCString(string.strerror(errno.errno))})")
        else
          new FileDescriptor(fd)
      }
    }
}
