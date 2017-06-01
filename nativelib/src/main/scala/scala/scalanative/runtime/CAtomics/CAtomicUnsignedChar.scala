package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CUnsignedChar, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicUnsignedChar extends CAtomic {

  private[this] val atm = alloc(sizeof[CUnsignedChar]).cast[Ptr[CUnsignedChar]]
  init_uchar(atm, 'a'.asInstanceOf[CUnsignedChar])

  def this(initValue: CUnsignedChar) = {
    this()
    init_uchar(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(
      expected: CUnsignedChar,
      desired: CUnsignedChar): (Boolean, CUnsignedChar) = {
    val expectedPtr = stackalloc[CUnsignedChar]
    !expectedPtr = expected

    if (compare_and_swap_strong_uchar(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CUnsignedChar,
                         desired: CUnsignedChar): (Boolean, CUnsignedChar) = {
    val expectedPtr = stackalloc[CUnsignedChar]
    !expectedPtr = expected

    if (compare_and_swap_weak_uchar(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CUnsignedChar): CUnsignedChar =
    atomic_add_uchar(atm.cast[Ptr[CUnsignedChar]], value)

  def subFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CUnsignedChar): CUnsignedChar =
    atomic_sub_uchar(atm.cast[Ptr[CUnsignedChar]], value)

  def andFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CUnsignedChar): CUnsignedChar =
    atomic_and_uchar(atm.cast[Ptr[CUnsignedChar]], value)

  def orFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CUnsignedChar): CUnsignedChar =
    atomic_or_uchar(atm.cast[Ptr[CUnsignedChar]], value)

  def xorFetch(value: CUnsignedChar): CUnsignedChar = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CUnsignedChar): CUnsignedChar =
    atomic_xor_uchar(atm.cast[Ptr[CUnsignedChar]], value)

  def load(): CUnsignedChar = load_uchar(atm)

}

object CAtomicUnsignedChar extends CAtomic {

  def apply(initValue: CUnsignedChar) = new CAtomicUnsignedChar(initValue)

  def apply() = new CAtomicUnsignedChar()

}
