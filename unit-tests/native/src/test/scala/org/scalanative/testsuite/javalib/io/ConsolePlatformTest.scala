package org.scalanative.testsuite.javalib.io

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.unistd
import scala.scalanative.unsafe._
import scala.scalanative.windows.ConsoleApi.GetConsoleMode
import scala.scalanative.windows.ConsoleApiExt.{stdIn, stdOut}
import scala.scalanative.windows.DWord

class ConsolePlatformTest {

  private def stdinAndStdoutAreTerminals(): Boolean =
    if (isWindows) {
      val mode = stackalloc[DWord]()
      GetConsoleMode(stdIn, mode) && GetConsoleMode(stdOut, mode)
    } else {
      unistd.isatty(unistd.STDIN_FILENO) == 1 &&
      unistd.isatty(unistd.STDOUT_FILENO) == 1
    }

  @Test def consoleExistsOnlyForTerminals(): Unit = {
    assertEquals(
      stdinAndStdoutAreTerminals(),
      System.console() != null
    )
  }
}
