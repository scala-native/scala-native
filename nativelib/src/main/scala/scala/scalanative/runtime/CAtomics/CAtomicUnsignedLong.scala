package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CInt, CUnsignedLong, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicUnsignedLong extends CAtomic {

  private[this] val atm = alloc(sizeof[CUnsignedLong]).cast[Ptr[CUnsignedLong]]
  init_ulong(atm, 0.asInstanceOf[CUnsignedLong])

  def this(initValue: CUnsignedLong) = {
    this()
    init_ulong(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CUnsignedLong, desired: CUnsignedLong): (Boolean, CUnsignedLong) = {
    val expectedPtr = stackalloc[CUnsignedLong]
    !expectedPtr = expected

    if(compare_and_swap_strong_ulong(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CUnsignedLong, desired: CUnsignedLong): (Boolean, CUnsignedLong) = {
    val expectedPtr = stackalloc[CUnsignedLong]
    !expectedPtr = expected

    if(compare_and_swap_weak_ulong(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CUnsignedLong): CUnsignedLong = atomic_add_ulong(atm.cast[Ptr[CUnsignedLong]], value)

  def subFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CUnsignedLong): CUnsignedLong = atomic_sub_ulong(atm.cast[Ptr[CUnsignedLong]], value)

  def andFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CUnsignedLong): CUnsignedLong = atomic_and_ulong(atm.cast[Ptr[CUnsignedLong]], value)

  def orFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CUnsignedLong): CUnsignedLong = atomic_or_ulong(atm.cast[Ptr[CUnsignedLong]], value)

  def xorFetch(value: CUnsignedLong): CUnsignedLong = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CUnsignedLong): CUnsignedLong = atomic_xor_ulong(atm.cast[Ptr[CUnsignedLong]], value)

  def load(): CUnsignedLong = load_ulong(atm)

}

object CAtomicUnsignedLong extends CAtomic {

  def apply(initValue: CUnsignedLong) = new CAtomicUnsignedLong(initValue)

  def apply() = new CAtomicUnsignedLong()

}
