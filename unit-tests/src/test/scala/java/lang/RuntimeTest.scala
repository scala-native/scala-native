package java.lang

import java.util.concurrent.TimeUnit
import java.io.File

import org.junit.Test
import org.junit.Assert._

class RuntimeTest {
  import ProcessSuite._
  @Test def execCommand(): Unit = {
    val proc = Runtime.getRuntime.exec(Array("ls", resourceDir))
    val out  = readInputStream(proc.getInputStream)
    assert(proc.waitFor(5, TimeUnit.SECONDS))
    assert(out.split("\n").toSet == Set("echo.sh", "err.sh", "hello.sh", "ls"))
  }
  @Test def execEnvp(): Unit = {
    val envp = Array(s"PATH=$resourceDir")
    val proc = Runtime.getRuntime.exec(Array("ls"), envp)
    val out  = readInputStream(proc.getInputStream)
    assert(proc.waitFor(5, TimeUnit.SECONDS))
    assert(out == "1")
  }
  @Test def execDir(): Unit = {
    val proc = Runtime.getRuntime.exec(Array("ls"), null, new File(resourceDir))
    val out  = readInputStream(proc.getInputStream)
    assert(proc.waitFor(5, TimeUnit.SECONDS))
    assert(out.split("\n").toSet == Set("echo.sh", "err.sh", "hello.sh", "ls"))
  }
}
