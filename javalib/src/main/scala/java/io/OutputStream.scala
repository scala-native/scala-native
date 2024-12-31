package java.io

abstract class OutputStream extends Object with Closeable with Flushable {

  def write(b: Int): Unit

  def write(b: Array[Byte]): Unit =
    write(b, 0, b.length)

  def write(b: Array[Byte], off: Int, len: Int): Unit = {
    if (off > b.length || off < 0 || len < 0 || len > b.length - off)
      throw new IndexOutOfBoundsException()

    var n = off
    val stop = off + len
    while (n < stop) {
      write(b(n))
      n += 1
    }
  }

  def flush(): Unit = ()

  def close(): Unit = ()
}

/** Java 11
 */
object OutputStream {

  /** Java 11
   */
  def nullOutputStream(): OutputStream = {
    new OutputStream() {
      private var closed = false

      private def nullWrite(): Unit = {
        if (closed)
          throw new IOException("Stream closed")
        // else silently do nothing
      }

      override def close(): Unit =
        closed = true

      override def write(b: Array[Byte]): Unit =
        nullWrite()

      override def write(b: Array[Byte], off: Int, len: Int): Unit =
        nullWrite()

      def write(b: Int): Unit =
        nullWrite()
    }
  }
}
