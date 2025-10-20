package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.util.zip.*
import java.io.ByteArrayOutputStream

import org.junit.Test
import org.junit.Assert.*

class CheckedOutputStreamTest {

  @Test def constructor(): Unit = {
    val out = new ByteArrayOutputStream()
    val chkOut = new CheckedOutputStream(out, new CRC32())
    assertTrue(chkOut.getChecksum().getValue() == 0)
  }

  @Test def getChecksum(): Unit = {
    val byteArray = Array[Byte](1, 2, 3, 'e', 'r', 't', 'g', 3, 6)
    val out = new ByteArrayOutputStream()
    val chkOut = new CheckedOutputStream(out, new Adler32())
    chkOut.write(byteArray(4))

    assertTrue(chkOut.getChecksum().getValue() == 7536755)
    chkOut.getChecksum().reset()
    chkOut.write(byteArray, 5, 4)

    assertTrue(chkOut.getChecksum().getValue() == 51708133)
  }

  @Test def writeInt(): Unit = {
    val byteArray = Array[Byte](1, 2, 3, 'e', 'r', 't', 'g', 3, 6)
    val out = new ByteArrayOutputStream()
    val chkOut = new CheckedOutputStream(out, new CRC32())
    byteArray.foreach(b => chkOut.write(b))
    assertTrue(chkOut.getChecksum().getValue() != 0)
  }

  @Test def writeArrayByteIntInt(): Unit = {
    val byteArray = Array[Byte](1, 2, 3, 'e', 'r', 't', 'g', 3, 6)
    val out = new ByteArrayOutputStream()
    val chkOut = new CheckedOutputStream(out, new CRC32())
    chkOut.write(byteArray, 4, 5)
    assertTrue(chkOut.getChecksum().getValue != 0)
  }

}
