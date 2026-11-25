package java.lang

import scalanative.libc.errno._
import scalanative.unsafe._

private[java] object IEEE754Helpers {
  // DO NOT USE STRING INTERPOLATION with an interior double quote ("),
  // see https://github.com/scala/bug/issues/6476

  private def exceptionMsg(s: String) = "For input string: \"" + s + "\""

  @inline private def isDigit(c: Int): scala.Boolean =
    c >= '0' && c <= '9'

  @inline private def isHexDigit(c: Int): scala.Boolean =
    isDigit(c) || ((c | 0x20) >= 'a' && (c | 0x20) <= 'f')

  @inline private def isSuffix(c: Int): scala.Boolean =
    (c | 0x20) == 'd' || (c | 0x20) == 'f'

  private def matches(b: CString, from: Int, end: Int, lit: String): Boolean = {
    if (end - from != lit.length) return false
    var i = 0
    while (i < lit.length) {
      if (b(from + i) != lit.charAt(i).toByte) return false
      i += 1
    }
    true
  }

  /** Validates the ASCII bytes `b[0, len)` against the `Double.valueOf(String)`
   *  grammar.
   *
   *  Returns the `(start, end)` range, packed into a Long, of the part that
   *  should be passed to `strtod`/`strtof`: surrounding whitespace (any char
   *  `<= '\u0020'`) and the `fFdD` suffix are excluded, and for `NaN` the sign
   *  is excluded as well. Returns -1 if the input is not a valid literal.
   */
  private def validate(b: CString, len: Int): scala.Long = {
    var end = len
    while (end > 0 && b(end - 1) <= ' ') end -= 1
    var start = 0
    while (start < end && b(start) <= ' ') start += 1
    if (start == end) return -1L

    var i = start
    val c0 = b(i)
    if (c0 == '+' || c0 == '-') i += 1
    if (i == end) return -1L

    def packed(from: Int, to: Int): scala.Long =
      (from.toLong << 32) | to.toLong

    val c = b(i)
    if (c == 'N')
      return if (matches(b, i, end, "NaN")) packed(i, end) else -1L
    if (c == 'I')
      return if (matches(b, i, end, "Infinity")) packed(start, end) else -1L

    if (isSuffix(b(end - 1))) end -= 1

    if (c == '0' && i + 1 < end && (b(i + 1) | 0x20) == 'x') {
      i += 2
      var nDigits = 0
      while (i < end && isHexDigit(b(i))) { i += 1; nDigits += 1 }
      if (i < end && b(i) == '.') {
        i += 1
        while (i < end && isHexDigit(b(i))) { i += 1; nDigits += 1 }
      }
      if (nDigits == 0 || i == end || (b(i) | 0x20) != 'p') return -1L
    } else {
      var nDigits = 0
      while (i < end && isDigit(b(i))) { i += 1; nDigits += 1 }
      if (i < end && b(i) == '.') {
        i += 1
        while (i < end && isDigit(b(i))) { i += 1; nDigits += 1 }
      }
      if (nDigits == 0) return -1L
      if (i == end) return packed(start, end)
      if ((b(i) | 0x20) != 'e') return -1L
    }

    // Exponent: 'e' / 'p' already checked at b(i)
    i += 1
    if (i < end && (b(i) == '+' || b(i) == '-')) i += 1
    if (i == end) return -1L
    while (i < end && isDigit(b(i))) i += 1
    if (i != end) return -1L
    packed(start, end)
  }

  def parseIEEE754[T](s: String, f: (CString, Ptr[CString]) => T): T = {
    if (s == null)
      throw new NumberFormatException(exceptionMsg(s))

    val len = s.length
    val cStr: CString = stackalloc[scala.Byte](len + 1)
    var i = 0
    while (i < len) {
      val c = s.charAt(i)
      // Grammar and trimmed whitespace are ASCII only
      if (c > 0x7f) throw new NumberFormatException(exceptionMsg(s))
      cStr(i) = c.toByte
      i += 1
    }

    val range = validate(cStr, len)
    if (range < 0L)
      throw new NumberFormatException(exceptionMsg(s))

    val start = cStr + (range >>> 32).toInt
    val stop = cStr + range.toInt
    !stop = 0.toByte

    val end = stackalloc[CString]()
    errno = 0
    val res = f(start, end)

    // ERANGE: res already holds 0.0 or +/-Infinity, as Java requires.
    if ((errno != 0 && errno != ERANGE) || !end != stop)
      throw new NumberFormatException(exceptionMsg(s))

    res
  }
}
