package org.scalanative.testsuite.javalib.nio

import java.nio._

// Ported from Scala.js

import BufferFactory.ByteBufferFactory

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

// Most of these tests require only JDK9 (alignedOffset) or JDK13 (bulk get/put)
// We put as much of JDK8 incompatible tests in a single file to not execute BaseBufferTest's multiple times
abstract class ByteBufferTestOnJDK16 extends BaseBufferTest {
  type Factory = BufferFactory.ByteBufferFactory

  import factory._

  @Test def absoluteBulkGet(): Unit = {
    val buf = withContent(10, elemRange(0, 10): _*)
    val arr = arrayOfElemType(capacity = 8)
    buf.position(1)
    assertSame(buf, buf.get(4, arr, 2, 4))
    assertEquals(1, buf.position())
    arr.zipWithIndex.foreach {
      case (elem, idx @ (0 | 1 | 6 | 7)) => assertEquals(elemFromInt(0), elem)
      case (elem, idx) => assertEquals(elemFromInt(idx + 2), elem)
    }
    assertThrows(
      "negative idx",
      classOf[IndexOutOfBoundsException],
      buf.get(-1, arr, 2, 4)
    )
    assertThrows(
      "offset+length < limit",
      classOf[IndexOutOfBoundsException],
      buf.get(4, arr, 8, 4)
    )
    assertThrows(
      "negative length",
      classOf[IndexOutOfBoundsException],
      buf.get(4, arr, 2, -1)
    )

    buf.limit(4)
    assertThrows(
      "idx > limit",
      classOf[IndexOutOfBoundsException],
      buf.get(5, arr, 2, 4)
    )
  }

  @Test def absoluteBulkGet2(): Unit = {
    val buf = withContent(10, elemRange(0, 10): _*)
    val arr = arrayOfElemType(capacity = 6)
    buf.position(1)
    assertSame(buf, buf.get(4, arr))
    assertEquals(1, buf.position())
    arr.zipWithIndex.foreach {
      case (elem, idx) => assertEquals(elemFromInt(idx + 4), elem)
    }
    assertThrows(
      "negative idx",
      classOf[IndexOutOfBoundsException],
      buf.get(-1, arr)
    )

    buf.limit(4)
    assertThrows(
      "idx > limit",
      classOf[IndexOutOfBoundsException],
      buf.get(5, arr)
    )
  }

  @Test def absoluteBulkPut(): Unit = {
    val buf = allocBuffer(10)
    val arr = arrayOfElemType(6)
    arr.indices.foreach(idx => arr(idx) = elemFromInt(idx))
    if (!createsReadOnly) {
      buf.put(4, arr, 2, 3)
      assertEquals(0, buf.position())
      arr.indices.foreach { idx =>
        val elem = buf.get(idx)
        if (idx < 4 || idx > 7) assertEquals(elemFromInt(0), elem)
        else assertEquals(elemFromInt(idx - 2), elem)
      }
      assertThrows(classOf[IndexOutOfBoundsException], buf.put(-1, arr, 2, 3))
      assertThrows(classOf[IndexOutOfBoundsException], buf.put(14, arr, 2, 3))

      buf.limit(4)
      assertThrows(classOf[IndexOutOfBoundsException], buf.put(4, arr, 2, 3))
    } else {
      assertThrows(classOf[ReadOnlyBufferException], buf.put(2, arr, 2, 3))
      assertEquals(elemFromInt(0), buf.get(2))
      assertEquals(0, buf.position())

      assertThrows(classOf[ReadOnlyBufferException], buf.put(-2, arr, 2, 3))
      assertThrows(classOf[ReadOnlyBufferException], buf.put(12, arr, 2, 3))
    }
  }

