package java.io

import java.{util => ju}
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.Arrays

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

  /** Java 9
   */
  def readAllBytes(): Array[Byte] = {
    /* Design Note:
     *   readAllBytes() was introduced in Java 9 without any implementation
     *   requirements. Java 11 added such a requirement:
     *
     *    Implementation Requirements:
     *    This method invokes readNBytes(int) with a length of
     *    Integer.MAX_VALUE.
     *
     *   The current JDK, 23, retains this requirement.
     *
     *   This requirement effects the way readNBytes(int) is implemented
     *   because it implies buffered or "chunked" intermediate reads.
     */

    readNBytes(Integer.MAX_VALUE)
  }

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

  /* Design Note:
   * The 'streamChunkSize' "constant" must manually be kept in synch with
   * the corresponding value in InputStreamTestOnJDK11.scala.
   *
   * The 4096 value is a guess at a sweet spot between memory used and
   * number of I/Os when N is large. It is the page size on many systems.
   * Experience and the passage of time may show that this number should be
   * increased.
   */

  private final val streamChunkSize = 4096 // remember InputStreamTestOnJDK11

  /** Java 11
   */
  def readNBytes(len: Int): Array[Byte] = {
    /* Design Note:
     *   See Design Note in method readAllBytes(). The constraint described
     *   there leads directly to the possibility that 'len' might be
     *   large (Integer.MAX_VALUE). This means that always blindly allocating
     *   an Array[Byte](len) is not robust.
     */

    if (len < 0)
      throw new IllegalArgumentException("len < 0")

    def readSmallN(len: Int): Array[Byte] = {
      /* Attempt to minimize the number of times the data is copied.
       *
       * When the caller has guessed correctly and len bytes are available,
       * only one copy is needed.  When less than len bytes
       * are available, a second is necessary.
       *
       * readLargeN() is likely to call readSmallN() with an exact match
       * len argument one or more times for each call which triggers
       * the second copy.
       */

      // caller has dispatched on argument, so OK to allocate size blindly.
      val buffer = new Array[Byte](len)

      var totalBytesRead = 0
      var remaining = len

      while (remaining > 0) {
        val nRead = read(buffer, totalBytesRead, remaining)

        if (nRead == -1) remaining = 0 // EOF
        else {
          remaining -= nRead
          totalBytesRead += nRead
        }
      }

      if (totalBytesRead == len)
        buffer
      else if (totalBytesRead < len)
        Arrays.copyOfRange(buffer, 0, totalBytesRead)
      else { // should never happen
        throw new IOException(
          s"total bytes read ${totalBytesRead} > len argument ${len}"
        )
      }
    }

    def readLargeN(len: Int): Array[Byte] = {
      /* The byteStore is not expected to be accessed concurrently.
       * ConcurrentedLinkedDeque is used here because the Scala Native JSR-166
       * code is newer, more studied, and likely to execute faster
       * than the SN LinkedListDequeue implementation. FUD, not measurement.
       *
       * Using a Deque rather than, say, a ByteArrayOutputStream may briefly
       * exceed the JDK documented upper bound of (2 * len) for memory
       * usage. Given that we are in large N territory here, it is highly
       * likely to reduce the number of data copies.
       */
      val byteStore = new ConcurrentLinkedDeque[Array[Byte]]

      var totalBytesRead = 0
      var remaining = len

      while (remaining > 0) {
        val bufferSize = Math.min(remaining, streamChunkSize)
        val buffer = readSmallN(bufferSize)

        val nRead = buffer.size

        if (nRead == 0) remaining = 0 /* EOF */
        else {
          remaining -= nRead
          totalBytesRead += nRead
          byteStore.addLast(buffer)
        }
      }

      val result = new Array[Byte](totalBytesRead)

      var resultPos = 0
      byteStore.forEach(b => {
        val n = b.size
        System.arraycopy(b, 0, result, resultPos, n)
        resultPos += n
      })

      result
    }

    if (len <= streamChunkSize) readSmallN(len)
    else readLargeN(len)
  }

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
