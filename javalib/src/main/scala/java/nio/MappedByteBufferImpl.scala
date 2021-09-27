package java.nio

import scala.scalanative.posix.sys.mman._
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.runtime.ByteArray
import scala.scalanative.runtime.IntArray
import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows.WinBaseApi.CreateFileMappingA
import scala.scalanative.windows.WinBaseApiExt._
import scala.scalanative.windows.MemoryApi._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.FileApi
import scala.scalanative.windows.FileApiExt._
import scala.scalanative.windows._

import scala.scalanative.libc.errno._
import scala.scalanative.libc.stdlib._

import java.io.IOException
import java.io.FileDescriptor
import java.nio.channels.FileChannel.MapMode
import java.nio.channels.FileChannel
import scala.scalanative.annotation.alwaysinline
import java.lang.ref.{WeakReference, WeakReferenceRegistry}

import scala.scalanative.windows.ErrorHandlingApi._
import java.lang.ref.ReferenceQueue

// Finalization object used to unmap file after GC.
class MappedByteBufferFinalizer(
    weakRef: WeakReference[_ >: Null <: AnyRef],
    ptr: Ptr[Byte],
    size: Int,
    windowsMappingHandler: Option[Handle]
) {

  WeakReferenceRegistry.addHandler(weakRef, apply)

  def apply(): Unit = {
    if (isWindows) {
      UnmapViewOfFile(ptr)
      CloseHandle(windowsMappingHandler.get)
    } else {
      munmap(ptr, size.toUInt)
    }
  }
}

class MappedByteBufferImpl private (
    mode: MapMode,
    array: GenArray[Byte],
    ptr: Ptr[Byte],
    windowsMappingHandler: Option[Handle]
) extends MappedByteBuffer(mode, array.length, array, 0) {

  // Finalization. Unmapping is done on garbage collection, like on JVM.
  private val selfWeakReference = new WeakReference(this)
  new MappedByteBufferFinalizer(
    selfWeakReference,
    ptr,
    array.length,
    windowsMappingHandler
  )

  override def force(): MappedByteBuffer = {
    if (mode eq MapMode.READ_WRITE) {
      if (isWindows) {
        if (!FlushViewOfFile(ptr, 0.toUInt))
          throw new IOException("Could not flush view of file")
      } else {
        if (msync(ptr, array.length.toUInt, MS_SYNC) == -1)
          throw new IOException("Could not sync with file")
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

    val prevPosition = channel.position()

    def throwException(): Unit =
      throw new IOException("Could not map file to memory")

    // JVM resizes file to accomodate mapping
    if (mode ne MapMode.READ_ONLY) {
      val prevSize = channel.size()
      val minSize = position + size
      if (minSize > prevSize) {
        channel.truncate(minSize)
        if (isWindows) {
          channel.position(prevSize)
          for (i <- prevSize until minSize)
            channel.write(ByteBuffer.wrap(Array[Byte](0.toByte)))
          channel.position(prevPosition)
        }
      }
    }

    val (ptr: Ptr[Byte], windowsMappingHandler: Option[Handle]) =
      if (isWindows) {
        val (flProtect: DWord, dwDesiredAccess: DWord) =
          if (mode eq MapMode.PRIVATE) (PAGE_WRITECOPY, FILE_MAP_COPY)
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
        if (mappingHandle == null) throwException()

        val dwFileOffsetHigh = (position >>> 32).toInt.toUInt
        val dwFileOffsetLow = position.toInt.toUInt

        val ptr = MapViewOfFile(
          mappingHandle,
          dwDesiredAccess,
          dwFileOffsetHigh,
          dwFileOffsetLow,
          size.toUInt
        )

        if (ptr == null) throwException()
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
        if (ptr.toInt == -1) throwException()
        (ptr, None)
      }

    new MappedByteBufferImpl(
      mode,
      PtrArray(ptr, size),
      ptr,
      windowsMappingHandler
    )
  }
}
