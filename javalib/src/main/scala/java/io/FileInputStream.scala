package java.io

import java.nio.channels.{FileChannel, FileChannelImpl}

import scala.scalanative.unsigned._

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

  // Like JDK 25: seeks regular files, possibly past the end or backward;
  // reads and discards anything else.
  override def skip(n: Long): Long =
    if (channel.isRegularFile) channel.skip(n)
    else super.skip(n)

  def getChannel: FileChannel = channel
}
