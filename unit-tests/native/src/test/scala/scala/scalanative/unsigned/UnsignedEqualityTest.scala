package scala.scalanative.unsigned

import org.junit.Test
import org.junit.Assert._

class UnsignedEqualityTest {

  def testEquality(u1: AnyRef, u2: AnyRef, u3: AnyRef): Unit = {
    assertFalse(u1.eq(u2))
    assertFalse(u1.eq(u3))
    assertFalse(u2.eq(u3))

    assertTrue(u1 == u2)
    assertEquals(u1, u2)
    assertNotEquals(u1, u3)

    assertEquals(u1.hashCode(), u2.hashCode())
    assertNotEquals(u1.hashCode(), u3.hashCode())
  }

  @Test def testUByteEquals(): Unit = {
    testEquality(1.toUByte, 1.toUByte, 2.toUByte)
    assertNotEquals(1.toUByte, 1.toUShort)
  }

  @Test def testUShortEquals(): Unit = {
    testEquality(1.toUShort, 1.toUShort, 2.toUShort)
    assertNotEquals(1.toShort, 1.toUByte)
  }

  @Test def testUIntEquals(): Unit = {
    testEquality(1.toUInt, 1.toUInt, 2.toUInt)
    assertNotEquals(1.toUInt, 1.toULong)
  }

  @Test def testULongEquals(): Unit = {
    testEquality(1.toULong, 1.toULong, 2.toULong)
    assertNotEquals(1.toULong, 1.toUInt)
  }

}
