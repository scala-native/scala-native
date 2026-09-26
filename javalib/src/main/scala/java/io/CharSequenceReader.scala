package java.io

import java.{lang => jl}

/* Adapted from StringReader.scala.
 * See method   override def read(cbuf: Array[Char], off: Int, len: Int): Int
 * for major change.
 */

private[io] class CharSequenceReader(cs: jl.CharSequence) extends Reader {
  private var closed = false
  private var pos = 0
  private var mark = 0

  override def close(): Unit =
    closed = true

  override def mark(readAheadLimit: Int): Unit = {
    ensureOpen()

    mark = pos
  }

  override def markSupported(): Boolean = true

  override def read(): Int = {
    ensureOpen()

    if (pos < cs.length()) {
      val res = cs.charAt(pos).toInt
      pos += 1
      res
    } else -1
  }

  /* Once SN PRs #5006 and #5007 have merged, this key method
   * should be re-implemented in terms of the potentially faster
   * and more efficient getChars(). Each subclass may have provided
   * an implementation aware of and using its internals rather than
   * repeated calls to 'charAt()'. Until then 'charAt()' provides the
   * method and is better than waiting for devo time which may never come.
   */
  override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
    ensureOpen()

    if (off < 0 || len < 0 || len > cbuf.length - off)
      throw new IndexOutOfBoundsException

    if (len == 0) 0
    else {
      val count = Math.min(len, cs.length() - pos)
      var i = 0
      while (i < count) {
        cbuf(off + i) = cs.charAt(pos + i)
        i += 1
      }
      pos += count
      if (count == 0) -1 else count
    }
  }

  override def ready(): Boolean = {
    ensureOpen()
    true
  }

  override def reset(): Unit = {
    ensureOpen()
    pos = mark
  }

  override def skip(ns: Long): Long = {
    // Follow StringReader.skip JVM practice of allowing negative skips
    val count = Math.max(Math.min(ns, cs.length() - pos).toInt, -pos)
    pos += count
    count.toLong
  }

  private def ensureOpen(): Unit = {
    if (closed)
      throw new IOException("Operation on closed stream")
  }
}
