package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.util.zip._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class Adler32Test {

  @Test def constructor(): Unit = {
    val adl = new Adler32()
    assertTrue(adl.getValue() == 1)
  }

  @Test def getValue(): Unit = {
    val adl = new Adler32()
    assertTrue(adl.getValue() == 1)

    adl.reset()
    adl.update(1)
    assertTrue(adl.getValue() == 131074)
    adl.reset()
    assertTrue(adl.getValue() == 1)

    adl.reset()
    adl.update(Int.MinValue)
    assertTrue(adl.getValue() == 65537L)
  }

  @Test def reset(): Unit = {
    val adl = new Adler32()
    adl.update(1)
    assertTrue(adl.getValue() == 131074)
    adl.reset()
    assertTrue(adl.getValue() == 1)
  }

  @Test def updateInt(): Unit = {
    val adl = new Adler32()
    adl.update(1)
    assertTrue(adl.getValue() == 131074)

    adl.reset()
    adl.update(Int.MaxValue)
    assertTrue(adl.getValue() == 16777472L)

    adl.reset()
    adl.update(Int.MinValue)
    assertTrue(adl.getValue() == 65537L)
  }

  @Test def updateArrayByte(): Unit = {
    val byteArray = Array[Byte](1, 2)
    val adl = new Adler32()
    adl.update(byteArray)
    assertTrue(adl.getValue() == 393220)

    adl.reset()
    val byteEmpty = new Array[Byte](10000)
    adl.update(byteEmpty)
    assertTrue(adl.getValue() == 655360001L)
  }

  @Test def updateArrayByteWithInt(): Unit = {
    val byteArray = Array[Byte](1, 2, 3)
    val adl = new Adler32()
    val off = 2
    val len = 1
    val lenError = 3
    val offError = 4
    adl.update(byteArray, off, len)
    assertTrue(adl.getValue() == 262148)

    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      adl.update(byteArray, off, lenError)
    )

    assertThrows(
      classOf[ArrayIndexOutOfBoundsException],
      adl.update(byteArray, offError, len)
    )
  }

}
