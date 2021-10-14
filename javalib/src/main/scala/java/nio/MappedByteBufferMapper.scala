package java.nio

import scala.scalanative.posix.sys.mman._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows.WinBaseApi.CreateFileMappingA
import scala.scalanative.windows.WinBaseApiExt._
import scala.scalanative.windows.MemoryApi._
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows._

import java.io.IOException
import java.io.FileDescriptor
import java.nio.channels.FileChannel.MapMode
import java.nio.channels.FileChannel
import scala.scalanative.annotation.alwaysinline
import java.lang.ref.{WeakReference, WeakReferenceRegistry}

// Finalization object used to unmap file after GC.
private class MappedByteBufferFinalizer(
    weakRef: WeakReference[_ >: Null <: AnyRef],
    ptr: Ptr[Byte],
    size: Int,
    windowsMappingHandle: Option[Handle]
) {

  WeakReferenceRegistry.addHandler(weakRef, apply)

  def apply(): Unit = {
    if (isWindows) {
      UnmapViewOfFile(ptr)
      CloseHandle(windowsMappingHandle.get)
    } else {
      munmap(ptr, size.toUInt)
    }
  }
}

// The part that results in finalization after dereferencing.
// We cannot include this directly in MappedByteBuffer implementations,
// as they can change classes via views (fe. MappedByteBuffer can become IntBuffer)
// on runtime.
private[nio] class MappedByteBufferData(
    private[nio] val mode: MapMode,
    private[nio] val ptr: Ptr[Byte],
    private[nio] val length: Int,
    private[nio] val windowsMappingHandle: Option[Handle]
) {

  // Finalization. Unmapping is done on garbage collection, like on JVM.
  private val selfWeakReference = new WeakReference(this)
  new MappedByteBufferFinalizer(
    selfWeakReference,
    ptr,
    length,
    windowsMappingHandle
  )

  def force(): Unit = {
    if (mode eq MapMode.READ_WRITE) {
      if (isWindows) {
        if (!FlushViewOfFile(ptr, 0.toUInt))
          throw new IOException("Could not flush view of file")
      } else {
        if (msync(ptr, length.toUInt, MS_SYNC) == -1)
          throw new IOException("Could not sync with file")
      }
    }
  }

  @inline def update(index: Int, value: Byte): Unit =
    ptr(index) = value

  @inline def apply(index: Int): Byte =
    ptr(index)
}

private[nio] object MappedByteBufferMapper {

  @alwaysinline private def failMapping(): Unit =
    throw new IOException("Could not map file to memory")

  def mapWindows(
      position: Long,
      size: Int,
      fd: FileDescriptor,
      mode: MapMode
  ): MappedByteBufferData = {
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
    if (mappingHandle == null) failMapping()

    val dwFileOffsetHigh = (position >>> 32).toUInt
    val dwFileOffsetLow = position.toUInt

    val ptr = MapViewOfFile(
      mappingHandle,
      dwDesiredAccess,
      dwFileOffsetHigh,
      dwFileOffsetLow,
      size.toUInt
    )
    if (ptr == null) failMapping()

    new MappedByteBufferData(mode, ptr, size, Some(mappingHandle))
  }

  def mapUnix(
      position: Long,
      size: Int,
      fd: FileDescriptor,
      mode: MapMode
  ): MappedByteBufferData = {
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
    if (ptr.toInt == -1) failMapping()

    new MappedByteBufferData(mode, ptr, size, None)
  }

  def map(
      mode: MapMode,
      position: Long,
      size: Int,
      fd: FileDescriptor,
      channel: FileChannel
  ): MappedByteBufferImpl = {

    // JVM resizes file to accomodate mapping
    if (mode ne MapMode.READ_ONLY) {
      val prevSize = channel.size()
      val minSize = position + size
      if (minSize > prevSize) {
        val prevPosition = channel.position()
        channel.truncate(minSize)
        if (isWindows) {
          channel.position(prevSize)
          for (i <- prevSize until minSize)
            channel.write(ByteBuffer.wrap(Array[Byte](0.toByte)))
          channel.position(prevPosition)
        }
      }
    }

    val mappedData =
      if (isWindows) mapWindows(position, size, fd, mode)
      else mapUnix(position, size, fd, mode)

    new MappedByteBufferImpl(
      mappedData.length,
      mappedData,
      0,
      0,
      size,
      mode == FileChannel.MapMode.READ_ONLY
    )
  }
}
