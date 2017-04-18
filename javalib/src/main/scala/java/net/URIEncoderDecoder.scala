package java.net

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

object URIEncoderDecoder {

  //$NON-NLS-1$
  val digits: String = "0123456789ABCDEF"

  //$NON-NLS-1$
  val encoding: String = "UTF8"

  /**
   * Validate a string by checking if it contains any characters other than:
   * 1. letters ('a'..'z', 'A'..'Z') 2. numbers ('0'..'9') 3. characters in
   * the legalset parameter 4. others (unicode characters that are not in
   * US-ASCII set, and are not ISO Control or are not ISO Space characters)
   * <p>
   * called from {@code URI.Helper.parseURI()} to validate each component
   *
   * @param s
   *            {@code java.lang.String} the string to be validated
   * @param legal
   *            {@code java.lang.String} the characters allowed in the String
   *            s
   */
  @throws(classOf[URISyntaxException])
  def validate(s: String, legal: String): Unit = {
    var i: Int = 0
    while (i < s.length) {
      val ch: Char = s.charAt(i)
      if (ch == '%') {
        do {
          if (i + 2 >= s.length) {
            throw new //$NON-NLS-1$
            URISyntaxException(s, "Incomplete % sequence", i)
          }
          val d1: Int = java.lang.Character.digit(s.charAt(i + 1), 16)
          val d2: Int = java.lang.Character.digit(s.charAt(i + 2), 16)
          if (d1 == -1 || d2 == -1) {
            throw new URISyntaxException(
              s,
              "Invalid % sequence (" + s.substring(i, i + 3) + ")",
              i)
          }
          i += 3
        } while (i < s.length && s.charAt(i) == '%')
      }
      if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
            (ch >= '0' && ch <= '9') ||
            legal.indexOf(ch) > -1 ||
            (ch > 127 && !java.lang.Character.isSpaceChar(ch) && !java.lang.Character
              .isISOControl(ch)))) {
        //$NON-NLS-1$
        throw new URISyntaxException(s, "Illegal character", i)
      }
      { i += 1; i - 1 }
    }
  }

  @throws(classOf[URISyntaxException])
  def validateSimple(s: String, legal: String): Unit = {
    var i: Int = 0
    while (i < s.length) {
      val ch: Char = s.charAt(i)
      if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
            (ch >= '0' && ch <= '9') ||
            legal.indexOf(ch) > -1)) {
        //$NON-NLS-1$
        throw new URISyntaxException(s, "Illegal character", i)
      }
      { i += 1; i - 1 }
    }
  }

  /**
   * All characters except letters ('a'..'z', 'A'..'Z') and numbers ('0'..'9')
   * and legal characters are converted into their hexidecimal value prepended
   * by '%'.
   * <p>
   * For example: '#' -> %23
   * Other characters, which are unicode chars that are not US-ASCII, and are
   * not ISO Control or are not ISO Space chars, are preserved.
   * <p>
   * Called from {@code URI.quoteComponent()} (for multiple argument
   * constructors)
   *
   * @param s
   *            java.lang.String the string to be converted
   * @param legal
   *            java.lang.String the characters allowed to be preserved in the
   *            string s
   * @return java.lang.String the converted string
   */
  @throws(classOf[UnsupportedEncodingException])
  def quoteIllegal(s: String, legal: String): String = {
    val buf: StringBuilder = new StringBuilder()
    for (i <- 0 until s.length) {
      val ch: Char = s.charAt(i)
      if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
          (ch >= '0' && ch <= '9') ||
          legal.indexOf(ch) > -1 ||
          (ch > 127 && !java.lang.Character.isSpaceChar(ch) && !java.lang.Character
            .isISOControl(ch))) {
        buf.append(ch)
      } else {
        val bytes: Array[Byte] = new String(Array(ch)).getBytes(encoding)
        for (j <- bytes.indices) {
          buf.append('%')
          buf.append(digits.charAt((bytes(j) & 0xf0) >> 4))
          buf.append(digits.charAt(bytes(j) & 0xf))
        }
      }
    }
    buf.toString
  }

  /**
   * Other characters, which are Unicode chars that are not US-ASCII, and are
   * not ISO Control or are not ISO Space chars are not preserved. They are
   * converted into their hexidecimal value prepended by '%'.
   * <p>
   * For example: Euro currency symbol -> "%E2%82%AC".
   * <p>
   * Called from URI.toASCIIString()
   *
   * @param s
   *            java.lang.String the string to be converted
   * @return java.lang.String the converted string
   */
  @throws(classOf[UnsupportedEncodingException])
  def encodeOthers(s: String): String = {
    val buf: StringBuilder = new StringBuilder()
    for (i <- 0 until s.length) {
      val ch: Char = s.charAt(i)
      if (ch <= 127) {
        buf.append(ch)
      } else {
        val bytes: Array[Byte] = new String(Array(ch)).getBytes(encoding)
        for (j <- bytes.indices) {
          buf.append('%')
          buf.append(digits.charAt((bytes(j) & 0xf0) >> 4))
          buf.append(digits.charAt(bytes(j) & 0xf))
        }
      }
    }
    buf.toString
  }

  /**
   * Decodes the string argument which is assumed to be encoded in the {@code
   * x-www-form-urlencoded} MIME content type using the UTF-8 encoding scheme.
   * <p>
   *'%' and two following hex digit characters are converted to the
   * equivalent byte value. All other characters are passed through
   * unmodified.
   * <p>
   * e.g. "A%20B%20C %24%25" -> "A B C $%"
   * <p>
   * Called from URI.getXYZ() methods
   *
   * @param s
   *            java.lang.String The encoded string.
   * @return java.lang.String The decoded version.
   */
  @throws(classOf[UnsupportedEncodingException])
  def decode(s: String): String = {
    val result: StringBuilder      = new StringBuilder()
    val out: ByteArrayOutputStream = new ByteArrayOutputStream()
    var i: Int                     = 0
    while (i < s.length) {
      val c: Char = s.charAt(i)
      if (c == '%') {
        out.reset()
        do {
          if (i + 2 >= s.length) {
            throw new IllegalArgumentException(
              "Incomplete % sequence at: " + i)
          }
          val d1: Int = java.lang.Character.digit(s.charAt(i + 1), 16)
          val d2: Int = java.lang.Character.digit(s.charAt(i + 2), 16)
          if (d1 == -1 || d2 == -1) {
            throw new IllegalArgumentException(
              "Invalid % sequence (" + s.substring(i, i + 3) + ") at: " + i)
          }
          out.write(((d1 << 4) + d2).toByte)
          i += 3
        } while (i < s.length && s.charAt(i) == '%')
        result.append(out.toString(encoding))
      }
      result.append(c) { i += 1; i - 1 }
    }
    result.toString
  }

}
