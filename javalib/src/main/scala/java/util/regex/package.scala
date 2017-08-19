package java.util

import java.nio.charset.Charset
import scala.scalanative.native.CString
import scala.scalanative.native.Zone

package object regex {

  /**
   * Convert a CString to a String using given charset.
   *
   * Mostly copied from scala.scalanative.native.fromCString.
   * fromCString cannot be used when an array of C-char contains null bytes in the middle of the array.
   *
   * @param len The number of characters in the C-style string.
   *   This should be equal to `strlen(cstr)` if `cstr` doesn't contain any null char.
   *   That means `len` doesn't count the terminating null byte.
   */
  private[regex] def fromCStringN(
      cstr: CString,
      len: Long,
      charset: Charset = Charset.defaultCharset()): String = {
    val bytes = new Array[Byte](len.toInt)

    var c = 0
    while (c < len) {
      bytes(c) = !(cstr + c)
      c += 1
    }

    new String(bytes)
  }
}
