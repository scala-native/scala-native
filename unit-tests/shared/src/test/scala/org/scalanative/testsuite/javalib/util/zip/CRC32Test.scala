package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.util.zip._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CRC32Test {

  @Test def constructor(): Unit = {
    val crc = new CRC32()
    assertTrue(crc.getValue() == 0)
  }

  @Test def getValue(): Unit = {
    val crc = new CRC32()
    assertTrue(crc.getValue() == 0)

    crc.reset()
    crc.update(Int.MaxValue)
    assertTrue(crc.getValue() == 4278190080L)

    crc.reset()
    val byteEmpty = new Array[Byte](10000)
    crc.update(byteEmpty)
    assertTrue(crc.getValue() == 1295764014L)

    crc.reset()
    crc.update(1)
    assertTrue(crc.getValue() == 2768625435L)

    crc.reset()
    assertTrue(crc.getValue() == 0)
  }

  @Test def updateArrayByte(): Unit = {
    val byteArray = Array[Byte](1, 2)
    val crc = new CRC32()
    crc.update(byteArray)
    assertTrue(crc.getValue() == 3066839698L)

    crc.reset()
    val empty = new Array[Byte](10000)
    crc.update(empty)
    assertTrue(crc.getValue() == 1295764014L)
  }

  @Test def updateArrayByteIntInt(): Unit = {
    val byteArray = Array[Byte](1, 2, 3)
    val crc = new CRC32()
    val off = 2
    val len = 1
    val lenError = 3
    val offError = 4
    crc.update(byteArray, off, len)
    assertTrue(crc.getValue() == 1259060791L)

    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      crc.update(byteArray, off, lenError)
    )

    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      crc.update(byteArray, offError, len)
    )
  }

  @Test def updateEmptyArray(): Unit = {
    val crc = new CRC32()
    crc.update(Array.emptyByteArray)
    assertEquals(crc.getValue(), 0)
  }

}
