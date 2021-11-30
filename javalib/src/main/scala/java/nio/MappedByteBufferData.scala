package java.nio

import scala.scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.posix.sys.mman._

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import java.lang.ref.{WeakReference, WeakReferenceRegistry}

import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.MemoryApi._
import java.nio.channels.FileChannel.MapMode
import java.io.IOException

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
