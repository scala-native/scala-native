package org.scalanative.testsuite.javalib.lang

import java.lang._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ByteTest {
  private def assertNFE(expectedMsg: String)(code: => Any): Unit = {
    val e = assertThrows(classOf[NumberFormatException], code)
    assertEquals(expectedMsg, e.getMessage())
  }

  @Test def parseByteOutOfRange(): Unit = {
    assertEquals(127.toByte, Byte.parseByte("127"))
    assertEquals(-128.toByte, Byte.parseByte("-80", 16))

    assertNFE("""Value out of range. Value:"128" Radix:10""")(
      Byte.parseByte("128")
    )
    assertNFE("""Value out of range. Value:"-129" Radix:10""")(
      Byte.parseByte("-129")
    )
    assertNFE("""Value out of range. Value:"ff" Radix:16""")(
      Byte.parseByte("ff", 16)
    )
    assertNFE("""Value out of range. Value:"128" Radix:10""")(
      Byte.valueOf("128")
    )
    assertNFE("""Value out of range. Value:"80" Radix:16""")(
      Byte.valueOf("80", 16)
    )
  }

  @Test def decodeOutOfRange(): Unit = {
    assertEquals(-128.toByte, Byte.decode("-0x80").byteValue())

    assertNFE("Value 128 out of range from input 128")(Byte.decode("128"))
    assertNFE("Value 128 out of range from input 0x80")(Byte.decode("0x80"))
    assertNFE("Value -129 out of range from input -0x81")(
      Byte.decode("-0x81")
    )
    assertNFE("Value 128 out of range from input 0200")(Byte.decode("0200"))
  }
}
