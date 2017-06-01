package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CChar, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicChar extends CAtomic {

  private[this] val atm = alloc(sizeof[CChar]).cast[Ptr[CChar]]
  init_char(atm, 'a'.asInstanceOf[CChar])

  def this(initValue: CChar) = {
    this()
    init_char(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm)

  def compareAndSwapStrong(expected: CChar, desired: CChar): (Boolean, CChar) = {
    val expectedPtr = stackalloc[CChar]
    !expectedPtr = expected

    if(compare_and_swap_strong_char(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CChar, desired: CChar): (Boolean, CChar) = {
    val expectedPtr = stackalloc[CChar]
    !expectedPtr = expected

    if(compare_and_swap_weak_char(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CChar): CChar = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CChar): CChar = atomic_add_char(atm.cast[Ptr[CChar]], value)

  def subFetch(value: CChar): CChar = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CChar): CChar = atomic_sub_char(atm.cast[Ptr[CChar]], value)

  def andFetch(value: CChar): CChar = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CChar): CChar = atomic_and_char(atm.cast[Ptr[CChar]], value)

  def orFetch(value: CChar): CChar = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CChar): CChar = atomic_or_char(atm.cast[Ptr[CChar]], value)

  def xorFetch(value: CChar): CChar = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CChar): CChar = atomic_xor_char(atm.cast[Ptr[CChar]], value)

  def load(): CChar = load_char(atm)

}

object CAtomicChar extends CAtomic {

  def apply(initValue: CChar) = new CAtomicChar(initValue)

  def apply() = new CAtomicChar()

}
