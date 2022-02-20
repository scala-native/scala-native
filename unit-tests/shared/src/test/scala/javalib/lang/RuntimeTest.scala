package javalib.lang

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
  private def lsCommand =
    if (isWindows)
      Array("cmd", "/c", "dir", "/b")
    else
      Array("ls")

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
