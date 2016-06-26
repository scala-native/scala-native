package scala.scalanative
package native

@extern
object signal {

  // Signals

  def signal(
      sig: CInt, handler: FunctionPtr1[CInt, Unit]): FunctionPtr1[CInt, Unit] =
    extern
  def raise(sig: CInt): CInt = extern

  // Types

  @struct class sig_atomic_t private ()

  // Macros

  @name("scalanative_libc_sig_dfl")
  def SIG_DFL: FunctionPtr1[CInt, Unit] = extern
  @name("scalanative_libc_sig_ign")
  def SIG_IGN: FunctionPtr1[CInt, Unit] = extern
  @name("scalanative_libc_sig_err")
  def SIG_ERR: FunctionPtr1[CInt, Unit] = extern
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
