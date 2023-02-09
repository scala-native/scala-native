package scala.collection.concurrent

import java.util.concurrent.atomic.{
  AtomicIntegerFieldUpdater,
  AtomicReferenceFieldUpdater
}

import scala.scalanative.runtime.Intrinsics.classFieldRawPtr
import scala.scalanative.runtime.{RawPtr, fromRawPtr}
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.atomic.{CAtomicRef, CAtomicInt, memory_order}
import scala.scalanative.unsafe.Ptr

private[concurrent] class IntrinsicAtomicReferenceFieldUpdater[
    T <: AnyRef,
    V <: AnyRef
](@alwaysinline selector: T => Ptr[V])
    extends AtomicReferenceFieldUpdater[T, V]() {
  @alwaysinline private def atomicRef(insideObj: T) =
    new CAtomicRef[V](selector(insideObj))

  @alwaysinline def compareAndSet(obj: T, expect: V, update: V): Boolean =
    atomicRef(obj).compareExchangeStrong(expect, update)

  @alwaysinline def weakCompareAndSet(obj: T, expect: V, update: V): Boolean =
    atomicRef(obj).compareExchangeWeak(expect, update)

  @alwaysinline def set(obj: T, newIntalue: V): Unit =
    atomicRef(obj).store(newIntalue)

  @alwaysinline def lazySet(obj: T, newIntalue: V): Unit =
    atomicRef(obj).store(newIntalue, memory_order.memory_order_release)

  @alwaysinline def get(obj: T): V = atomicRef(obj).load()
}

class IntrinsicAtomicIntegerFieldUpdater[T <: AnyRef](
    @alwaysinline selector: T => Ptr[Int]
) extends AtomicIntegerFieldUpdater[T]() {
  @alwaysinline private def atomicRef(insideObj: T) = new CAtomicInt(
    selector(insideObj)
  )

  @alwaysinline def compareAndSet(obj: T, expect: Int, update: Int): Boolean =
    atomicRef(obj).compareExchangeStrong(expect, update)

  @alwaysinline def weakCompareAndSet(
      obj: T,
      expect: Int,
      update: Int
  ): Boolean =
    atomicRef(obj).compareExchangeWeak(expect, update)

  @alwaysinline def set(obj: T, newIntalue: Int): Unit =
    atomicRef(obj).store(newIntalue)

  @alwaysinline def lazySet(obj: T, newIntalue: Int): Unit =
    atomicRef(obj).store(newIntalue, memory_order.memory_order_release)

  @alwaysinline def get(obj: T): Int = atomicRef(obj).load()
}
