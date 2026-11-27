package java.io

import java.nio.charset.{Charset, CodingErrorAction}
import java.nio.{ByteBuffer, CharBuffer}
import java.util.{Arrays, Objects}

import scala.scalanative.javalib.io.Terminal
import scala.scalanative.meta.LinktimeInfo.isWindows

/** Character-based access to the interactive terminal the process was started
 *  from.
 *
 *  An instance exists only when both the standard input and the standard output
 *  are attached to a terminal, see `System.console()`. It always reads from and
 *  writes to the process' standard file descriptors, independently of
 *  `System.in` and `System.out`.
 *
 *  Input is handled one terminal read at a time: on POSIX, a read that ends
 *  without a line terminator (e.g. Ctrl-D after some input) also ends the line.
 *
 *  Reads are guarded by a read lock and writes by a write lock. Operations
 *  doing both (prompting reads) always take the write lock first.
 */
class Console private[io] (inCs: Charset, outCs: Charset) extends Flushable {
  private val readLock = new Object
  private val writeLock = new Object

  private val pw: PrintWriter = new Console.ConsoleWriter(
    new OutputStreamWriter(new FileOutputStream(FileDescriptor.out), outCs),
    writeLock
  )

  private val in = new FileInputStream(FileDescriptor.in)
  private val decoder = inCs
    .newDecoder()
    .onMalformedInput(CodingErrorAction.REPLACE)
    .onUnmappableCharacter(CodingErrorAction.REPLACE)
  // Bytes not decoded yet, in write mode
  private val bytes = ByteBuffer.allocate(Console.ReadSize)

  // Decoded input shared by reader(), readLine() and readPassword(),
  // valid in buf[pos, lim). Consumed input is zeroed, it may be a secret.
  private var buf = new Array[Char](Console.InitialBufferSize)
  private var pos = 0
  private var lim = 0
  // A line ended with CR at the end of the input read so far: an LF
  // arriving next belongs to that line terminator.
  private var skipLF = false

  // Closing it must not close the terminal input.
  private val rd: Reader = new Reader {
    def read(cbuf: Array[Char], off: Int, len: Int): Int = {
      if (off < 0 || len < 0 || len > cbuf.length - off)
        throw new IndexOutOfBoundsException()
      readLock.synchronized {
        if (len == 0) 0
        else {
          var eof = false
          while (pos == lim && !eof) eof = fill() < 0
          if (eof) -1
          else {
            // Never read past the end of the current line
            val n = Math.min(len, lineEnd(pos) - pos)
            System.arraycopy(buf, pos, cbuf, off, n)
            // An LF still buffered after a returned CR is skipped by readLine
            skipLF = buf(pos + n - 1) == '\r'
            consume(pos + n)
            n
          }
        }
      }
    }
    def close(): Unit = ()
  }

  def writer(): PrintWriter = pw

  def reader(): Reader = rd

  // TODO: JDK 23 overloads taking a java.util.Locale, blocked until javalib
  // provides Locale (Formatter(Appendable, Locale) needs java.text to link):
  // def format(l: Locale, fmt: String, args: Array[Object]): Console = ???
  // def printf(l: Locale, format: String, args: Array[Object]): Console = ???
  // def readLine(l: Locale, fmt: String, args: Array[Object]): String = ???
  // def readPassword(l: Locale, fmt: String, args: Array[Object]): Array[Char] = ???

  def format(fmt: String, args: Array[Object]): Console = {
    Objects.requireNonNull(fmt)
    writeLock.synchronized(pw.format(fmt, args))
    this
  }

  def printf(format: String, args: Array[Object]): Console =
    this.format(format, args)

  def readLine(fmt: String, args: Array[Object]): String = {
    Objects.requireNonNull(fmt)
    writeLock.synchronized {
      readLock.synchronized {
        prompt(fmt, args)
        val line = readLineChars()
        if (line == null) null else new String(line)
      }
    }
  }

  def readLine(): String = readLine("", null)

  def readPassword(fmt: String, args: Array[Object]): Array[Char] = {
    Objects.requireNonNull(fmt)
    writeLock.synchronized {
      readLock.synchronized {
        Terminal.disableEcho()
        try {
          prompt(fmt, args)
          readLineChars()
        } finally {
          Terminal.restoreEcho()
          // The line terminator typed by the user was not echoed
          pw.println()
        }
      }
    }
  }

  def readPassword(): Array[Char] = readPassword("", null)

  def flush(): Unit = pw.flush()

  def charset(): Charset = outCs

  /** Always true: a Console exists only when attached to a terminal. */
  def isTerminal(): Boolean = true

  // Requires writeLock. Flushes pending output even without a prompt.
  private def prompt(fmt: String, args: Array[Object]): Unit =
    if (fmt.isEmpty()) pw.flush()
    else pw.format(fmt, args)

