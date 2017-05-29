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

    assertNot(compare_and_swap_strong_int(a, !b, 3))

    assert(compare_and_swap_strong_int(c, !a, 3))

    assert(!c == 3)
  }
/*
  test("add/sub and fetch long") {
    val a = stackalloc[CLong]
    val b = stackalloc[CLong]

    !a = 1
    !b = 2

    assert(add_and_fetch_long(a, 1) == 2)
    assert(sub_and_fetch_long(b, 1) == 1)
  }

  test("add/sub and fetch short") {
    val a = stackalloc[CShort]
    val b = stackalloc[CShort]

    !a = 1.toShort
    !b = 2.toShort

    assert(add_and_fetch_short(a, 1) == 2)
    assert(sub_and_fetch_short(b, 1) == 1)
  }

  test("add/sub and fetch csize") {
    val a = stackalloc[CSize]
    val b = stackalloc[CSize]

    !a = 1
    !b = 2

    assert(add_and_fetch_csize(a, 1) == 2.toShort)
    assert(sub_and_fetch_csize(b, 1) == 1.toShort)
  }

  test("add/sub and fetch char") {
    val a = stackalloc[CChar]
    val b = stackalloc[CChar]

    !a = 'a'
    !b = 'b'

    assert(add_and_fetch_char(a, 1) == 'b')
    assert(sub_and_fetch_char(b, 1) == 'a')
  }

  test("or/nand and fetch on booleans") {
    val b = stackalloc[CBool]

    !b = false

    assert(or_and_fetch_bool(b, true) == true)
    assert(nand_and_fetch_bool(b, true) == false)
  }*/

}
