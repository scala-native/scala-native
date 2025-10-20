package scala.scalanative.posix

import scala.scalanative.unsafe.*
import scala.scalanative.posix.time.timespec
import scala.scalanative.posix.sys.types.pid_t

@extern
@define("__SCALANATIVE_POSIX_SCHED")
object sched {

  def sched_setparam(pid: pid_t, param: Ptr[sched_param]): CInt = extern

  def sched_getparam(pid: pid_t, param: Ptr[sched_param]): CInt = extern

  def sched_setscheduler(
      pid: pid_t,
      policy: CInt,
      param: Ptr[sched_param]
  ): CInt =
    extern

  def sched_getscheduler(pid: pid_t): CInt = extern

  @blocking
  def sched_yield(): CInt = extern

  def sched_get_priority_max(algorithm: CInt): CInt = extern

  def sched_get_priority_min(algorithm: CInt): CInt = extern

  def sched_rr_get_interval(pid: pid_t, t: Ptr[timespec]): CInt = extern

  def sched_setaffinity(
      pid: pid_t,
      cpusetsize: CSize,
      cpuset: Ptr[cpu_set_t]
  ): CInt = extern

  def sched_getaffinity(
      pid: pid_t,
      cpusetsize: CSize,
      cpuset: Ptr[cpu_set_t]
  ): CInt = extern

  @name("scalanative_sched_other")
  def SCHED_OTHER: CInt = extern

  @name("scalanative_sched_fifo")
  def SCHED_FIFO: CInt = extern

  @name("scalanative_sched_rr")
  def SCHED_RR: CInt = extern

  @name("scalanative_sched_sporadic")
  def SCHED_SPORADIC: CInt = extern

  /** Not defined in POSIX standard, might lead to runtime errors */
  @name("scalanative_sched_batch")
  def SCHED_BATCH: CInt = extern

  /** Not defined in POSIX standard, might lead to runtime errors */
  @name("scalanative_sched_idle")
  def SCHED_IDLE: CInt = extern

  /** Not defined in POSIX standard, might lead to runtime errors */
  @name("scalanative_sched_deadline")
  def SCHED_DEADLINE: CInt = extern

  // Types
  type cpu_set_t = CInt

  type sched_param = CStruct5[CInt, CInt, timespec, timespec, CInt]

}

object schedOps {
  import sched.*
  implicit class SchedParamOps(ref: Ptr[sched_param]) {
    def priority: CInt = ref._1
    def priority_=(value: CInt): Unit = ref._1 = value
  }
}
