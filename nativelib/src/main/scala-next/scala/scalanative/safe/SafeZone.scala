package scala.scalanative.safe

import language.experimental.captureChecking
import scalanative.unsigned._
import scala.annotation.implicitNotFound
import scala.scalanative.runtime.{RawPtr, CZone}

@implicitNotFound("Given method requires an implicit zone.")
trait SafeZone {

  /** Frees allocations. This zone allocator is not reusable once closed. */
  def close(): Unit

  /** Return this zone allocator is open or not. */
  def isOpen: Boolean

  /** Return this zone allocator is closed or not. */
  def isClosed: Boolean = !isOpen

  /** Return the handle of this zone allocator. */
  private[scalanative] def handle: RawPtr

  /** Require this zone allocator is open. */
  def checkOpen(): Unit = {
    if (!isOpen)
      throw new IllegalStateException(s"Zone ${this} is already closed.")
  }
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
    val sz: {*} SafeZone = open()
    try f(sz)
    finally sz.close()
  }

  final def open(): {*} SafeZone = new MemorySafeZone(CZone.open())
}
