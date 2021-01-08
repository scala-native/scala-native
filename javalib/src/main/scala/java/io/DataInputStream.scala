package java.io

class DataInputStream(in: InputStream)
    extends FilterInputStream(in)
    with DataInput {
  // Notes on End of File (EOF) handling.
  //
  // The Java 8 API describes/defines the first two read routines as returning
  // an Int. At EOF this is the -1 bubbled up by the underlying InputStream.
  //
  // The other read*() routines are expected/defined to throw EOFException
  // if the underlying InputStream has returned EOF (-1).

  override final def read(b: Array[Byte]): Int =
    in.read(b)

  override final def read(b: Array[Byte], off: Int, len: Int): Int =
    in.read(b, off, len)

  override final def readBoolean(): Boolean =
    readByte() != 0

  override final def readByte(): Byte = {
    val v = in.read()

    if (v == -1)
      throw new EOFException

    v.toByte
  }

  override final def readChar(): Char =
    readUnsignedShort().asInstanceOf[Char]

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

  @deprecated("BufferedReader.readLine() is preferred", "JDK 1.1")
  override final def readLine(): String = {
    var v = in.read()
    if (v == -1) null
    else {
      val builder = new StringBuilder
      var c       = v.toChar
      while (v != -1 && c != '\n' && c != '\r') {
        builder.append(c)
        v = in.read()
        c = v.toChar
      }

      if (c == '\r') {
        mark(1)
        if (in.read().toChar != '\n') reset()
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

  // The obvious & wrong implementation using (an implied) toInt does sign
  // extension: Char 255 wrongly becomes -1, high bytes set.
  // The bitwise "and" here does the required conversion to Int with high bytes
  // clear, returning a true char 255.
  override final def readUnsignedByte(): Int =
    readByte() & 0xFF

  override final def readUnsignedShort(): Int = {
    val b1, b2 = readUnsignedByte()
    (b1 << 8) | b2
  }

  override final def readUTF(): String =
    DataInputStream.readUTF(this)

  override def skipBytes(n: Int): Int =
    in.skip(n.toLong).toInt
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
