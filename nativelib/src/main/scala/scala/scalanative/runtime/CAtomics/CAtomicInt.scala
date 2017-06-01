package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CInt, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicInt extends CAtomic {

  private[this] val atm = alloc(sizeof[CInt]).cast[Ptr[CInt]]
  init_int(atm, 0)

  def this(initValue: CInt) = {
    this()
    init_int(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CInt, desired: CInt): (Boolean, CInt) = {
    val expectedPtr = stackalloc[CInt]
    !expectedPtr = expected

    if(compare_and_swap_strong_int(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CInt, desired: CInt): (Boolean, CInt) = {
    val expectedPtr = stackalloc[CInt]
    !expectedPtr = expected

    if(compare_and_swap_weak_int(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CInt): CInt = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CInt): CInt = atomic_add_int(atm.cast[Ptr[CInt]], value)

  def subFetch(value: CInt): CInt = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CInt): CInt = atomic_sub_int(atm.cast[Ptr[CInt]], value)

  def andFetch(value: CInt): CInt = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CInt): CInt = atomic_and_int(atm.cast[Ptr[CInt]], value)

  def orFetch(value: CInt): CInt = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CInt): CInt = atomic_or_int(atm.cast[Ptr[CInt]], value)

  def xorFetch(value: CInt): CInt = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CInt): CInt = atomic_xor_int(atm.cast[Ptr[CInt]], value)

  def load(): CInt = load_int(atm)

}

object CAtomicInt extends CAtomic {

  def apply(initValue: CInt) = new CAtomicInt(initValue)

  def apply() = new CAtomicInt()

}
