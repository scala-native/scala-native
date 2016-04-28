package java.lang

import scala.scalanative.native.{Ptr, cast}
import scala.scalanative.runtime.{Monitor, Type}

class _Object {
  def _equals(obj: _Object) = ???

  def _getClass(): Class[_] =
    new Class(Type.get(this))

  def _hashCode(): scala.Int = ???

  def _toString(): String = ???

  def _notify(): Unit =
    Monitor.get(this)._notify

  def _notifyAll(): Unit =
    Monitor.get(this)._notifyAll

  def _wait(): Unit =
    Monitor.get(this)._wait

  def _wait(timeout: scala.Long): Unit =
    Monitor.get(this)._wait(timeout)

  def _wait(timeout: scala.Long, nanos: Int): Unit =
    Monitor.get(this)._wait(timeout, nanos)

  protected def _clone(): _Object = ???

  protected def _finalize(): Unit = ()
}
