package java.util.concurrent.atomic

import scala.scalanative.native
import scala.scalanative.native.Atomic

class AtomicBoolean(private[this] var value: Boolean) extends Serializable {
  def this() = this(false)

  private val lock: native.Ptr[native.CInt] = native.stackalloc[native.CInt]
  !lock = 0

  private def getLock: Boolean = {
    Atomic.compareAndSwapInt(lock, 0, 1)
  }

  private def releaseLock: Unit = {
    !lock = 0
  }

  final def get(): Boolean = value

  final def compareAndSet(expect: Boolean, update: Boolean): Boolean = {
    while (!getLock) {}
    val result =
      if (expect != value) false
      else {
        value = update
        true
      }
    releaseLock
    result
  }

  // For some reason, this method is not final
  def weakCompareAndSet(expect: Boolean, update: Boolean): Boolean =
    compareAndSet(expect, update)

  final def set(newValue: Boolean): Unit =
    value = newValue

  final def lazySet(newValue: Boolean): Unit =
    set(newValue)

  final def getAndSet(newValue: Boolean): Boolean = {
    var oldValue = get
    while (!compareAndSet(oldValue, newValue)) {
      oldValue = get
    }
    oldValue
  }

  override def toString(): String =
    value.toString()
}
