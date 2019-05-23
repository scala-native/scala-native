package scala.scalanative
package unsafe

import scalanative.libc.stdio._
import scalanative.libc.string._

object CStringSuite extends tests.Suite {

  test("""c"..." literals with various escapes""") {
    // note: `fromCString` is needed to trigger compilation errors against malformed literals
    fromCString(c"")
    fromCString(c"no escapes")
    fromCString(c"\'"); fromCString(c"\\'")
    fromCString(c"\?"); fromCString(c"\\?")
    fromCString(c"\\")
    fromCString(c"\a"); fromCString(c"\\a")
    fromCString(c"\b"); fromCString(c"\\b")
    fromCString(c"\f"); fromCString(c"\\f")
    fromCString(c"\n"); fromCString(c"\\n")
    fromCString(c"\r"); fromCString(c"\\r")
    fromCString(c"\t"); fromCString(c"\\t")
    fromCString(c"\v"); fromCString(c"\\v")
    fromCString(c"\012\x6a")
    fromCString(c"%s \\t %.2f %s/s\00")

    // uncomment the following to trigger compilation errors for testing
    // fromCString(c"\")     // error at NIR
    // fromCString(c"\x2ae") // error at NIR
    // fromCString(c"\"")    // error at Scala compiler
  }

  test("""the value of c"..." literals""") {
    assertEquals("\t", fromCString(c"\t"))
    assertEquals("\\t", fromCString(c"\\t"))
    assertEquals("\u0020\u0020\u0061\u0062", fromCString(c"\040\40\141\x62"))
  }

  test("fromCString") {
    val cstrFrom = c"1234"
    val szTo     = fromCString(cstrFrom)

    assert(szTo.size == 4)
    assert(szTo.charAt(0) == '1')
    assert(szTo.charAt(1) == '2')
    assert(szTo.charAt(2) == '3')
    assert(szTo.charAt(3) == '4')
  }

  test("toCString") {
    Zone { implicit z =>
      val szFrom = "abcde"
      val cstrTo = toCString(szFrom)

      assert(strlen(cstrTo) == 5)
      assert(cstrTo(0) == 'a'.toByte)
      assert(cstrTo(1) == 'b'.toByte)
      assert(cstrTo(2) == 'c'.toByte)
      assert(cstrTo(3) == 'd'.toByte)
      assert(cstrTo(4) == 'e'.toByte)
      assert(cstrTo(5) == '\0'.toByte)
    }
  }
}
