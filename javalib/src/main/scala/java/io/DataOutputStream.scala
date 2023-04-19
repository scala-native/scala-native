package java.io

class DataOutputStream(out: OutputStream)
    extends FilterOutputStream(out)
    with DataOutput {
  // Capacity is a guess: Balance memory use & execution speed.
  // Allow small to moderate sized Strings to be written as Chars in one shot.
  private val bufferCapacity = 1024 * 2
  private val buffer = new Array[Byte](bufferCapacity)

  protected var written: Int = 0

  override def flush(): Unit =
    out.flush()

  final def size(): Int =
    written

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    out.write(b, off, len)
    written += len
  }

  override def write(b: Int): Unit = {
    out.write(b) // underlying stream will convert to byte.
    written += 1
  }

  override final def writeBoolean(v: Boolean): Unit =
    write(if (v) 1 else 0)

  override final def writeByte(v: Int): Unit =
    write(v)

  override final def writeBytes(s: String): Unit = {
    var index = 0

    for (ch <- s) {
      if (index == bufferCapacity) {
        write(buffer, 0, bufferCapacity)
        index = 0
      }

      buffer(index) = ch.toByte
      index += 1
    }

    if (index > 0)
      write(buffer, 0, index)
  }

  override final def writeChar(v: Int): Unit = {
    buffer(0) = (v >> 8).toByte
    buffer(1) = v.toByte
    write(buffer, 0, 2)
  }

  override final def writeChars(s: String): Unit = {
    var index = 0

    for (ch <- s) {
      if (index == bufferCapacity) {
        write(buffer, 0, bufferCapacity)
        index = 0
      }

      buffer(index) = (ch >> 8).toByte
      buffer(index + 1) = ch.toByte
      index += 2
    }

    if (index > 0)
      write(buffer, 0, index)
  }

  override final def writeDouble(v: Double): Unit =
    writeLong(java.lang.Double.doubleToLongBits(v))

  override final def writeFloat(v: Float): Unit =
    writeInt(java.lang.Float.floatToIntBits(v))

  override final def writeInt(v: Int): Unit = {
    buffer(0) = (v >> 24).toByte
    buffer(1) = (v >> 16).toByte
    buffer(2) = (v >> 8).toByte
    buffer(3) = v.toByte
    write(buffer, 0, 4)
  }

  override final def writeLong(v: Long): Unit = {
    buffer(0) = (v >> 56).toByte
    buffer(1) = (v >> 48).toByte
    buffer(2) = (v >> 40).toByte
    buffer(3) = (v >> 32).toByte
    buffer(4) = (v >> 24).toByte
    buffer(5) = (v >> 16).toByte
    buffer(6) = (v >> 8).toByte
    buffer(7) = v.toByte
    write(buffer, 0, 8)
  }

  override final def writeShort(v: Int): Unit = {
    buffer(0) = (v >> 8).toByte
    buffer(1) = v.toByte
    write(buffer, 0, 2)
  }

  // Ported from Scala.js commit: f700b9f dated: Sep 12, 2019

  final def writeUTF(s: String): Unit = {
    val buffer = new Array[Byte](2 + 3 * s.length)

    var idx = 2
    for (i <- 0 until s.length()) {
      val c = s.charAt(i)
      if (c <= 0x7f && c >= 0x01) {
        buffer(idx) = c.toByte
        idx += 1
      } else if (c < 0x0800) {
        buffer(idx) = ((c >> 6) | 0xc0).toByte
        buffer(idx + 1) = ((c & 0x3f) | 0x80).toByte
        idx += 2
      } else {
        buffer(idx) = ((c >> 12) | 0xe0).toByte
        buffer(idx + 1) = (((c >> 6) & 0x3f) | 0x80).toByte
        buffer(idx + 2) = ((c & 0x3f) | 0x80).toByte
        idx += 3
      }
    }

    val len = idx - 2

    if (len >= 0x10000)
      throw new UTFDataFormatException(s"encoded string too long: $len bytes")

    buffer(0) = (len >> 8).toByte
    buffer(1) = len.toByte

    write(buffer, 0, idx)
  }
}