  @Test def absoluteBulkPut2(): Unit = {
    val buf = allocBuffer(10)
    val arr = arrayOfElemType(6)
    arr.indices.foreach(idx => arr(idx) = elemFromInt(idx))
    if (!createsReadOnly) {
      buf.put(4, arr)
      assertEquals(0, buf.position())
      arr.indices.foreach { idx =>
        val elem = buf.get(idx)
        if (idx < 4) assertEquals(elemFromInt(0), elem)
        else assertEquals(elemFromInt(idx - 4), elem)
      }
      assertThrows(classOf[IndexOutOfBoundsException], buf.put(-1, arr))
      assertThrows(classOf[IndexOutOfBoundsException], buf.put(14, arr))

      buf.limit(4)
      assertThrows(classOf[IndexOutOfBoundsException], buf.put(4, arr))
    } else {
      assertThrows(classOf[ReadOnlyBufferException], buf.put(2, arr))
      assertEquals(elemFromInt(0), buf.get(2))
      assertEquals(0, buf.position())

      assertThrows(classOf[ReadOnlyBufferException], buf.put(-2, arr))
      assertThrows(classOf[ReadOnlyBufferException], buf.put(12, arr))
    }
  }

  @Test def putBuffer(): Unit = {
    val bufSize = 10
    val srcSize = 8
    val buf = allocBuffer(bufSize)
    val src = withContent(srcSize, elemRange(0, srcSize): _*)
    val srcOffset = 1
    src.position(srcOffset)
    assertEquals(srcOffset, src.position())
    if (!createsReadOnly) {
      assertSame(buf, buf.put(src))
      (0 until bufSize).foreach { n =>
        val elem = buf.get(n)
        if (n >= srcSize - 1) assertEquals(elemFromInt(0), elem)
        else assertEquals(s"$n", elemFromInt(n + srcOffset), elem)
      }
      assertEquals(buf.position(), srcSize - srcOffset)
      assertEquals(srcSize, src.position())
    } else {
      assertThrows(
        classOf[ReadOnlyBufferException],
        buf.put(src)
      )
      assertEquals(0, buf.position())
      assertEquals(elemFromInt(0), buf.get(0))

      buf.position(8)
      assertEquals(8, buf.position())
      assertEquals(elemFromInt(0), buf.get(8))
    }
  }

  @Test def putAbsoluteBuffer(): Unit = {
    val bufSize = 10
    val srcSize = 8
    val buf = allocBuffer(bufSize)
    val src = withContent(srcSize, elemRange(0, srcSize): _*)
    val srcOffset = 1
    src.position(srcOffset)
    assertEquals(srcOffset, src.position())
    if (!createsReadOnly) {
      assertSame(buf, buf.put(2, src, 3, 4))
      (0 until bufSize).foreach { n =>
        val elem = buf.get(n)
        if (n < 2 || n >= 2 + 4) assertEquals(s"$n", elemFromInt(0), elem)
        else assertEquals(s"$n", elemFromInt(n + srcOffset), elem)
      }
      assertEquals("bufPositon", 0, buf.position())
      assertEquals("srcPosition", srcOffset, src.position())
    } else {
      assertThrows(
        classOf[ReadOnlyBufferException],
        buf.put(src)
      )
      assertEquals(0, buf.position())
      assertEquals(elemFromInt(0), buf.get(0))

      buf.position(8)
      assertEquals(8, buf.position())
      assertEquals(elemFromInt(0), buf.get(8))
    }
  }

  @Test def sliceAbsolute(): Unit = {
    val buf1 = withContent(10, elemRange(0, 10): _*)
    buf1.position(3)
    buf1.limit(7)
    buf1.mark()
    val buf2 = buf1.sliceChain(2, 4)
    assertEquals(0, buf2.position())
    assertEquals(4, buf2.limit())
    assertEquals(4, buf2.capacity())
    assertThrows(classOf[InvalidMarkException], buf2.reset())

    assertEquals(elemFromInt(3), buf2.get(1))

    buf2.position(2)
    assertEquals(3, buf1.position())

    if (!createsReadOnly) {
      buf2.put(89)
      assertEquals(elemFromInt(5), buf1.get(5))
      assertEquals(3, buf2.position())
      assertEquals(3, buf1.position())
    }

    assertThrows(classOf[IllegalArgumentException], buf2.limit(5))
    assertEquals(4, buf2.limit())

    buf2.limit(3)
    assertEquals(7, buf1.limit())

    if (!createsReadOnly) {
      buf1.put(3, 23)
      assertEquals(elemFromInt(2), buf2.get(0))
    }
  }

