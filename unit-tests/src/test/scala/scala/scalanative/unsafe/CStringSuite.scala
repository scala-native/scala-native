package scala.scalanative
package unsafe

import java.nio.charset.Charset
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

    assertEquals("\n", fromCString(c"\n"))
    assertEquals("\\n", fromCString(c"\\n"))

    assertEquals("\r", fromCString(c"\r"))
    assertEquals("\\r", fromCString(c"\\r"))

    assertEquals("\u0065", fromCString(c"\x65"))
    assertEquals("\\x65", fromCString(c"\\x65"))

    assertEquals("""
    {
      "greeting": "Hello world!"
    }""",
                 fromCString(c"""
    {
      "greeting": "Hello world!"
    }"""))

    assertEquals("\u0020\\X20\u006a\u006b", fromCString(c"\x20\X20\x6a\x6B"))

    assertEquals("\'", fromCString(c"\'"))
    assertEquals("\\'", fromCString(c"\\'"))
  }

  // Issue 1796
  test("fromCString(null) returns null") {
    assertNull(fromCString(null))
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

  // Issue 1796
  test("toCString(null) return null") {
    Zone { implicit z => assertNull(toCString(null)) }
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

      val piArr = Charset.forName("UTF-8").encode("\u03c0")
      val cstr2 = toCString("2\u03c0r")
//    val cstr3 = c"2\u03c0r" //would result in error at NIR
      assertEquals(strlen(cstr2), 4)
      assertEquals(cstr2(0), '2')
      assertEquals(cstr2(1), piArr.get(0))
      assertEquals(cstr2(2), piArr.get(1))
      assertEquals(cstr2(3), 'r')
      assertEquals(cstr2(4), 0)
    }
  }

  test("to/from CString") {
    Zone { implicit z =>
      type _11 = Nat.Digit2[Nat._1, Nat._1]
      val arr = unsafe.stackalloc[CArray[Byte, _11]]

      val jstr1 = "a\b\\c\u0064"
      val cstr1 = c"a\b\\c\x64"
      val cstr2 = toCString(jstr1)

      strcat(arr.at(0), cstr1)
      strcat(arr.at(0), cstr2)

      val jstr2: String = fromCString(arr.at(0))

      assertEquals(strcmp(cstr1, cstr2), 0)
      assertEquals(strlen(arr.at(0)), 10)

      assertEquals(jstr2, jstr1 * 2)
      assertEquals(jstr2.last, 'd')
      assertEquals(!(cstr1 + 4), 'd')
      assertEquals(!(cstr1 + 5), 0)
    }
  }
}
