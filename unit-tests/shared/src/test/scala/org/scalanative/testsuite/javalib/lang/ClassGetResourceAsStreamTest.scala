package org.scalanative.testsuite.javalib.lang

import org.junit.Assert.*
import org.junit.Assume.*
import org.junit.Test

import java.nio.charset.StandardCharsets
import java.io.{BufferedReader, InputStreamReader}
import java.io.FileInputStream

import org.scalanative.testsuite.utils.Platform

class ClassGetResourceAsStreamTest {

  val basicFileAbsolute = "/embedded-resources-tests/basic-file.txt"
  val basicFileRelative = "../../../embedded-resources-tests/basic-file.txt"

  @Test def doesNotLoseUtf8Data(): Unit = {
    val inputStream = getClass().getResourceAsStream(
      "/embedded-resources-tests/no_extension_utf8"
    )
    val text = new BufferedReader(
      new InputStreamReader(inputStream, StandardCharsets.UTF_8)
    ).readLine()

    assertEquals(text, "śćŹŻźó")
  }

  @Test def resourceInputStreamSkip(): Unit = {
    val inputStream = getClass().getResourceAsStream(basicFileAbsolute)

    inputStream.skip(11)
    val expected = "second line"
    val expectedCharArray = expected.toCharArray()
    for (i <- 0 until expected.size) {
      assertEquals(
        s"Byte #$i in \'$expected\'",
        expectedCharArray(i),
        inputStream.read()
      )
    }

    inputStream.skip(7)
    val expectedSecond = "line"
    val expectedSecondCharArray = expectedSecond.toCharArray
    for (i <- 0 until expectedSecond.size) {
      assertEquals(
        s"Byte #${i} in \'$expectedSecond\'",
        expectedSecondCharArray(i),
        inputStream.read()
      )
    }

    val inputStream2 = getClass().getResourceAsStream(basicFileAbsolute)
    val skipped = inputStream2.skip(100)
    val expectedSkipped = 33
    assertEquals(
      "Skipped when skipping more than allowed",
      expectedSkipped,
      skipped
    )
    assertEquals(
      "Read value after skipping to the end",
      -1,
      inputStream2.read()
    )
  }

  @Test def resourceInputStreamMark(): Unit = {
    val inputStream = getClass().getResourceAsStream(basicFileAbsolute)
    assumeTrue(
      "Mark supported on resource InputStream",
      inputStream.markSupported()
    )

    val readBeforeMark = 5
    val readAfterMark = 6
    val readLimit = 10
    for (i <- 0 to readBeforeMark) inputStream.read()
    val expectedAvailable = inputStream.available()

    inputStream.mark(readLimit)
    for (i <- 0 to readAfterMark) inputStream.read()
    inputStream.reset()
    assertEquals(
      "available after reset() after mark()",
      expectedAvailable,
      inputStream.available()
    )
  }

  private def resourceInputStreamReadByte(path: String): Unit = {
    val embeddedInputStream = getClass().getResourceAsStream(basicFileAbsolute)
    val fileInputStream =
      if (Platform.executingInJVM) // In JVM, cwd is set to unit-tests/jvm/[scala-version]
        new FileInputStream(
          "../../shared/src/test/resources/embedded-resources-tests/basic-file.txt"
        )
      else
        new FileInputStream(
          "unit-tests/shared/src/test/resources/embedded-resources-tests/basic-file.txt"
        )

    var idx = 0
    var i = 0
    var j = 0
    while (i != -1) {
      i = fileInputStream.read()
      j = embeddedInputStream.read()
      assertEquals(s"Byte $idx", i, j)
      idx += 1
    }
  }

  @Test def resourceInputStreamReadByteRelative(): Unit =
    resourceInputStreamReadByte(basicFileRelative)

  @Test def resourceInputStreamReadByteAbsolute(): Unit =
    resourceInputStreamReadByte(basicFileAbsolute)

  @Test def returnsNullIfNotExists(): Unit = {
    val inputStream = getClass().getResourceAsStream("NotExists")
    assertNull(inputStream)
  }

}
