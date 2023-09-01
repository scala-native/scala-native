package scala.scalanative

import java.nio.charset.{Charset, StandardCharsets}
import scalanative.annotation.alwaysinline
import scalanative.runtime.{Platform, fromRawPtr, intrinsic, libc}
import scalanative.runtime.Intrinsics.{
  castIntToRawPtr,
  castIntToRawSize,
  castLongToRawPtr,
  castLongToRawSize
}
import scalanative.unsigned._
import scala.scalanative.meta.LinktimeInfo

package object unsafe extends unsafe.UnsafePackageCompat {
  val is32BitPlatform = LinktimeInfo.is32BitPlatform

  /** Int on 32-bit architectures and Long on 64-bit ones. */
  @deprecated("Word type is deprecated, use Size instead", since = "0.5.0")
  type Word = Size

  /** UInt on 32-bit architectures and ULong on 64-bit ones. */
  @deprecated("UWord type is deprecated, use USize instead", since = "0.5.0")
  type UWord = USize

  /** The C 'char' type. */
  type CChar = Byte

  /** The C 'unsigned char' type. */
  type CUnsignedChar = UByte

  /** The C 'unsigned short' type. */
  type CUnsignedShort = UShort

  /** The C 'unsigned int' type. */
  type CUnsignedInt = UInt

  /** The C 'unsigned long' type. */
  type CUnsignedLong = USize

  /** The C 'unsigned long int' type. */
  type CUnsignedLongInt = USize

  /** The C 'unsigned long long' type. */
  type CUnsignedLongLong = ULong

  /** The C 'signed char' type. */
  type CSignedChar = Byte

  /** The C 'short' type. */
  type CShort = Short

  /** The C 'int' type. */
  type CInt = Int

  /** The C 'long' type. */
  type CLong = Size

  /** The C 'long int' type. */
  type CLongInt = Size

  /** The C 'long long' type. */
  type CLongLong = Long

  /** The C 'float' type. */
  type CFloat = Float

  /** The C 'double' type. */
  type CDouble = Double

  /** The C++ 'wchar_t' type. */
  type CWideChar = UInt

  /** The C++ 'char16_t' type. */
  type CChar16 = UShort

  /** The C++ 'char32_t' type. */
  type CChar32 = UInt

  /** The C '_Bool' and C++ 'bool' type. */
  type CBool = Boolean

  /** The C/C++ 'size_t' type. */
  type CSize = USize

  /** The C/C++ 'ssize_t' type. */
  type CSSize = Size

  /** The C/C++ 'ptrdiff_t' type. */
  type CPtrDiff = Size

  /** C-style string with trailing 0. */
  type CString = Ptr[CChar]

  /* C-style wide string with trail 0. */
  type CWideString = Ptr[CWideChar]

  /** Materialize tag for given type. */
  @alwaysinline def tagof[T](implicit tag: Tag[T]): Tag[T] = tag

  /** An annotation that is used to mark objects that contain externally-defined
   *  members
   */
  final class extern extends scala.annotation.StaticAnnotation

  /** An annotation that is used to mark methods that contain externally-defined
   *  and potentially blocking methods
   */
  final class blocking extends scala.annotation.StaticAnnotation

  /** Used as right hand side of external method and field declarations. */
  def extern: Nothing = intrinsic

  /** Used as right hand side of values resolved at link-time. */
  private[scalanative] def resolved: Nothing = intrinsic

  /** C-style string literal. */
  implicit class CQuote(val ctx: StringContext) {
    def c(): CString = intrinsic
  }

  // UnsafeRich* have lower priority then extension methods defined in scala-3 UnsafePackageCompat
  /** Scala Native unsafe extensions to the standard Byte. */
  implicit class UnsafeRichByte(val value: Byte) extends AnyVal {
    @inline def toSize: Size = Size.valueOf(castIntToRawSize(value.toInt))
  }

  /** Scala Native unsafe extensions to the standard Short. */
  implicit class UnsafeRichShort(val value: Short) extends AnyVal {
    @inline def toSize: Size = Size.valueOf(castIntToRawSize(value.toInt))
  }

  /** Scala Native unsafe extensions to the standard Int. */
  implicit class UnsafeRichInt(val value: Int) extends AnyVal {
    @inline def toPtr[T]: Ptr[T] = fromRawPtr[T](castIntToRawPtr(value))
    @inline def toSize: Size = Size.valueOf(castIntToRawSize(value))
  }

  /** Scala Native unsafe extensions to the standard Long. */
  implicit class UnsafeRichLong(val value: Long) extends AnyVal {
    @inline def toPtr[T]: Ptr[T] = fromRawPtr[T](castLongToRawPtr(value))
    @inline def toSize: Size = Size.valueOf(castLongToRawSize(value))
  }

  /** Scala Native unsafe extensions to Arrays */
  implicit class UnsafeRichArray[T](val value: Array[T]) extends AnyVal {
    @inline def at(i: Int): Ptr[T] = value.asInstanceOf[runtime.Array[T]].at(i)
    @inline def atUnsafe(i: Int): Ptr[T] =
      value.asInstanceOf[runtime.Array[T]].atUnsafe(i)
  }

  /** Convert a CString to a String using given charset. */
  def fromCString(
      cstr: CString,
      charset: Charset = Charset.defaultCharset()
  ): String = {
    if (cstr == null) {
      null
    } else {
      val len = libc.strlen(cstr)
      val intLen = len.toInt
      if (intLen > 0) {
        val bytes = new Array[Byte](intLen)

        libc.memcpy(bytes.at(0), cstr, len)

        new String(bytes, charset)
      } else ""
    }
  }

  /** Convert a java.lang.String to a CString using default charset and given
   *  allocator.
   */
  def toCString(str: String)(implicit z: Zone): CString =
    toCString(str, Charset.defaultCharset())(z)

  /** Convert a java.lang.String to a CString using given charset and allocator.
   */
  def toCString(str: String, charset: Charset)(implicit z: Zone): CString = {
    if (str == null) {
      null
    } else {
      val bytes = str.getBytes(charset)
      if (bytes.length > 0) {
        val len = bytes.length.toUSize
        val cstr = z.alloc(len + 1.toUInt)

        libc.memcpy(cstr, bytes.at(0), len)

        !(cstr + len) = 0.toByte

        cstr
      } else c""
    }
  }

  // wchar_t size may vary across platforms from 2 to 4 bytes.
  private final val WideCharSize = Platform.SizeOfWChar.toInt

  /** Convert a java.lang.String to a CWideString using given charset and
   *  allocator.
   */
  @alwaysinline
  def toCWideString(str: String, charset: Charset = StandardCharsets.UTF_16LE)(
      implicit z: Zone
  ): Ptr[CWideString] = {
    toCWideStringImpl(str, charset, WideCharSize)
  }

  /** Convert a java.lang.String to a CWideString using given UTF-16 LE charset.
   */
  @alwaysinline
  def toCWideStringUTF16LE(str: String)(implicit z: Zone): Ptr[CChar16] = {
    toCWideStringImpl(str, StandardCharsets.UTF_16LE, 2)
      .asInstanceOf[Ptr[CChar16]]
  }

  private def toCWideStringImpl(str: String, charset: Charset, charSize: CInt)(
      implicit z: Zone
  ) = {
    if (str == null) {
      null
    } else {
      val bytes = str.getBytes(charset)
      val cstr = z.alloc((bytes.length + charSize).toUSize)

      var c = 0
      while (c < bytes.length) {
        !(cstr + c) = bytes(c)
        c += 1
      }

      // Set null termination bytes
      val cstrEnd = cstr + c
      c = 0
      while (c < charSize) {
        !(cstrEnd + c) = 0.toByte
        c += 1
      }
      cstr.asInstanceOf[Ptr[CWideString]]
    }
  }

  /** Convert a CWideString to a String using given charset, assumes platform
   *  default wchar_t size
   */
  @alwaysinline
  def fromCWideString(cwstr: CWideString, charset: Charset): String =
    fromCWideStringImpl(
      bytes = cwstr.asInstanceOf[Ptr[Byte]],
      charset = charset,
      charSize = WideCharSize
    )

  /** Convert a CWideString based on Ptr[CChar16] to a String using given
   *  charset
   */
  @alwaysinline
  def fromCWideString(cwstr: Ptr[CChar16], charset: Charset)(implicit
      d: DummyImplicit
  ): String = {
    fromCWideStringImpl(
      bytes = cwstr.asInstanceOf[Ptr[Byte]],
      charset = charset,
      charSize = 2
    )
  }

  private def fromCWideStringImpl(
      bytes: Ptr[Byte],
      charset: Charset,
      charSize: Int
  ): String = {
    if (bytes == null) {
      null
    } else {
      val cwstr = bytes.asInstanceOf[CWideString]
      val len = charSize * libc.wcslen(cwstr).toInt
      val buf = new Array[Byte](len)

      var c = 0
      while (c < len) {
        buf(c) = !(bytes + c)
        c += 1
      }

      new String(buf, charset)
    }
  }

  /** Create an empty CVarArgList. */
  def toCVarArgList()(implicit z: Zone): CVarArgList =
    toCVarArgList(Seq.empty)

  /** Convert given CVarArgs into a c CVarArgList. */
  def toCVarArgList(vararg: CVarArg, varargs: CVarArg*)(implicit
      z: Zone
  ): CVarArgList =
    toCVarArgList(vararg +: varargs)

  /** Convert a sequence of CVarArg into a c CVarArgList. */
  def toCVarArgList(varargs: Seq[CVarArg])(implicit z: Zone): CVarArgList =
    CVarArgList.fromSeq(varargs)

}
