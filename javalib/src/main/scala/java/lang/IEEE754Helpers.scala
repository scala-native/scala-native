package java.lang

import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.libc.errno

import scalanative.posix.errno.ERANGE

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
  // bug in many Scala versions, including 2.11.n, 2.12.n, & 2.13.2.
  // See URLS:
  //     https://github.com/scala/bug/issues/6476
  //     https://github.com/scala/scala/pull/8830
  // The second is yet unmerged for Scala 2.13.x.

  private def exceptionMsg(s: String) = "For input string \"" + s + "\""

  private def bytesToCString(bytes: Array[scala.Byte], n: Int)(
      implicit z: Zone): CString = {
    val cStr = z.alloc((n + 1).toUInt) // z.alloc() does not clear bytes.

    var c = 0
    while (c < n) {
      !(cStr + c) = bytes(c)
      c += 1
    }

    !(cStr + n) = 0.toByte

    cStr
  }

  def parseIEEE754[T](s: String, f: (CString, Ptr[CString]) => T): T = {
    Zone { implicit z =>
      val bytes    = s.getBytes(java.nio.charset.Charset.defaultCharset())
      val bytesLen = bytes.length

      val cStr = bytesToCString(bytes, bytesLen)

      val end = stackalloc[CString] // Address one past last parsed cStr byte.

      errno.errno = 0
      var res = f(cStr, end)

      if (errno.errno != 0) {
        if (errno.errno == ERANGE) {
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

        val nSeen = !end - cStr

        // magic: is first char one of D d F f
        var idx = if ((cStr(nSeen) & 0xdd) == 0x44) (nSeen + 1) else nSeen

        while (idx < bytesLen) { // Check for garbage in the unparsed remnant.
          val b = cStr(idx)
          if ((b < 0) || b > 0x20) {
            throw new NumberFormatException(exceptionMsg(s))
          }
          idx += 1
        }
      }

      res
    }
  }
}
