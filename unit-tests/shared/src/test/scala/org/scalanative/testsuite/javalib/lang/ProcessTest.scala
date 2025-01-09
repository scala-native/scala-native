package org.scalanative.testsuite.javalib.lang

import java.util.concurrent.TimeUnit
import java.io._
import java.nio.file._
import java.nio.charset.StandardCharsets

import scala.io.Source

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._
import org.junit.Ignore

import org.scalanative.testsuite.utils.Platform, Platform._
import scala.scalanative.junit.utils.AssumesHelper._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ProcessTest {
  import ProcessUtils._

  @Test def concurrentPipe(): Unit = {
    assumeNotCrossCompiling()
    assumeMultithreadingIsEnabled()
    assumeNot32Bit() // Flaky on x86
    // Ensure that reading from process stdout does not lead to exceptions
    // when thread terminates (was failing with Bad file descriptor in FileChannel.read)
//    val iterations = 16
    val iterations = 4
    val tasks = for (n <- 0 until iterations) yield Future {
      val proc = processForScript(Scripts.hello).start()
      var done = false
      val t = new Thread(() => {
        val src = proc.getInputStream()
        val buffer = new Array[Byte](64)
        while (src.read(buffer) != -1) ()
        assertEquals(
          "concurrentPipe()",
          s"hello",
          new String(buffer, StandardCharsets.UTF_8).trim()
        )
        done = true
      })
      t.start()
      assertEquals(0, proc.waitFor())
      t.join()
      done
    }
    assertTrue(
      Await
        .result(Future.sequence(tasks), iterations.seconds)
        .forall(_ == true)
    )
  }

}
