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
    val mode: MapMode,
    mapAddress: Ptr[Byte],
    val length: Int,
    /** Offset from mapped address (page boundary) to start of requested data */
    val pagePosition: Int,
    val windowsMappingHandle: Option[Handle]
) {
  val data: Ptr[Byte] =
    if (mapAddress != null) mapAddress + pagePosition
    else null

  // Finalization. Unmapping is done on garbage collection, like on JVM.
//  private val selfWeakReference = new WeakReference(this)

  if (mapAddress != null) {
    // Finalization. Unmapping is done on garbage collection, like on JVM.
    val selfWeakReference = new WeakReference(this)

    new MappedByteBufferFinalizer(
      selfWeakReference,
      mapAddress,
      length,
      windowsMappingHandle
    )
  }

  def force(): Unit = {
    if (mode eq MapMode.READ_WRITE) {
      if (isWindows) {
        if (!FlushViewOfFile(mapAddress, 0.toUInt))
          throw new IOException("Could not flush view of file")
      } else {
        if (msync(mapAddress, length.toUInt, MS_SYNC) == -1)
          throw new IOException("Could not sync with file")
      }
    }
  }

  @inline def update(index: Int, value: Byte): Unit =
    data(index) = value

  @inline def apply(index: Int): Byte =
    data(index)
}

object MappedByteBufferData {

  /* Create an "empty" instance for the special case of size == 0.
   * This removes that complexity from the execution paths of the
   * more frequently used size > 0 case.
   *
   * Keep the nasty null confined to this file, so caller does not
   * need to know about it.
   *
   * Execution should never reach update() or apply() (bb.get()).
   * Code earlier in the execution chain should have detected and rejected
   * an attempt to access an empty MappedByteBufferData instance.
   * Have those two methods return "reasonable" values just in case.
   * Could have thrown an Exception.  Fielder's choice.
   *
   * Since it is never called, the return value for apply() is just to
   * keep the compiler happy; it can be any Byte, zero seemed to make the
   * most sense. Fielder's choice redux.
   */
  def empty = new MappedByteBufferData(MapMode.READ_ONLY, null, 0, 0, None) {
    override def force(): Unit = () // do nothing
    override def update(index: Int, value: Byte): Unit = () // do nothing
    override def apply(index: Int): Byte = 0 // Should never reach here
  }

}
