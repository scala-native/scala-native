package java.nio

import scala.scalanative.posix.sys.mman._
import scala.scalanative.posix.unistd.ftruncate
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.runtime.ByteArray
import scala.scalanative.runtime.IntArray
import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.libc.errno._
import scala.scalanative.libc.stdlib._

import java.io.IOException
import java.io.FileDescriptor
import java.nio.channels.FileChannel.MapMode
import java.nio.channels.FileChannel

// Unmap is not actually found in Java API. Unmapping should be done on
// garbage collection, however, Scala Native does not provide that
// functionality. Users have to rely on automatic unmapping at the end
// of a process. Essentially, a memory leak on lost reference.
class MappedByteBufferImpl private (
    mode: MapMode,
    size: Int,
    array: GenArray[Byte],
    fd: FileDescriptor,
    ptr: Ptr[Byte]
) extends MappedByteBuffer(mode, size, array, 0) {

  override def force(): MappedByteBuffer = {
    if (mode eq MapMode.READ_WRITE) {
      if (isWindows) {
        ???
      } else {
        if (msync(ptr, size.toUInt, MS_SYNC) == -1)
          throw new IOException
      }
    }
    this
  }

  // Not found in java API
  override def unmap(): Unit = {
    if (isWindows) {
      ???
    } else {
      if (munmap(ptr, size.toUInt) == -1)
        throw new IOException
    }
  }

  override def isLoaded(): Boolean = true

  override def load(): MappedByteBuffer = this
}

private[nio] object MappedByteBufferImpl {
  def map(
      mode: MapMode,
      position: Long,
      size: Int,
      fd: FileDescriptor,
      channel: FileChannel
  ): MappedByteBufferImpl = {

    // JVM resizes file to accomodate mapping
    if (mode ne MapMode.READ_ONLY) {
      if (position + size > channel.size())
        if (isWindows) ???
        else {
          if (ftruncate(fd.fd, position + size) == -1)
            throw new IOException
        }
    }

    val ptr: Ptr[Byte] =
      if (isWindows) {
        ???
      } else {
        val (prot: Int, isPrivate: Int) =
          if (mode eq MapMode.PRIVATE) (PROT_WRITE, MAP_PRIVATE)
          else if (mode eq MapMode.READ_ONLY) (PROT_READ, MAP_SHARED)
          else if (mode eq MapMode.READ_WRITE) (PROT_WRITE, MAP_SHARED)

        val ptr = mmap(
          null,
          size.toUInt,
          prot,
          isPrivate,
          fd.fd,
          position
        )
        if (ptr.toInt == -1) throw new IOException
        ptr
      }

    new MappedByteBufferImpl(mode, size, PtrArray(ptr, size), fd, ptr)
  }
}
