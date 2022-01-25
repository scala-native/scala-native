package java.net

// Ported from Scala.js, revision 1337656, dated 4 Jun 2020

import java.io.UnsupportedEncodingException
import java.nio.{CharBuffer, ByteBuffer}
import java.nio.charset.{Charset, MalformedInputException}

object URLDecoder {

  @Deprecated
  def decode(s: String): String = decodeImpl(s, Charset.defaultCharset())

  def decode(s: String, enc: String): String = {
    /* An exception is thrown only if the
     * character encoding needs to be consulted
     */
    lazy val charset = {
      if (!Charset.isSupported(enc))
        throw new UnsupportedEncodingException(enc)
      else
        Charset.forName(enc)
    }

    decodeImpl(s, charset)
  }

  private def throwIllegalHex() = {
    throw new IllegalArgumentException(
      "URLDecoder: Illegal hex characters in escape (%) pattern"
    )
  }

  private def decodeImpl(s: String, charset: => Charset): String = {
    val len = s.length
    lazy val charsetDecoder = charset.newDecoder()

    lazy val byteBuffer = ByteBuffer.allocate(len / 3)
    val charBuffer = CharBuffer.allocate(len)

    def throwIllegalHex() = {
      throw new IllegalArgumentException(
        "URLDecoder: Illegal hex characters in escape (%) pattern"
      )
    }

    val in = CharBuffer.wrap(s)
    while (in.hasRemaining()) {
      in.get() match {
        case '+' => charBuffer.append(' ')

        case '%' if in.remaining() < 2 =>
          throwIllegalHex()

        case '%' =>
          val decoder = charsetDecoder
          val buffer = byteBuffer
          buffer.clear()
          decoder.reset()
          var inEscape = true
          while (in.remaining() >= 2 && inEscape) {
            val c1 = Character.digit(in.get(), 16)
            val c2 = Character.digit(in.get(), 16)
            if (c1 < 0 || c2 < 0)
              throwIllegalHex()

            buffer.put(((c1 << 4) + c2).toByte)
            // Peak next char to check if it's also an escape.
            // If so drop next char to allign with state before loop
            inEscape = in.remaining() > 2 && in.get(in.position()) == '%'
            if (inEscape) in.get()
          }

          buffer.flip()
          val decodeResult = decoder.decode(buffer, charBuffer, true)
          val flushResult = decoder.flush(charBuffer)

          if (decodeResult.isError() || flushResult.isError())
            throwIllegalHex()

        case c =>
          charBuffer.append(c)
      }
    }

    charBuffer.flip()
    charBuffer.toString
  }
}
