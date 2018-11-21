package scala.scalanative.native

object FieldSuite extends tests.Suite {

  class C { var x: Int = 42 }

  @noinline def nullC: C    = null
  @noinline def notNullC: C = new C

  test("load non-null object field") {
    assert(notNullC.x == 42)
  }

  test("load null object field") {
    assertThrows[NullPointerException](nullC.x)
  }

  test("store non-null object field") {
    val c = notNullC
    c.x = 84
    assert(c.x == 84)
  }

  test("store null object field") {
    assertThrows[NullPointerException](nullC.x = 84)
  }
}
