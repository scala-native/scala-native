package scala.scalanative.safe

import language.experimental.captureChecking
import scalanative.unsigned._
import scala.annotation.implicitNotFound
import scala.scalanative.runtime.{RawPtr, CMemoryPool, CMemoryPoolZone}

@implicitNotFound("Given method requires an implicit zone.")
trait SafeZone {

  /** Frees allocations. This zone allocator is not reusable once closed. */
  def close(): Unit

  /** Return this zone allocator is open or not. */
  def isOpen: Boolean

  /** Return this zone allocator is closed or not. */
  def isClosed: Boolean = !isOpen

  /** Return the handle of this zone allocator. */
  def handle: RawPtr
}

final class MemorySafeZone (private[this] val zoneHandle: RawPtr) extends SafeZone {

  private[this] var flagIsOpen = true

  protected def checkOpen(): Unit = {
    if (!isOpen)
      throw new IllegalStateException(s"Zone ${this} is already closed.")
  }

  override def close(): Unit = {
    checkOpen()
    flagIsOpen = false
    CMemoryPoolZone.close(zoneHandle)
    CMemoryPoolZone.free(zoneHandle)
  }

  override def isOpen: Boolean = flagIsOpen

  override def handle: RawPtr = zoneHandle
}


object SafeZone {

  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: ({*} SafeZone) => T): T = {
    val sz: {*} SafeZone = open()
    try f(sz)
    finally sz.close()
  }

  final def open(): {*} SafeZone = new MemorySafeZone(
    CMemoryPoolZone.open(CMemoryPool.defaultMemoryPoolHandle)
  )
}
