package scala.scalanative.native

import Atomic._

object AtomicSuite extends tests.Suite{

  test("compare and swap int") {
    val a = stackalloc[CInt]
    val b = stackalloc[CInt]
    val c = stackalloc[CInt]

    !a = 1
    !b = 2
    !c = 1

    assertNot(compare_and_swap_int(a, !b, 3))

    assert(compare_and_swap_int(c, !a, 3))

    assert(!c == 3)
  }

  test("add/sub and fetch long") {
    val a = stackalloc[CLong]
    val b = stackalloc[CLong]

    !a = 1
    !b = 2

    assert(add_and_fetch_long(a, 1) == 2)
    assert(sub_and_fetch_long(b, 1) == 1)
  }

  test("or and fetch on booleans") {
    val b = stackalloc[CBool]

    !b = false

    assert(or_and_fetch_bool(b, true) == true)
  }

}
