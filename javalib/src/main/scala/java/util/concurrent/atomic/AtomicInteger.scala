package java.util.concurrent.atomic

import scala.scalanative.native
import scala.scalanative.native.Atomic

class AtomicInteger(private[this] var value: Int)
    extends Number
    with Serializable {

  def this() = this(0)

  private val lock: native.Ptr[native.CInt] = native.stackalloc[native.CInt]
  !lock = 0

  private def getLock: Boolean = {
    Atomic.compareAndSwapInt(lock, 0, 1)
  }

  private def releaseLock: Unit = {
    !lock = 0
  }

  final def get(): Int = value

  final def set(newValue: Int): Unit =
    value = newValue

  final def lazySet(newValue: Int): Unit =
    set(newValue)

  final def getAndSet(newValue: Int): Int = {
    var oldValue = get
    while (!compareAndSet(oldValue, newValue)) {
      oldValue = get
    }
    oldValue
  }

  final def compareAndSet(expect: Int, update: Int): Boolean = {
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

  final def weakCompareAndSet(expect: Int, update: Int): Boolean =
    compareAndSet(expect, update)

  final def getAndIncrement(): Int =
    getAndAdd(1)

  final def getAndDecrement(): Int =
    getAndAdd(-1)

  @inline final def getAndAdd(delta: Int): Int = {
    var oldValue = get
    while (!compareAndSet(oldValue, oldValue + delta)) {
      oldValue = get
    }
    oldValue
  }

  final def incrementAndGet(): Int =
    addAndGet(1)

  final def decrementAndGet(): Int =
    addAndGet(-1)

  @inline final def addAndGet(delta: Int): Int = {
    var oldValue = get
    while (!compareAndSet(oldValue, oldValue + delta)) {
      oldValue = get
    }
    oldValue + delta
  }

  override def toString(): String =
    value.toString()

  def intValue(): Int       = value
  def longValue(): Long     = value.toLong
  def floatValue(): Float   = value.toFloat
  def doubleValue(): Double = value.toDouble
}
