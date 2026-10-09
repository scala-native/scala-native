package java.net

// Ported from Harmony

import java.io.UnsupportedEncodingException
import java.nio.charset.{Charset, UnsupportedCharsetException}
import java.{lang => jl}

import scala.annotation.tailrec

object URLEncoder {
  private val digits = "0123456789ABCDEF".toCharArray

  def encode(s: String, enc: String): String = {
    if (s == null || enc == null) {
      throw new NullPointerException
    }
    val charset =
      try Charset.forName(enc)
      catch {
        case _: UnsupportedCharsetException =>
          throw new UnsupportedEncodingException(enc)
      }
    encode(s, charset)
  }

  def encode(s: String, charset: Charset): String = {
    if (s == null || charset == null)
      throw new NullPointerException
    val buf = new jl.StringBuilder(s.length + 16)
    var start = -1
    @tailrec
    def loop(i: Int): Unit = {
      if (i < s.length) {
        val ch = s.charAt(i)
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || " .-*_"
              .indexOf(ch) > -1) {
          if (start >= 0) {
            convert(s.substring(start, i), buf, charset)
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
      convert(s.substring(start, s.length), buf, charset)
    }
    buf.toString
  }

  private def convert(
      s: String,
      buf: jl.StringBuilder,
      charset: Charset
  ): Unit = {
    val bytes = s.getBytes(charset)
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
