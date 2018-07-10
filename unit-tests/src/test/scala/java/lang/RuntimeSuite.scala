package java.lang

import java.util.concurrent.TimeUnit
import java.io.File

object RuntimeSuite extends tests.Suite {
  import ProcessSuite._
  test("exec command") {
    val proc = Runtime.getRuntime.exec(Array("ls", resourceDir))
    val out  = readInputStream(proc.getInputStream)
    assert(proc.waitFor(5, TimeUnit.SECONDS))
    assert(out.split("\n").toSet == Set("echo.sh", "err.sh", "hello.sh", "ls"))
  }
  test("exec envp") {
    val envp = Array(s"PATH=$resourceDir")
    val proc = Runtime.getRuntime.exec(Array("ls"), envp)
    val out  = readInputStream(proc.getInputStream)
    assert(proc.waitFor(5, TimeUnit.SECONDS))
    assert(out == "1")
  }
  test("exec dir") {
    val proc = Runtime.getRuntime.exec(Array("ls"), null, new File(resourceDir))
    val out  = readInputStream(proc.getInputStream)
    assert(proc.waitFor(5, TimeUnit.SECONDS))
    assert(out.split("\n").toSet == Set("echo.sh", "err.sh", "hello.sh", "ls"))
  }
}
