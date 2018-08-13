package tests

class A             extends Exception
class B             extends Exception
class C(val v: Int) extends Exception

object Foo {
  def bar(): Unit = throw new IllegalStateException()
}

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

  test("catch IllegalStateException") {
    assertThrows[IllegalStateException] { Foo.bar() }
  }

  test("catch RuntimeException") {
    assertThrows[RuntimeException] { Foo.bar() }
  }

  test("assert(false, msg) Exception should contain expected message") {
    val expected = "<Your message goes here.>"
    try {
      assert(false, expected)
    } catch {

      case AssertionFailed(mesg: String) =>
        if (mesg != expected) {
          throw AssertionFailed(s"unexpected message found: ${mesg}")
        }

      case exc: Throwable =>
        if (exc == AssertionFailed) // got case object, expected case class
          throw AssertionFailed("expected message yet none found")
        else
          throw exc
    }
  }

}
