package org.scalanative.testsuite.javalib.lang

import java.nio.charset.{
  Charset, CharsetDecoder, CharsetEncoder, CoderResult, StandardCharsets
}
import java.nio.{ByteBuffer, CharBuffer}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringBackingArrayTest {
  @Test def customEncoderCannotObtainStringBackingArray(): Unit = {
    var inspected = false
    val charset = new Charset("UTF-8", Array.empty[String]) {
      def contains(other: Charset): Boolean = other eq this
      def newDecoder(): CharsetDecoder = StandardCharsets.US_ASCII.newDecoder()
      def newEncoder(): CharsetEncoder = new CharsetEncoder(this, 1f, 1f) {
        protected def encodeLoop(
            in: CharBuffer,
            out: ByteBuffer
        ): CoderResult = {
          inspected = true
          Seq(in, in.duplicate(), in.slice()).foreach { buffer =>
            assertFalse(
              "encoder can access string backing array",
              buffer.hasArray()
            )
            assertThrows(classOf[UnsupportedOperationException], buffer.array())
          }
          while (in.hasRemaining() && out.hasRemaining())
            out.put(in.get().toByte)
          if (in.hasRemaining()) CoderResult.OVERFLOW else CoderResult.UNDERFLOW
        }
      }
    }
    Seq(
      "literal",
      "substring".substring(3, 7),
      new String(Array('h', 'e', 'a', 'p'))
    )
      .foreach { string =>
        inspected = false
        val bytes = string.getBytes(charset)
        assertTrue("custom encoder was not called", inspected)
        assertArrayEquals(string.toCharArray().map(_.toByte), bytes)
      }
  }
}
