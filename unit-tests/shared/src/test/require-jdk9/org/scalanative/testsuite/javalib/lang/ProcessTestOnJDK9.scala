package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.TimeUnit
import java.io._
import java.nio.file._
import java.nio.charset.StandardCharsets

import scala.io.Source

import org.junit._
import org.junit.Assert._
import org.junit.Assume._
import org.junit.Ignore

import org.scalanative.testsuite.utils.Platform, Platform._
import scala.scalanative.junit.utils.AssumesHelper._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ProcessTestOnJDK9 {
  @BeforeClass
  def checkRuntime(): Unit = {
    assumeTrue(isMultithreadingEnabled)
  }
}

class ProcessTestOnJDK9 {
  import ProcessUtils._

  @Test def onExitHandles(): Unit = {
    val proc = processSleep(0.1).start()
    val handle = proc.toHandle()
    val f1, f2 = handle.onExit()
    val f3, f4 = proc.onExit()

    assertNotSame("each onExit invocation should return a new instance", f1, f2)
    assertNotSame("each onExit invocation should return a new instance", f3, f4)
    assertEquals(
      "All completions should refer to the same pid",
      1,
      Seq(f1, f2, f3, f4)
        .map(_.get())
        .map {
          case handle: ProcessHandle => handle.pid()
          case proc: Process         => proc.pid()
        }
        .distinct
        .size
    )

  }

  @Test def onExitTerminates(): Unit = {
    val proc = processSleep(1).start()
    val completion = proc.onExit()
    assertFalse(completion.isDone())
    assertFalse(completion.isCancelled())

    completion.get()
    assertTrue(completion.isDone())
    assertFalse(completion.isCancelled())
    assertFalse(proc.isAlive())
  }

  // copy from ProcessTest
  private def processForDestruction(): Process = {
    val proc = processForCommand("ping", "-c", "2", "-i", "10", "127.0.0.1")
      .start()
    proc.getInputStream().read()
    proc
  }

  @Test def processInfo(): Unit = {
    val proc = processForDestruction()
    try {
      val info = proc.toHandle().info()
      if (executingInJVM) {
        // Might be empty sometimes on JVM but needs to be defined in SN runtime
        assumeTrue(s"no command on JVM - $info", info.command().isPresent())
        assumeTrue(s"no args on JVM - $info", info.arguments().isPresent())
        assumeTrue(
          s"no commandLine on JVM - $info",
          info.commandLine().isPresent()
        )
      }
      assertTrue(
        s"command: ${info.command()}",
        info.command().get().contains("ping")
      )
      assertEquals(
        Seq("-c", "2", "-i", "10", "127.0.0.1"),
        info.arguments().get().toSeq
      )
      assertTrue(
        s"command line (cmd): ${info.commandLine()}",
        info.commandLine().get().contains("ping")
      )
      assertTrue(
        s"command line (args): ${info.commandLine()}",
        info.commandLine().get().contains("-c")
      )
      // TODO not implemented:
      // startInstant: Optional[Instant]
      // totalCpuDuration: Optional[Duration]
      // user: Optional[String]
    } finally proc.destroy()
  }

  @Test def destroy(): Unit = {
    val proc = processForDestruction()
    val handle = proc.toHandle()

    assertTrue(handle.destroy())

    // Throws on timeout
    assertFalse(
      "process should have been terminated",
      handle.onExit().get(500, TimeUnit.MILLISECONDS).isAlive()
    )
    assertEquals(
      // SIGTERM, use unix signal 'excess 128' convention on non-Windows.
      if (isWindows) 1 else 0x80 + 15,
      proc.exitValue
    )
  }

  @Test def destroyForcibly(): Unit = {
    val proc = processForDestruction()
    val handle = proc.toHandle()

    assertTrue(handle.destroyForcibly())

    // Throws on timeout
    assertFalse(
      "process should have been terminated",
      handle.onExit().get(500, TimeUnit.MILLISECONDS).isAlive()
    )
    assertEquals(
      // SIGKILL, use unix signal 'excess 128' convention on non-Windows.
      if (isWindows) 1 else 0x80 + 9,
      proc.exitValue
    )
  }

}
