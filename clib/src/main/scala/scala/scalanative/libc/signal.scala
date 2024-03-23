package scala.scalanative
package libc

import scalanative.unsafe._

@extern object signal extends signal

@extern
@define("__SCALANATIVE_POSIX_SIGNAL")
private[scalanative] trait signal {

  // Signals
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

}
