package java.lang

private[lang] object DecimalParseHelpers {

  // The caller bounds the prefix so its magnitude fits in the signed
  // accumulator. The remaining digits still use checked arithmetic.
  @inline
  def parsePrefix(
      s: String,
      start: scala.Int,
      end: scala.Int
  ): scala.Long = {
    var result = 0L
    var offset = start
    while (offset < end) {
      val ch = s.charAt(offset)
      val asciiDigit = ch - '0'
      val digit =
        if (asciiDigit >= 0 && asciiDigit <= 9) asciiDigit
        else {
          val unicodeDigit = Character.digit(ch, 10)
          if (unicodeDigit < 0) fail(s)
          unicodeDigit
        }
      result = result * 10 + digit
      offset += 1
    }
    result
  }

  private def fail(s: String): Nothing =
    throw new NumberFormatException(s"""For input string: "$s"""")
}
