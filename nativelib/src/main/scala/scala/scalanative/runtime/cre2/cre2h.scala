package java.util.regex

import scala.scalanative._
import native._
import native.stdlib._
import runtime.GC

import java.nio.charset.Charset

private[regex] object cre2h {
  type Regex       = CStruct0
  type Options     = CStruct0
  type _StringPart = CStruct2[CString, CInt]
  object StringPart {
    @inline def stackalloc: StringPart =
      new StringPart(native.stackalloc[_StringPart])

    def array(n: CInt): StringPart =
      new StringPart(GC.malloc(sizeof[_StringPart] * n).cast[Ptr[_StringPart]])

    def apply(str: String): StringPart = {
      val ptr =
        new StringPart(GC.malloc(sizeof[_StringPart]).cast[Ptr[_StringPart]])

      ptr.data = toCString(str)
      ptr.lenght = str.length
      ptr
    }
  }
  class StringPart(val ptr: Ptr[_StringPart]) extends AnyVal {

    def data: CString = !ptr._1
    def lenght: CInt  = !ptr._2

    def data_=(v: CString): Unit = !ptr._1 = v
    def lenght_=(v: CInt): Unit  = !ptr._2 = v

    def apply(i: Int): StringPart = new StringPart(ptr + i)

    override def toString: String = {
      val charset = Charset.defaultCharset()
      val bytes   = new Array[Byte](lenght)
      val d       = data
      var c       = 0
      while (c < lenght) {
        bytes(c) = !(d + c)
        c += 1
      }

      new String(bytes, charset)
    }
  }

  class Encoding(val value: CInt) extends AnyVal
  object Encoding {
    final val Unknown = new Encoding(0)
    final val Utf8    = new Encoding(1)
    final val Latin1  = new Encoding(2)
  }

  class Anchor(val value: CInt) extends AnyVal
  object Anchor {
    final val None  = new Anchor(1)
    final val Start = new Anchor(2)
    final val Both  = new Anchor(3)
  }

  class ErrorCode(val value: CInt) extends AnyVal
  object ErrorCode {
    final val NoError           = new ErrorCode(0)
    final val Internal          = new ErrorCode(1)
    final val BadEscape         = new ErrorCode(2)
    final val BadCharClass      = new ErrorCode(3)
    final val BadCharRange      = new ErrorCode(4)
    final val MissingBracket    = new ErrorCode(5)
    final val MissingParent     = new ErrorCode(6)
    final val TrailingBackslash = new ErrorCode(7)
    final val RepeatArgument    = new ErrorCode(8)
    final val RepeatSize        = new ErrorCode(9)
    final val RepeatOp          = new ErrorCode(10)
    final val BadPerlOp         = new ErrorCode(11)
    final val BadUtf8           = new ErrorCode(12)
    final val BadNamedCapture   = new ErrorCode(13)
    final val PatternTooLarge   = new ErrorCode(14)
  }
}
