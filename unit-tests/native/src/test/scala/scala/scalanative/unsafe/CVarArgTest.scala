package scala.scalanative
package unsafe

import org.junit.{Test, BeforeClass}
import org.junit.Assert._
import org.junit.Assume._

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc.{stdio, stdlib, string}

import scala.scalanative.junit.utils.AssumesHelper._

class CVarArgTest {
  def vatest(cstr: CString, output: String)(
      generator: (CString, Ptr[CChar]) => Unit
  ): Unit = {
    val buff: Ptr[CChar] = stackalloc[CChar](1024.toUSize)
    generator(buff, cstr)
    val got = fromCString(buff)
    assertEquals(got, output)
  }

  @Test def empty(): Unit =
    vatest(c"hello", "hello")(stdio.sprintf(_, _))
  @Test def byteValue0(): Unit =
    vatest(c"%d", "0")(stdio.sprintf(_, _, 0.toByte))
  @Test def byteValue1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1.toByte))
  @Test def byteValueMinus1(): Unit =
    vatest(c"%d", "-1")(stdio.sprintf(_, _, -1.toByte))
  @Test def byteValueMin(): Unit =
    vatest(c"%d", "-128")(stdio.sprintf(_, _, java.lang.Byte.MIN_VALUE))
  @Test def byteValueMax(): Unit =
    vatest(c"%d", "127")(stdio.sprintf(_, _, java.lang.Byte.MAX_VALUE))
  @Test def byteArgs1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1.toByte))
  @Test def byteArgs2(): Unit =
    vatest(c"%d %d", "1 2")(stdio.sprintf(_, _, 1.toByte, 2.toByte))
  @Test def byteArgs3(): Unit =
    vatest(c"%d %d %d", "1 2 3")(
      stdio.sprintf(_, _, 1.toByte, 2.toByte, 3.toByte)
    )
  @Test def byteArgs4(): Unit =
    vatest(c"%d %d %d %d", "1 2 3 4")(
      stdio.sprintf(_, _, 1.toByte, 2.toByte, 3.toByte, 4.toByte)
    )
  @Test def byteArgs5(): Unit =
    vatest(c"%d %d %d %d %d", "1 2 3 4 5")(
      stdio.sprintf(_, _, 1.toByte, 2.toByte, 3.toByte, 4.toByte, 5.toByte)
    )
  @Test def byteArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", "1 2 3 4 5 6")(
      stdio.sprintf(
        _,
        _,
        1.toByte,
        2.toByte,
        3.toByte,
        4.toByte,
        5.toByte,
        6.toByte
      )
    )
  @Test def byteArgs7(): Unit =
    vatest(c"%d %d %d %d %d %d %d", "1 2 3 4 5 6 7")(
      stdio.sprintf(
        _,
        _,
        1.toByte,
        2.toByte,
        3.toByte,
        4.toByte,
        5.toByte,
        6.toByte,
        7.toByte
      )
    )
  @Test def byteArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8"
    )(
      stdio.sprintf(
        _,
        _,
        1.toByte,
        2.toByte,
        3.toByte,
        4.toByte,
        5.toByte,
        6.toByte,
        7.toByte,
        8.toByte
      )
    )
  @Test def byteArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8 9"
    )(
      stdio.sprintf(
        _,
        _,
        1.toByte,
        2.toByte,
        3.toByte,
        4.toByte,
        5.toByte,
        6.toByte,
        7.toByte,
        8.toByte,
        9.toByte
      )
    )

  @Test def shortValue0(): Unit =
    vatest(c"%d", "0")(stdio.sprintf(_, _, 0.toShort))
  @Test def shortValue1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1.toShort))
  @Test def shortValueMinus1(): Unit =
    vatest(c"%d", "-1")(stdio.sprintf(_, _, -1.toShort))
  @Test def shortValueMin(): Unit =
    vatest(c"%d", "-32768")(stdio.sprintf(_, _, java.lang.Short.MIN_VALUE))
  @Test def shortValueMax(): Unit =
    vatest(c"%d", "32767")(stdio.sprintf(_, _, java.lang.Short.MAX_VALUE))
  @Test def shortArgs1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1.toShort))
  @Test def shortArgs2(): Unit =
    vatest(c"%d %d", "1 2")(stdio.sprintf(_, _, 1.toShort, 2.toShort))
  @Test def shortArgs3(): Unit =
    vatest(c"%d %d %d", "1 2 3")(
      stdio.sprintf(_, _, 1.toShort, 2.toShort, 3.toShort)
    )
  @Test def shortArgs4(): Unit =
    vatest(c"%d %d %d %d", "1 2 3 4")(
      stdio.sprintf(_, _, 1.toShort, 2.toShort, 3.toShort, 4.toShort)
    )
  @Test def shortArgs5(): Unit =
    vatest(c"%d %d %d %d %d", "1 2 3 4 5")(
      stdio.sprintf(_, _, 1.toShort, 2.toShort, 3.toShort, 4.toShort, 5.toShort)
    )
  @Test def shortArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", "1 2 3 4 5 6")(
      stdio.sprintf(
        _,
        _,
        1.toShort,
        2.toShort,
        3.toShort,
        4.toShort,
        5.toShort,
        6.toShort
      )
    )
  @Test def shortArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7"
    )(
      stdio.sprintf(
        _,
        _,
        1.toShort,
        2.toShort,
        3.toShort,
        4.toShort,
        5.toShort,
        6.toShort,
        7.toShort
      )
    )
  @Test def shortArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8"
    )(
      stdio.sprintf(
        _,
        _,
        1.toShort,
        2.toShort,
        3.toShort,
        4.toShort,
        5.toShort,
        6.toShort,
        7.toShort,
        8.toShort
      )
    )
  @Test def shortArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8 9"
    )(
      stdio.sprintf(
        _,
        _,
        1.toShort,
        2.toShort,
        3.toShort,
        4.toShort,
        5.toShort,
        6.toShort,
        7.toShort,
        8.toShort,
        9.toShort
      )
    )

  @Test def intValue0(): Unit =
    vatest(c"%d", "0")(stdio.sprintf(_, _, 0))
  @Test def intValue1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1))
  @Test def intValueMinus1(): Unit =
    vatest(c"%d", "-1")(stdio.sprintf(_, _, -1))
  @Test def intValueMin(): Unit =
    vatest(c"%d", "-2147483648")(
      stdio.sprintf(_, _, java.lang.Integer.MIN_VALUE)
    )
  @Test def intValueMax(): Unit =
    vatest(c"%d", "2147483647")(
      stdio.sprintf(_, _, java.lang.Integer.MAX_VALUE)
    )
  @Test def intArgs1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1))
  @Test def intArgs2(): Unit =
    vatest(c"%d %d", "1 2")(stdio.sprintf(_, _, 1, 2))
  @Test def intArgs3(): Unit =
    vatest(c"%d %d %d", "1 2 3")(stdio.sprintf(_, _, 1, 2, 3))
  @Test def intArgs4(): Unit =
    vatest(c"%d %d %d %d", "1 2 3 4")(stdio.sprintf(_, _, 1, 2, 3, 4))
  @Test def intArgs5(): Unit =
    vatest(c"%d %d %d %d %d", "1 2 3 4 5")(stdio.sprintf(_, _, 1, 2, 3, 4, 5))
  @Test def intArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", "1 2 3 4 5 6")(
      stdio.sprintf(_, _, 1, 2, 3, 4, 5, 6)
    )
  @Test def intArgs7(): Unit =
    vatest(c"%d %d %d %d %d %d %d", "1 2 3 4 5 6 7")(
      stdio.sprintf(_, _, 1, 2, 3, 4, 5, 6, 7)
    )
  @Test def intArgs8(): Unit =
    vatest(c"%d %d %d %d %d %d %d %d", "1 2 3 4 5 6 7 8")(
      stdio.sprintf(_, _, 1, 2, 3, 4, 5, 6, 7, 8)
    )
  @Test def intArgs9(): Unit =
    vatest(c"%d %d %d %d %d %d %d %d %d", "1 2 3 4 5 6 7 8 9")(
      stdio.sprintf(_, _, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    )

  @Test def longValue0(): Unit =
    vatest(c"%d", "0")(stdio.sprintf(_, _, 0L))
  @Test def longValue1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1L))
  @Test def longValueMinus1(): Unit =
    vatest(c"%d", "-1")(stdio.sprintf(_, _, -1L))
  @Test def longValueMin(): Unit = {
    assumeNot32Bit()
    vatest(c"%lld", "-9223372036854775808")(
      stdio.sprintf(_, _, java.lang.Long.MIN_VALUE)
    )
  }
  @Test def longValueMax(): Unit = {
    assumeNot32Bit()
    vatest(c"%lld", "9223372036854775807")(
      stdio.sprintf(_, _, java.lang.Long.MAX_VALUE)
    )
  }
  @Test def longArgs1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1L))
  @Test def longArgs2(): Unit =
    vatest(c"%d %d", "1 2")(stdio.sprintf(_, _, 1L, 2L))
  @Test def longArgs3(): Unit =
    vatest(c"%d %d %d", "1 2 3")(stdio.sprintf(_, _, 1L, 2L, 3L))
  @Test def longArgs4(): Unit =
    vatest(c"%d %d %d %d", "1 2 3 4")(stdio.sprintf(_, _, 1L, 2L, 3L, 4L))
  @Test def longArgs5(): Unit =
    vatest(c"%d %d %d %d %d", "1 2 3 4 5")(
      stdio.sprintf(_, _, 1L, 2L, 3L, 4L, 5L)
    )
  @Test def longArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", "1 2 3 4 5 6")(
      stdio.sprintf(_, _, 1L, 2L, 3L, 4L, 5L, 6L)
    )
  @Test def longArgs7(): Unit =
    vatest(c"%d %d %d %d %d %d %d", "1 2 3 4 5 6 7")(
      stdio.sprintf(_, _, 1L, 2L, 3L, 4L, 5L, 6L, 7L)
    )
  @Test def longArgs8(): Unit =
    vatest(c"%d %d %d %d %d %d %d %d", "1 2 3 4 5 6 7 8")(
      stdio.sprintf(_, _, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L)
    )
  @Test def longArgs9(): Unit =
    vatest(c"%d %d %d %d %d %d %d %d %d", "1 2 3 4 5 6 7 8 9")(
      stdio.sprintf(_, _, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
    )

  @Test def ubyteValueMin(): Unit =
    vatest(c"%d", "0")(stdio.sprintf(_, _, UByte.MinValue))
  @Test def ubyteValueMax(): Unit =
    vatest(c"%d", "255")(stdio.sprintf(_, _, UByte.MaxValue))
  @Test def ubyteArgs1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1.toUByte))
  @Test def ubyteArgs2(): Unit =
    vatest(c"%d %d", "1 2")(stdio.sprintf(_, _, 1.toUByte, 2.toUByte))
  @Test def ubyteArgs3(): Unit =
    vatest(c"%d %d %d", "1 2 3")(
      stdio.sprintf(_, _, 1.toUByte, 2.toUByte, 3.toUByte)
    )
  @Test def ubyteArgs4(): Unit =
    vatest(c"%d %d %d %d", "1 2 3 4")(
      stdio.sprintf(_, _, 1.toUByte, 2.toUByte, 3.toUByte, 4.toUByte)
    )
  @Test def ubyteArgs5(): Unit =
    vatest(c"%d %d %d %d %d", "1 2 3 4 5")(
      stdio.sprintf(_, _, 1.toUByte, 2.toUByte, 3.toUByte, 4.toUByte, 5.toUByte)
    )
  @Test def ubyteArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", "1 2 3 4 5 6")(
      stdio.sprintf(
        _,
        _,
        1.toUByte,
        2.toUByte,
        3.toUByte,
        4.toUByte,
        5.toUByte,
        6.toUByte
      )
    )
  @Test def ubyteArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUByte,
        2.toUByte,
        3.toUByte,
        4.toUByte,
        5.toUByte,
        6.toUByte,
        7.toUByte
      )
    )
  @Test def ubyteArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUByte,
        2.toUByte,
        3.toUByte,
        4.toUByte,
        5.toUByte,
        6.toUByte,
        7.toUByte,
        8.toUByte
      )
    )
  @Test def ubyteArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8 9"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUByte,
        2.toUByte,
        3.toUByte,
        4.toUByte,
        5.toUByte,
        6.toUByte,
        7.toUByte,
        8.toUByte,
        9.toUByte
      )
    )

  @Test def ushortValueMin(): Unit =
    vatest(c"%d", "0")(stdio.sprintf(_, _, UShort.MinValue))
  @Test def ushortValueMax(): Unit =
    vatest(c"%d", "65535")(stdio.sprintf(_, _, UShort.MaxValue))
  @Test def ushortArgs1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1.toUShort))
  @Test def ushortArgs2(): Unit =
    vatest(c"%d %d", "1 2")(stdio.sprintf(_, _, 1.toUShort, 2.toUShort))
  @Test def ushortArgs3(): Unit =
    vatest(c"%d %d %d", "1 2 3")(
      stdio.sprintf(_, _, 1.toUShort, 2.toUShort, 3.toUShort)
    )
  @Test def ushortArgs4(): Unit =
    vatest(c"%d %d %d %d", "1 2 3 4")(
      stdio.sprintf(_, _, 1.toUShort, 2.toUShort, 3.toUShort, 4.toUShort)
    )
  @Test def ushortArgs5(): Unit =
    vatest(c"%d %d %d %d %d", "1 2 3 4 5")(
      stdio.sprintf(
        _,
        _,
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort
      )
    )
  @Test def ushortArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d",
      "1 2 3 4 5 6"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort
      )
    )
  @Test def ushortArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort,
        7.toUShort
      )
    )
  @Test def ushortArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort,
        7.toUShort,
        8.toUShort
      )
    )
  @Test def ushortArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8 9"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort,
        7.toUShort,
        8.toUShort,
        9.toUShort
      )
    )

  @Test def uintValueMin(): Unit =
    vatest(c"%u", "0")(stdio.sprintf(_, _, UInt.MinValue))
  @Test def uintValueMax(): Unit =
    vatest(c"%u", "4294967295")(stdio.sprintf(_, _, UInt.MaxValue))
  @Test def uintArgs1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1.toUInt))
  @Test def uintArgs2(): Unit =
    vatest(c"%d %d", "1 2")(stdio.sprintf(_, _, 1.toUInt, 2.toUInt))
  @Test def uintArgs3(): Unit =
    vatest(c"%d %d %d", "1 2 3")(
      stdio.sprintf(_, _, 1.toUInt, 2.toUInt, 3.toUInt)
    )
  @Test def uintArgs4(): Unit =
    vatest(c"%d %d %d %d", "1 2 3 4")(
      stdio.sprintf(_, _, 1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt)
    )
  @Test def uintArgs5(): Unit =
    vatest(c"%d %d %d %d %d", "1 2 3 4 5")(
      stdio.sprintf(_, _, 1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt, 5.toUInt)
    )
  @Test def uintArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", "1 2 3 4 5 6")(
      stdio.sprintf(
        _,
        _,
        1.toUInt,
        2.toUInt,
        3.toUInt,
        4.toUInt,
        5.toUInt,
        6.toUInt
      )
    )
  @Test def uintArgs7(): Unit =
    vatest(c"%d %d %d %d %d %d %d", "1 2 3 4 5 6 7")(
      stdio.sprintf(
        _,
        _,
        1.toUInt,
        2.toUInt,
        3.toUInt,
        4.toUInt,
        5.toUInt,
        6.toUInt,
        7.toUInt
      )
    )
  @Test def uintArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUInt,
        2.toUInt,
        3.toUInt,
        4.toUInt,
        5.toUInt,
        6.toUInt,
        7.toUInt,
        8.toUInt
      )
    )
  @Test def uintArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8 9"
    )(
      stdio.sprintf(
        _,
        _,
        1.toUInt,
        2.toUInt,
        3.toUInt,
        4.toUInt,
        5.toUInt,
        6.toUInt,
        7.toUInt,
        8.toUInt,
        9.toUInt
      )
    )

  @Test def ulongValueMin(): Unit = {
    assumeNot32Bit()
    vatest(c"%llu", "0")(stdio.sprintf(_, _, ULong.MinValue))
  }
  @Test def ulongValueMax(): Unit = {
    assumeNot32Bit()
    vatest(c"%llu", "18446744073709551615")(stdio.sprintf(_, _, ULong.MaxValue))
  }
  @Test def ulongArgs1(): Unit =
    vatest(c"%d", "1")(stdio.sprintf(_, _, 1.toULong))
  @Test def ulongArgs2(): Unit =
    vatest(c"%d %d", "1 2")(stdio.sprintf(_, _, 1.toULong, 2.toULong))
  @Test def ulongArgs3(): Unit =
    vatest(c"%d %d %d", "1 2 3")(
      stdio.sprintf(_, _, 1.toULong, 2.toULong, 3.toULong)
    )
  @Test def ulongArgs4(): Unit =
    vatest(c"%d %d %d %d", "1 2 3 4")(
      stdio.sprintf(_, _, 1.toULong, 2.toULong, 3.toULong, 4.toULong)
    )
  @Test def ulongArgs5(): Unit =
    vatest(c"%d %d %d %d %d", "1 2 3 4 5")(
      stdio.sprintf(_, _, 1.toULong, 2.toULong, 3.toULong, 4.toULong, 5.toULong)
    )
  @Test def ulongArgs6(): Unit =
    vatest(c"%d %d %d %d %d %d", "1 2 3 4 5 6")(
      stdio.sprintf(
        _,
        _,
        1.toULong,
        2.toULong,
        3.toULong,
        4.toULong,
        5.toULong,
        6.toULong
      )
    )
  @Test def ulongArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7"
    )(
      stdio.sprintf(
        _,
        _,
        1.toULong,
        2.toULong,
        3.toULong,
        4.toULong,
        5.toULong,
        6.toULong,
        7.toULong
      )
    )
  @Test def ulongArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8"
    )(
      stdio.sprintf(
        _,
        _,
        1.toULong,
        2.toULong,
        3.toULong,
        4.toULong,
        5.toULong,
        6.toULong,
        7.toULong,
        8.toULong
      )
    )
  @Test def ulongArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d",
      "1 2 3 4 5 6 7 8 9"
    )(
      stdio.sprintf(
        _,
        _,
        1.toULong,
        2.toULong,
        3.toULong,
        4.toULong,
        5.toULong,
        6.toULong,
        7.toULong,
        8.toULong,
        9.toULong
      )
    )

  @Test def floatArgs1(): Unit =
    vatest(c"%1.1f", "1.1")(stdio.sprintf(_, _, 1.1f))
  @Test def floatArgs2(): Unit =
    vatest(c"%1.1f %1.1f", "1.1 2.2")(stdio.sprintf(_, _, 1.1f, 2.2f))
  @Test def floatArgs3(): Unit =
    vatest(c"%1.1f %1.1f %1.1f", "1.1 2.2 3.3")(
      stdio.sprintf(_, _, 1.1f, 2.2f, 3.3f)
    )
  @Test def floatArgs4(): Unit =
    vatest(c"%1.1f %1.1f %1.1f %1.1f", "1.1 2.2 3.3 4.4")(
      stdio.sprintf(_, _, 1.1f, 2.2f, 3.3f, 4.4f)
    )
  @Test def floatArgs5(): Unit =
    vatest(c"%1.1f %1.1f %1.1f %1.1f %1.1f", "1.1 2.2 3.3 4.4 5.5")(
      stdio.sprintf(_, _, 1.1f, 2.2f, 3.3f, 4.4f, 5.5f)
    )
  @Test def floatArgs6(): Unit =
    vatest(c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f", "1.1 2.2 3.3 4.4 5.5 6.6")(
      stdio.sprintf(_, _, 1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f)
    )
  @Test def floatArgs7(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7"
    )(stdio.sprintf(_, _, 1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f))
  @Test def floatArgs8(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8"
    )(stdio.sprintf(_, _, 1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f))
  @Test def floatArgs9(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9"
    )(stdio.sprintf(_, _, 1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f))

  @Test def doubleArgs1(): Unit =
    vatest(c"%1.1f", "1.1")(stdio.sprintf(_, _, 1.1d))
  @Test def doubleArgs2(): Unit =
    vatest(c"%1.1f %1.1f", "1.1 2.2")(stdio.sprintf(_, _, 1.1d, 2.2d))
  @Test def doubleArgs3(): Unit =
    vatest(c"%1.1f %1.1f %1.1f", "1.1 2.2 3.3")(
      stdio.sprintf(_, _, 1.1d, 2.2d, 3.3d)
    )
  @Test def doubleArgs4(): Unit =
    vatest(c"%1.1f %1.1f %1.1f %1.1f", "1.1 2.2 3.3 4.4")(
      stdio.sprintf(_, _, 1.1d, 2.2d, 3.3d, 4.4d)
    )
  @Test def doubleArgs5(): Unit =
    vatest(c"%1.1f %1.1f %1.1f %1.1f %1.1f", "1.1 2.2 3.3 4.4 5.5")(
      stdio.sprintf(_, _, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d)
    )
  @Test def doubleArgs6(): Unit =
    vatest(c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f", "1.1 2.2 3.3 4.4 5.5 6.6")(
      stdio.sprintf(_, _, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d)
    )
  @Test def doubleArgs7(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7"
    )(stdio.sprintf(_, _, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d))
  @Test def doubleArgs8(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8"
    )(stdio.sprintf(_, _, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d, 8.8d))
  @Test def doubleArgs9(): Unit =
    vatest(
      c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9"
    )(stdio.sprintf(_, _, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d, 8.8d, 9.9d))

  @Test def mixArgs1(): Unit =
    vatest(c"%d %1.1f", "1 1.1")(stdio.sprintf(_, _, 1, 1.1d))
  @Test def mixArgs2(): Unit =
    vatest(c"%d %d %1.1f %1.1f", "1 2 1.1 2.2")(
      stdio.sprintf(_, _, 1, 2, 1.1d, 2.2d)
    )
  @Test def mixArgs3(): Unit =
    vatest(c"%d %d %d %1.1f %1.1f %1.1f", "1 2 3 1.1 2.2 3.3")(
      stdio.sprintf(_, _, 1, 2, 3, 1.1d, 2.2d, 3.3d)
    )
  @Test def mixArgs4(): Unit =
    vatest(c"%d %d %d %d %1.1f %1.1f %1.1f %1.1f", "1 2 3 4 1.1 2.2 3.3 4.4")(
      stdio.sprintf(_, _, 1, 2, 3, 4, 1.1d, 2.2d, 3.3d, 4.4d)
    )
  @Test def mixArgs5(): Unit =
    vatest(
      c"%d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1 2 3 4 5 1.1 2.2 3.3 4.4 5.5"
    )(stdio.sprintf(_, _, 1, 2, 3, 4, 5, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d))
  @Test def mixArgs6(): Unit =
    vatest(
      c"%d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1 2 3 4 5 6 1.1 2.2 3.3 4.4 5.5 6.6"
    )(stdio.sprintf(_, _, 1, 2, 3, 4, 5, 6, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d))
  @Test def mixArgs7(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1 2 3 4 5 6 7 1.1 2.2 3.3 4.4 5.5 6.6 7.7"
    )(
      stdio.sprintf(
        _,
        _,
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        1.1d,
        2.2d,
        3.3d,
        4.4d,
        5.5d,
        6.6d,
        7.7d
      )
    )
  @Test def mixArgs8(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1 2 3 4 5 6 7 8 1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8"
    )(
      stdio.sprintf(
        _,
        _,
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        1.1d,
        2.2d,
        3.3d,
        4.4d,
        5.5d,
        6.6d,
        7.7d,
        8.8d
      )
    )
  @Test def mixArgs9(): Unit =
    vatest(
      c"%d %d %d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
      "1 2 3 4 5 6 7 8 9 1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9"
    )(
      stdio.sprintf(
        _,
        _,
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        1.1d,
        2.2d,
        3.3d,
        4.4d,
        5.5d,
        6.6d,
        7.7d,
        8.8d,
        9.9d
      )
    )
}
