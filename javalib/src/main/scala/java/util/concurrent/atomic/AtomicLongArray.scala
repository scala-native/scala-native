package java.util.concurrent.atomic

import scala.scalanative.native
import scala.scalanative.native.Atomic

class AtomicLongArray(length: Int) extends Serializable {
  def this(array: Array[Long]) = {
    this(array.size)
    System.arraycopy(array, 0, inner, 0, length)
  }

  private val lock: native.Ptr[native.CInt] = native.stackalloc[native.CInt]
  !lock = 0

  private def getLock: Boolean = {
    Atomic.compareAndSwapInt(lock, 0, 1)
  }

  private def releaseLock: Unit = {
    !lock = 0
  }

  private val inner: Array[Long] = new Array[Long](length)

  final def length(): Int =
    inner.length

  final def get(i: Int): Long =
    inner(i)

  final def set(i: Int, newValue: Long): Unit =
    inner(i) = newValue

  final def lazySet(i: Int, newValue: Long): Unit =
    set(i, newValue)

  final def getAndSet(i: Int, newValue: Long): Long = {
    var oldValue = get(i)
    while (!compareAndSet(i, oldValue, newValue)) {
      oldValue = get(i)
    }
    oldValue
  }

  final def compareAndSet(i: Int, expect: Long, update: Long): Boolean = {
    while (!getLock) {}
    val result = if (get(i) != expect) {
      false
    } else {
      set(i, update)
      true
    }
    releaseLock
    result
  }

  final def weakCompareAndSet(i: Int, expect: Long, update: Long): Boolean =
    compareAndSet(i, expect, update)

  final def getAndIncrement(i: Int): Long =
    getAndAdd(i, 1)

  final def getAndDecrement(i: Int): Long =
    getAndAdd(i, -1)

  final def getAndAdd(i: Int, delta: Long): Long = {
    var oldValue = get(i)
    while (!compareAndSet(i, oldValue, oldValue + delta)) {
      oldValue = get(i)
    }
    oldValue
  }

  final def incrementAndGet(i: Int): Long =
    addAndGet(i, 1)

  final def decrementAndGet(i: Int): Long =
    addAndGet(i, -1)

  final def addAndGet(i: Int, delta: Long): Long = {
    var oldValue = get(i)
    while (!compareAndSet(i, oldValue, oldValue + delta)) {
      oldValue = get(i)
    }
    oldValue + delta
  }

  override def toString(): String =
    inner.mkString("[", ", ", "]")
}
