package scala.scalanative
package native

import runtime.Atomic._

object AtomicSuite extends tests.Suite {

  test("compare and swap int") {
    val a = stackalloc[CInt]
    val b = stackalloc[CInt]
    val c = stackalloc[CInt]

    !a = 1
    !b = 2
    !c = 1

    assertNot(compare_and_swap_strong_int(a, b, 3))

    assert(compare_and_swap_strong_int(c, a, 3))

    assert(!c == 3)
  }

  test("atomic add/sub on long") {
    val a = stackalloc[CLong]
    val b = stackalloc[CLong]

    !a = 1
    !b = 2

    assert(atomic_add_long(a, 1) == 2)
    assert(atomic_sub_long(b, 1) == 1)
  }

  test("atomic add/sub on short") {
    val a = stackalloc[CShort]
    val b = stackalloc[CShort]

    !a = 1.toShort
    !b = 2.toShort

    assert(atomic_add_short(a, 1) == 2)
    assert(atomic_sub_short(b, 1) == 1)
  }

  test("atomic add/sub on csize") {
    val a = stackalloc[CSize]
    val b = stackalloc[CSize]

    !a = 1
    !b = 2

    assert(atomic_add_csize(a, 1) == 2.toShort)
    assert(atomic_sub_csize(b, 1) == 1.toShort)
  }

  test("atomic add/sub on char") {
    val a = stackalloc[CChar]
    val b = stackalloc[CChar]

    !a = 'a'
    !b = 'b'

    assert(atomic_add_char(a, 1) == 'b')
    assert(atomic_sub_char(b, 1) == 'a')
  }

  test("atomic or/and on boolean") {
    val a = stackalloc[CInt]
    val b = stackalloc[CInt]

    !a = 1
    !b = 0

    assert(atomic_and(a, 0) == 0)
    assert(atomic_or(b, 1) == 1)
  }

}
