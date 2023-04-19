package java.net

// Ported from Harmony

import scala.annotation.tailrec

object URLEncoder {
  private[this] val digits = "0123456789ABCDEF".toCharArray

  def encode(s: String, enc: String): String = {
    if (s == null || enc == null) {
      throw new NullPointerException
    }
    // check for UnsupportedEncodingException
    "".getBytes(enc)
    val buf = new java.lang.StringBuilder(s.length + 16)
    var start = -1
    @tailrec
    def loop(i: Int): Unit = {
      if (i < s.length) {
        val ch = s.charAt(i)
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || " .-*_"
              .indexOf(ch) > -1) {
          if (start >= 0) {
            convert(s.substring(start, i), buf, enc)
            start = -1
          }
          if (ch != ' ') {
            buf.append(ch)
          } else {
            buf.append('+')
          }
        } else if (start < 0) {
          start = i
        }
        loop(i + 1)
      }
    }
    loop(0)

    if (start >= 0) {
      convert(s.substring(start, s.length), buf, enc)
    }
    buf.toString
  }

  private[this] def convert(
      s: String,
      buf: java.lang.StringBuilder,
      enc: String
  ): Unit = {
    val bytes = s.getBytes(enc)
    @tailrec
    def loop(j: Int): Unit = {
      if (j < bytes.length) {
        buf.append('%')
        buf.append(digits((bytes(j) & 0xf0) >> 4))
        buf.append(digits(bytes(j) & 0xf))
        loop(j + 1)
      }
    }
    loop(0)
  }
}
