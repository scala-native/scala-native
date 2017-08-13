package java.util.concurrent.atomic

import scala.scalanative.runtime.CAtomicLong

class AtomicReferenceArray[E <: AnyRef](length: Int) extends Serializable {

  def this(array: Array[E]) = {
    this(array.size)
    System.arraycopy(array, 0, inner, 0, length)
  }

  private[this] val inner: Array[CAtomicLong] =
    new Array[Long](length).map(e => CAtomicLong(e))

  final def length(): Int =
    inner.length

  final def get(i: Int): E =
    inner(i).load()

  final def set(i: Int, newValue: E): Unit =
    inner(i).store(newValue)

  final def lazySet(i: Int, newValue: E): Unit =
    set(i, newValue)

  final def getAndSet(i: Int, newValue: E): E = {
    val ret = get(i)
    set(i, newValue)
    ret
  }

  final def compareAndSet(i: Int, expect: E, update: E): Boolean = {
    inner(i).compareAndSwapStrong(expect, update)._1
  }

  final def weakCompareAndSet(i: Int, expect: E, update: E): Boolean =
    inner(i).compareAndSwapWeak(expect, update)._1

  override def toString(): String =
    inner.mkString("[", ", ", "]")

  private implicit def toLong(e: E): Long = e.asInstanceOf[Long]

  private implicit def toRef(l: Long): E = l.asInstanceOf[E]
}

object AtomicReferenceArray {

  private final val serialVersionUID: Long = -6209656149925076980L

}
