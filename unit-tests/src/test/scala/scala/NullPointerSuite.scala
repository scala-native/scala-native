package scala.scalanative.native

object NullPointerSuite extends tests.Suite {
  class E extends Exception
  class C { var x: Int = 42; def f(): Int = x }

  @noinline def notNullE: E            = new E
  @noinline def nullE: E               = null
  @noinline def throwNotNullE: Nothing = throw notNullE
  @noinline def throwNullE: Nothing    = throw nullE

  @noinline def nullC: C    = null
  @noinline def notNullC: C = new C

  @noinline def notNullArray: Array[Int] = Array(1, 2, 3)
  @noinline def nullArray: Array[Int]    = null

  test("call method on on non-null object") {
    assert(notNullC.f() == 42)
  }

  test("call method on on null object") {
    assertThrows[NullPointerException](nullC.f())
  }

  test("load field on non-null object") {
    assert(notNullC.x == 42)
  }

  test("load field on null object") {
    assertThrows[NullPointerException](nullC.x)
  }

  test("store field on non-null object") {
    val c = notNullC
    c.x = 84
    assert(c.x == 84)
  }

  test("store field on null object") {
    assertThrows[NullPointerException](nullC.x = 84)
  }

  test("load element from non-null array") {
    assert(notNullArray(0) == 1)
  }

  test("load element from null array") {
    assertThrows[NullPointerException](nullArray(0))
  }

  test("store element to non-null array") {
    val arr = notNullArray
    arr(0) = 42
    assert(arr(0) == 42)
  }

  test("store element to null array") {
    val arr = nullArray
    assertThrows[NullPointerException](arr(0) == 42)
  }

  test("load length from non-null array") {
    assert(notNullArray.length == 3)
  }

  test("load length from null array") {
    assertThrows[NullPointerException](nullArray.length)
  }

  test("throw exception") {
    assertThrows[E](throwNotNullE)
  }

  test("throw null") {
    assertThrows[NullPointerException](throwNullE)
  }
}