  /** Reads one line without its terminator, or returns null at end of input if
   *  nothing was read. Requires readLock.
   */
  private def readLineChars(): Array[Char] =
    try {
      while (true) {
        if (skipLF && pos < lim) {
          if (buf(pos) == '\n') consume(pos + 1)
          skipLF = false
        }
        if (pos < lim) {
          val end = lineEnd(pos)
          val terminated = buf(end - 1) == '\n' || buf(end - 1) == '\r'
          // A Windows console read can return part of a line, so only a line
          // terminator (or end of input) ends it there.
          val recordEndsLine = if (isWindows) false else true
          if (terminated || recordEndsLine) return takeLine(end)
        }
        if (fill() < 0) return if (pos < lim) takeLine(lim) else null
      }
      null
    } catch {
      case e: IOException => throw new IOError(e)
    }

  /** End of the line starting at `from`, including its terminator (LF, CR or
   *  CRLF), or `lim` if the buffered input has none. Requires readLock.
   */
  private def lineEnd(from: Int): Int = {
    var i = from
    while (i < lim && buf(i) != '\n' && buf(i) != '\r') i += 1
    if (i == lim) lim
    else if (buf(i) == '\r' && i + 1 < lim && buf(i + 1) == '\n') i + 2
    else i + 1
  }

  // Takes the line in buf[pos, end), see lineEnd. Requires readLock.
  private def takeLine(end: Int): Array[Char] = {
    var stop = end
    if (stop > pos && buf(stop - 1) == '\n') stop -= 1
    if (stop > pos && buf(stop - 1) == '\r') {
      stop -= 1
      skipLF = end == lim && buf(end - 1) == '\r'
    }
    val line = Arrays.copyOfRange(buf, pos, stop)
    consume(end)
    line
  }

  // Zeroes and drops buffered chars up to `next`. Requires readLock.
  private def consume(next: Int): Unit = {
    Arrays.fill(buf, pos, next, '\u0000')
    pos = next
  }

  /** Performs one read of the terminal and decodes it into `buf`, compacting or
   *  growing `buf` first if needed. Returns the number of chars added (possibly
   *  0, for an incomplete character), or -1 at end of input. Requires readLock.
   */
  private def fill(): Int = {
    if (pos > 0) {
      val remaining = lim - pos
      System.arraycopy(buf, pos, buf, 0, remaining)
      Arrays.fill(buf, remaining, lim, '\u0000')
      pos = 0
      lim = remaining
    }
    val needed =
      Math.ceil(bytes.capacity() * decoder.maxCharsPerByte()).toInt + 1
    if (buf.length - lim < needed) {
      val old = buf
      buf = Arrays.copyOf(old, Math.max(old.length * 2, lim + needed))
      Arrays.fill(old, '\u0000')
    }

    val n = in.read(bytes.array(), bytes.position(), bytes.remaining())
    val out = CharBuffer.wrap(buf, lim, buf.length - lim)
    if (n > 0) bytes.position(bytes.position() + n)
    bytes.flip()
    if (n < 0) {
      // Decode any incomplete character left, then allow reading further
      decoder.decode(bytes, out, true)
      decoder.flush(out)
      decoder.reset()
      bytes.clear()
    } else {
      decoder.decode(bytes, out, false)
      bytes.compact()
    }
    Arrays.fill(bytes.array(), bytes.position(), bytes.capacity(), 0.toByte)

    val added = out.position() - lim
    lim = out.position()
    if (n < 0 && added == 0) -1 else added
  }
}

object Console {
  private final val InitialBufferSize = 256
  private final val ReadSize = 8192

  /** The console of this process, or null when standard input or standard
   *  output is not a terminal.
   */
  private[java] lazy val system: Console =
    if (Terminal.stdinAndStdoutAreTerminals())
      new Console(
        charsetOf("stdin.encoding"),
        charsetOf("stdout.encoding")
      )
    else null

  private def charsetOf(property: String): Charset = {
    val name = System.getProperty(property)
    if (name == null) Charset.defaultCharset()
    else Charset.forName(name, Charset.defaultCharset())
  }

  // Serializes all output on the console's write lock.
  private final class LockedWriter(out: Writer, writeLock: Object)
      extends Writer(writeLock) {
    def write(cbuf: Array[Char], off: Int, len: Int): Unit =
      writeLock.synchronized(out.write(cbuf, off, len))
    override def write(c: Int): Unit =
      writeLock.synchronized(out.write(c))
    override def write(s: String, off: Int, len: Int): Unit =
      writeLock.synchronized(out.write(s, off, len))
    def flush(): Unit = writeLock.synchronized(out.flush())
    def close(): Unit = flush()
  }

  // Closing it must not close the terminal output.
  private final class ConsoleWriter(out: Writer, writeLock: Object)
      extends PrintWriter(new LockedWriter(out, writeLock), true) {
    override def close(): Unit = flush()
  }
}
