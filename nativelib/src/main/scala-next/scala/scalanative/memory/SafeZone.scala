package scala.scalanative.memory

import language.experimental.captureChecking
import scalanative.unsigned._
import scala.annotation.implicitNotFound
import scala.scalanative.unsafe.CSize
import scala.scalanative.unsigned.USize
import scala.scalanative.runtime.{
  RawPtr,
  RawSize,
  SafeZoneAllocator,
  Intrinsics
}
import scala.scalanative.runtime.SafeZoneAllocator.allocate

@implicitNotFound("Given method requires an implicit zone.")
trait SafeZone {

  /** Return this zone is open or not. */
  def isOpen: Boolean

  /** Return this zone is closed or not. */
  def isClosed: Boolean = !isOpen

  /** Require this zone to be open. */
  def checkOpen(): Unit = {
    if (!isOpen)
      throw new IllegalStateException(s"Zone ${this} is already closed.")
  }

  /** Allocates an object in this zone. The expression of obj must be an
   *  instance creation expression.
   */
  infix inline def alloc[T <: AnyRef](inline obj: T): T^{this} =
    allocate(this, obj)

  /** Frees allocations. This zone is not reusable once closed. */
  private[scalanative] def close(): Unit

  /** Return the handle of this zone. */
  private[scalanative] def handle: RawPtr

  /** The low-level implementation of allocation. This function shouldn't be
   *  inlined because it's directly called in the lowering phase.
   */
  @noinline
  private[scalanative] def allocImpl(cls: RawPtr, size: RawSize): RawPtr = {
    checkOpen()
    SafeZoneAllocator.Impl.alloc(handle, cls, size)
  }
}

object SafeZone {

  /** Run given function with a fresh zone and destroy it afterwards. */
  final def apply[T](f: (SafeZone^) ?=> T): T = {
    val sz: SafeZone^ = new MemorySafeZone(SafeZoneAllocator.Impl.open())
    try f(using sz)
    finally sz.close()
  }

  /* Allocates an object in the implicit zone. The expression of obj must be an instance creation expression. */
  inline def alloc[T <: AnyRef](inline obj: T)(using sz: SafeZone^): T^{sz} =
    allocate(sz, obj)

  /** Summon the implicit zone. */
  transparent inline def zone(using sz: SafeZone^): SafeZone^{sz} = sz

  private class MemorySafeZone(private[scalanative] val handle: RawPtr)
      extends SafeZone {
    private var flagIsOpen = true
    override def isOpen: Boolean = flagIsOpen
    override def close(): Unit = {
      checkOpen()
      flagIsOpen = false
      SafeZoneAllocator.Impl.close(handle)
    }
  }

}
