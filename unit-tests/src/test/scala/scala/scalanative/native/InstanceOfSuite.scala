package scala.scalanative.native

object InstanceOfSuite extends tests.Suite {

  // asInstanceOf

//  test("expects anyRef.asInstanceOf[String] fail") {
//    shouldNotGetHere((new AnyRef).asInstanceOf[String])
//  }

  test("expects \"\".asInstanceOf[String] succeed") {
    assertNotNull("".asInstanceOf[String])
  }

  test("expects null.asInstanceOf[String] should succeed") {
    assertNull(null.asInstanceOf[String])
  }

  test("expects a.asInstanceOf[String], where a = null should succeed") {
    castNullToString(null)
  }

  def castNullToString(x: AnyRef): Unit = {
    if (x.isInstanceOf[String]) {
      assertNull(x.asInstanceOf[String])
    }

  }

  def assertNull(s: String): Unit = {
    assert(s == null)
  }

  def assertNotNull(s: String): Unit = {
    assert(s != null)
  }

  def shouldNotGetHere(s: String): Unit = {
    assert(false)
  }

  // isInstanceOf

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
