package scala.scalanative.runtime.dwarf

import java.io.DataInputStream
import Endianness.LITTLE
import Endianness.BIG
import java.io.RandomAccessFile
import java.nio.channels.Channels
import scalanative.unsigned._
import MachO._
import scala.collection.mutable.ArrayBuffer
import java.io.File
import java.io.BufferedInputStream
import java.io.InputStream

class SeekableBufferedInputStream(in: InputStream, size: Int)
    extends BufferedInputStream(in, size) {
  def position(_pos: Long) = _pos - count + this.pos
  def getCount() = count
  def seek(pos: Int) = this.pos = pos
}

class BinaryFile(file: File) {
  private val raf = new RandomAccessFile(file, "r")
  private val ch = raf.getChannel()
  private var buf =
    new SeekableBufferedInputStream(Channels.newInputStream(ch), 8192)
  private var ds = new DataInputStream(buf)

  def seek(pos: Long): Unit = {
    val origin = ch.position() - buf.getCount()
    val diff = pos - origin
    val seekBuffer = 0 < diff && diff < buf.getCount()
    if (seekBuffer) {
      buf.seek(diff.toInt)
    } else {
      raf.seek(pos)
      buf = new SeekableBufferedInputStream(Channels.newInputStream(ch), 8192)
      ds = new DataInputStream(buf)
    }
  }
  def readByte(): Byte = ds.readByte()
  def readUnsignedByte(): UByte = ds.readByte().toUByte
  def readUnsignedShort(): UShort = ds.readUnsignedShort().toUShort
  def readLong(): Long = ds.readLong()
  def readInt(): Int = ds.readInt()
  def readNBytes(bytes: Int): Array[Byte] = {
    if (bytes <= 0) Array.empty
    else {
      val buf = ArrayBuffer.empty[Byte]
      (1 to bytes).foreach { _ => buf += ds.readByte() }
      buf.toArray
    }
  }
  def position(): Long = buf.position(ch.position())
  def readFully(ar: Array[Byte]) = ds.readFully(ar)

  def skipNBytes(n: Long): Unit = ds.skipBytes(n.toInt)
}
