package org.scalanative.testsuite.javalib.io

import java.io.{File, OutputStream, PrintStream}
import java.nio.charset.StandardCharsets

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class PrintStreamTestOnJDK10 {
  /* Due to limited time to implement, this Class is a minimal implantation.
   * For each of the various PrintStream constructors, it invokes that
   * constructor and then invokes a write method on it. If it can write,
   * it can sing!
   *
   * More extensive tests could be written, given extended resources and time.
   */

  final val secondComing = """
               |""Things fall apart; the centre cannot hold;
               |Mere anarchy is loosed upon the world,
               """

  // Do not use /dev/null on Windows. It leads to FileNotFoundException
  final val devNull = if (Platform.isWindows) "NUL" else "/dev/null"

  @Test def constructorFileCharset(): Unit = {
    val bytesUTF_8 = secondComing.getBytes(StandardCharsets.UTF_8)

    val outCharset = StandardCharsets.UTF_16LE
    val ps = new PrintStream(new File(devNull), outCharset)

    // check only that created ps is healthy enough that no Exception is thown
    ps.write(bytesUTF_8)
  }

  @Test def constructorOutputStreamBooleanCharset(): Unit = {
    val bytesUTF_8 = secondComing.getBytes(StandardCharsets.UTF_8)

    val outStream = OutputStream.nullOutputStream()
    val outCharset = StandardCharsets.UTF_16LE
    val ps = new PrintStream(outStream, false, outCharset)

    // check only that created ps is healthy enough that no Exception is thown
    ps.write(bytesUTF_8)
  }

  @Test def constructorString(): Unit = {
    val bytesUTF_8 = secondComing.getBytes(StandardCharsets.UTF_8)

    val ps = new PrintStream(devNull)

    // check only that created ps is healthy enough that no Exception is thown
    ps.write(bytesUTF_8)
  }

  @Test def constructorStringCharset(): Unit = {
    val bytesUTF_8 = secondComing.getBytes(StandardCharsets.UTF_8)

    val outCharset = StandardCharsets.UTF_16LE
    val ps = new PrintStream(devNull, outCharset)

    // check only that created ps is healthy enough that no Exception is thown
    ps.write(bytesUTF_8)
  }

  @Test def constructorStringString(): Unit = {
    val bytesUTF_8 = secondComing.getBytes(StandardCharsets.UTF_8)

    val ps = new PrintStream(devNull, "UTF_16LE")

    // check only that created ps is healthy enough that no Exception is thown
    ps.write(bytesUTF_8)
  }
}