  @Test def testAlignmentOffset(): Unit = {
    for (input @ (capacity, unitSize) <- Seq(
          (0, 1),
          (5, 2),
          (10, 4),
          (20, 8),
          (30, 16)
        )) {
      val buf = allocBuffer(capacity)
      def getAlignmentOffset() = buf.alignmentOffset(capacity, unitSize)
      if (buf.isDirect() || unitSize <= 8) {
        val alignment = getAlignmentOffset()
        assertTrue(s"$input", alignment >= 0 && alignment < unitSize)
      } else {
        assertThrows(
          s"$input",
          classOf[UnsupportedOperationException],
          getAlignmentOffset()
        )
      }
    }
  }

  @Test def testAlignmentSlice(): Unit = {
    for (input @ (capacity, unitSize) <- Seq(
          (0, 1),
          (7, 2),
          (13, 4),
          (21, 8),
          (31, 16)
        )) {
      val buf = withContent(capacity, elemRange(1, capacity): _*)
      def getAlignmentSlice() = buf.alignedSlice(unitSize)
      if (buf.isDirect() || unitSize <= 8) {
        val alignBuf = getAlignmentSlice()
        assertEquals(0, alignBuf.limit() % unitSize)
        assertEquals(0, alignBuf.capacity() % unitSize)
        assertTrue(alignBuf.limit() <= buf.limit())
        assertTrue(alignBuf.capacity() <= buf.capacity())
        if (capacity > 0) {
          assertNotSame(buf, alignBuf)
          val offset = (0 until alignBuf.capacity())
            .find { n =>
              buf.get(n) == alignBuf.get(0)
            }
            .getOrElse { fail("Not matching elements in sliced buffer"); ??? }
          assertTrue(offset < unitSize)
          (0 until alignBuf.capacity()).foreach { n =>
            assertEquals(buf.get(n + offset), alignBuf.get(n))
          }
        }
      } else {
        assertThrows(
          s"$input",
          classOf[UnsupportedOperationException],
          getAlignmentSlice()
        )
      }
    }
  }

}

class AllocByteBufferTestOnJDK16 extends ByteBufferTestOnJDK16 {
  val factory: ByteBufferFactory =
    new ByteBufferFactories.AllocByteBufferFactory
}

class WrappedByteBufferTestOnJDK16 extends ByteBufferTestOnJDK16 {
  val factory: ByteBufferFactory =
    new ByteBufferFactories.WrappedByteBufferFactory
}

class ReadOnlyWrappedByteBufferTestOnJDK16 extends ByteBufferTestOnJDK16 {
  val factory: ByteBufferFactory =
    new ByteBufferFactories.ReadOnlyWrappedByteBufferFactory
}

class SlicedAllocByteBufferTestOnJDK16 extends ByteBufferTestOnJDK16 {
  val factory: ByteBufferFactory =
    new ByteBufferFactories.SlicedAllocByteBufferFactory
}

class AllocDirectByteBufferTestOnJDK16 extends ByteBufferTestOnJDK16 {
  val factory: ByteBufferFactory =
    new ByteBufferFactories.AllocDirectByteBufferFactory
}

class SlicedAllocDirectByteBufferTesOnJDK16 extends ByteBufferTestOnJDK16 {
  val factory: ByteBufferFactory =
    new ByteBufferFactories.SlicedAllocDirectByteBufferFactory
}
