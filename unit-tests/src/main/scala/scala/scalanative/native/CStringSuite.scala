package scala.scalanative.native

import stdio._
import string._

object CStringSuite extends tests.Suite {
  implicit val alloc = Alloc.system

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
