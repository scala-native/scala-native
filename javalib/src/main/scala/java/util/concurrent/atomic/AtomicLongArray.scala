package java.util.concurrent.atomic

import scala.scalanative.runtime.CAtomicLong

class AtomicLongArray(sz: Int) extends Serializable {

  def this(array: Array[Long]) = {
    this(array.length)
    System.arraycopy(array, 0, inner, 0, sz)
  }

  private[this] val inner: Array[CAtomicLong] =
    new Array[Long](sz).map(e => CAtomicLong(e))

  final def length(): Int =
    inner.length

  final def get(i: Int): Long =
    inner(i).load()

  final def set(i: Int, newValue: Long): Unit =
    inner(i).store(newValue)

  final def lazySet(i: Int, newValue: Long): Unit =
    set(i, newValue)

  final def getAndSet(i: Int, newValue: Long): Long = {
    val ret = get(i)
    set(i, newValue)
    ret
  }

  final def compareAndSet(i: Int, expect: Long, update: Long): Boolean =
    inner(i).compareAndSwapStrong(expect, update)._1

  final def weakCompareAndSet(i: Int, expect: Long, update: Long): Boolean =
    inner(i).compareAndSwapWeak(expect, update)._1

  final def getAndIncrement(i: Int): Long =
    inner(i).fetchAdd(1)

  final def getAndDecrement(i: Int): Long =
    inner(i).fetchSub(1)

  final def getAndAdd(i: Int, delta: Long): Long =
    inner(i).fetchAdd(delta)

  final def incrementAndGet(i: Int): Long =
    inner(i).addFetch(1)

  final def decrementAndGet(i: Int): Long =
    inner(i).subFetch(1)

  final def addAndGet(i: Int, delta: Long): Long =
    inner(i).addFetch(delta)

  override def toString(): String =
    inner.mkString("[", ", ", "]")
}

object AtomicLongArray {

  private final val serialVersionUID: Long = -2308431214976778248L

}
