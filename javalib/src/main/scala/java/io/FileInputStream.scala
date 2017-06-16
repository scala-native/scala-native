package java.io

import scalanative.native._, stdlib._, stdio._, string._
import scalanative.posix.{fcntl, unistd}, unistd._
import scalanative.runtime

class FileInputStream(fd: FileDescriptor) extends InputStream {

  def this(file: File) = this(FileDescriptor.openReadOnly(file))
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

  final def getFD: FileDescriptor =
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
    val buf       = buffer.asInstanceOf[runtime.ByteArray].at(offset)
    val readCount = unistd.read(fd.fd, buf, count)

    if (readCount == 0) {
      // end of file
      -1
    } else if (readCount < 0) {
      // negative value (typically -1) indicates that read failed
      throw new IOException("couldn't read from the file")
    } else {
      // successfully read readCount bytes
      readCount
    }
  }

  override def skip(n: Long): Long =
    if (n < 0) {
      throw new IOException()
    } else {
      val bytesToSkip = Math.min(n, available())
      lseek(fd.fd, bytesToSkip, SEEK_CUR)
      bytesToSkip
    }

  // TODO:
  // def getChannel: FileChannel
}
