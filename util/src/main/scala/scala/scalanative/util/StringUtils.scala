package scala.scalanative.util

import scala.StringContext.InvalidEscapeException
import scala.annotation.tailrec

object StringUtils {

  /**
   * Custom implementation of StringContext.processEscapes which also parses hex values
   * @param str UTF-8 encoded input string optionally containing literal escapes and hex values
   * @return UTF-8 representation of escaped ByteString
   */
  def processEscapes(str: String): String = {
    val len = str.length
    val b   = new java.lang.StringBuilder()

    // replace escapes with given first escape
    def isHex(c: Char): Boolean =
      Character.isDigit(c) ||
        (c >= 'a' && c <= 'f') ||
        (c >= 'A' && c <= 'F')

    // append replacement starting at index `i`, with `next` backslash
    @tailrec def loop(from: Int): java.lang.StringBuilder = {

      str.indexOf('\\', from) match {
        case -1 => b.append(str.substring(from))
        case idx =>
          b.append(str.substring(from, idx))
          if (idx >= len) throw new InvalidEscapeException(str, from)
          str(idx + 1) match {
            case 'b'  => b.append('\b'); loop(idx + 2)
            case 't'  => b.append('\t'); loop(idx + 2)
            case 'n'  => b.append('\n'); loop(idx + 2)
            case 'f'  => b.append('\f'); loop(idx + 2)
            case 'r'  => b.append('\r'); loop(idx + 2)
            case '"'  => b.append('\"'); loop(idx + 2)
            case '\'' => b.append('\''); loop(idx + 2)
            case '\\' => b.append('\\'); loop(idx + 2)
            case o if '0' <= o && o <= '7' =>
              throw new InvalidEscapeException(str, idx)
            case 'x' =>
              val hex      = str.drop(idx + 2).takeWhile(isHex)
              val hexValue = Integer.parseInt(hex, 16).toChar
              if (hexValue > 255) {
                throw new IllegalArgumentException(
                  s"malformed C string - hex value greater then 0xFF: $str")
              }
              b.append(hexValue.toChar)
              loop(idx + 2 + hex.length)
            case c =>
              b.append('\\')
                .append(c)
              loop(idx + 2)
          }
      }
    }

    loop(0).toString
  }

}
