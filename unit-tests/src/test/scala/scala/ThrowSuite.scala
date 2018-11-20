package scala.scalanative.native

object ThrowSuite extends tests.Suite {

  class E extends Exception

  test("throw exception") {
    assertThrows[E](throw new E)
  }

  test("throw null") {
    assertThrows[NullPointerException](throw null)
  }
}
