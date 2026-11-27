package java.io

import java.nio.charset.Charset
import java.util.{Arrays, Objects}

import scala.scalanative.javalib.io.Terminal

/** Character-based access to the interactive terminal the process was started
 *  from.
 *
 *  An instance exists only when both the standard input and the standard output
 *  are attached to a terminal, see `System.console()`. It always reads from and
 *  writes to the process' standard file descriptors, independently of
 *  `System.in` and `System.out`.
 *
 *  Reads are guarded by a read lock and writes by a write lock. Operations
 *  doing both (prompting reads) always take the write lock first.
 */
class Console private[io] (cs: Charset) extends Flushable {
  private val readLock = new Object
  private val writeLock = new Object

  private val pw: PrintWriter = new Console.ConsoleWriter(
    new OutputStreamWriter(new FileOutputStream(FileDescriptor.out), cs)
  )
  private val decoder: Reader =
    new InputStreamReader(new FileInputStream(FileDescriptor.in), cs)

  // Decoded input shared by reader(), readLine() and readPassword(),
  // valid in buf[pos, lim). Consumed chars are zeroed, they may be secrets.
  private var buf = new Array[Char](Console.InitialBufferSize)
  private var pos = 0
  private var lim = 0

  // Closing it must not close the terminal input.
  private val rd: Reader = new Reader {
    def read(cbuf: Array[Char], off: Int, len: Int): Int = {
      if (off < 0 || len < 0 || len > cbuf.length - off)
        throw new IndexOutOfBoundsException()
      readLock.synchronized {
        if (len == 0) 0
        else if (pos == lim && !fill()) -1
        else {
          val n = Math.min(len, lim - pos)
          System.arraycopy(buf, pos, cbuf, off, n)
          consume(pos + n)
          n
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

  def charset(): Charset = cs

  /** Always true: a Console exists only when attached to a terminal. */
  def isTerminal(): Boolean = true

  // Requires writeLock. Flushes pending output even without a prompt.
  private def prompt(fmt: String, args: Array[Object]): Unit =
    if (fmt.isEmpty()) pw.flush()
    else pw.format(fmt, args)

  /** Reads more input into `buf`, compacting or growing it first if needed.
   *  Returns false at end of input. Requires readLock.
   */
  private def fill(): Boolean = {
    if (pos > 0) {
      val remaining = lim - pos
      System.arraycopy(buf, pos, buf, 0, remaining)
      Arrays.fill(buf, remaining, lim, '\u0000')
      pos = 0
      lim = remaining
    }
    if (lim == buf.length) {
      val old = buf
      buf = Arrays.copyOf(old, old.length * 2)
      Arrays.fill(old, '\u0000')
    }
    val n =
      try decoder.read(buf, lim, buf.length - lim)
      catch { case e: IOException => throw new IOError(e) }
    if (n > 0) lim += n
    n >= 0
  }

  /** Reads one line without its terminator, or returns null at end of input if
   *  nothing was read. Requires readLock.
   */
  private def readLineChars(): Array[Char] = {
    // Scan offset relative to pos, stays valid when fill() compacts buf
    var scanned = 0
    while (true) {
      while (pos + scanned < lim && buf(pos + scanned) != '\n') scanned += 1
      if (pos + scanned < lim)
        return takeLine(pos + scanned, pos + scanned + 1)
      if (!fill()) // End of input: return a partial line, if any
        return if (lim > pos) takeLine(lim, lim) else null
    }
    null
  }

  // Requires readLock
  private def takeLine(end: Int, next: Int): Array[Char] = {
    val stop =
      if (end > pos && buf(end - 1) == '\r') end - 1
      else end
    val line = Arrays.copyOfRange(buf, pos, stop)
    consume(next)
    line
  }

  // Zeroes and drops buffered chars up to `next`. Requires readLock.
  private def consume(next: Int): Unit = {
    Arrays.fill(buf, pos, next, '\u0000')
    pos = next
  }
}

object Console {
  private final val InitialBufferSize = 256

  /** The console of this process, or null when standard input or standard
   *  output is not a terminal.
   */
  private[java] lazy val system: Console =
    if (Terminal.stdinAndStdoutAreTerminals()) new Console(terminalCharset())
    else null

  private def terminalCharset(): Charset = {
    val name = System.getProperty("stdout.encoding")
    if (name == null) Charset.defaultCharset()
    else Charset.forName(name, Charset.defaultCharset())
  }

  // Closing it must not close the terminal output.
  private final class ConsoleWriter(out: Writer)
      extends PrintWriter(out, true) {
    override def close(): Unit = flush()
  }
}
