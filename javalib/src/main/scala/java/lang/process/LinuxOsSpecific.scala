package java.lang.process

import scala.scalanative.posix.poll._
import scala.scalanative.posix.signal.sigset_t
import scala.scalanative.posix.sys.types.pid_t
import scala.scalanative.posix.time.timespec
import scala.scalanative.runtime._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scalanative.meta.LinktimeInfo.isLinux

object LinuxOsSpecific {
  private lazy val _hasPidfdOpen: Boolean =
    if (!isLinux) false
    else Extern.linux_has_pidfd_open()

  def hasPidfdOpen(): Boolean = _hasPidfdOpen

  final val EPOLLIN = 0x001

  final val EPOLL_CTL_ADD = 1 // Add a file descriptor to the interface.
  final val EPOLL_CTL_DEL = 2 // Remove a file descriptor from the interface.
  final val EPOLL_CTL_MOD = 3 // Change file descriptor epoll_event structure.

  // scalafmt: { align.preset = more }

  type epoll_data_t = CStruct1[Long]
  type epoll_event  = CStruct2[UInt, epoll_data_t]

  implicit class epollDataTOps(val ref: epoll_data_t) extends AnyVal {
    def ptr = ref._1.toPtr[Byte]
    def fd  = ref._1.toInt
    def u32 = ref._1.toUInt
    def u64 = ref._1.toULong

    def ptr_=(v: CVoidPtr): Unit = ref._1 = v.toLong
    def fd_=(v: CInt): Unit      = ref._1 = v.toLong
    def u32_=(v: UInt): Unit     = ref._1 = v.toLong
    def u64_=(v: ULong): Unit    = ref._1 = v.toLong
  }

  implicit class epollEventOps(val ptr: Ptr[epoll_event]) extends AnyVal {
    def events = ptr._1
    def data   = ptr._2

    def events_=(v: UInt): Unit       = ptr._1 = v
    def data_=(v: epoll_data_t): Unit = ptr._2 = v
  }

  // scalafmt: { align.preset = none }

  @extern
  @define("__SCALANATIVE_JAVALIB_SYS_LINUX_SYSCALL")
  object Extern {
    @name("scalanative_linux_has_pidfd_open")
    def linux_has_pidfd_open(): CBool = extern

    @name("scalanative_linux_pidfd_open")
    def pidfd_open(pid: pid_t, flags: CUnsignedInt): CInt = extern

    @blocking def ppoll(
        fds: Ptr[struct_pollfd],
        nfds: nfds_t,
        tmo_p: Ptr[timespec],
        sigmask: Ptr[sigset_t]
    ): CInt = extern

    def epoll_create(size: CInt): CInt = extern
    def epoll_create1(flags: CInt): CInt = extern

    def epoll_ctl(
        epfd: CInt,
        op: CInt,
        fd: CInt,
        event: Ptr[epoll_event]
    ): CInt = extern

    def epoll_wait(
        epfd: CInt,
        events: Ptr[epoll_event],
        maxevents: CInt,
        timeoutMillis: CInt
    ): CInt = extern

    def eventfd(initval: UInt, flags: CInt): CInt = extern

  }
}
