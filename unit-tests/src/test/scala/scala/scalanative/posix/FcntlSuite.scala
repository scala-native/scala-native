package scala.scalanative.posix

import scala.scalanative.unsafe._

import scalanative.libc.{errno => Cerrno}

import scalanative.posix.sys.stat

object FcntlSuite extends tests.Suite {

  test(s"open(pathname, flags) - existing file") {

    Cerrno.errno = 0
    val fileName = c"/dev/null"

    val fd = fcntl.open(fileName, fcntl.O_RDWR)

    unistd.close(fd)
    assert(fd != -1, s"fd == -1 errno: ${Cerrno.errno}")
  }

  test(s"open(pathname, flags, mode) - existing file") {

    Cerrno.errno = 0
    val fileName = c"/dev/null"

    val fd = fcntl.open(fileName, fcntl.O_RDWR, stat.S_IRUSR)

    unistd.close(fd)
    assert(fd != -1, s"fd == -1 errno: ${Cerrno.errno}")
  }

}
