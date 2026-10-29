package scala.scalanative.runtime.monitor

import java.nio.charset.{
  Charset, CharsetDecoder, CharsetEncoder, CoderResult, StandardCharsets
}
import java.nio.{ByteBuffer, CharBuffer}

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.unsafe._

class StringBackingArrayMonitorRootTest {
  // Keep the literal opaque so the optimizer cannot replace its value with a
  // newly allocated array when folding the getBytes call.
  @scala.noinline
  private def encode(string: String, charset: Charset): Unit = {
    string.getBytes(charset)
    ()
  }

  @Test def escapedLiteralBackingArrayMonitorSurvivesCollections(): Unit = {
    var backing: Array[Char] = null
    val charset = new Charset("X-BackingArrayMonitor", Array.empty[String]) {
      def contains(other: Charset): Boolean = other eq this
      def newDecoder(): CharsetDecoder = StandardCharsets.US_ASCII.newDecoder()
      def newEncoder(): CharsetEncoder = new CharsetEncoder(this, 1f, 1f) {
        protected def encodeLoop(
            in: CharBuffer,
            out: ByteBuffer
        ): CoderResult = {
          backing = in.array()
          while (in.hasRemaining() && out.hasRemaining())
            out.put(in.get().toByte)
          if (in.hasRemaining()) CoderResult.OVERFLOW else CoderResult.UNDERFLOW
        }
      }
    }
    encode("escaped static backing array", charset)
    assertNotNull(backing)
    val section = !BackingArraySections.strings()
    val address = castRawPtrToLong(castObjectToRawPtr(backing))
    assertTrue(
      "expected a static literal backing array",
      address >= section._2.toLong && address < section._3.toLong
    )
    ClassMonitorRootTest.assertStaticMonitorSurvives(backing)
  }
}

@extern
private[monitor] object BackingArraySections {
  @name("scalanative_static_strings_section")
  def strings(): Ptr[CStruct3[CString, CVoidPtr, CVoidPtr]] = extern
}
