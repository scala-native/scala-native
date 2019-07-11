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

import scalanative.unsafe.{CInt, CStruct2, CUnsignedLongInt, Ptr, name, extern}

@extern
object resource {

  type id_t = sys.types.id_t

  type rlim_t = CUnsignedLongInt

  type timeval = sys.time.timeval

  type rlimit = CStruct2[rlim_t, // rlim_cur
                         rlim_t] // rlim_max

  type rusage = CStruct2[timeval, // ru_utime
                         timeval] // ru_stime

  def getpriority(which: CInt, who: id_t): CInt = extern

  @name("scalanative_getrlimit")
  def getrlimit(resource: CInt, rlim: Ptr[rlimit]): CInt = extern

  @name("scalanative_getrusage")
  def getrusage(who: CInt, usage: Ptr[rusage]): CInt = extern

  def setpriority(which: CInt, who: id_t, prio: CInt): CInt = extern

  @name("scalanative_setrlimit")
  def setrlimit(resource: CInt, rlim: Ptr[rlimit]): CInt = extern

  // Constants

  @name("scalanative_PRIO_PROCESS")
  def PRIO_PROCESS: CInt = extern

  @name("scalanative_PRIO_PGRP")
  def PRIO_PGRP: CInt = extern

  @name("scalanative_PRIO_USER")
  def PRIO_USER: CInt = extern

  @name("scalanative_RLIM_INFINITY")
  def RLIM_INFINITY: rlim_t = extern

  @name("scalanative_RLIM_SAVED_CUR")
  def RLIM_SAVED_CUR: rlim_t = extern

  @name("scalanative_RLIM_SAVED_MAX")
  def RLIM_SAVED_MAX: rlim_t = extern

  @name("scalanative_RLIMIT_AS")
  def RLIMIT_AS: CInt = extern

  @name("scalanative_RLIMIT_CORE")
  def RLIMIT_CORE: CInt = extern

  @name("scalanative_RLIMIT_CPU")
  def RLIMIT_CPU: CInt = extern

  @name("scalanative_RLIMIT_DATA")
  def RLIMIT_DATA: CInt = extern

  @name("scalanative_RLIMIT_FSIZE")
  def RLIMIT_FSIZE: CInt = extern

  @name("scalanative_RLIMIT_NOFILE")
  def RLIMIT_NOFILE: CInt = extern

  @name("scalanative_RLIMIT_STACK")
  def RLIMIT_STACK: CInt = extern

  @name("scalanative_RUSAGE_CHILDREN")
  def RUSAGE_CHILDREN: CInt = extern

  @name("scalanative_RUSAGE_SELF")
  def RUSAGE_SELF: CInt = extern

}

object resourceOps {

  import resource.{rlimit, rlim_t, rusage, timeval}

  implicit class rlimitOps(val ptr: Ptr[rlimit]) extends AnyVal {
    def rlim_cur: rlim_t            = ptr._1
    def rlim_max: rlim_t            = ptr._2
    def rlim_cur_=(v: rlim_t): Unit = ptr._1 = v
    def rlim_max_=(v: rlim_t): Unit = ptr._2 = v
  }

  implicit class rusageOps(val ptr: Ptr[rusage]) extends AnyVal {
    def ru_utime: timeval            = ptr._1
    def ru_stime: timeval            = ptr._2
    def ru_utime_=(v: timeval): Unit = ptr._1 = v
    def ru_stime_=(v: timeval): Unit = ptr._2 = v
  }

}
