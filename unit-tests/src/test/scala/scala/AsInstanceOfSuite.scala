package scala.scalanative.unsafe

object AsInstanceOfSuite extends tests.Suite {
  class C
  val c                      = new C
  @noinline def anyNull: Any = null
  @noinline def any42: Any   = 42
  @noinline def anyC: Any    = c

  test("null.asInstanceOf[Object]") {
    assert(anyNull.asInstanceOf[Object] == null)
  }

  test("null.asInstanceOf[Int]") {
    assert(anyNull.asInstanceOf[Int] == 0)
  }

  test("null.asInstanceOf[C]") {
    assert(anyNull.asInstanceOf[C] == null)
  }

  test("null.asInstanceOf[Null]") {
//    (anyNull.asInstanceOf[Null] == null) and
//    (anyNull.asInstanceOf[Null] eq null) are _compile_ time tests and
//    will always be true. How do I know? Scalac tells me so...
//    Wrapping in an Option gives a somewhat convoluted runtime test.
//    If something is massively wrong with anyNull.asInstanceOf[Null]
//    the Option will hold a Some(), of some kind, rather than the expected
//    None. I (LeeTibbert) am not sure what value this test adds beyond
//    symmetry & completeness.
    assert(Option(anyNull.asInstanceOf[Null]).isEmpty)
  }

  test("null.asInstanceOf[Nothing]") {
    assertThrows[NullPointerException](anyNull.asInstanceOf[Nothing])
  }

  test("null.asInstanceOf[Unit]") {
    assert(anyNull.asInstanceOf[Unit] == anyNull)
  }

  test("42.asInstanceOf[Object]") {
    assert(any42.asInstanceOf[Object] == 42)
  }

  test("42.asInstanceOf[Int]") {
    assert(any42.asInstanceOf[Int] == 42)
  }

  test("42.asInstanceOf[C]") {
    assertThrows[ClassCastException](any42.asInstanceOf[C])
  }

  test("42.asInstanceOf[Null]") {
    assertThrows[ClassCastException](any42.asInstanceOf[Null])
  }

  test("42.asInstanceOf[Nothing]") {
    assertThrows[ClassCastException](any42.asInstanceOf[Nothing])
  }

  test("42.asInstanceOf[Unit]") {
    assertThrows[ClassCastException](any42.asInstanceOf[Unit])
  }

  test("c.asInstanceOf[Object]") {
    assert(anyC.asInstanceOf[Object] == anyC)
  }

  test("c.asInstanceOf[Int]") {
    assertThrows[ClassCastException](anyC.asInstanceOf[Int])
  }

  test("c.asInstanceOf[C]") {
    assert(anyC.asInstanceOf[C] == anyC)
  }

  test("c.asInstanceOf[Null]") {
    assertThrows[ClassCastException](anyC.asInstanceOf[Null])
  }

  test("c.asInstanceOf[Nothing]") {
    assertThrows[ClassCastException](anyC.asInstanceOf[Nothing])
  }

  test("c.asInstanceOf[Unit]") {
    assertThrows[ClassCastException](anyC.asInstanceOf[Unit])
  }
}
