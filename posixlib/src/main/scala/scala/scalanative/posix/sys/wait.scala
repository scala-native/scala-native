//POSIX.1-2017: https://pubs.opengroup.org/onlinepubs/9699919799/
package scala.scalanative.posix.sys

import scala.scalanative.unsafe._
import scala.scalanative.posix.sys.types.pid_t

@extern
object wait {
  //XSI
  @name("scala_native_wait_WCONTINUED")
  val WCONTINUED: CInt = extern

  @name("scala_native_wait_WNOHANG")
  val WNOHANG: CInt = extern

  @name("scala_native_wait_WUNTRACED")
  val WUNTRACED: CInt = extern

  @name("scala_native_wait_WIFEXITED")
  def WIFEXITED(statVal: CInt): CInt = extern

  @name("scala_native_wait_WEXITSTATUS")
  def WEXITSTATUS(statVal: CInt): CInt = extern

  @name("scala_native_wait_WIFSIGNALED")
  def WIFSIGNALED(statVal: CInt): CInt = extern

  @name("scala_native_wait_WTERMSIG")
  def WTERMSIG(statVal: CInt): CInt = extern

  @name("scala_native_wait_WIFSTOPPED")
  def WIFSTOPPED(statVal: CInt): CInt = extern

  @name("scala_native_wait_WSTOPSIG")
  def WSTOPSIG(statVal: CInt): CInt = extern

  //XSI
  @name("scala_native_wait_WIFCONTINUED")
  def WIFCONTINUED(statVal: CInt): CInt = extern

  //renamed to avoid shadowing by with Object.wait
  @name("wait")
  def waitpid(statLoc: Ptr[CInt]): pid_t = extern

  def waitpid(pid: pid_t, statLoc: Ptr[CInt], options: CInt): pid_t = extern
}
