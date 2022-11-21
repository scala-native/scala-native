package scala.scalanative.safe

import scalanative.runtime.{CMemoryPool, CMemoryPoolZone, RawPtr}

object MemoryPool {
  lazy val defaultMemoryPoolHandle = CMemoryPool.open()

  // TODO: free the default memory pool.
  def freeDefaultMemoryPool(): Unit = {
    CMemoryPool.free(defaultMemoryPoolHandle)
  }
}

final class MemoryPoolSafeZone(private[this] val poolHandle: RawPtr)
    extends SafeZone {

  private[this] lazy val zoneHandle = CMemoryPoolZone.open(poolHandle)
  private[this] var flagIsOpen = true

  private def checkOpen(): Unit =
    if (!isOpen)
      throw new IllegalStateException("Zone {this} is already closed.")

  override def close(): Unit = {
    checkOpen()

    flagIsOpen = false
    CMemoryPoolZone.close(zoneHandle)
    CMemoryPoolZone.free(zoneHandle)
  }
  override def isOpen: Boolean = flagIsOpen
  override def isClosed: Boolean = !isOpen
}

object MemoryPoolSafeZone {
  def open(poolHandle: RawPtr): MemoryPoolSafeZone = {
    new MemoryPoolSafeZone(poolHandle)
  }
}
