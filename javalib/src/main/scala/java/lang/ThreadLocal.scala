package java.lang

import java.util.function.Supplier

class ThreadLocal[T] {
  private var hasValue: Boolean = false
  private var v: T = _

  protected def initialValue(): T = null.asInstanceOf[T]

  def get(): T = {
    if (!hasValue)
      set(initialValue())
    v
  }

  def set(o: T): Unit = {
    v = o
    hasValue = true
  }

  def remove(): Unit = {
    hasValue = false
    v = null.asInstanceOf[T] // for gc
  }
}

object ThreadLocal {

  def withInitial[S](supplier: Supplier[S]): ThreadLocal[S] =
    new ThreadLocal[S] {
      override protected def initialValue(): S = supplier.get()
    }

}
