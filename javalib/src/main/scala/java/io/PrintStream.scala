package java.io

import java.nio.charset.{Charset, UnsupportedCharsetException}
import java.util.Formatter
import java.util.Objects

class PrintStream(
    _out: OutputStream,
    autoFlush: Boolean,
    _charset: Charset
) extends FilterOutputStream(_out)
    with Appendable
    with Closeable {

  def this(out: OutputStream) =
    this(out, false, null: Charset)

  def this(out: OutputStream, autoFlush: Boolean) =
    this(out, autoFlush, null: Charset)

  def this(out: OutputStream, autoFlush: Boolean, encoding: String) =
    this(
      out,
      autoFlush,
      try {
        Charset.forName(Objects.requireNonNull(encoding))
      } catch {
        case e: UnsupportedCharsetException =>
          throw new java.io.UnsupportedEncodingException(encoding)
      }
    )

  def this(file: File) =
    this(new BufferedOutputStream(new FileOutputStream(file)))

  def this(file: File, csn: String) =
    this(new BufferedOutputStream(new FileOutputStream(file)), false, csn)

  /** @since JDK 10 */
  def this(file: File, charset: Charset) =
    this(new BufferedOutputStream(new FileOutputStream(file)), false, charset)

  def this(fileName: String) =
    this(new File(fileName))

  def this(fileName: String, csn: String) =
    this(new File(fileName), csn)

  /** @since JDK 10 */
  def this(fileName: String, charset: Charset) =
    this(new File(fileName), charset)

  private lazy val encoder = {
    val c =
      if (_charset == null) Charset.defaultCharset()
      else _charset
    /* We pass `this` as the output stream for the encoding writer so that
     * we can apply auto-flushing. Note that this will flush() more often
     * than required by the spec. It appears to be consistent with how the
     * JDK behaves.
     */
    new OutputStreamWriter(this, c)
  }

  private var closing: Boolean = false
  private var closed: Boolean = false
  private var errorFlag: Boolean = false

  override def flush(): Unit = synchronized {
    ensureOpenAndTrapIOExceptions(out.flush())
  }

  override def close(): Unit = synchronized {
    trapIOExceptions {
      if (!closing) {
        closing = true
        encoder.close()
        flush()
        closed = true
        out.close()
      }
    }
  }

  def checkError(): Boolean = {
    if (closed) {
      /* Just check the error flag.
       * Common sense would tell us to look at the underlying writer's
       * checkError() result too (like we do in the not closed case below).
       * But the JDK does not behave like that. So we don't either.
       */
      errorFlag
    } else {
      flush()
      /* If the underlying writer is also a PrintStream, we also check its
       * checkError() result. This is not clearly specified by the JavaDoc,
       * but, experimentally, the JDK seems to behave that way.
       */
      errorFlag || (out match {
        case out: PrintStream => out.checkError()
        case _                => false
      })
    }
  }

  protected[io] def setError(): Unit = errorFlag = true
  protected[io] def clearError(): Unit = errorFlag = false

  /* Note that calling directly the write() methods will happily bypass the
   * potential lone high surrogate that is buffered in the underlying
   * OutputStreamWriter. This means that the following sequence of operations:
   *
   *   ps.print('\ud83d') // high surrogate of PILE OF POO
   *   ps.write('a')
   *   ps.print('\udca9') // low surrogate of PILE OF POO
   *
   * will result in the following bytes being emitted to the underlying stream:
   *
   *   a\ud83d\udca9
   *
   * i.e., first the 'a', then the PILE OF POO.
   *
   * This is consistent with the behavior of the JDK.
   */

  override def write(b: Int): Unit = synchronized {
    ensureOpenAndTrapIOExceptions {
      out.write(b)
      if (autoFlush && b == '\n')
        flush()
    }

  }

  override def write(buf: Array[Byte], off: Int, len: Int): Unit =
    synchronized {
      ensureOpenAndTrapIOExceptions {
        out.write(buf, off, len)
        if (autoFlush)
          flush()
      }
    }

  def print(b: Boolean): Unit = printString(String.valueOf(b))
  def print(c: Char): Unit = printString(String.valueOf(c))
  def print(i: Int): Unit = printString(String.valueOf(i))
  def print(l: Long): Unit = printString(String.valueOf(l))
  def print(f: Float): Unit = printString(String.valueOf(f))
  def print(d: Double): Unit = printString(String.valueOf(d))
  def print(s: String): Unit = printString(if (s == null) "null" else s)
  def print(obj: AnyRef): Unit = printString(String.valueOf(obj))

  private def printString(s: String): Unit = synchronized {
    ensureOpenAndTrapIOExceptions {
      encoder.write(s)
      encoder.flushBuffer()
    }
  }

  def print(s: Array[Char]): Unit = synchronized {
    ensureOpenAndTrapIOExceptions {
      encoder.write(s)
      encoder.flushBuffer()
    }
  }

  def println(): Unit = synchronized {
    ensureOpenAndTrapIOExceptions {
      encoder.write(System.lineSeparator())
      encoder.flushBuffer()
      if (autoFlush)
        flush()
    }
  }

  def println(b: Boolean): Unit = synchronized { print(b); println() }
  def println(c: Char): Unit = synchronized { print(c); println() }
  def println(i: Int): Unit = synchronized { print(i); println() }
  def println(l: Long): Unit = synchronized { print(l); println() }
  def println(f: Float): Unit = synchronized { print(f); println() }
  def println(d: Double): Unit = synchronized { print(d); println() }
  def println(s: Array[Char]): Unit = synchronized { print(s); println() }
  def println(s: String): Unit = synchronized { print(s); println() }
  def println(obj: AnyRef): Unit = synchronized { print(obj); println() }

  def printf(fmt: String, args: Array[Object]): PrintStream =
    format(fmt, args)

  // Not implemented:
  // def printf(l: java.util.Locale, fmt: String, args: Array[Object]): PrintStream = ???

  def format(fmt: String, args: Array[Object]): PrintStream = synchronized {
    new Formatter(this).format(fmt, args)
    this
  }

  // Not implemented:
  // def format(l: java.util.Locale, fmt: String, args: Array[Object]): PrintStream = ???

  def append(csq: CharSequence): PrintStream = {
    print(if (csq == null) "null" else csq.toString)
    this
  }

  def append(csq: CharSequence, start: Int, end: Int): PrintStream = {
    val csq1 = if (csq == null) "null" else csq
    print(csq1.subSequence(start, end).toString)
    this
  }

  def append(c: Char): PrintStream = {
    print(c)
    this
  }

  @inline private def trapIOExceptions(body: => Unit): Unit = {
    try {
      body
    } catch {
      case _: IOException => setError()
    }
  }

  @inline private def ensureOpenAndTrapIOExceptions(
      body: => Unit
  ): Unit = {
    if (closed) setError()
    else trapIOExceptions(body)
  }

  /** @since JDK 18 */
  def charset(): Charset =
    this._charset

  /** @since JDK 14 */
  def writeBytes(buf: Array[Byte]): Unit =
    this.write(buf, 0, buf.length)

}
