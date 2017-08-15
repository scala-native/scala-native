package java.util.zip

// Ported from Apache Harmony

import java.io.{
  EOFException,
  IOException,
  InputStream,
  RandomAccessFile,
  UnsupportedEncodingException
}
import java.util.{Calendar, Date, GregorianCalendar}

class ZipEntry private (private[zip] var name: String,
                        private[zip] var comment: String,
                        private[zip] var compressedSize: Long,
                        private[zip] var crc: Long,
                        private[zip] var size: Long,
                        private[zip] var compressionMethod: Int,
                        private[zip] var time: Int,
                        private[zip] var modDate: Int,
                        private[zip] var extra: Array[Byte],
                        private[zip] var nameLen: Int,
                        private[zip] var mLocalHeaderRelOffset: Long)
    extends ZipConstants
    with Cloneable {

  def this(name: String) =
    this(name, null, -1, -1, -1, -1, -1, -1, null, -1, -1)

  def this(e: ZipEntry) =
    this(e.name,
         e.comment,
         e.compressedSize,
         e.crc,
         e.size,
         e.compressionMethod,
         e.time,
         e.modDate,
         e.extra,
         e.nameLen,
         e.mLocalHeaderRelOffset)

  if (name == null) {
    throw new NullPointerException()
  }
  if (name.length() > 0xFFFF) {
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

  def getTime(): Long =
    -1
  // TODO: Uncomment once we have Calendar
  // if (time != -1) {
  //   val cal = new GregorianCalendar()
  //   cal.set(Calendar.MILLISECOND, 0)
  //   cal.set(1980 + ((modDate >> 9) & 0x7f),
  //           ((modDate >> 5) & 0xf) - 1,
  //           modDate & 0x1f,
  //           (time >> 11) & 0x1f,
  //           (time >> 5) & 0x3f,
  //           (time & 0x1f) << 1)
  //   cal.getTime().getTime()
  // } else {
  //   -1
  // }

  def isDirectory(): Boolean =
    name.charAt(name.length - 1) == '/'

  def setComment(string: String): Unit =
    if (string == null || string.length <= 0xFFFF) {
      comment = string
    } else {
      throw new IllegalArgumentException()
    }

  def setCompressedSize(value: Long): Unit =
    compressedSize = value

  def setCrc(value: Long): Unit = {
    if (value >= 0 && value <= 0xFFFFFFFFL) {
      crc = value
    } else {
      throw new IllegalArgumentException()
    }
  }

  def setExtra(data: Array[Byte]): Unit = {
    if (data == null || data.length <= 0xFFFF) {
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
    if (value >= 0 && value <= 0xFFFFFFFFL) {
      size = value
    } else {
      throw new IllegalArgumentException()
    }

  def setTime(value: Long): Unit = {
    // TODO: Uncomment once we have Date
    // val cal = new GregorianCalendar()
    // cal.setTime(new Date(value))
    // val year = cal.get(Calendar.YEAR)
    // if (year < 1980) {
    //   modDate = 0x21
    //   time = 0
    // } else {
    //   modDate = cal.get(Calendar.DATE)
    //   modDate = (cal.get(Calendar.MONTH) + 1 << 5) | modDate
    //   modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate
    //   time = cal.get(Calendar.SECOND) >> 1
    //   time = (cal.get(Calendar.MINUTE) << 5) | time
    //   time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time
    // }
  }

  override def toString(): String =
    name

  override def clone(): Object =
    new ZipEntry(this)

  override def hashCode(): Int =
    name.hashCode()

}

object ZipEntry extends ZipConstants {
  final val DEFLATED: Int = 8
  final val STORED: Int   = 0

  private def myReadFully(in: InputStream, b: Array[Byte]): Unit = {
    var len = b.length
    var off = 0

    while (len > 0) {
      val count = in.read(b, off, len)
      if (count <= 0) {
        throw new EOFException()
      }
      off += count
      len -= count
    }
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

  def fromInputStream(ler: LittleEndianReader, in: InputStream): ZipEntry = {
    val hdrBuf = ler.hdrBuf
    myReadFully(in, hdrBuf)

    val sig = ((hdrBuf(0) & 0xFF) | ((hdrBuf(1) & 0xFF) << 8) | ((hdrBuf(2) & 0xFF) << 16) | ((hdrBuf(
      3) & 0xFF) << 24)).toLong & 0xFFFFFFFFL
    if (sig != CENSIG) {
      throw new ZipException("Central Directory Entry not found")
    }

    val compressionMethod = (hdrBuf(10) & 0xff) | ((hdrBuf(11) & 0xff) << 8)
    val time              = (hdrBuf(12) & 0xff) | ((hdrBuf(13) & 0xff) << 8)
    val modDate           = (hdrBuf(14) & 0xff) | ((hdrBuf(15) & 0xff) << 8)
    val crc = (hdrBuf(16) & 0xff) | ((hdrBuf(17) & 0xff) << 8) | ((hdrBuf(18) & 0xff) << 16) | ((hdrBuf(
      19) << 24) & 0xffffffffL)
    val compressedSize = (hdrBuf(20) & 0xff) | ((hdrBuf(21) & 0xff) << 8) | ((hdrBuf(
      22) & 0xff) << 16) | ((hdrBuf(23) << 24) & 0xffffffffL)
    val size = (hdrBuf(24) & 0xff) | ((hdrBuf(25) & 0xff) << 8) | ((hdrBuf(26) & 0xff) << 16) | ((hdrBuf(
      27) << 24) & 0xffffffffL)
    val nameLen    = (hdrBuf(28) & 0xff) | ((hdrBuf(29) & 0xff) << 8)
    val extraLen   = (hdrBuf(30) & 0xff) | ((hdrBuf(31) & 0xff) << 8)
    val commentLen = (hdrBuf(32) & 0xff) | ((hdrBuf(33) & 0xff) << 8)
    val mLocalHeaderRelOffset = (hdrBuf(42) & 0xff) | ((hdrBuf(43) & 0xff) << 8) | ((hdrBuf(
      44) & 0xff) << 16) | ((hdrBuf(45) << 24) & 0xffffffffL)

    val nameBytes = new Array[Byte](nameLen)
    myReadFully(in, nameBytes)

    val commentBytes =
      if (commentLen > 0) {
        val commentBytes = new Array[Byte](commentLen)
        myReadFully(in, commentBytes)
        commentBytes
      } else {
        null
      }

    val extra =
      if (extraLen > 0) {
        val extra = new Array[Byte](extraLen)
        myReadFully(in, extra)
        extra
      } else {
        null
      }

    try {
      /*
       * The actual character set is "IBM Code Page 437".  As of
       * Sep 2006, the Zip spec (APPNOTE.TXT) supports UTF-8.  When
       * bit 11 of the GP flags field is set, the file name and
       * comment fields are UTF-8.
       *
       * TODO: add correct UTF-8 support.
       */
      val name = new String(nameBytes, "iso-8859-1")
      val comment =
        if (commentBytes != null) new String(commentBytes, "iso-8859-1")
        else null
      new ZipEntry(name,
                   comment,
                   compressedSize,
                   crc,
                   size,
                   compressionMethod,
                   time,
                   modDate,
                   extra,
                   nameLen,
                   mLocalHeaderRelOffset)
    } catch {
      case uee: UnsupportedEncodingException =>
        throw new InternalError(uee.getMessage())
    }
  }

  private[zip] class LittleEndianReader extends ZipConstants {
    private val b: Array[Byte] = new Array[Byte](4)
    val hdrBuf                 = new Array[Byte](CENHDR)

    def readShortLE(in: InputStream): Int =
      if (in.read(b, 0, 2) == 2) {
        (b(0) & 0xFF) | ((b(1) & 0xFF) << 8)
      } else {
        throw new EOFException()
      }

    def readIntLE(in: InputStream): Int =
      if (in.read(b, 0, 4) == 4) {
        ((((b(0) & 0xFF) | ((b(1) & 0xFF) << 8) | ((b(2) & 0xFF) << 16) | ((b(
          3) & 0xFF) << 24))).toLong & 0xFFFFFFFFL).toInt
      } else {
        throw new EOFException()
      }
  }
}
