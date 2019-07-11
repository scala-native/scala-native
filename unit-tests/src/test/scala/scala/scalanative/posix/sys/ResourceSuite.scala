package scala.scalanative
package posix
package sys

import scalanative.libc.errno
import scalanative.posix.errno._
import scalanative.unsafe.{CInt, Ptr, Zone, alloc}
import scalanative.unsigned._

import resource._, resourceOps._
import timeOps._

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
//     A save/restore approach is possible, but works only with _exqusite_
//     care that the restore is _always_executed, even on error. Too fragile
//     for now.
//
//   * The intent here is not to be exhaustive but rather to exercise
//     expected common sucess & failure paths for each method.

object ResourceSuite extends tests.Suite {

  case class TestInfo(name: String, value: CInt)

  test("getpriority(which, who) - invalid arg: which") {

    errno.errno = 0

    val result = getpriority(-1, 0.toUInt)

    assert(((result == -1) && (errno.errno != 0)),
           s"Unexpected success result: ${result}")

    val expected = EINVAL
    assert(errno.errno == expected,
           s"errno: ${errno.errno} != expected: ${expected}")
  }

  test("getpriority(which, who) - invalid arg: who") {

    errno.errno = 0

    val result = getpriority(PRIO_PROCESS, -1.toUInt)

    assert(((result == -1) && (errno.errno != 0)),
           s"Unexpected success result: ${result}")

    val expected = ESRCH
    assert(errno.errno == expected,
           s"errno: ${errno.errno} != expected: ${expected}")
  }

  test("getpriority(which, who)") {

    // format: off
    val cases = Array(TestInfo("PRIO_PROCESS", PRIO_PROCESS),
                      TestInfo("PRIO_PGRP",    PRIO_PGRP),
                      TestInfo("PRIO_USER",    PRIO_USER)
                     )
    // format: on

    for (c <- cases) {
      errno.errno = 0

      val result = getpriority(c.value, 0.toUInt)

      assert(errno.errno == 0, s"errno: ${errno.errno} != expected: 0")

      // Beware: these are linux un-nice "nice" priorities,
      // where -20 is least "nice", so highest priority.
      assert(((result >= -20) && (result <= 19)),
             s"${c.name} result: ${result} not in inclusive range [-20, 19]")
    }
  }

  test("getrlimit(resource, rlim) - invalid arg: resource") {
    Zone { implicit z =>
      val rlimPtr = alloc[rlimit]

      val result = getrlimit(Integer.MAX_VALUE, rlimPtr)

      val expectedResult = -1
      assert(result == expectedResult,
             s"result: ${result} != expected: ${expectedResult}")

      val expectedErrno = EINVAL
      assert(errno.errno == expectedErrno,
             s"errno: ${errno.errno} != expected: ${expectedErrno}")
    }

  }

  test("getrlimit(resource, rlim)") {
    Zone { implicit z =>
      // format: off
      val cases = Array(TestInfo("RLIMIT_AS",     RLIMIT_AS),
                        TestInfo("RLIMIT_CORE",   RLIMIT_CORE),
                        TestInfo("RLIMIT_CPU",    RLIMIT_CPU),
                        TestInfo("RLIMIT_DATA",   RLIMIT_DATA),
                        TestInfo("RLIMIT_FSIZE",  RLIMIT_FSIZE),
                        TestInfo("RLIMIT_NOFILE", RLIMIT_NOFILE),
                        TestInfo("RLIMIT_STACK",  RLIMIT_STACK)
                        )
      // format: on

      for (c <- cases) {
        val rlimPtr = alloc[rlimit] // start each pass with all bytes 0.

        val result = getrlimit(c.value, rlimPtr)

        assert(result == 0, s"${c.name} result: ${result} != expected: 0")

        // Coarse grain sanity checks. Do better someday.
        assert(rlimPtr.rlim_cur >= 0.toUInt,
               s"${c.name} rlim_cur: ${rlimPtr.rlim_cur} < 0")

        assert(rlimPtr.rlim_max >= 0.toUInt,
               s"${c.name} rlim_max: ${rlimPtr.rlim_max} < 0")

        assert(rlimPtr.rlim_cur <= rlimPtr.rlim_max,
               s"${c.name} rlim_cur > rlim_max")
      }
    }
  }

  test("getrusage(who, usage) - invalid arg: who") {
    Zone { implicit z =>
      val rusagePtr = alloc[rusage]

      val result = getrusage(Integer.MIN_VALUE, rusagePtr)

      assert(result == -1, s"Unexpected success result: ${result}")

      val expected = EINVAL

      assert(errno.errno == expected,
             s"errno: ${errno.errno} != expected: ${expected}")
    }
  }

  test("getrusage(who, usage) - RUSAGE_SELF") {
    Zone { implicit z =>
      val rusagePtr = alloc[rusage]

      val result = getrusage(RUSAGE_SELF, rusagePtr)

      assert(result == 0, s"result: ${result} != expected: 0")

      assert(rusagePtr.ru_utime.tv_sec >= 0,
             s"unexpected ru_utime.tv_sec: ${rusagePtr.ru_utime.tv_sec} < 0")

      val MICROS_PER_SECOND = 1000 * 1000

      val utUsec = rusagePtr.ru_utime.tv_usec
      assert((utUsec >= 0) && (utUsec < MICROS_PER_SECOND),
             s"unexpected ru_utime: ${rusagePtr.ru_utime.tv_sec} " +
               s"${rusagePtr.ru_utime.tv_usec}")

      val stUsec = rusagePtr.ru_stime.tv_usec
      assert((stUsec >= 0) && (stUsec < MICROS_PER_SECOND),
             s"unexpected ru_stime: ${rusagePtr.ru_utime.tv_sec} " +
               s"${rusagePtr.ru_utime.tv_usec}")
    }
  }

  test("getrusage(who, usage) - RUSAGE_CHILDREN") {
    Zone { implicit z =>
      val rusagePtr = alloc[rusage]

      val result = getrusage(RUSAGE_CHILDREN, rusagePtr)

      assert(result == 0, s"result: ${result} != expected: 0")

      // tv_sec could validly be 0 if either no descendents
      // have been created or descendents were created but
      // all completed quickly.

      assert(rusagePtr.ru_utime.tv_sec >= 0, s"unexpected ru_utime.tv_sec < 0")

      assert(rusagePtr.ru_stime.tv_sec >= 0, s"unexpected ru_stime.tv_sec < 0")
    }
  }

}
