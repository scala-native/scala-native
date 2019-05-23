package scala.scalanative.unsafe

object IsInstanceOfSuite extends tests.Suite {

  test("expects (new AnyRef).asInstanceOf[AnyRef] should succeeds") {
    (new AnyRef).asInstanceOf[AnyRef]
  }

  test("expects anyRef.isInstanceOf[String] == false") {
    val anyRef = new AnyRef
    assert(!anyRef.isInstanceOf[String])
  }

  test("expects literal null.isInstanceOf[String] == false") {
    assert(!null.isInstanceOf[String])
  }

  test("expects \"\".isInstanceOf[String] == true") {
    assert("".isInstanceOf[String])
  }

  test("expects a.isInstanceOf[String] == true, where a = \"\"") {
    assertIsInstanceOfString("", "")
  }

  test("expects a.isInstanceOf[String] == false, where a = null") {
    assertNullIsNotInstanceOfString(null, null)
  }

  def assertNullIsNotInstanceOfString(a: AnyRef, b: AnyRef): Unit = {
    assert(!a.isInstanceOf[String])
    assert(!b.isInstanceOf[String])
  }

  def assertIsInstanceOfString(a: AnyRef, b: AnyRef): Unit = {
    assert(a.isInstanceOf[String])
    assert(b.isInstanceOf[String])
  }

}
