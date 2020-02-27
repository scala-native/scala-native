package scala.scalanative.posix.sys

import scala.scalanative.unsafe._

@extern
object wait {
  type pid_t = CInt

  def wait(stat_loc: Ptr[CInt]): pid_t                          = extern
  def waitpid(pid: pid_t, stat_loc: CInt, options: CInt): pid_t = extern
}
