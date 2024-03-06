package java.util.zip

// Ported from Apache Harmony. Extensive changes for Scala Native.

import java.io.{
  EOFException,
  InputStream,
  RandomAccessFile,
  UnsupportedEncodingException
}

import java.nio.charset.Charset

import scala.scalanative.posix.time._
import scala.scalanative.posix.timeOps.tmOps

import scala.scalanative.unsafe._

class ZipEntry private (
    private[zip] val name: String, // immutable for safety
    private[zip] var comment: String,
    private[zip] var compressedSize: Long,
    private[zip] var crc: Long,
    private[zip] var size: Long,
    private[zip] var compressionMethod: Int,
    private[zip] var time: Int,
    private[zip] var modDate: Int,
    private[zip] var extra: Array[Byte],
    private[zip] var mLocalHeaderRelOffset: Long
) extends ZipConstants
    with Cloneable {

  def this(name: String) =
    this(name, null, -1L, -1L, -1L, -1, -1, -1, null, -1L)

  def this(e: ZipEntry) =
    this(
      e.name,
      e.comment,
      e.compressedSize,
      e.crc,
      e.size,
      e.compressionMethod,
      e.time,
      e.modDate,
      e.extra,
      e.mLocalHeaderRelOffset
    )

  if (name == null) {
    throw new NullPointerException()
  }

  if (name.length() > 0xffff) {
    throw new IllegalArgumentException()
  }

  def getComment(): String =
    comment

  def getCompressedSize(): Long =
    compressedSize

  def getCrc(): Long =
    crc

  def getExtra(): Array[Byte] =
    extra

  def getMethod(): Int =
    compressionMethod

  def getName(): String =
    name

  def getSize(): Long =
    size

  def getTime(): Long = {
    if ((time == -1) || (modDate == -1)) -1L
    else
      synchronized {
        val tm = stackalloc[tm]()

        tm.tm_year = ((modDate >> 9) & 0x7f) + 80
        tm.tm_mon = ((modDate >> 5) & 0xf) - 1
        tm.tm_mday = modDate & 0x1f

        tm.tm_hour = (time >> 11) & 0x1f
        tm.tm_min = (time >> 5) & 0x3f
        tm.tm_sec = (time & 0x1f) << 1

        tm.tm_isdst = -1

        val unixEpochSeconds = mktime(tm)

        if (unixEpochSeconds < 0) -1L // Per JVM doc, -1 means "Unspecified"
        else unixEpochSeconds * 1000L
      }
  }

  def isDirectory(): Boolean =
    name.charAt(name.length - 1) == '/'

  def setComment(string: String): Unit = {
    /* This length is a count of Java UTF-16 characters. It is
     * accurate for Strings which contain characters < 128 but may
     * not be for greater values.
     *
     * Depending on the charset given to ZipOutputStream, its conversion
     * to bytes may generate more than lengthLimit bytes, resulting in
     * truncation that is not obvious or tested here.
     */
    val lengthLimit = 0xffff
    comment =
      if (string == null || string.length() <= lengthLimit) string
      else string.substring(0, lengthLimit)
  }

  def setCompressedSize(value: Long): Unit =
    compressedSize = value

  def setCrc(value: Long): Unit = {
    if (value >= 0 && value <= 0xffffffffL) {
      crc = value
    } else {
      throw new IllegalArgumentException()
    }
  }

  def setExtra(data: Array[Byte]): Unit = {
    if (data == null || data.length <= 0xffff) {
      extra = data
    } else {
      throw new IllegalArgumentException()
    }
  }

  def setMethod(value: Int): Unit =
    if (value != ZipEntry.STORED && value != ZipEntry.DEFLATED) {
      throw new IllegalArgumentException()
    } else {
      compressionMethod = value
    }

  def setSize(value: Long): Unit =
    if (value >= 0 && value <= 0xffffffffL) {
      size = value
    } else {
      throw new IllegalArgumentException()
    }

  def setTime(value: Long): Unit = {
    /* Convert Java time in milliseconds since the Unix epoch to
     * MS-DOS standard time.
     *
     * This URL gives a good description of standard MS-DOS time & the
     * required bit manipulations:
     *     https://learn.microsoft.com/en-us/windows/win32/api/oleauto/
     *         nf-oleauto-dosdatetimetovarianttime
     *
     * Someone familiar with Windows could probably provide an operating
     * system specific version of this method.
     */

    /* Concurrency issue:
     *   localtime() is not required to be thread-safe, but is likely to exist
     *   on Windows. Change to known thread-safe localtime_r() when this
     *   section is unix-only.
     */

    val timer = stackalloc[time_t]()

    // truncation OK, MS-DOS uses 2 second intervals, no rounding.
    !timer = (value / 1000L).toSize

    val tm = localtime(timer) // Not necessarily thread safe.

    if (tm == null) {
      modDate = 0x21
      time = 0
    } else {
      val msDosYears = tm.tm_year - 80

      if (msDosYears <= 0) {
        modDate = 0x21 // 01-01-1980 00:00 MS-DOS epoch
        time = 0
      } else {
        modDate = tm.tm_mday
        modDate = ((tm.tm_mon + 1) << 5) | modDate
        modDate = (msDosYears << 9) | modDate

        time = tm.tm_sec >> 1
        time = (tm.tm_min << 5) | time
        time = (tm.tm_hour << 11) | time
      }
    }
  }

  override def toString(): String =
    name

  override def clone(): Object =
    new ZipEntry(this)

  override def hashCode(): Int =
    name.hashCode()

}

