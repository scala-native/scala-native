package org.scalanative.testsuite.posixlib
package sys

import org.junit.Test
import org.junit.Assert.*

import scalanative.runtime.Platform
import scalanative.meta.LinktimeInfo.isWindows
import scalanative.unsafe.{CInt, Ptr, Zone, alloc}
import scalanative.unsigned.*

import scalanative.posix.errno.*
import scalanative.posix.sys.resource.*
import scalanative.posix.sys.resourceOps.*
import scalanative.posix.sys.timeOps.*

// Design notes:
//
//   * The two methods setpriority() & setrlimit() make changes to the
//     process execution environment. For that reason they are not tested
//     here.
//
//     unit-tests run sequentially in the same process, so a change caused
//     by either of those methods could affect later tests in ways that are
//     hard to trace.
//
//     A save/restore approach is possible, but works only with _exquisite_
//     care that the restore is _always_executed, even on error. Too fragile
//     for now.
//
//   * The intent here is not to be exhaustive but rather to exercise
//     expected common sucess & failure paths for each method.

class ResourceTest {

  case class TestInfo(name: String, value: CInt)

  @Test def getpriorityInvalidArgWhich() = if (!isWindows) {
    errno = 0
    val invalidWhich = -1

    getpriority(invalidWhich, 0.toUInt)

    assertEquals("unexpected errno", EINVAL, errno)
  }

  @Test def getpriorityInvalidArgWho() = if (!isWindows) {
    errno = 0

    getpriority(PRIO_PROCESS, UInt.MaxValue)

    // Most operating systems will return EINVAL. Handle corner cases here.
    if (errno != EINVAL) {
      if (!(Platform.isLinux() || Platform.isFreeBSD() || Platform
            .isOpenBSD() || Platform.isNetBSD())) {
        assertEquals("unexpected errno", EINVAL, errno)
      } else if (errno != 0) { // Linux, FreeBSD
        // A pid of UInt.MaxValue is highly unlikely but, by one reading,
        // possible. If it exists and is found, it should not cause this test
        // to fail, just to be specious (have false look of genuineness).
        assertEquals("unexpected errno", ESRCH, errno)
      }
    }
  }

  @Test def testGetpriority() = if (!isWindows) {
    val cases = Array(
      TestInfo("PRIO_PROCESS", PRIO_PROCESS),
      TestInfo("PRIO_PGRP", PRIO_PGRP),
      TestInfo("PRIO_USER", PRIO_USER)
    )

    for (c <- cases) {
      errno = 0

      val result = getpriority(c.value, 0.toUInt)

      assertEquals("unexpected errno", 0, errno)

      // Beware: these are linux un-nice "nice" priorities,
      // where -20 is least "nice", so highest priority.
      assertTrue(
        s"${c.name} result: ${result} not in inclusive range [-20, 19]",
        ((result >= -20) && (result <= 19))
      )
    }
  }

  @Test def getrlimitInvalidArgResource() = if (!isWindows) {
    Zone.acquire { implicit z =>
      errno = 0
      val rlimPtr = alloc[rlimit]()

      getrlimit(Integer.MAX_VALUE, rlimPtr)

      assertEquals("unexpected errno", EINVAL, errno)
    }
  }

  @Test def testGetrlimit() = if (!isWindows) {
    Zone.acquire { implicit z =>
      val cases = Array(
        TestInfo("RLIMIT_AS", RLIMIT_AS),
        TestInfo("RLIMIT_CORE", RLIMIT_CORE),
        TestInfo("RLIMIT_CPU", RLIMIT_CPU),
        TestInfo("RLIMIT_DATA", RLIMIT_DATA),
        TestInfo("RLIMIT_FSIZE", RLIMIT_FSIZE),
        TestInfo("RLIMIT_NOFILE", RLIMIT_NOFILE),
        TestInfo("RLIMIT_STACK", RLIMIT_STACK)
      )

      for (c <- cases) {
        errno = 0
        val rlimPtr = alloc[rlimit]() // start each pass with all bytes 0.

        val result = getrlimit(c.value, rlimPtr)

        assertEquals(
          s"${c.name} unexpected failure, errno: ${errno}",
          0,
          result
        )

        // Coarse grain sanity checks. Do better someday.
        assertTrue(
          s"${c.name} rlim_cur: ${rlimPtr.rlim_cur} < 0",
          rlimPtr.rlim_cur >= 0.toUInt
        )

        assertTrue(
          s"${c.name} rlim_max: ${rlimPtr.rlim_max} < 0",
          rlimPtr.rlim_max >= 0.toUInt
        )

        assertTrue(
          s"${c.name} rlim_cur > rlim_max",
          rlimPtr.rlim_cur <= rlimPtr.rlim_max
        )
      }
    }
  }

  @Test def getrusageInvalidArgWho() = if (!isWindows) {
    Zone.acquire { implicit z =>
      errno = 0
      val rusagePtr = alloc[rusage]()

      getrusage(Integer.MIN_VALUE, rusagePtr)

      assertEquals("unexpected errno", EINVAL, errno)
    }
  }

  @Test def getrusageSelf() = if (!isWindows) {
    Zone.acquire { implicit z =>
      errno = 0
      val rusagePtr = alloc[rusage]()

      val result = getrusage(RUSAGE_SELF, rusagePtr)

      assertEquals(s"unexpected failure, errno: ${errno}", 0, result)

      assertTrue(
        s"unexpected ru_utime.tv_sec: ${rusagePtr.ru_utime.tv_sec} < 0",
        rusagePtr.ru_utime.tv_sec >= 0
      )

      val MICROS_PER_SECOND = 1000 * 1000

      val utUsec = rusagePtr.ru_utime.tv_usec
      assertTrue(
        s"unexpected ru_utime: ${rusagePtr.ru_utime.tv_sec} " +
          s"${rusagePtr.ru_utime.tv_usec}",
        (utUsec >= 0) && (utUsec < MICROS_PER_SECOND)
      )

      val stUsec = rusagePtr.ru_stime.tv_usec
      assertTrue(
        s"unexpected ru_stime: ${rusagePtr.ru_utime.tv_sec} " +
          s"${rusagePtr.ru_utime.tv_usec}",
        (stUsec >= 0) && (stUsec < MICROS_PER_SECOND)
      )
    }
  }

  @Test def getrusageChildren() = if (!isWindows) {
    Zone.acquire { implicit z =>
      errno = 0
      val rusagePtr = alloc[rusage]()

      val result = getrusage(RUSAGE_CHILDREN, rusagePtr)

      assertEquals(s"unexpected failure, errno: ${errno}", 0, result)

      // tv_sec could validly be 0 if either no descendents
      // have been created or descendents were created but
      // all completed quickly.

      assertTrue(
        s"unexpected ru_utime.tv_sec < 0",
        rusagePtr.ru_utime.tv_sec >= 0
      )

      assertTrue(
        s"unexpected ru_stime.tv_sec < 0",
        rusagePtr.ru_stime.tv_sec >= 0
      )
    }
  }
}
