package java.io

import scalanative.annotation.stub
import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc._, stdlib._, stdio._, string._
import scalanative.runtime

class FileInputStream(fd: FileDescriptor, file: Option[File])
    extends InputStream {

  def this(fd: FileDescriptor) = this(fd, None)
  def this(file: File) = this(FileDescriptor.openReadOnly(file), Some(file))
  def this(str: String) = this(new File(str))

  override def available(): Int = {
    fd.available()
  }

  override def close(): Unit =
    fd.close()

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
    val readCount = fd.read(buffer, offset, count)

    if (readCount == 0) {
      // end of file
      -1
    } else if (readCount < 0) {
      // negative value (typically -1) indicates that read failed
      throw new IOException(file.fold("")(_.toString))//, errno.errno)
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
      fd.ignore(bytesToSkip)
      bytesToSkip
    }

  @stub
  def getChannel: java.nio.channels.FileChannel = ???
}
