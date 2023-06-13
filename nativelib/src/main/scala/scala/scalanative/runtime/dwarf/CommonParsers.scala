package scala.scalanative.runtime.dwarf

import java.io.DataInputStream
import Endianness.LITTLE
import Endianness.BIG
import java.io.RandomAccessFile
import java.nio.channels.Channels
import scalanative.unsigned._
import MachO._

object CommonParsers {
  val BYTE = 1
  val INT = 4
  val LONG = 8

  def uint8()(implicit endi: Endianness, bf: BinaryFile): UByte =
    bf.readUnsignedByte()

  def uint16()(implicit endi: Endianness, stream: BinaryFile): UShort =
    endi match {
      case LITTLE =>
        val b1 = stream.readByte()
        val b2 = stream.readByte()

        ((b1 & 0xff) | (b2 & 0xff) << 8).toShort.toUShort
      case BIG =>
        stream.readUnsignedShort()
    }

  def uint32()(implicit endi: Endianness, stream: BinaryFile): UInt =
    endi match {
      case LITTLE =>
        val b1 = stream.readUnsignedByte().toLong
        val b2 = stream.readUnsignedByte().toLong
        val b3 = stream.readUnsignedByte().toLong
        val b4 = stream.readUnsignedByte().toLong

        ((b1 & 0xff) |
          (b2 & 0xff) << 8 |
          (b3 & 0xff) << 16 |
          (b4 & 0xff) << 24).toUInt
      case BIG =>
        stream.readInt().toUInt
    }

  def uint64()(implicit endi: Endianness, stream: BinaryFile): Long =
    endi match {
      case LITTLE =>
        val b1 = stream.readUnsignedByte().toLong
        val b2 = stream.readUnsignedByte().toLong
        val b3 = stream.readUnsignedByte().toLong
        val b4 = stream.readUnsignedByte().toLong
        val b5 = stream.readUnsignedByte().toLong
        val b6 = stream.readUnsignedByte().toLong
        val b7 = stream.readUnsignedByte().toLong
        val b8 = stream.readUnsignedByte().toLong

        ((b1 & 0xff) |
          (b2 & 0xff) << 8 |
          (b3 & 0xff) << 16 |
          (b4 & 0xff) << 24 |
          (b5 & 0xff) << 32 |
          (b6 & 0xff) << 40 |
          (b7 & 0xff) << 48 |
          (b8 & 0xff) << 56)

      case BIG =>
        stream.readLong()
    }

  def skipBytes(n: Long)(implicit stream: BinaryFile): Unit =
    stream.skipNBytes(n)

  def string(n: Int)(implicit stream: BinaryFile) =
    new String(stream.readNBytes(n).takeWhile(_ != 0))

}
