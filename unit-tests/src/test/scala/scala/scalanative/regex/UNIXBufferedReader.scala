package scala.scalanative
package regex

import java.io.Reader

class UNIXBufferedReader(r: Reader) extends Reader {
//  private var r: Reader = null
  private val buf    = new Array[Char](4096)
  private var buflen = 0 // length prefix of |buf| that is filled

  private var inext = 0 // index in buf of next char

  def readLine(): String = {
    var s: StringBuffer = null
    // holds '\n'-free gulps of input
    var istart = 0 // index of first char

    while (true) { // Should we refill the buffer?
      if (inext >= buflen) {
        var n = 0
        do n = r.read(buf, 0, buf.length) while (n == 0)
        if (n > 0) {
          buflen = n
          inext = 0
        }
      }
      // Did we reach end-of-file?
      if (inext >= buflen)
        return if (s != null && s.length > 0) s.toString
        else null
      // Did we read a newline?
      var i = 0
      i = inext
      while (i < buflen) {
        if (buf(i) == '\n') {
          istart = inext
          inext = i
          var str: String = null
          if (s == null) str = new String(buf, istart, i - istart)
          else {
            s.append(buf, istart, i - istart)
            str = s.toString
          }
          inext += 1
          return str
        }

        {
          i += 1; i - 1
        }
      }
      istart = inext
      inext = i
      if (s == null) s = new StringBuffer(80)
      s.append(buf, istart, i - istart)
    }
    s.toString
  }

  override def close(): Unit = {
    r.close()
  }

  // Unimplemented:

  override def read(buf: Array[Char], off: Int, len: Int) =
    throw new UnsupportedOperationException

  override def read = throw new UnsupportedOperationException

  override def skip(n: Long) = throw new UnsupportedOperationException

  override def ready = throw new UnsupportedOperationException

  override def markSupported = throw new UnsupportedOperationException

  override def mark(readAheadLimit: Int): Unit = {
    throw new UnsupportedOperationException
  }

  override def reset(): Unit = {
    throw new UnsupportedOperationException
  }

}
