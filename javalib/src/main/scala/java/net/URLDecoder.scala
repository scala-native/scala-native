package java.net

// Ported from Scala.js, commit: 617fc8e, dated 2022-03-07

import java.io.UnsupportedEncodingException
import java.nio.{CharBuffer, ByteBuffer}
import java.nio.charset.{Charset, CharsetDecoder}

object URLDecoder {

  @Deprecated
  def decode(s: String): String = decodeImpl(s, () => Charset.defaultCharset())

  def decode(s: String, enc: String): String = {
    decodeImpl(
      s,
      { () =>
        /* An exception is thrown only if the
         * character encoding needs to be consulted
         */
        if (!Charset.isSupported(enc))
          throw new UnsupportedEncodingException(enc)
        else
          Charset.forName(enc)
      }
    )
  }

  private def decodeImpl(s: String, getCharset: () => Charset): String = {
    val len = s.length
    val charBuffer = CharBuffer.allocate(len)

    // For charset-based decoding
    var decoder: CharsetDecoder = null
    var byteBuffer: ByteBuffer = null

    def throwIllegalHex() = {
      throw new IllegalArgumentException(
        "URLDecoder: Illegal hex characters in escape (%) pattern"
      )
    }

    var i = 0
    while (i < len) {
      s.charAt(i) match {
        case '+' =>
          charBuffer.append(' ')
          i += 1

        case '%' if i + 3 > len =>
          throwIllegalHex()

        case '%' =>
          if (decoder == null) { // equivalent to `byteBuffer == null`
            decoder = getCharset().newDecoder()
            byteBuffer = ByteBuffer.allocate(len / 3)
          } else {
            byteBuffer.clear()
            decoder.reset()
          }

          while (i + 3 <= len && s.charAt(i) == '%') {
            val c1 = Character.digit(s.charAt(i + 1), 16)
            val c2 = Character.digit(s.charAt(i + 2), 16)

            if (c1 < 0 || c2 < 0)
              throwIllegalHex()

            byteBuffer.put(((c1 << 4) + c2).toByte)
            i += 3
          }

          byteBuffer.flip()
          val decodeResult = decoder.decode(byteBuffer, charBuffer, true)
          val flushResult = decoder.flush(charBuffer)

          if (decodeResult.isError() || flushResult.isError())
            throwIllegalHex()

        case c =>
          charBuffer.append(c)
          i += 1
      }
    }

    charBuffer.flip()
    charBuffer.toString
  }
}
