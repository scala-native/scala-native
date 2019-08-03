package java.io

class DataInputStream(in: InputStream)
    extends FilterInputStream(in)
    with DataInput {

  override final def read(b: Array[Byte]): Int =
    in.read(b)

  override final def read(b: Array[Byte], off: Int, len: Int): Int =
    in.read(b, off, len)

  override final def readBoolean(): Boolean =
    read() != 0

  override final def readByte(): Byte =
    read().toByte

  override final def readChar(): Char = {
    val b1, b2 = readUnsignedByte()
    ((b1 << 8) | b2).asInstanceOf[Char]
  }

  override final def readDouble(): Double = {
    val long = readLong()
    java.lang.Double.longBitsToDouble(long)
  }

  override final def readFloat(): Float = {
    val int = readInt()
    java.lang.Float.intBitsToFloat(int)
  }

  override final def readFully(b: Array[Byte]): Unit =
    readFully(b, 0, b.length)

  override final def readFully(b: Array[Byte], off: Int, len: Int): Unit = {
    if (b == null) {
      throw new NullPointerException
    }

    // Use the same message texts as the JVM for all 3 cases. Yes, 3rd differs.
    if ((off < 0) || ((off + len) > b.length)) {
      val msg =
        s"Range [${off}, ${off} + ${len}) out of bounds for length ${b.length}"
      throw new IndexOutOfBoundsException(msg)
    }

    if (len < 0) {
      throw new IndexOutOfBoundsException()
    }

    var offset = off
    var length = len

    while (length > 0) {
      val nread = in.read(b, offset, length)

      if (nread == -1) {
        throw new EOFException()
      } else {
        offset += nread
        length -= nread
      }
    }
  }

  override final def readInt(): Int = {
    val b1, b2, b3, b4 = readUnsignedByte()
    (b1 << 24) | (b2 << 16) + (b3 << 8) + b4
  }

  override final def readLine(): String = {
    var v = read()
    if (v == -1) null
    else {
      val builder = new StringBuilder
      var c       = v.toChar
      while (v != -1 && c != '\n' && c != '\r') {
        builder.append(c)
        v = read()
        c = v.toChar
      }

      if (c == '\r') {
        mark(1)
        if (read().toChar != '\n') reset()
      }
      builder.toString
    }
  }

  override final def readLong(): Long = {
    val b1, b2, b3, b4, b5, b6, b7, b8 = readUnsignedByte()
    (b1.toLong << 56) + (b2.toLong << 48) +
      (b3.toLong << 40) + (b4.toLong << 32) +
      (b5.toLong << 24) + (b6.toLong << 16) +
      (b7.toLong << 8) + b8
  }

  override final def readShort(): Short = {
    val b1, b2 = readUnsignedByte()
    ((b1 << 8) | b2).asInstanceOf[Short]
  }

  override final def readUnsignedByte(): Int =
    readByte() & 0xFF

  override final def readUnsignedShort(): Int = {
    val b1, b2 = readUnsignedByte()
    (b1 << 8) | b2
  }

  override final def readUTF(): String =
    DataInputStream.readUTF(this)

  override def skipBytes(n: Int): Int = {
    var i = 0
    var v = 0
    while (i < n && v != -1) {
      v = read()
      i += 1
    }
    i
  }

}

object DataInputStream {

  def readUTF(in: DataInput): String = {
    val nbBytes  = in.readUnsignedShort()
    val utfBytes = new Array[Byte](nbBytes)
    in.readFully(utfBytes)
    fromModifiedUTF(utfBytes)
  }

  private def fromModifiedUTF(b: Array[Byte]): String = {
    val builder = new StringBuilder
    var i       = 0
    while (i < b.length) {
      if ((b(i) & 0x80) == 0) {
        builder.append(b(i).toChar)
        i += 1
      } else if ((b(i) & 0xE0) == 0xC0) {
        val b1 = (b(i) & 0x1F) << 6
        val b2 = (b(i + 1) & 0x3F)
        val c  = (b1 | b2).toChar
        builder.append(c)
        i += 2
      } else {
        val b1 = (b(i) & 0x0F) << 12
        val b2 = (b(i + 1) & 0x3F) << 6
        val b3 = (b(i + 2) & 0x3F)
        val c  = (b1 | b2 | b3).toChar
        builder.append(c)
        i += 3
      }
    }
    builder.toString
  }
}
