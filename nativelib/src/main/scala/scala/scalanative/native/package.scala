package scala.scalanative

import scala.language.experimental.macros
import java.nio.charset.Charset
import scalanative.runtime.undefined

package object native {

  /** Int on 32-bit architectures and Long on 64-bit ones. */
  type Word = Long

  /** UInt on 32-bit architectures and ULong on 64-bit ones. */
  type UWord = ULong

  /** The C 'char' type. */
  type CChar = Byte

  /** The C 'unsigned char' type. */
  type CUnsignedChar = UByte

  /** The C 'unsigned short' type. */
  type CUnsignedShort = UShort

  /** The C 'unsigned int' type. */
  type CUnsignedInt = UInt

  /** The C 'unsigned long' type. */
  type CUnsignedLong = UWord

  /** The C 'unsigned long int' type. */
  type CUnsignedLongInt = ULong

  /** The C 'unsigned long long' type. */
  type CUnsignedLongLong = ULong

  /** The C 'signed char' type. */
  type CSignedChar = Byte

  /** The C 'short' type. */
  type CShort = Short

  /** The C 'int' type. */
  type CInt = Int

  /** The C 'long' type. */
  type CLong = Word

  /** The C 'long int' type. */
  type CLongInt = Long

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
  type CSize = Word

  /** The C/C++ 'ssize_t' type. */
  type CSSize = Word

  /** The C/C++ 'ptrdiff_t' type. */
  type CPtrDiff = Long

  /** C-style string with trailing 0. */
  type CString = Ptr[CChar]

  /** The C 'sizeof' operator. */
  def sizeof[T](implicit tag: Tag[T]): CSize = undefined

  /** Heap allocate and zero-initialize a value
   *  using current implicit allocator.
   */
  def alloc[T](implicit tag: Tag[T], z: Zone): Ptr[T] =
    macro MacroImpl.alloc1[T]

  /** Heap allocate and zero-initialize n values
   *  using current implicit allocator.
   */
  def alloc[T](n: CSize)(implicit tag: Tag[T], z: Zone): Ptr[T] =
    macro MacroImpl.allocN[T]

  /** Stack allocate a value of given type.
   *
   *  Note: unlike alloc, the memory is not zero-initialized.
   */
  def stackalloc[T](implicit tag: Tag[T]): Ptr[T] = undefined

  /** Stack allocate n values of given type.
   *
   *  Note: unlike alloc, the memory is not zero-initialized.
   */
  def stackalloc[T](n: CSize)(implicit tag: Tag[T]): Ptr[T] = undefined

  /** Used as right hand side of external method and field declarations. */
  def extern: Nothing = undefined

  /** C-style string literal. */
  implicit class CQuote(val ctx: StringContext) {
    def c(): CString = undefined
  }

  /** C-style unchecked cast. */
  implicit class CCast[From](val from: From) {
    def cast[To](implicit fromtag: Tag[From], totag: Tag[To]): To =
      undefined
  }

  /** Scala Native extensions to the standard Byte. */
  implicit class NativeRichByte(val value: Byte) extends AnyVal {
    @inline def toUByte: UByte   = new UByte(value)
    @inline def toUShort: UShort = toUByte.toUShort
    @inline def toUInt: UInt     = toUByte.toUInt
    @inline def toULong: ULong   = toUByte.toULong
  }

  /** Scala Native extensions to the standard Short. */
  implicit class NativeRichShort(val value: Short) extends AnyVal {
    @inline def toUByte: UByte   = toUShort.toUByte
    @inline def toUShort: UShort = new UShort(value)
    @inline def toUInt: UInt     = toUShort.toUInt
    @inline def toULong: ULong   = toUShort.toULong
  }

  /** Scala Native extensions to the standard Int. */
  implicit class NativeRichInt(val value: Int) extends AnyVal {
    @inline def toUByte: UByte   = toUInt.toUByte
    @inline def toUShort: UShort = toUInt.toUShort
    @inline def toUInt: UInt     = new UInt(value)
    @inline def toULong: ULong   = toUInt.toULong
  }

  /** Scala Native extensions to the standard Long. */
  implicit class NativeRichLong(val value: Long) extends AnyVal {
    @inline def toUByte: UByte   = toULong.toUByte
    @inline def toUShort: UShort = toULong.toUShort
    @inline def toUInt: UInt     = toULong.toUInt
    @inline def toULong: ULong   = new ULong(value)
  }

  /** Convert a CString to a String using given charset. */
  def fromCString(cstr: CString,
                  charset: Charset = Charset.defaultCharset()): String = {
    val len   = string.strlen(cstr).toInt
    val bytes = new Array[Byte](len)

    var c = 0
    while (c < len) {
      bytes(c) = !(cstr + c)
      c += 1
    }

    new String(bytes, charset)
  }

  /** Convert a java.lang.String to a CString using default charset and
   *  given allocator.
   */
  def toCString(str: String)(implicit z: Zone): CString =
    toCString(str, Charset.defaultCharset())(z)

  /** Convert a java.lang.String to a CString using given charset and allocator.
   */
  def toCString(str: String, charset: Charset)(implicit z: Zone): CString = {
    val bytes = str.getBytes(charset)
    val cstr  = z.alloc(bytes.length + 1)

    var c = 0
    while (c < bytes.length) {
      !(cstr + c) = bytes(c)
      c += 1
    }

    !(cstr + c) = 0.toByte

    cstr
  }

  private object MacroImpl {
    import scala.reflect.macros.blackbox.Context

    def alloc1[T: c.WeakTypeTag](c: Context)(tag: c.Tree, z: c.Tree): c.Tree = {
      import c.universe._
      val T         = weakTypeOf[T]
      val size, ptr = TermName(c.freshName())
      q"""{
        val $size = _root_.scala.scalanative.native.sizeof[$T]($tag)
        val $ptr = $z.alloc($size)
        _root_.scala.scalanative.native.string.memset($ptr, 0, $size)
        $ptr.cast[Ptr[$T]]
      }"""
    }

    def allocN[T: c.WeakTypeTag](c: Context)(n: c.Tree)(tag: c.Tree,
                                                        z: c.Tree): c.Tree = {
      import c.universe._
      val T         = weakTypeOf[T]
      val size, ptr = TermName(c.freshName())
      q"""{
        val $size = _root_.scala.scalanative.native.sizeof[$T]($tag) * $n
        val $ptr = $z.alloc($size)
        _root_.scala.scalanative.native.string.memset($ptr, 0, $size)
        $ptr.cast[Ptr[$T]]
      }"""
    }
  }
}