object ZipEntry extends ZipConstants {
  final val DEFLATED = 8
  final val STORED = 0

  private[zip] def myReadFully(in: InputStream, b: Array[Byte]): Array[Byte] = {
    var len = b.length
    var off = 0

    while (len > 0) {
      val count = in.read(b, off, len)
      if (count <= 0)
        throw new EOFException()

      off += count
      len -= count
    }

    b
  }

  private[zip] def readIntLE(raf: RandomAccessFile): Long = {
    val b0 = raf.read()
    val b1 = raf.read()
    val b2 = raf.read()
    val b3 = raf.read()

    if (b3 < 0) {
      throw new EOFException()
    } else {
      b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) // ATTENTION: DOES SIGN EXTENSION: IS THIS WANTED?
    }
  }

  private[zip] def fromInputStream(
      ler: LittleEndianReader,
      in: InputStream,
      defaultCharset: Charset
  ): ZipEntry = {
    val hdrBuf = myReadFully(in, ler.hdrBuf)

    val sig =
      ((hdrBuf(0) & 0xff) | ((hdrBuf(1) & 0xff) << 8) |
        ((hdrBuf(2) & 0xff) << 16) | ((hdrBuf(3) & 0xff) << 24)).toLong &
        0xffffffffL
    if (sig != CENSIG) {
      throw new ZipException("Central Directory Entry not found")
    }

    val gpBitFlag = ((hdrBuf(8) & 0xff) | ((hdrBuf(9) & 0xff) << 8)).toShort
    val compressionMethod = (hdrBuf(10) & 0xff) | ((hdrBuf(11) & 0xff) << 8)
    val time = (hdrBuf(12) & 0xff) | ((hdrBuf(13) & 0xff) << 8)
    val modDate = (hdrBuf(14) & 0xff) | ((hdrBuf(15) & 0xff) << 8)
    val crc =
      (hdrBuf(16) & 0xff) | ((hdrBuf(17) & 0xff) << 8) | ((hdrBuf(
        18
      ) & 0xff) << 16) | ((hdrBuf(19) << 24) & 0xffffffffL)
    val compressedSize =
      (hdrBuf(20) & 0xff) | ((hdrBuf(21) & 0xff) << 8) | ((hdrBuf(
        22
      ) & 0xff) << 16) | ((hdrBuf(23) << 24) & 0xffffffffL)
    val size =
      (hdrBuf(24) & 0xff) | ((hdrBuf(25) & 0xff) << 8) | ((hdrBuf(
        26
      ) & 0xff) << 16) | ((hdrBuf(27) << 24) & 0xffffffffL)

    val nameLen = (hdrBuf(28) & 0xff) | ((hdrBuf(29) & 0xff) << 8)
    val extraLen = (hdrBuf(30) & 0xff) | ((hdrBuf(31) & 0xff) << 8)
    val commentLen = (hdrBuf(32) & 0xff) | ((hdrBuf(33) & 0xff) << 8)

    val mLocalHeaderRelOffset =
      (hdrBuf(42) & 0xff) | ((hdrBuf(43) & 0xff) << 8) | ((hdrBuf(
        44
      ) & 0xff) << 16) | ((hdrBuf(45) << 24) & 0xffffffffL)

    val nameBytes = myReadFully(in, new Array[Byte](nameLen))

    val extra =
      if (extraLen <= 0) null
      else myReadFully(in, new Array[Byte](extraLen))

    val commentBytes =
      if (commentLen <= 0) null
      else myReadFully(in, new Array[Byte](commentLen))

    try {
      val name =
        ZipByteConversions.bytesToString(nameBytes, gpBitFlag, defaultCharset)

      val comment =
        ZipByteConversions.bytesToString(
          commentBytes,
          gpBitFlag,
          defaultCharset
        )

      new ZipEntry(
        name,
        comment,
        compressedSize,
        crc,
        size,
        compressionMethod,
        time,
        modDate,
        extra,
        mLocalHeaderRelOffset
      )
    } catch {
      case uee: UnsupportedEncodingException =>
        throw new InternalError(uee.getMessage())
    }
  }

  private[zip] class LittleEndianReader extends ZipConstants {
    private val b: Array[Byte] = new Array[Byte](4)
    val hdrBuf = new Array[Byte](CENHDR)

    def readShortLE(in: InputStream): Int =
      if (in.read(b, 0, 2) == 2) {
        (b(0) & 0xff) | ((b(1) & 0xff) << 8)
      } else {
        throw new EOFException()
      }

    def readIntLE(in: InputStream): Int =
      if (in.read(b, 0, 4) == 4) {
        ((((b(0) & 0xff) | ((b(1) & 0xff) << 8) | ((b(2) & 0xff) << 16) |
          ((b(3) & 0xff) << 24))).toLong & 0xffffffffL).toInt
      } else {
        throw new EOFException()
      }
  }
}
