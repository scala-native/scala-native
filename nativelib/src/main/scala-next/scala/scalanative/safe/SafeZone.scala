package scala.scalanative.safe

import language.experimental.captureChecking
import scalanative.unsigned._
import scala.annotation.implicitNotFound
import scala.scalanative.runtime.{RawPtr, CZone}

@implicitNotFound("Given method requires an implicit zone.")
trait SafeZone {

  /** Return this zone allocator is open or not. */
  def isOpen: Boolean

  /** Return this zone allocator is closed or not. */
  def isClosed: Boolean = !isOpen

  /** Require this zone allocator is open. */
  def checkOpen(): Unit = {
    if (!isOpen)
      throw new IllegalStateException(s"Zone ${this} is already closed.")
  }

  /** Frees allocations. This zone allocator is not reusable once closed. */
  private[scalanative] def close(): Unit

  /** Return the handle of this zone allocator. */
  private[scalanative] def handle: RawPtr
}

final class MemorySafeZone (private[scalanative] val handle: RawPtr) extends SafeZone {

  private[this] var flagIsOpen = true

  override def close(): Unit = {
    checkOpen()
    flagIsOpen = false
    CZone.close(handle)
  }

  override def isOpen: Boolean = flagIsOpen
}


object SafeZone {
  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: ({*} SafeZone) => T): T = {
    val sz: {*} SafeZone = new MemorySafeZone(CZone.open())
    try f(sz)
    finally sz.close()
  }
}
