package java.io

// Ported from Scala.js, commit: 7d7a621, dated 2022-03-07
import java.nio._

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

  def write(chars: Array[Char]): Unit =
    write(chars, 0, chars.length)

  def write(chars: Array[Char], off: Int, len: Int): Unit

  def write(str: String): Unit =
    write(str.toCharArray)

  def write(str: String, off: Int, len: Int): Unit =
    write(str.toCharArray, off, len)

  protected[io] def writeCharBuffer(cbuf: CharBuffer): Unit = {
    val chars =
      if (cbuf.hasArray()) cbuf.array()
      else {
        val chars0 = new Array[Char](cbuf.length())
        cbuf.get(chars0)
        chars0
      }

    write(chars)
  }

  def append(c: Char): Writer = {
    write(c.toInt)
    this
  }

  def append(csq: CharSequence): Writer = {
    if (csq == null) write("null")
    else this.append(csq, 0, csq.length())
    this
  }

  def append(csq: CharSequence, start: Int, end: Int): Writer = {
    if (csq == null) write("null")
    else {
      writeCharBuffer(CharBuffer.wrap(csq, start, end))
    }

    this
  }

  def flush(): Unit

  def close(): Unit

}
