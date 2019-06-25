package scala.scalanative
package unsafe

import scalanative.unsigned._
import scalanative.unsafe._
import scalanative.libc.{stdio, stdlib, string}

object CVarArgListSuite extends tests.Suite {
  def vatest(
      name: String)(cstr: CString, varargs: Seq[CVarArg], output: String) =
    test(name) {
      Zone { implicit z =>
        val buff = alloc[CChar](1024)
        stdio.vsprintf(buff, cstr, toCVarArgList(varargs))
        val got = fromCString(buff)
        assert(got == output, s"$got != $output")
      }
    }

  vatest("byte value 0")(c"%d", Seq(0.toByte), "0")
  vatest("byte value 1")(c"%d", Seq(1.toByte), "1")
  vatest("byte value -1")(c"%d", Seq(-1.toByte), "-1")
  vatest("byte value min")(c"%d", Seq(java.lang.Byte.MIN_VALUE), "-128")
  vatest("byte value max")(c"%d", Seq(java.lang.Byte.MAX_VALUE), "127")
  vatest("byte args 1")(c"%d", Seq(1.toByte), "1")
  vatest("byte args 2")(c"%d %d", Seq(1.toByte, 2.toByte), "1 2")
  vatest("byte args 3")(c"%d %d %d", Seq(1.toByte, 2.toByte, 3.toByte), "1 2 3")
  vatest("byte args 4")(c"%d %d %d %d",
                        Seq(1.toByte, 2.toByte, 3.toByte, 4.toByte),
                        "1 2 3 4")
  vatest("byte args 5")(c"%d %d %d %d %d",
                        Seq(1.toByte, 2.toByte, 3.toByte, 4.toByte, 5.toByte),
                        "1 2 3 4 5")
  vatest("byte args 6")(
    c"%d %d %d %d %d %d",
    Seq(1.toByte, 2.toByte, 3.toByte, 4.toByte, 5.toByte, 6.toByte),
    "1 2 3 4 5 6")
  vatest("byte args 7")(
    c"%d %d %d %d %d %d %d",
    Seq(1.toByte, 2.toByte, 3.toByte, 4.toByte, 5.toByte, 6.toByte, 7.toByte),
    "1 2 3 4 5 6 7")
  vatest("byte args 8")(c"%d %d %d %d %d %d %d %d",
                        Seq(1.toByte,
                            2.toByte,
                            3.toByte,
                            4.toByte,
                            5.toByte,
                            6.toByte,
                            7.toByte,
                            8.toByte),
                        "1 2 3 4 5 6 7 8")
  vatest("byte args 9")(c"%d %d %d %d %d %d %d %d %d",
                        Seq(1.toByte,
                            2.toByte,
                            3.toByte,
                            4.toByte,
                            5.toByte,
                            6.toByte,
                            7.toByte,
                            8.toByte,
                            9.toByte),
                        "1 2 3 4 5 6 7 8 9")

  vatest("short value 0")(c"%d", Seq(0.toShort), "0")
  vatest("short value 1")(c"%d", Seq(1.toShort), "1")
  vatest("short value -1")(c"%d", Seq(-1.toShort), "-1")
  vatest("short value min")(c"%d", Seq(java.lang.Short.MIN_VALUE), "-32768")
  vatest("short value max")(c"%d", Seq(java.lang.Short.MAX_VALUE), "32767")
  vatest("short args 1")(c"%d", Seq(1.toShort), "1")
  vatest("short args 2")(c"%d %d", Seq(1.toShort, 2.toShort), "1 2")
  vatest("short args 3")(c"%d %d %d",
                         Seq(1.toShort, 2.toShort, 3.toShort),
                         "1 2 3")
  vatest("short args 4")(c"%d %d %d %d",
                         Seq(1.toShort, 2.toShort, 3.toShort, 4.toShort),
                         "1 2 3 4")
  vatest("short args 5")(
    c"%d %d %d %d %d",
    Seq(1.toShort, 2.toShort, 3.toShort, 4.toShort, 5.toShort),
    "1 2 3 4 5")
  vatest("short args 6")(
    c"%d %d %d %d %d %d",
    Seq(1.toShort, 2.toShort, 3.toShort, 4.toShort, 5.toShort, 6.toShort),
    "1 2 3 4 5 6")
  vatest("short args 7")(c"%d %d %d %d %d %d %d",
                         Seq(1.toShort,
                             2.toShort,
                             3.toShort,
                             4.toShort,
                             5.toShort,
                             6.toShort,
                             7.toShort),
                         "1 2 3 4 5 6 7")
  vatest("short args 8")(c"%d %d %d %d %d %d %d %d",
                         Seq(1.toShort,
                             2.toShort,
                             3.toShort,
                             4.toShort,
                             5.toShort,
                             6.toShort,
                             7.toShort,
                             8.toShort),
                         "1 2 3 4 5 6 7 8")
  vatest("short args 9")(c"%d %d %d %d %d %d %d %d %d",
                         Seq(1.toShort,
                             2.toShort,
                             3.toShort,
                             4.toShort,
                             5.toShort,
                             6.toShort,
                             7.toShort,
                             8.toShort,
                             9.toShort),
                         "1 2 3 4 5 6 7 8 9")

