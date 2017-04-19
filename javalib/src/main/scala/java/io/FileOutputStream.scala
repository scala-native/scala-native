package java.io

import scala.scalanative.posix.{fcntl, unistd}
import scala.scalanative.native._
import scala.scalanative.posix.sys.stat
import scala.scalanative.runtime.GC

class FileOutputStream(fd: FileDescriptor) extends OutputStream {
  def this(file: File, append: Boolean) =
    this(FileOutputStream.fileDescriptor(file, append))
  def this(file: File) = this(file, false)
  def this(name: String, append: Boolean) = this(new File(name), append)
  def this(name: String) = this(new File(name))

  override def close(): Unit =
    fcntl.close(fd.fd)

  override protected def finalize(): Unit =
    close()

  // def getChannel(): FileChannel

  final def getFD(): FileDescriptor =
    fd

  override def write(b: Array[Byte]): Unit =
    write(b, 0, b.length)

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    val buffer = GC.malloc(len)
    var i      = off
    while (i < len) {
      !(buffer + i) = b(off + i)
      i += 1
    }
    unistd.write(fd.fd, buffer, len)
  }

  override def write(b: Int): Unit =
    write(Array(b.toByte))
}

object FileOutputStream {
  private def fileDescriptor(file: File, append: Boolean) = {
    import fcntl._
    import stat._
    val flags = O_CREAT | O_WRONLY | (if (append) O_APPEND else 0)
    val mode  = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH
    val fd    = open(toCString(file.getPath), flags, mode)
    new FileDescriptor(fd)
  }
}
