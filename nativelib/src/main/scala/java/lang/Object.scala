package java.lang

import scala.scalanative.native.{Ptr, cast}
import scala.scalanative.runtime.{Monitor, Type}

class _Object {
  def _equals(obj: _Object) = ???

  def _getClass(): Class[_] =
    new Class(getType)

  def _hashCode(): scala.Int = ???

  def _toString(): String = ???

  def _notify(): Unit =
    getMonitor._notify

  def _notifyAll(): Unit =
    getMonitor._notifyAll

  def _wait(): Unit =
    getMonitor._wait

  def _wait(timeout: scala.Long): Unit =
    getMonitor._wait(timeout)

  def _wait(timeout: scala.Long, nanos: Int): Unit =
    getMonitor._wait(timeout, nanos)

  protected def _clone(): _Object = ???

  protected def _finalize(): Unit = ()

  def getMonitor(): Monitor = Monitor.dummy

  def getType(): Ptr[Type] = ???
}
