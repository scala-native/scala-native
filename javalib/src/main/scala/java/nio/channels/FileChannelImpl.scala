package java.nio.channels

import java.nio.file.{
  FileAlreadyExistsException,
  Files,
  NoSuchFileException,
  OpenOption,
  Path,
  StandardOpenOption
}
import java.nio.file.attribute.FileAttribute
import java.nio.{ByteBuffer, MappedByteBuffer}

import java.io.RandomAccessFile

import java.util.Set

final class FileChannelImpl(path: Path,
                            options: Set[_ <: OpenOption],
                            attrs: Array[FileAttribute[_]])
    extends FileChannel {

  private val deleteOnClose =
    options.contains(StandardOpenOption.DELETE_ON_CLOSE)
  private val raf = FileChannelImpl.getRAF(path, options, attrs)

  // override def force(metadata: Boolean): Unit
  // override def tryLock(position: Long, size: Long, shared: Boolean): FileLock
  // override def lock(position: Long, size: Long, shared: Boolean): FileLock

  override protected def implCloseChannel(): Unit = {
    raf.close()
    if (deleteOnClose) Files.delete(path)
  }

  override def map(mode: FileChannel.MapMode,
                   position: Long,
                   size: Long): MappedByteBuffer = {
    var total  = 0
    var copied = 0
    val buffer =
      new MappedByteBuffer(mode, size.toInt, new Array(size.toInt), 0) {}
    while (copied < size && { copied = read(buffer); copied > 0 }) {
      total += copied
    }
    buffer
  }

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

private object FileChannelImpl {
  def getRAF(path: Path,
             options: Set[_ <: OpenOption],
             attrs: Array[FileAttribute[_]]): RandomAccessFile = {
    import StandardOpenOption._

    if (options.contains(APPEND) && options.contains(TRUNCATE_EXISTING)) {
      throw new IllegalArgumentException(
        "APPEND + TRUNCATE_EXISTING not allowed")
    }

    if (options.contains(APPEND) && options.contains(READ)) {
      throw new IllegalArgumentException("APPEND + READ not allowed")
    }

    val writing = options.contains(WRITE) || options.contains(APPEND)

    val mode = new StringBuilder("r")
    if (writing) mode.append("w")

    if (!Files.exists(path, Array.empty)) {
      if (!options.contains(CREATE) && !options.contains(CREATE_NEW)) {
        throw new NoSuchFileException(path.toString)
      } else if (writing) {
        Files.createFile(path, attrs)
      }
    } else if (options.contains(CREATE_NEW)) {
      throw new FileAlreadyExistsException(path.toString)
    }

    if (writing && options.contains(DSYNC) && !options
          .contains(SYNC)) {
      mode.append("d")
    }

    if (writing && options.contains(SYNC)) {
      mode.append("s")
    }

    val raf = new RandomAccessFile(path.toFile, mode.toString)

    if (writing && options.contains(TRUNCATE_EXISTING)) {
      raf.setLength(0L)
    }

    if (writing && options.contains(APPEND)) {
      raf.seek(raf.length())
    }

    raf

  }
}
