package java.nio.channels

import java.nio.{ByteBuffer, MappedByteBuffer}
import java.nio.file.{OpenOption, Path}
import java.nio.file.attribute.FileAttribute
import spi.AbstractInterruptibleChannel

import java.io.FileDescriptor

import java.util.{HashSet, Set}
import java.io.RandomAccessFile

import scala.scalanative.windows.ErrorHandlingApi._
import scala.scalanative.windows.FileApi._
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows.HandleApiExt._
import scala.scalanative.windows.winnt.AccessRights._
import java.nio.file._

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

  def open(
      path: Path,
      options: Set[_ <: OpenOption],
      attrs: Array[FileAttribute[_]]
  ): FileChannel = {
    import StandardOpenOption._

    if (options.contains(APPEND) && options.contains(TRUNCATE_EXISTING)) {
      throw new IllegalArgumentException(
        "APPEND + TRUNCATE_EXISTING not allowed"
      )
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

    val file = path.toFile()
    val raf = new RandomAccessFile(file, mode.toString)

    if (writing && options.contains(TRUNCATE_EXISTING)) {
      raf.setLength(0L)
    }

    if (writing && options.contains(APPEND)) {
      raf.seek(raf.length())
    }

    new FileChannelImpl(
      raf.getFD(),
      Some(file),
      deleteOnClose = options.contains(StandardOpenOption.DELETE_ON_CLOSE)
    )
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
