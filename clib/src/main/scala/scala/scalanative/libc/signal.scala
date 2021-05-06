package scala.scalanative
package libc

import scalanative.unsafe._

@extern
object signal {

  // Signals
  @deprecated("Use kill from posix signal", "libc 0.4.1")
  def kill(pid: CInt, sig: CInt): CInt = extern
  def signal(sig: CInt, handler: CFuncPtr1[CInt, Unit]): CFuncPtr1[CInt, Unit] =
    extern
  def raise(sig: CInt): CInt = extern

  // Macros

  @name("scalanative_sig_dfl")
  def SIG_DFL: CFuncPtr1[CInt, Unit] = extern
  @name("scalanative_sig_ign")
  def SIG_IGN: CFuncPtr1[CInt, Unit] = extern
  @name("scalanative_sig_err")
  def SIG_ERR: CFuncPtr1[CInt, Unit] = extern
  @name("scalanative_sigabrt")
  def SIGABRT: CInt = extern
  @name("scalanative_sigfpe")
  def SIGFPE: CInt = extern
  @name("scalanative_sigill")
  def SIGILL: CInt = extern
  @name("scalanative_sigint")
  def SIGINT: CInt = extern
  @name("scalanative_sigsegv")
  def SIGSEGV: CInt = extern
  @name("scalanative_sigterm")
  def SIGTERM: CInt = extern
  @deprecated("Use SIGUSR1 from posix signal", "libc 0.4.1")
  @name("scalanative_sigusr1")
  def SIGUSR1: CInt = extern
}
