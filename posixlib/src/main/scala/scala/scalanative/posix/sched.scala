package scala.scalanative.posix

import scala.scalanative.native.{CInt, CSize, CStruct1, CStruct5, Ptr, extern}
import scala.scalanative.posix.time.timespec
import scala.scalanative.posix.sys.types.pid_t

@extern
object sched {

  def sched_setparam(pid: pid_t, param: Ptr[sched_param]): CInt = extern

  def sched_getparam(pid: pid_t, param: Ptr[sched_param]): CInt = extern

  def sched_setscheduler(pid: pid_t,
                         policy: CInt,
                         param: Ptr[sched_param]): CInt =
    extern

  def sched_getscheduler(pid: pid_t): CInt = extern

  def sched_yield(): CInt = extern

  def sched_get_priority_max(algorithm: CInt): CInt = extern

  def sched_get_priority_min(algorithm: CInt): CInt = extern

  def sched_rr_get_interval(pid: pid_t, t: Ptr[timespec]): CInt = extern

  def sched_setaffinity(pid: pid_t,
                        cpusetsize: CSize,
                        cpuset: Ptr[cpu_set_t]): CInt = extern

  def sched_getaffinity(pid: pid_t,
                        cpusetsize: CSize,
                        cpuset: Ptr[cpu_set_t]): CInt = extern

  // Types
  type cpu_set_t = CInt

  type sched_param = CStruct5[CInt, CInt, timespec, timespec, CInt]

}
