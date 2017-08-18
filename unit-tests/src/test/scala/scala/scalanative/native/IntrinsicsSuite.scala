package scala.scalanative.native

import scalanative.runtime.select

object IntrinsicsSuite extends tests.Suite {
  def condTrue: Boolean  = true
  def condFalse: Boolean = false

  test("select") {
    val one = select(condTrue, 1, 2)
    val two = select(condFalse, 1, 2)

    assert(one == 1)
    assert(two == 2)
  }
}
