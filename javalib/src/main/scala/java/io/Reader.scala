// Ported from Scala.js, commit: 7d7a621, dated 2022-03-07
// See SN Repository git history for Scala Native additions.

package java.io

import java.nio.CharBuffer
import java.util.Objects
import java.{nio => jnio, util => ju}

import scala.annotation.tailrec

abstract class Reader() extends Readable with Closeable {
  protected var lock: Object = this

  protected def this(lock: Object) = {
    this()
    if (lock eq null)
      throw new NullPointerException()
    this.lock = lock
  }

  def read(target: CharBuffer): Int = {
    Objects.requireNonNull(target, "target") // JVM uses helpful null msg

    if (!target.hasRemaining()) 0
    else if (target.hasArray()) {
      val charsRead = read(
        target.array(),
        target.position() + target.arrayOffset(),
        target.remaining()
      )
      if (charsRead != -1)
        target.position(target.position() + charsRead)
      charsRead
    } else {
      val buf = new Array[Char](target.remaining())
      val charsRead = read(buf)
      if (charsRead != -1)
        target.put(buf, 0, charsRead)
      charsRead
    }
  }

  def read(): Int = {
    val buf = new Array[Char](1)
    if (read(buf) == -1) -1
    else buf(0).toInt
  }

  def read(cbuf: Array[Char]): Int =
    read(cbuf, 0, cbuf.length)

  def read(cbuf: Array[Char], off: Int, len: Int): Int

  /** @since JDK 25 */
  def readAllAsString(): String = {
    /* Maintainer:
     *   if cbuf.length get increased, change ReaderTestOnJDK25 so
     *   the while loop below is exercised more than once. See comments there.
     *   That Test exercises buffer re-fill logic.
     */
    val cbuf = new Array[Char](256)

    // 256 is a generous guess to avoid initial ramp-up allocations.
    val sBldr = new StringBuilder(256)

    var done = false

    while (!done) {
      val nRead = read(cbuf, 0, cbuf.length)

      /* else clause works around suspected bug in sBldr.append(cbuf, 0, nRead)
       * where the cbuf gets appended as an Object, and not its constituent
       * characters.
       */

      if (nRead < 0) done = true
      else sBldr.append(String.valueOf(cbuf, 0, nRead))
    }

    sBldr.toString()
  }

  /** @since JDK 25 */
  def readAllLines(): ju.List[String] = {
    /* Someday it would be nice to reduce duplication between this code
     * and earlier, similar code in BufferedReader.scala.
     */

    /* Maintainer:
     *   if cbuf.length get increased, change ReaderTestOnJDK25 so
     *   the while loop below is exercised more than once. See comments there.
     *   That Test exercises buffer re-fill logic.
     */
    val cbuf = new Array[Char](256)

    // 256 is a generous guess to avoid initial ramp-up allocations.
    val sBldr = new StringBuilder(256)

    val al = new ju.ArrayList[String](256) // arbitrary generous estimate

    var crSeenAt = -1 // -1 indicates 'not encountered', else in [0, nRead)

    var done = false

    while (!done) {
      val nRead = read(cbuf, 0, cbuf.length)

      if (nRead <= 0) {
        done = true

        if (sBldr.length() != 0)
          al.addLast(sBldr.toString())
      } else {
        var start = 0

        for (j <- 0 until nRead) {
          if ((cbuf(j) == '\n') || (cbuf(j) == '\r')) {

            /* The JVM readAllLines() specification of line terminators
             * taken with the need to read a finite number of characters
             * is a recipe for bugs, especially off-by-one bugs.
             * Sigh, wimper, moan.
             *
             * The following 'if()' clause is hard to read and comprehend
             * on-the-fly.
             *
             * The idea and intent is skip '\n' _immediately_ after '\r'
             * but only then.
             *
             * An English translation so somewhat like the following:
             *   if a '\r' has been recently encountered
             *     if looking at a '\n'
             *   and either, in order
             *     if
             *       at the beginning of the characters just read,
             *       meaning the '\r' was the final character of the
             *       previous read.
             *     or
             *       now j is known to be >= 1, so can safely look back one
             *       to see if the '\r' was immediately preceeding current '\n'
             *
             *  Extracting into a method would probably be just as messy.
             *
             *  Any section of code which has 13 times more comment lines
             *  explaining it is probably going to be visited again.
             */
            if ((crSeenAt >= 0) && (cbuf(j) == '\n') &&
                ((j == 0) || (crSeenAt == j - 1))) {
              crSeenAt = -1 // unseen
              start = j + 1 // skip current '\n'
            } else {
              if (cbuf(j) == '\r')
                crSeenAt = j

              // count (j - start) skips <LF> & solo <CR>terminators by intent
              sBldr.append(String.valueOf(cbuf, start, j - start))

              al.addLast(sBldr.toString())
              sBldr.setLength(0)

              // OK if + 1 is past nRead, next buffered read will reset to 0.
              start = j + 1
            }
          } else if (j == (nRead - 1)) { // partial line, cache it.
            sBldr.append(String.valueOf(cbuf, start, j - start + 1))
          }
        }
      }
    }

    al.trimToSize()
    al.asInstanceOf[ju.List[String]]
  }

  def skip(n: Long): Long = {
    if (n < 0)
      throw new IllegalArgumentException("Cannot skip negative amount")

    val buffer = new Array[Char](8192)
    @tailrec
    def loop(m: Long, lastSkipped: Long): Long = {
      if (m <= 0) {
        lastSkipped
      } else {
        val mMin = Math.min(m, 8192).toInt
        val skipped = read(buffer, 0, mMin)
        if (skipped < 0) {
          lastSkipped
        } else {
          val totalSkipped = lastSkipped + skipped
          loop(m - mMin, totalSkipped)
        }
      }
    }
    loop(n, 0)
  }

  def ready(): Boolean = false

  def markSupported(): Boolean = false

  def mark(readAheadLimit: Int): Unit =
    throw new IOException("Mark not supported")

  def reset(): Unit =
    throw new IOException("Reset not supported")

  def close(): Unit

  // Since: Java 10
  def transferTo(out: Writer): Long = {
    val buffer = new Array[Char](4096)

    @tailrec
    def loop(nRead: Long): Long = {
      val n = this.read(buffer)
      if (n == -1) {
        nRead
      } else {
        out.write(buffer, 0, n)
        loop(nRead + n)
      }
    }

    loop(0)
  }
}

object Reader {

  /** @since JDK 11 */
  def nullReader(): Reader = {
    new Reader() {
      private var closed = false

      private def ensureOpen(): Unit = {
        if (closed)
          throw new IOException("Stream closed")
      }

      def close(): Unit =
        closed = true

      def read(cbuf: Array[Char], off: Int, len: Int): Int = {
        Objects.requireNonNull(
          cbuf,
          "Cannot read the array length because \"cbuf\" is null"
        )
        Objects.checkFromIndexSize(off, len, cbuf.length)

        ensureOpen()

        if (len == 0) 0 else -1 // zero is JVM corner case.
      }

      override def ready(): Boolean = {
        ensureOpen()
        super.ready()
      }

      override def skip(n: Long): Long = {
        ensureOpen()
        super.skip(n)
      }

      override def transferTo(out: Writer): Long = {
        ensureOpen()
        super.transferTo(out)
      }
    }
  }
}
