package scala.scalanative
package runtime.CAtomics

import scala.scalanative.native.{CInt, CLong, Ptr, sizeof, stackalloc}
import scala.scalanative.runtime.Atomic
import scala.scalanative.runtime.Atomic._

class CAtomicLong extends CAtomic {

  private[this] val atm = alloc(sizeof[CLong]).cast[Ptr[CLong]]
  init_long(atm, 0.toLong)

  def this(initValue: CLong) = {
    this()
    init_long(atm, initValue)
  }

  def free(): Unit = Atomic.free(atm.cast[Ptr[Byte]])

  def compareAndSwapStrong(expected: CLong, desired: CLong): (Boolean, CLong) = {
    val expectedPtr = stackalloc[CLong]
    !expectedPtr = expected

    if(compare_and_swap_strong_long(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def compareAndSwapWeak(expected: CLong, desired: CLong): (Boolean, CLong) = {
    val expectedPtr = stackalloc[CLong]
    !expectedPtr = expected

    if(compare_and_swap_weak_long(atm, expectedPtr, expected)) {
      (true, desired)
    } else {
      (false, !expectedPtr)
    }
  }

  def addFetch(value: CLong): CLong = {
    fetchAdd(value)
    load()
  }

  def fetchAdd(value: CLong): CLong = atomic_add_long(atm.cast[Ptr[CLong]], value)

  def subFetch(value: CLong): CLong = {
    fetchSub(value)
    load()
  }

  def fetchSub(value: CLong): CLong = atomic_sub_long(atm.cast[Ptr[CLong]], value)

  def andFetch(value: CLong): CLong = {
    fetchAnd(value)
    load()
  }

  def fetchAnd(value: CLong): CLong = atomic_and_long(atm.cast[Ptr[CLong]], value)

  def orFetch(value: CLong): CLong = {
    fetchOr(value)
    load()
  }

  def fetchOr(value: CLong): CLong = atomic_or_long(atm.cast[Ptr[CLong]], value)

  def xorFetch(value: CLong): CLong = {
    fetchXor(value)
    load()
  }

  def fetchXor(value: CLong): CLong = atomic_xor_long(atm.cast[Ptr[CLong]], value)

  def load(): CLong = load_long(atm)

}

object CAtomicLong extends CAtomic {

  def apply(initValue: CLong) = new CAtomicLong(initValue)

  def apply() = new CAtomicLong()

}
