package java.util.concurrent.atomic

import scala.scalanative.runtime.CAtomicLong

class AtomicLong(private[this] var value: Long)
    extends Number
    with Serializable {
  def this() = this(0L)

  private[this] val inner = CAtomicLong(value)

  final def get(): Long = inner.load()

  final def set(newValue: Long): Unit =
    inner.store(newValue)

  final def lazySet(newValue: Long): Unit =
    inner.store(newValue)

  final def getAndSet(newValue: Long): Long = {
    val old = inner.load()
    inner.store(newValue)
    old
  }

  final def compareAndSet(expect: Long, update: Long): Boolean =
    inner.compareAndSwapStrong(expect, update)._1

  final def weakCompareAndSet(expect: Long, update: Long): Boolean =
    inner.compareAndSwapWeak(expect, update)._1

  final def getAndIncrement(): Long =
    inner.fetchAdd(1)

  final def getAndDecrement(): Long =
    inner.fetchSub(1)

  @inline final def getAndAdd(delta: Long): Long =
    inner.fetchAdd(delta)

  final def incrementAndGet(): Long =
    inner.addFetch(1)

  final def decrementAndGet(): Long =
    inner.subFetch(1)

  @inline final def addAndGet(delta: Long): Long =
    inner.addFetch(delta)

  override def toString(): String =
    value.toString()

  def intValue(): Int       = inner.load().toInt
  def longValue(): Long     = inner.load()
  def floatValue(): Float   = inner.load().toFloat
  def doubleValue(): Double = inner.load().toDouble
}
