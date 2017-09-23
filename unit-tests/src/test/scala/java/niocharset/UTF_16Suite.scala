package java.niocharset

import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.Charset
import java.niocharset.BaseCharset.{BufferPart, OutPart}

object UTF_16Suite extends BaseUTF_16(Charset.forName("UTF-16")) {
  def BigEndianBOM: ByteBuffer =
  ByteBuffer.wrap(Array(0xfe.toByte, 0xff.toByte))

  override protected def testDecode(in: ByteBuffer)(
    outParts: OutPart[CharBuffer]*): Unit = {
    // Without BOM, big endian is assumed
    super.testDecode(in)(outParts: _*)

    // With BOM, big endian
    val inWithBOM = ByteBuffer.allocate(2+in.remaining)
    inWithBOM.put(BigEndianBOM).put(in).flip()
    super.testDecode(inWithBOM)(outParts: _*)

    // With BOM, little endian
    UTF_16LESuite.flipByteBuffer(inWithBOM)
    super.testDecode(inWithBOM)(outParts: _*)
  }

  override protected def testEncode(in: CharBuffer)(
    outParts: OutPart[ByteBuffer]*): Unit = {
    if (in.remaining == 0) super.testEncode(in)(outParts: _*)
    else super.testEncode(in)(BufferPart(BigEndianBOM) +: outParts: _*)
  }
}
