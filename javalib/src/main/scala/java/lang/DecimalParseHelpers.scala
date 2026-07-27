package java.lang

private[lang] object DecimalParseHelpers {
  @inline def parseDecimalDigit(s: String, offset: scala.Int): scala.Int = {
    val ch = s.charAt(offset)
    val digit = ch - '0'
    if (digit >= 0 && digit <= 9) digit
    else {
      val unicodeDigit = Character.digit(ch, 10)
      if (unicodeDigit == -1) fail(s)
      unicodeDigit
    }
  }

  private def fail(s: String): Nothing =
    throw new NumberFormatException(s"""For input string: "$s"""")
}
