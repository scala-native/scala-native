package org.scalanative.testsuite.posixlib
package sys

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.sys.time._
import scala.scalanative.posix.sys.timeOps._
import scala.scalanative.unsafe._

class TimeTest {

  @Test def gettimeofdayMatchesCurrentTimeMillis(): Unit =
    if (!LinktimeInfo.isWindows) gettimeofdayTest()

  private def gettimeofdayTest(): Unit = {
    val tv = stackalloc[timeval]()
    val before = System.currentTimeMillis() / 1000L
    assertEquals(0, gettimeofday(tv, null))
    val after = System.currentTimeMillis() / 1000L
    val sec = tv.tv_sec.toLong
    assertTrue(s"$before <= $sec <= $after", before <= sec && sec <= after)
    val usec = tv.tv_usec.toLong
    assertTrue(s"tv_usec $usec", usec >= 0 && usec < 1000000L)
  }
}
