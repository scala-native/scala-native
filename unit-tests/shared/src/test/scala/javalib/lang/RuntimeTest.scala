package javalib.lang

import java.lang._

import java.util.concurrent.TimeUnit
import java.io.File

import org.junit.Test
import org.junit.Assert._

class RuntimeTest {
  import ProcessUtils._
  @Test def execCommand(): Unit = {
    val proc = Runtime.getRuntime.exec(Array("ls", resourceDir))
    val out = readInputStream(proc.getInputStream)
    assertTrue(proc.waitFor(5, TimeUnit.SECONDS))
    assertTrue(
      out.split("\n").toSet == Set("echo.sh", "err.sh", "hello.sh", "ls")
    )
  }
  @Test def execEnvp(): Unit = {
    val envp = Array(s"PATH=$resourceDir")
    val proc = Runtime.getRuntime.exec(Array("ls"), envp)
    val out = readInputStream(proc.getInputStream)
    assertTrue(proc.waitFor(5, TimeUnit.SECONDS))
    assertTrue(out == "1")
  }
  @Test def execDir(): Unit = {
    val proc = Runtime.getRuntime.exec(Array("ls"), null, new File(resourceDir))
    val out = readInputStream(proc.getInputStream)
    assertTrue(proc.waitFor(5, TimeUnit.SECONDS))
    assertTrue(
      out.split("\n").toSet == Set("echo.sh", "err.sh", "hello.sh", "ls")
    )
  }
}
