package java.nio.channels

import java.nio.{ByteBuffer, MappedByteBuffer}
import java.nio.file.{OpenOption, Path}
import java.nio.file.attribute.FileAttribute
import spi.AbstractInterruptibleChannel

import java.util.{HashSet, Set}

abstract class FileChannel protected ()
    extends AbstractInterruptibleChannel
    with SeekableByteChannel
    with GatheringByteChannel
    with ScatteringByteChannel {

  // def force(metadata: Boolean): Unit
  // final def lock(): FileLock =
  //   lock(0L, Long.MaxValue, false)
  // def lock(position: Long, size: Long, shared: Boolean): FileLock
  // final def tryLock(): FileLock =
  //   tryLock(0L, Long.MaxValue, false)
  // def tryLock(position: Long, size: Long, shared: Boolean): FileLock

  def map(mode: FileChannel.MapMode,
          position: Long,
          size: Long): MappedByteBuffer

  def position(): Long

  def position(offset: Long): FileChannel

  def read(buffer: ByteBuffer): Int

  def read(buffer: ByteBuffer, position: Long): Int

  final def read(buffers: Array[ByteBuffer]) =
    read(buffers, 0, buffers.length)

  def read(buffers: Array[ByteBuffer], start: Int, number: Int): Long

  def size(): Long

  def transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long

  def transferTo(position: Long,
                 count: Long,
                 target: WritableByteChannel): Long

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
    final val PRIVATE    = new MapMode {}
    final val READ_ONLY  = new MapMode {}
    final val READ_WRITE = new MapMode {}
  }

  def open(path: Path,
           options: Set[_ <: OpenOption],
           attrs: Array[FileAttribute[_]]): FileChannel =
    new FileChannelImpl(path, options, attrs)

  def open(path: Path, options: Array[OpenOption]): FileChannel = {
    var i   = 0
    val set = new HashSet[OpenOption]()
    while (i < options.length) {
      set.add(options(i))
      i += 1
    }
    open(path, set, Array.empty)
  }

}
