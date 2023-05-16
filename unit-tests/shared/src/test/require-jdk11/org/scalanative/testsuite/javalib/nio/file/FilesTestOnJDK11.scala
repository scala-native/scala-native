package org.scalanative.testsuite
package javalib.nio.file

import java.{lang => jl}
import java.{util => ju}

import java.nio.charset.StandardCharsets
import java.nio.CharBuffer
import java.nio.file.Files
import java.nio.file.{Path, Paths}
import java.nio.file.{FileAlreadyExistsException, StandardOpenOption}

import org.junit.Test
import org.junit.Assert._
import org.junit.{BeforeClass, AfterClass}
import org.junit.Ignore

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class FilesTestOnJDK11 {
  import FilesTestOnJDK11._

  /* Design Notes:
   *
   * 1) This set of Tests is designed to clean up after itself. That is
   *    delete all directories and files created.  For debugging one
   *    can comment-out the Files.delete() in the afterClass() static method.
   *
   * 2) To simplify implementation, The readString() Tests call writeString().
   *    This leaves open the possibility of complementary and/or compensating
   *    errors in the two methods.
   *
   *    In a better World, writeString() would be Test'ed before possibly
   *    being used by readString(). Currently the order of execution of tests
   *    is hard to determine and harder (not possible) to specify.
   *    This Suite is forced to rely on writeString() eventually getting
   *    strongly tested.
   *
   *    Normally one would expect to see the writeString() tests at the top
   *    of the file, expecting them to be run before being used by
   *    readString(). Here, the writeString() methods are later in the file
   *    because there are hints, but not guarantees, that Tests later in the
   *    file are run before those earlier.  No wonder why it is hard to
   *    find Test developers.
   *
   * 3) The comment above the largeWriteStringThenReadString() Test explains
   *    why it is @Ignore'd in normal Continuous Integration.
   *
   * 4) There are no tests for CharSequence javax.swing.text.Segment because
   *    Segment is not implemented by Scala Native.
   */

  @Test def readStringUsingDefaultCharsetUTF8(): Unit = {
    val ioPath = getCleanIoPath("utf8_forReadBack")
    val dataOut = getDataOut()

    Files.writeString(ioPath, dataOut)

    val dataIn = Files.readString(ioPath)

    assertEquals("data read back does not match data written", dataOut, dataIn)
  }

  @Test def readStringUTF16LEUsingExplicitCharsetUTF16LE(): Unit = {
    val ioPath = getCleanIoPath("utf16LE_forReadBack")
    val dataOut = getDataOut()

    Files.writeString(ioPath, dataOut, StandardCharsets.UTF_16LE)

    val dataIn = Files.readString(ioPath, StandardCharsets.UTF_16LE)

    assertEquals("data read back does not match data written", dataOut, dataIn)
  }

  @Test def readStringUTF16BEUsingExplicitCharsetUTF16BE(): Unit = {
    val ioPath = getCleanIoPath("utf16BE_forReadBack")
    val dataOut = getDataOut()

    Files.writeString(ioPath, dataOut, StandardCharsets.UTF_16BE)

    val dataIn = Files.readString(ioPath, StandardCharsets.UTF_16BE)

    assertEquals("data read back does not match data written", dataOut, dataIn)
  }

  @Test def writeStringFromStringUsingDefaultCharsetUTF8(): Unit = {
    val ioPath = getCleanIoPath("utf8_file")
    val dataOut = getDataOut()

    /* Test, at the same time, correctness of both writing the file and of
     * using a variable number of arguments whilst doing so.
     *
     * Call without "OpenOption" third argument.
     * Java documention says this will cause options "CREATE",
     * "TRUNCATE_EXISTING", and "WRITE" to be used. CREATE and
     * WRITE are exercised here.
     */

    Files.writeString(ioPath, dataOut)

    verifySmallUtf8Payload(ioPath)
  }

  @Test def writeStringFromStringUsingExplicitCharsetUTF16LE(): Unit = {
    val ioPath = getCleanIoPath("utf16LE_file")
    val dataOut = getDataOut()

   // format: off
    val expectedValues = Array(
	0xAC,
	0x20,
	0xA3,
	0x00,
	0x24,
	0x00,
    ).map (_.toByte)
   // format: on

    /* Euro, Pound, and Dollar characters are all 2 bytes in UTF-16.
     * End-of-line newline will be 2 bytes. Windows carriage-return (CR),
     * if present, will be another two bytes.
     */
    val expectedDataLength = expectedValues.size + (EOLlen * 2)

    /* Write String out in "odd, non-standard 16LE" format instead of
     * Java standard 16BE just to shake things up and invite faults.
     */
    Files.writeString(ioPath, dataOut, StandardCharsets.UTF_16LE)

    val bytesRead = Files.readAllBytes(ioPath)

    assertEquals("bytes read", expectedDataLength, bytesRead.length)

    for (j <- 0 until (expectedValues.length)) {
      assertEquals(
        s"write/read mismatch at index ${j}",
        expectedValues(j),
        bytesRead(j)
      )
    }

    verifyUtf16LeEOL(bytesRead)
  }

  @Test def writeStringFromStringUsingExplicitCharsetUTF16BE(): Unit = {
    val ioPath = getCleanIoPath("utf16BE_file")
    val dataOut = getDataOut()

   // format: off
    val expectedValues = Array(
	0x20,
	0xAC,
	0x00,
	0xA3,
	0x00,
	0x24,
    ).map (_.toByte)
   // format: on

    /* Euro, Pound, and Dollar characters are all 2 bytes in UTF-16.
     * End-of-line newline will be 2 bytes. Windows carriage-return (CR),
     * if present, will be another two bytes.
     */
    val expectedDataLength = expectedValues.size + (EOLlen * 2)

    // Write as represented in Java Characters, Big Endian or network order.
    Files.writeString(ioPath, dataOut, StandardCharsets.UTF_16BE)

    val bytesRead = Files.readAllBytes(ioPath)

    assertEquals("bytes read", expectedDataLength, bytesRead.length)

    for (j <- 0 until (expectedValues.length)) {
      assertEquals(
        s"write/read mismatch at index ${j}",
        expectedValues(j),
        bytesRead(j)
      )
    }

    verifyUtf16BeEOL(bytesRead)
  }

  @Test def writeStringFromCharBufferWrapSmallArray(): Unit = {
    val ioPath = getCleanIoPath("CharBufferWrapSmallArray")
    val dataOut = getDataOut()

    val output = CharBuffer.wrap(dataOut.toArray[Char])
    Files.writeString(ioPath, output)

    verifySmallUtf8Payload(ioPath)
  }

  @Test def writeStringFromCharBufferWrapSmallString(): Unit = {
    val ioPath = getCleanIoPath("CharBufferWrapSmallString")
    val dataOut = getDataOut()

    val output = CharBuffer.wrap(dataOut)
    Files.writeString(ioPath, output)

    verifySmallUtf8Payload(ioPath)
  }

  @Test def writeStringFromStringBuilderSmall(): Unit = {
    val ioPath = getCleanIoPath("StringBuilderSmall")
    val dataOut = getDataOut()

    val output = new jl.StringBuilder(dataOut)
    Files.writeString(ioPath, output)

    verifySmallUtf8Payload(ioPath)
  }

  @Test def writeStringFromStringBufferSmall(): Unit = {
    val ioPath = getCleanIoPath("StringBufferSmall")
    val dataOut = getDataOut()

    val output = new jl.StringBuffer(dataOut)
    Files.writeString(ioPath, output)

    verifySmallUtf8Payload(ioPath)
  }

  @Test def writeStringFromStringUsingOptionArg(): Unit = {
    /* Check that both writeString() variants properly pass an explicitly
     * specified file open attributes varargs argument.
     */
    val ioPath = getCleanIoPath("utf8_forCreateNewOptionArg")
    val dataOut = getDataOut()

    Files.createFile(ioPath)

    assertThrows(
      classOf[FileAlreadyExistsException],
      Files.writeString(
        ioPath,
        dataOut,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE_NEW
      )
    )

    assertThrows(
      classOf[FileAlreadyExistsException],
      Files.writeString(
        ioPath,
        dataOut,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE_NEW
      )
    )
  }

  /* This Test is next to essential for both development & debugging.
   * It is Ignore'd for normal Continuous Integration (CI) because, by
   * its very purpose it creates and verifies a larger-than-a-breadbox
   * file. In CI, this takes CPU & IO resources and has the possibility
   * of leaving a large otherwise useless file lying around.
   *
   * The SN standard is to use "/* */" for multiline comments, as is done here.
   * If someone if offended by the @Ignore, they could convert the contents
   * below to "//" line comments then enclose the region in a "/* */" pair.
   */
  @Ignore
  @Test def largeWriteStringThenReadString(): Unit = {
    /* Same logic as small readString() tests but with enough data to
     * exercise any internal buffering.
     */

    val ioPath = getCleanIoPath("LargeFile")

    /* Use an unexpected string size to try to reveal defects in any
     * underlying buffering.
     *
     * This test does not, and should not, know the sizes of any
     * internal buffers used by writeString() and readString().
     * If any such exist, they are likely to have a power-of-two size
     * and more likely to have an even size. Developers like even sizes.
     *
     * Add an odd, and preferably prime, increment to a "reasonable" string
     * size to almost certainly force any last buffer to be partial.
     */

    /* For a String filled with all but two 1-byte UTF-8 and two 2-byte
     * UTF-8 characters, expect an actual file size of
     * (40960 + 2 + 41) = 41003 bytes.
     */
    val maxStringSize = (40 * 1024) + 41

    val startChar = '\u03B1' // Greek lowercase alpha; file bytes 0xCE 0xB1
    val endChar = '\u03A9' // Greek uppercase omega; file bytes 0xCE 0xA9

    val dataOut = getLargeDataOut(maxStringSize, startChar, endChar)

    Files.writeString(ioPath, dataOut)

    val dataIn = Files.readString(ioPath)

    assertEquals("Unexpected dataIn size", maxStringSize, dataIn.size)
    assertEquals(
      "dataOut & dataIn sizes do not match",
      dataOut.size,
      dataIn.size
    )

    assertEquals("Unexpected first dataIn character", startChar, dataIn(0))
    assertEquals(
      "Unexpected last dataIn character",
      endChar,
      dataIn(maxStringSize - 1)
    )

    assertEquals("data read back does not match data written", dataOut, dataIn)
  }
}

