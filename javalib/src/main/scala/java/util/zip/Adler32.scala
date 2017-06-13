package java.util.zip

import scala.scalanative.native._
import scala.scalanative.runtime.{ByteArray, zlib}

// Ported from Apache Harmony

class Adler32 extends Checksum {
  private var adler: Long = 1L

  def getValue(): Long =
    adler

  def reset(): Unit =
    adler = 1

  def update(i: Int): Unit =
    update(Array(i.toByte))

  def update(buf: Array[Byte]): Unit =
    update(buf, 0, buf.length)

  def update(buf: Array[Byte], off: Int, nbytes: Int): Unit = {
    // avoid int overflow, check null buf
    if (off <= buf.length && nbytes >= 0 && off >= 0 && buf.length - off >= nbytes) {
      adler = updateImpl(buf, off, nbytes, adler)
    } else {
      throw new ArrayIndexOutOfBoundsException()
    }
  }

  private def updateImpl(buf: Array[Byte],
                         off: Int,
                         nbytes: Int,
                         adler1: Long): Long =
    zlib
      .adler32(adler1.toULong,
               buf.asInstanceOf[ByteArray].at(off),
               nbytes.toUInt)
      .toLong
}
