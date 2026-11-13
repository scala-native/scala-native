package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.signal._
import scala.scalanative.posix.signalOps._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

@extern
object SignalTestNative {
  @name("scalanative_sigaction_size")
  def sigactionSize(): CSize = extern
}

object SignalTest {
  @volatile var received: CInt = 0
  val handler: CFuncPtr1[CInt, Unit] = (sig: CInt) => received = sig
}

class SignalTest {
  import SignalTest._

  // Issue #4274
  @Test def sigactionSizeMatchesBridge(): Unit = if (!isWindows) {
    assertEquals(SignalTestNative.sigactionSize(), sizeof[sigaction])
  }

  @Test def sigactionRoundTrip(): Unit = if (!isWindows) {
    val act = stackalloc[sigaction]()
    val oact = stackalloc[sigaction]()
    val restored = stackalloc[sigaction]()
    val maskBit = 1.toCSize << (SIGUSR2 - 1)
    act.sa_handler = handler
    act.sa_flags = SA_RESTART
    !act.at2.asInstanceOf[Ptr[CSize]] = maskBit

    assertEquals(0, sigaction(SIGUSR1, act, oact))
    try {
      val current = stackalloc[sigaction]()
      assertEquals(0, sigaction(SIGUSR1, null, current))
      assertEquals(
        CFuncPtr.toPtr(handler),
        CFuncPtr.toPtr(current.sa_handler)
      )
      assertEquals(SA_RESTART, current.sa_flags & SA_RESTART)
      assertEquals(0, current.sa_flags & SA_SIGINFO)
      assertEquals(maskBit, !current.at2.asInstanceOf[Ptr[CSize]] & maskBit)

      received = 0
      assertEquals(0, raise(SIGUSR1))
      assertEquals(SIGUSR1, received)
    } finally {
      assertEquals(0, sigaction(SIGUSR1, oact, restored))
    }
  }
}
