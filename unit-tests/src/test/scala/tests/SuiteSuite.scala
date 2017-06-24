package tests

class A             extends Exception
class B             extends Exception
class C(val v: Int) extends Exception

object SuiteSuite extends Suite {
  test("expects true") {
    assert(true)
  }

  testFails("does not expect false", issue = -1) {
    assert(false)
  }

  test("expects not false") {
    assertNot(false)
  }

  testFails("does not expect true", issue = -1) {
    assertNot(true)
  }

  test("expects A and throws A") {
    assertThrows[A] {
      throw new A
    }
  }

  test("expects C and throws C with function") {
    assertThrowsAnd[C](throw new C(42))(_.v == 42)
  }

  testFails("expects A and throws B", issue = -1) {
    assertThrows[A] {
      throw new B
    }
  }

  testFails("expects A and doesn't throw", issue = -1) {
    assertThrows[A] {}
  }
}
