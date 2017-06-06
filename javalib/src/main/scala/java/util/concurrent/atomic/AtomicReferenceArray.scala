package java.util.concurrent.atomic

import scala.scalanative.native
import scala.scalanative.native.Atomic

class AtomicReferenceArray[E <: AnyRef](length: Int) extends Serializable {

  def this(array: Array[E]) = {
    this(array.size)
    System.arraycopy(array, 0, inner, 0, length)
  }

  private val inner: Array[AnyRef] = new Array[AnyRef](length)

  private val lock: native.Ptr[native.CInt] = native.stackalloc[native.CInt]
  !lock = 0

  private def getLock: Boolean = {
    Atomic.compareAndSwapInt(lock, 0, 1)
  }

  private def releaseLock: Unit = {
    !lock = 0
  }

  final def length(): Int =
    inner.length

  final def get(i: Int): E =
    inner(i).asInstanceOf[E]

  final def set(i: Int, newValue: E): Unit =
    inner(i) = newValue

  final def lazySet(i: Int, newValue: E): Unit =
    set(i, newValue)

  final def getAndSet(i: Int, newValue: E): E = {
    var oldValue = get(i)
    while (!compareAndSet(i, oldValue, newValue)) {
      oldValue = get(i)
    }
    oldValue
  }

  final def compareAndSet(i: Int, expect: E, update: E): Boolean = {
    while (!getLock) {}
    val result =
      if (get(i) ne expect) false
      else {
        set(i, update)
        true
      }
    releaseLock
    result
  }

  final def weakCompareAndSet(i: Int, expect: E, update: E): Boolean =
    compareAndSet(i, expect, update)

  override def toString(): String =
    inner.mkString("[", ", ", "]")
}
