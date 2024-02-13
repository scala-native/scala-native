package java.util.zip

// Ported from Apache Harmony

import java.nio.charset.{Charset, StandardCharsets}
import java.io.{
  BufferedInputStream,
  Closeable,
  File,
  InputStream,
  RandomAccessFile
}

import java.util.Enumeration

import java.{util => ju}
import java.util.{stream => jus}

class ZipFile(file: File, mode: Int, charset: Charset) extends Closeable {
  def this(file: File, mode: Int) = this(file, mode, StandardCharsets.UTF_8)
  def this(file: File, charset: Charset) =
    this(file, ZipFile.OPEN_READ, charset)
  def this(file: File) = this(file, StandardCharsets.UTF_8)
  def this(name: String, charset: Charset) = this(new File(name), charset)
  def this(name: String) = this(name, StandardCharsets.UTF_8)

  private final val fileName: String = file.getPath()

  if (mode != ZipFile.OPEN_READ &&
      mode != (ZipFile.OPEN_READ | ZipFile.OPEN_DELETE)) {
    throw new IllegalArgumentException()
  }

  private var fileToDeleteOnClose =
    if ((mode & ZipFile.OPEN_DELETE) != 0) {
      file
    } else {
      null
    }

  private var mRaf = new RandomAccessFile(fileName, "r")
  private val ler = new ZipEntry.LittleEndianReader()

  /* A note on concurrency:
   *
   *   One might expect to see the mEntires map wrapped in the heavy weight
   *   ju.Collections.synchronizedMap().  It may come to that but it would
   *   be nice to avoid that cost, if correctness can be established/maintained.
   *
   *   Most users of this class will probably be using it in a single thread.
   *   jdk.zipfs is defined in such a way that two or more threads in the
   *   same JVM/SN might come to access the same instance of this class.
   *
   *   The central directory is read when this class is instantiated, so
   *   a second instantiation would be blocked until the first completed.
   *
   *   All other uses of mEnties are read-only, so synchronization is OK
   *   there. Reading the entries is synchronized in RAF.
   *
   *   The working presumption is that any I/O reads done by this class
   *   that might block have the @blocking annotation at actual method
   *   which could block.
   *
   *   Now, let Cold Experience show what I have missed in this analysis.
   */

  /* The magic number 64 is a guess.
   * With the defaults of 16 entries and a 0.75 load factor, the map will
   * resize at 12 entires.  Seems like most zip archives will have more than
   * that number. With specified 64 entries and the default load factor,
   * the map will resize at 48 entries & the "growth" will be be larger to
   * boot. The number could probably be more like 128 or so.
   */

  private val mEntries = ju.LinkedHashMap[String, ZipEntry](64)

  readCentralDir()

  override protected def finalize(): Unit =
    close()

  def close(): Unit = {
    val raf = mRaf

    if (raf != null) {
      raf.synchronized {
        mRaf = null
        raf.close()
      }
      if (fileToDeleteOnClose != null) {
        fileToDeleteOnClose.delete()
        fileToDeleteOnClose = null
      }
    }
  }

  private def checkNotClosed(): Unit =
    if (mRaf == null) {
      throw new IllegalStateException("Zip file closed.")
    }

  def entries(): Enumeration[_ <: ZipEntry] = {
    checkNotClosed()
    val iterator = mEntries.values().iterator()

    new Enumeration[ZipEntry] {
      override def hasMoreElements(): Boolean = {
        checkNotClosed()
        iterator.hasNext()
      }

      override def nextElement(): ZipEntry = {
        checkNotClosed()
        iterator.next()
      }
    }
  }

  def getEntry(entryName: String): ZipEntry = {
    checkNotClosed()
    if (entryName == null)
      throw new NullPointerException()

    mEntries.getOrDefault(entryName + "/", null)
  }

  def getInputStream(_entry: ZipEntry): InputStream = {
    if (_entry == null) {
      throw new NullPointerException()
    }
    val entry = getEntry(_entry.getName())

    if (entry == null) {
      null
    } else {
      val raf = mRaf
      raf.synchronized {
        // We don't know the entry data's start position. All we have is the
        // position of the entry's local header. At position 28 we find the
        // length of the extra data. In some cases this length differs from
        // the one coming in the central header.
        val rafstrm =
          new ZipFile.RAFStream(raf, entry.mLocalHeaderRelOffset + 28)
        val localExtraLenOrWhatever = ler.readShortLE(rafstrm)
        // Skip the name and this "extra" data or whatever it is:
        rafstrm.skip(entry.nameLen + localExtraLenOrWhatever)
        rafstrm.mLength = rafstrm.mOffset + entry.compressedSize
        if (entry.compressionMethod == ZipEntry.DEFLATED) {
          val bufSize = Math.max(1024, Math.min(entry.getSize(), 65535L).toInt)
          new ZipFile.ZipInflaterInputStream(
            rafstrm,
            new Inflater(true),
            bufSize,
            entry
          )
        } else {
          rafstrm
        }
      }
    }
  }

