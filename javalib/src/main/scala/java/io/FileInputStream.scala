package java.io

import scala.scalanative.native._, scala.scalanative.native.stdlib._,
scala.scalanative.native.stdio._, scala.scalanative.native.string._
import scala.scalanative.posix.{fcntl, unistd}
import unistd._
import scala.scalanative.runtime.GC

class FileInputStream(fd: FileDescriptor) extends InputStream {

  def this(file: File) = this(FileInputStream.fileDescriptor(file))
  def this(str: String) = this(new File(str))

  override def available(): Int = {
    val currentPosition = lseek(fd.fd, 0, SEEK_CUR)
    val lastPosition    = lseek(fd.fd, 0, SEEK_END)
    lseek(fd.fd, currentPosition, SEEK_SET)
    (lastPosition - currentPosition).toInt
  }

  override def close(): Unit =
    fcntl.close(fd.fd)

  override protected def finalize(): Unit =
    close()

  // def getChannel: FileChannel

  final def getFD: FileDescriptor =
    fd

  override def read(): Int = {
    val buffer = new Array[Byte](1)
    if (read(buffer) <= 0) -1
    else buffer(0)
  }

  override def read(buffer: Array[Byte]): Int =
    read(buffer, 0, buffer.length)

  override def read(buffer: Array[Byte], offset: Int, count: Int): Int = {
    val buf       = GC.malloc(count)
    val readCount = unistd.read(fd.fd, buf, count)

    if (readCount <= 0) -1
    else {
      var i = 0
      while (i < readCount) {
        buffer(offset + i) = buf(i)
        i += 1
      }
      readCount
    }
  }

  override def skip(n: Long): Long =
    if (n < 0) throw new IOException()
    else {
      val bytesToSkip = Math.min(n, available())
      lseek(fd.fd, bytesToSkip, SEEK_CUR)
      bytesToSkip
    }
}

object FileInputStream {
  private def fileDescriptor(file: File): FileDescriptor = {
    val fd = fcntl.open(toCString(file.getPath), fcntl.O_RDONLY)
    new FileDescriptor(fd)
  }
}
