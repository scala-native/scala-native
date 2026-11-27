package scala.scalanative.javalib.io

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

  /** Turns the echo of the standard input off, until `restoreEcho()`. */
  def disableEcho(): Unit = synchronized {
    restoreHook
    if (!echoDisabled) echoDisabled = setEcho(false)
  }

  /** Turns the echo back on if `disableEcho()` turned it off. */
  def restoreEcho(): Unit = synchronized {
    if (echoDisabled) {
      setEcho(true)
      echoDisabled = false
    }
  }

  // Returns true if the echo flag was changed
  private def setEcho(on: Boolean): Boolean =
    if (isWindows) setWindowsEcho(on) else setPosixEcho(on)

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
