package java.lang

import java.io._
import java.nio.file.Files

import java.util.concurrent.TimeUnit
import scala.scalanative.unsigned._
import scala.scalanative.unsafe._
import scala.scalanative.posix.{fcntl, unistd}
import scala.io.Source

import org.junit.Test
import org.junit.Assert._

class ProcessInheritTest {
  import javalib.lang.ProcessUtils._

  @Test def inherit(): Unit = {
    val f = Files.createTempFile("/tmp", "out")
    val savedFD = unistd.dup(unistd.STDOUT_FILENO)
    val flags = fcntl.O_RDWR | fcntl.O_TRUNC | fcntl.O_CREAT
    val fd = Zone { implicit z =>
      fcntl.open(toCString(f.toAbsolutePath.toString), flags, 0.toUInt)
    }

    val out =
      try {
        unistd.dup2(fd, unistd.STDOUT_FILENO)
        unistd.close(fd)
        val proc = new ProcessBuilder("ls", resourceDir).inheritIO().start()
        proc.waitFor(5, TimeUnit.SECONDS)
        readInputStream(new FileInputStream(f.toFile))
      } finally {
        unistd.dup2(savedFD, unistd.STDOUT_FILENO)
        unistd.close(savedFD)
      }

    assertEquals(scripts, out.split("\n").toSet)
  }
}
