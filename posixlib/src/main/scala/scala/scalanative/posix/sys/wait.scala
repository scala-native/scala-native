package scala.scalanative
package posix
package sys

import scalanative.unsafe._

import scalanative.posix.signal

/** POSIX wait.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 *
 *  A method with an XSI comment indicates it is defined in extended POSIX
 *  X/Open System Interfaces, not base POSIX.
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
  type idtype_t = Int // POSIX enumeration in simple Scala common to 2.n & 3.n
  @name("scalanative_posix_p_all")
  def P_ALL: CInt = extern

  @name("scalanative_posix_p_pgid")
  def P_PGID: CInt = extern

  @name("scalanative_posix_p_pid")
  def P_PID: CInt = extern

// Symbolic constants, roughly in POSIX declaration order

  // "constants" for waitpid() options

  /** XSI
   */
  @name("scalanative_posix_wcontinued")
  def WCONTINUED: CInt = extern

  @name("scalanative_posix_wnohang")
  def WNOHANG: CInt = extern

  @name("scalanative_posix_wuntraced")
  def WUNTRACED: CInt = extern

  // "constants" for waitid()
  @name("scalanative_posix_wexited")
  def WEXITED: CInt = extern

  @name("scalanative_posix_wnowait")
  def WNOWAIT: CInt = extern

  @name("scalanative_posix_wstopped")
  def WSTOPPED: CInt = extern

// POSIX "Macros"
  @name("scalanative_posix_wexitstatus")
  def WEXITSTATUS(wstatus: CInt): CInt = extern

  /** XSI
   */
  @name("scalanative_posix_wifcontinued")
  def WIFCONTINUED(wstatus: CInt): CInt = extern

  @name("scalanative_posix_wifexited")
  def WIFEXITED(wstatus: CInt): Boolean = extern

  @name("scalanative_posix_wifsignaled")
  def WIFSIGNALED(wstatus: CInt): Boolean = extern

  @name("scalanative_posix_wifstopped")
  def WIFSTOPPED(wstatus: CInt): Boolean = extern

  @name("scalanative_posix_wstopsig")
  def WSTOPSIG(wstatus: CInt): Boolean = extern

  @name("scalanative_posix_wtermsig")
  def WTERMSIG(wstatus: CInt): CInt = extern

// Methods

  def wait(status: Ptr[CInt]): pid_t = extern

  def waitid(
      idtype: idtype_t,
      id: id_t,
      status: Ptr[CInt],
      options: CInt
  ): CInt = extern

  def waitpid(pid: pid_t, status: Ptr[CInt], options: CInt): pid_t = extern

}
