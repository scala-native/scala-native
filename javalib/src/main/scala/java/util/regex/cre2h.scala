package java.util.regex

import scala.scalanative._
import native._
import native.stdlib._

import java.nio.charset.Charset

object cre2h {
  implicit class RE2StringOps(val ptr: Ptr[cre2.string_t]) extends AnyVal {
    def data: CString            = !ptr._1
    def length: CInt             = !ptr._2
    def data_=(v: CString): Unit = !ptr._1 = v
    def length_=(v: CInt): Unit  = !ptr._2 = v
  }

  def fromRE2String(restr: Ptr[cre2.string_t]): String = {
    val charset = Charset.defaultCharset()
    val bytes   = new Array[Byte](restr.length)
    val length  = restr.length
    val data    = restr.data
    var i       = 0
    while (i < length) {
      bytes(i) = !(data + i)
      i += 1
    }

    new String(bytes, charset)
  }

  def toRE2String(str: String, restr: Ptr[cre2.string_t])(
      implicit z: Zone): Unit = {
    restr.data = toCString(str)
    restr.length = str.length
  }

  final val ENCODING_UNKNOWN = 0
  final val ENCODING_UTF_8   = 1
  final val ENCODING_LATIN1  = 2

  final val UNANCHORED   = 1
  final val ANCHOR_START = 2
  final val ANCHOR_BOTH  = 3

  final val ERROR_NO_ERROR           = 0
  final val ERROR_INTERNAL           = 1
  final val ERROR_BAD_ESCAPE         = 2
  final val ERROR_BAD_CHAR_CLASS     = 3
  final val ERROR_BAD_CHAR_RANGE     = 4
  final val ERROR_MISSING_BRACKET    = 5
  final val ERROR_MISSING_PAREN      = 6
  final val ERROR_TRAILING_BACKSLASH = 7
  final val ERROR_REPEAT_ARGUMENT    = 8
  final val ERROR_REPEAT_SIZE        = 9
  final val ERROR_REPEAT_OP          = 10
  final val ERROR_BAD_PERL_OP        = 11
  final val ERROR_BAD_UTF8           = 12
  final val ERROR_BAD_NAMED_CAPTURE  = 13
  final val ERROR_PATTERN_TOO_LARGE  = 14
}