object FilesTestOnJDK11 {
  private var orgPath: Path = _
  private var workPath: Path = _

  final val testsuitePackagePrefix = "org.scalanative."

  val EOL = System.getProperty("line.separator") // end-of-line
  val EOLlen = EOL.size

  private def getCleanIoPath(fileName: String): Path = {
    val ioPath = workPath.resolve(fileName)
    Files.deleteIfExists(ioPath)
    ioPath
  }

  def getDataOut(): String = {
    /* Euro sign, Pound sign, dollarSign
     *  Ref: https://www.compart.com/en/unicode
     */

    "\u20AC\u00A3\u0024" + EOL // ensure file ends with OS end-of-line.
  }

  def getLargeDataOut(maxSize: Int, startChar: Char, endChar: Char): String = {
    val sb = new StringBuilder(maxSize)
    sb.insert(0, startChar)
    sb.setLength(maxSize - 1) // extend to size, filled with NUL characters
    sb.append(endChar) // final size should be maxSize
    // leave the string _without_ a terminal line.separator to trip things up.
    sb.toString()
  }

  def verifyUtf8EOL(bytes: Array[Byte]): Unit = {
    if (EOLlen == 2)
      assertEquals("Expected Windows CR", '\r', bytes(bytes.length - EOLlen))

    assertEquals("Expected newline", '\n', bytes(bytes.length - 1))
  }

