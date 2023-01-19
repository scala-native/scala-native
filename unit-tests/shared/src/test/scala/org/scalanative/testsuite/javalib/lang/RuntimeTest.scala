package org.scalanative.testsuite.javalib.lang

import java.lang._

import java.util.concurrent.TimeUnit
import java.io.File

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.scalanative.testsuite.utils.Platform._
import scala.scalanative.junit.utils.AssumesHelper._

class RuntimeTest {
  import ProcessUtils._
  private val EOL = System.lineSeparator()

  /* See Issues #2649 & #2652 re use of 'ls'.
   * Avoid a problem with ":.:", or variants in the user's PATH definition.
   * Historically the 'resourceDir' has contained an executable file
   * named 'ls'. When $PATH contains "." that file gets executed
   * rather that the system standard "ls", causing the execDir test to
   * fail.
   *
   * A private "ls" being found on the user's path before the
   * system standard "ls" causes similar issues.
   *
   * Guessing and using a hard coded "ls" is the least bad
   * of several design alternatives ("dir", "find").
   *
   * The chosen locations should work on unmodified Linux, macOS, and
   * FreeBSD systems. Given the variety of system
   * configurations in the wild, some OS or system is bound to have 'ls'
   * in a different location.
   */

  private def lsCommand =
    if (isWindows) {
      Array("cmd", "/c", "dir", "/b")
    } else {
      Array("/bin/ls")
    }

  @Test def execCommand(): Unit = {
    val proc = Runtime.getRuntime.exec(lsCommand :+ resourceDir)
    val out = readInputStream(proc.getInputStream)
    assertTrue(proc.waitFor(5, TimeUnit.SECONDS))
    assertEquals(Scripts.values.map(_.filename), out.split(EOL).toSet)
  }
  @Test def execEnvp(): Unit = {
    assumeNotJVMCompliant()
    assumeFalse(
      "Not possible in Windows, would use dir keyword anyway",
      isWindows
    )

    val envp = Array(s"PATH=$resourceDir")
    val proc = Runtime.getRuntime.exec(Array("ls"), envp)
    assertTrue(proc.waitFor(5, TimeUnit.SECONDS))
    assertEquals("1", readInputStream(proc.getInputStream))
  }
  @Test def execDir(): Unit = {
    val proc =
      Runtime.getRuntime.exec(lsCommand, null, new File(resourceDir))
    val out = readInputStream(proc.getInputStream)
    assertTrue(proc.waitFor(5, TimeUnit.SECONDS))
    assertEquals(Scripts.values.map(_.filename), out.split(EOL).toSet)

  }
}
