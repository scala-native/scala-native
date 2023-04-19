package scala.scalanative
package posix
package sys

import scalanative.unsafe._

/** POSIX wait.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 *
 *  A method with an XSI comment indicates it is defined in extended POSIX
 *  X/Open System Interfaces, not base POSIX.
 *
 *  Note well: It is neither expect nor obvious from the declaration that the
 *  wait() method of this class can conflict with Object.wait(Long). This makes
 *  declaration and usage more difficult.
 *
 *  The simplest approach is to avoid "wait(Ptr[CInt])" and use the directly
 *  equivalent idiom: // import scala.scalanative.posix.sys.wait.waitpid // or
 *  sys.wait._ // Replace Ptr[CInt] with your variable. val status = waitpid(-1,
 *  Ptr[CInt], 0)
 *
 *  If that approach is not available, one can try the following idiom: //
 *  import scalanative.posix.sys.{wait => Wait} // import
 *  scalanative.posix.sys.wait._ // for WIFEXITED etc. // Replace Ptr[CInt] with
 *  your variable. val status = Wait.wait(Ptr[CInt])
 */
@extern
object wait {
  type id_t = types.id_t
  type pid_t = types.pid_t

  type sigval = signal.sigval
  type siginfo_t = signal.siginfo_t

  /* The type idtype_t shall be defined as an enumeration type whose possible
   * values shall include at least the following: P_ALL P_PGID P_PID
   */
  type idtype_t = CInt // POSIX enumeration in simple Scala common to 2.n & 3.n
  @name("scalanative_c_p_all")
  def P_ALL: CInt = extern

  @name("scalanative_c_p_pgid")
  def P_PGID: CInt = extern

  @name("scalanative_c_p_pid")
  def P_PID: CInt = extern

// Symbolic constants, roughly in POSIX declaration order

  // "constants" for waitpid() options

  /** XSI
   */
  @name("scalanative_c_wcontinued")
  def WCONTINUED: CInt = extern

  @name("scalanative_c_wnohang")
  def WNOHANG: CInt = extern

  @name("scalanative_c_wuntraced")
  def WUNTRACED: CInt = extern

  // "constants" for waitid()
  @name("scalanative_c_wexited")
  def WEXITED: CInt = extern

  @name("scalanative_c_wnowait")
  def WNOWAIT: CInt = extern

  @name("scalanative_c_wstopped")
  def WSTOPPED: CInt = extern

// POSIX "Macros"
  @name("scalanative_c_wexitstatus")
  def WEXITSTATUS(wstatus: CInt): CInt = extern

  /** XSI
   */
  @name("scalanative_c_wifcontinued")
  def WIFCONTINUED(wstatus: CInt): CInt = extern

  @name("scalanative_c_wifexited")
  def WIFEXITED(wstatus: CInt): Boolean = extern

  @name("scalanative_c_wifsignaled")
  def WIFSIGNALED(wstatus: CInt): Boolean = extern

  @name("scalanative_c_wifstopped")
  def WIFSTOPPED(wstatus: CInt): Boolean = extern

  @name("scalanative_c_wstopsig")
  def WSTOPSIG(wstatus: CInt): Boolean = extern

  @name("scalanative_c_wtermsig")
  def WTERMSIG(wstatus: CInt): CInt = extern

// Methods

  /** See declaration & usage note in class description.
   */
  @blocking
  def wait(status: Ptr[CInt]): pid_t = extern

  @blocking
  def waitid(
      idtype: idtype_t,
      id: id_t,
      status: Ptr[CInt],
      options: CInt
  ): CInt = extern

  @blocking
  def waitpid(pid: pid_t, status: Ptr[CInt], options: CInt): pid_t = extern

}
