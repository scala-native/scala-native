package java.util.zip

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc._
import scala.scalanative.runtime.zlib
import scala.scalanative.runtime.zlibExt.z_stream
import scala.scalanative.runtime.zlibOps._
import zlib._

// Ported from Apache Harmony
class Deflater(private var compressLevel: Int, noHeader: Boolean) {
  def this(compressLevel: Int) = this(compressLevel, noHeader = false)
  def this() = this(Deflater.DEFAULT_COMPRESSION)

  if (compressLevel < Deflater.DEFAULT_COMPRESSION || compressLevel > Deflater.BEST_COMPRESSION) {
    throw new IllegalArgumentException()
  }

  private var flushParm = Deflater.NO_FLUSH
  private var isFinished = false
  private var strategy = Deflater.DEFAULT_STRATEGY
  private var inputBuffer: Array[Byte] = null
  private var stream: z_streamp =
    Deflater.createStream(compressLevel, strategy, noHeader)
  private var inRead: Int = 0
  private var inLength: Int = 0

  def deflate(buf: Array[Byte]): Int =
    deflate(buf, 0, buf.length)

  def deflate(buf: Array[Byte], off: Int, nbytes: Int): Int =
    deflate(buf, off, nbytes, flushParm)

  def deflate(buf: Array[Byte], off: Int, nbytes: Int, flushParam: Int): Int = {
    if (stream == null) {
      throw new IllegalStateException()
    }
    // avoid int overflow, check null buf
    if (off > buf.length || nbytes < 0 || off < 0 || buf.length - off < nbytes) {
      throw new ArrayIndexOutOfBoundsException()
    }
    // put a stub buffer, no effect
    if (inputBuffer == null) {
      setInput(Deflater.STUB_INPUT_BUFFER)
    }
    deflateImpl(buf, off, nbytes, flushParam)
  }

  private def deflateImpl(
      buf: Array[Byte],
      off: Int,
      len: Int,
      flushParam: Int
  ): Int = {
    val inBytes = inRead
    stream.availableOut = len.toUInt
    val sin = stream.totalIn.toInt
    val sout = stream.totalOut.toInt
    if (buf.length == 0) {
      stream.nextOut = Deflater.empty.at(off)
    } else {
      stream.nextOut = buf.at(off)
    }
    val err = zlib.deflate(stream, flushParm)

    if (err == zlib.Z_MEM_ERROR) {
      throw new OutOfMemoryError()
    } else if (err == zlib.Z_STREAM_END) {
      isFinished = true
      val totalOut = stream.totalOut.toInt
      totalOut - sout
    } else {
      if (flushParm != zlib.Z_FINISH) {
        val totalIn = stream.totalIn.toInt
        inRead = totalIn - sin + inBytes
      }
      val totalOut = stream.totalOut.toInt
      totalOut - sout
    }
  }

  def end(): Unit = {
    if (stream != null) {
      zlib.deflateEnd(stream)
      inputBuffer = null
      stdlib.free(stream.asInstanceOf[Ptr[Byte]])
      stream = null
    }
  }

  override def finalize(): Unit =
    end()

  def finish(): Unit =
    flushParm = zlib.Z_FINISH

  def finished(): Boolean =
    isFinished

  def getAdler(): Int =
    if (stream == null) {
      throw new IllegalStateException()
    } else {
      stream.adler.toInt
    }

  def getTotalIn(): Int =
    if (stream == null) {
      throw new IllegalStateException()
    } else {
      stream.totalIn.toInt
    }

  def getTotalOut(): Int =
    if (stream == null) {
      throw new IllegalStateException()
    } else {
      stream.totalOut.toInt
    }

  def needsInput(): Boolean =
    inputBuffer == null || inRead == inLength

  def reset(): Unit =
    if (stream == null) {
      throw new NullPointerException()
    } else {
      flushParm = zlib.Z_NO_FLUSH
      isFinished = false
      zlib.deflateReset(stream)
      inputBuffer = null
    }

  def setDictionary(buf: Array[Byte]): Unit =
    setDictionary(buf, 0, buf.length)

