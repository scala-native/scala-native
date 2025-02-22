// Ported from Scala.js, commit: 7d7a621, dated 2022-03-07
// See SN Repository git history for Scala Native additions.

package java.io

import java.util.Objects

abstract class Writer() extends Appendable with Closeable with Flushable {
  protected var lock: Object = this

  protected def this(lock: Object) = {
    this()
    if (lock eq null)
      throw new NullPointerException()
    this.lock = lock
  }

  def write(c: Int): Unit =
    write(Array(c.toChar))

  def write(cbuf: Array[Char]): Unit =
    write(cbuf, 0, cbuf.length)

  def write(cbuf: Array[Char], off: Int, len: Int): Unit

  def write(str: String): Unit =
    write(str.toCharArray)

  def write(str: String, off: Int, len: Int): Unit =
    write(str.toCharArray, off, len)

  def append(csq: CharSequence): Writer = {
    write(if (csq == null) "null" else csq.toString)
    this
  }

  def append(csq: CharSequence, start: Int, end: Int): Writer = {
    val csq1 = if (csq == null) "null" else csq
    write(csq1.subSequence(start, end).toString)
    this
  }

  def append(c: Char): Writer = {
    write(c.toInt)
    this
  }

  def flush(): Unit

  def close(): Unit

}

object Writer {

  /** @since JDK 11 */
  def nullWriter(): Writer = {
    new Writer() {
      private var closed = false

      private def ensureOpen(): Unit = {
        if (closed)
          throw new IOException("Stream closed")
      }

      def close(): Unit =
        closed = true

      def flush(): Unit =
        ensureOpen()

      def write(cbuf: Array[Char], off: Int, len: Int): Unit =
        Objects.requireNonNull(
          cbuf,
          "Cannot read the array length because \"cbuf\" is null"
        )
        Objects.checkFromIndexSize(off, len, cbuf.length)

        ensureOpen()
    }
  }
}
