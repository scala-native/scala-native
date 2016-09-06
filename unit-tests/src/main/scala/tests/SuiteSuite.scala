package tests

class A extends Exception
class B extends Exception

object SuiteSuite extends Suite {
  test("expects true") {
    assert(true)
  }

  testNot("does not expect false") {
    assert(false)
  }

  test("expects not false") {
    assertNot(false)
  }

  testNot("does not expect true") {
    assertNot(true)
  }

  test("expects A and throws A") {
    assertThrows[A] {
      throw new A
    }
  }

  testNot("expects A and throws B") {
    assertThrows[A] {
      throw new B
    }
  }

  testNot("expects A and doesn't throw") {
    assertThrows[A] {}
  }
}
