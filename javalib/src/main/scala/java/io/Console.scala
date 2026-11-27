package java.io

import java.nio.charset.Charset
import java.util.{Formatter, Objects}

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.termios._
import scala.scalanative.posix.termiosOps._
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.ConsoleApi._
import scala.scalanative.windows.ConsoleApiExt._
import scala.scalanative.windows.DWord

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

  private val rd: Reader = new Console.ConsoleReader(this)

  def writer(): PrintWriter = pw

  def reader(): Reader = rd

  // TODO: JDK 23 overloads taking a java.util.Locale, blocked until javalib
  // provides Locale (Formatter(Appendable, Locale) needs java.text to link):
  // def format(l: Locale, fmt: String, args: Array[Object]): Console = ???
  // def printf(l: Locale, format: String, args: Array[Object]): Console = ???
  // def readLine(l: Locale, fmt: String, args: Array[Object]): String = ???
  // def readPassword(l: Locale, fmt: String, args: Array[Object]): Array[Char] = ???

  def format(fmt: String, args: Array[Object]): Console =
    doFormat(new Formatter(pw), fmt, args)

  def printf(format: String, args: Array[Object]): Console =
    this.format(format, args)

  def readLine(fmt: String, args: Array[Object]): String =
    doReadLine(new Formatter(pw), fmt, args)

  def readLine(): String = doReadLine(null, "", null)

  def readPassword(fmt: String, args: Array[Object]): Array[Char] =
    doReadPassword(new Formatter(pw), fmt, args)

  def readPassword(): Array[Char] = doReadPassword(null, "", null)

  def flush(): Unit = pw.flush()

  def charset(): Charset = cs

  /** Always true: a Console exists only when attached to a terminal. */
  def isTerminal(): Boolean = true

  private def doFormat(
      formatter: Formatter,
      fmt: String,
      args: Array[Object]
  ): Console = {
    Objects.requireNonNull(fmt)
    writeLock.synchronized {
      formatter.format(fmt, args)
      pw.flush()
    }
    this
  }

  private def doReadLine(
      formatter: Formatter,
      fmt: String,
      args: Array[Object]
  ): String = {
    Objects.requireNonNull(fmt)
    writeLock.synchronized {
      readLock.synchronized {
        prompt(formatter, fmt, args)
        val line = readLineChars()
        if (line == null) null else new String(line)
      }
    }
  }

  private def doReadPassword(
      formatter: Formatter,
      fmt: String,
      args: Array[Object]
  ): Array[Char] = {
    Objects.requireNonNull(fmt)
    writeLock.synchronized {
      readLock.synchronized {
        Console.Echo.installRestoreHook()
        val echoDisabled = Console.Echo.disable()
        try {
          prompt(formatter, fmt, args)
          readLineChars()
        } finally {
          if (echoDisabled) Console.Echo.restore()
          // The line terminator typed by the user was not echoed
          pw.println()
          pw.flush()
        }
      }
    }
  }

  // Requires writeLock
  private def prompt(
      formatter: Formatter,
      fmt: String,
      args: Array[Object]
  ): Unit = {
    if (!fmt.isEmpty()) formatter.format(fmt, args)
    pw.flush()
  }

  /** Reads more input into `buf`, compacting or growing it first if needed.
   *  Returns the number of chars by which already buffered chars were shifted
   *  left, or -1 at end of input. Requires readLock.
   */
  private def fill(): Int = {
    val shift = pos
    if (pos > 0) {
      val remaining = lim - pos
      System.arraycopy(buf, pos, buf, 0, remaining)
      java.util.Arrays.fill(buf, remaining, lim, '\u0000')
      pos = 0
      lim = remaining
    }
    if (lim == buf.length) {
      val old = buf
      buf = java.util.Arrays.copyOf(old, old.length * 2)
      java.util.Arrays.fill(old, '\u0000')
    }
    val n =
      try decoder.read(buf, lim, buf.length - lim)
      catch { case e: IOException => throw new IOError(e) }
    if (n < 0) -1
    else {
      lim += n
      shift
    }
  }

  /** Reads one line without its terminator, or returns null at end of input if
   *  nothing was read. Requires readLock.
   */
  private def readLineChars(): Array[Char] = {
    var result: Array[Char] = null
    var done = false
    var scan = pos
    while (!done) {
      while (scan < lim && buf(scan) != '\n') scan += 1
      if (scan < lim) {
        result = takeLine(scan, scan + 1)
        done = true
      } else {
        val shift = fill()
        if (shift < 0) {
          // End of input: return a partial line, if any
          if (lim > pos) result = takeLine(lim, lim)
          done = true
        } else {
          scan -= shift
        }
      }
    }
    result
  }

  // Requires readLock
  private def takeLine(end: Int, next: Int): Array[Char] = {
    val stop =
      if (end > pos && buf(end - 1) == '\r') end - 1
      else end
    val line = java.util.Arrays.copyOfRange(buf, pos, stop)
    java.util.Arrays.fill(buf, pos, next, '\u0000')
    pos = next
    line
  }

  private def readChars(cbuf: Array[Char], off: Int, len: Int): Int = {
    if (off < 0 || len < 0 || len > cbuf.length - off)
      throw new IndexOutOfBoundsException()
    readLock.synchronized {
      if (len == 0) 0
      else if (pos == lim && fill() < 0) -1
      else {
        val n = Math.min(len, lim - pos)
        System.arraycopy(buf, pos, cbuf, off, n)
        java.util.Arrays.fill(buf, pos, pos + n, '\u0000')
        pos += n
        n
      }
    }
  }
}

