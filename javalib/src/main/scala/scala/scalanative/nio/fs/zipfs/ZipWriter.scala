package scala.scalanative.nio.fs.zipfs

import java.io.{
  BufferedOutputStream, ByteArrayOutputStream, FileOutputStream, IOException,
  InputStream, OutputStream
}
import java.nio.file.{
  AtomicMoveNotSupportedException, Files, Path, Paths, StandardCopyOption
}
import java.util.zip.{
  CRC32, Deflater, ZipEntry, ZipException, ZipFileSystemSupport
}

/* Rewrite a ZipFileSystem to disk by streaming live inodes in insertion
 * order, raw-copying compressed payloads for `Original` inodes and
 * pre-deflating `Modified` ones into a buffer before emitting the LFH
 * (so we never need bit 3 / data descriptor).
 *
 * The three ZIP signature constants are small and well-defined, so they
 * are spelled out here instead of widening `java.util.zip.ZipConstants`
 * to package-public. UT / NTFS extra-block layout is delegated to
 * `java.util.zip.ZipEntry` (via ZipFileSystemSupport) which already
 * builds the bytes the way `ZipOutputStream` does.
 */
private[zipfs] object ZipWriter {

  private final val LOCSIG = 0x04034b50
  private final val CENSIG = 0x02014b50
  private final val ENDSIG = 0x06054b50

  // Minimum extract version (2.0 = 20). We do not produce ZIP64 or
  // encrypted entries.
  private final val VERSION = 20

  // We do not produce ZIP64 archives: entry counts are limited to the
  // 16-bit EOCD field and every size/offset to 32 bits. Exceeding a
  // limit must fail loudly — the writeShortLE/writeIntLE masks would
  // otherwise silently truncate and corrupt the archive.
  private def requireU16(value: Long, what: String): Unit =
    if (value < 0L || value > 0xffffL)
      throw new ZipException(
        s"$what ($value) exceeds 16-bit limit; ZIP64 is not supported"
      )

  private def requireU32(value: Long, what: String): Unit =
    if (value < 0L || value > 0xffffffffL)
      throw new ZipException(
        s"$what ($value) exceeds 32-bit limit; ZIP64 is not supported"
      )

  /** Phase 1: build a sibling temp file holding the rewritten archive. Caller
   *  is responsible for closing `fs.sourceZip` and then calling [[commit]].
   *  Splitting in two lets `ZipFileSystem.close()` release the source ZipFile's
   *  OS handle before renaming the temp over the archive, which Windows
   *  requires.
   */
  def writeToTemp(fs: ZipFileSystem): Path = {
    val archive = fs.archivePath
    val abs = archive.toAbsolutePath()
    val parent: Path = {
      val p = abs.getParent()
      if (p != null) p else Paths.get(".", Array.empty[String])
    }
    val tmp =
      Files.createTempFile(parent, ".zipfs", ".tmp", Array.empty)
    var success = false
    try {
      val raf = new java.io.RandomAccessFile(tmp.toFile(), "rw")
      try writeArchive(fs, raf)
      finally raf.close()
      success = true
      tmp
    } finally {
      if (!success) {
        try Files.deleteIfExists(tmp)
        catch { case _: Throwable => () }
      }
    }
  }

  /** Phase 2: atomically replace `archive` with `tmp` (or non-atomic on
   *  filesystems that reject `ATOMIC_MOVE`). The source ZipFile must already be
   *  closed.
   */
  def commit(tmp: Path, archive: Path): Unit = {
    var renamed = false
    try {
      try {
        Files.move(
          tmp,
          archive,
          Array[java.nio.file.CopyOption](
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING
          )
        )
        renamed = true
      } catch {
        // Some filesystems reject ATOMIC_MOVE with a generic IOException
        // (e.g. cross-device tmpfs) rather than the spec'd subclass —
        // fall back in either case.
        case _: AtomicMoveNotSupportedException =>
          Files.move(
            tmp,
            archive,
            Array[java.nio.file.CopyOption](StandardCopyOption.REPLACE_EXISTING)
          )
          renamed = true
        case _: java.io.IOException =>
          Files.move(
            tmp,
            archive,
            Array[java.nio.file.CopyOption](StandardCopyOption.REPLACE_EXISTING)
          )
          renamed = true
      }
    } finally {
      if (!renamed) {
        try Files.deleteIfExists(tmp)
        catch { case _: Throwable => () }
      }
    }
  }

  // --- writer internals ---

  private final case class CdhRecord(
      name: String,
      method: Int,
      dosTime: Int,
      dosDate: Int,
      crc: Long,
      compressedSize: Long,
      uncompressedSize: Long,
      extra: Array[Byte],
      comment: Array[Byte],
      gpFlags: Int,
      lfhOffset: Long
  )

  private def writeArchive(
      fs: ZipFileSystem,
      raf: java.io.RandomAccessFile
  ): Unit = {
    val records = new java.util.ArrayList[CdhRecord]()
    val it = fs.inodes.entrySet().iterator()
    while (it.hasNext()) {
      val entry = it.next()
      val name = entry.getKey()
      entry.getValue() match {
        case Inode.Deleted                            => ()
        case Inode.Original(srcName, cached, gpFlags) =>
          records.add(emitOriginal(fs, raf, name, srcName, cached, gpFlags))
        case Inode.Modified(bytes, cached, gpFlags) =>
          records.add(emitModified(raf, name, bytes, cached, gpFlags))
      }
    }
    val cdOffset = raf.getFilePointer()
    var i = 0
    val n = records.size()
    requireU16(n.toLong, "entry count")
    while (i < n) {
      writeCdh(raf, records.get(i))
      i += 1
    }
    val cdEnd = raf.getFilePointer()
    val cdSize = cdEnd - cdOffset
    requireU32(cdOffset, "central directory offset")
    requireU32(cdSize, "central directory size")
    writeEocd(raf, n, cdSize, cdOffset)
  }

  private def emitOriginal(
      fs: ZipFileSystem,
      raf: java.io.RandomAccessFile,
      name: String,
      sourceArchiveName: String,
      cached: ZipEntry,
      gpFlags: Int
  ): CdhRecord = {
    val zf = fs.sourceZip.getOrElse(
      throw new IOException(
        s"Original inode without source ZipFile: $name"
      )
    )
    val raw = ZipFileSystemSupport.getRawInputStream(zf, sourceArchiveName)
    if (raw == null)
      throw new IOException(
        s"Source entry not found in archive: $sourceArchiveName"
      )
    try {
      val lfhOffset = raf.getFilePointer()
      requireU32(lfhOffset, s"local header offset of $name")
      requireU32(cached.getCompressedSize(), s"compressed size of $name")
      requireU32(cached.getSize(), s"uncompressed size of $name")
      val (extra, comment) = encodedExtras(name, cached)
      val nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8)
      writeLfh(
        raf,
        gpFlagsForUtf8(gpFlags, nameBytes, name),
        cached.getMethod(),
        ZipFileSystemSupport.getDosTime(cached),
        ZipFileSystemSupport.getDosDate(cached),
        cached.getCrc(),
        cached.getCompressedSize(),
        cached.getSize(),
        nameBytes,
        extra
      )
      // Stream raw compressed bytes verbatim. Track the count so a
      // short read on `getRawInputStream` surfaces here rather than
      // silently breaking every subsequent LFH offset in the CDH.
      val expected = cached.getCompressedSize()
      val buf = new Array[Byte](16 * 1024)
      var copied = 0L
      var n = raw.read(buf)
      while (n != -1) {
        raf.write(buf, 0, n)
        copied += n
        n = raw.read(buf)
      }
      if (expected >= 0L && copied != expected) {
        throw new IOException(
          s"Short raw read for $sourceArchiveName: " +
            s"got $copied bytes, expected $expected"
        )
      }
      CdhRecord(
        name = name,
        method = cached.getMethod(),
        dosTime = ZipFileSystemSupport.getDosTime(cached),
        dosDate = ZipFileSystemSupport.getDosDate(cached),
        crc = cached.getCrc(),
        compressedSize = cached.getCompressedSize(),
        uncompressedSize = cached.getSize(),
        extra = extra,
        comment = encodeComment(cached.getComment()),
        gpFlags = gpFlagsForUtf8(gpFlags, nameBytes, name),
        lfhOffset = lfhOffset
      )
    } finally raw.close()
  }

  private def emitModified(
      raf: java.io.RandomAccessFile,
      name: String,
      bytes: Array[Byte],
      cached: ZipEntry,
      gpFlags: Int
  ): CdhRecord = {
    val isDir = name.endsWith("/")
    val method =
      if (isDir) ZipEntry.STORED
      else
        cached.getMethod() match {
          case ZipEntry.STORED   => ZipEntry.STORED
          case ZipEntry.DEFLATED => ZipEntry.DEFLATED
          case -1    => if (isDir) ZipEntry.STORED else ZipEntry.DEFLATED
          case other =>
            throw new IOException(
              s"Unsupported method $other for entry $name"
            )
        }
    val crc = new CRC32()
    crc.update(bytes)
    val crcValue = crc.getValue()
    val (compressed: Array[Byte], compressedSize: Long) =
      if (method == ZipEntry.STORED) (bytes, bytes.length.toLong)
      else deflate(bytes)

    val (dosTime, dosDate) = chooseDosDateTime(cached)

    val lfhOffset = raf.getFilePointer()
    requireU32(lfhOffset, s"local header offset of $name")
    requireU32(compressedSize, s"compressed size of $name")
    requireU32(bytes.length.toLong, s"uncompressed size of $name")
    val (extra, comment) = encodedExtras(name, cached)
    val nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8)
    val effectiveFlags = gpFlagsForUtf8(gpFlags, nameBytes, name)
    writeLfh(
      raf,
      effectiveFlags,
      method,
      dosTime,
      dosDate,
      crcValue,
      compressedSize,
      bytes.length.toLong,
      nameBytes,
      extra
    )
    raf.write(compressed, 0, compressedSize.toInt)

    CdhRecord(
      name = name,
      method = method,
      dosTime = dosTime,
      dosDate = dosDate,
      crc = crcValue,
      compressedSize = compressedSize,
      uncompressedSize = bytes.length.toLong,
      extra = extra,
      comment = comment,
      gpFlags = effectiveFlags,
      lfhOffset = lfhOffset
    )
  }

  private def deflate(bytes: Array[Byte]): (Array[Byte], Long) = {
    // nowrap=true: raw DEFLATE stream, no zlib header/trailer. ZIP
    // stores raw streams; default Deflater would add a 2-byte header
    // + 4-byte Adler-32 trailer that an Inflater(nowrap=true) reader
    // can't decode.
    val def_ = new Deflater(Deflater.DEFAULT_COMPRESSION, true)
    try {
      def_.setInput(bytes)
      def_.finish()
      val baos = new ByteArrayOutputStream(bytes.length)
      val buf = new Array[Byte](4096)
      while (!def_.finished()) {
        val n = def_.deflate(buf)
        if (n > 0) baos.write(buf, 0, n)
      }
      val out = baos.toByteArray()
      (out, out.length.toLong)
    } finally def_.end()
  }

  /** Pick a (dosTime, dosDate) pair for a Modified entry. Prefer the cached
   *  entry's existing encoded pair; if missing, use the mtime via setTime →
   *  ZipEntry encodes the LE shorts for us.
   */
  private def chooseDosDateTime(cached: ZipEntry): (Int, Int) = {
    val t = ZipFileSystemSupport.getDosTime(cached)
    val d = ZipFileSystemSupport.getDosDate(cached)
    if (t != -1 && d != -1) (t, d)
    else {
      val now =
        if (cached.getTime() != -1L) cached.getTime()
        else System.currentTimeMillis()
      // Round-trip through a temp entry so the timezone-sensitive
      // conversion lives in exactly one place (ZipEntry.setTime).
      val tmp = new ZipEntry("_tmp_")
      tmp.setTime(now)
      (
        ZipFileSystemSupport.getDosTime(tmp),
        ZipFileSystemSupport.getDosDate(tmp)
      )
    }
  }

  private def encodedExtras(
      name: String,
      cached: ZipEntry
  ): (Array[Byte], Array[Byte]) = {
    val baseExtra = cached.getExtra()
    val stripped = ZipFileSystemSupport.stripTimestampBlocks(baseExtra)
    val ut =
      ZipFileSystemSupport.buildUTExtraBlock(cached, includeAtimeCtime = true)
    val merged = ZipFileSystemSupport.mergeExtra(stripped, ut)
    val extra = if (merged == null) new Array[Byte](0) else merged
    val comment = encodeComment(cached.getComment())
    (extra, comment)
  }

  private def encodeComment(comment: String): Array[Byte] = {
    if (comment == null) new Array[Byte](0)
    else comment.getBytes(java.nio.charset.StandardCharsets.UTF_8)
  }

  /** Ensure bit 11 (UTF-8 names) is set whenever the encoded name (or comment)
   *  contains any byte outside ASCII. Mirrors what ZipOutputStream does for
   *  newly emitted entries.
   */
  private def gpFlagsForUtf8(
      base: Int,
      nameBytes: Array[Byte],
      name: String
  ): Int = {
    val nonAscii = name.length != nameBytes.length || {
      var i = 0
      var seen = false
      while (i < nameBytes.length && !seen) {
        if ((nameBytes(i) & 0x80) != 0) seen = true
        i += 1
      }
      seen
    }
    if (nonAscii) base | 0x0800 else base
  }

  // --- wire-format writers ---

  private def writeLfh(
      raf: java.io.RandomAccessFile,
      gpFlags: Int,
      method: Int,
      dosTime: Int,
      dosDate: Int,
      crc: Long,
      compressedSize: Long,
      uncompressedSize: Long,
      nameBytes: Array[Byte],
      extra: Array[Byte]
  ): Unit = {
    writeIntLE(raf, LOCSIG)
    writeShortLE(raf, VERSION)
    writeShortLE(raf, gpFlags & ~0x0008) // never emit bit 3
    writeShortLE(raf, method)
    writeShortLE(raf, dosTime & 0xffff)
    writeShortLE(raf, dosDate & 0xffff)
    writeIntLE(raf, (crc & 0xffffffffL).toInt)
    writeIntLE(raf, (compressedSize & 0xffffffffL).toInt)
    writeIntLE(raf, (uncompressedSize & 0xffffffffL).toInt)
    writeShortLE(raf, nameBytes.length)
    writeShortLE(raf, extra.length)
    raf.write(nameBytes)
    if (extra.length > 0) raf.write(extra)
  }

  private def writeCdh(
      raf: java.io.RandomAccessFile,
      r: CdhRecord
  ): Unit = {
    val nameBytes = r.name.getBytes(java.nio.charset.StandardCharsets.UTF_8)
    writeIntLE(raf, CENSIG)
    writeShortLE(raf, VERSION) // version made by
    writeShortLE(raf, VERSION) // version needed to extract
    writeShortLE(raf, r.gpFlags & ~0x0008)
    writeShortLE(raf, r.method)
    writeShortLE(raf, r.dosTime & 0xffff)
    writeShortLE(raf, r.dosDate & 0xffff)
    writeIntLE(raf, (r.crc & 0xffffffffL).toInt)
    writeIntLE(raf, (r.compressedSize & 0xffffffffL).toInt)
    writeIntLE(raf, (r.uncompressedSize & 0xffffffffL).toInt)
    writeShortLE(raf, nameBytes.length)
    writeShortLE(raf, r.extra.length)
    writeShortLE(raf, r.comment.length)
    writeShortLE(raf, 0) // disk number start
    writeShortLE(raf, 0) // internal file attributes
    writeIntLE(raf, 0) // external file attributes
    writeIntLE(raf, (r.lfhOffset & 0xffffffffL).toInt)
    raf.write(nameBytes)
    if (r.extra.length > 0) raf.write(r.extra)
    if (r.comment.length > 0) raf.write(r.comment)
  }

  private def writeEocd(
      raf: java.io.RandomAccessFile,
      numEntries: Int,
      cdSize: Long,
      cdOffset: Long
  ): Unit = {
    writeIntLE(raf, ENDSIG)
    writeShortLE(raf, 0) // disk number
    writeShortLE(raf, 0) // disk with start of CD
    writeShortLE(raf, numEntries)
    writeShortLE(raf, numEntries)
    writeIntLE(raf, (cdSize & 0xffffffffL).toInt)
    writeIntLE(raf, (cdOffset & 0xffffffffL).toInt)
    writeShortLE(raf, 0) // comment length
  }

  private def writeShortLE(raf: java.io.RandomAccessFile, v: Int): Unit = {
    raf.write(v & 0xff)
    raf.write((v >>> 8) & 0xff)
  }

  private def writeIntLE(raf: java.io.RandomAccessFile, v: Int): Unit = {
    raf.write(v & 0xff)
    raf.write((v >>> 8) & 0xff)
    raf.write((v >>> 16) & 0xff)
    raf.write((v >>> 24) & 0xff)
  }
}
