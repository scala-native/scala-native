package java.util.concurrent.atomic

import scala.scalanative.runtime.CAtomicLong

class AtomicReference[T <: AnyRef](private[this] var value: T)
    extends Serializable {

  def this() = this(0L.asInstanceOf[T])

  private[this] val inner = CAtomicLong(value.asInstanceOf[Long])

  final def get(): T = inner.load()

  final def set(newValue: T): Unit =
    inner.store(newValue.asInstanceOf[Long])

  final def lazySet(newValue: T): Unit =
    inner.store(newValue.asInstanceOf[Long])

  final def compareAndSet(expect: T, update: T): Boolean = {
    inner
      .compareAndSwapStrong(expect.asInstanceOf[Long],
                            update.asInstanceOf[Long])
      ._1
  }

  final def weakCompareAndSet(expect: T, update: T): Boolean =
    inner
      .compareAndSwapWeak(expect.asInstanceOf[Long], update.asInstanceOf[Long])
      ._1

  final def getAndSet(newValue: T): T = {
    val old = inner.load()
    inner.store(newValue.asInstanceOf[Long])
    old
  }

  override def toString(): String =
    String.valueOf(value)

  private implicit def toLong(e: T): Long = e.asInstanceOf[Long]

  private implicit def toRef(l: Long): T = l.asInstanceOf[T]
}

object AtomicReference {

  private final val serialVersionUID: Long = -1848883965231344442L

}
