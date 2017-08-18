package java.util.zip

// Ported from Apache Harmony

import java.io.OutputStream

class GZIPOutputStream(out: OutputStream, size: Int, syncFlush: Boolean)
    extends DeflaterOutputStream(
      out,
      new Deflater(Deflater.DEFAULT_COMPRESSION, true),
      size,
      syncFlush) {

  protected var crc: CRC32 = new CRC32()

  def this(out: OutputStream, syncFlush: Boolean) =
    this(out, DeflaterOutputStream.BUF_SIZE, syncFlush)
  def this(out: OutputStream, size: Int) = this(out, size, false)
  def this(out: OutputStream) = this(out, false)

  writeShort(GZIPInputStream.GZIP_MAGIC)
  out.write(Deflater.DEFLATED)
  out.write(0) // flags
  writeLong(0) // mod time
  out.write(0) // extra flags
  out.write(0) // operating system

  override def flush(): Unit = {
    val count = `def`.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH)
    out.write(buf, 0, count)
    out.flush()
  }

  override def finish(): Unit = {
    super.finish()
    writeLong(crc.getValue())
    writeLong(crc.tbytes)
  }

  override def write(buffer: Array[Byte], off: Int, nbytes: Int): Unit = {
    super.write(buffer, off, nbytes)
    crc.update(buffer, off, nbytes)
  }

  private def writeLong(i: Long): Unit = {
    // Write out the long value as an unsigned int
    val unsigned = i.toInt
    out.write(unsigned & 0xFF)
    out.write((unsigned >> 8) & 0xFF)
    out.write((unsigned >> 16) & 0xFF)
    out.write((unsigned >> 24) & 0xFF)
  }

  private def writeShort(i: Int): Int = {
    out.write(i & 0xFF)
    out.write((i >> 8) & 0xFF)
    i
  }
}
