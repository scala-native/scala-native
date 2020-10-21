package java.io

import scala.util.Try

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

class RandomAccessFileTest {

  var raf: RandomAccessFile = _

  @Before
  def setUp(): Unit = {
    // some tests do not use this
    val file = File.createTempFile("tmp", "")
    raf = new RandomAccessFile(file, "rw")
  }

  @After
  def tearDown(): Unit = {
    try raf.close()
    catch {
      case _: Throwable =>
    }
  }

  @Test def creatingRandomAccessFileWithInvalidModeThrowsException(): Unit = {
    assertThrows(classOf[IllegalArgumentException],
                 new RandomAccessFile("file", "foo"))
  }

  @Test def creatingRandomAccessFileWithModeReadOnNonExistentFileThrowsException()
      : Unit = {
    val file = new File("i-dont-exist")
    assertFalse(file.exists)
    assertThrows(classOf[FileNotFoundException],
                 new RandomAccessFile(file, "r"))
  }

  @Test def validFileDescriptorAndSyncSuccess(): Unit = {
    val file = File.createTempFile("raffdtest", "")
    // assign to var for @After to close
    raf = new RandomAccessFile(file, "r")
    val fd = raf.getFD
    assertTrue(fd.valid())
    assertTrue(Try(fd.sync()).isSuccess)
  }

  @Test def canWriteAndReadBoolean(): Unit = {
    raf.writeBoolean(true)
    raf.writeBoolean(false)
    raf.seek(0)
    assertTrue(raf.readBoolean() == true)
    assertTrue(raf.readBoolean() == false)
  }

  @Test def canWriteAndReadByte(): Unit = {
    val value: Byte = 42
    raf.writeByte(value)
    raf.seek(0)
    assertTrue(raf.readByte() == value)
  }

  @Test def canWriteAndReadUnsignedByte(): Unit = {
    val value: Int = 0xFE
    raf.writeByte(value.toByte)
    raf.seek(0)
    assertTrue(raf.readUnsignedByte() == value)
  }

  @Test def canWriteAndReadChar(): Unit = {
    val value = 'c'
    raf.writeChar(value)
    raf.seek(0)
    assertTrue(raf.readChar() == value)
  }

  @Test def canWriteAndReadDouble(): Unit = {
    val value: Double = 42.42
    raf.writeDouble(value)
    raf.seek(0)
    assertTrue(raf.readDouble() == value)
  }

  @Test def canWriteAndReadFloat(): Unit = {
    val value: Float = 42.42f
    raf.writeFloat(value)
    raf.seek(0)
    assertTrue(raf.readFloat() == value)
  }

  @Test def canWriteAndReadInt(): Unit = {
    val value: Int = 42
    raf.writeInt(value)
    raf.seek(0)
    assertTrue(raf.readInt() == value)
  }

  @Test def canWriteAndReadLineWithTerminatorLf(): Unit = {
    val line = "Hello, world!"
    raf.writeChars(line + '\n')
    raf.seek(0)
    assertTrue(raf.readLine() == line)
  }

  @Test def canWriteAndReadLineWithTerminatorCr(): Unit = {
    val line = "Hello, world!"
    raf.writeChars(line + '\r')
    raf.seek(0)
    assertTrue(raf.readLine() == line)
  }

  @Test def canWriteAndReadLineWithTerminatorCrLf(): Unit = {
    val line = "Hello, world!"
    raf.writeChars(line + "\r\n")
    raf.seek(0)
    assertTrue(raf.readLine() == line)
  }

  @Test def canWriteAndReadLong(): Unit = {
    val value: Long = 4631166901565532406L
    raf.writeLong(value)
    raf.seek(0)
    assertTrue(raf.readLong() == value)
  }

  @Test def canWriteAndReadModifiedUtfString(): Unit = {
    val value = "Hello, world!" + '\u0102' + '\u0802'
    raf.writeUTF(value)
    raf.seek(0)
    assertTrue(raf.readUTF() == value)
  }
}
