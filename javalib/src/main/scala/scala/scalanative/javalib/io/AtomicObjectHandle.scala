package scala.scalanative.javalib.io

import java.util.concurrent.atomic.AtomicLong

import scala.scalanative.windows.HandleApi._

class AtomicObjectHandle private (val value: AtomicLong) extends AnyVal {
  def get(): ObjectHandle = new ObjectHandle(value.get())
  def valid(): Boolean = get().valid()
  def release(): ObjectHandle =
    new ObjectHandle(value.getAndSet(ObjectHandle.Invalid.value))
  def close(): Unit = release().close()
}

object AtomicObjectHandle {
  def apply(fh: ObjectHandle): AtomicObjectHandle =
    new AtomicObjectHandle(new AtomicLong(fh.value))
  def apply(id: Int): AtomicObjectHandle = apply(ObjectHandle(id))
  def apply(handle: Handle): AtomicObjectHandle = apply(ObjectHandle(handle))
}
