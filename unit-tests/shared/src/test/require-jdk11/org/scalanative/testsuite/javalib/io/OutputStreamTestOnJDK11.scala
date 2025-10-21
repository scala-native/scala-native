package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class OutputStreamTestOnJDK11 {

  @Test def nullOutputStreamWhenOpen(): Unit = {
    val streamOut = OutputStream.nullOutputStream()

    val outputBytes =
      List(255, 254, 253, 252, 251, 128, 127, 2, 1, 0)
        .map(_.toByte)
        .toArray[Byte]

    // Exercise each API method. Expect that no method will throw

    streamOut.flush() // empty stream

    streamOut.write(outputBytes)

    streamOut.write(outputBytes, 2, outputBytes.length - 2)

    streamOut.write(255)

    streamOut.flush() // flush again with something in stream.

    streamOut.close()
  }

  @Test def nullOutputStreamWhenClosed(): Unit = {
    val streamOut = OutputStream.nullOutputStream()

    streamOut.close() // Expect no Exception thrown

    streamOut.flush() // Expect no Exception thrown

    val outputBytes =
      List(255, 254, 253, 252, 251, 128, 127, 2, 1, 0)
        .map(_.toByte)
        .toArray[Byte]

    assertThrows(
      "write(Array[Byte])",
      classOf[IOException],
      streamOut.write(outputBytes)
    )

    assertThrows(
      "write(Array[Byte], off, len)",
      classOf[IOException],
      streamOut.write(outputBytes, 2, outputBytes.length - 2)
    )

    assertThrows(
      "write(Int)",
      classOf[IOException],
      streamOut.write(255)
    )

    streamOut.close() // close() of closed steam should be silent
  }

}
