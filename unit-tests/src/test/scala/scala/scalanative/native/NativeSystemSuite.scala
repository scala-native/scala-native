package scala.scalanative.native

import java.lang.System._

object NativeSystemSuite extends tests.Suite {
  val name  = "FOO"
  val value = "foobar"

  test("setenv") {
    val res = system.setenv(name, value)
    assert(res == true)
  }

  test("getenv matches") {
    val foobar = getenv(name)
    assertEquals(value, foobar)
  }

  test("setenv name check") {
    assertThrows[IllegalArgumentException](system.setenv(null, value))
    assertThrows[IllegalArgumentException](system.setenv("", value))
    assertThrows[IllegalArgumentException](system.setenv("foo=bar", value))
  }

}
