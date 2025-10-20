package java.lang

import scala.scalanative.unsafe.*

@define("__SCALANATIVE_JAVALIB_SYS_LINUX_SCHED_H")
@extern
object RuntimeLinuxOsSpecific {

  // @blocking considered but not used.
  @name("scalanative_sched_cpuset_cardinality")
  def sched_cpuset_cardinality(): CInt = extern
}