object Console {
  private final val InitialBufferSize = 256

  /** The console of this process, or null when standard input or standard
   *  output is not a terminal.
   */
  private[java] lazy val system: Console =
    if (isTerminal()) new Console(terminalCharset()) else null

  private def isTerminal(): Boolean =
    if (isWindows) {
      val mode = stackalloc[DWord]()
      GetConsoleMode(stdIn, mode) && GetConsoleMode(stdOut, mode)
    } else {
      unistd.isatty(unistd.STDIN_FILENO) == 1 &&
      unistd.isatty(unistd.STDOUT_FILENO) == 1
    }

  private def terminalCharset(): Charset = {
    val name = System.getProperty("stdout.encoding")
    if (name == null) Charset.defaultCharset()
    else
      try Charset.forName(name)
      catch {
        case _: IllegalArgumentException => Charset.defaultCharset()
      }
  }

  // Closing the writer or reader must not close the terminal streams.
  private final class ConsoleWriter(out: Writer)
      extends PrintWriter(out, true) {
    override def close(): Unit = flush()
  }

  private final class ConsoleReader(console: Console) extends Reader {
    def read(cbuf: Array[Char], off: Int, len: Int): Int =
      console.readChars(cbuf, off, len)
    def close(): Unit = ()
  }

  /** Echo of the terminal input, turned off while reading passwords. Echo is
   *  restored by readPassword itself and, if the process terminates while a
   *  password is read, by a shutdown hook.
   */
  private object Echo {
    // Set while echo is turned off by us and not yet restored
    @volatile private var disabled = false

    private lazy val restoreHook: Unit =
      try
        Runtime
          .getRuntime()
          .addShutdownHook(new Thread(new Runnable {
            def run(): Unit = restore()
          }))
      catch {
        case _: IllegalStateException => () // shutdown already in progress
      }

    def installRestoreHook(): Unit = restoreHook

    /** Turns echo off. Returns true if it was on and is now off. */
    def disable(): Boolean = synchronized {
      val changed =
        if (isWindows) setWindowsEcho(false)
        else setPosixEcho(false)
      if (changed) disabled = true
      changed
    }

    def restore(): Unit = synchronized {
      if (disabled) {
        if (isWindows) setWindowsEcho(true)
        else setPosixEcho(true)
        disabled = false
      }
    }

    // Returns true if the echo flag was changed
    private def setPosixEcho(on: Boolean): Boolean = {
      val attrs = stackalloc[termios]()
      val fd = unistd.STDIN_FILENO
      if (tcgetattr(fd, attrs) != 0) false
      else {
        val echo = ECHO.toUInt
        val isOn = (attrs.c_lflag & echo) != 0.toUInt
        if (isOn == on) false
        else {
          attrs.c_lflag =
            if (on) attrs.c_lflag | echo
            else attrs.c_lflag & ~echo
          tcsetattr(fd, TCSANOW, attrs) == 0
        }
      }
    }

    // Returns true if the echo mode was changed
    private def setWindowsEcho(on: Boolean): Boolean = {
      val mode = stackalloc[DWord]()
      val handle = stdIn
      if (!GetConsoleMode(handle, mode)) false
      else {
        val isOn = (!mode & ENABLE_ECHO_INPUT) != 0.toUInt
        if (isOn == on) false
        else {
          val newMode =
            if (on) !mode | ENABLE_ECHO_INPUT
            else !mode & ~ENABLE_ECHO_INPUT
          SetConsoleMode(handle, newMode)
        }
      }
    }
  }
}
