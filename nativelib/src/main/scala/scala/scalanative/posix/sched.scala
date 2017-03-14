package scala.scalanative.posix

import scala.scalanative.native._
import scala.scalanative.posix.sys.types.pid_t
import scala.scalanative.runtime.time.timespec

/**
  * Created by remi on 14/03/17.
  */
@extern
object sched {

  // http://man7.org/linux/man-pages/man7/sched.7.html

  /* Set scheduling parameters for a process.  */
  def sched_setparam (pid: pid_t, param: sched_param): CInt = extern

  /* Retrieve scheduling parameters for a particular process.  */
  def sched_getparam (pid: pid_t, param: sched_param): CInt = extern

  /* Set scheduling algorithm and/or parameters for a process.  */
  def sched_setscheduler (pid: pid_t, policy: CInt, param: sched_param): CInt = extern

  /* Retrieve scheduling algorithm for a particular purpose.  */
  def sched_getscheduler (pid: pid_t): CInt = extern

  /* Yield the processor.  */
  def sched_yield(): CInt = extern

  /* Get maximum priority value for a scheduler.  */
  def sched_get_priority_max (algorithm: CInt): CInt = extern

  /* Get minimum priority value for a scheduler.  */
  def sched_get_priority_min (algorithm: CInt): CInt = extern

  /* Get the SCHED_RR interval for the named process.  */
  def sched_rr_get_interval (pid: pid_t, t: timespec): CInt = extern

  /* Set the CPU affinity for a task */
  def sched_setaffinity (pid: pid_t, cpusetsize: CSize,
    cpuset: Ptr[cpu_set_t]): CInt = extern

  /* Get the CPU affinity for a task */
  def sched_getaffinity (pid: pid_t, cpusetsize: CSize,
    cpuset: Ptr[cpu_set_t]): CInt = extern

  // Types
  type cpu_set_t = CInt
  type sched_param = CStruct1[CInt]

}
