package scala.scalanative
package native

import runtime.Atomic._
import runtime.CAtomics._
import stdlib._

object AtomicSuite extends tests.Suite {

  test("compare and swap int") {
    val a = CAtomicInt(1)

    assertNot(a.compareAndSwapStrong(2, 3)._1)

    assert(a.compareAndSwapStrong(1, 3)._2 == 3)

    a.free()
  }

  test("atomic char") {
    val a = CAtomicChar()

    assert(a.load() == 'a')
    assert(a.addFetch(1) == 'b')

    a.free()
  }

  test("compare and swap unsigned int uninitialized") {
    val a = CAtomicUnsignedInt()

    assertNot(
      a.compareAndSwapStrong(2.asInstanceOf[CUnsignedInt],
                              3.asInstanceOf[CUnsignedInt])
        ._1)

    assert(
      a.compareAndSwapStrong(0.asInstanceOf[CUnsignedInt],
                              3.asInstanceOf[CUnsignedInt])
        ._2 == 3.asInstanceOf[CUnsignedInt])

    a.free()
  }

  test("atomic {add, sub}_fetch on long") {
    val a = CAtomicLong(1.toLong)

    assert(a.addFetch(1) == 2.toLong)
    assert(a.subFetch(1) == 1.toLong)

    a.free()
  }

  test("atomic fetch_{add, sub} on short + load") {
    val a = CAtomicShort(1.toShort)

    assert(a.fetchAdd(1) == 1.toShort)
    assert(a.load() == 2.toShort)

    a.free()
  }

  test("atomic {or/and}_fetch on boolean") {
    val a = CAtomicInt(1)

    assert(a.andFetch(0) == 0)
    assert(a.orFetch(1) == 1)

    a.free()
  }

}
