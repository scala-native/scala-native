package java.util.concurrent.atomic

import scala.scalanative.native
import scala.scalanative.native.Atomic

class AtomicLong(private[this] var value: Long)
    extends Number
    with Serializable {
  def this() = this(0L)

  private val lock: native.Ptr[native.CInt] = native.stackalloc[native.CInt]
  !lock = 0

  private def getLock: Boolean = {
    Atomic.compareAndSwapInt(lock, 0, 1)
  }

  private def releaseLock: Unit = {
    !lock = 0
  }

  final def get(): Long = value

  final def set(newValue: Long): Unit =
    value = newValue

  final def lazySet(newValue: Long): Unit =
    set(newValue)

  final def getAndSet(newValue: Long): Long = {
    var oldValue = get
    while (!compareAndSet(oldValue, newValue)) {
      oldValue = get
    }
    oldValue
  }

  final def compareAndSet(expect: Long, update: Long): Boolean = {
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

  final def weakCompareAndSet(expect: Long, update: Long): Boolean =
    compareAndSet(expect, update)

  final def getAndIncrement(): Long =
    getAndAdd(1L)

  final def getAndDecrement(): Long =
    getAndAdd(-1L)

  @inline final def getAndAdd(delta: Long): Long = {
    var oldValue = get
    while (!compareAndSet(oldValue, oldValue + delta)) {
      oldValue = get
    }
    oldValue
  }

  final def incrementAndGet(): Long =
    addAndGet(1L)

  final def decrementAndGet(): Long =
    addAndGet(-1L)

  @inline final def addAndGet(delta: Long): Long = {
    var oldValue = get
    while (!compareAndSet(oldValue, oldValue + delta)) {
      oldValue = get
    }
    oldValue + delta
  }

  override def toString(): String =
    value.toString()

  def intValue(): Int       = value.toInt
  def longValue(): Long     = value
  def floatValue(): Float   = value.toFloat
  def doubleValue(): Double = value.toDouble
}
