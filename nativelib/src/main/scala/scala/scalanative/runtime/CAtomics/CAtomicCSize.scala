package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CSize, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicCSize extends CAtomic {

  private[this] val atm = alloc(sizeof[CSize]).cast[Ptr[CSize]]
  init_csize(atm, 0)

  def this(initValue: CSize) = {
    this()
    init_csize(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CSize, desired: CSize): (Boolean, CSize) = {
    val expectedPtr = stackalloc[CSize]
    !expectedPtr = expected

    if (compare_and_swap_strong_csize(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CSize, desired: CSize): (Boolean, CSize) = {
    val expectedPtr = stackalloc[CSize]
    !expectedPtr = expected

    if (compare_and_swap_weak_csize(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CSize): CSize = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CSize): CSize =
    atomic_add_csize(atm.cast[Ptr[CSize]], value)

  def subFetch(value: CSize): CSize = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CSize): CSize =
    atomic_sub_csize(atm.cast[Ptr[CSize]], value)

  def andFetch(value: CSize): CSize = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CSize): CSize =
    atomic_and_csize(atm.cast[Ptr[CSize]], value)

  def orFetch(value: CSize): CSize = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CSize): CSize =
    atomic_or_csize(atm.cast[Ptr[CSize]], value)

  def xorFetch(value: CSize): CSize = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CSize): CSize =
    atomic_xor_csize(atm.cast[Ptr[CSize]], value)

  def load(): CSize = load_csize(atm)

}

object CAtomicCSize extends CAtomic {

  def apply(initValue: CSize) = new CAtomicCSize(initValue)

  def apply() = new CAtomicCSize()

}
