package java.util.concurrent.atomic

import scala.scalanative.runtime.CAtomicInt

class AtomicIntegerArray(sz: Int) extends Serializable {

  def this(array: Array[Int]) = {
    this(array.length)
    System.arraycopy(array, 0, inner, 0, sz)
  }

  private[this] val inner: Array[CAtomicInt] =
    new Array[Int](sz).map(e => CAtomicInt(e))

  final def length(): Int =
    inner.length

  final def get(i: Int): Int =
    inner(i).load()

  final def set(i: Int, newValue: Int): Unit =
    inner(i).store(newValue)

  final def lazySet(i: Int, newValue: Int): Unit =
    set(i, newValue)

  final def getAndSet(i: Int, newValue: Int): Int = {
    val ret = get(i)
    set(i, newValue)
    ret
  }

  final def compareAndSet(i: Int, expect: Int, update: Int): Boolean =
    inner(i).compareAndSwapStrong(expect, update)._1

  final def weakCompareAndSet(i: Int, expect: Int, update: Int): Boolean =
    inner(i).compareAndSwapWeak(expect, update)._1

  final def getAndIncrement(i: Int): Int =
    inner(i).fetchAdd(1)

  final def getAndDecrement(i: Int): Int =
    inner(i).fetchSub(1)

  final def getAndAdd(i: Int, delta: Int): Int =
    inner(i).fetchAdd(delta)

  final def incrementAndGet(i: Int): Int =
    inner(i).addFetch(1)

  final def decrementAndGet(i: Int): Int =
    inner(i).subFetch(1)

  final def addAndGet(i: Int, delta: Int): Int =
    inner(i).addFetch(delta)

  override def toString(): String =
    inner.mkString("[", ", ", "]")
}

object AtomicIntegerArray {

  private final val serialVersionUID: Long = 2862133569453604235L

}
