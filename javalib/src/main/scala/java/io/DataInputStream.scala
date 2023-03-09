package java.io

import java.nio.ByteBuffer

import scala.annotation.tailrec

import scalanative.unsafe.sizeOf

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
  private final val inBasket = new Array[Byte](sizeOf[Long])
  private final val outBasket = ByteBuffer.wrap(inBasket) // default: BigEndian

  private final def rebuffer(n: Int): ByteBuffer = {
    @tailrec
    def rebufferImpl(n: Int, runningTotal: Int): Int = {
      in.read(inBasket, 0, n) match {
        case `n` => n

        case -1 => throw new java.io.EOFException()

        case 0 =>
          // Much ado about nothing. In a correct system, this case
          // should never happen, yet here we are.
          //
          // rebuffer() is private, so all its possible callers are known
          // In that closed world, it should always be called with n > 0.
          // The specification states that, given a positive count,
          // a read from the underlying stream should return either at
          // least 1 byte or throw an Exception.
          //
          // Any nRead == 0, either from n == 0 passed in or from a short read
          // of the underlying stream is a blivet.
          throw new java.io.IOException(
            s"error in rebuffer: expected to read ${n} > 0 bytes, got: 0"
          )

        case nRead =>
          rebufferImpl(n - nRead, runningTotal + nRead)
      }
    }

    rebufferImpl(n, 0) // 3rd arg is something other than 0.
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
    rebuffer(sizeOf[Byte]).get()

  override final def readChar(): Char =
    rebuffer(sizeOf[Char]).getChar()

  override final def readDouble(): Double =
    rebuffer(sizeOf[Double]).getDouble()

  override final def readFloat(): Float =
    rebuffer(sizeOf[Float]).getFloat()

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
    rebuffer(sizeOf[Int]).getInt()

  @deprecated("BufferedReader.readLine() is preferred", "JDK 1.1")
  override final def readLine(): String = {
    var v = in.read()
    if (v == -1) null
    else {
      val builder = new StringBuilder
      var c = v.toChar
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
    rebuffer(sizeOf[Long]).getLong()

  override final def readShort(): Short =
    rebuffer(sizeOf[Short]).getShort()

  override final def readUnsignedByte(): Int =
    readByte() & 0xff

  override final def readUnsignedShort(): Int =
    rebuffer(sizeOf[Short]).getShort() & 0xffff

  def readUTF(): String =
    DataInputStream.readUTF(this)

  override def skipBytes(n: Int): Int =
    in.skip(n.toLong).toInt
}

object DataInputStream {
  // Retain Scala.js formatting for readUTF().
  // The Scala.js original uses  long (> 80 char) lines as the
  // argument for badFormat(). scalafmt inserts line breaks at odd places,
  // and makes the code unreadable.
  //
  // format: off

  def readUTF(in: DataInput): String = {
    // Ported from Scala.js commit: 1337656 dated: 2020-06-04
    // Then modified to operate as a static, not class, method.

    val length = in.readUnsignedShort()
    var res    = ""
    var i      = 0

    def hex(x: Int): String =
      (if (x < 0x10) "0" else "") + Integer.toHexString(x)

    def badFormat(msg: String) = throw new UTFDataFormatException(msg)

    // Minimize changes to ported code by using "EOF returns -1" contract of
    // InputStream not the "EOFException" of provided DataInput 'in' argument.
    def read(): Int =
      try {
        in.readUnsignedByte()
      } catch {
        case _: EOFException => -1
      }

    while (i < length) {
      val a = read()

      if (a == -1)
        badFormat("Unexpected EOF: " + (length - i) + " bytes to go")

      i += 1

      val char = {
        if ((a & 0x80) == 0x00) { // 0xxxxxxx
          a.toChar
        } else if ((a & 0xE0) == 0xC0 && i < length) { // 110xxxxx
          val b = read()
          i += 1

          if (b == -1)
            badFormat("Expected 2 bytes, found: EOF (init: " + hex(a) + ")")
          if ((b & 0xC0) != 0x80) // 10xxxxxx
            badFormat("Expected 2 bytes, found: " + hex(b) + " (init: " + hex(a) + ")")

          (((a & 0x1F) << 6) | (b & 0x3F)).toChar
        } else if ((a & 0xF0) == 0xE0 && i < length - 1) { // 1110xxxx
          val b = read()
          val c = read()
          i += 2

          if (b == -1)
            badFormat("Expected 3 bytes, found: EOF (init: " + hex(a) + ")")

          if ((b & 0xC0) != 0x80)   // 10xxxxxx
            badFormat("Expected 3 bytes, found: " + hex(b) + " (init: " + hex(a) + ")")

          if (c == -1)
            badFormat("Expected 3 bytes, found: " + hex(b) + ", EOF (init: " + hex(a) + ")")

          if ((c & 0xC0) != 0x80)   // 10xxxxxx
            badFormat("Expected 3 bytes, found: " + hex(b) + ", " + hex(c) + " (init: " + hex(a) + ")")

          (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F)).toChar
        } else {
          val rem = length - i
          badFormat("Unexpected start of char: " + hex(a) + " (" + rem + " bytes to go)")
        }
      }

      res += char
    }

    res
  }
  // format: on
}
