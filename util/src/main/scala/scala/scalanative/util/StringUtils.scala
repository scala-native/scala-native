package scala.scalanative.util

import scala.StringContext.InvalidEscapeException
import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuilder

object StringUtils {

  /** Custom implementation of StringContext.processEscapes which also parses
   *  hex values
   *  @param str
   *    input string optionally containing literal escapes and hex values
   *  @return
   *    escaped ByteString
   */
  def processEscapes(str: String): Array[Byte] = {
    val len = str.length
    val b = Array.newBuilder[Byte]

    def append(str: String): ArrayBuilder[Byte] = b ++= str.getBytes

    // replace escapes with given first escape
    def isHex(c: Char): Boolean =
      Character.isDigit(c) ||
        (c >= 'a' && c <= 'f') ||
        (c >= 'A' && c <= 'F')

    // append replacement starting at index `i`, with `next` backslash
    @tailrec def loop(from: Int): ArrayBuilder[Byte] = {

      str.indexOf('\\', from) match {
        case -1  => append(str.substring(from))
        case idx =>
          append(str.substring(from, idx))
          if (idx >= len) throw new InvalidEscapeException(str, from)
          str(idx + 1) match {
            case 'b'                       => b += '\b'; loop(idx + 2)
            case 't'                       => b += '\t'; loop(idx + 2)
            case 'n'                       => b += '\n'; loop(idx + 2)
            case 'f'                       => b += '\f'; loop(idx + 2)
            case 'r'                       => b += '\r'; loop(idx + 2)
            case '"'                       => b += '\"'; loop(idx + 2)
            case '\''                      => b += '\''; loop(idx + 2)
            case '\\'                      => b += '\\'; loop(idx + 2)
            case o if '0' <= o && o <= '7' =>
              throw new InvalidEscapeException(str, idx)
            case 'x' =>
              val hex = str.drop(idx + 2).takeWhile(isHex)
              val hexValue = Integer.parseInt(hex, 16).toChar
              if (hexValue > 255) {
                throw new IllegalArgumentException(
                  s"malformed C string - hex value greater then 0xFF: $str"
                )
              }
              b += hexValue.toByte
              loop(idx + 2 + hex.length)
            case c =>
              b += '\\'
              b += c.toByte
              loop(idx + 2)
          }
      }
    }

    loop(0).result()
  }

}
