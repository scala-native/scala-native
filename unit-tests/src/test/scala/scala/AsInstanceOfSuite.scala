package scala.scalanative.native

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
    assert(anyNull.asInstanceOf[Null] == null)
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
