package java.security

// Ported from Apache Harmony

import java.nio.ByteBuffer

abstract class MessageDigestSpi {
  protected def engineGetDigestLength(): Int =
    0

  protected def engineUpdate(input: Byte): Unit

  protected def engineUpdate(input: Array[Byte], offset: Int, len: Int): Unit

  protected def engineUpdate(input: ByteBuffer): Unit = {
    if (input.hasRemaining) {
      if (input.hasArray()) {
        val tmp      = input.array()
        val offset   = input.arrayOffset()
        val position = input.position()
        val limit    = input.limit()
        engineUpdate(tmp, offset + position, limit - position)
        input.position(limit)
      } else {
        val tmp = new Array[Byte](input.limit() - input.position())
        input.get(tmp)
        engineUpdate(tmp, 0, tmp.length)
      }
    }
  }

  protected def engineDigest(): Array[Byte]

  protected def engineDigest(buf: Array[Byte], offset: Int, len: Int): Int =
    if (len < engineGetDigestLength()) {
      engineReset()
      throw new DigestException(
        "The value of len parameter is less than the actual digest length.")
    } else if (offset < 0) {
      engineReset()
      throw new DigestException("Invalid negative offset")
    } else if (offset + len > buf.length) {
      engineReset()
      throw new DigestException("Incorrect offset or len value")
    } else {
      val tmp = engineDigest()
      if (len < tmp.length) {
        throw new DigestException(
          "The value of len parameter is less than the actual digest length.")
      } else {
        System.arraycopy(tmp, 0, buf, offset, tmp.length)
        tmp.length
      }
    }

  protected def engineReset(): Unit
}
