package org.scalanative.testsuite.javalib.nio

import java.nio.{ByteBuffer, CharBuffer}

import org.junit.Assert._
import org.junit.Test

class CharBufferCovarianceTest {
  @Test def subSequenceThroughBufferAndCharSequence(): Unit = {
    val heap = CharBuffer.wrap("abcdef".toCharArray)
    val direct = ByteBuffer.allocateDirect(12).asCharBuffer()
    direct.put("abcdef").clear()
    val buffers = Array(
      heap,
      CharBuffer.wrap("abcdef".toCharArray).asReadOnlyBuffer(),
      direct,
      CharBuffer.wrap("abcdef")
    )
    for (buffer <- buffers) {
      buffer.position(1)
      buffer.limit(5)
      val sub: CharBuffer = buffer.subSequence(1, 3)
      assertEquals("cd", sub.toString())
      assertEquals(2, sub.position())
      assertEquals(4, sub.limit())
      assertEquals(buffer.capacity(), sub.capacity())
      assertEquals(buffer.isReadOnly(), sub.isReadOnly())
      assertEquals(buffer.isDirect(), sub.isDirect())
      val parent: CharSequence = buffer
      assertEquals("cd", parent.subSequence(1, 3).toString())
      assertEquals(1, buffer.position())
      assertEquals(5, buffer.limit())
      if (!sub.isReadOnly()) {
        sub.put(2, 'X')
        assertEquals('X', buffer.get(2))
      }
    }
  }
}
