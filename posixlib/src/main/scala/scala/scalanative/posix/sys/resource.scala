package scala.scalanative
package posix
package sys

// Reference:
//   The Open Group Base Specifications Issue 7, 2018 edition
//   https://pubs.opengroup.org/onlinepubs/9699919799/basedefs\
//       /sys_resource.h.html
//
//   Method argument names come from Ubuntu 19.04 linux man pages.
//   Open Group seems to no longer suggest them.

import scalanative.unsafe.*

@extern
@define("__SCALANATIVE_POSIX_SYS_RESOURCE")
object resource {

  type id_t = sys.types.id_t

  type rlim_t = CUnsignedLongInt

  type timeval = sys.time.timeval

  type rlimit = CStruct2[
    rlim_t, // rlim_cur
    rlim_t // rlim_max
  ]

  type rusage = CStruct2[
    timeval, // ru_utime
    timeval // ru_stime
  ]

  def getpriority(which: CInt, who: id_t): CInt = extern

  @name("scalanative_getrlimit")
  def getrlimit(resource: CInt, rlim: Ptr[rlimit]): CInt = extern

  @name("scalanative_getrusage")
  def getrusage(who: CInt, usage: Ptr[rusage]): CInt = extern

  def setpriority(which: CInt, who: id_t, prio: CInt): CInt = extern

  @name("scalanative_setrlimit")
  def setrlimit(resource: CInt, rlim: Ptr[rlimit]): CInt = extern

  // Constants
  @name("scalanative_prio_process")
  def PRIO_PROCESS: CInt = extern

  @name("scalanative_prio_pgrp")
  def PRIO_PGRP: CInt = extern

  @name("scalanative_prio_user")
  def PRIO_USER: CInt = extern

  @name("scalanative_rlim_infinity")
  def RLIM_INFINITY: rlim_t = extern

  @name("scalanative_rlim_saved_cur")
  def RLIM_SAVED_CUR: rlim_t = extern

  @name("scalanative_rlim_saved_max")
  def RLIM_SAVED_MAX: rlim_t = extern

  @name("scalanative_rlimit_as")
  def RLIMIT_AS: CInt = extern

  @name("scalanative_rlimit_core")
  def RLIMIT_CORE: CInt = extern

  @name("scalanative_rlimit_cpu")
  def RLIMIT_CPU: CInt = extern

  @name("scalanative_rlimit_data")
  def RLIMIT_DATA: CInt = extern

  @name("scalanative_rlimit_fsize")
  def RLIMIT_FSIZE: CInt = extern

  @name("scalanative_rlimit_nofile")
  def RLIMIT_NOFILE: CInt = extern

  @name("scalanative_rlimit_stack")
  def RLIMIT_STACK: CInt = extern

  @name("scalanative_rusage_children")
  def RUSAGE_CHILDREN: CInt = extern

  @name("scalanative_rusage_self")
  def RUSAGE_SELF: CInt = extern
}

object resourceOps {

  import resource.{rlimit, rlim_t, rusage, timeval}

  implicit class rlimitOps(val ptr: Ptr[rlimit]) extends AnyVal {
    def rlim_cur: rlim_t = ptr._1
    def rlim_max: rlim_t = ptr._2
    def rlim_cur_=(v: rlim_t): Unit = ptr._1 = v
    def rlim_max_=(v: rlim_t): Unit = ptr._2 = v
  }

  implicit class rusageOps(val ptr: Ptr[rusage]) extends AnyVal {
    def ru_utime: timeval = ptr._1
    def ru_stime: timeval = ptr._2
    def ru_utime_=(v: timeval): Unit = ptr._1 = v
    def ru_stime_=(v: timeval): Unit = ptr._2 = v
  }
}
