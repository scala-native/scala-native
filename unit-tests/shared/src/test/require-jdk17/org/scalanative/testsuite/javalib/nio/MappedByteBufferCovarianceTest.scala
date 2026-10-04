package org.scalanative.testsuite.javalib.nio

import java.io.{File, RandomAccessFile}
import java.nio.channels.FileChannel.MapMode
import java.nio.{
  ByteBuffer, ByteOrder, CharBuffer, InvalidMarkException, MappedByteBuffer,
  ReadOnlyBufferException
}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class MappedByteBufferCovarianceTest {
  private def withMapping(mode: MapMode = MapMode.READ_WRITE)(
      test: MappedByteBuffer => Unit
  ): Unit = {
    val path = File.createTempFile("mapped-covariance", ".tmp")
    val file = new RandomAccessFile(path, "rw")
    try {
      file.write(Array[Byte](0, 1, 2, 3, 4, 5, 6, 7))
      test(file.getChannel().map(mode, 0, 8))
    } finally {
      file.close()
      path.delete()
    }
  }

  @Test def duplicatePreservesStateAndSharesContent(): Unit = withMapping() {
    buffer =>
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      buffer.position(2).mark().position(3).limit(6)
      val duplicate: MappedByteBuffer = buffer.duplicate()
      assertEquals(8, duplicate.capacity())
      assertEquals(3, duplicate.position())
      assertEquals(6, duplicate.limit())
      assertEquals(ByteOrder.BIG_ENDIAN, duplicate.order())
      assertTrue(duplicate.isDirect())
      duplicate.reset()
      assertEquals(2, duplicate.position())
      assertEquals(3, buffer.position())
      duplicate.put(2, 42.toByte)
      assertEquals(42, buffer.get(2).toInt)
      val parent: ByteBuffer = buffer
      assertTrue(parent.duplicate().isInstanceOf[MappedByteBuffer])
  }

  @Test def sliceSharesRemainingContent(): Unit = withMapping() { buffer =>
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    buffer.position(2).mark().limit(6)
    val slice: MappedByteBuffer = buffer.slice()
    assertEquals(0, slice.position())
    assertEquals(4, slice.limit())
    assertEquals(4, slice.capacity())
    assertEquals(ByteOrder.BIG_ENDIAN, slice.order())
    assertTrue(slice.isDirect())
    assertThrows(classOf[InvalidMarkException], slice.reset())
    slice.put(0, 42.toByte)
    assertEquals(42, buffer.get(2).toInt)
    assertEquals(2, buffer.position())
    val parent: ByteBuffer = buffer
    assertTrue(parent.slice().isInstanceOf[MappedByteBuffer])
  }

  @Test def indexedSliceUsesAbsoluteIndex(): Unit = withMapping() { buffer =>
    buffer.position(4).limit(6)
    val slice: MappedByteBuffer = buffer.slice(1, 3)
    assertEquals(0, slice.position())
    assertEquals(3, slice.limit())
    assertEquals(3, slice.capacity())
    assertEquals(1, slice.get(0).toInt)
    slice.put(2, 42.toByte)
    assertEquals(42, buffer.get(3).toInt)
    assertEquals(4, buffer.position())
    assertThrows(classOf[IndexOutOfBoundsException], buffer.slice(-1, 1))
    assertThrows(classOf[IndexOutOfBoundsException], buffer.slice(5, 2))
    assertEquals(0, buffer.slice(6, 0).capacity())
    val parent: ByteBuffer = buffer
    assertTrue(parent.slice(1, 3).isInstanceOf[MappedByteBuffer])
  }

  @Test def compactReturnsSameMappingAndDiscardsMark(): Unit = withMapping() {
    buffer =>
      buffer.position(2).mark().limit(6)
      val compacted: MappedByteBuffer = buffer.compact()
      assertSame(buffer, compacted)
      assertEquals(4, buffer.position())
      assertEquals(8, buffer.limit())
      assertEquals(2, buffer.get(0).toInt)
      assertEquals(5, buffer.get(3).toInt)
      assertThrows(classOf[InvalidMarkException], buffer.reset())
      val parent: ByteBuffer = buffer
      assertSame(buffer, parent.compact())
  }

  @Test def readOnlyViewsPreserveProtection(): Unit =
    withMapping(MapMode.READ_ONLY) { buffer =>
      val duplicate: MappedByteBuffer = buffer.duplicate()
      val slice: MappedByteBuffer = buffer.slice()
      val indexed: MappedByteBuffer = buffer.slice(1, 3)
      for (view <- Array(duplicate, slice, indexed)) {
        assertTrue(view.isReadOnly())
        assertThrows(classOf[ReadOnlyBufferException], view.put(0, 42.toByte))
        assertThrows(classOf[ReadOnlyBufferException], view.compact())
      }
      assertThrows(classOf[ReadOnlyBufferException], buffer.compact())
    }

  @Test def compactSliceRespectsMappingOffset(): Unit = withMapping() {
    buffer =>
      val slice: MappedByteBuffer = buffer.slice(2, 5)
      slice.position(1).limit(5)
      assertSame(slice, slice.compact())
      assertEquals(4, slice.position())
      assertEquals(5, slice.limit())
      assertEquals(1, buffer.get(1).toInt)
      assertEquals(3, buffer.get(2).toInt)
      assertEquals(6, buffer.get(5).toInt)
      assertEquals(7, buffer.get(7).toInt)
      val empty: MappedByteBuffer = buffer.slice(8, 0)
      assertSame(empty, empty.compact())
      assertEquals(0, empty.position())
      assertEquals(0, empty.limit())
      buffer.position(8).mark()
      assertSame(buffer, buffer.compact())
      assertEquals(0, buffer.position())
      assertEquals(8, buffer.limit())
      assertThrows(classOf[InvalidMarkException], buffer.reset())
  }

  @Test def mappedCharViewHasCovariantSubSequence(): Unit = withMapping() {
    buffer =>
      val chars = buffer.asCharBuffer()
      chars.put("abcd").position(1)
      val sub: CharBuffer = chars.subSequence(1, 3)
      assertEquals("cd", sub.toString())
      sub.put(2, 'X')
      assertEquals('X', chars.get(2))
      val parent: CharSequence = chars
      assertEquals("Xd", parent.subSequence(1, 3).toString())
      assertEquals(1, chars.position())
  }
}
