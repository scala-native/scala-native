package scala.scalanative.safe

import scala.scalanative.unsafe._

final class MockSafeZone(zone: Zone) extends SafeZone {

  private var flagIsOpen: Boolean = true

  override def alloc[T]()(using tag: Tag[T]): T = {
    val ptr: Ptr[T] = scala.scalanative.unsafe.alloc[T](1)(using tag, zone)
    !ptr
  }

  override def close(): Unit = flagIsOpen = false

  override def isClosed: Boolean = !flagIsOpen
}

object MockSafeZone {
  def open(): MockSafeZone = new MockSafeZone(Zone.open())
}
