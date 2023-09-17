package java.lang.process

import scala.scalanative.unsafe._

import scala.scalanative.posix.poll._
import scala.scalanative.posix.signal.sigset_t
import scala.scalanative.posix.time.timespec
import scala.scalanative.posix.sys.types.pid_t

import scalanative.meta.LinktimeInfo.isLinux

object LinuxOsSpecific {
  lazy val _hasPidfdOpen: Boolean =
    if (!isLinux) false
    else Extern.linux_has_pidfd_open()

  def hasPidfdOpen(): Boolean = _hasPidfdOpen

  @extern
  object Extern {
    @name("scalanative_linux_has_pidfd_open")
    def linux_has_pidfd_open(): CBool = extern

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
