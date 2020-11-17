package java.io

class DataOutputStream(out: OutputStream)
    extends FilterOutputStream(out)
    with DataOutput {

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
    out.write(b)
    written += 1
  }

  override final def writeBoolean(v: Boolean): Unit =
    write(if (v) 1 else 0)

  override final def writeByte(v: Int): Unit =
    write(v.toByte)

  override final def writeBytes(s: String): Unit =
    write(s.toArray.map(_.toByte))

  override final def writeChar(v: Int): Unit = {
    write((v >> 8) & 0xFF)
    write(v & 0xFF)
  }

  override final def writeChars(s: String): Unit =
    s.toCharArray.foreach(c => writeChar(c.toInt))

  override final def writeDouble(v: Double): Unit =
    writeLong(java.lang.Double.doubleToLongBits(v))

  override final def writeFloat(v: Float): Unit =
    writeInt(java.lang.Float.floatToIntBits(v))

  override final def writeInt(v: Int): Unit = {
    write((v >> 24) & 0xFF)
    write((v >> 16) & 0xFF)
    write((v >> 8) & 0xFF)
    write(v & 0xFF)
  }

  override final def writeLong(v: Long): Unit = {
    write(((v >> 56) & 0xFF).toInt)
    write(((v >> 48) & 0xFF).toInt)
    write(((v >> 40) & 0xFF).toInt)
    write(((v >> 32) & 0xFF).toInt)
    write(((v >> 24) & 0xFF).toInt)
    write(((v >> 16) & 0xFF).toInt)
    write(((v >> 8) & 0xFF).toInt)
    write((v & 0xFF).toInt)
  }

  override final def writeShort(v: Int): Unit = {
    write((v >> 8) & 0xFF)
    write(v & 0xFF)
  }

  override final def writeUTF(str: String): Unit = {
    val utfBytes = toModifiedUTF(str)
    writeShort(utfBytes.length)
    write(utfBytes)
  }

  private def toModifiedUTF(str: String): Array[Byte] =
    str.toArray.flatMap(c => toModifiedUTF(c))

  private def toModifiedUTF(c: Char): Array[Byte] = {
    if (c >= '\u0001' && c <= '\u007F') {
      Array(c.toByte)
    } else if (c == '\u0000' || (c >= '\u0080' && c <= '\u07FF')) {
      val b1 = 0xC0 | (c >>> 6)
      val b2 = 0x80 | (c & 0x3F)
      Array(b1.toByte, b2.toByte)
    } else {
      val b1 = 0xE0 | (c >>> 12)
      val b2 = 0x80 | ((c >>> 6) & 0x3F)
      val b3 = 0x80 | (c & 0x3F)
      Array(b1.toByte, b2.toByte, b3.toByte)
    }
  }
}
