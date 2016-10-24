package scala.scalanative.issues

object _350 extends tests.Suite {
  test("github.com/scala-native/scala-native/issues/350") {
    val div = java.lang.Long.divideUnsigned(42L, 2L)
    assert(div == 21L)
  }
}
