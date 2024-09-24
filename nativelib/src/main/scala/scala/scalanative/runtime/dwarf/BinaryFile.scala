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

  // Get the actual reading position,
  // ch.position() should be forwarded because BufferedStream load bytes into buffer (the amount can be retrieved by buf.getCount)
  // The starting position of BufferedStream is `ch.position() - buf.getCount`.
  // The actual reading position is the starting point + buf.getPos (where buf.getPos returns the position in the buffer).
  //
  //        ---------------------
  //       /   buf.getCount      \
  // |----|--------------|-------|----------------|
  //      \ buf.getPos  /|       |
  //       ------------  |     ch.position()
  //                     |
  //            actual reading pos
  def position(): Long = ch.position() - buf.getCount() + buf.getPos()

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
  def readWhile(predicate: Byte => Boolean): Array[Byte] = {
    val buffer = new scala.collection.mutable.ArrayBuffer[Byte]()
    var byte = readByte()
    while (predicate(byte)) {
      buffer += byte
      byte = readByte()
    }
    buffer.toArray
  }

  def readFully(ar: Array[Byte]) = ds.readFully(ar)

  def skipNBytes(n: Long): Unit = ds.skipBytes(n.toInt)
}
