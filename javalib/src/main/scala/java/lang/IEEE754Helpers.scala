package java.lang

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

  @inline private def skipDigits(
      b: CString,
      from: Int,
      end: Int,
      hex: scala.Boolean
  ): Int = {
    var i = from
    while (i < end && (if (hex) isHexDigit(b(i)) else isDigit(b(i)))) i += 1
    i
  }

  private def isLiteral(b: CString, from: Int, end: Int, lit: String) = {
    var i = 0
    while (i < lit.length && from + i < end && b(from + i) == lit.charAt(i))
      i += 1
    i == lit.length && from + i == end
  }

  /** Is `b[from, end)` an unsigned decimal or hexadecimal floating point
   *  literal without the type suffix.
   */
  private def isNumber(b: CString, from: Int, end: Int): scala.Boolean = {
    val hex = end - from >= 2 && b(from) == '0' && (b(from + 1) | 0x20) == 'x'
    val mantissa = if (hex) from + 2 else from
    var i = skipDigits(b, mantissa, end, hex)
    var nDigits = i - mantissa
    if (i < end && b(i) == '.') {
      val j = skipDigits(b, i + 1, end, hex)
      nDigits += j - i - 1
      i = j
    }
    if (nDigits == 0) false
    else if (i == end) !hex // hex requires a binary exponent
    else if ((b(i) | 0x20) != (if (hex) 'p' else 'e')) false
    else {
      i += 1
      if (i < end && (b(i) == '+' || b(i) == '-')) i += 1
      val j = skipDigits(b, i, end, hex = false)
      j > i && j == end
    }
  }

  /** Validates `b[0, len)` against the `Double.valueOf(String)` grammar and
   *  NUL-terminates it before the trailing whitespace and suffix. Returns the
   *  offset to pass to `strtod`/`strtof`, or -1 if invalid.
   */
  private def prepare(b: CString, len: Int): Int = {
    var end = len
    while (end > 0 && b(end - 1) <= ' ') end -= 1
    var start = 0
    while (start < end && b(start) <= ' ') start += 1
    val body =
      if (start < end && (b(start) == '+' || b(start) == '-')) start + 1
      else start

    val offset =
      if (isLiteral(b, body, end, "NaN")) body // strtod("-NaN") keeps the sign
      else if (isLiteral(b, body, end, "Infinity")) start
      else {
        if (end > body && isSuffix(b(end - 1))) end -= 1
        if (isNumber(b, body, end)) start else -1
      }
    b(end) = 0.toByte
    offset
  }

  def parseIEEE754[T](s: String, f: (CString, Ptr[CString]) => T): T = {
    if (s == null)
      throw new NumberFormatException(exceptionMsg(s))

    val len = s.length
    val cStr: CString = stackalloc[scala.Byte](len + 1)
    var i = 0
    while (i < len) {
      val c = s.charAt(i)
      if (c > 0x7f) throw new NumberFormatException(exceptionMsg(s))
      cStr(i) = c.toByte
      i += 1
    }

    val offset = prepare(cStr, len)
    if (offset < 0)
      throw new NumberFormatException(exceptionMsg(s))

    // Out of range values yield 0.0 or +/-Infinity, as Java requires.
    val end = stackalloc[CString]()
    val res = f(cStr + offset, end)
    if (!(!end) != 0)
      throw new NumberFormatException(exceptionMsg(s))
    res
  }
}
