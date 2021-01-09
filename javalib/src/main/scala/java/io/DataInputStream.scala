package java.io

import java.nio.ByteBuffer
import scalanative.unsafe.sizeof

class DataInputStream(in: InputStream)
    extends FilterInputStream(in)
    with DataInput {
  // Design notes:
  //   1) Using ByteBuffer.get* methods results in a slightly longer code
  //      path than open coding BigEndian byte order, bit shifts, and
  //      logical Ors. This approach greatly simplifies the code and makes
  //      it easier to trace; increasing the likelihood of correctness.
  //
  //   2) Both the Java 8 and 15 APIs describe the methods of this class
  //      as only optionally thread-safe and requiring external
  //      synchronization. A buffer per instance does not introduce a
  //      concern.
  private final val inBasket  = new Array[Byte](sizeof[Long].toInt)
  private final val outBasket = ByteBuffer.wrap(inBasket) // default: BigEndian

  private final def rebuffer(n: Int): ByteBuffer = {
    if (in.read(inBasket, 0, n) < n)
      throw new java.io.EOFException
    outBasket.clear() // tricky here: contents preserved, bookkeeping reset.
  }

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

  override final def readByte(): Byte =
    rebuffer(sizeof[Byte].toInt).get()

  override final def readChar(): Char =
    rebuffer(sizeof[Char].toInt).getChar()

  override final def readDouble(): Double =
    rebuffer(sizeof[Double].toInt).getDouble()

  override final def readFloat(): Float =
    rebuffer(sizeof[Float].toInt).getFloat()

  override final def readFully(b: Array[Byte]): Unit =
    readFully(b, 0, b.length)

  override final def readFully(b: Array[Byte], off: Int, len: Int): Unit = {
    if (b == null)
      throw new NullPointerException

    // Use the same message texts as the JVM for all 3 cases. Yes, 3rd differs.
    if ((off < 0) || ((off + len) > b.length)) {
      val msg =
        s"Range [${off}, ${off} + ${len}) out of bounds for length ${b.length}"
      throw new IndexOutOfBoundsException(msg)
    }

    if (len < 0)
      throw new IndexOutOfBoundsException()

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

  override final def readInt(): Int =
    rebuffer(sizeof[Int].toInt).getInt()

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
  override final def readLong(): Long =
    rebuffer(sizeof[Long].toInt).getLong()

  override final def readShort(): Short =
    rebuffer(sizeof[Short].toInt).getShort()

  override final def readUnsignedByte(): Int =
    readByte() & 0xFF

  override final def readUnsignedShort(): Int =
    rebuffer(sizeof[Short].toInt).getShort() & 0xFFFF

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
