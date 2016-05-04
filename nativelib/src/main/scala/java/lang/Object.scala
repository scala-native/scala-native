package java.lang

import scala.scalanative.native._
import scala.scalanative.runtime.{Monitor, Type}

class _Object {
  def _equals(that: _Object): scala.Boolean =
    cast[Word](this) == cast[Word](that)

  def _hashCode(): scala.Int =
    cast[Word](this).hashCode

  def _toString(): String =
    getClass.getName + "@" + Integer.toHexString(hashCode)

  def _getClass(): _Class[_] =
    new _Class(Type.get(this))

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
