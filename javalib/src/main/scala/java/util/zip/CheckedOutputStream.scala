package java.util.zip

// Ported from Apache Harmony

import java.io.{FilterOutputStream, OutputStream}

class CheckedOutputStream(out: OutputStream, cksum: Checksum)
    extends FilterOutputStream(out) {

  def getChecksum(): Checksum =
    cksum

  override def write(`val`: Int): Unit = {
    out.write(`val`)
    cksum.update(`val`)
  }

  override def write(buf: Array[Byte], off: Int, nbytes: Int): Unit = {
    out.write(buf, off, nbytes)
    cksum.update(buf, off, nbytes)
  }

}
