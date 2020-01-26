package java.util.zip

// Ported from Apache Harmony

import java.io.{FilterOutputStream, IOException, OutputStream}

class DeflaterOutputStream(os: OutputStream,
                           protected var `def`: Deflater,
                           size: Int,
                           syncFlush: Boolean)
    extends FilterOutputStream(os) {

  if (os == null || `def` == null) {
    throw new NullPointerException()
  }
  if (size <= 0) {
    throw new IllegalArgumentException()
  }

  protected var buf: Array[Byte] = new Array[Byte](size)
  private[zip] var done: Boolean = false

  def this(out: OutputStream, `def`: Deflater, size: Int) =
    this(out, `def`, size, false)
  def this(out: OutputStream, `def`: Deflater, syncFlush: Boolean) =
    this(out, `def`, DeflaterOutputStream.BUF_SIZE, syncFlush)
  def this(out: OutputStream, `def`: Deflater) = this(out, `def`, false)
  def this(out: OutputStream, syncFlush: Boolean) =
    this(out, new Deflater(), DeflaterOutputStream.BUF_SIZE, syncFlush)
  def this(out: OutputStream) = this(out, false)

  protected def deflate(): Unit = {
    var x = 0
    do {
      x = `def`.deflate(buf)
      out.write(buf, 0, x)
    } while (!`def`.needsInput())
  }

  override def close(): Unit = {
    if (!`def`.finished()) {
      finish()
    }
    `def`.end()
    out.close()
  }

  def finish(): Unit = {
    if (!done) {
      `def`.finish()
      var x = 0
      while (!`def`.finished()) {
        if (`def`.needsInput()) {
          `def`.setInput(buf, 0, 0)
        }
        x = `def`.deflate(buf)
        out.write(buf, 0, x)
      }
      done = true
    }
  }

  override def write(i: Int): Unit = {
    val b = new Array[Byte](1)
    b(0) = i.toByte
    write(b, 0, 1)
  }

  override def write(buffer: Array[Byte], off: Int, nbytes: Int): Unit = {
    if (done) {
      throw new IOException("attempt to write after finish")
    } else {
      if (off <= buffer.length && nbytes >= 0 && off >= 0 && buffer.length - off >= nbytes) {
        if (!`def`.needsInput()) {
          throw new IOException()
        } else {
          `def`.setInput(buffer, off, nbytes)
          deflate()
        }
      } else {
        throw new ArrayIndexOutOfBoundsException()
      }
    }
  }

  override def flush(): Unit = {
    if (syncFlush && !`def`.finished()) {
      var written = 0
      while ({
        written = `def`.deflate(buf, 0, size, Deflater.SYNC_FLUSH);
        written != 0
      }) {
        out.write(buf, 0, written)
      }
    }
    os.flush()
  }
}

object DeflaterOutputStream {
  private[zip] final val BUF_SIZE: Int = 512
}
