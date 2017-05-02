package scala.scalanative
package native

import scala.scalanative.posix.sys.types.pid_t

@extern
object signal {

  // Signals

  def signal(sig: CInt,
             handler: CFunctionPtr1[CInt, Unit]): CFunctionPtr1[CInt, Unit] =
    extern
  def raise(sig: CInt): CInt = extern

  // Type
  type sigval = Unit // Union[2]
  type sigevent =
    CStruct6[CInt, CInt, sigval, CFunctionPtr1[sigval, Unit], Ptr[Byte], pid_t]

  // Macros

  @name("scalanative_libc_sig_dfl")
  def SIG_DFL: CFunctionPtr1[CInt, Unit] = extern
  @name("scalanative_libc_sig_ign")
  def SIG_IGN: CFunctionPtr1[CInt, Unit] = extern
  @name("scalanative_libc_sig_err")
  def SIG_ERR: CFunctionPtr1[CInt, Unit] = extern
  @name("scalanative_libc_sigabrt")
  def SIGABRT: CInt = extern
  @name("scalanative_libc_sigfpe")
  def SIGFPE: CInt = extern
  @name("scalanative_libc_sigill")
  def SIGILL: CInt = extern
  @name("scalanative_libc_sigint")
  def SIGINT: CInt = extern
  @name("scalanative_libc_sigsegv")
  def SIGSEGV: CInt = extern
  @name("scalanative_libc_sigterm")
  def SIGTERM: CInt = extern
}
