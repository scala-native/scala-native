package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CInt, CUnsignedShort, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicUnsignedShort extends CAtomic {

  private[this] val atm = alloc(sizeof[CUnsignedShort]).cast[Ptr[CUnsignedShort]]
  init_ushort(atm, 0.asInstanceOf[CUnsignedShort])

  def this(initValue: CUnsignedShort) = {
    this()
    init_ushort(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CUnsignedShort, desired: CUnsignedShort): (Boolean, CUnsignedShort) = {
    val expectedPtr = stackalloc[CUnsignedShort]
    !expectedPtr = expected

    if(compare_and_swap_strong_ushort(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CUnsignedShort, desired: CUnsignedShort): (Boolean, CUnsignedShort) = {
    val expectedPtr = stackalloc[CUnsignedShort]
    !expectedPtr = expected

    if(compare_and_swap_weak_ushort(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CUnsignedShort): CUnsignedShort = atomic_add_ushort(atm.cast[Ptr[CUnsignedShort]], value)

  def subFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CUnsignedShort): CUnsignedShort = atomic_sub_ushort(atm.cast[Ptr[CUnsignedShort]], value)

  def andFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CUnsignedShort): CUnsignedShort = atomic_and_ushort(atm.cast[Ptr[CUnsignedShort]], value)

  def orFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CUnsignedShort): CUnsignedShort = atomic_or_ushort(atm.cast[Ptr[CUnsignedShort]], value)

  def xorFetch(value: CUnsignedShort): CUnsignedShort = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CUnsignedShort): CUnsignedShort = atomic_xor_ushort(atm.cast[Ptr[CUnsignedShort]], value)

  def load(): CUnsignedShort = load_ushort(atm)

}

object CAtomicUnsignedShort extends CAtomic {

  def apply(initValue: CUnsignedShort) = new CAtomicUnsignedShort(initValue)

  def apply() = new CAtomicUnsignedShort()

}
