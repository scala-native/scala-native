package java.nio.channels

import java.io.{RandomAccessFile, FileNotFoundException}

import java.nio.{ByteBuffer, MappedByteBuffer}
import java.nio.channels.spi.AbstractInterruptibleChannel
import java.nio.file._
import java.nio.file.attribute.FileAttribute

import java.util.{HashSet, Set}

abstract class FileChannel protected ()
    extends AbstractInterruptibleChannel
    with SeekableByteChannel
    with GatheringByteChannel
    with ScatteringByteChannel {

  def force(metadata: Boolean): Unit

  final def lock(): FileLock = lock(0L, Long.MaxValue, false)

  def lock(position: Long, size: Long, shared: Boolean): FileLock

  final def tryLock(): FileLock = tryLock(0L, Long.MaxValue, false)

  def tryLock(position: Long, size: Long, shared: Boolean): FileLock

  def map(
      mode: FileChannel.MapMode,
      position: Long,
      size: Long
  ): MappedByteBuffer

  def position(): Long

  def position(offset: Long): FileChannel

  def read(buffer: ByteBuffer): Int

  def read(buffer: ByteBuffer, position: Long): Int

  final def read(buffers: Array[ByteBuffer]) =
    read(buffers, 0, buffers.length)

  def read(buffers: Array[ByteBuffer], start: Int, number: Int): Long

  def size(): Long

  def transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long

  def transferTo(position: Long, count: Long, target: WritableByteChannel): Long

  def truncate(size: Long): FileChannel

  def write(src: ByteBuffer): Int

  def write(buffer: ByteBuffer, position: Long): Int

  final def write(buffers: Array[ByteBuffer]): Long =
    write(buffers, 0, buffers.length)

  def write(buffers: Array[ByteBuffer], offset: Int, length: Int): Long

}

object FileChannel {
  sealed abstract class MapMode
  object MapMode {
    final val PRIVATE = new MapMode {}
    final val READ_ONLY = new MapMode {}
    final val READ_WRITE = new MapMode {}
  }

  private def tryRandomAccessFile(
      fileName: String,
      mode: String
  ): RandomAccessFile = {
    try {
      new RandomAccessFile(fileName, mode)
    } catch {
      case fnf: FileNotFoundException =>
        throw new AccessDeniedException(fileName)
    }
  }

  def open(
      path: Path,
      options: Set[_ <: OpenOption],
      attrs: Array[FileAttribute[_]]
  ): FileChannel = {
    import StandardOpenOption._

    val appending = options.contains(APPEND)
    val writing = options.contains(WRITE) || appending

    if (appending) {
      if (options.contains(TRUNCATE_EXISTING)) {
        throw new IllegalArgumentException(
          "APPEND + TRUNCATE_EXISTING not allowed"
        )
      }

      if (options.contains(READ)) {
        throw new IllegalArgumentException("READ + APPEND not allowed")
      }
    }

    val mode = new java.lang.StringBuilder("r")
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

    val raf = tryRandomAccessFile(path.toString, mode.toString)

    try {
      if (writing && options.contains(TRUNCATE_EXISTING))
        raf.setLength(0L)

      new FileChannelImpl(
        raf.getFD(),
        Some(path.toFile()),
        deleteFileOnClose =
          options.contains(StandardOpenOption.DELETE_ON_CLOSE),
        openForReading = true,
        openForWriting = writing,
        openForAppending = appending
      )
    } catch {
      case e: Throwable =>
        try {
          raf.close()
        } catch {
          case _: Throwable => // caller interested in original e not this one.
        }
        throw e
    }
  }

  def open(path: Path, options: Array[OpenOption]): FileChannel = {
    var i = 0
    val set = new HashSet[OpenOption]()
    while (i < options.length) {
      set.add(options(i))
      i += 1
    }
    open(path, set, Array.empty)
  }

}
