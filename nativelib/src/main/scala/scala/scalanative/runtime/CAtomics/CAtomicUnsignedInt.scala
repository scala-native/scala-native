package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CUnsignedInt, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicUnsignedInt extends CAtomic {

  private[this] val atm = alloc(sizeof[CUnsignedInt]).cast[Ptr[CUnsignedInt]]
  init_uint(atm, 0.asInstanceOf[CUnsignedInt])

  def this(initValue: CUnsignedInt) = {
    this()
    init_uint(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CUnsignedInt,
                           desired: CUnsignedInt): (Boolean, CUnsignedInt) = {
    val expectedPtr = stackalloc[CUnsignedInt]
    !expectedPtr = expected

    if (compare_and_swap_strong_uint(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CUnsignedInt,
                         desired: CUnsignedInt): (Boolean, CUnsignedInt) = {
    val expectedPtr = stackalloc[CUnsignedInt]
    !expectedPtr = expected

    if (compare_and_swap_weak_uint(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CUnsignedInt): CUnsignedInt =
    atomic_add_uint(atm.cast[Ptr[CUnsignedInt]], value)

  def subFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CUnsignedInt): CUnsignedInt =
    atomic_sub_uint(atm.cast[Ptr[CUnsignedInt]], value)

  def andFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CUnsignedInt): CUnsignedInt =
    atomic_and_uint(atm.cast[Ptr[CUnsignedInt]], value)

  def orFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CUnsignedInt): CUnsignedInt =
    atomic_or_uint(atm.cast[Ptr[CUnsignedInt]], value)

  def xorFetch(value: CUnsignedInt): CUnsignedInt = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CUnsignedInt): CUnsignedInt =
    atomic_xor_uint(atm.cast[Ptr[CUnsignedInt]], value)

  def load(): CUnsignedInt = load_uint(atm)

}

object CAtomicUnsignedInt extends CAtomic {

  def apply(initValue: CUnsignedInt) = new CAtomicUnsignedInt(initValue)

  def apply() = new CAtomicUnsignedInt()

}
