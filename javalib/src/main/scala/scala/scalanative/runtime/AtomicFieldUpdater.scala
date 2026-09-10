package scala.scalanative.runtime

import java.util.concurrent.atomic.{
  AtomicIntegerFieldUpdater, AtomicLongFieldUpdater, AtomicReferenceFieldUpdater
}

import scala.scalanative.unsafe.Ptr

private class AtomicFieldUpdater {}
object AtomicFieldUpdater {
  def createIntegerFieldUpdater[T <: AnyRef](
      binding: AnyRef => Ptr[Int]
  ): AtomicIntegerFieldUpdater[T] =
    new AtomicIntegerFieldUpdater.Impl[T](binding)

  def createLongFieldUpdater[T <: AnyRef](
      binding: AnyRef => Ptr[Long]
  ): AtomicLongFieldUpdater[T] = new AtomicLongFieldUpdater.Impl[T](binding)

  def createReferenceFieldUpdater[T <: AnyRef, V <: AnyRef](
      binding: AnyRef => Ptr[V]
  ): AtomicReferenceFieldUpdater[T, V] =
    new AtomicReferenceFieldUpdater.Impl[T, V](binding)
}
