package org.scalanative.testsuite.posixlib

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.bsd.kevent
import scala.scalanative.libc.signal.raise
import scala.scalanative.meta.LinktimeInfo._
import scala.scalanative.posix.signal._
import scala.scalanative.posix.sys.select
import scala.scalanative.posix.time.timespec
import scala.scalanative.posix.timeOps._
import scala.scalanative.posix.{fcntl, sched, unistd}
import scala.scalanative.unsafe._

/* Functions NetBSD renames in its headers (e.g. kevent -> __kevent100),
 * called through C glue so the current symbols are used.
 */
class RenamedSymbolsTest {

  private final val SigsetBytes = 256

  // alloc zeroes memory
  private def zeroTimeout()(implicit z: Zone): Ptr[timespec] = alloc[timespec]()

  @Test def dup3SetsCloseOnExec(): Unit = if (!isWindows && !isApple) {
    val fds = stackalloc[CInt](2)
    assertEquals(0, unistd.pipe(fds))
    val fd = fds(0)
    val target = fds(1)
    try {
      assertEquals(target, unistd.dup3(fd, target, fcntl.O_CLOEXEC))
      val flags = fcntl.fcntl(target, fcntl.F_GETFD, 0)
      assertEquals(fcntl.FD_CLOEXEC, flags & fcntl.FD_CLOEXEC)
    } finally {
      unistd.close(fd)
      unistd.close(target)
    }
  }

  @Test def pselectWithZeroTimeout(): Unit = if (!isWindows) {
    Zone.acquire { implicit z =>
      val mask = alloc[Byte](SigsetBytes).asInstanceOf[Ptr[sigset_t]]
      sigemptyset(mask)
      assertEquals(0, select.pselect(0, null, null, null, zeroTimeout(), mask))
    }
  }

  @Test def schedRrGetInterval(): Unit =
    if (!isWindows && !isApple && !isOpenBSD) {
      val ts = stackalloc[timespec]()
      assertEquals(0, sched.sched_rr_get_interval(0, ts))
      assertTrue(ts.tv_sec.toLong >= 0L)
      val nsec = ts.tv_nsec.toLong
      assertTrue(s"tv_nsec $nsec", nsec >= 0L && nsec < 1000000000L)
    }

  @Test def sigtimedwaitReturnsPendingSignal(): Unit =
    if (!isWindows && !isApple && !isOpenBSD) Zone.acquire { implicit z =>
      val set = alloc[Byte](SigsetBytes).asInstanceOf[Ptr[sigset_t]]
      val old = alloc[Byte](SigsetBytes).asInstanceOf[Ptr[sigset_t]]
      sigemptyset(set)
      sigaddset(set, SIGUSR2)
      assertEquals(0, pthread_sigmask(SIG_BLOCK, set, old))
      try {
        assertEquals(0, raise(SIGUSR2))
        assertEquals(SIGUSR2, sigtimedwait(set, null, zeroTimeout()))
      } finally pthread_sigmask(SIG_SETMASK, old, null)
    }

  @Test def keventWithoutChanges(): Unit =
    if (isFreeBSD || isNetBSD || isApple) Zone.acquire { implicit z =>
      val kq = kevent.kqueue()
      assertTrue(kq >= 0)
      try
        assertEquals(
          0,
          kevent.kevent(kq, null, 0, null, 0, zeroTimeout())
        )
      finally unistd.close(kq)
    }
}