  def verifyUtf16LeEOL(bytes: Array[Byte]): Unit = {
    if (EOLlen == 2)
      assertEquals("Expected Windows CR", '\r', bytes(bytes.length - 4))

    assertEquals("Expected newline", '\n', bytes(bytes.length - 2))
  }

  def verifyUtf16BeEOL(bytes: Array[Byte]): Unit = {
    if (EOLlen == 2)
      assertEquals("Expected Windows CR", '\r', bytes(bytes.length - 3))

    assertEquals("Expected newline", '\n', bytes(bytes.length - 1))
  }

  def verifySmallUtf8Payload(ioPath: Path): Unit = {
   // format: off
    val expectedValues = Array(
	0xE2, 0x82, 0xAC, // EURO SIGN
	0xC2, 0xA3,       // POUND (sterling) SIGN
	0x24,             // DOLLAR SIGN
    ).map (_.toByte)
   // format: on

    val expectedDataLength = expectedValues.size + EOLlen

    val bytesRead = Files.readAllBytes(ioPath)
    assertEquals("bytes read", expectedDataLength, bytesRead.length)

    for (j <- 0 until (expectedValues.length)) {
      assertEquals(
        s"write/read mismatch at index ${j}",
        expectedValues(j),
        bytesRead(j)
      )
    }

    verifyUtf8EOL(bytesRead)
  }

  @BeforeClass
  def beforeClass(): Unit = {
    /* Scala package statement does not allow "-", so the testsuite
     * packages are all "scalanative", not the "scala-native" used
     * in distribution artifacts or the name of the GitHub repository.
     */
    orgPath = Files.createTempDirectory(s"${testsuitePackagePrefix}testsuite")

    val tmpPath =
      orgPath.resolve(s"javalib/nio/file/${this.getClass().getSimpleName()}")
    workPath = Files.createDirectories(tmpPath)
  }

  @AfterClass
  def afterClass(): Unit = {
    // Delete items created by this test.

    // Avoid blind "rm -r /" and other oops! catastrophes.
    if (!orgPath.toString().contains(s"${testsuitePackagePrefix}"))
      fail(s"Refusing recursive delete of unknown path: ${orgPath}")

    // Avoid resize overhead; 64 is a high guess. deque will grow if needed.
    val stack = new ju.ArrayDeque[Path](64)
    val stream = Files.walk(orgPath)

    try {
      // Delete Files; start with deepest & work upwards to beginning of walk.
      stream.forEach(stack.addFirst(_)) // push() Path
      stack.forEach(Files.delete(_)) // pop() a Path then delete it's File.
    } finally {
      stream.close()
    }
  }
}
