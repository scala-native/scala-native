package java.nio.channels

import java.nio.file.Path
import java.nio.{ByteBuffer, MappedByteBuffer}

import java.io.RandomAccessFile

final class FileChannelImpl(path: Path) extends FileChannel {

  val raf = new RandomAccessFile(path.toFile, "rw")

  override protected def implCloseChannel(): Unit =
    raf.close()

  // TODO:
  // override def force(metadata: Boolean): Unit = ???
  // override def tryLock(position: Long, size: Long, shared: Boolean): FileLock = ???
  // override def lock(position: Long, size: Long, shared: Boolean): FileLock = ???
  // override def map(mode: FileChannel.MapMode, position: Long, size: Long): MappedByteBuffer = ???

  override def position(offset: Long): FileChannel = {
    raf.seek(offset)
    this
  }

  override def position(): Long = raf.getFilePointer()

  override def read(buffers: Array[ByteBuffer],
                    start: Int,
                    number: Int): Long = {
    ensureOpen()
    val dst = new Array[Byte](1)
    val nb  = raf.read(dst)
    var i   = 0
    while (i < number) {
      buffers(start + i).put(dst)
      i += 1
    }
    nb
  }

  override def read(buffer: ByteBuffer, pos: Long): Int = {
    ensureOpen()
    position(pos)
    val dst = new Array[Byte](1)
    val nb  = raf.read(dst)
    if (nb > 0) buffer.put(dst)
    nb
  }

  override def read(buffer: ByteBuffer): Int = {
    read(buffer, position())
  }

  override def size(): Long = raf.length()

  override def transferFrom(src: ReadableByteChannel,
                            position: Long,
                            count: Long): Long = {
    ensureOpen()
    val buf = ByteBuffer.allocate(count.toInt)
    src.read(buf)
    write(buf, position)
  }

  override def transferTo(pos: Long,
                          count: Long,
                          target: WritableByteChannel): Long = {
    ensureOpen()
    position(pos)
    val buf = new Array[Byte](count.toInt)
    val nb  = raf.read(buf)
    target.write(ByteBuffer.wrap(buf, 0, nb))
    nb
  }

  override def truncate(size: Long): FileChannel = {
    ensureOpen()
    raf.setLength(size)
    this
  }

  override def write(buffers: Array[ByteBuffer],
                     offset: Int,
                     length: Int): Long = {
    ensureOpen()
    var i = 0
    while (i < length) {
      raf.write(buffers(offset + i).get())
      i += 1
    }
    i
  }

  override def write(buffer: ByteBuffer, pos: Long): Int = {
    ensureOpen()
    position(pos)
    val toWrite = buffer.get()
    raf.write(toWrite)
    1
  }

  override def write(src: ByteBuffer): Int =
    write(src, position())

  private def ensureOpen(): Unit =
    if (!isOpen()) throw new ClosedChannelException()
}
