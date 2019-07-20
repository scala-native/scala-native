package scala.scalanative.posix.sys

import scala.scalanative.unsafe._
import scalanative.posix.sys.types.pid_t

@extern
object wait {
  @name("wait")
  def _wait(status: Ptr[CInt]): pid_t                             = extern
  def waitpid(pid: pid_t, status: Ptr[CInt], options: CInt): CInt = extern
}
