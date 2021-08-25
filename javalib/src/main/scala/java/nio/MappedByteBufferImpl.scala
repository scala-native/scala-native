package java.nio

import scala.scalanative.posix.sys.mman._
import scala.scalanative.posix.unistd.ftruncate
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.runtime.ByteArray
import scala.scalanative.runtime.IntArray
import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows.WinBaseApi.CreateFileMappingA
import scala.scalanative.windows.WinBaseApiExt._
import scala.scalanative.windows.MemoryApi._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows._

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
    ptr: Ptr[Byte],
    windowsMappingHandler: Option[Handle]
) extends MappedByteBuffer(mode, size, array, 0) {

  // Not found in java API
  override def unmap(): Unit = {
    if (isWindows) {
      if (!UnmapViewOfFile(ptr)) throw new IOException
      if (!CloseHandle(windowsMappingHandler.get)) throw new IOException
    } else {
      if (munmap(ptr, size.toUInt) == -1)
        throw new IOException
    }
  }

  override def force(): MappedByteBuffer = {
    if (mode eq MapMode.READ_WRITE) {
      if (isWindows) {
        if (!FlushViewOfFile(ptr, size.toUInt))
          throw new IOException
      } else {
        if (msync(ptr, size.toUInt, MS_SYNC) == -1)
          throw new IOException
      }
    }
    this
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
        if (isWindows) {
          // _chsize( _fileno(f), size);
          ???
        } else {
          if (ftruncate(fd.fd, position + size) == -1)
            throw new IOException
        }
    }

    val (ptr: Ptr[Byte], windowsMappingHandler: Option[Handle]) =
      if (isWindows) {
        val (flProtect: DWord, dwDesiredAccess: DWord) =
          if (mode eq MapMode.PRIVATE)
            (PAGE_WRITECOPY, FILE_MAP_WRITE | FILE_MAP_COPY)
          else if (mode eq MapMode.READ_ONLY) (PAGE_READONLY, FILE_MAP_READ)
          else if (mode eq MapMode.READ_WRITE) (PAGE_READWRITE, FILE_MAP_WRITE)

        val mappingHandle =
          CreateFileMappingA(
            fd.handle,
            null,
            flProtect,
            0.toUInt,
            0.toUInt,
            null
          )
        if (mappingHandle == null) throw new IOException

        val dwFileOffsetHigh = (position >> 32).toInt.toUInt
        val dwFileOffsetLow = position.toInt.toUInt

        val ptr = MapViewOfFile(
          mappingHandle,
          dwDesiredAccess,
          dwFileOffsetHigh,
          dwFileOffsetLow,
          size.toUInt
        )
        if (ptr.toInt == -1) throw new IOException
        (ptr, Some(mappingHandle))
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
        (ptr, None)
      }

    new MappedByteBufferImpl(
      mode,
      size,
      PtrArray(ptr, size),
      ptr,
      windowsMappingHandler
    )
  }
}
