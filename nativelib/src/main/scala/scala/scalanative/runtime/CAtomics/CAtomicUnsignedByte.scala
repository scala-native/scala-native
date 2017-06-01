package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CInt, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicUnsignedByte extends CAtomic {

  private[this] val atm = alloc(sizeof[Byte])
  init_ubyte(atm, 0.toByte)

  def this(initValue: Byte) = {
    this()
    init_ubyte(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm)

  def compareAndSwapStrong(expected: Byte, desired: Byte): (Boolean, Byte) = {
    val expectedPtr = stackalloc[Byte]
    !expectedPtr = expected

    if (compare_and_swap_strong_ubyte(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: Byte, desired: Byte): (Boolean, Byte) = {
    val expectedPtr = stackalloc[Byte]
    !expectedPtr = expected

    if (compare_and_swap_weak_ubyte(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: Byte): Byte = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: Byte): Byte = atomic_add_ubyte(atm, value)

  def subFetch(value: Byte): Byte = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: Byte): Byte = atomic_sub_ubyte(atm, value)

  def andFetch(value: Byte): Byte = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: Byte): Byte = atomic_and_ubyte(atm, value)

  def orFetch(value: Byte): Byte = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: Byte): Byte = atomic_or_ubyte(atm, value)

  def xorFetch(value: Byte): Byte = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: Byte): Byte = atomic_xor_ubyte(atm, value)

  def load(): Byte = load_ubyte(atm)

}

object CAtomicUnsignedByte extends CAtomic {

  def apply(initValue: Byte) = new CAtomicUnsignedByte(initValue)

  def apply() = new CAtomicUnsignedByte()

}
