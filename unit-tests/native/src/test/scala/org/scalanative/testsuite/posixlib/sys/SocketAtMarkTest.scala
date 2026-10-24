package org.scalanative.testsuite.posixlib
package sys

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import scala.scalanative.meta.LinktimeInfo.isWindows
import scala.scalanative.posix.errno._
import scala.scalanative.posix.sys.socket._
import scala.scalanative.posix.unistd.close

class SocketAtMarkTest {
  @Test def noUrgentData(): Unit = {
    assumeFalse("sockatmark is not supported on Windows", isWindows)
    if (!isWindows) {
      val fd = socket(AF_INET, SOCK_STREAM, 0)
      assertTrue(s"socket failed: $errno", fd >= 0)
      try assertEquals(0, sockatmark(fd))
      finally close(fd)
    }
  }

  @Test def invalidDescriptor(): Unit = {
    assumeFalse("sockatmark is not supported on Windows", isWindows)
    if (!isWindows) {
      assertEquals(-1, sockatmark(-1))
      assertEquals(EBADF, errno)
    }
  }
}
