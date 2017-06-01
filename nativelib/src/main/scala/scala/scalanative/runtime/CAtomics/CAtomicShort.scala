package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CInt, CShort, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicShort extends CAtomic {

  private[this] val atm = alloc(sizeof[CShort]).cast[Ptr[CShort]]
  init_short(atm, 0.toShort)

  def this(initValue: CShort) = {
    this()
    init_short(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CShort,
                           desired: CShort): (Boolean, CShort) = {
    val expectedPtr = stackalloc[CShort]
    !expectedPtr = expected

    if (compare_and_swap_strong_short(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CShort,
                         desired: CShort): (Boolean, CShort) = {
    val expectedPtr = stackalloc[CShort]
    !expectedPtr = expected

    if (compare_and_swap_weak_short(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CShort): CShort = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CShort): CShort =
    atomic_add_short(atm.cast[Ptr[CShort]], value)

  def subFetch(value: CShort): CShort = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CShort): CShort =
    atomic_sub_short(atm.cast[Ptr[CShort]], value)

  def andFetch(value: CShort): CShort = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CShort): CShort =
    atomic_and_short(atm.cast[Ptr[CShort]], value)

  def orFetch(value: CShort): CShort = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CShort): CShort =
    atomic_or_short(atm.cast[Ptr[CShort]], value)

  def xorFetch(value: CShort): CShort = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CShort): CShort =
    atomic_xor_short(atm.cast[Ptr[CShort]], value)

  def load(): CShort = load_short(atm)

}

object CAtomicShort extends CAtomic {

  def apply(initValue: CShort) = new CAtomicShort(initValue)

  def apply() = new CAtomicShort()

}
