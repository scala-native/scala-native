package java.util.zip

import scala.scalanative.native._
import scala.scalanative.runtime.{ByteArray, zlib}

// Ported from Apache Harmony

class CRC32 extends Checksum {
  private var crc: Long         = 0L
  private[zip] var tbytes: Long = 0L

  def getValue(): Long =
    crc

  def reset(): Unit = {
    tbytes = 0L
    crc = 0L
  }

  def update(v: Int): Unit =
    update(Array(v.toByte))

  def update(buf: Array[Byte]): Unit =
    update(buf, 0, buf.length)

  def update(buf: Array[Byte], off: Int, nbytes: Int): Unit = {
    // avoid int overflow, check null buf
    if (off <= buf.length && nbytes >= 0 && off >= 0 && buf.length - off >= nbytes) {
      tbytes += nbytes
      crc = updateImpl(buf, off, nbytes, crc)
    } else {
      throw new ArrayIndexOutOfBoundsException()
    }
  }

  private def updateImpl(buf: Array[Byte],
                         off: Int,
                         nbytes: Int,
                         crc1: Long): Long =
    zlib
      .crc32(crc1.toULong, buf.asInstanceOf[ByteArray].at(off), nbytes.toUInt)
      .toLong
}
