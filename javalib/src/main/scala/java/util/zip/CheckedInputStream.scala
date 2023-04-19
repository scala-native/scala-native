package java.util.zip

import java.io.{FilterInputStream, InputStream}

// Ported from Apache Harmony

class CheckedInputStream(in: InputStream, cksum: Checksum)
    extends FilterInputStream(in) {

  override def read(): Int = {
    val x = in.read()
    if (x != -1) {
      cksum.update(x)
    }
    x
  }

  override def read(buf: Array[Byte], off: Int, nbytes: Int): Int = {
    val x = in.read(buf, off, nbytes)
    if (x != -1) {
      cksum.update(buf, off, x)
    }
    x
  }

  def getChecksum(): Checksum =
    cksum

  override def skip(nbytes: Long): Long = {
    if (nbytes < 1) {
      0
    } else {
      var skipped: Long = 0L
      val b = new Array[Byte](Math.min(nbytes, 2048L).toInt)
      var x, v = 0
      while (skipped != nbytes) {
        x = in.read(
          b,
          0, {
            v = (nbytes - skipped).toInt; if (v > b.length) b.length else v
          }
        )
        if (x == -1) {
          return skipped
        }
        cksum.update(b, 0, x)
        skipped += x
      }
      skipped
    }
  }

}
