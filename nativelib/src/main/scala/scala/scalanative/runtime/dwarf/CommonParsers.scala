package scala.scalanative.runtime.dwarf

import Endianness.LITTLE
import Endianness.BIG
import java.io.DataInputStream
import java.io.RandomAccessFile
import java.nio.channels.Channels
import MachO.*
import scalanative.unsafe.*
import scalanative.unsigned.*

private[runtime] object CommonParsers {
  final val BYTE = 1
  final val SHORT = 2
  final val INT = 4
  final val LONG = 8

  def uint8()(implicit endi: Endianness, bf: BinaryFile): UByte =
    bf.readUnsignedByte()

  def uint16()(implicit endi: Endianness, stream: BinaryFile): UShort = {
    val v = stream.readUnsignedShort()
    endi match {
      case LITTLE =>
        ((v >>> 8) | ((v & 0xff.toUShort) << 8)).toUShort
      case BIG =>
        v
    }
  }

  def uint32()(implicit endi: Endianness, stream: BinaryFile): UInt = {
    val v = stream.readInt()
    endi match {
      case LITTLE =>
        (v >>> 24 & 0xff |
          v >>> 8 & 0xff00 |
          v << 8 & 0xff0000 |
          v << 24 & 0xff000000).toUInt
      case BIG =>
        v.toUInt
    }
  }

  def uint64()(implicit endi: Endianness, stream: BinaryFile): Long = {
    val v = stream.readLong()
    endi match {
      case LITTLE =>
        (v << 56) |
          ((v & 0xff00L) << 40) |
          ((v & 0xff0000L) << 24) |
          ((v & 0xff000000L) << 8) |
          ((v >> 8) & 0xff000000L) |
          ((v >> 24) & 0xff0000L) |
          ((v >> 40) & 0xff00L) |
          (v >>> 56)
      case BIG =>
        v
    }
  }

  def skipBytes(n: Long)(implicit stream: BinaryFile): Unit =
    stream.skipNBytes(n)

  def string(n: Int)(implicit stream: BinaryFile) =
    fromCString(stream.readNBytes(n).at(0))

}