  vatest("int value 0")(c"%d", Seq(0), "0")
  vatest("int value 1")(c"%d", Seq(1), "1")
  vatest("int value -1")(c"%d", Seq(-1), "-1")
  vatest("int value min")(c"%d",
                          Seq(java.lang.Integer.MIN_VALUE),
                          "-2147483648")
  vatest("int value max")(c"%d", Seq(java.lang.Integer.MAX_VALUE), "2147483647")
  vatest("int args 1")(c"%d", Seq(1), "1")
  vatest("int args 2")(c"%d %d", Seq(1, 2), "1 2")
  vatest("int args 3")(c"%d %d %d", Seq(1, 2, 3), "1 2 3")
  vatest("int args 4")(c"%d %d %d %d", Seq(1, 2, 3, 4), "1 2 3 4")
  vatest("int args 5")(c"%d %d %d %d %d", Seq(1, 2, 3, 4, 5), "1 2 3 4 5")
  vatest("int args 6")(c"%d %d %d %d %d %d",
                       Seq(1, 2, 3, 4, 5, 6),
                       "1 2 3 4 5 6")
  vatest("int args 7")(c"%d %d %d %d %d %d %d",
                       Seq(1, 2, 3, 4, 5, 6, 7),
                       "1 2 3 4 5 6 7")
  vatest("int args 8")(c"%d %d %d %d %d %d %d %d",
                       Seq(1, 2, 3, 4, 5, 6, 7, 8),
                       "1 2 3 4 5 6 7 8")
  vatest("int args 9")(c"%d %d %d %d %d %d %d %d %d",
                       Seq(1, 2, 3, 4, 5, 6, 7, 8, 9),
                       "1 2 3 4 5 6 7 8 9")

