package javalib.nio

import java.nio._
import java.nio.channels.FileChannel.MapMode
import java.nio.channels.NonWritableChannelException

import org.junit.{Test, Before}
import org.junit.Assert._
import scala.scalanative.junit.utils.AssertThrows.assertThrows

import java.io._

class MappedByteBufferTest {

  def writeBytes(buffer: ByteBuffer, count: Int): Unit = {
    for (i <- 0 until count) {
      val byte = ('A'.toByte + i / 10).toByte
      buffer.put(byte)
    }
  }

  def withTemporaryFile(mode: String)(fn: RandomAccessFile => Unit): Unit = {
    val file = File.createTempFile("test", ".tmp")
    val memoryMappedFile = new RandomAccessFile(file, mode)
    fn(memoryMappedFile)
  }

  @Test def readWriteOnReadOnlyFileThrowsException(): Unit = {
    withTemporaryFile("r") { file: RandomAccessFile =>
      assertThrows(
        classOf[NonWritableChannelException],
        file.getChannel.map(MapMode.READ_WRITE, 0, 100)
      )
      file.getChannel().close()
    }
  }

  @Test def readOnlyMappedBuffer(): Unit = {
    withTemporaryFile("rw") { file: RandomAccessFile =>
      val count = 100
      for (i <- 0 until count) {
        val byte = ('A'.toByte + i / 10).toByte
        file.writeByte(byte)
      }
      file.seek(0)
      val ch = file.getChannel()
      val mapped = ch.map(MapMode.READ_ONLY, 0, count)
      ch.close()

      for (i <- 0 until count) {
        val expected = ('A'.toByte + i / 10).toByte
        assertEquals(f"Byte #${i}", mapped.get(), expected)
      }

      mapped.position(0)
      assertThrows(classOf[ReadOnlyBufferException], mapped.put(0.toByte))
    }
  }

  @Test def readWriteMappedBuffer(): Unit = {
    withTemporaryFile("rw") { file: RandomAccessFile =>
      val count = 100
      val ch = file.getChannel()
      val mapped = ch.map(MapMode.READ_WRITE, 0, count)

      writeBytes(mapped, count)
      mapped.force()

      assertEquals("File size after mapping", file.length(), count)

      for (i <- 0 until count) {
        val expected = ('A'.toByte + i / 10).toByte
        assertEquals(f"Byte #${i}", file.read(), expected)
      }
      ch.close()
    }
  }

  @Test def privateMappedBuffer(): Unit = {
    withTemporaryFile("rw") { file: RandomAccessFile =>
      val count = 100
      val mapped = file.getChannel.map(MapMode.PRIVATE, 0, count)

      writeBytes(mapped, count)
      mapped.force()

      assertEquals(file.length(), 100)

      for (i <- 0 until count) {
        assertEquals(file.read(), 0)
      }
    }
  }

  // Apache Harmony tests
  var tmpFile: File = null
  var emptyFile: File = null

  // Ported from Apache Harmony
  @Before def setUp(): Unit = {
    // Create temp file with 26 bytes and 5 ints
    tmpFile = File.createTempFile("harmony", "test");
    tmpFile.deleteOnExit();
    val fileOutputStream = new FileOutputStream(tmpFile);
    val fileChannel = fileOutputStream.getChannel();
    val byteBuffer = ByteBuffer.allocateDirect(26 + 20);
    for (i <- 0 until 26) {
      byteBuffer.put(('A' + i).toByte);
    }
    for (i <- 0 until 5) {
      byteBuffer.putInt(i + 1);
    }
    byteBuffer.rewind();
    fileChannel.write(byteBuffer);
    fileChannel.close();
    fileOutputStream.close();

    emptyFile = File.createTempFile("harmony", "test");
    emptyFile.deleteOnExit();
  }

  // Ported from Apache Harmony
  @Test def testForce(): Unit = {
    // buffer was not mapped in read/write mode
    val fileInputStream = new FileInputStream(tmpFile);
    val fileChannelRead = fileInputStream.getChannel();
    val mmbRead =
      fileChannelRead.map(MapMode.READ_ONLY, 0, fileChannelRead.size());

    mmbRead.force();

    val inputStream = new FileInputStream(tmpFile);
    val fileChannelR = inputStream.getChannel();
    val resultRead =
      fileChannelR.map(MapMode.READ_ONLY, 0, fileChannelR.size());

    // If this buffer was not mapped in read/write mode,
    // then invoking this method has no effect.
    assertEquals(
      "Invoking force() should have no effect when this buffer was not mapped in read/write mode",
      mmbRead,
      resultRead
    );

    // Buffer was mapped in read/write mode
    val randomFile = new RandomAccessFile(tmpFile, "rw");
    val fileChannelReadWrite = randomFile.getChannel();
    val mmbReadWrite = fileChannelReadWrite.map(
      MapMode.READ_WRITE,
      0,
      fileChannelReadWrite.size()
    );

    mmbReadWrite.put('o'.toByte);
    mmbReadWrite.force();

    val random = new RandomAccessFile(tmpFile, "rw");
    val fileChannelRW = random.getChannel();
    val resultReadWrite =
      fileChannelRW.map(MapMode.READ_WRITE, 0, fileChannelRW.size());

    // Invoking force() will change the buffer
    assertFalse(mmbReadWrite.equals(resultReadWrite));

    fileChannelRead.close();
    fileChannelR.close();
    fileChannelReadWrite.close();
    fileChannelRW.close();
  }

  // Ported from Apache Harmony
  @Test def testLoad(): Unit = {
    val fileInputStream = new FileInputStream(tmpFile);
    val fileChannelRead = fileInputStream.getChannel();
    val mmbRead =
      fileChannelRead.map(MapMode.READ_ONLY, 0, fileChannelRead.size());

    assertEquals(mmbRead, mmbRead.load());

    val randomFile = new RandomAccessFile(tmpFile, "rw");
    val fileChannelReadWrite = randomFile.getChannel();
    val mmbReadWrite = fileChannelReadWrite.map(
      MapMode.READ_WRITE,
      0,
      fileChannelReadWrite.size()
    );

    assertEquals(mmbReadWrite, mmbReadWrite.load());

    fileChannelRead.close();
    fileChannelReadWrite.close();
  }

  // Ported from Apache Harmony
  @Test def testPosition(): Unit = {
    val tmp = File.createTempFile("hmy", "tmp");
    tmp.deleteOnExit();
    val f = new RandomAccessFile(tmp, "rw");
    val ch = f.getChannel();
    val mbb = ch.map(MapMode.READ_WRITE, 0L, 100L);
    ch.close();

    mbb.putInt(1, 1);
    mbb.position(50);
    mbb.putInt(50);

    mbb.flip();
    mbb.get();
    assertEquals(1, mbb.getInt());

    mbb.position(50);
    assertEquals(50, mbb.getInt());
  }

}
