package java.net

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

object URIEncoderDecoder {

  val digits: String = "0123456789ABCDEF"

  val encoding: String = "UTF8"

  def validate(s: String, legal: String): Unit = {
    var i: Int = 0
    while (i < s.length) {
      var continue = false
      val ch: Char = s.charAt(i)
      if (ch == '%') {
        continue = true
        do {
          if (i + 2 >= s.length) {
            throw new URISyntaxException(s, "Incomplete % sequence", i)
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
      } else if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
                   (ch >= '0' && ch <= '9') ||
                   legal.indexOf(ch) > -1 ||
                   (ch > 127 && !java.lang.Character.isSpaceChar(ch) && !java.lang.Character
                     .isISOControl(ch)))) {
        throw new URISyntaxException(s, "Illegal character", i)
      }
      if (!continue) i += 1
    }
  }

  def validateSimple(s: String, legal: String): Unit = {
    var i: Int = 0
    while (i < s.length) {
      val ch: Char = s.charAt(i)
      if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
            (ch >= '0' && ch <= '9') ||
            legal.indexOf(ch) > -1)) {
        throw new URISyntaxException(s, "Illegal character", i)
      }
      i += 1
    }
  }

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
      result.append(c)
      i += 1
    }
    result.toString
  }

}
