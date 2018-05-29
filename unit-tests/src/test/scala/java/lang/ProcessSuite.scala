package java.lang

import java.util.concurrent.TimeUnit
import java.io._
import java.nio.file.Files

import scala.scalanative.native.{Zone, toCString}
import scala.scalanative.posix.{fcntl, unistd}
import scala.io.Source

object ProcessSuite extends tests.Suite {
  def readInputStream(s: InputStream) = Source.fromInputStream(s).mkString
  val resourceDir =
    s"${System.getProperty("user.dir")}/unit-tests/src/test/resources/process"
  // This makes it easy to decorate the test for debugging
  def addTest[R](name: String)(f: => R): Unit = test(name) {
    f
  }
  addTest("ls") {
    val proc = new ProcessBuilder("ls", resourceDir).start()
    val out  = readInputStream(proc.getInputStream)
    proc.waitFor
    assert(out.split("\n").toSet == Set("echo.sh", "err.sh", "ls"))
  }

  addTest("inherit") {
    val f       = Files.createTempFile("/tmp", "out")
    val savedFD = unistd.dup(unistd.STDOUT_FILENO)
    val flags   = fcntl.O_RDWR | fcntl.O_TRUNC | fcntl.O_CREAT
    val fd = Zone { implicit z =>
      fcntl.open(toCString(f.toAbsolutePath.toString), flags)
    }
    val out = try {
      unistd.dup2(fd, unistd.STDOUT_FILENO)
      fcntl.close(fd)
      val proc = new ProcessBuilder("ls", resourceDir).inheritIO().start()
      proc.waitFor(5, TimeUnit.SECONDS)
      readInputStream(new FileInputStream(f.toFile))
    } finally {
      unistd.dup2(savedFD, unistd.STDOUT_FILENO)
      fcntl.close(savedFD)
    }
    assert(out.split("\n").toSet == Set("echo.sh", "err.sh", "ls"))
  }

  private def checkPathOverride(pb: ProcessBuilder) = {
    val proc = pb.start()
    val out  = readInputStream(proc.getInputStream)
    proc.waitFor
    assert(out == "1")
  }

  addTest("PATH override") {
    val pb = new ProcessBuilder("ls", resourceDir)
    pb.environment.put("PATH", resourceDir)
    checkPathOverride(pb)
  }

  addTest("PATH prefix override") {
    val pb = new ProcessBuilder("ls", resourceDir)
    pb.environment.put("PATH", s"$resourceDir:${pb.environment.get("PATH")}")
    checkPathOverride(pb)
  }

  addTest("input and error stream") {
    val pb  = new ProcessBuilder("err.sh")
    val cwd = System.getProperty("user.dir")
    pb.environment.put("PATH", s"$cwd/unit-tests/src/test/resources/process")
    val proc = pb.start()
    proc.waitFor
    assert(readInputStream(proc.getErrorStream) == "foo")
    assert(readInputStream(proc.getInputStream) == "bar")
  }

  addTest("input stream writes to file") {
    val pb = new ProcessBuilder("echo.sh")
    pb.environment.put("PATH", resourceDir)
    val file = File.createTempFile("istest", ".tmp", new File("/tmp"))
    pb.redirectOutput(file)
    try {
      val proc = pb.start()
      proc.getOutputStream.write("hello\n".getBytes)
      proc.getOutputStream.write("quit\n".getBytes)
      proc.getOutputStream.flush()
      proc.waitFor
      val out = Source.fromFile(file.toString).getLines mkString "\n"
      assert(out == "hello")
    } finally {
      file.delete()
    }
  }

  addTest("output stream reads from file") {
    val pb = new ProcessBuilder("echo.sh")
    pb.environment.put("PATH", resourceDir)
    val file = File.createTempFile("istest", ".tmp", new File("/tmp"))
    pb.redirectInput(file)
    try {
      val proc = pb.start()
      val os   = new FileOutputStream(file)
      os.write("hello\n".getBytes)
      os.write("quit\n".getBytes)
      proc.waitFor
      val out = readInputStream(proc.getInputStream)
      assert(out == "hello")
    } finally {
      file.delete()
    }
  }

  addTest("redirectErrorStream") {
    val pb  = new ProcessBuilder("err.sh")
    val cwd = System.getProperty("user.dir")
    pb.environment.put("PATH", s"$cwd/unit-tests/src/test/resources/process")
    pb.redirectErrorStream(true)
    val proc = pb.start()
    proc.waitFor
    val out = readInputStream(proc.getInputStream)
    val err = readInputStream(proc.getErrorStream)
    assert(out == "foobar")
    assert(err == "")
  }

  addTest("waitFor with timeout completes") {
    val proc = new ProcessBuilder("sleep", "0.001").start()
    assert(proc.waitFor(1, TimeUnit.SECONDS))
    assert(proc.exitValue == 0)
  }

  addTest("waitFor with timeout times out") {
    val proc = new ProcessBuilder("sleep", ".005").start()
    assert(!proc.waitFor(1, TimeUnit.MILLISECONDS))
    assert(proc.isAlive)
    proc.waitFor(1, TimeUnit.SECONDS)
  }

  addTest("destroy") {
    val proc = new ProcessBuilder("sleep", ".005").start()
    assert(proc.isAlive)
    proc.destroy()
    assert(proc.waitFor(100, TimeUnit.MILLISECONDS))
    assert(proc.exitValue == 0x80 + 9)
  }

  addTest("destroyForcibly") {
    val proc = new ProcessBuilder("sleep", ".005").start()
    assert(proc.isAlive)
    val p = proc.destroyForcibly()
    assert(p.waitFor(100, TimeUnit.MILLISECONDS))
    assert(p.exitValue == 0x80 + 9)
  }
}
