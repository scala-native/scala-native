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

  // Checks that constants are accessible to the linker and at run time.
  //
  // The whole point of dynamically fetching a constant is that
  // its specific value  is likely to vary across operating systems.
  // It may not have a fixed expected value but it is a reasonable
  // expectation that the value is not zero.

  test(s"AT_FDCWD") {
    val value = fcntl.AT_FDCWD

    assert(value != 0, s"AT_FDCWD has unexpected value 0")
  }

}
