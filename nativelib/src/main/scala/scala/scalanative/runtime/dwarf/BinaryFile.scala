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

class PosBufferedInputStream(in: InputStream, size: Int) extends BufferedInputStream(in, size) {
  def position(_pos: Long) = {
    _pos - count + this.pos
  }
  def getCount() = count
  def getSize() = size
  def seek(pos: Int) = {
    this.pos = pos
  }
  def fill = {
    this.read()
    this.pos = pos - 1
  }
}

class BinaryFile(file: File) {
  private val raf = new RandomAccessFile(file, "r")
  private val ch = raf.getChannel()
  private var buf = new PosBufferedInputStream(Channels.newInputStream(ch), 8192)
  private var ds = new DataInputStream(buf)

  // implicit val implicitDS: DataInputStream = ds

  def seek(pos: Long): Unit = {
    // println(s"pos: $pos")
    val origin = ch.position() - buf.getCount() 
    val diff = pos - origin
    val seekBuffer = 0 < diff && diff < buf.getSize()
    if (seekBuffer) {
      // println(s"current: ${ch.position()}")
      // // val origin = ch.position() - buf.getSize() 
      // println(s"origin: $origin")
      // // val diff = pos - origin
      // println(s"diff: $diff")
      buf.seek(diff.toInt)
    }
    else {
      raf.seek(pos)
      buf = new PosBufferedInputStream(Channels.newInputStream(ch), 8192)
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
