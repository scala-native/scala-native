package scala.scalanative.unsigned

import org.junit.Test
import org.junit.Assert._
import scala.scalanative.unsafe._
import scala.reflect.ClassTag
import scala.scalanative.meta.LinktimeInfo.is32BitPlatform

class UnsignedUniversalEqualityTest {
  private def testUniversalEquality[Signed: ClassTag, Unsigned: ClassTag](
      sPos: Signed,
      uPos: Unsigned,
      sNeg: Signed,
      uNeg: Unsigned,
      sZero: Signed,
      uZero: Unsigned
  ): Unit = {
    def clue = {
      val sType = sPos.getClass.getName()
      val uType = uPos.getClass().getName()
      s"{sType=$sType, uType=$uType,sPos=$sPos, uPos=$uPos, sNeg=$sNeg, uNeg=$uNeg}"
    }
    assertNotSame(s"same1-$clue", uPos, uNeg)
    assertNotSame(s"same2-$clue", sPos, sNeg)
    assertNotSame(s"same3-$clue", sPos, uPos)
    assertNotSame(s"same4-$clue", sPos, uPos)
    assertNotSame(s"same5-$clue", sZero, uZero)

    assertNotEquals(s"equals1-$clue", sPos, uPos)
    assertNotEquals(s"equals2-$clue", uPos, sPos)
    assertNotEquals(s"equals3-$clue", sNeg, uNeg)
    assertNotEquals(s"equals4-$clue", uNeg, sNeg)
    assertNotEquals(s"equals5-$clue", uZero, sZero)
    assertNotEquals(s"equals6-$clue", sZero, uZero)

    assertTrue(s"==1-$clue", sPos == uPos)
    assertTrue(s"==2-$clue", uPos == sPos)

    val negativeUnsignedCanEqual =
      (implicitly[ClassTag[Signed]], implicitly[ClassTag[Unsigned]]) match {
        // Special case for char which is an unsigned
        // Specific to this test, we do narrow the original -1:Int using -1.toChar
        // It makes it yield true for -1:char == -1:uint|ulong
        // Transformation using not narrowed char is done outside this test
        case (ClassTag.Char, ClassTag(unsigned)) =>
          if (unsigned == classOf[UByte]) false
          else true
        case _ => false
      }
    if (negativeUnsignedCanEqual) {
      assertTrue(s"==3a-$clue", sNeg == uNeg)
      assertTrue(s"==4a-$clue", uNeg == sNeg)
    } else {
      assertFalse(s"==3b-$clue", sNeg == uNeg)
      assertFalse(s"==4b-$clue", uNeg == sNeg)
    }
    assertTrue(s"==5-$clue", sZero == uZero)
    assertTrue(s"==6-$clue", uZero == sZero)

    assertTrue(s"!=1-$clue", sPos != uNeg)
    assertTrue(s"!=2-$clue", uPos != sNeg)
    assertTrue(s"!=3-$clue", sNeg != uPos)
    assertTrue(s"!=4-$clue", uNeg != sPos)
    assertFalse("!=5-$clue", sZero != uZero)
    assertFalse("!=6-$clue", uZero != sZero)
  }

  private def testUniversalEquality[Signed: ClassTag, Unsigned: ClassTag](
      toSigned: Int => Signed
  )(toUnsigned: Signed => Unsigned): Unit = {
    val posOne = toSigned(1)
    val minusOne = toSigned(-1)
    val zero = toSigned(0)
    testUniversalEquality[Signed, Unsigned](
      sPos = posOne,
      uPos = toUnsigned(posOne),
      sNeg = minusOne,
      uNeg = toUnsigned(minusOne),
      sZero = zero,
      uZero = toUnsigned(zero)
    )
  }

  @Test def testByte(): Unit = {
    testUniversalEquality(_.toByte)(_.toUByte)
    testUniversalEquality(_.toByte)(_.toUShort)
    testUniversalEquality(_.toByte)(_.toUInt)
    testUniversalEquality(_.toByte)(_.toULong)
    testUniversalEquality(_.toByte)(_.toUSize)
    assertTrue(-1.toUByte == 255)
    assertNotEquals(-1.toUByte, 255)
  }

