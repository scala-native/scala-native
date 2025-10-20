package org.scalanative.testsuite.posixlib

import org.junit.Test
import org.junit.Assert.*

import scala.scalanative.unsafe.*
import scalanative.meta.LinktimeInfo.isWindows

import scalanative.libc.errno as Cerrno

import scalanative.posix.fcntl
import scalanative.posix.sys.stat
import scalanative.posix.unistd

class FcntlTest {

  @Test def openPathnameFlagsExistingFile(): Unit = if (!isWindows) {

    Cerrno.errno = 0
    val fileName = c"/dev/null"

    val fd = fcntl.open(fileName, fcntl.O_RDWR)

    unistd.close(fd)
    assertTrue(s"fd == -1 errno: ${Cerrno.errno}", fd != -1)
  }

  @Test def openPathnameFlagsModeExistingFile(): Unit = if (!isWindows) {

    Cerrno.errno = 0
    val fileName = c"/dev/null"

    val fd = fcntl.open(fileName, fcntl.O_RDWR, stat.S_IRUSR)

    unistd.close(fd)
    assertTrue(s"fd == -1 errno: ${Cerrno.errno}", fd != -1)
  }

}
