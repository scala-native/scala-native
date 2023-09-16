package java.lang.process

import scala.scalanative.unsafe._

import scala.scalanative.posix.poll._
import scala.scalanative.posix.signal.sigset_t
import scala.scalanative.posix.time.timespec
import scala.scalanative.posix.sys.types.pid_t

import scalanative.meta.LinktimeInfo.isLinux

object LinuxOsSpecific {
  lazy val _hasPidfdOpen: Boolean = {
    // True when Platform.isLinux & "os.version" >= 5.3.
    if (!isLinux) false
    else if (System.getProperty("os.arch", "unknown") == "arm64") {
      false
    } else {
      // Opportunities abound for simplifying and/or improving this parsing.
      val osVersion = System.getProperty("os.version", "0.0")

      // This parse will start to report bad results at Linux 10.0
      val verdict =
        if (osVersion.isEmpty() ||
            (osVersion(0) <= '4') || (osVersion(0) > '9')) false
        else {
          osVersion match {
            case e if (e(0) > '5') => true
            case e if (e(0) == '5') =>
              !(e.startsWith("5.0") ||
                e.startsWith("5.1") ||
                e.startsWith("5.2"))
            case _ => false
          }
        }
      verdict
    }
  }

  def hasPidfdOpen(): Boolean = _hasPidfdOpen

  @extern
  object Extern {
    @name("scalanative_linux_pidfd_open")
    def pidfd_open(pid: pid_t, flags: CUnsignedInt): CInt = extern

    def ppoll(
        fds: Ptr[struct_pollfd],
        nfds: nfds_t,
        tmo_p: Ptr[timespec],
        sigmask: Ptr[sigset_t]
    ): CInt = extern
  }
}