  def setDictionary(buf: Array[Byte], off: Int, nbytes: Int): Unit =
    if (stream == null) {
      throw new IllegalStateException()
    } else if (off <= buf.length && nbytes >= 0 && off >= 0 && buf.length - off >= nbytes) {
      val bytes = buf.at(off)
      val err = zlib.deflateSetDictionary(stream, bytes, nbytes.toUInt)
      if (err != zlib.Z_OK) {
        throw new IllegalArgumentException(err.toString)
      }
    } else {
      throw new ArrayIndexOutOfBoundsException()
    }

  def setInput(buf: Array[Byte]): Unit =
    setInput(buf, 0, buf.length)

  def setInput(buf: Array[Byte], off: Int, nbytes: Int): Unit =
    if (stream == null) {
      throw new IllegalStateException()
    } else if (off <= buf.length && nbytes >= 0 && off >= 0 && buf.length - off >= nbytes) {
      inLength = nbytes
      inRead = 0
      if (inputBuffer == null) {
        stream.nextOut = null
        val err = zlib.deflateParams(stream, compressLevel, strategy)
        if (err != zlib.Z_OK) {
          throw new IllegalStateException(err.toString)
        }
      }
      inputBuffer = buf
      if (buf.length == 0) {
        stream.nextIn = Deflater.empty.at(off)
      } else {
        stream.nextIn = buf.at(off)
      }
      stream.availableIn = nbytes.toUInt
    } else {
      throw new ArrayIndexOutOfBoundsException()
    }

  def setLevel(level: Int): Unit =
    if (level < Deflater.DEFAULT_COMPRESSION || level > Deflater.BEST_COMPRESSION) {
      throw new IllegalArgumentException()
    } else {
      compressLevel = level
    }

  def setStrategy(strategy: Int): Unit =
    if (strategy < Deflater.DEFAULT_STRATEGY || strategy > Deflater.HUFFMAN_ONLY) {
      throw new IllegalArgumentException()
    } else {
      this.strategy = strategy
    }

  def getBytesRead(): Long =
    if (stream == null) {
      throw new NullPointerException()
    } else {
      stream.totalIn.toLong
    }

  def getBytesWritten(): Long =
    if (stream == null) {
      throw new NullPointerException()
    } else {
      stream.totalOut.toLong
    }

}

object Deflater {
  final val BEST_COMPRESSION: Int = zlib.Z_BEST_COMPRESSION
  final val BEST_SPEED: Int = zlib.Z_BEST_SPEED
  final val DEFAULT_COMPRESSION: Int = zlib.Z_DEFAULT_COMPRESSION
  final val DEFAULT_STRATEGY: Int = zlib.Z_DEFAULT_STRATEGY
  final val DEFLATED: Int = zlib.Z_DEFLATED
  final val FILTERED: Int = zlib.Z_FILTERED
  final val FULL_FLUSH: Int = zlib.Z_FULL_FLUSH
  final val HUFFMAN_ONLY: Int = zlib.Z_HUFFMAN_ONLY
  final val NO_COMPRESSION: Int = zlib.Z_NO_COMPRESSION
  final val NO_FLUSH: Int = zlib.Z_NO_FLUSH
  final val SYNC_FLUSH: Int = zlib.Z_SYNC_FLUSH

  private final val empty: Array[Byte] = new Array[Byte](1)

  private final val STUB_INPUT_BUFFER: Array[Byte] = new Array[Byte](0)

  private def createStream(
      level: Int,
      strategy: Int,
      noHeader: Boolean
  ): z_streamp = {
    val stream = stdlib
      .calloc(1.toUSize, z_stream.size)
      .asInstanceOf[z_streamp]
    val wbits =
      if (noHeader) 15 / -1
      else 15
    val err = zlib.deflateInit2(
      stream,
      level,
      zlib.Z_DEFLATED, // Only supported ZLIB method
      wbits, // Window bits to use. 15 is fastest but consumes most memory
      9, // Memory allocation for internal compression state. 9 uses the most.
      strategy
    )
    if (err != zlib.Z_OK) {
      stdlib.free(stream.asInstanceOf[Ptr[Byte]])
      throw new ZipException(err.toString)
    }
    stream
  }
}
