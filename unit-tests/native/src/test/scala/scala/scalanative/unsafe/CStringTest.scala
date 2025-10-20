package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert.*

import java.nio.charset.Charset
import scalanative.libc.string.*
import scalanative.unsigned.*
// Scala 2.13.7 needs explicit import for implicit conversions
import scalanative.unsafe.Ptr.ptrToCArray

class CStringTest {

  @Test def cInterpolationLiteralsWithVariousEscapes(): Unit = {
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

  @Test def valueOfCInterpolationLiterals(): Unit = {

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

    assertEquals(
      """
    {
      "greeting": "Hello world!"
    }""",
      fromCString(c"""
    {
      "greeting": "Hello world!"
    }""")
    )

    assertEquals("\u0020\\X20\u006a\u006b", fromCString(c"\x20\X20\x6a\x6B"))

    assertEquals("\'", fromCString(c"\'"))
    assertEquals("\\'", fromCString(c"\\'"))
  }

  @Test def fromCStringNullReturnsNullIssue1796(): Unit = {
    assertNull(fromCString(null))
  }

  @Test def testFromCString(): Unit = {
    val cstrFrom = c"1234"
    val szTo = fromCString(cstrFrom)

    assertTrue(szTo.size == 4)
    assertTrue(szTo.charAt(0) == '1')
    assertTrue(szTo.charAt(1) == '2')
    assertTrue(szTo.charAt(2) == '3')
    assertTrue(szTo.charAt(3) == '4')
  }

  @Test def fromCStringSliceNullReturnsNull(): Unit = {
    assertNull(fromCStringSlice(null, 0.toUSize))
  }

  @Test def testFromCStringSlice(): Unit = {
    val cstrFrom = c"1234"
    val sameSize = fromCStringSlice(cstrFrom, 4.toUSize)

    assertTrue(sameSize.size == 4)
    assertTrue(sameSize.charAt(0) == '1')
    assertTrue(sameSize.charAt(1) == '2')
    assertTrue(sameSize.charAt(2) == '3')
    assertTrue(sameSize.charAt(3) == '4')

    val smaller = fromCStringSlice(cstrFrom, 3.toUSize)

    assertTrue(smaller.size == 3)
    assertTrue(smaller.charAt(0) == '1')
    assertTrue(smaller.charAt(1) == '2')
    assertTrue(smaller.charAt(2) == '3')
  }

  @Test def toCStringNullReturnsNullIssue1796(): Unit = {
    Zone.acquire { implicit z => assertNull(toCString(null)) }
  }

  @Test def testToCString(): Unit = {
    Zone.acquire { implicit z =>
      val szFrom = "abcde"
      val cstrTo = toCString(szFrom)
      assertEquals(5.toUSize, strlen(cstrTo))
      assertTrue(cstrTo(0) == 'a'.toByte)
      assertTrue(cstrTo(1) == 'b'.toByte)
      assertTrue(cstrTo(2) == 'c'.toByte)
      assertTrue(cstrTo(3) == 'd'.toByte)
      assertTrue(cstrTo(4) == 'e'.toByte)
      assertTrue(cstrTo(5) == '\u0000'.toByte)

      val piArr = Charset.forName("UTF-8").encode("\u03c0")
      val cstr2 = toCString("2\u03c0r")
//    val cstr3 = c"2\u03c0r" //would result in error at NIR
      assertEquals(4.toUSize, strlen(cstr2))
      assertEquals(cstr2(0), '2')
      assertEquals(cstr2(1), piArr.get(0))
      assertEquals(cstr2(2), piArr.get(1))
      assertEquals(cstr2(3), 'r')
      assertEquals(cstr2(4), 0)
    }
  }

  @Test def toFromCString(): Unit = {
    Zone.acquire { implicit z =>
      type _11 = Nat.Digit2[Nat._1, Nat._1]
      val arr = unsafe.stackalloc[CArray[Byte, _11]]()

      val jstr1 = "a\b\\c\u0064"
      val cstr1 = c"a\b\\c\x64"
      val cstr2 = toCString(jstr1)

      strcat(arr.at(0), cstr1)
      strcat(arr.at(0), cstr2)

      val jstr2: String = fromCString(arr.at(0))

      assertEquals(strcmp(cstr1, cstr2), 0)
      assertEquals(strlen(arr.at(0)), 10.toUSize)

      assertEquals(jstr2, jstr1 * 2)
      assertEquals(jstr2.last, 'd')
      assertEquals(!(cstr1 + 4), 'd')
      assertEquals(!(cstr1 + 5), 0)
    }
  }

  @Test def cStringNonASCII(): Unit = {
    // note: `fromCString` is needed to trigger compilation errors against malformed literals
    fromCString(c"日本語")
    fromCString(c"język polski")
    fromCString(c"한국어")

    fromCString(c"🚂🚀🚁🍔")
  }
}
