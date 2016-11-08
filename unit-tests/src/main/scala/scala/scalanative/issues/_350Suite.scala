package scala.scalanative.issues

object _350Suite extends tests.Suite {
  test("#350") {
    val div = java.lang.Long.divideUnsigned(42L, 2L)
    assert(div == 21L)
  }
}
