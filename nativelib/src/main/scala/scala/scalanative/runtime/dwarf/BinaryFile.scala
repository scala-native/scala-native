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

private[runtime] class SeekableBufferedInputStream(
    in: InputStream,
    size: Int = SeekableBufferedInputStream.DEFAULT_BUF_SIZE
) extends BufferedInputStream(in, size) {
  def getCount() = count
  def getPos() = pos
  def seek(pos: Int) = this.pos = pos
}
private object SeekableBufferedInputStream {
  val DEFAULT_BUF_SIZE = 8192
}

private[runtime] class BinaryFile(file: File) {
  private val raf = new RandomAccessFile(file, "r")
  private val ch = raf.getChannel()
  private var buf =
    new SeekableBufferedInputStream(Channels.newInputStream(ch))
  private var ds = new DataInputStream(buf)

  private var _position: Long = 0L
  def position(): Long = _position

  def seek(pos: Long): Unit = {
    // `origin` is the starting point that BufferedStream loaded into its buffer (see: `position` method)
    // if the `pos` to seek is already loaded by buffer (`loadedInBuffer = true`),
    // we can just move the `pos` in BufferedStream
    // Otherwise, seek in the disk, and recreate the BufferedStream.
    val origin = ch.position() - buf.getCount()
    val posInBuf = pos - origin
    val loadedInBuffer =
      0 < posInBuf &&
        posInBuf < buf.getCount() &&
        posInBuf < Int.MaxValue // probably obvious that posInBuf < Int.MaxValue since it should be smaller than DEFAULT_BUF_SIZE, but just in case

    if (loadedInBuffer) {
      buf.seek(posInBuf.toInt)
    } else {
      raf.seek(pos)
      buf = new SeekableBufferedInputStream(Channels.newInputStream(ch))
      ds = new DataInputStream(buf)
    }

    _position = pos
  }
  def readByte(): Byte = {
    _position += 1
    ds.readByte()
  }
  def readUnsignedByte(): UByte = {
    _position += 1
    ds.readByte().toUByte
  }
  def readUnsignedShort(): UShort = {
    _position += 2
    ds.readUnsignedShort().toUShort
  }
  def readLong(): Long = {
    _position += 8
    ds.readLong()
  }
  def readInt(): Int = {
    _position += 4
    ds.readInt()
  }
  def readNBytes(bytes: Int): Array[Byte] = {
    if (bytes <= 0) Array.empty
    else {
      val buf = ArrayBuffer.empty[Byte]
      (1 to bytes).foreach { _ => buf += ds.readByte() }
      _position += bytes
      buf.toArray
    }
  }

  def readWhile(predicate: Byte => Boolean): Array[Byte] = {
    val buffer = new scala.collection.mutable.ArrayBuffer[Byte]()
    var byte = readByte()
    while (predicate(byte)) {
      buffer += byte
      byte = readByte()
      _position += 1
    }
    buffer.toArray
  }

  def readFully(ar: Array[Byte]) = {
    _position += ar.length
    ds.readFully(ar)
  }

  def skipNBytes(n: Long): Unit = {
    _position += n
    ds.skipBytes(n.toInt)
  }
}
