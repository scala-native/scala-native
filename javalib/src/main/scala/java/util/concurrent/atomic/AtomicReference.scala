package java.util.concurrent.atomic

// 2019-04-25 Note Well!
//    Almost all of the methods in this and other Atomic*.scala files
//    are manifestly not atomic.  The two methods added to day
//    and the prior art all rely upon the fact that Scala Native is
//    currently single threaded.  They will break, bring great gnashing
//    of teeth & horrid pain if/when SN becomes multi-threaded.

import java.util.function.UnaryOperator

class AtomicReference[T <: AnyRef](private[this] var value: T)
    extends Serializable {

  def this() = this(null.asInstanceOf[T])

  final def get(): T = value

  final def set(newValue: T): Unit =
    value = newValue

  final def lazySet(newValue: T): Unit =
    set(newValue)

  final def compareAndSet(expect: T, update: T): Boolean = {
    if (expect ne value) false
    else {
      value = update
      true
    }
  }

  final def weakCompareAndSet(expect: T, update: T): Boolean =
    compareAndSet(expect, update)

  final def getAndSet(newValue: T): T = {
    val old = value
    value = newValue
    old
  }

  final def getAndUpdate(updateFunction: UnaryOperator[T]): T = {
    val old = value
    value = updateFunction(old)
    old
  }

  final def updateAndGet(updateFunction: UnaryOperator[T]): T = {
    value = updateFunction(value)
    value
  }

  override def toString(): String =
    String.valueOf(value)
}
