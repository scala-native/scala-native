package java.util.concurrent.atomic

import scala.scalanative.native.CInt
import scala.scalanative.runtime.CAtomicInt

class AtomicBoolean(private[this] var value: Boolean) extends Serializable {
  def this() = this(false)

  private[this] val inner = CAtomicInt(value)

  final def get(): Boolean = inner.load()

  final def compareAndSet(expect: Boolean, update: Boolean): Boolean = {
    inner.compareAndSwapStrong(expect, update)._1
  }

  // For some reason, this method is not final
  def weakCompareAndSet(expect: Boolean, update: Boolean): Boolean =
    inner.compareAndSwapWeak(expect, update)._1

  final def set(newValue: Boolean): Unit =
    inner.store(newValue)

  final def lazySet(newValue: Boolean): Unit =
    inner.store(newValue)

  final def getAndSet(newValue: Boolean): Boolean = {
    val old = inner.load()
    inner.store(newValue)
    old
  }

  override def toString(): String =
    inner.toString()

  private implicit def toInt(b: Boolean): Int = if (b) 1 else 0

  private implicit def toBool(i: Int): Boolean = i != 0
}

object AtomicBoolean {

  private final val serialVersionUID: Long = 4654671469794556979L

}