  @Test def testShort(): Unit = {
    testUniversalEquality(_.toShort)(_.toUByte)
    testUniversalEquality(_.toShort)(_.toUShort)
    testUniversalEquality(_.toShort)(_.toUInt)
    testUniversalEquality(_.toShort)(_.toULong)
    testUniversalEquality(_.toShort)(_.toUSize)
    assertTrue(-1.toUShort == 65535)
    assertNotEquals(-1.toUShort, 65535)
  }

  @Test def testInt(): Unit = {
    testUniversalEquality(_.toInt)(_.toUByte)
    testUniversalEquality(_.toInt)(_.toUShort)
    testUniversalEquality(_.toInt)(_.toUInt)
    testUniversalEquality(_.toInt)(_.toULong)
    testUniversalEquality(_.toInt)(_.toUSize)
    assertTrue(-1.toUInt == java.lang.Integer.toUnsignedLong(-1))
    assertTrue(-1.toUInt == 4294967295L)
    assertNotEquals(-1.toUInt, 4294967295L)
  }

  @Test def testLong(): Unit = {
    testUniversalEquality(_.toLong)(_.toUByte)
    testUniversalEquality(_.toLong)(_.toUShort)
    testUniversalEquality(_.toLong)(_.toUInt)
    testUniversalEquality(_.toLong)(_.toULong)
    testUniversalEquality(_.toLong)(_.toUSize)
    assertTrue(-1.toULong == java.lang.Integer.toUnsignedLong(-1))
    assertTrue(-1.toULong == 4294967295L)
    assertEquals(-1L.toULong.toString(), java.lang.Long.toUnsignedString(-1L))
    assertEquals(-1L.toULong.toString(), "18446744073709551615")
  }

  @Test def testSize(): Unit = {
    testUniversalEquality(_.toSize)(_.toUByte)
    testUniversalEquality(_.toSize)(_.toUShort)
    testUniversalEquality(_.toSize)(_.toUInt)
    testUniversalEquality(_.toSize)(_.toULong)
    testUniversalEquality(_.toSize)(_.toUSize)
    assertTrue(-1.toSize == -1)
    assertTrue(-1.toUSize == 4294967295L)
    assertTrue(-1.toUSize == -1.toUInt)
    // different base when converting to unsigned
    assertFalse(-1.toUSize == -1L.toULong)
    assertFalse(-1L.toUSize == -1.toULong)
    if (is32BitPlatform) {
      assertTrue(-1.toUSize == -1.toUInt)
      assertTrue(-1.toUSize == -1.toULong)
      // TODO: this one might be bugged, -1: uint32 should not equal -1: uint64 
      // uses USize.== not the universal equality resolved using BoxesRunTime
      // assertFalse(-1L.toUSize == -1L.toULong)
    } else {
      assertTrue(-1L.toUSize == -1L.toULong)
      assertEquals(-1L.toUSize.toString(), java.lang.Long.toUnsignedString(-1L))
      assertEquals(-1L.toUSize.toString(), "18446744073709551615")
    }
  }

  @Test def testChar(): Unit = {
    testUniversalEquality(_.toChar)(_.toUByte)
    testUniversalEquality(_.toChar)(_.toUShort)
    testUniversalEquality(_.toChar)(_.toUInt)
    testUniversalEquality(_.toChar)(_.toULong)
    testUniversalEquality(_.toChar)(_.toUSize)
    assertFalse(-1.toUByte == -1.toChar)
    assertTrue(-1.toUByte == 255.toChar)

    assertTrue(-1.toUShort == -1.toChar)
    assertTrue(-1.toUShort == 65535.toChar)

    assertFalse(-1.toUInt == -1.toChar)
    assertFalse(-1.toULong == -1.toChar)
    // variant observed in testUniversalEquality
    assertTrue(-1.toChar.toUInt == -1.toChar)
    assertTrue(-1.toChar.toULong == -1.toChar)
  }
}
