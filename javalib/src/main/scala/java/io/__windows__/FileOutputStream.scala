package java.io

import scalanative.unsafe._
import scalanative.libc._
import scalanative.runtime

class FileOutputStream(fd: FileDescriptor, file: Option[File] = None)
    extends OutputStream {
  def this(file: File, append: Boolean) =
    this(FileOutputStream.fileDescriptor(file, append), Some(file))
  def this(file: File) = this(file, false)
  def this(name: String, append: Boolean) = this(new File(name), append)
  def this(name: String) = this(new File(name))

  override def close(): Unit =
    fd.close()

  override protected def finalize(): Unit =
    close()

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
    val writeCount = fd.write(buffer, offset, count)

    if (writeCount < 0) {
      // negative value (typically -1) indicates that write failed
      throw new IOException(file.fold("")(_.toString))//, errno.errno)
    }
  }

  override def write(b: Int): Unit =
    write(Array(b.toByte))

  // TODO:
  // def getChannel(): FileChannel
}

object FileOutputStream {
  private def fileDescriptor(file: File, append: Boolean) = FileDescriptor.openWriteOnly(file, append)    
}
