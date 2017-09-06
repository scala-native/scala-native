package java.util.concurrent.atomic

import scala.scalanative.runtime.CAtomicInt

class AtomicInteger(private[this] var value: Int)
    extends Number
    with Serializable {

  def this() = this(0)

  private[this] val inner = CAtomicInt(value)

  final def get(): Int = inner.load()

  final def set(newValue: Int): Unit =
    inner.store(newValue)

  final def lazySet(newValue: Int): Unit =
    inner.store(newValue)

  final def getAndSet(newValue: Int): Int = {
    val old = inner.load()
    inner.store(newValue)
    old
  }

  final def compareAndSet(expect: Int, update: Int): Boolean =
    inner.compareAndSwapStrong(expect, update)._1

  final def weakCompareAndSet(expect: Int, update: Int): Boolean =
    inner.compareAndSwapWeak(expect, update)._1

  final def getAndIncrement(): Int =
    inner.fetchAdd(1)

  final def getAndDecrement(): Int =
    inner.fetchSub(1)

  @inline final def getAndAdd(delta: Int): Int =
    inner.fetchAdd(delta)

  final def incrementAndGet(): Int =
    inner.addFetch(1)

  final def decrementAndGet(): Int =
    inner.subFetch(1)

  @inline final def addAndGet(delta: Int): Int =
    inner.addFetch(delta)

  override def toString(): String =
    inner.toString()

  def intValue(): Int       = inner.load()
  def longValue(): Long     = inner.load().toLong
  def floatValue(): Float   = inner.load().toFloat
  def doubleValue(): Double = inner.load().toDouble
}

object AtomicInteger {

  private final val serialVersionUID: Long = 6214790243416807050L

}