  def getName(): String =
    fileName

  def size(): Int = {
    checkNotClosed()
    mEntries.size()
  }

  def stream(): jus.Stream[ZipEntry] = {
    val spliter = mEntries.values().spliterator()
    jus.StreamSupport.stream(spliter, parallel = false)
  }

  private def readCentralDir(): Unit = {
    var scanOffset = mRaf.length() - ZipFile.ENDHDR
    if (scanOffset < 0) {
      throw new ZipException("too short to be Zip")
    }

    var stopOffset = scanOffset - 65536
    if (stopOffset < 0) {
      stopOffset = 0
    }

    var done: Boolean = false
    while (!done) {
      mRaf.seek(scanOffset)
      if (ZipEntry.readIntLE(mRaf) == 101010256L) {
        done = true
      } else {
        scanOffset -= 1
        if (scanOffset < stopOffset) {
          throw new ZipException("EOCD not found; not a Zip archive?")
        }
      }
    }

    /*
     * Found it, read the EOCD.
     *
     * For performance we want to use buffered I/O when reading the
     * file.  We wrap a buffered stream around the random-access file
     * object.  If we just read from the RandomAccessFile we'll be
     * doing a read() system call every time.
     */
    var rafs = new ZipFile.RAFStream(mRaf, mRaf.getFilePointer())
    var bin = new BufferedInputStream(rafs, ZipFile.ENDHDR)

    val diskNumber = ler.readShortLE(bin)
    val diskWithCentralDir = ler.readShortLE(bin)
    val numEntries = ler.readShortLE(bin)
    val totalNumEntries = ler.readShortLE(bin)
    /*centralDirSize =*/
    ler.readIntLE(bin)
    val centralDirOffset = ler.readIntLE(bin)
    /*commentLen =*/
    ler.readShortLE(bin)

    if (numEntries != totalNumEntries || diskNumber != 0 || diskWithCentralDir != 0) {
      throw new ZipException("spanned archves not supported")
    }

    /*
     * Seek to the first CDE and read all entries.
     * However, when Z_SYNC_FLUSH is used the offset may not point directly
     * to the CDE so skip over until we find it.
     * At most it will be 6 bytes away (one or two bytes for empty block, 4 bytes for
     * empty block signature).
     */
    scanOffset = centralDirOffset
    stopOffset = scanOffset + 6

    done = false
    while (!done) {
      mRaf.seek(scanOffset)
      if (ZipEntry.readIntLE(mRaf) == ZipFile.CENSIG) {
        done = true
      } else {
        scanOffset += 1
        if (scanOffset > stopOffset) {
          throw new ZipException("Central Directory Entry not found")
        }
      }
    }

    // If CDE is found then go and read all the entries
    rafs = new ZipFile.RAFStream(mRaf, scanOffset)
    bin = new BufferedInputStream(rafs, 4096)

    var i = 0
    while (i < numEntries) {
      val newEntry = ZipEntry.fromInputStream(ler, bin)
      mEntries.put(newEntry.getName(), newEntry)
      i += 1
    }
  }

}

object ZipFile extends ZipConstants {
  final val OPEN_READ = 1
  final val OPEN_DELETE = 4

  private class RAFStream(
      private var mSharedRaf: RandomAccessFile,
      private[zip] var mOffset: Long
  ) extends InputStream {
    private[zip] var mLength = mSharedRaf.length()

    override def available(): Int = {
      if (mLength > mOffset) {
        if (mLength - mOffset < Int.MaxValue) {
          (mLength - mOffset).toInt
        } else {
          Int.MaxValue
        }
      } else {
        0
      }
    }

    override def read(): Int = {
      val singleByteBuf = new Array[Byte](1)
      if (read(singleByteBuf, 0, 1) == 1) {
        singleByteBuf(0) & 0xff
      } else {
        -1
      }
    }

    override def read(b: Array[Byte], off: Int, _len: Int): Int =
      mSharedRaf.synchronized {
        var len = _len
        mSharedRaf.seek(mOffset)
        if (len > mLength - mOffset) {
          len = (mLength - mOffset).toInt
        }
        val count = mSharedRaf.read(b, off, len)
        if (count > 0) {
          mOffset += count
          count
        } else {
          -1
        }
      }
  }

  private class ZipInflaterInputStream(
      is: InputStream,
      inf: Inflater,
      bsize: Int,
      private var entry: ZipEntry
  ) extends InflaterInputStream(is, inf, bsize) {
    private var bytesRead: Long = 0L

    override def read(buffer: Array[Byte], off: Int, nbytes: Int): Int = {
      val i = super.read(buffer, off, nbytes)
      if (i != -1) {
        bytesRead += i
      }
      i
    }

    override def available(): Int = {
      if (super.available() == 0) {
        0
      } else {
        (entry.getSize() - bytesRead).toInt
      }
    }
  }

}
