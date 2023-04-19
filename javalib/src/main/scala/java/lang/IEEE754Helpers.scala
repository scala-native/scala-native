package java.lang

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.libc.errno._

private[java] object IEEE754Helpers {
  // Java parseDouble() and parseFloat() allow characters at and after
  // the address where C strtod() or strtof() stopped.
  // Continuous trailing whitespace with or without an initial
  // 'D', 'd', 'F', 'f' size indicator is allowed.
  //
  // Whitespace is defined by Java as being any character with an unsigned code
  // <= '\u0020'. URL:
  //   https://docs.oracle.com/javase/8/docs/api/java/lang/String.html#trim--
  // It can include horizontal tabs ('\t') and even newlines!('\n').

  // DO NOT USE STRING INTERPOLATION with an interior double quote ("),
  // a.k.a Unicode "QUOTATION MARK" (\u0022).
  // Double quote failing interpolated strings is a longstanding
  // bug in many Scala versions, including 2.12.n, & 2.13.2.
  // See URLS:
  //     https://github.com/scala/bug/issues/6476
  //     https://github.com/scala/scala/pull/8830
  // The second is yet unmerged for Scala 2.13.x.

  private def exceptionMsg(s: String) = "For input string: \"" + s + "\""

  /** Converts a `CharSequence` to a `CString` type. The `CString` pointer is
   *  passed to allow stack allocation from caller. The `CharSequence`
   *  characters are iterated and converted to ASCII bytes. In order to be
   *  considered as a valid ASCII sequence, its characters be all ASCII. This
   *  should be the case if the first byte of the `Char` is zero, which is
   *  verified by applying the mask 0xFF80.
   */
  @inline
  private def _numericCharSeqToCString(
      csq: CharSequence,
      nChars: Int,
      cStrOut: CString
  ): Boolean = {

    var i = 0
    while (i < nChars) {
      // If the CharSequence contains valid characters (see strtod/strtof)
      // they should correspond to ASCII chars (thus first byte is zero).
      if ((csq.charAt(i) & 0xff80) != 0) {
        return false
      }
      // Convert UTF16 Char to ASCII Byte
      cStrOut(i) = csq.charAt(i).toByte
      i += 1
    }

    // Add NUL-terminator to CString
    cStrOut(nChars) = 0.toByte

    // Return true if conversion went fine
    true
  }

  def parseIEEE754[T](s: String, f: (CString, Ptr[CString]) => T): T = {
    if (s == null)
      throw new NumberFormatException(exceptionMsg(s))

    val nChars = s.length
    if (nChars == 0)
      throw new NumberFormatException(exceptionMsg(s))

    val cStr: CString = stackalloc[scala.Byte]((nChars + 1).toUInt)

    if (_numericCharSeqToCString(s, nChars, cStr) == false) {
      throw new NumberFormatException(exceptionMsg(s))
    }

    val end = stackalloc[CString]() // Address one past last parsed cStr byte.

    errno = 0

    val res = f(cStr, end)

    if (errno != 0) {
      if (errno == ERANGE) {
        // Do nothing. res holds the proper value as returned by strtod()
        // or strtof(): 0.0 for string translations too close to zero
        // or +/- infinity for values too +/- large for an IEEE754.
        // Slick C lib design!
      } else {
        throw new NumberFormatException(exceptionMsg(s))
      }
    } else if (!end == cStr) { // No leading digit found: only "D" not "0D"
      throw new NumberFormatException(exceptionMsg(s))
    } else {
      // Beware: cStr may have interior NUL/null bytes. Better to
      //         consider it a counted byte array rather than a proper
      //         C string.

      val bytesLen = nChars
      val nSeen = !end - cStr

      // If we used less bytes than in our input, there is a risk that the input contains invalid characters.
      // We should thus verify if the input contains only valid characters.
      // See: https://github.com/scala-native/scala-native/issues/2903
      if (nSeen != bytesLen) {
        // magic: is first char one of D d F f
        var idx =
          if ((cStr(nSeen.toUSize) & 0xdd) == 0x44) (nSeen + 1) else nSeen

        while (idx < bytesLen) { // Check for garbage in the unparsed remnant.
          val b = cStr(idx.toUSize)
          if ((b < 0) || b > 0x20) {
            throw new NumberFormatException(exceptionMsg(s))
          }
          idx += 1
        }
      }
    }

    res
  }
}
