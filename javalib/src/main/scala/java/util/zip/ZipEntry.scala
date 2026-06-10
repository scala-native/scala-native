package java.util.zip

// Ported from Apache Harmony. Extensive changes for Scala Native.

import java.io.{
  EOFException, InputStream, RandomAccessFile, UnsupportedEncodingException
}
import java.nio.charset.Charset
import java.nio.file.attribute.FileTime

import scala.scalanative.libc.string.memset
import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.time._
import scala.scalanative.posix.timeOps.tmOps
import scala.scalanative.unsafe._
import scala.scalanative.windows.crt.{time => winTime}

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

  def this(e: ZipEntry) = {
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
    this.mtime = e.mtime
    this.atime = e.atime
    this.ctime = e.ctime
    this.mtimeFromExtra = e.mtimeFromExtra
    this.atimeFromExtra = e.atimeFromExtra
    this.ctimeFromExtra = e.ctimeFromExtra
  }

  if (name == null) {
    throw new NullPointerException()
  }

  if (name.length() > 0xffff) {
    throw new IllegalArgumentException()
  }

  // Java 8 FileTime triple. `Long.MinValue` means "unset"; readers
  // populate these from the UT (0x5455) or NTFS (0x000A) extra
  // fields, writers emit a UT block when any are set. #3788
  private[zip] var mtime: Long = Long.MinValue
  private[zip] var atime: Long = Long.MinValue
  private[zip] var ctime: Long = Long.MinValue
  private[zip] var mtimeFromExtra: Boolean = false
  private[zip] var atimeFromExtra: Boolean = false
  private[zip] var ctimeFromExtra: Boolean = false

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
    // Prefer the high-precision FileTime mtime (from UT/NTFS extra
    // fields) when present. JDK semantics: setTime() clears mtime so
    // legacy callers always see what they just wrote.
    if (mtime != Long.MinValue) mtime
    else if ((time == -1) || (modDate == -1)) -1L
    else {
      val tm = stackalloc[tm]()
      // mktime reads fields beyond the date/time we set (tm_gmtoff,
      // tm_zone, tm_wday on macOS); stackalloc returns uninitialised
      // memory, so zero the struct before populating. #3816
      memset(tm, 0, sizeof[tm])

      tm.tm_year = ((modDate >> 9) & 0x7f) + 80
      tm.tm_mon = ((modDate >> 5) & 0xf) - 1
      tm.tm_mday = modDate & 0x1f

      tm.tm_hour = (time >> 11) & 0x1f
      tm.tm_min = (time >> 5) & 0x3f
      tm.tm_sec = (time & 0x1f) << 1

      tm.tm_isdst = -1

      // DOS dates run to 2107; the 32-bit Windows CRT (`_mktime32`)
      // tops out at 2038-01-19, so use `_mktime64` on Windows.
      val unixEpochSeconds =
        if (isWindows) winTime.mktime64(tm).toLong
        else mktime(tm).toLong

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
      // Match JDK: setting the extra block scans for UT (0x5455) /
      // NTFS (0x000A) timestamp records and updates the FileTime
      // triple. Clear previously parsed values first, but preserve
      // values explicitly set through the FileTime setters when the
      // new extra block has no timestamp record.
      if (mtimeFromExtra) mtime = Long.MinValue
      if (atimeFromExtra) atime = Long.MinValue
      if (ctimeFromExtra) ctime = Long.MinValue
      mtimeFromExtra = false
      atimeFromExtra = false
      ctimeFromExtra = false
      ZipEntry.parseExtraTimestamps(this, data)
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
     * MS-DOS standard time. MS-DOS time is local-zone, so go through
     * the platform's thread-safe local-time decomposition: POSIX
     * `localtime_r` on Unix, `_localtime64_s` on Windows (libc
     * `localtime` returns a static buffer and is not thread-safe;
     * the 32-bit Windows variants cap at 2038-01-19, below the DOS
     * date ceiling of 2107).
     *
     * Format reference:
     *   https://learn.microsoft.com/en-us/windows/win32/api/oleauto/
     *       nf-oleauto-dosdatetimetovarianttime
     */
    // Match JDK: legacy setTime() clears the high-precision FileTime
    // mtime, so a subsequent getTime() reflects exactly what was set.
    mtime = Long.MinValue
    mtimeFromExtra = false
    val tm = stackalloc[tm]()
    // localtime_r populates every field, but zero first as defence
    // against partial writes on exotic libcs. #3816
    memset(tm, 0, sizeof[tm])

    // DOS dates run to 2107; the 32-bit Windows CRT (`__time32_t`)
    // tops out at 2038-01-19, so use 64-bit `_localtime64_s` on Windows.
    val ok =
      if (isWindows) {
        val timer = stackalloc[winTime.time64_t]()
        !timer = value / 1000L
        winTime.localtime64_s(tm, timer) == 0
      } else {
        val timer = stackalloc[time_t]()
        // truncation OK, MS-DOS uses 2 second intervals, no rounding.
        !timer = (value / 1000L).toSize
        localtime_r(timer, tm) != null
      }

    if (!ok) {
      modDate = 0x21
      time = 0
    } else {
      val msDosYears = tm.tm_year - 80

      // msDosYears == 0 is 1980 — the MS-DOS epoch year — and is valid.
      if (msDosYears < 0) {
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

  def getLastModifiedTime(): FileTime =
    if (mtime != Long.MinValue) FileTime.fromMillis(mtime)
    else {
      val t = getTime()
      if (t != -1L) FileTime.fromMillis(t) else null
    }

  def setLastModifiedTime(time: FileTime): ZipEntry = {
    if (time == null) throw new NullPointerException()
    val ms = time.toMillis()
    // setTime() also clears mtime as a JDK-compat side effect; assign
    // afterwards so the FileTime precision survives.
    setTime(ms)
    mtime = ms
    mtimeFromExtra = false
    this
  }

  def getLastAccessTime(): FileTime =
    if (atime != Long.MinValue) FileTime.fromMillis(atime) else null

  def setLastAccessTime(time: FileTime): ZipEntry = {
    if (time == null) throw new NullPointerException()
    atime = time.toMillis()
    atimeFromExtra = false
    this
  }

  def getCreationTime(): FileTime =
    if (ctime != Long.MinValue) FileTime.fromMillis(ctime) else null

  def setCreationTime(time: FileTime): ZipEntry = {
    if (time == null) throw new NullPointerException()
    ctime = time.toMillis()
    ctimeFromExtra = false
    this
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

      val ze = new ZipEntry(
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
      parseExtraTimestamps(ze, extra)
      ze
    } catch {
      case uee: UnsupportedEncodingException =>
        throw new InternalError(uee.getMessage())
    }
  }

  /** Walk a ZIP `extra` byte block (LFH or CDH form) and populate `mtime` /
   *  `atime` / `ctime` on `entry` from any Extended-Timestamp (UT, 0x5455) or
   *  NTFS (0x000A) sub-blocks present. APPNOTE 4.5.7 for UT; APPNOTE 4.5.5 for
   *  NTFS.
   *
   *  UT in the central-directory copy only carries mtime; the local header copy
   *  may also carry atime/ctime. NTFS carries all three in both forms.
   */
  private[zip] def parseExtraTimestamps(
      entry: ZipEntry,
      extra: Array[Byte]
  ): Unit = {
    if (extra == null) return
    var pos = 0
    while (pos + 4 <= extra.length) {
      val tag = (extra(pos) & 0xff) | ((extra(pos + 1) & 0xff) << 8)
      val size = (extra(pos + 2) & 0xff) | ((extra(pos + 3) & 0xff) << 8)
      val dataStart = pos + 4
      if (dataStart + size > extra.length) {
        return // truncated block; bail rather than read past end
      }
      tag match {
        case 0x5455 if size >= 1 =>
          val flags = extra(dataStart) & 0xff
          var p = dataStart + 1
          // Each present timestamp is a 4-byte LE seconds-since-epoch.
          // APPNOTE is silent on signedness; we treat it as unsigned
          // to cover 1970..2106 (matches Info-ZIP and most modern
          // tools, extending the signed-int32 ceiling of 2038-01-19).
          if ((flags & 0x1) != 0 && p + 4 <= dataStart + size) {
            entry.mtime = readUInt32LE(extra, p) * 1000L
            entry.mtimeFromExtra = true
            p += 4
          }
          if ((flags & 0x2) != 0 && p + 4 <= dataStart + size) {
            entry.atime = readUInt32LE(extra, p) * 1000L
            entry.atimeFromExtra = true
            p += 4
          }
          if ((flags & 0x4) != 0 && p + 4 <= dataStart + size) {
            entry.ctime = readUInt32LE(extra, p) * 1000L
            entry.ctimeFromExtra = true
            p += 4
          }
        case 0x000a if size >= 4 =>
          // Reserved (4 bytes), then sub-tags. Sub-tag 0x0001 carries
          // a triple of Windows FILETIMEs (100-ns since 1601-01-01).
          var p = dataStart + 4
          val end = dataStart + size
          while (p + 4 <= end) {
            val subTag = (extra(p) & 0xff) | ((extra(p + 1) & 0xff) << 8)
            val subSize = (extra(p + 2) & 0xff) | ((extra(p + 3) & 0xff) << 8)
            val subStart = p + 4
            if (subStart + subSize > end) {
              p = end // truncated; stop
            } else {
              if (subTag == 0x0001 && subSize >= 24) {
                entry.mtime = filetimeToUnixMillis(readInt64LE(extra, subStart))
                entry.mtimeFromExtra = true
                entry.atime = filetimeToUnixMillis(
                  readInt64LE(extra, subStart + 8)
                )
                entry.atimeFromExtra = true
                entry.ctime = filetimeToUnixMillis(
                  readInt64LE(extra, subStart + 16)
                )
                entry.ctimeFromExtra = true
              }
              p = subStart + subSize
            }
          }
        case _ => () // ignore other tags
      }
      pos = dataStart + size
    }
  }

  private def readInt32LE(b: Array[Byte], off: Int): Int =
    (b(off) & 0xff) | ((b(off + 1) & 0xff) << 8) |
      ((b(off + 2) & 0xff) << 16) | ((b(off + 3) & 0xff) << 24)

  private def readUInt32LE(b: Array[Byte], off: Int): Long =
    readInt32LE(b, off).toLong & 0xffffffffL

  private def readInt64LE(b: Array[Byte], off: Int): Long = {
    val lo = readUInt32LE(b, off)
    val hi = readUInt32LE(b, off + 4)
    (hi << 32) | lo
  }

  // Windows FILETIME counts 100-ns intervals since 1601-01-01 UTC;
  // 11644473600 seconds separate that epoch from the Unix epoch.
  private final val FiletimeUnixEpochOffsetMillis = 11644473600000L
  private def filetimeToUnixMillis(filetime: Long): Long =
    (filetime / 10000L) - FiletimeUnixEpochOffsetMillis

  /** UT seconds field is 4 bytes; we treat it as unsigned (range 1970..2106).
   *  Callers must check `!= Long.MinValue` before calling.
   */
  private def fitsInUInt32Seconds(millis: Long): Boolean = {
    val secs = millis / 1000L
    secs >= 0L && secs <= 0xffffffffL
  }

  /** Build a UT (0x5455) Extended-Timestamp extra-field block for the
   *  timestamps that are set on `entry`. If only mtime is set (or the caller
   *  asks for CDH form), the resulting block is the 9-byte CDH form (tag + size
   *  + flags + mtime); otherwise the LFH form with any subset of
   *  mtime/atime/ctime, in that order.
   *
   *  Returns `null` if no FileTime is set, so callers can leave the existing
   *  `extra` block untouched.
   */
  private[zip] def buildUTExtraBlock(
      entry: ZipEntry,
      includeAtimeCtime: Boolean
  ): Array[Byte] = {
    // If any present timestamp is outside the UT-representable range,
    // skip the whole UT block — partial encoding would silently drop
    // mtime, which is worse than emitting nothing (the MS-DOS time is
    // still written in the LFH/CDH proper). A future NTFS extra-field
    // writer could carry far-future or pre-1970 timestamps.
    val mPresent = entry.mtime != Long.MinValue
    val aPresent = entry.atime != Long.MinValue
    val cPresent = entry.ctime != Long.MinValue
    if (mPresent && !fitsInUInt32Seconds(entry.mtime)) return null
    if (aPresent && !fitsInUInt32Seconds(entry.atime)) return null
    if (cPresent && !fitsInUInt32Seconds(entry.ctime)) return null

    val hasMtime = mPresent
    val hasAtime = aPresent && includeAtimeCtime
    val hasCtime = cPresent && includeAtimeCtime
    // The flags byte reports presence in the LOCAL header per APPNOTE,
    // even for the CDH copy.
    val flagsBit =
      (if (mPresent) 0x1 else 0) |
        (if (aPresent) 0x2 else 0) |
        (if (cPresent) 0x4 else 0)
    if (flagsBit == 0) return null

    val ntimes =
      (if (hasMtime) 1 else 0) + (if (hasAtime) 1 else 0) +
        (if (hasCtime) 1 else 0)
    val size = 1 + 4 * ntimes
    val block = new Array[Byte](4 + size)
    block(0) = 0x55.toByte // 'U'
    block(1) = 0x54.toByte // 'T'
    block(2) = (size & 0xff).toByte
    block(3) = ((size >> 8) & 0xff).toByte
    block(4) = flagsBit.toByte
    var p = 5
    def writeSecs(millis: Long): Unit = {
      // Already bounds-checked; truncation to 32 bits keeps the
      // unsigned encoding.
      val secs = (millis / 1000L).toInt
      block(p) = (secs & 0xff).toByte
      block(p + 1) = ((secs >> 8) & 0xff).toByte
      block(p + 2) = ((secs >> 16) & 0xff).toByte
      block(p + 3) = ((secs >> 24) & 0xff).toByte
      p += 4
    }
    if (hasMtime) writeSecs(entry.mtime)
    if (hasAtime) writeSecs(entry.atime)
    if (hasCtime) writeSecs(entry.ctime)
    block
  }

  private[zip] def mergeExtra(
      existing: Array[Byte],
      addition: Array[Byte]
  ): Array[Byte] = {
    if (addition == null) existing
    else if (existing == null) addition
    else {
      val merged = new Array[Byte](existing.length + addition.length)
      System.arraycopy(existing, 0, merged, 0, existing.length)
      System.arraycopy(addition, 0, merged, existing.length, addition.length)
      merged
    }
  }

  /** Drop any UT (0x5455) and NTFS (0x000A) timestamp blocks from `extra`,
   *  returning the rest of the bytes verbatim. Used before emitting fresh
   *  timestamp blocks so that re-writing an entry that was read from a
   *  UT/NTFS-bearing archive does not produce duplicate (and potentially
   *  conflicting) timestamp records.
   */
  private[zip] def stripTimestampBlocks(extra: Array[Byte]): Array[Byte] = {
    if (extra == null) return null
    val keep = new Array[Byte](extra.length)
    var pos = 0
    var kept = 0
    while (pos + 4 <= extra.length) {
      val tag = (extra(pos) & 0xff) | ((extra(pos + 1) & 0xff) << 8)
      val size = (extra(pos + 2) & 0xff) | ((extra(pos + 3) & 0xff) << 8)
      val blockLen = 4 + size
      if (pos + blockLen > extra.length) {
        // Truncated trailing block — preserve verbatim to avoid
        // silently dropping bytes a different reader might accept.
        System.arraycopy(extra, pos, keep, kept, extra.length - pos)
        kept += (extra.length - pos)
        pos = extra.length
      } else {
        if (tag != 0x5455 && tag != 0x000a) {
          System.arraycopy(extra, pos, keep, kept, blockLen)
          kept += blockLen
        }
        pos += blockLen
      }
    }
    if (kept == 0) null
    else if (kept == extra.length) extra
    else {
      val out = new Array[Byte](kept)
      System.arraycopy(keep, 0, out, 0, kept)
      out
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
