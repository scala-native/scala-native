package java.util.zip

import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.libc._
import scala.scalanative.runtime.zlib
import scala.scalanative.runtime.zlibExt.z_stream
import scala.scalanative.runtime.zlibOps._
import zlib._

// Ported from Apache Harmony

class Inflater(noHeader: Boolean) {

  private var isFinished: Boolean = false

  private[zip] var inLength: Int = 0
  private[zip] var inRead: Int = 0
  private var doesNeedDictionary: Boolean = false
  private var stream: z_streamp = Inflater.createStream(noHeader)

  def this() = this(noHeader = false)

  def end(): Unit = {
    if (stream != null) {
      zlib.inflateEnd(stream)
      inRead = 0
      inLength = 0
      stdlib.free(stream.asInstanceOf[Ptr[Byte]])
      stream = null
    }
  }

  override protected def finalize(): Unit =
    end()

  def finished(): Boolean =
    isFinished

  def getAdler(): Int =
    if (stream == null) {
      throw new NullPointerException()
    } else {
      stream.adler.toInt
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

  def getRemaining(): Int =
    inLength - inRead

  def getTotalIn(): Int =
    if (stream == null) {
      throw new NullPointerException()
    } else {
      val totalIn = getBytesRead()
      if (totalIn <= Int.MaxValue) totalIn.toInt
      else Int.MaxValue
    }

  def getTotalOut(): Int =
    if (stream == null) {
      throw new NullPointerException()
    } else {
      val totalOut = getBytesWritten()
      if (totalOut <= Int.MaxValue) totalOut.toInt
      else Int.MaxValue
    }

  def inflate(buf: Array[Byte]): Int =
    inflate(buf, 0, buf.length)

  def inflate(buf: Array[Byte], off: Int, nbytes: Int): Int = {
    // avoid int overflow, check null buf
    if (off > buf.length || nbytes < 0 || off < 0 || buf.length - off < nbytes) {
      throw new ArrayIndexOutOfBoundsException()
    } else if (stream == null) {
      throw new NullPointerException()
    } else if (needsInput()) {
      0
    } else {
      val neededDict = doesNeedDictionary
      doesNeedDictionary = false
      val result = inflateImpl(buf, off, nbytes)
      if (doesNeedDictionary && neededDict) {
        throw new DataFormatException("Needs dictionary")
      }
      result
    }
  }

  def needsDictionary(): Boolean =
    doesNeedDictionary

  def needsInput(): Boolean =
    inRead == inLength

  def reset(): Unit =
    if (stream == null) {
      throw new NullPointerException()
    } else {
      isFinished = false
      doesNeedDictionary = false
      inLength = 0
      inRead = 0
      val err = zlib.inflateReset(stream)
      if (err != zlib.Z_OK) {
        throw new ZipException(err.toString)
      }
    }

  def setDictionary(buf: Array[Byte]): Unit =
    setDictionary(buf, 0, buf.length)

  def setDictionary(buf: Array[Byte], off: Int, nbytes: Int): Unit = {
    if (stream == null) {
      throw new NullPointerException()
    } else {
      val bytes = buf.at(off)
      val err = zlib.inflateSetDictionary(stream, bytes, nbytes.toUInt)
      if (err != zlib.Z_OK) {
        throw new IllegalArgumentException(err.toString)
      }
    }
  }

  def setInput(buf: Array[Byte]): Unit =
    setInput(buf, 0, buf.length)

  def setInput(buf: Array[Byte], off: Int, nbytes: Int): Unit =
    if (stream == null) {
      throw new NullPointerException()
    } else if (off <= buf.length && nbytes >= 0 && off >= 0 && buf.length - off >= nbytes) {
      inRead = 0
      inLength = nbytes
      if (buf.length == 0) {
        stream.nextIn = Inflater.empty.at(off)
      } else {
        stream.nextIn = buf.at(off)
      }
      stream.availableIn = nbytes.toUInt
    } else {
      throw new ArrayIndexOutOfBoundsException()
    }

  private def inflateImpl(buf: Array[Byte], off: Int, nbytes: Int): Int = {
    stream.availableOut = nbytes.toUInt
    val sin = stream.totalIn
    val sout = stream.totalOut
    if (buf.length == 0) {
      stream.nextOut = Inflater.empty.at(off)
    } else {
      stream.nextOut = buf.at(off)
    }
    val err = zlib.inflate(stream, zlib.Z_SYNC_FLUSH)

    if (err != zlib.Z_OK) {
      if (err == zlib.Z_STREAM_ERROR) {
        0
      } else if (err == zlib.Z_STREAM_END || err == zlib.Z_NEED_DICT) {
        val totalIn = stream.totalIn
        inRead += (totalIn - sin).toInt

        if (err == zlib.Z_STREAM_END) {
          isFinished = true
        } else {
          doesNeedDictionary = true
        }
        val totalOut = stream.totalOut
        (totalOut - sout).toInt
      } else {
        throw new DataFormatException(err.toString)
      }
    } else {
      val totalIn = stream.totalIn
      val totalOut = stream.totalOut
      inRead += (totalIn - sin).toInt
      (totalOut - sout).toInt
    }
  }

}

private object Inflater {
  // Used when we try to read to a zero-sized array.
  val empty = new Array[Byte](1)

  def createStream(noHeader: Boolean): z_streamp = {
    val stream = stdlib
      .calloc(1.toUSize, z_stream.size)
      .asInstanceOf[z_streamp]
    val wbits: Int =
      if (noHeader) 15 / -1
      else 15
    val err = zlib.inflateInit2(stream, wbits)
    if (err != zlib.Z_OK) {
      stdlib.free(stream.asInstanceOf[Ptr[Byte]])
      throw new ZipException(err.toString)
    }
    stream
  }
}