  vatest("long value 0")(c"%d", Seq(0L), "0")
  vatest("long value 1")(c"%d", Seq(1L), "1")
  vatest("long value -1")(c"%d", Seq(-1L), "-1")
  vatest("long value min")(c"%lld",
                           Seq(java.lang.Long.MIN_VALUE),
                           "-9223372036854775808")
  vatest("long value max")(c"%lld",
                           Seq(java.lang.Long.MAX_VALUE),
                           "9223372036854775807")
  vatest("long args 1")(c"%d", Seq(1L), "1")
  vatest("long args 2")(c"%d %d", Seq(1L, 2L), "1 2")
  vatest("long args 3")(c"%d %d %d", Seq(1L, 2L, 3L), "1 2 3")
  vatest("long args 4")(c"%d %d %d %d", Seq(1L, 2L, 3L, 4L), "1 2 3 4")
  vatest("long args 5")(c"%d %d %d %d %d", Seq(1L, 2L, 3L, 4L, 5L), "1 2 3 4 5")
  vatest("long args 6")(c"%d %d %d %d %d %d",
                        Seq(1L, 2L, 3L, 4L, 5L, 6L),
                        "1 2 3 4 5 6")
  vatest("long args 7")(c"%d %d %d %d %d %d %d",
                        Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L),
                        "1 2 3 4 5 6 7")
  vatest("long args 8")(c"%d %d %d %d %d %d %d %d",
                        Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L),
                        "1 2 3 4 5 6 7 8")
  vatest("long args 9")(c"%d %d %d %d %d %d %d %d %d",
                        Seq(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L),
                        "1 2 3 4 5 6 7 8 9")

  vatest("ubyte value min")(c"%d", Seq(UByte.MinValue), "0")
  vatest("ubyte value max")(c"%d", Seq(UByte.MaxValue), "255")
  vatest("ubyte args 1")(c"%d", Seq(1.toUByte), "1")
  vatest("ubyte args 2")(c"%d %d", Seq(1.toUByte, 2.toUByte), "1 2")
  vatest("ubyte args 3")(c"%d %d %d",
                         Seq(1.toUByte, 2.toUByte, 3.toUByte),
                         "1 2 3")
  vatest("ubyte args 4")(c"%d %d %d %d",
                         Seq(1.toUByte, 2.toUByte, 3.toUByte, 4.toUByte),
                         "1 2 3 4")
  vatest("ubyte args 5")(
    c"%d %d %d %d %d",
    Seq(1.toUByte, 2.toUByte, 3.toUByte, 4.toUByte, 5.toUByte),
    "1 2 3 4 5")
  vatest("ubyte args 6")(
    c"%d %d %d %d %d %d",
    Seq(1.toUByte, 2.toUByte, 3.toUByte, 4.toUByte, 5.toUByte, 6.toUByte),
    "1 2 3 4 5 6")
  vatest("ubyte args 7")(c"%d %d %d %d %d %d %d",
                         Seq(1.toUByte,
                             2.toUByte,
                             3.toUByte,
                             4.toUByte,
                             5.toUByte,
                             6.toUByte,
                             7.toUByte),
                         "1 2 3 4 5 6 7")
  vatest("ubyte args 8")(c"%d %d %d %d %d %d %d %d",
                         Seq(1.toUByte,
                             2.toUByte,
                             3.toUByte,
                             4.toUByte,
                             5.toUByte,
                             6.toUByte,
                             7.toUByte,
                             8.toUByte),
                         "1 2 3 4 5 6 7 8")
  vatest("ubyte args 9")(c"%d %d %d %d %d %d %d %d %d",
                         Seq(1.toUByte,
                             2.toUByte,
                             3.toUByte,
                             4.toUByte,
                             5.toUByte,
                             6.toUByte,
                             7.toUByte,
                             8.toUByte,
                             9.toUByte),
                         "1 2 3 4 5 6 7 8 9")

  vatest("ushort value min")(c"%d", Seq(UShort.MinValue), "0")
  vatest("ushort value max")(c"%d", Seq(UShort.MaxValue), "65535")
  vatest("ushort args 1")(c"%d", Seq(1.toUShort), "1")
  vatest("ushort args 2")(c"%d %d", Seq(1.toUShort, 2.toUShort), "1 2")
  vatest("ushort args 3")(c"%d %d %d",
                          Seq(1.toUShort, 2.toUShort, 3.toUShort),
                          "1 2 3")
  vatest("ushort args 4")(c"%d %d %d %d",
                          Seq(1.toUShort, 2.toUShort, 3.toUShort, 4.toUShort),
                          "1 2 3 4")
  vatest("ushort args 5")(
    c"%d %d %d %d %d",
    Seq(1.toUShort, 2.toUShort, 3.toUShort, 4.toUShort, 5.toUShort),
    "1 2 3 4 5")
  vatest("ushort args 6")(
    c"%d %d %d %d %d %d",
    Seq(1.toUShort, 2.toUShort, 3.toUShort, 4.toUShort, 5.toUShort, 6.toUShort),
    "1 2 3 4 5 6")
  vatest("ushort args 7")(c"%d %d %d %d %d %d %d",
                          Seq(1.toUShort,
                              2.toUShort,
                              3.toUShort,
                              4.toUShort,
                              5.toUShort,
                              6.toUShort,
                              7.toUShort),
                          "1 2 3 4 5 6 7")
  vatest("ushort args 8")(c"%d %d %d %d %d %d %d %d",
                          Seq(1.toUShort,
                              2.toUShort,
                              3.toUShort,
                              4.toUShort,
                              5.toUShort,
                              6.toUShort,
                              7.toUShort,
                              8.toUShort),
                          "1 2 3 4 5 6 7 8")
  vatest("ushort args 9")(
    c"%d %d %d %d %d %d %d %d %d",
    Seq(1.toUShort,
        2.toUShort,
        3.toUShort,
        4.toUShort,
        5.toUShort,
        6.toUShort,
        7.toUShort,
        8.toUShort,
        9.toUShort),
    "1 2 3 4 5 6 7 8 9"
  )

  vatest("uint value min")(c"%u", Seq(UInt.MinValue), "0")
  vatest("uint value max")(c"%u", Seq(UInt.MaxValue), "4294967295")
  vatest("uint args 1")(c"%d", Seq(1.toUInt), "1")
  vatest("uint args 2")(c"%d %d", Seq(1.toUInt, 2.toUInt), "1 2")
  vatest("uint args 3")(c"%d %d %d", Seq(1.toUInt, 2.toUInt, 3.toUInt), "1 2 3")
  vatest("uint args 4")(c"%d %d %d %d",
                        Seq(1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt),
                        "1 2 3 4")
  vatest("uint args 5")(c"%d %d %d %d %d",
                        Seq(1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt, 5.toUInt),
                        "1 2 3 4 5")
  vatest("uint args 6")(
    c"%d %d %d %d %d %d",
    Seq(1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt, 5.toUInt, 6.toUInt),
    "1 2 3 4 5 6")
  vatest("uint args 7")(
    c"%d %d %d %d %d %d %d",
    Seq(1.toUInt, 2.toUInt, 3.toUInt, 4.toUInt, 5.toUInt, 6.toUInt, 7.toUInt),
    "1 2 3 4 5 6 7")
  vatest("uint args 8")(c"%d %d %d %d %d %d %d %d",
                        Seq(1.toUInt,
                            2.toUInt,
                            3.toUInt,
                            4.toUInt,
                            5.toUInt,
                            6.toUInt,
                            7.toUInt,
                            8.toUInt),
                        "1 2 3 4 5 6 7 8")
  vatest("uint args 9")(c"%d %d %d %d %d %d %d %d %d",
                        Seq(1.toUInt,
                            2.toUInt,
                            3.toUInt,
                            4.toUInt,
                            5.toUInt,
                            6.toUInt,
                            7.toUInt,
                            8.toUInt,
                            9.toUInt),
                        "1 2 3 4 5 6 7 8 9")

  vatest("ulong value min")(c"%llu", Seq(ULong.MinValue), "0")
  vatest("ulong value max")(c"%llu",
                            Seq(ULong.MaxValue),
                            "18446744073709551615")
  vatest("ulong args 1")(c"%d", Seq(1.toULong), "1")
  vatest("ulong args 2")(c"%d %d", Seq(1.toULong, 2.toULong), "1 2")
  vatest("ulong args 3")(c"%d %d %d",
                         Seq(1.toULong, 2.toULong, 3.toULong),
                         "1 2 3")
  vatest("ulong args 4")(c"%d %d %d %d",
                         Seq(1.toULong, 2.toULong, 3.toULong, 4.toULong),
                         "1 2 3 4")
  vatest("ulong args 5")(
    c"%d %d %d %d %d",
    Seq(1.toULong, 2.toULong, 3.toULong, 4.toULong, 5.toULong),
    "1 2 3 4 5")
  vatest("ulong args 6")(
    c"%d %d %d %d %d %d",
    Seq(1.toULong, 2.toULong, 3.toULong, 4.toULong, 5.toULong, 6.toULong),
    "1 2 3 4 5 6")
  vatest("ulong args 7")(c"%d %d %d %d %d %d %d",
                         Seq(1.toULong,
                             2.toULong,
                             3.toULong,
                             4.toULong,
                             5.toULong,
                             6.toULong,
                             7.toULong),
                         "1 2 3 4 5 6 7")
  vatest("ulong args 8")(c"%d %d %d %d %d %d %d %d",
                         Seq(1.toULong,
                             2.toULong,
                             3.toULong,
                             4.toULong,
                             5.toULong,
                             6.toULong,
                             7.toULong,
                             8.toULong),
                         "1 2 3 4 5 6 7 8")
  vatest("ulong args 9")(c"%d %d %d %d %d %d %d %d %d",
                         Seq(1.toULong,
                             2.toULong,
                             3.toULong,
                             4.toULong,
                             5.toULong,
                             6.toULong,
                             7.toULong,
                             8.toULong,
                             9.toULong),
                         "1 2 3 4 5 6 7 8 9")

  vatest("float args 1")(c"%1.1f", Seq(1.1f), "1.1")
  vatest("float args 2")(c"%1.1f %1.1f", Seq(1.1f, 2.2f), "1.1 2.2")
  vatest("float args 3")(c"%1.1f %1.1f %1.1f",
                         Seq(1.1f, 2.2f, 3.3f),
                         "1.1 2.2 3.3")
  vatest("float args 4")(c"%1.1f %1.1f %1.1f %1.1f",
                         Seq(1.1f, 2.2f, 3.3f, 4.4f),
                         "1.1 2.2 3.3 4.4")
  vatest("float args 5")(c"%1.1f %1.1f %1.1f %1.1f %1.1f",
                         Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f),
                         "1.1 2.2 3.3 4.4 5.5")
  vatest("float args 6")(c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
                         Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f),
                         "1.1 2.2 3.3 4.4 5.5 6.6")
  vatest("float args 7")(c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
                         Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f),
                         "1.1 2.2 3.3 4.4 5.5 6.6 7.7")
  vatest("float args 8")(c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
                         Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f),
                         "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8")
  vatest("float args 9")(
    c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
    Seq(1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f),
    "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9")

  vatest("double args 1")(c"%1.1f", Seq(1.1d), "1.1")
  vatest("double args 2")(c"%1.1f %1.1f", Seq(1.1d, 2.2d), "1.1 2.2")
  vatest("double args 3")(c"%1.1f %1.1f %1.1f",
                          Seq(1.1d, 2.2d, 3.3d),
                          "1.1 2.2 3.3")
  vatest("double args 4")(c"%1.1f %1.1f %1.1f %1.1f",
                          Seq(1.1d, 2.2d, 3.3d, 4.4d),
                          "1.1 2.2 3.3 4.4")
  vatest("double args 5")(c"%1.1f %1.1f %1.1f %1.1f %1.1f",
                          Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d),
                          "1.1 2.2 3.3 4.4 5.5")
  vatest("double args 6")(c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
                          Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d),
                          "1.1 2.2 3.3 4.4 5.5 6.6")
  vatest("double args 7")(c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
                          Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d),
                          "1.1 2.2 3.3 4.4 5.5 6.6 7.7")
  vatest("double args 8")(c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
                          Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d, 8.8d),
                          "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8")
  vatest("double args 9")(
    c"%1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
    Seq(1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d, 8.8d, 9.9d),
    "1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9")

  vatest("mix args 1")(c"%d %1.1f", Seq(1, 1.1d), "1 1.1")
  vatest("mix args 2")(c"%d %d %1.1f %1.1f",
                       Seq(1, 2, 1.1d, 2.2d),
                       "1 2 1.1 2.2")
  vatest("mix args 3")(c"%d %d %d %1.1f %1.1f %1.1f",
                       Seq(1, 2, 3, 1.1d, 2.2d, 3.3d),
                       "1 2 3 1.1 2.2 3.3")
  vatest("mix args 4")(c"%d %d %d %d %1.1f %1.1f %1.1f %1.1f",
                       Seq(1, 2, 3, 4, 1.1d, 2.2d, 3.3d, 4.4d),
                       "1 2 3 4 1.1 2.2 3.3 4.4")
  vatest("mix args 5")(c"%d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f",
                       Seq(1, 2, 3, 4, 5, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d),
                       "1 2 3 4 5 1.1 2.2 3.3 4.4 5.5")
  vatest("mix args 6")(
    c"%d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
    Seq(1, 2, 3, 4, 5, 6, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d),
    "1 2 3 4 5 6 1.1 2.2 3.3 4.4 5.5 6.6")
  vatest("mix args 7")(
    c"%d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
    Seq(1, 2, 3, 4, 5, 6, 7, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d),
    "1 2 3 4 5 6 7 1.1 2.2 3.3 4.4 5.5 6.6 7.7"
  )
  vatest("mix args 8")(
    c"%d %d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
    Seq(1, 2, 3, 4, 5, 6, 7, 8, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d, 8.8d),
    "1 2 3 4 5 6 7 8 1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8"
  )
  vatest("mix args 9")(
    c"%d %d %d %d %d %d %d %d %d %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f %1.1f",
    Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 1.1d, 2.2d, 3.3d, 4.4d, 5.5d, 6.6d, 7.7d,
      8.8d, 9.9d),
    "1 2 3 4 5 6 7 8 9 1.1 2.2 3.3 4.4 5.5 6.6 7.7 8.8 9.9"
  )
}
