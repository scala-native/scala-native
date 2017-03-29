package java.util.concurrent.atomic

import scala.scalanative.native
import scala.scalanative.native.Atomic

class AtomicReference[T <: AnyRef](private[this] var value: T)
    extends Serializable {

  def this() = this(null.asInstanceOf[T])

  private val lock: native.Ptr[native.CInt] = native.stackalloc[native.CInt]
  !lock = 0

  private def getLock: Boolean = {
    Atomic.compareAndSwapInt(lock, 0, 1)
  }

  private def releaseLock: Unit = {
    !lock = 0
  }

  final def get(): T = value

  final def set(newValue: T): Unit =
    value = newValue

  final def lazySet(newValue: T): Unit =
    set(newValue)

  final def compareAndSet(expect: T, update: T): Boolean = {
    while (!getLock) {}
    val result =
      if (expect ne value) false
      else {
        value = update
        true
      }
    releaseLock
    result
  }

  final def weakCompareAndSet(expect: T, update: T): Boolean =
    compareAndSet(expect, update)

  final def getAndSet(newValue: T): T = {
    var oldValue = get
    while (!compareAndSet(oldValue, newValue)) {
      oldValue = get
    }
    oldValue
  }

  override def toString(): String =
    String.valueOf(value)
}
