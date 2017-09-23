package java.niocharset

import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.Charset
import java.niocharset.BaseCharset.{BufferPart, OutPart}

// Ported from Scala.js

object UTF_16LESuite extends BaseUTF_16(Charset.forName("UTF-16LE")) {
  override protected def testDecode(in: ByteBuffer)(
      outParts: OutPart[CharBuffer]*): Unit = {
    flipByteBuffer(in)
    super.testDecode(in)(outParts: _*)
  }

  override protected def testEncode(in: CharBuffer)(
      outParts: OutPart[ByteBuffer]*): Unit = {
    for (BufferPart(buf) <- outParts)
      flipByteBuffer(buf)
    super.testEncode(in)(outParts: _*)
  }

  /** Flips all pairs of bytes in a byte buffer, except a potential lonely
   *  last byte.
   */
  def flipByteBuffer(buf: ByteBuffer): Unit = {
    buf.mark()
    while (buf.remaining() >= 2) {
      val high = buf.get()
      val low  = buf.get()
      buf.position(buf.position - 2)
      buf.put(low)
      buf.put(high)
    }
    buf.reset()
  }
}
