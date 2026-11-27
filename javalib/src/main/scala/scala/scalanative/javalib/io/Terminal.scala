package scala.scalanative.javalib.io

import java.io.{IOError, IOException}

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.termios._
import scala.scalanative.posix.termiosOps._
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.ConsoleApi._
import scala.scalanative.windows.ConsoleApiExt._
import scala.scalanative.windows.DWord

/** Platform access to the terminal of the process, used by java.io.Console. */
object Terminal {

  /** True if both the standard input and the standard output are terminals. */
  def stdinAndStdoutAreTerminals(): Boolean =
    if (isWindows) {
      val mode = stackalloc[DWord]()
      GetConsoleMode(stdIn, mode) && GetConsoleMode(stdOut, mode)
    } else {
      unistd.isatty(unistd.STDIN_FILENO) == 1 &&
      unistd.isatty(unistd.STDOUT_FILENO) == 1
    }

  // Set while echo is turned off by us and not yet restored
  private var echoDisabled = false

  // Restores echo if the process terminates while it is turned off
  private lazy val restoreHook: Unit =
    try
      Runtime
        .getRuntime()
        .addShutdownHook(new Thread(new Runnable {
          def run(): Unit = restoreEcho()
        }))
    catch {
      case _: IllegalStateException => () // shutdown already in progress
    }

  /** Turns the echo of the standard input off, until `restoreEcho()`.
   *
   *  @throws java.io.IOError
   *    if echo is on and cannot be turned off
   */
  def disableEcho(): Unit = synchronized {
    restoreHook
    // Always check the terminal: echo may be on again despite a pending
    // restore. Unchanged keeps any pending restore.
    setEcho(false) match {
      case Changed   => echoDisabled = true
      case Unchanged => ()
      case Failed    =>
        throw new IOError(new IOException("Cannot disable terminal echo"))
    }
  }

  /** Turns the echo back on if `disableEcho()` turned it off. If that fails,
   *  the shutdown hook tries again.
   */
  def restoreEcho(): Unit = synchronized {
    if (echoDisabled && setEcho(true) != Failed) echoDisabled = false
  }

  // Outcome of setEcho
  private final val Changed = 0
  private final val Unchanged = 1
  private final val Failed = 2

  private def setEcho(on: Boolean): Int =
    if (isWindows) setWindowsEcho(on) else setPosixEcho(on)

  private def setPosixEcho(on: Boolean): Int = {
    val attrs = stackalloc[termios]()
    val fd = unistd.STDIN_FILENO
    if (tcgetattr(fd, attrs) != 0) Failed
    else {
      val echo = ECHO.toUInt
      val isOn = (attrs.c_lflag & echo) != 0.toUInt
      if (isOn == on) Unchanged
      else {
        attrs.c_lflag =
          if (on) attrs.c_lflag | echo
          else attrs.c_lflag & ~echo
        if (tcsetattr(fd, TCSANOW, attrs) == 0) Changed else Failed
      }
    }
  }

  private def setWindowsEcho(on: Boolean): Int = {
    val mode = stackalloc[DWord]()
    val handle = stdIn
    if (!GetConsoleMode(handle, mode)) Failed
    else {
      val isOn = (!mode & ENABLE_ECHO_INPUT) != 0.toUInt
      if (isOn == on) Unchanged
      else {
        val newMode =
          if (on) !mode | ENABLE_ECHO_INPUT
          else !mode & ~ENABLE_ECHO_INPUT
        if (SetConsoleMode(handle, newMode)) Changed else Failed
      }
    }
  }
}
