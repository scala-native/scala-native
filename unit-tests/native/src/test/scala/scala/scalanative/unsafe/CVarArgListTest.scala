package scala.scalanative
package unsafe

import org.junit.{Test, BeforeClass}
import org.junit.Assert._
import org.junit.Assume._

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc.{stdio, stdlib, string}
import scalanative.windows
import scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.junit.utils.AssumesHelper._

class CVarArgListTest {
  def vatest(cstr: CString, varargs: Seq[CVarArg], output: String): Unit =
    Zone { implicit z =>
      val buff: Ptr[CChar] = alloc[CChar](1024)
      stdio.vsprintf(buff, cstr, toCVarArgList(varargs))
      val got = fromCString(buff)
      assertTrue(s"$got != $output", got == output)
    }
  @Test def empty(): Unit =
    vatest(c"hello", Seq(), "hello")
  @Test def byteValue0(): Unit =
    vatest(c"%d", Seq(0.toByte), "0")
  @Test def byteValue1(): Unit =
    vatest(c"%d", Seq(1.toByte), "1")
  @Test def byteValueMinus1(): Unit =
    vatest(c"%d", Seq(-1.toByte), "-1")
  @Test def byteValueMin(): Unit =
    vatest(c"%d", Seq(java.lang.Byte.MIN_VALUE), "-128")
  @Test def byteValueMax(): Unit =
    vatest(c"%d", Seq(java.lang.Byte.MAX_VALUE), "127")
  @Test def byteArgs1(): Unit =
    vatest(c"%d", Seq(1.toByte), "1")
  @Test def byteArgs2(): Unit =
    vatest(c"%d %d", Seq(1.toByte, 2.toByte), "1 2")
  @Test def byteArgs3(): Unit =
    vatest(c"%d %d %d", Seq(1.toByte, 2.toByte, 3.toByte), "1 2 3")
  @Test def byteArgs4(): Unit =
    vatest(
      c"%d %d %d %d",
      Seq(1.toByte, 2.toByte, 3.toByte, 4.toByte),
      "1 2 3 4"
    )
  @Test def byteArgs5(): Unit =
    vatest(
      c"%d %d %d %d %d",
      Seq(1.toByte, 2.toByte, 3.toByte, 4.toByte, 5.toByte),
      "1 2 3 4 5"
    )
  @Test def byteArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d",
      Seq(1.toByte, 2.toByte, 3.toByte, 4.toByte, 5.toByte, 6.toByte),
      "1 2 3 4 5 6"
    )
  @Test def byteArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      Seq(1.toByte, 2.toByte, 3.toByte, 4.toByte, 5.toByte, 6.toByte, 7.toByte),
      "1 2 3 4 5 6 7"
    )
  @Test def byteArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      Seq(
        1.toByte,
        2.toByte,
        3.toByte,
        4.toByte,
        5.toByte,
        6.toByte,
        7.toByte,
        8.toByte
      ),
      "1 2 3 4 5 6 7 8"
    )
  @Test def byteArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      Seq(
        1.toByte,
        2.toByte,
        3.toByte,
        4.toByte,
        5.toByte,
        6.toByte,
        7.toByte,
        8.toByte,
        9.toByte
      ),
      "1 2 3 4 5 6 7 8 9"
    )

  @Test def shortValue0(): Unit =
    vatest(c"%d", Seq(0.toShort), "0")
  @Test def shortValue1(): Unit =
    vatest(c"%d", Seq(1.toShort), "1")
  @Test def shortValueMinus1(): Unit =
    vatest(c"%d", Seq(-1.toShort), "-1")
  @Test def shortValueMin(): Unit =
    vatest(c"%d", Seq(java.lang.Short.MIN_VALUE), "-32768")
  @Test def shortValueMax(): Unit =
    vatest(c"%d", Seq(java.lang.Short.MAX_VALUE), "32767")
  @Test def shortArgs1(): Unit =
    vatest(c"%d", Seq(1.toShort), "1")
  @Test def shortArgs2(): Unit =
    vatest(c"%d %d", Seq(1.toShort, 2.toShort), "1 2")
  @Test def shortArgs3(): Unit =
    vatest(c"%d %d %d", Seq(1.toShort, 2.toShort, 3.toShort), "1 2 3")
  @Test def shortArgs4(): Unit =
    vatest(
      c"%d %d %d %d",
      Seq(1.toShort, 2.toShort, 3.toShort, 4.toShort),
      "1 2 3 4"
    )
  @Test def shortArgs5(): Unit =
    vatest(
      c"%d %d %d %d %d",
      Seq(1.toShort, 2.toShort, 3.toShort, 4.toShort, 5.toShort),
      "1 2 3 4 5"
    )
  @Test def shortArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d",
      Seq(1.toShort, 2.toShort, 3.toShort, 4.toShort, 5.toShort, 6.toShort),
      "1 2 3 4 5 6"
    )
  @Test def shortArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      Seq(
        1.toShort,
        2.toShort,
        3.toShort,
        4.toShort,
        5.toShort,
        6.toShort,
        7.toShort
      ),
      "1 2 3 4 5 6 7"
    )
  @Test def shortArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      Seq(
        1.toShort,
        2.toShort,
        3.toShort,
        4.toShort,
        5.toShort,
        6.toShort,
        7.toShort,
        8.toShort
      ),
      "1 2 3 4 5 6 7 8"
    )
  @Test def shortArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      Seq(
        1.toShort,
        2.toShort,
        3.toShort,
        4.toShort,
        5.toShort,
        6.toShort,
        7.toShort,
        8.toShort,
        9.toShort
      ),
      "1 2 3 4 5 6 7 8 9"
    )

  @Test def intValue0(): Unit =
    vatest(c"%d", Seq(0), "0")
  @Test def intValue1(): Unit =
    vatest(c"%d", Seq(1), "1")
  @Test def intValueMinus1(): Unit =
    vatest(c"%d", Seq(-1), "-1")
  @Test def intValueMin(): Unit =
    vatest(c"%d", Seq(java.lang.Integer.MIN_VALUE), "-2147483648")
  @Test def intValueMax(): Unit =
    vatest(c"%d", Seq(java.lang.Integer.MAX_VALUE), "2147483647")
  @Test def intArgs1(): Unit =
    vatest(c"%d", Seq(1), "1")
  @Test def intArgs2(): Unit =
    vatest(c"%d %d", Seq(1, 2), "1 2")
  @Test def intArgs3(): Unit =
    vatest(c"%d %d %d", Seq(1, 2, 3), "1 2 3")
  @Test def intArgs4(): Unit =
    vatest(c"%d %d %d %d", Seq(1, 2, 3, 4), "1 2 3 4")
  @Test def intArgs5(): Unit =
    vatest(c"%d %d %d %d %d", Seq(1, 2, 3, 4, 5), "1 2 3 4 5")
  @Test def intArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", Seq(1, 2, 3, 4, 5, 6), "1 2 3 4 5 6")
  @Test def intArgs7(): Unit =
    vatest(c"%d %d %d %d %d %d %d", Seq(1, 2, 3, 4, 5, 6, 7), "1 2 3 4 5 6 7")
  @Test def intArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      Seq(1, 2, 3, 4, 5, 6, 7, 8),
      "1 2 3 4 5 6 7 8"
    )
  @Test def intArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      Seq(1, 2, 3, 4, 5, 6, 7, 8, 9),
      "1 2 3 4 5 6 7 8 9"
    )

  @Test def longValue0(): Unit =
    vatest(c"%d", Seq(0L), "0")
  @Test def longValue1(): Unit =
    vatest(c"%d", Seq(1L), "1")
  @Test def longValueMinus1(): Unit =
    vatest(c"%d", Seq(-1L), "-1")
  @Test def longValueMin(): Unit = {
    assumeNot32Bit()
    vatest(c"%lld", Seq(java.lang.Long.MIN_VALUE), "-9223372036854775808")
  }
  @Test def longValueMax(): Unit = {
    assumeNot32Bit()
    vatest(c"%lld", Seq(java.lang.Long.MAX_VALUE), "9223372036854775807")
  }
  @Test def longArgs1(): Unit =
    vatest(c"%d", Seq(1L), "1")
  @Test def longArgs2(): Unit =
    vatest(c"%d %d", Seq(1L, 2L), "1 2")
  @Test def longArgs3(): Unit =
    vatest(c"%d %d %d", Seq(1L, 2L, 3L), "1 2 3")
  @Test def longArgs4(): Unit =
    vatest(c"%d %d %d %d", Seq(1L, 2L, 3L, 4L), "1 2 3 4")
  @Test def longArgs5(): Unit =
    vatest(c"%d %d %d %d %d", Seq(1L, 2L, 3L, 4L, 5L), "1 2 3 4 5")
  @Test def longArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", Seq(1L, 2L, 3L, 4L, 5L, 6L), "1 2 3 4 5 6")
  @Test def longArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L),
      "1 2 3 4 5 6 7"
    )
  @Test def longArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L),
      "1 2 3 4 5 6 7 8"
    )
  @Test def longArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L),
      "1 2 3 4 5 6 7 8 9"
    )

  @Test def ubyteValueMin(): Unit =
    vatest(c"%d", Seq(UByte.MinValue), "0")
  @Test def ubyteValueMax(): Unit =
    vatest(c"%d", Seq(UByte.MaxValue), "255")
  @Test def ubyteArgs1(): Unit =
    vatest(c"%d", Seq(1.toUByte), "1")
  @Test def ubyteArgs2(): Unit =
    vatest(c"%d %d", Seq(1.toUByte, 2.toUByte), "1 2")
  @Test def ubyteArgs3(): Unit =
    vatest(c"%d %d %d", Seq(1.toUByte, 2.toUByte, 3.toUByte), "1 2 3")
  @Test def ubyteArgs4(): Unit =
    vatest(
      c"%d %d %d %d",
      Seq(1.toUByte, 2.toUByte, 3.toUByte, 4.toUByte),
      "1 2 3 4"
    )
  @Test def ubyteArgs5(): Unit =
    vatest(
      c"%d %d %d %d %d",
      Seq(1.toUByte, 2.toUByte, 3.toUByte, 4.toUByte, 5.toUByte),
      "1 2 3 4 5"
    )
  @Test def ubyteArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d",
      Seq(1.toUByte, 2.toUByte, 3.toUByte, 4.toUByte, 5.toUByte, 6.toUByte),
      "1 2 3 4 5 6"
    )
  @Test def ubyteArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      Seq(
        1.toUByte,
        2.toUByte,
        3.toUByte,
        4.toUByte,
        5.toUByte,
        6.toUByte,
        7.toUByte
      ),
      "1 2 3 4 5 6 7"
    )
  @Test def ubyteArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      Seq(
        1.toUByte,
        2.toUByte,
        3.toUByte,
        4.toUByte,
        5.toUByte,
        6.toUByte,
        7.toUByte,
        8.toUByte
      ),
      "1 2 3 4 5 6 7 8"
    )
  @Test def ubyteArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      Seq(
        1.toUByte,
        2.toUByte,
        3.toUByte,
        4.toUByte,
        5.toUByte,
        6.toUByte,
        7.toUByte,
        8.toUByte,
        9.toUByte
      ),
      "1 2 3 4 5 6 7 8 9"
    )

  @Test def ushortValueMin(): Unit =
    vatest(c"%d", Seq(UShort.MinValue), "0")
  @Test def ushortValueMax(): Unit =
    vatest(c"%d", Seq(UShort.MaxValue), "65535")
  @Test def ushortArgs1(): Unit =
    vatest(c"%d", Seq(1.toUShort), "1")
  @Test def ushortArgs2(): Unit =
    vatest(c"%d %d", Seq(1.toUShort, 2.toUShort), "1 2")
  @Test def ushortArgs3(): Unit =
    vatest(c"%d %d %d", Seq(1.toUShort, 2.toUShort, 3.toUShort), "1 2 3")
  @Test def ushortArgs4(): Unit =
    vatest(
      c"%d %d %d %d",
      Seq(1.toUShort, 2.toUShort, 3.toUShort, 4.toUShort),
      "1 2 3 4"
    )
  @Test def ushortArgs5(): Unit =
    vatest(
      c"%d %d %d %d %d",
      Seq(1.toUShort, 2.toUShort, 3.toUShort, 4.toUShort, 5.toUShort),
      "1 2 3 4 5"
    )
  @Test def ushortArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d",
      Seq(
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort
      ),
      "1 2 3 4 5 6"
    )
  @Test def ushortArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      Seq(
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort,
        7.toUShort
      ),
      "1 2 3 4 5 6 7"
    )
  @Test def ushortArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      Seq(
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort,
        7.toUShort,
        8.toUShort
      ),
      "1 2 3 4 5 6 7 8"
    )
  @Test def ushortArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      Seq(
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort,
        7.toUShort,
        8.toUShort,
        9.toUShort
      ),
      "1 2 3 4 5 6 7 8 9"
    )

  @Test def uintValueMin(): Unit =
    vatest(c"%u", Seq(UInt.MinValue), "0")
  @Test def uintValueMax(): Unit =
    vatest(c"%u", Seq(UInt.MaxValue), "4294967295")
  @Test def uintArgs1(): Unit =
    vatest(c"%d", Seq(1.toUInt), "1")
  @Test def uintArgs2(): Unit =
    vatest(c"%d %d", Seq(1.toUInt, 2.toUInt), "1 2")
  @Test def uintArgs3(): Unit =
    vatest(c"%d %d %d", Seq(1.toUInt, 2.toUInt, 3.toUInt), "1 2 3")
  @Test def uintArgs4(): Unit =
    vatest(
      c"%d %d %d %d",
      Seq(1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt),
      "1 2 3 4"
    )
  @Test def uintArgs5(): Unit =
    vatest(
      c"%d %d %d %d %d",
      Seq(1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt, 5.toUInt),
      "1 2 3 4 5"
    )
  @Test def uintArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d",
      Seq(1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt, 5.toUInt, 6.toUInt),
      "1 2 3 4 5 6"
    )
  @Test def uintArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      Seq(1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt, 5.toUInt, 6.toUInt, 7.toUInt),
      "1 2 3 4 5 6 7"
    )
  @Test def uintArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      Seq(
        1.toUInt,
        2.toUInt,
        3.toUInt,
        4.toUInt,
        5.toUInt,
        6.toUInt,
        7.toUInt,
        8.toUInt
      ),
      "1 2 3 4 5 6 7 8"
    )
  @Test def uintArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      Seq(
        1.toUInt,
        2.toUInt,
        3.toUInt,
        4.toUInt,
        5.toUInt,
        6.toUInt,
        7.toUInt,
        8.toUInt,
        9.toUInt
      ),
      "1 2 3 4 5 6 7 8 9"
    )

  @Test def ulongValueMin(): Unit = {
    assumeNot32Bit()
    vatest(c"%llu", Seq(ULong.MinValue), "0")
  }
  @Test def ulongValueMax(): Unit = {
    assumeNot32Bit()
    vatest(c"%llu", Seq(ULong.MaxValue), "18446744073709551615")
  }
  @Test def ulongArgs1(): Unit =
    vatest(c"%d", Seq(1.toULong), "1")
  @Test def ulongArgs2(): Unit =
    vatest(c"%d %d", Seq(1.toULong, 2.toULong), "1 2")
  @Test def ulongArgs3(): Unit =
    vatest(c"%d %d %d", Seq(1.toULong, 2.toULong, 3.toULong), "1 2 3")
  @Test def ulongArgs4(): Unit =
    vatest(
      c"%d %d %d %d",
      Seq(1.toULong, 2.toULong, 3.toULong, 4.toULong),
      "1 2 3 4"
    )
  @Test def ulongArgs5(): Unit =
    vatest(
      c"%d %d %d %d %d",
      Seq(1.toULong, 2.toULong, 3.toULong, 4.toULong, 5.toULong),
      "1 2 3 4 5"
    )
  @Test def ulongArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d",
      Seq(1.toULong, 2.toULong, 3.toULong, 4.toULong, 5.toULong, 6.toULong),
      "1 2 3 4 5 6"
    )
  @Test def ulongArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      Seq(
        1.toULong,
        2.toULong,
        3.toULong,
        4.toULong,
        5.toULong,
        6.toULong,
        7.toULong
      ),
      "1 2 3 4 5 6 7"
    )
  @Test def ulongArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      Seq(
        1.toULong,
        2.toULong,
        3.toULong,
        4.toULong,
        5.toULong,
        6.toULong,
        7.toULong,
        8.toULong
      ),
      "1 2 3 4 5 6 7 8"
    )
  @Test def ulongArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      Seq(
        1.toULong,
        2.toULong,
        3.toULong,
        4.toULong,
        5.toULong,
        6.toULong,
        7.toULong,
        8.toULong,
        9.toULong
      ),
      "1 2 3 4 5 6 7 8 9"
    )

  @Test def floatArgs1(): Unit =
    vatest(c"%1.1f", Seq(1.1f), "1.1")
  @Test def floatArgs2(): Unit =
    vatest(c"%1.1f %1.1f", Seq(1.1f, 2.2f), "1.1 2.2")
  @Test def floatArgs3(): Unit =
    vatest(c"%1.1f %1.1f %1.1f", Seq(1.1f, 2.2f, 3.3f), "1.1 2.2 3.3")
  @Test def floatArgs4(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f",
      Seq(1.1f, 2.2f, 3.3f, 4.4f),
      "1.1 2.2 3.3 4.4"
    )
  @Test def floatArgs5(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f),
      "1.1 2.2 3.3 4.4 5.5"
    )
  @Test def floatArgs6(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f),
      "1.1 2.2 3.3 4.4 5.5 6.6"
    )
  @Test def floatArgs7(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f),
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7"
    )
  @Test def floatArgs8(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f),
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8"
    )
  @Test def floatArgs9(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f),
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9"
    )

  @Test def doubleArgs1(): Unit =
    vatest(c"%1.1f", Seq(1.1d), "1.1")
  @Test def doubleArgs2(): Unit =
    vatest(c"%1.1f %1.1f", Seq(1.1d, 2.2d), "1.1 2.2")
  @Test def doubleArgs3(): Unit =
    vatest(c"%1.1f %1.1f %1.1f", Seq(1.1d, 2.2d, 3.3d), "1.1 2.2 3.3")
  @Test def doubleArgs4(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f",
      Seq(1.1d, 2.2d, 3.3d, 4.4d),
      "1.1 2.2 3.3 4.4"
    )
  @Test def doubleArgs5(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d),
      "1.1 2.2 3.3 4.4 5.5"
    )
  @Test def doubleArgs6(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d),
      "1.1 2.2 3.3 4.4 5.5 6.6"
    )
  @Test def doubleArgs7(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d),
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7"
    )
  @Test def doubleArgs8(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d, 8.8d),
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8"
    )
  @Test def doubleArgs9(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d, 8.8d, 9.9d),
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9"
    )

  @Test def mixArgs1(): Unit =
    vatest(c"%d %1.1f", Seq(1, 1.1d), "1 1.1")
  @Test def mixArgs2(): Unit =
    vatest(c"%d %d %1.1f %1.1f", Seq(1, 2, 1.1d, 2.2d), "1 2 1.1 2.2")
  @Test def mixArgs3(): Unit =
    vatest(
      c"%d %d %d %1.1f %1.1f %1.1f",
      Seq(1, 2, 3, 1.1d, 2.2d, 3.3d),
      "1 2 3 1.1 2.2 3.3"
    )
  @Test def mixArgs4(): Unit =
    vatest(
      c"%d %d %d %d %1.1f %1.1f %1.1f %1.1f",
      Seq(1, 2, 3, 4, 1.1d, 2.2d, 3.3d, 4.4d),
      "1 2 3 4 1.1 2.2 3.3 4.4"
    )
  @Test def mixArgs5(): Unit =
    vatest(
      c"%d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1, 2, 3, 4, 5, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d),
      "1 2 3 4 5 1.1 2.2 3.3 4.4 5.5"
    )
  @Test def mixArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1, 2, 3, 4, 5, 6, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d),
      "1 2 3 4 5 6 1.1 2.2 3.3 4.4 5.5 6.6"
    )
  @Test def mixArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1, 2, 3, 4, 5, 6, 7, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d),
      "1 2 3 4 5 6 7 1.1 2.2 3.3 4.4 5.5 6.6 7.7"
    )
  @Test def mixArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1, 2, 3, 4, 5, 6, 7, 8, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d,
        8.8d),
      "1 2 3 4 5 6 7 8 1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8"
    )
  @Test def mixArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d,
        8.8d, 9.9d),
      "1 2 3 4 5 6 7 8 9 1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9"
    )
}
