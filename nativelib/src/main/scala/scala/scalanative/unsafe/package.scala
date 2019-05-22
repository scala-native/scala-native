package scala.scalanative

import java.nio.charset.Charset
import scala.language.experimental.macros
import scalanative.annotation.alwaysinline
import scalanative.unsigned._
import scalanative.runtime.{libc, intrinsic, fromRawPtr}
import scalanative.runtime.Intrinsics.{castIntToRawPtr, castLongToRawPtr}

package object unsafe {

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

  /** Materialize tag for given type. */
  @alwaysinline def tagof[T](implicit tag: Tag[T]): Tag[T] = tag

  /** The C 'sizeof' operator. */
  @alwaysinline def sizeof[T](implicit tag: Tag[T]): CSize = tag.size

  /** C-style alignment operator. */
  @alwaysinline def alignmentof[T](implicit tag: Tag[T]): CSize = tag.alignment

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
  def stackalloc[T](implicit tag: Tag[T]): Ptr[T] =
    macro MacroImpl.stackalloc1[T]

  /** Stack allocate n values of given type.
   *
   *  Note: unlike alloc, the memory is not zero-initialized.
   */
  def stackalloc[T](n: CSize)(implicit tag: Tag[T]): Ptr[T] =
    macro MacroImpl.stackallocN[T]

  /** Used as right hand side of external method and field declarations. */
  def extern: Nothing = intrinsic

  /** C-style string literal. */
  implicit class CQuote(val ctx: StringContext) {
    def c(): CString = intrinsic
  }

  /** Scala Native unsafe extensions to the standard Int. */
  implicit class UnsafeRichInt(val value: Int) extends AnyVal {
    @inline def toPtr[T]: Ptr[T] = fromRawPtr[T](castIntToRawPtr(value))
  }

  /** Scala Native unsafe extensions to the standard Long. */
  implicit class UnsafeRichLong(val value: Long) extends AnyVal {
    @inline def toPtr[T]: Ptr[T] = fromRawPtr[T](castLongToRawPtr(value))
  }

  /** Convert a CString to a String using given charset. */
  def fromCString(cstr: CString,
                  charset: Charset = Charset.defaultCharset()): String = {
    val len   = libc.strlen(cstr).toInt
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

  /** Create an empty CVarArgList. */
  def toCVarArgList()(implicit z: Zone): CVarArgList =
    toCVarArgList(Seq.empty)

  /** Convert given CVarArgs into a c CVarArgList. */
  def toCVarArgList(vararg: CVarArg, varargs: CVarArg*)(
      implicit z: Zone): CVarArgList =
    toCVarArgList(vararg +: varargs)

  /** Convert a sequence of CVarArg into a c CVarArgList. */
  def toCVarArgList(varargs: Seq[CVarArg])(implicit z: Zone): CVarArgList =
    CVarArgList.fromSeq(varargs)

  private object MacroImpl {
    import scala.reflect.macros.blackbox.Context

    def alloc1[T: c.WeakTypeTag](c: Context)(tag: c.Tree, z: c.Tree): c.Tree = {
      import c.universe._
      val T = weakTypeOf[T]

      val size, ptr, rawptr = TermName(c.freshName())

      val runtime = q"_root_.scala.scalanative.runtime"

      q"""{
        val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag)
        val $ptr    = $z.alloc($size)
        val $rawptr = $runtime.toRawPtr($ptr)
        $runtime.libc.memset($rawptr, 0, $size)
        $ptr.asInstanceOf[Ptr[$T]]
      }"""
    }

    def allocN[T: c.WeakTypeTag](c: Context)(n: c.Tree)(tag: c.Tree,
                                                        z: c.Tree): c.Tree = {
      import c.universe._

      val T = weakTypeOf[T]

      val size, ptr, rawptr = TermName(c.freshName())

      val runtime = q"_root_.scala.scalanative.runtime"

      q"""{
        val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag) * $n
        val $ptr    = $z.alloc($size)
        val $rawptr = $runtime.toRawPtr($ptr)
        $runtime.libc.memset($rawptr, 0, $size)
        $ptr.asInstanceOf[Ptr[$T]]
      }"""
    }

    def stackalloc1[T: c.WeakTypeTag](c: Context)(tag: c.Tree): c.Tree = {
      import c.universe._

      val T = weakTypeOf[T]

      val size, rawptr = TermName(c.freshName())

      val runtime = q"_root_.scala.scalanative.runtime"

      q"""{
        val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag)
        val $rawptr = $runtime.Intrinsics.stackalloc($size)
        $runtime.libc.memset($rawptr, 0, $size)
        $runtime.fromRawPtr[$T]($rawptr)
      }"""
    }

    def stackallocN[T: c.WeakTypeTag](c: Context)(n: c.Tree)(
        tag: c.Tree): c.Tree = {
      import c.universe._

      val T = weakTypeOf[T]

      val size, rawptr = TermName(c.freshName())

      val runtime = q"_root_.scala.scalanative.runtime"

      q"""{
        val $size   = _root_.scala.scalanative.unsafe.sizeof[$T]($tag) * $n
        val $rawptr = $runtime.Intrinsics.stackalloc($size)
        $runtime.libc.memset($rawptr, 0, $size)
        $runtime.fromRawPtr[$T]($rawptr)
      }"""
    }
  }
}
