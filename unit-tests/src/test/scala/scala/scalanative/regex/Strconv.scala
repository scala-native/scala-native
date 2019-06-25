package scala.scalanative
package regex

object Strconv { // unquoteChar decodes the first character or byte in the escaped
  // string or character literal represented by the Go literal encoded
  // in UTF-16 in s.
  //
  // On success, it advances the UTF-16 cursor i[0] (an in/out
  // parameter) past the consumed codes and returns the decoded Unicode
  // code point or byte value.  On failure, it throws
  // IllegalArgumentException or StringIndexOutOfBoundsException
  //
  // |quote| specifies the type of literal being parsed
  // and therefore which escaped quote character is permitted.
  // If set to a single quote, it permits the sequence \' and disallows
  // unescaped '.
  // If set to a double quote, it permits \" and disallows unescaped ".
  // If set to zero, it does not permit either escape and allows both
  // quote characters to appear unescaped.
  private def unquoteChar(s: String, i: Array[Int], quote: Char): Int = {
    var c = s.codePointAt(i(0))
    i(0) = s.offsetByCodePoints(i(0), 1) // (throws if falls off end)

    // easy cases
    if (c == quote && (quote == '\'' || quote == '"'))
      throw new IllegalArgumentException("unescaped quotation mark in literal")
    if (c != '\\') return c
    // hard case: c is backslash
    c = s.codePointAt(i(0))
    i(0) = s.offsetByCodePoints(i(0), 1)
    c match {
      case 'a' =>
        0x07
      case 'b' =>
        '\b'
      case 'f' =>
        '\f'
      case 'n' =>
        '\n'
      case 'r' =>
        '\r'
      case 't' =>
        '\t'
      case 'v' =>
        0x0B
      case 'x' | 'u' | 'U' =>
        var n = 0
        c match {
          case 'x' =>
            n = 2
          case 'u' =>
            n = 4
          case 'U' =>
            n = 8
        }
        var v = 0
        var j = 0
        while (j < n) {
          val d = s.codePointAt(i(0))
          i(0) = s.offsetByCodePoints(i(0), 1)
          val x = Utils.unhex(d)
          if (x == -1)
            throw new IllegalArgumentException("not a hex char: " + d)
          v = (v << 4) | x

          {
            j += 1;
            j - 1
          }
        }
        if (c == 'x') return v
        if (v > Unicode.MAX_RUNE)
          throw new IllegalArgumentException("Unicode code point out of range")
        v
      case '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' =>
        var v = c - '0'
        var j = 0
        while (j < 2) { // one digit already; two more
          val d = s.codePointAt(i(0))
          i(0) = s.offsetByCodePoints(i(0), 1)
          val x = d - '0'
          if (x < 0 || x > 7)
            throw new IllegalArgumentException("illegal octal digit")
          v = (v << 3) | x

          {
            j += 1; j - 1
          }
        }
        if (v > 255)
          throw new IllegalArgumentException("octal value out of range")
        v

      case '\\' =>
        '\\'
      case '\'' | '"' =>
        if (c != quote)
          throw new IllegalArgumentException("unnecessary backslash escape")
        c
      case _ =>
        throw new IllegalArgumentException("unexpected character")
    }
  }

  // Unquote interprets s as a single-quoted, double-quoted,
  // or backquoted Go string literal, returning the string value
  // that s quotes.  (If s is single-quoted, it would be a Go
  // character literal; Unquote returns the corresponding
  // one-character string.)
  def unquote(_s: String): String = {
    var s = _s
    val n = s.length
    if (n < 2) throw new IllegalArgumentException("too short")
    val quote = s.charAt(0)
    if (quote != s.charAt(n - 1))
      throw new IllegalArgumentException("quotes don't match")
    s = s.substring(1, n - 1)
    if (quote == '`') {
      if (s.indexOf('`') >= 0)
        throw new IllegalArgumentException("backquoted string contains '`'")
      return s
    }
    if (quote != '"' && quote != '\'')
      throw new IllegalArgumentException("invalid quotation mark")
    if (s.indexOf('\n') >= 0)
      throw new IllegalArgumentException("multiline string literal")
    // Is it trivial?  Avoid allocation.
    if (s.indexOf('\\') < 0 && s.indexOf(quote) < 0)
      if (quote == '"' || // "abc"
          s.codePointCount(0, s.length) == 1) { // 'a'
        // if s == "\\" then this return is wrong.
        return s
      }
    val i = Array(0)
    // UTF-16 index, an in/out-parameter of unquoteChar.
    val buf = new StringBuffer()
    val len = s.length
    while (i(0) < len) {
      buf.appendCodePoint(unquoteChar(s, i, quote))
      if (quote == '\'' && i(0) != len)
        throw new IllegalArgumentException("single-quotation must be one char")
    }
    buf.toString
  }
}
