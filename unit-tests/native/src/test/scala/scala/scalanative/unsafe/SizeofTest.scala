package scala.scalanative
package unsafe

import org.junit.Test
import org.junit.Assert._

import scalanative.unsigned._
import scala.scalanative.meta.LinktimeInfo.is32BitPlatform

class SizeofTest {

  @Test def testByte(): Unit = {
    assertTrue(sizeof[Byte] == 1.toUSize)
  }

  @Test def testUByte(): Unit = {
    assertTrue(sizeof[UByte] == 1.toUSize)
  }

  @Test def testShort(): Unit = {
    assertTrue(sizeof[Short] == 2.toUSize)
  }

  @Test def testUShort(): Unit = {
    assertTrue(sizeof[UShort] == 2.toUSize)
  }

  @Test def testInt(): Unit = {
    assertTrue(sizeof[Int] == 4.toUSize)
  }

  @Test def testUInt(): Unit = {
    assertTrue(sizeof[UInt] == 4.toUSize)
  }

  @Test def testLong(): Unit = {
    assertTrue(sizeof[Long] == 8.toUSize)
  }

  @Test def testULong(): Unit = {
    assertTrue(sizeof[ULong] == 8.toUSize)
  }

  @Test def testPtr(): Unit = {
    assertTrue(sizeof[Ptr[_]] == sizeof[Size])
  }

  @Test def testCStruct1Byte(): Unit = {
    assertTrue(sizeof[CStruct1[Byte]] == 1.toUSize)
  }

  @Test def testCStruct2ByteByte(): Unit = {
    assertTrue(sizeof[CStruct2[Byte, Byte]] == 2.toUSize)
  }

  @Test def testCStruct2ByteInt(): Unit = {
    assertTrue(sizeof[CStruct2[Byte, Int]] == 8.toUSize)
  }

  @Test def testCStruct3ByteShortByte(): Unit = {
    assertTrue(sizeof[CStruct3[Byte, Short, Byte]] == 6.toUSize)
  }

  @Test def testCStruct4ByteShortByteInt(): Unit = {
    assertTrue(sizeof[CStruct4[Byte, Short, Byte, Int]] == 12.toUSize)
  }

  @Test def testInnerStructCStruct2ByteCStruct2LongByte(): Unit = {
    val expectedSize = if (!is32BitPlatform) {
      24
    } else {
      16
    }

    assertTrue(
      sizeof[CStruct2[Byte, CStruct2[Long, Byte]]] == expectedSize.toUSize
    )
  }

  @Test def testInnerStructCStruct3ByteLongCStruct3IntIntByte(): Unit = {
    val expectedSize = if (!is32BitPlatform) {
      32
    } else {
      24
    }

    assertTrue(
      sizeof[
        CStruct3[Byte, Long, CStruct3[Int, Int, Byte]]
      ] == expectedSize.toUSize
    )
  }

  @Test def testInnerStructCStruct3ByteLongCStruct3IntIntCStruct4ByteIntShortByte()
      : Unit = {
    val expectedSize = if (!is32BitPlatform) {
      40
    } else {
      32
    }

    assertTrue(
      sizeof[CStruct3[
        Byte,
        Long,
        CStruct3[Int, Int, CStruct4[Byte, Int, Short, Byte]]
      ]] == expectedSize.toUSize
    )
  }

  type _32 = Nat.Digit2[Nat._3, Nat._2]
  type _128 = Nat.Digit3[Nat._1, Nat._2, Nat._8]
  type _1024 = Nat.Digit4[Nat._1, Nat._0, Nat._2, Nat._4]

  @Test def testCArrayByteNat32(): Unit = {
    assertTrue(sizeof[CArray[Byte, _32]] == 32.toUSize)
  }

  @Test def testCArrayByteNat128(): Unit = {
    assertTrue(sizeof[CArray[Byte, _128]] == 128.toUSize)
  }

  @Test def testCArrayByteNat1024(): Unit = {
    assertTrue(sizeof[CArray[Byte, _1024]] == 1024.toUSize)
  }

  @Test def testCArrayCStruct3ByteIntByteNat32(): Unit = {
    assertTrue(
      sizeof[CArray[CStruct3[Byte, Int, Byte], _32]] == (12 * 32).toUSize
    )
  }
}
