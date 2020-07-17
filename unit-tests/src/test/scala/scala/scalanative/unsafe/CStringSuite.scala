package scala.scalanative
package unsafe

import scalanative.libc.string._

object CStringSuite extends tests.Suite {

  test("""c"..." literals with various escapes""") {
    // note: `fromCString` is needed to trigger compilation errors against malformed literals
    fromCString(c"")
    fromCString(c"no escapes")
    fromCString(c"\'"); fromCString(c"\\'")
    fromCString(c"\\?")
    fromCString(c"\\")
    fromCString(c"\\a")
    fromCString(c"\\v")
    fromCString(c"%s \\t %.2f %s/s\x00")

    // uncomment the following to trigger compilation errors for testing
    // fromCString(c"\")     // error at NIR
    // fromCString(c"\x2ae") // error at NIR
    // fromCString(c"\"")    // error at Scala compiler
    // fromCString(c"\?")
    // fromCString(c"\v")
    // fromCString(c"\a")
    // fromCString(c"\012")
  }

  test("""the value of c"..." literals""") {

    assertEquals("\b", fromCString(c"\b"))
    assertEquals("\\b", fromCString(c"\\b"))

    assertEquals("\f", fromCString(c"\f"))
    assertEquals("\\f", fromCString(c"\\f"))

    assertEquals("\t", fromCString(c"\t"))
    assertEquals("\\t", fromCString(c"\\t"))

    assertEquals("\\n", fromCString(c"\\n"))
    assertEquals("\n", fromCString(c"\n"))

    assertEquals("\\r", fromCString(c"\\r"))
    assertEquals("\r", fromCString(c"\r"))

    assertEquals("""
    {
      "greeting": "Hello world!"
    }""",
                 fromCString(c"""
    {
      "greeting": "Hello world!"
    }"""))

    assertEquals("\u0020\u0020\u006a\u006b", fromCString(c"\x20\X20\x6a\x6B"))

    assertEquals("\'", fromCString(c"\'"))
    assertEquals("\\'", fromCString(c"\\'"))
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
      assert(cstrTo(5) == '\u0000'.toByte)
    }
  }
}
