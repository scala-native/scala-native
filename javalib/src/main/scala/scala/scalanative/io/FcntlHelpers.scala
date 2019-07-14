package scala.scalanative.io

import scalanative.libc.{errno, string}
import scalanative.posix.fcntl
import scalanative.posix.sys.stat.mode_t
import scalanative.unsafe.{CInt, fromCString, toCString, Zone}

import java.io.{FileNotFoundException}

object FcntlHelpers {

  def checkedOpen(pathname: String, flags: CInt, mode: mode_t): CInt =
    Zone { implicit z =>
      val fd = fcntl.open(toCString(pathname), flags, mode)

      if (fd == -1) {
        throw new FileNotFoundException(
          s"${pathname} (${fromCString(string.strerror(errno.errno))})")
      }

      fd
    }
}
