package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.signal._
import scala.scalanative.posix.signalOps._
import scala.scalanative.unsafe._

object RuntimeSignalHandlerTest {
  @volatile var received: CInt = 0
  val handler: CFuncPtr1[CInt, Unit] = (sig: CInt) => received = sig
}

// Issue #5288
class RuntimeSignalHandlerTest {
  import RuntimeSignalHandlerTest._

  private def currentHandler(sig: CInt): CVoidPtr = {
    val current = stackalloc[sigaction]()
    assertEquals(0, sigaction(sig, null, current))
    !current.at1.asInstanceOf[Ptr[CVoidPtr]]
  }

  private def withDisposition(sig: CInt, fn: CFuncPtr1[CInt, Unit])(
      body: => Unit
  ): Unit = {
    val act = stackalloc[sigaction]()
    val oact = stackalloc[sigaction]()
    act.sa_handler = fn
    assertEquals(0, sigaction(sig, act, oact))
    val hook = new Thread(() => ())
    try {
      Runtime.getRuntime().addShutdownHook(hook)
      body
    } finally {
      Runtime.getRuntime().removeShutdownHook(hook)
      assertEquals(0, sigaction(sig, oact, null))
    }
  }

  @Test def keepsApplicationSignalHandlers(): Unit = {
    assumeFalse("POSIX only", isWindows)
    for (sig <- Seq(SIGINT, SIGTERM)) withDisposition(sig, handler) {
      assertEquals(CFuncPtr.toPtr(handler), currentHandler(sig))
      received = 0
      assertEquals(0, raise(sig))
      assertEquals(sig, received)
    }
  }

  @Test def keepsIgnoredSignals(): Unit = {
    assumeFalse("POSIX only", isWindows)
    for (sig <- Seq(SIGINT, SIGTERM)) withDisposition(sig, SIG_IGN) {
      assertEquals(CFuncPtr.toPtr(SIG_IGN), currentHandler(sig))
    }
  }

  @Test def installsHandlerOverDefaultDisposition(): Unit = {
    assumeFalse("POSIX only", isWindows)
    for (sig <- Seq(SIGINT, SIGTERM)) withDisposition(sig, SIG_DFL) {
      val installed = currentHandler(sig)
      assertNotEquals(CFuncPtr.toPtr(SIG_DFL), installed)
      assertNotEquals(CFuncPtr.toPtr(SIG_IGN), installed)
    }
  }
}
