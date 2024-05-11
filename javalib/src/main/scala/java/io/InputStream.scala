package java.io

import java.{util => ju}

abstract class InputStream extends Closeable {
  def read(): Int

  def read(b: Array[Byte]): Int = read(b, 0, b.length)

  def read(b: Array[Byte], off: Int, len: Int): Int = {
    if (off < 0 || len < 0 || len > b.length - off)
      throw new IndexOutOfBoundsException

    if (len == 0) 0
    else {
      var bytesWritten = 0
      var next = 0

      while (bytesWritten < len && next != -1) {
        next =
          if (bytesWritten == 0) read()
          else {
            try read()
            catch { case _: IOException => -1 }
          }
        if (next != -1) {
          b(off + bytesWritten) = next.toByte
          bytesWritten += 1
        }
      }

      if (bytesWritten <= 0) -1
      else bytesWritten
    }
  }

  // Allow obvious implementation of readAllBytes() to work on both Java 9 & 11
  def readNBytesImpl(len: Int): Array[Byte] = {
    if (len < 0)
      throw new IllegalArgumentException("len < 0")

    def readBytes(len: Int): ByteArrayOutputStream = {
      val limit = Math.min(len, 1024)

      val storage = new ByteArrayOutputStream(limit) // can grow itself
      val buffer = new Array[Byte](limit)

      var remaining = len

      while (remaining > 0) {
        val nRead = read(buffer, 0, limit)

        if (nRead == -1) remaining = 0 // EOF
        else {
          storage.write(buffer, 0, nRead)
          remaining -= nRead
        }
      }

      storage
    }

    /* To stay within the documented 2 * len memory bound for this method,
     * ensure that the temporary intermediate read buffer is out of scope
     * and released before calling toByteArray().
     */

    readBytes(len).toByteArray()
  }

  /** Java 9
   */
  def readAllBytes(): Array[Byte] = readNBytesImpl(Integer.MAX_VALUE)

  /** Java 9
   */
  def readNBytes(buffer: Array[Byte], off: Int, len: Int): Int = {
    ju.Objects.requireNonNull(buffer)

    if ((off < 0) || (len < 0) || (len > buffer.length - off)) {
      val range = s"Range [${off}, ${off} + ${len})"
      throw new IndexOutOfBoundsException(
        s"${range} out of bounds for length ${buffer.length}"
      )
    }

    if (len == 0) 0
    else {
      var totalBytesRead = 0
      var remaining = len
      var offset = off

      while (remaining > 0) {
        val nRead = read(buffer, offset, remaining)

        if (nRead == -1) remaining = 0 // EOF
        else {
          totalBytesRead += nRead
          remaining -= nRead
          offset += nRead
        }
      }

      totalBytesRead
    }
  }

  /** Java 11
   */
  def readNBytes(len: Int): Array[Byte] = readNBytesImpl(len)

  def skip(n: Long): Long = {
    var skipped = 0
    while (skipped < n && read() != -1) skipped += 1
    skipped
  }

  def available(): Int = 0

  def close(): Unit = ()

  def mark(readlimit: Int): Unit = ()

  def reset(): Unit =
    throw new IOException("Reset not supported")

  def markSupported(): Boolean = false

  /** Java 9
   */
  def transferTo(out: OutputStream): Long = {
    val limit = 4096 // sector & page sizes on most architectures circa 2024
    val buffer = new Array[Byte](limit)

    var nTransferred = 0L
    var done = false

    while (!done) {
      val nRead = readNBytes(buffer, 0, limit)
      if (nRead == 0) done = true // EOF
      else {
        out.write(buffer, 0, nRead)
        nTransferred += nRead
      }
    }

    nTransferred
  }
}
